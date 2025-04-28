const editor = document.getElementById("editor");
let lastValue = "";
let selfChange = false;

fetch(`/content?session=${sessionId}`)
    .then(res => res.text())
    .then(text => {
    editor.innerHTML = text.replace(/\n/g, "<br>");
    lastValue = editor.innerHTML;
});

editor.addEventListener("input", () => {
    if (selfChange) {
        selfChange = false;
        return;
    }

    clearTimeout(inputTimeout);
    inputTimeout = setTimeout(() => {
        const newValue = editor.innerHTML;
        const diff = generateDiff(lastValue, newValue);
        const cursorPos = getCaretCharacterOffsetWithin(editor);
        lastValue = newValue;

        socket.send(JSON.stringify({
            type: "update",
            userId,
            username,
            userColor,
            sessionId,
            start: diff.start,
            end: diff.end,
            inserted: diff.inserted,
            cursor: cursorPos
        }));

        debounceSave(newValue);
    }, 100);
});

let timeoutId;
function debounceSave(newValue) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
        saveContentToServer(newValue);
    }, 500);
}
