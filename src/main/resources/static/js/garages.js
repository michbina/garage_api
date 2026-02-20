// Fonctions pour la gestion des garages


function filterGarages() {
	var input = document.getElementById("garageSearch");
	var filter = input.value.toLowerCase();
	var table = document.getElementById("garagesTable");
	var trs = table.getElementsByTagName("tr");
	for (var i = 1; i < trs.length; i++) {
		var td = trs[i].getElementsByTagName("td")[0];
		if (td) {
			var txtValue = td.textContent || td.innerText;
			trs[i].style.display = txtValue.toLowerCase().indexOf(filter) > -1 ? "" : "none";
		}
	}
}


function resetGarageSearch() {
	document.getElementById("garageSearch").value = "";
	filterGarages();
}		        