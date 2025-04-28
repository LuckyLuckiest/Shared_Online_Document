document.getElementById("session-form").addEventListener("submit", function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const color = document.getElementById("color").value;
    let sessionId = document.getElementById("session-id").value.trim();

    if (!sessionId) {
        sessionId = uuidv4();
    }

    if (!isValidSessionId(sessionId)) {
        alert("Invalid session ID format. Please follow UUID v4 standard.");
        return;
    }

    sessionStorage.setItem('sessionData', JSON.stringify({
        sessionId,
        username,
        color
    }));

    window.location.href = `/editor.html`;
});

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
        .replace(/[xy]/g, function (c) {
        const r = Math.random() * 16 | 0,
        v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

function isValidSessionId(sessionId) {
    const regex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
    return regex.test(sessionId);
}