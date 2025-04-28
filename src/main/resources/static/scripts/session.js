const sessionData = JSON.parse(sessionStorage.getItem('sessionData'));

if (!sessionData) {
    alert('No session information found. Please go back and create/join a session.');
    window.location.href = '/';
} else {
    var { sessionId, username, color } = sessionData;
    var userId = "user-" + Math.floor(Math.random() * 10000);
    var userColor = color || "#000000";
    var socket = new WebSocket(`ws://${location.host}/edit`);
}
