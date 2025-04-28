const messageQueue = [];
let isProcessing = false;

function generateDiff(oldText, newText) {
    let start = 0;
    while (start < oldText.length && start < newText.length && oldText[start] === newText[start]) {
        start++;
    }

    let oldEnd = oldText.length - 1;
    let newEnd = newText.length - 1;
    while (oldEnd >= start && newEnd >= start && oldText[oldEnd] === newText[newEnd]) {
        oldEnd--;
        newEnd--;
    }

    return {
        start,
        end: oldEnd + 1,
        inserted: newText.slice(start, newEnd + 1)
    };
}

function applyDiffToEditor(diff) {
    const value = editor.innerHTML;
    editor.innerHTML = value.slice(0, diff.start) + diff.inserted + value.slice(diff.end);
}

function processQueue() {
    if (isProcessing) return;
    if (messageQueue.length === 0) return;

    isProcessing = true;
    const data = messageQueue.shift();

    if (data.type === "update") {
        selfChange = true;
        applyDiffToEditor(data);
        showRemoteCursor(data.userId, data.cursor, data.userColor, data.username);
        lastValue = editor.innerHTML;
    } else if (data.type === "cursor-update") {
        showRemoteCursor(data.userId, data.start, data.userColor, data.username, data.end);
    } else if (data.type === "user-left") {
        if (cursors[data.userId]) {
            cursors[data.userId].cursor.remove();
            cursors[data.userId].label.remove();
            delete cursors[data.userId];
        }
    }

    isProcessing = false;
    processQueue();
}
