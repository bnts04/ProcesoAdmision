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

    // 🌟 DISPARADORES AUTOMÁTICOS DE DATOS 🌟
    // Evaluamos 'moduloId' (que es el parámetro real de tu función)
    if (moduloId === 'historial') {
        if (typeof cargarHistorial === 'function') {
            cargarHistorial(); // 🚀 Dispara la carga automática de la BD al entrar a la vista
        }
    }

    // Aprovechamos para enganchar también tu tabla de vacantes del Proceso 1 cuando entres a 'reporte'
    if (moduloId === 'reporte') {
        if (typeof cargarTablaVacantesDinamica === 'function') {
            cargarTablaVacantesDinamica(); // 🚀 Rellena la tabla de vacantes automáticamente
        }
    }
// 🌟 DISPARADOR AUTOMÁTICO PARA EL MÓDULO DE ANULACIÓN 🌟
    if (moduloId === 'anulacion') {
        if (typeof inicializarModuloAnulacion === 'function') {
            inicializarModuloAnulacion(); // Re-evalúa el DOM y limpia el estado del botón
        }

    }

    // Si entra al módulo de Formulario de Anulación
    if (moduloId === 'anulacion') {
        if (typeof inicializarModuloAnulacion === 'function') {
            inicializarModuloAnulacion();
        }
    }
    // FOOTER
    const domFooter = document.getElementById('footer-modulo');
    if (domFooter) {
        domFooter.textContent = nombreModulo.toUpperCase();
    }
}