const editor = document.getElementById("editor");
let lastValue = "";
let selfChange = false;

fetch(`/content?session=${sessionId}`)
    .then(res => res.text())
    .then(text => {
    editor.innerHTML = text.replace(/\n/g, "<br>");
    lastValue = editor.innerHTML;
});

let inputTimeout;
editor.addEventListener("input", () => {
    if (selfChange) {
        selfChange = false;
        return;
    }

    clearTimeout(inputTimeout);
    inputTimeout = setTimeout(() => {
        const newValue = editor.innerHTML;
        const difference = generateDifference(lastValue, newValue);
        lastValue = newValue;

        socket.send(JSON.stringify({
            type: "update",
            userId,
            username,
            userColor,
            sessionId,
            start: difference.start,
            end: difference.end,
            inserted: difference.inserted
        }));

        debounceSave(newValue);
    }, 100);
});

function saveContentToServer(content) {
    fetch(`/save-content?session=${sessionId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ content })
    }).then(response => {
        if (!response.ok) {
            throw new Error("Failed to save content");
        }
        console.log("Content saved successfully.");
    }).catch(err => console.error("Save content error:", err));
}

let timeoutId;
function debounceSave(newValue) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
        saveContentToServer(newValue);
    }, 500);
}
