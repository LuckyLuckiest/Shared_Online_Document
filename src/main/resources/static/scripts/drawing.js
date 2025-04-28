let isDrawing = false;
let shapeType = null;
let shapeElement = null;
const drawingArea = document.getElementById('drawingArea');

function startDrawing(type) {
    shapeType = type;
    isDrawing = true;
}

drawingArea.addEventListener('mousedown', (e) => {
    if (!isDrawing) return;

    shapeElement = document.createElement('div');
    shapeElement.classList.add('shape');
    shapeElement.style.left = e.offsetX + 'px';
    shapeElement.style.top = e.offsetY + 'px';
    shapeElement.dataset.startX = e.offsetX;
    shapeElement.dataset.startY = e.offsetY;

    drawingArea.appendChild(shapeElement);

    drawingArea.addEventListener('mousemove', resizeShape);
    drawingArea.addEventListener('mouseup', finishShape);
});

function resizeShape(e) {
    if (!shapeElement) return;

    const startX = parseInt(shapeElement.dataset.startX);
    const startY = parseInt(shapeElement.dataset.startY);
    const width = e.offsetX - startX;
    const height = e.offsetY - startY;

    shapeElement.style.width = Math.abs(width) + 'px';
    shapeElement.style.height = Math.abs(height) + 'px';
    shapeElement.style.left = (width < 0 ? e.offsetX : startX) + 'px';
    shapeElement.style.top = (height < 0 ? e.offsetY : startY) + 'px';

    if (shapeType === 'circle') {
        shapeElement.style.borderRadius = '50%';
    } else {
        shapeElement.style.borderRadius = '0';
    }
}

function finishShape() {
    isDrawing = false;
    shapeType = null;
    shapeElement = null;
    drawingArea.removeEventListener('mousemove', resizeShape);
    drawingArea.removeEventListener('mouseup', finishShape);
}
