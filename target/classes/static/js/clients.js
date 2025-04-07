// Fonctions pour la gestion des clients

// Filtrer les clients selon le terme de recherche
function filterClients() {
    var input = document.getElementById("clientSearch");
    var filter = input.value.toUpperCase();
    var table = document.getElementById("clientsTable");
    var tr = table.getElementsByTagName("tr");
    
    for (var i = 1; i < tr.length; i++) {
        var tdUsername = tr[i].getElementsByTagName("td")[1];
        if (tdUsername) {
            var txtValue = tdUsername.textContent || tdUsername.innerText;
            if (txtValue.toUpperCase().indexOf(filter) > -1) {
                tr[i].style.display = "";
            } else {
                tr[i].style.display = "none";
            }
        }
    }
}

// Réinitialiser la recherche
function resetSearch() {
    document.getElementById("clientSearch").value = "";
    filterClients();
}

// Trier les clients selon différents critères
function sortClients() {
    var table = document.getElementById("clientsTable");
    var tbody = table.tBodies[0];
    var rows = Array.from(tbody.rows);
    var sortOrder = document.getElementById("sortOrder").value;
    
    rows.sort(function(a, b) {
        var aValue, bValue;
        
        if (sortOrder === "name-asc" || sortOrder === "name-desc") {
            aValue = a.cells[1].innerText.trim().toLowerCase();
            bValue = b.cells[1].innerText.trim().toLowerCase();
            
            return sortOrder === "name-asc" 
                ? aValue.localeCompare(bValue) 
                : bValue.localeCompare(aValue);
        } else {
            // Tri par date (format DD/MM/YYYY)
            aValue = a.cells[2].innerText.trim();
            bValue = b.cells[2].innerText.trim();
            
            if (aValue === "Non disponible") return sortOrder === "date-asc" ? -1 : 1;
            if (bValue === "Non disponible") return sortOrder === "date-asc" ? 1 : -1;
            
            var aParts = aValue.split('/');
            var bParts = bValue.split('/');
            
            var aDate = new Date(aParts[2], aParts[1]-1, aParts[0]);
            var bDate = new Date(bParts[2], bParts[1]-1, bParts[0]);
            
            return sortOrder === "date-asc" 
                ? aDate - bDate 
                : bDate - aDate;
        }
    });
    
    // Réinsérer les lignes triées
    rows.forEach(function(row) {
        tbody.appendChild(row);
    });
}

// Afficher les détails d'un client
function showClientDetails(clientId) {
    // Simuler le chargement des détails du client
    // Dans une vraie application, vous feriez un appel AJAX ici
    var clientsTable = document.getElementById("clientsTable");
    var rows = clientsTable.getElementsByTagName("tr");
    var clientData = null;
    
    for (var i = 1; i < rows.length; i++) {
        if (rows[i].cells[0].innerText == clientId) {
            clientData = {
                id: rows[i].cells[0].innerText,
                username: rows[i].cells[1].innerText.trim(),
                dateInscription: rows[i].cells[2].innerText,
                factures: rows[i].cells[3].innerText,
                devis: rows[i].cells[4].innerText
            };
            break;
        }
    }
    
    if (clientData) {
        var detailsHTML = `
            <p><strong>ID:</strong> ${clientData.id}</p>
            <p><strong>Nom d'utilisateur:</strong> ${clientData.username}</p>
            <p><strong>Date d'inscription:</strong> ${clientData.dateInscription}</p>
            <p><strong>Nombre de factures:</strong> ${clientData.factures}</p>
            <p><strong>Nombre de devis:</strong> ${clientData.devis}</p>
            <div class="client-actions" style="margin-top: 15px;">
                <a href="/admin/client/${clientData.id}/factures" class="btn btn-primary">Voir factures</a>
                <a href="/admin/client/${clientData.id}/devis" class="btn btn-primary">Voir devis</a>
            </div>
        `;
        
        document.getElementById("clientDetailsContent").innerHTML = detailsHTML;
        document.getElementById("clientDetails").style.display = "block";
    }
}

// Cacher les détails du client
function hideClientDetails() {
    document.getElementById("clientDetails").style.display = "none";
}

// Initialiser les événements quand la page est chargée
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser les événements si nécessaire
    console.log("Page clients chargée");
});