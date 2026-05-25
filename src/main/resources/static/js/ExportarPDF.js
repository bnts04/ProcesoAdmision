

const BASE_URL_PDF = "http://localhost:8080/api/pdf";

// 1. CONSOLIDADO GENERAL PDF (Abre en pestaña nueva para visualizar/imprimir)
function exportarConsolidadoGeneral() {
    console.log("Solicitando la generación del PDF General...");

    fetch(`${BASE_URL_PDF}/proceso/1/general`, {
        method: 'POST'
    })
        .then(res => {
            if (!res.ok) throw new Error("Error al generar el PDF en el servidor");
            return res.json();
        })
        .then(data => {
            // Usamos 'urlVer' para que se abra elegantemente en otra pestaña
            // Si prefieres que se descargue directo sin ver, cambia 'urlVer' por 'urlDescargar'
            if (data.urlVer) {
                window.open(data.urlVer, '_blank');
            } else {
                alert("El servidor no proporcionó la URL del archivo.");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Hubo un error al procesar el Consolidado General.");
        });
}

// 2. REPORTE POR ESCUELA PDF (Abre en pestaña nueva filtrado por la carrera actual)
function exportarReportePorEscuela() {
    const carrera = document.getElementById("selector-carrera").value;

    if (carrera === "") {
        alert("Por favor, selecciona una carrera/escuela específica. Para todo junto, usa el Consolidado General.");
        return;
    }

    console.log(`Solicitando generación de PDF para: ${carrera}...`);

    fetch(`${BASE_URL_PDF}/proceso/1/carrera?nombre=${encodeURIComponent(carrera)}`, {
        method: 'POST'
    })
        .then(res => {
            if (!res.ok) throw new Error("Error al generar el PDF de la carrera");
            return res.json();
        })
        .then(data => {
            // Al igual que el anterior, abrimos directo en pestaña nueva usando la respuesta del backend
            if (data.urlVer) {
                window.open(data.urlVer, '_blank');
            } else {
                alert("El servidor no proporcionó la URL del archivo.");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Hubo un error al procesar el reporte de la escuela.");
        });
}