function inicializarModuloAnulacion() {
    const fileInput = document.getElementById("anulacion-file-input");
    const textoArchivo = document.getElementById("anulacion-texto-archivo");
    const toggle = document.getElementById("anulacion-toggle");
    const btn = document.getElementById("btn-submit-anulacion");

    if (btn && toggle) {
        btn.disabled = !toggle.checked;
        toggle.onchange = () => {
            btn.disabled = !toggle.checked;
        };
    }

    if (fileInput && textoArchivo) {
        fileInput.onchange = (e) => {
            if (e.target.files.length > 0) {
                textoArchivo.textContent = `📄 ${e.target.files[0].name}`;
                textoArchivo.className = "text-sm font-bold text-green-600";
            } else {
                textoArchivo.textContent = "Arrastre o seleccione archivo";
                textoArchivo.className = "text-sm font-bold text-[#0052cc]";
            }
        };
    }
}

const formAnulacion = document.getElementById("form-anulacion");

if (formAnulacion) {
    formAnulacion.addEventListener("submit", async function (e) {
        e.preventDefault();

        const procesoId = getProcesoIdActual();

        if (!procesoId) {
            alert("No hay proceso activo seleccionado.");
            return;
        }

        const codigo = document.getElementById("anulacion-codigo").value.trim();
        const motivo = document.getElementById("anulacion-motivo").value;
        const observacion = document.getElementById("anulacion-observacion").value.trim();
        const evidencia = document.getElementById("anulacion-file-input").files[0];
        const toggle = document.getElementById("anulacion-toggle");

        if (!codigo) {
            alert("Ingresa el código del postulante.");
            return;
        }

        if (!motivo) {
            alert("Selecciona un motivo.");
            return;
        }

        if (!evidencia) {
            alert("Adjunta evidencia.");
            return;
        }

        if (!toggle.checked) {
            alert("Debes confirmar la anulación.");
            return;
        }

        const formData = new FormData();
        formData.append("procesoId", procesoId);
        formData.append("codigo", codigo);
        formData.append("motivo", observacion ? `${motivo} - Detalle: ${observacion}` : motivo);
        formData.append("evidencia", evidencia);

        try {
            const res = await fetch(`${API_BASE}/api/anulaciones-postulante`, {
                method: "POST",
                body: formData
            });

            if (!res.ok) {
                const text = await res.text();
                throw new Error(text || "Error anulando postulante.");
            }

            const data = await res.json();

            alert(data.mensaje || "Postulante anulado correctamente.");

            formAnulacion.reset();
            inicializarModuloAnulacion();

            await jalarDatosDashboard();

            if (window.estadoProcesoActual && window.estadoProcesoActual.mostrarDashboard) {
                navegarModulo("dashboard", "Dashboard global");
            }

        } catch (error) {
            console.error("Error anulando postulante:", error);
            alert("No se pudo registrar la anulación: " + error.message);
        }
    });
}
