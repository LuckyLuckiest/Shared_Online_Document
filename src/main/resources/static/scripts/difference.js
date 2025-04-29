const messageQueue = [];
let isProcessing = false;

socket.addEventListener("message", (event) => {
    const data = JSON.parse(event.data);

    if (data.userId === userId) return;

    messageQueue.push(data);
    processQueue();
});

function generateDifference(oldText, newText) {
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

function applyDifferenceToEditor(difference) {
    const value = editor.innerHTML;
    editor.innerHTML = value.slice(0, difference.start) + difference.inserted + value.slice(difference.end);
}

function processQueue() {
    if (isProcessing) return;
    if (messageQueue.length === 0) return;

    isProcessing = true;
    const data = messageQueue.shift();

    if (data.type === "update") {
        selfChange = true;
        applyDifferenceToEditor(data);
        lastValue = editor.innerHTML;
    }

    isProcessing = false;
    processQueue();
}
