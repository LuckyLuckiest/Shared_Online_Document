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

socket.addEventListener("close", () => {
    stopCursorUpdates();
});

socket.addEventListener("message", (event) => {
    const data = JSON.parse(event.data);

    if (data.userId === userId) return;

    messageQueue.push(data);
    processQueue();
});

editor.addEventListener("mouseup", sendCursorUpdate);
editor.addEventListener("keyup", (e) => {
    if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown"].includes(e.key)) {
        sendCursorUpdate();
    }
});

function startCursorUpdates() {
    if (cursorUpdateInterval) return;

    cursorUpdateInterval = setInterval(() => {
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
    }, 300);
}

function stopCursorUpdates() {
    if (cursorUpdateInterval) {
        clearInterval(cursorUpdateInterval);
        cursorUpdateInterval = null;
    }
}
