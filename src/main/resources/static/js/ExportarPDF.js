async function generarPdfGeneral() {
    const procesoId = getProcesoIdActual();

    if (!procesoId) {
        alert("Primero debes crear o seleccionar un proceso.");
        return;
    }

    try {
        const data = await fetchJson(`${API_BASE}/api/pdf/proceso/${procesoId}/general`, {
            method: "POST"
        });

        alert(data.mensaje || "PDF generado correctamente.");

        if (data.urlVer) window.open(data.urlVer, "_blank");

        await cargarHistorial();

    } catch (error) {
        console.error("Error generando PDF general:", error);
        alert("No se pudo generar el PDF general: " + error.message);
    }
}

async function generarPdfPorCarrera() {
    const procesoId = getProcesoIdActual();
    const selector = document.getElementById("selector-carrera");
    const carrera = selector ? selector.value : "";

    if (!procesoId) {
        alert("Primero debes crear o seleccionar un proceso.");
        return;
    }

    if (!carrera) {
        alert("Selecciona una carrera para generar el PDF individual. Para todo el proceso usa PDF general.");
        return;
    }

    try {
        const data = await fetchJson(`${API_BASE}/api/pdf/proceso/${procesoId}/carrera?nombre=${encodeURIComponent(carrera)}`, {
            method: "POST"
        });

        alert(data.mensaje || "PDF de carrera generado correctamente.");

        if (data.urlVer) window.open(data.urlVer, "_blank");

        await cargarHistorial();

    } catch (error) {
        console.error("Error generando PDF por carrera:", error);
        alert("No se pudo generar el PDF por carrera: " + error.message);
    }
}
