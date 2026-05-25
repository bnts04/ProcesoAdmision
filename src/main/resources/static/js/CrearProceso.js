const URL_PROCESOS = "http://localhost:8080/api/procesos";
const URL_ARCHIVOS = "http://localhost:8080/api/archivos";

let modalidadSeleccionada = "ORDINARIO";

// ==========================================
// CONTROL DE MODALIDADES
// ==========================================
document.querySelectorAll(".modal-pill").forEach(btn => {

    btn.addEventListener("click", function () {

        document.querySelectorAll(".modal-pill").forEach(b => {

            b.classList.remove(
                "active",
                "bg-[#0052cc]",
                "text-white",
                "shadow-md"
            );

            b.classList.add(
                "bg-gray-100",
                "text-gray-700"
            );
        });

        this.classList.remove(
            "bg-gray-100",
            "text-gray-700"
        );

        this.classList.add(
            "active",
            "bg-[#0052cc]",
            "text-white",
            "shadow-md"
        );

        modalidadSeleccionada =
            this.dataset.modalidad.toUpperCase();

        console.log("📌 Modalidad:", modalidadSeleccionada);
    });
});

// ==========================================
// EFECTOS VISUALES ARCHIVOS
// ==========================================
function evaluarArchivoSeleccionado(
    inputId,
    textId,
    cardId,
    iconContainerId
) {

    const input = document.getElementById(inputId);
    const textSpan = document.getElementById(textId);
    const card = document.getElementById(cardId);
    const iconContainer = document.getElementById(iconContainerId);

    if (!input || !textSpan || !card || !iconContainer) {
        console.warn("⚠️ Elementos visuales no encontrados");
        return;
    }

    if (input.files.length > 0) {

        textSpan.textContent = input.files[0].name;

        textSpan.className =
            "text-xs text-green-700 font-semibold block truncate";

        card.classList.remove(
            "bg-white",
            "border-gray-300"
        );

        card.classList.add(
            "bg-green-50",
            "border-green-400"
        );

        iconContainer.className =
            "w-9 h-9 bg-green-500 text-white rounded-lg flex items-center justify-center shrink-0 shadow-sm";

        iconContainer.innerHTML =
            '<i data-lucide="check" class="w-5 h-5"></i>';

    } else {

        textSpan.textContent =
            input.accept.includes("pdf")
                ? "Seleccionar archivo (.pdf)..."
                : "Seleccionar archivo (.dbf)...";
    }

    lucide.createIcons();
}

// ==========================================
// SUBIR UN ARCHIVO
// ==========================================
async function subirUnArchivo(
    procesoId,
    tipoArchivo,
    archivo
) {

    const formData = new FormData();

    formData.append("tipoArchivo", tipoArchivo);
    formData.append("archivo", archivo);

    console.log(`📤 Subiendo ${tipoArchivo}...`);

    const response = await fetch(
        `${URL_ARCHIVOS}/cargar/${procesoId}`,
        {
            method: "POST",
            body: formData
        }
    );

    if (!response.ok) {

        const error = await response.text();

        throw new Error(
            `Error subiendo ${tipoArchivo}: ${error}`
        );
    }

    const data = await response.json();

    console.log(`✅ ${tipoArchivo} subido correctamente`);

    return data;
}

// ==========================================
// LIMPIAR TARJETAS VISUALES
// ==========================================
function limpiarTarjetasArchivos() {

    const configs = [

        {
            input: "file-postulantes",
            text: "txt-file-postulantes",
            card: "card-file-postulantes",
            icon: "icon-container-postulantes"
        },

        {
            input: "file-claves",
            text: "txt-file-claves",
            card: "card-file-claves",
            icon: "icon-container-claves"
        },

        {
            input: "file-respuestas",
            text: "txt-file-respuestas",
            card: "card-file-respuestas",
            icon: "icon-container-respuestas"
        },

        {
            input: "file-requisito",
            text: "txt-file-requisito",
            card: "card-file-requisito",
            icon: "icon-container-requisito"
        }
    ];

    configs.forEach(c => {

        const input = document.getElementById(c.input);
        const text = document.getElementById(c.text);
        const card = document.getElementById(c.card);
        const icon = document.getElementById(c.icon);

        input.value = "";

        text.textContent =
            c.input === "file-requisito"
                ? "Seleccionar archivo (.pdf)..."
                : "Seleccionar archivo (.dbf)...";

        text.className =
            "text-xs text-gray-400 block truncate";

        card.className =
            "bg-white border border-gray-300 rounded-lg p-3 flex items-center justify-between shadow-sm transition-all duration-300";

        if (c.input === "file-requisito") {

            icon.className =
                "w-9 h-9 bg-red-50 text-red-600 rounded-lg flex items-center justify-center shrink-0";

            icon.innerHTML =
                '<i data-lucide="file-text" class="w-5 h-5"></i>';

        } else {

            icon.className =
                "w-9 h-9 bg-blue-50 text-blue-600 rounded-lg flex items-center justify-center shrink-0";

            icon.innerHTML =
                '<i data-lucide="database" class="w-5 h-5"></i>';
        }
    });

    lucide.createIcons();
}

