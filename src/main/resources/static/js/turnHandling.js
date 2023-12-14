document.getElementById('moveButton').addEventListener('click', function (event) {
    event.preventDefault();

    const moveInputValue = document.getElementById('moveInput').value;
    fetch('/api/chess-games/move', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'moveInput=' + encodeURIComponent(moveInputValue),
    }).then(response => response.text())
        .catch(error => {
            console.error('Error:', error);
        });
});

document.getElementById('shortCastling').addEventListener('click', function (event) {
    event.preventDefault();
    fetch('/api/chess-games/move/castling/short', {
        method: 'POST'
    }).then(response => response.text())
        .catch(error => {
            console.error('Error:', error);
        });
});

document.getElementById('longCastling').addEventListener('click', function (event) {
    event.preventDefault();
    fetch('/api/chess-games/move/castling/long', {
        method: 'POST',
    }).then(response => response.text())
        .catch(error => {
            console.error('Error:', error);
        });
});

