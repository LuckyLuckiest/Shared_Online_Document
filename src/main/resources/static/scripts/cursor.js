const edit = document.getElementById("editor");
let cursorUpdateInterval = null;
const cursors = {};

socket.addEventListener("open", () => {
    socket.send(JSON.stringify({
        type: "init",
        sessionId,
        username,
        userColor
    }));
    startCursorUpdates();
});

socket.addEventListener("message", (event) => {
    const message = JSON.parse(event.data);

    if (message.type === "cursor-update") {
        updateRemoteCursor(message);
    } else if (message.type === "user-left") {
        const { userId } = message;
        const cursorElement = cursors[userId];
        if (cursorElement) {
            cursorElement.remove();
            delete cursors[userId];
        }
    }

});

socket.addEventListener("close", () => {
    stopCursorUpdates();
});

edit.addEventListener("mouseup", sendCursorUpdate);
edit.addEventListener("keyup", (e) => {
    if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown"].includes(e.key)) {
        sendCursorUpdate();
    }
});

function startCursorUpdates() {
    if (cursorUpdateInterval) return;
    cursorUpdateInterval = setInterval(sendCursorUpdate, 300);
}

function stopCursorUpdates() {
    if (cursorUpdateInterval) {
        clearInterval(cursorUpdateInterval);
        cursorUpdateInterval = null;
    }
}

function sendCursorUpdate() {
    const selection = window.getSelection();
    if (!selection || !selection.anchorNode) return;

    const start = getCursorPosition(selection.anchorNode, selection.anchorOffset);
    const end = getCursorPosition(selection.focusNode, selection.focusOffset);

    socket.send(JSON.stringify({
        type: "cursor-update",
        userId,
        userColor,
        username,
        sessionId,
        start,
        end
    }));
}

function getCursorPosition(node, offset) {
    let position = 0;
    const walker = document.createTreeWalker(edit, NodeFilter.SHOW_TEXT, null, false);

    while (walker.nextNode()) {
        const currentNode = walker.currentNode;
        if (currentNode === node) {
            return position + offset;
        }
        position += currentNode.textContent.length;
    }

    return position;
}

function updateRemoteCursor(data) {
    const { userId, userColor, username, start, end } = data;

    let cursorElement = cursors[userId];

    if (!cursorElement) {
        cursorElement = document.createElement("div");
        cursorElement.classList.add("remote-cursor");
        cursorElement.style.position = "absolute";
        cursorElement.style.width = "2px";
        cursorElement.style.zIndex = 10;

        cursors[userId] = cursorElement;
        edit.appendChild(cursorElement);
    }

    // Always update dynamic properties:
    cursorElement.style.backgroundColor = userColor;
    cursorElement.title = username;

    const position = getDomPositionFromOffset(start);
    if (position) {
        cursorElement.style.left = position.x + "px";
        cursorElement.style.top = position.y + "px";
        cursorElement.style.height = "1em";
    }
}

function getDomPositionFromOffset(offset) {
    const range = document.createRange();
    const walker = document.createTreeWalker(edit, NodeFilter.SHOW_TEXT, null, false);

    let currentOffset = 0;
    let node;

    while (walker.nextNode()) {
        node = walker.currentNode;
        const length = node.textContent.length;

        if (currentOffset + length >= offset) {
            const withinNodeOffset = offset - currentOffset;
            range.setStart(node, withinNodeOffset);
            range.collapse(true);

            const rect = range.getBoundingClientRect();
            const editorRect = edit.getBoundingClientRect();

            return {
                x: rect.left - editorRect.left + edit.scrollLeft,
                y: rect.top - editorRect.top + edit.scrollTop
            };
        }

        currentOffset += length;
    }

    return null;
}