// ==========================================
// FORMULARIO PRINCIPAL
// ==========================================
document
    .getElementById("form-proceso")
    .addEventListener("submit", async function (e) {

        e.preventDefault();

        try {

            // ======================================
            // DATOS
            // ======================================

            const nombreProceso =
                document
                    .getElementById("input-nombre-proceso")
                    .value
                    .trim();

            if (!nombreProceso) {

                alert("Ingrese el nombre del proceso.");

                return;
            }

            // ======================================
            // ARCHIVOS
            // ======================================

            const filePostulantes =
                document.getElementById(
                    "file-postulantes"
                ).files[0];

            const fileClaves =
                document.getElementById(
                    "file-claves"
                ).files[0];

            const fileRespuestas =
                document.getElementById(
                    "file-respuestas"
                ).files[0];

            const filePdf =
                document.getElementById(
                    "file-requisito"
                ).files[0];

            // ======================================
            // VALIDACIÓN
            // ======================================

            if (
                !filePostulantes ||
                !fileClaves ||
                !fileRespuestas ||
                !filePdf
            ) {

                alert(
                    "Todos los archivos son obligatorios."
                );

                return;
            }

            console.log("🚀 Creando proceso...");

            // ======================================
            // CREAR PROCESO
            // ======================================

            const responseProceso = await fetch(
                URL_PROCESOS,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        nombreProceso: nombreProceso,
                        modalidad: modalidadSeleccionada
                    })
                }
            );

            if (!responseProceso.ok) {

                const error =
                    await responseProceso.text();

                throw new Error(
                    `Error creando proceso: ${error}`
                );
            }

            const proceso =
                await responseProceso.json();

            const procesoId = proceso.id;

            console.log(
                `✅ Proceso creado ID: ${procesoId}`
            );

            // ======================================
            // SUBIR ARCHIVOS
            // ======================================

            await Promise.all([

                subirUnArchivo(
                    procesoId,
                    "IDENTIFI",
                    filePostulantes
                ),

                subirUnArchivo(
                    procesoId,
                    "CLAVES",
                    fileClaves
                ),

                subirUnArchivo(
                    procesoId,
                    "RESPUEST",
                    fileRespuestas
                ),

                subirUnArchivo(
                    procesoId,
                    "PDF_RESULTADOS",
                    filePdf
                )
            ]);

            console.log("✅ Archivos subidos");

            // ======================================
            // VALIDAR ARCHIVOS
            // ======================================

            console.log("🔍 Validando archivos...");

            const responseValidacion =
                await fetch(
                    `${URL_ARCHIVOS}/validar/${procesoId}`,
                    {
                        method: "POST"
                    }
                );

            if (!responseValidacion.ok) {

                throw new Error(
                    "Error validando archivos."
                );
            }

            const validacion =
                await responseValidacion.json();

            console.log(
                "📋 Validación:",
                validacion
            );

            // ======================================
            // GUARDAR ID ACTIVO
            // ======================================

            localStorage.setItem(
                "procesoActivoId",
                procesoId
            );

            console.log(
                "💾 Proceso activo almacenado."
            );

            // ======================================
            // ÉXITO
            // ======================================

            mostrarModalExito();

            document
                .getElementById("form-proceso")
                .reset();

            limpiarTarjetasArchivos();

        } catch (error) {

            console.error(error);

            alert(error.message);
        }
    });

// ==========================================
// MODAL
// ==========================================
function mostrarModalExito() {

    const modal =
        document.getElementById("modal-exito");

    const modalBox =
        modal.querySelector(".bg-white");

    modal.classList.remove("hidden");

    setTimeout(() => {

        modalBox.classList.remove(
            "scale-95",
            "opacity-0"
        );

        modalBox.classList.add(
            "scale-100",
            "opacity-100"
        );

    }, 10);
}

function ocultarModalExito() {

    const modal =
        document.getElementById("modal-exito");

    const modalBox =
        modal.querySelector(".bg-white");

    modalBox.classList.remove(
        "scale-100",
        "opacity-100"
    );

    modalBox.classList.add(
        "scale-95",
        "opacity-0"
    );

    setTimeout(() => {

        modal.classList.add("hidden");

    }, 300);
}

// MODIFICA ESTA LÍNEA AL FINAL DE TU js/cargarArchivos.js
document.getElementById('btn-cerrar-modal').addEventListener('click', function() {
    ocultarModalExito();

    // 1. Recuperamos el ID recién creado que guardamos en el localStorage
    const nuevoProcesoId = localStorage.getItem('procesoActivoId');

    if (nuevoProcesoId) {
        // 2. Disparamos la carga de datos del dashboard de inmediato
        if (typeof cargarDatosDashboard === 'function') {
            cargarDatosDashboard(nuevoProcesoId);
        }

        // 3. CAMBIO DE VISTAS EN EL FRONT: Ocultamos el formulario de carga y encendemos el Dashboard
        const vistaCarga = document.getElementById('vista-carga');
        const vistaDashboard = document.getElementById('vista-dashboard');

        if (vistaCarga && vistaDashboard) {
            vistaCarga.classList.add('hidden'); // Oculta subida de archivos
            vistaDashboard.classList.remove('hidden'); // Muestra estadísticas y tablas
            console.log("🔄 Transición exitosa: Redirigido a Vista Dashboard.");
        }
    }
});