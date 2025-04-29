const sessionLabel = document.getElementById("sessionLabel");

sessionLabel.innerHTML = `Session ID: ${sessionId}`;

sessionLabel.style.left = '50%';
sessionLabel.style.bottom = '20px';
sessionLabel.style.transform = 'translateX(-50%)';
sessionLabel.style.fontSize = '14px';
sessionLabel.style.color = '#007bff';
sessionLabel.style.fontWeight = 'bold';
sessionLabel.style.backgroundColor = 'rgba(255, 255, 255, 0.7)';
sessionLabel.style.padding = '5px 10px';
sessionLabel.style.borderRadius = '5px';

sessionLabel.addEventListener("click", () => {
    navigator.clipboard.writeText(sessionId)
        .then(() => {
        console.log("Session ID copied to clipboard!");
        sessionLabel.innerText = "Copied!";
        setTimeout(() => {
            sessionLabel.innerText = `Session ID: ${sessionId}`;
        }, 1000);
    })
        .catch(err => {
        console.error("Failed to copy Session ID:", err);
    });
});
