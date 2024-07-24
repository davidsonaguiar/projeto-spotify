// Log para verificar se o script está sendo carregado
console.log("Modal script carregado");

// Get the modal
var modal = document.getElementById("playlistModal");

// Get the button that opens the modal
var btn = document.getElementById("openModalBtn");

// Get the <span> element that closes the modal
var span = document.getElementById("closeModalBtn");

// When the user clicks on the button, open the modal
btn.onclick = function() {
    console.log("Botão de criar playlist clicado");
    modal.style.display = "block";
}

// When the user clicks on <span> (x), close the modal
span.onclick = function() {
    modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}
document.getElementById("createPlaylistForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const name = document.getElementById("playlistName").value;
    const description = document.getElementById("playlistDescription").value;
    const isPublic = document.getElementById("playlistPublic").checked;

    fetch('/createPlaylist', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, description, isPublic })
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            } else {
                return response.json();
            }
        })
        .then(data => {
            if (data.success) {
                alert("Playlist criada com sucesso!");
            } else {
                alert("Falha ao criar playlist.");
            }
            closeCreatePlaylistModal();
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Erro ao criar playlist.");
            closeCreatePlaylistModal();
        });
});

