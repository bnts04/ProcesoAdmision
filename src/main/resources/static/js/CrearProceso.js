let modalidadSeleccionada = "Admisión Ordinaria";

document.querySelectorAll(".modal-pill").forEach(btn => {
    btn.addEventListener("click", function () {
        document.querySelectorAll(".modal-pill").forEach(b => {
            b.classList.remove("active", "bg-[#0052cc]", "text-white", "shadow-md");
            b.classList.add("bg-gray-100", "text-gray-700");
        });

        this.classList.remove("bg-gray-100", "text-gray-700");
        this.classList.add("active", "bg-[#0052cc]", "text-white", "shadow-md");

        modalidadSeleccionada = this.dataset.modalidad;
    });
});

function evaluarArchivoSeleccionado(inputId, textId, cardId, iconContainerId) {
    const input = document.getElementById(inputId);
    const textSpan = document.getElementById(textId);
    const card = document.getElementById(cardId);
    const icon = document.getElementById(iconContainerId);

    if (!input || !textSpan || !card || !icon) return;

    if (input.files.length > 0) {
        textSpan.textContent = input.files[0].name;
        textSpan.className = "text-xs text-green-700 font-semibold block truncate";

        card.classList.remove("bg-white", "border-gray-300");
        card.classList.add("bg-green-50", "border-green-400");

        icon.className = "w-9 h-9 bg-green-500 text-white rounded-lg flex items-center justify-center shrink-0 shadow-sm";
        icon.innerHTML = '<i data-lucide="check" class="w-5 h-5"></i>';
    }

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}

async function subirUnArchivo(procesoId, tipoArchivo, archivo) {
    const formData = new FormData();

    formData.append("tipoArchivo", tipoArchivo);
    formData.append("archivo", archivo);

    const response = await fetch(`${API_BASE}/api/archivos/cargar/${procesoId}`, {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        const error = await response.text();
        throw new Error(`Error subiendo ${tipoArchivo}: ${error}`);
    }

    return await response.json();
}

function limpiarTarjetasArchivos() {
    const configs = [
        [
            "file-postulantes",
            "txt-file-postulantes",
            "card-file-postulantes",
            "icon-container-postulantes",
            "Seleccionar IDENTIFI.DBF"
        ],
        [
            "file-claves",
            "txt-file-claves",
            "card-file-claves",
            "icon-container-claves",
            "Seleccionar CLAVES.DBF"
        ],
        [
            "file-respuestas",
            "txt-file-respuestas",
            "card-file-respuestas",
            "icon-container-respuestas",
            "Seleccionar RESPUEST.DBF"
        ]
    ];

    configs.forEach(([inputId, textId, cardId, iconId, textDefault]) => {
        const input = document.getElementById(inputId);
        const text = document.getElementById(textId);
        const card = document.getElementById(cardId);
        const icon = document.getElementById(iconId);

        if (input) {
            input.value = "";
        }

        if (text) {
            text.textContent = textDefault;
            text.className = "text-xs text-gray-400 block truncate";
        }

        if (card) {
            card.className = "bg-white border border-gray-300 rounded-lg p-3 flex items-center justify-between shadow-sm transition-all duration-300";
        }

        if (icon) {
            icon.className = "w-9 h-9 bg-blue-50 text-blue-600 rounded-lg flex items-center justify-center shrink-0";
            icon.innerHTML = '<i data-lucide="database" class="w-5 h-5"></i>';
        }
    });

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}

const formProceso = document.getElementById("form-proceso");

if (formProceso) {
    formProceso.addEventListener("submit", async function (e) {
        e.preventDefault();

        try {
            const nombreProceso = document.getElementById("input-nombre-proceso").value.trim();

            if (!nombreProceso) {
                alert("Ingrese el nombre del proceso.");
                return;
            }

            const fileIdentifi = document.getElementById("file-postulantes").files[0];
            const fileClaves = document.getElementById("file-claves").files[0];
            const fileRespuest = document.getElementById("file-respuestas").files[0];

            if (!fileIdentifi || !fileClaves || !fileRespuest) {
                alert("Debe seleccionar los 3 DBF: IDENTIFI, CLAVES y RESPUEST.");
                return;
            }

            const configuracionCalificacion = obtenerValoresCalificacionFormulario();

            mostrarMensajeCarga("Creando proceso y subiendo archivos DBF...", "warn");

            const proceso = await fetchJson(`${API_BASE}/api/procesos`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    nombreProceso,
                    modalidad: modalidadSeleccionada,
                    ...configuracionCalificacion
                })
            });

            const procesoId = proceso.id;

            setProcesoIdActual(procesoId);

            await subirUnArchivo(procesoId, "IDENTIFI", fileIdentifi);
            await subirUnArchivo(procesoId, "CLAVES", fileClaves);
            await subirUnArchivo(procesoId, "RESPUEST", fileRespuest);

            await fetchJson(`${API_BASE}/api/validaciones/proceso/${procesoId}/fuentes/aplicar`, {
                method: "POST"
            });

            mostrarModalExito();

            formProceso.reset();
            limpiarTarjetasArchivos();

            await verificarEstadoProcesoActual({ navegar: true });

            if (typeof cargarConfiguracionCalificacionProcesoActual === "function") {
                await cargarConfiguracionCalificacionProcesoActual(false);
            }

        } catch (error) {
            console.error(error);
            alert(error.message);
            await verificarEstadoProcesoActual();
        }
    });
}

