/* dropdown-click.js
Permet d'ouvrir les menus dropdown au clic tout en conservant l'ouverture au hover.

Le clic bascule l'état 'active' du dropdown courant.
Critère: si on clique sur un lien interne, on ne bloque pas le lien.
Lorsqu’on ouvre un dropdown, les autres sont fermés.
Fermeture au clic en dehors du dropdown. */
document.addEventListener('DOMContentLoaded', function() {
// Gestion du clic sur chaque dropdown
document.querySelectorAll('.dropdown').forEach(function(el){
el.addEventListener('click', function(e){
// Ne pas détourner les liens internes
if (e.target.closest('a')) return;
  e.preventDefault();

  // Fermer les autres dropdowns actives
  document.querySelectorAll('.dropdown.active').forEach(function(d){
    if (d !== el) d.classList.remove('active');
  });

  // Basculer l'état actif du dropdown courant
  el.classList.toggle('active');
});


});

// Fermer les dropdowns si clic ailleurs
document.addEventListener('click', function(e){
if (!e.target.closest('.dropdown')) {
document.querySelectorAll('.dropdown.active').forEach(function(d){
d.classList.remove('active');
});
}
});
});