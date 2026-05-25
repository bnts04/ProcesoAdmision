// ==========================================
// CONTROL DE ANULACIONES DE POSTULANTES
// ==========================================

function inicializarModuloAnulacion() {
    console.log("[ANULACIÓN] Sincronizando interfaz y listeners de seguridad...");

    const fileInput = document.getElementById('anulacion-file-input');
    const textoArchivo = document.getElementById('anulacion-texto-archivo');
    const toggleAnulacion = document.getElementById('anulacion-toggle');
    const btnSubmit = document.getElementById('btn-submit-anulacion');

    // Estado inicial del botón según el interruptor
    if (btnSubmit) {
        if (toggleAnulacion && toggleAnulacion.checked) {
            btnSubmit.disabled = false;
            btnSubmit.classList.remove('opacity-50', 'cursor-not-allowed');
        } else {
            btnSubmit.disabled = true;
            btnSubmit.classList.add('opacity-50', 'cursor-not-allowed');
        }
    }

    // Listener del Interruptor de Confirmación
    if (toggleAnulacion && btnSubmit) {
        toggleAnulacion.onchange = (e) => {
            if (e.target.checked) {
                console.log("[ANULACIÓN] Acción confirmada por el usuario. Botón desbloqueado.");
                btnSubmit.disabled = false;
                btnSubmit.classList.remove('opacity-50', 'cursor-not-allowed');
            } else {
                btnSubmit.disabled = true;
                btnSubmit.classList.add('opacity-50', 'cursor-not-allowed');
            }
        };
    }

    // Listener de carga de archivos (Input File)
    if (fileInput && textoArchivo) {
        fileInput.onchange = (e) => {
            if (e.target.files.length > 0) {
                const file = e.target.files[0];
                console.log(`[ANULACIÓN] Archivo cargado listo: ${file.name}`);
                textoArchivo.textContent = `📄 ${file.name}`;
                textoArchivo.className = "text-sm font-bold text-green-600 animate-pulse";
            } else {
                textoArchivo.textContent = "Arrastre o seleccione archivo";
                textoArchivo.className = "text-sm font-bold text-[#0052cc]";
            }
        };
    }

    // Listener de envío del formulario
    if (btnSubmit) {
        btnSubmit.onclick = function(e) {
            e.preventDefault();
            ejecutarAnulacionFormulario();
        };
    }
}

function cargarAnulaciones() {
    console.log("[ANULACIÓN] Inicializando vista de control...");
    inicializarModuloAnulacion();
}

function ejecutarAnulacionFormulario() {
    const procesoId = localStorage.getItem('procesoActivoId');

    if (!procesoId) {
        alert("No hay ningún proceso activo seleccionado en la sesión.");
        return;
    }

    const inputCodigo = document.getElementById('anulacion-codigo');
    const inputMotivo = document.getElementById('anulacion-motivo');
    const inputEvidencia = document.getElementById('anulacion-file-input');
    const inputObservacion = document.getElementById('anulacion-observacion');
    const toggleConfirmacion = document.getElementById('anulacion-toggle');
    const btnSubmit = document.getElementById('btn-submit-anulacion');

    if (!inputCodigo || !inputCodigo.value.trim()) {
        alert("Por favor, ingresa el código del postulante.");
        inputCodigo.focus();
        return;
    }

    if (!inputMotivo || !inputMotivo.value) {
        alert("Por favor, seleccione un motivo principal de la lista.");
        inputMotivo.focus();
        return;
    }

    if (!inputEvidencia || inputEvidencia.files.length === 0) {
        alert("Es obligatorio adjuntar un archivo (Foto o Acta) como evidencia del incidente.");
        return;
    }

    if (!toggleConfirmacion || !toggleConfirmacion.checked) {
        alert("Debe activar el interruptor de 'Confirmar anulación' antes de ejecutar esta acción.");
        return;
    }

    let motivoFinal = inputMotivo.value;
    if (inputObservacion && inputObservacion.value.trim()) {
        motivoFinal += ` - Detalle: ${inputObservacion.value.trim()}`;
    }

    // Construcción del empaquetado binario Multipart
    const formData = new FormData();
    formData.append('procesoId', parseInt(procesoId));
    formData.append('codigo', inputCodigo.value.trim());
    formData.append('motivo', motivoFinal);
    console.log(`[ANULACIÓN] Despachando FormData a Spring Boot para Postulante: ${inputCodigo.value.trim()}`);

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = `<i data-lucide="loader" class="w-5 h-5 animate-spin"></i> Procesando...`;
        if (typeof lucide !== 'undefined') lucide.createIcons();
    }

    fetch('http://localhost:8080/api/anulaciones-postulante', {
        method: 'POST',
        body: formData
    })
        .then(res => {
            if (!res.ok) {
                return res.text().then(text => { throw new Error(text || `Error del servidor (${res.status})`) });
            }
            return res.json();
        })
        .then(data => {
            console.log("[ANULACIÓN] Servidor respondió con éxito:", data);
            alert(`¡Operación exitosa! El examen del postulante con código ${inputCodigo.value.trim()} ha sido anulado correctamente.`);

            // Reset del Formulario
            const form = document.getElementById('form-anulacion');
            if (form) form.reset();
            if (toggleConfirmacion) toggleConfirmacion.checked = false;

            const textoArchivo = document.getElementById('anulacion-texto-archivo');
            if (textoArchivo) {
                textoArchivo.textContent = "Arrastre o seleccione archivo";
                textoArchivo.className = "text-sm font-bold text-[#0052cc]";
            }

            // Refrescar Dashboard y saltar de vista
            if (typeof jalarDatosDashboard === 'function') jalarDatosDashboard();
            if (typeof navegarModulo === 'function') navegarModulo('dashboard', 'Dashboard global');
        })
        .catch(error => {
            console.error("[ANULACIÓN] Error devuelto por Spring Boot:", error);
            alert(`No se pudo registrar la anulación. Verifique que el código exista en la base de datos para este proceso.`);
        })
        .finally(() => {
            if (btnSubmit) {
                btnSubmit.disabled = false;
                btnSubmit.innerHTML = `<i data-lucide="file-warning" class="w-5 h-5"></i> Ejecutar Anulación`;
                if (typeof lucide !== 'undefined') lucide.createIcons();
                inicializarModuloAnulacion();
            }
        });
}

// Registro Global
window.inicializarModuloAnulacion = inicializarModuloAnulacion;
window.cargarAnulaciones = cargarAnulaciones;

// Auto-disparo preventivo al cargar el script
if (document.readyState === "complete" || document.readyState === "interactive") {
    inicializarModuloAnulacion();
}