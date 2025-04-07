		//Script pour afficher la signature
        // Modal pour la signature
        var modal = document.getElementById("signatureModal");
        var signatureImage = document.getElementById("signatureImage");
        var span = document.getElementsByClassName("close")[0];
        
        function showSignature(element) {
            var signatureData = element.getAttribute("data-signature");
            signatureImage.src = signatureData;
            modal.style.display = "block";
            return false; // Empêcher le lien de naviguer
        }
        
        // Fermer le modal quand on clique sur X
        span.onclick = function() {
            modal.style.display = "none";
        }
        
        // Fermer le modal quand on clique en dehors
        window.onclick = function(event) {
            if (event.target == modal) {
                modal.style.display = "none";
            }
        }