function mostrarModalExito() {
    const modal = document.getElementById("modal-exito");

    if (!modal) return;

    const box = modal.querySelector(".bg-white");

    modal.classList.remove("hidden");

    setTimeout(() => {
        if (!box) return;

        box.classList.remove("scale-95", "opacity-0");
        box.classList.add("scale-100", "opacity-100");
    }, 10);
}

function ocultarModalExito() {
    const modal = document.getElementById("modal-exito");

    if (!modal) return;

    const box = modal.querySelector(".bg-white");

    if (box) {
        box.classList.remove("scale-100", "opacity-100");
        box.classList.add("scale-95", "opacity-0");
    }

    setTimeout(() => {
        modal.classList.add("hidden");
    }, 300);
}

const btnCerrarModal = document.getElementById("btn-cerrar-modal");

if (btnCerrarModal) {
    btnCerrarModal.addEventListener("click", async function () {
        ocultarModalExito();

        await verificarEstadoProcesoActual({ navegar: true });

        if (typeof cargarConfiguracionCalificacionProcesoActual === "function") {
            await cargarConfiguracionCalificacionProcesoActual(false);
        }

        if (typeof navegarModulo === "function") {
            navegarModulo("carga", "Gestión de proceso");
        }
    });
}

function actualizarModoCreacionProceso(procesoActivo) {
    const inputNombre = document.getElementById("input-nombre-proceso");
    const btnCrear = formProceso ? formProceso.querySelector('button[type="submit"]') : null;

    const archivos = [
        {
            inputId: "file-postulantes",
            cardId: "card-file-postulantes"
        },
        {
            inputId: "file-claves",
            cardId: "card-file-claves"
        },
        {
            inputId: "file-respuestas",
            cardId: "card-file-respuestas"
        }
    ];

    if (inputNombre) {
        inputNombre.disabled = procesoActivo;

        if (procesoActivo) {
            inputNombre.placeholder = "Proceso activo seleccionado. Use Limpiar selección para crear otro.";
            inputNombre.classList.add("bg-gray-100", "cursor-not-allowed", "opacity-70");
        } else {
            inputNombre.placeholder = "Ej. Admisión 2026-I";
            inputNombre.classList.remove("bg-gray-100", "cursor-not-allowed", "opacity-70");
        }
    }

    document.querySelectorAll(".modal-pill").forEach(btn => {
        btn.disabled = procesoActivo;

        if (procesoActivo) {
            btn.classList.add("opacity-50", "cursor-not-allowed");
        } else {
            btn.classList.remove("opacity-50", "cursor-not-allowed");
        }
    });

    archivos.forEach(item => {
        const input = document.getElementById(item.inputId);
        const card = document.getElementById(item.cardId);

        if (input) {
            input.disabled = procesoActivo;
        }

        if (card) {
            if (procesoActivo) {
                card.classList.add("opacity-60", "cursor-not-allowed", "bg-gray-100");
            } else {
                card.classList.remove("opacity-60", "cursor-not-allowed", "bg-gray-100");
            }
        }
    });

    if (btnCrear) {
        btnCrear.disabled = procesoActivo;

        if (procesoActivo) {
            btnCrear.innerHTML = `
                <i data-lucide="lock" class="w-5 h-5"></i>
                Proceso activo seleccionado
            `;
            btnCrear.classList.add("opacity-50", "cursor-not-allowed");
            btnCrear.classList.remove("hover:bg-blue-700");
        } else {
            btnCrear.innerHTML = `
                <i data-lucide="plus-circle" class="w-5 h-5"></i>
                Crear proceso y subir DBF
            `;
            btnCrear.classList.remove("opacity-50", "cursor-not-allowed");
            btnCrear.classList.add("hover:bg-blue-700");
        }
    }

    if (typeof lucide !== "undefined") {
        lucide.createIcons();
    }
}