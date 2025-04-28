document.getElementById("fontSelect").addEventListener("change", (e) => {
    document.execCommand('fontName', false, e.target.value);
});

document.getElementById("colorPicker").addEventListener("input", (e) => {
    document.execCommand('foreColor', false, e.target.value);
});

document.getElementById("boldBtn").addEventListener("click", () => {
    document.execCommand('bold');
});

document.getElementById("italicBtn").addEventListener("click", () => {
    document.execCommand('italic');
});

document.getElementById("underlineBtn").addEventListener("click", () => {
    document.execCommand('underline');
});

document.getElementById("insertImageBtn").addEventListener("click", () => {
    const url = prompt("Enter Image URL:");
    if (url) {
        document.execCommand('insertImage', false, url);
    }
});
