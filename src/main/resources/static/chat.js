/**
 * ChatWave — WebSocket Chat Client
 * Handles STOMP/SockJS connection, message rendering, image upload,
 * and real-time typing indicators.
 */

let stompClient = null;
let pendingImageUrl = null;

// Typing state
let typingTimeout = null;
let isTyping = false;
const TYPING_DEBOUNCE_MS = 2500; // stop signal sent after 2.5s of no keystrokes

// Tracks which remote users are currently typing: Map<userId, timeoutId>
const remoteTypers = new Map();

// ─── Init ───────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    renderInitialMessages();
    connectWebSocket();

    const input = document.getElementById('messageInput');

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
            return;
        }
        handleTypingInput();
    });

    // Also handle paste / autocomplete changes
    input.addEventListener('input', handleTypingInput);

    scrollToBottom();
});

// ─── WebSocket Connection ────────────────────────────────────────────────────

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        onConnect: () => {
            setStatus('Connected', true);
            stompClient.subscribe(`/topic/chat/${ROOM_ID}`, onMessageReceived);
            stompClient.subscribe(`/topic/chat/${ROOM_ID}/typing`, onTypingReceived);
        },
        onDisconnect: () => setStatus('Disconnected', false),
        onStompError: (frame) => {
            console.error('STOMP error', frame);
            setStatus('Connection error', false);
        }
    });
    stompClient.activate();
}

function setStatus(text, connected) {
    const el = document.getElementById('connectionStatus');
    if (!el) return;
    el.textContent = connected ? '● ' + text : '○ ' + text;
    el.className = 'chat-header-status' + (connected ? ' connected' : '');
}

// ─── Typing — outbound ───────────────────────────────────────────────────────

function handleTypingInput() {
    if (!stompClient || !stompClient.connected) return;

    const hasText = document.getElementById('messageInput').value.length > 0;

    if (hasText && !isTyping) {
        isTyping = true;
        publishTyping(true);
    }

    // Reset debounce timer on every keystroke
    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        if (isTyping) {
            isTyping = false;
            publishTyping(false);
        }
    }, TYPING_DEBOUNCE_MS);
}

function publishTyping(typing) {
    if (!stompClient || !stompClient.connected) return;
    stompClient.publish({
        destination: `/app/chat/${ROOM_ID}/typing`,
        body: JSON.stringify({ typing })
    });
}

function stopTyping() {
    clearTimeout(typingTimeout);
    if (isTyping) {
        isTyping = false;
        publishTyping(false);
    }
}

// ─── Typing — inbound ────────────────────────────────────────────────────────

function onTypingReceived(frame) {
    const event = JSON.parse(frame.body);

    // Ignore our own typing events
    if (event.userId === CURRENT_USER_ID) return;

    if (event.typing) {
        // Clear any existing auto-hide timer for this user
        if (remoteTypers.has(event.userId)) {
            clearTimeout(remoteTypers.get(event.userId));
        }

        // Auto-hide after 4s as a safety net if stop signal is lost
        const autoHide = setTimeout(() => {
            remoteTypers.delete(event.userId);
            updateTypingUI();
        }, 4000);

        remoteTypers.set(event.userId, autoHide);
    } else {
        if (remoteTypers.has(event.userId)) {
            clearTimeout(remoteTypers.get(event.userId));
            remoteTypers.delete(event.userId);
        }
    }

    updateTypingUI();
}

function updateTypingUI() {
    const bar = document.getElementById('typingIndicatorBar');
    const label = document.getElementById('typingLabel');

    if (remoteTypers.size === 0) {
        bar.style.display = 'none';
        return;
    }

    label.textContent = remoteTypers.size === 1
        ? 'typing…'
        : `${remoteTypers.size} people typing…`;

    bar.style.display = 'flex';
    scrollToBottom();
}

// ─── Send Message ────────────────────────────────────────────────────────────

