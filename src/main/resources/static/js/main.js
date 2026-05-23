/**
 * Evento de arranque global del sistema
 */
window.onload = function () {
    // Inicializar los iconos estéticos de Lucide
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // Carga inicial sincronizada de datos
    jalarDatosDashboard();
    cargarCarreras();
    cargarTablaVacantesDinamica();
};