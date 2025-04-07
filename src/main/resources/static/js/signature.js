document.addEventListener('DOMContentLoaded', function() {
    const canvas = document.getElementById('signature-pad');
    
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    const form = canvas.closest('form');
    const clearButton = document.getElementById('clear');
    const submitButton = document.getElementById('submit-signature');
    const signatureInput = document.getElementById('signature-data');
    
    let isDrawing = false;
    
    canvas.addEventListener('mousedown', startDrawing);
    canvas.addEventListener('mousemove', draw);
    canvas.addEventListener('mouseup', stopDrawing);
    canvas.addEventListener('mouseout', stopDrawing);
    
    // Support tactile
    canvas.addEventListener('touchstart', startDrawingTouch);
    canvas.addEventListener('touchmove', drawTouch);
    canvas.addEventListener('touchend', stopDrawing);
    
    clearButton.addEventListener('click', clearSignature);
    form.addEventListener('submit', saveSignature);
    
    function startDrawing(e) {
        isDrawing = true;
        draw(e);
    }
    
    function startDrawingTouch(e) {
        e.preventDefault();
        isDrawing = true;
        drawTouch(e);
    }
    
    function draw(e) {
        if (!isDrawing) return;
        
        ctx.lineWidth = 2;
        ctx.lineCap = 'round';
        ctx.strokeStyle = '#000';
        
        ctx.lineTo(e.clientX - canvas.getBoundingClientRect().left, 
                   e.clientY - canvas.getBoundingClientRect().top);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(e.clientX - canvas.getBoundingClientRect().left, 
                   e.clientY - canvas.getBoundingClientRect().top);
    }
    
    function drawTouch(e) {
        if (!isDrawing) return;
        e.preventDefault();
        
        const touch = e.touches[0];
        
        ctx.lineWidth = 2;
        ctx.lineCap = 'round';
        ctx.strokeStyle = '#000';
        
        ctx.lineTo(touch.clientX - canvas.getBoundingClientRect().left,
                  touch.clientY - canvas.getBoundingClientRect().top);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(touch.clientX - canvas.getBoundingClientRect().left,
                  touch.clientY - canvas.getBoundingClientRect().top);
    }
    
    function stopDrawing() {
        isDrawing = false;
        ctx.beginPath();
    }
    
    function clearSignature() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
    
    function saveSignature(e) {
        if (isCanvasEmpty()) {
            e.preventDefault();
            alert('Veuillez signer le devis avant de le valider.');
            return;
        }
        
        signatureInput.value = canvas.toDataURL();
    }
    
    function isCanvasEmpty() {
        const pixelData = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
        
        for (let i = 0; i < pixelData.length; i += 4) {
            if (pixelData[i + 3] !== 0) {
                return false;
            }
        }
        return true;
    }
});