function sendMessage() {
    if (!stompClient || !stompClient.connected) {
        alert('Not connected. Please wait or refresh.');
        return;
    }

    if (pendingImageUrl) {
        stompClient.publish({
            destination: `/app/chat/${ROOM_ID}/send`,
            body: JSON.stringify({ type: 'IMAGE', imageUrl: pendingImageUrl })
        });
        stopTyping();
        clearImagePreview();
        return;
    }

    const input = document.getElementById('messageInput');
    const text = input.value.trim();
    if (!text) return;

    stompClient.publish({
        destination: `/app/chat/${ROOM_ID}/send`,
        body: JSON.stringify({ type: 'TEXT', content: text })
    });

    input.value = '';
    stopTyping();
}

// ─── Receive Message ─────────────────────────────────────────────────────────

function onMessageReceived(frame) {
    const message = JSON.parse(frame.body);

    // When a message arrives from the remote user, immediately hide their typing indicator
    if (message.senderId !== CURRENT_USER_ID) {
        if (remoteTypers.has(message.senderId)) {
            clearTimeout(remoteTypers.get(message.senderId));
            remoteTypers.delete(message.senderId);
            updateTypingUI();
        }
    }

    appendMessage(message);
    scrollToBottom();
}

// ─── Render Messages ─────────────────────────────────────────────────────────

function renderInitialMessages() {
    const area = document.getElementById('messagesArea');
    area.innerHTML = '';

    if (!INITIAL_MESSAGES || INITIAL_MESSAGES.length === 0) {
        area.innerHTML = '<div class="date-separator">Start of conversation</div>';
        return;
    }

    area.innerHTML = '<div class="date-separator">Chat history</div>';
    INITIAL_MESSAGES.forEach(msg => appendMessage(msg, false));
}

function appendMessage(msg, animate = true) {
    const area = document.getElementById('messagesArea');
    const isMine = msg.senderId === CURRENT_USER_ID;

    const row = document.createElement('div');
    row.className = `message-row ${isMine ? 'mine' : 'theirs'}`;
    if (!animate) row.style.animation = 'none';

    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';

    if (msg.type === 'IMAGE' && msg.imageUrl) {
        const img = document.createElement('img');
        img.src = msg.imageUrl;
        img.alt = 'Image';
        img.loading = 'lazy';
        img.onclick = () => openLightbox(msg.imageUrl);
        bubble.appendChild(img);
    } else {
        bubble.textContent = msg.content;
    }

    const meta = document.createElement('div');
    meta.className = 'message-meta';
    const time = msg.sentAt ? formatTime(msg.sentAt) : 'now';
    meta.textContent = isMine ? time : `${msg.senderUsername} · ${time}`;

    row.appendChild(bubble);
    row.appendChild(meta);
    area.appendChild(row);
}

function formatTime(sentAt) {
    let date;
    if (Array.isArray(sentAt)) {
        date = new Date(sentAt[0], sentAt[1] - 1, sentAt[2],
                        sentAt[3] || 0, sentAt[4] || 0, sentAt[5] || 0);
    } else {
        date = new Date(sentAt);
    }
    if (isNaN(date.getTime())) return '';
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function scrollToBottom() {
    const area = document.getElementById('messagesArea');
    if (area) area.scrollTop = area.scrollHeight;
}

// ─── Image Handling ───────────────────────────────────────────────────────────

async function handleImageSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
        alert('File is too large. Maximum size is 10MB.');
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        document.getElementById('imagePreviewImg').src = e.target.result;
        document.getElementById('imagePreviewBar').style.display = 'flex';
    };
    reader.readAsDataURL(file);

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/upload-image', {
            method: 'POST',
            body: formData
        });
        if (!response.ok) throw new Error('Upload failed');
        const data = await response.json();
        pendingImageUrl = data.url;
    } catch (err) {
        console.error('Image upload failed:', err);
        alert('Failed to upload image. Please try again.');
        clearImagePreview();
    }

    event.target.value = '';
}

function clearImagePreview() {
    pendingImageUrl = null;
    document.getElementById('imagePreviewBar').style.display = 'none';
    document.getElementById('imagePreviewImg').src = '';
}

// ─── Lightbox ─────────────────────────────────────────────────────────────────

function openLightbox(url) {
    const overlay = document.createElement('div');
    overlay.className = 'lightbox-overlay';
    overlay.onclick = () => document.body.removeChild(overlay);

    const img = document.createElement('img');
    img.src = url;
    img.alt = 'Full size image';

    overlay.appendChild(img);
    document.body.appendChild(overlay);
}