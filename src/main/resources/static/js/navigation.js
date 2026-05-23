/**
 * Gestión de la navegación interna (Single Page Application)
 */
function navegarModulo(moduloId, nombreModulo) {
    const modulos = [
        'dashboard',
        'carga',
        'procesamiento',
        'vista-previa',
        'pdfs',
        'reporte',
        'historial',
        'anulacion'
    ];

    modulos.forEach(m => {
        // OCULTAR SECCIONES
        const domSec = document.getElementById(`vista-${m}`);
        if (domSec) {
            domSec.classList.add('hidden');
        }

        // RESET BOTONES
        const domBtn = document.getElementById(`btn-${m}`);
        if (domBtn) {
            domBtn.className = "w-full flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-white/10 hover:text-white transition-colors text-left";
        }
    });

    // MOSTRAR SECCIÓN ACTIVA
    const seccionActiva = document.getElementById(`vista-${moduloId}`);
    if (seccionActiva) {
        seccionActiva.classList.remove('hidden');
    }

    // BOTÓN ACTIVO
    const btnActivo = document.getElementById(`btn-${moduloId}`);
    if (btnActivo) {
        btnActivo.className = "w-full flex items-center gap-3 px-4 py-3 rounded-lg text-white bg-[#0052cc] font-medium transition-colors text-left";
    }

    // FOOTER
    document.getElementById('footer-modulo').textContent = nombreModulo.toUpperCase();
}