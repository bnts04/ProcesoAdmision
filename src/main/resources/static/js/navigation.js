function navegarModulo(moduloId, nombreModulo) {
    if (window.modulosBloqueados && window.modulosBloqueados.has(moduloId)) {
        alert("Este módulo se habilita después de ejecutar el procesamiento completo.");
        return;
    }

    const modulos = ["dashboard", "carga", "reporte", "vista-previa", "historial", "anulacion"];

    modulos.forEach(m => {
        const sec = document.getElementById(`vista-${m}`);
        if (sec) sec.classList.add("hidden");

        const btn = document.getElementById(`btn-${m}`);
        if (btn) {
            btn.className = "w-full flex items-center gap-3 px-4 py-3 rounded-lg sidebar-normal transition-colors text-left";
            if (m === "anulacion") btn.classList.add("text-red-300", "hover:text-red-200");
        }
    });

    const seccion = document.getElementById(`vista-${moduloId}`);
    if (seccion) seccion.classList.remove("hidden");

    const activo = document.getElementById(`btn-${moduloId}`);
    if (activo) {
        activo.className = "w-full flex items-center gap-3 px-4 py-3 rounded-lg sidebar-active transition-colors text-left";
    }

    const footer = document.getElementById("footer-modulo");
    if (footer) footer.textContent = nombreModulo.toUpperCase();

    if (moduloId === "dashboard") {
        jalarDatosDashboard();
    }

    if (moduloId === "vista-previa") {
        cargarCarreras().then(() => filtrarPorCarrera());
    }

    if (moduloId === "reporte") {
        cargarTablaVacantesDinamica();
    }

    if (moduloId === "historial") {
        cargarHistorial();
    }

    if (moduloId === "anulacion") {
        inicializarModuloAnulacion();
    }

    if (typeof lucide !== "undefined") lucide.createIcons();
}
