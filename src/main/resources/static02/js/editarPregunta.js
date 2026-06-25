const API_BASE_URL = 'http://localhost:8080/api/banco-preguntas';
let preguntaSeleccionadaId = null;
let imagenModificada = false;
let imagenEliminarPendiente = false;

const subcursosPorComponente = {
  'CTA': [
    { value: 'FISICA', label: 'Física' },
    { value: 'QUIMICA', label: 'Química' },
    { value: 'BIOLOGIA', label: 'Biología' }
  ],
  'MATEMATICA': [
    { value: 'ALGEBRA', label: 'Álgebra' },
    { value: 'ARITMETICA', label: 'Aritmética' },
    { value: 'GEOMETRIA', label: 'Geometría' },
    { value: 'TRIGONOMETRIA', label: 'Trigonometría' }
  ],
  'HUMANIDADES': [
    { value: 'HISTORIA', label: 'Historia' },
    { value: 'LENGUAJE', label: 'Lenguaje' },
    { value: 'LITERATURA', label: 'Literatura' },
    { value: 'ECONOMIA', label: 'Economía' },
    { value: 'EDUCACION_CIVICA', label: 'Educación Cívica' },
    { value: 'PSICOLOGIA', label: 'Psicología' },
    { value: 'GEOGRAFIA', label: 'Geografía' }
  ],
  'RAZONAMIENTO_VERBAL': [
    { value: 'RAZONAMIENTO_VERBAL', label: 'Razonamiento Verbal General' }
  ],
  'RAZONAMIENTO_MATEMATICO': [
    { value: 'RAZONAMIENTO_MATEMATICO', label: 'Razonamiento Matemático General' }
  ]
};

const seccionListado = document.getElementById('seccionListado');
const seccionFormulario = document.getElementById('seccionFormulario');
const tablaPreguntasBody = document.getElementById('tablaPreguntasBody');

const formEditarPregunta = document.getElementById('formEditarPregunta');
const txtCodigoPregunta = document.getElementById('txtCodigoPregunta');
const editComponente = document.getElementById('editComponente');
const editSubcurso = document.getElementById('editSubcurso');
const editEstado = document.getElementById('editEstado');
const editEnunciado = document.getElementById('editEnunciado');
const editRespuestaCorrecta = document.getElementById('editRespuestaCorrecta');

const editAltA = document.getElementById('editAltA');
const editAltB = document.getElementById('editAltB');
const editAltC = document.getElementById('editAltC');
const editAltD = document.getElementById('editAltD');
const editAltE = document.getElementById('editAltE');

const inputFileImagen = document.getElementById('inputFileImagen');
const imgPreviewSrc = document.getElementById('imgPreviewSrc');
const fallbackNoImg = document.getElementById('fallbackNoImg');
const btnCambiarImg = document.getElementById('btnCambiarImg');
const btnQuitarImg = document.getElementById('btnQuitarImg');

const liveEnunciado = document.getElementById('liveEnunciado');
const liveContenedorImg = document.getElementById('liveContenedorImg');
const liveImgSrc = document.getElementById('liveImgSrc');
const previewOptA = document.getElementById('previewOptA');
const previewOptB = document.getElementById('previewOptB');
const previewOptC = document.getElementById('previewOptC');
const previewOptD = document.getElementById('previewOptD');
const previewOptE = document.getElementById('previewOptE');

document.addEventListener('DOMContentLoaded', () => {
    cargarListaPreguntas();
    vincularEscuchasLivePreview();

    editComponente.addEventListener('change', () => {
        actualizarSelectSubcursos(editComponente.value, null);
    });

    editEstado.addEventListener('change', actualizarColorEstado);
});


async function cargarListaPreguntas() {
    try {

        const response = await fetch(`${API_BASE_URL}?_=${Date.now()}`, {
            method: 'GET',
            headers: {
                'Cache-Control': 'no-cache',
                'Pragma': 'no-cache'
            }
        });

        if (response.ok) {
            const preguntas = await response.json();
            renderizarTablaPreguntas(preguntas);
        } else {
            tablaPreguntasBody.innerHTML = `<tr><td colspan="6" class="px-6 py-4 text-center text-red-500">Error al procesar el listado del servidor.</td></tr>`;
        }
    } catch (error) {
        console.error(error);
        tablaPreguntasBody.innerHTML = `<tr><td colspan="6" class="px-6 py-4 text-center text-red-500">Error crítico de red. ¿El backend está encendido?</td></tr>`;
    }
}

function renderizarTablaPreguntas(lista) {
    if (!lista || lista.length === 0) {
        tablaPreguntasBody.innerHTML = `<tr><td colspan="6" class="px-6 py-8 text-center text-slate-400 italic">No existen preguntas registradas en el banco.</td></tr>`;
        return;
    }

    tablaPreguntasBody.innerHTML = '';
    lista.forEach(p => {
        const tr = document.createElement('tr');
        tr.className = "hover:bg-slate-50 transition border-b border-slate-100";

        const esActiva = (p.estado === "ACTIVA");
        const badgeEstado = esActiva
            ? `<span class="bg-green-100 text-green-700 px-2.5 py-1 rounded-full text-xs font-bold border border-green-200">ACTIVA</span>`
            : `<span class="bg-red-100 text-red-700 px-2.5 py-1 rounded-full text-xs font-bold border border-red-200">INACTIVA</span>`;

        tr.innerHTML = `
            <td class="px-6 py-4 font-semibold text-slate-700 font-mono">${p.codigo || '#' + p.id}</td>
            <td class="px-6 py-4 text-slate-600 font-medium">${p.nombreComponente || p.componente}</td>
            <td class="px-6 py-4 text-slate-500">${p.nombreSubcurso || p.subcurso || 'General'}</td>
            <td class="px-6 py-4 text-slate-700 max-w-xs truncate">${p.enunciado}</td>
            <td class="px-6 py-4 text-center">${badgeEstado}</td>
            <td class="px-6 py-4 text-center">
                <button type="button" onclick="abrirEdicionPregunta(${p.id})" class="px-3 py-1.5 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-lg font-medium text-xs transition">
                    Seleccionar y Editar
                </button>
            </td>
        `;
        tablaPreguntasBody.appendChild(tr);
    });
}

function actualizarSelectSubcursos(componenteKey, subcursoSeleccionado) {
    editSubcurso.innerHTML = '';
    const lista = subcursosPorComponente[componenteKey] || [];

    if (lista.length === 0) {
        editSubcurso.innerHTML = `<option value="">General (Sin subcursos)</option>`;
        return;
    }

    lista.forEach(sub => {
        const opcion = document.createElement('option');
        opcion.value = sub.value;
        opcion.textContent = sub.label;
        if (sub.value === subcursoSeleccionado) {
            opcion.selected = true;
        }
        editSubcurso.appendChild(opcion);
    });
}

async function abrirEdicionPregunta(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`);
        if (!response.ok) throw new Error("No se pudo obtener la pregunta.");

        const pregunta = await response.json();
        preguntaSeleccionadaId = id;
        imagenModificada = false;
        imagenEliminarPendiente = false;

        txtCodigoPregunta.innerText = pregunta.codigo ? `CÓDIGO: ${pregunta.codigo}` : `ID: ${pregunta.id}`;
        editEnunciado.value = pregunta.enunciado;

        editEstado.value = (pregunta.estado === "ACTIVA") ? "true" : "false";
        editRespuestaCorrecta.value = pregunta.respuestaCorrecta;

        editComponente.disabled = false;
        editSubcurso.disabled = false;

        editComponente.innerHTML = `
            <option value="CTA" ${pregunta.componente === 'CTA' ? 'selected' : ''}>Ciencia, Tecnología y Ambiente</option>
            <option value="MATEMATICA" ${pregunta.componente === 'MATEMATICA' ? 'selected' : ''}>Matemática</option>
            <option value="HUMANIDADES" ${pregunta.componente === 'HUMANIDADES' ? 'selected' : ''}>Humanidades</option>
            <option value="RAZONAMIENTO_VERBAL" ${pregunta.componente === 'RAZONAMIENTO_VERBAL' ? 'selected' : ''}>Razonamiento Verbal</option>
            <option value="RAZONAMIENTO_MATEMATICO" ${pregunta.componente === 'RAZONAMIENTO_MATEMATICO' ? 'selected' : ''}>Razonamiento Matemático</option>
        `;

        actualizarSelectSubcursos(pregunta.componente, pregunta.subcurso);

        if (pregunta.alternativas && pregunta.alternativas.length >= 5) {
            editAltA.value = pregunta.alternativas[0].texto;
            editAltB.value = pregunta.alternativas[1].texto;
            editAltC.value = pregunta.alternativas[2].texto;
            editAltD.value = pregunta.alternativas[3].texto;
            editAltE.value = pregunta.alternativas[4].texto;
        }

        if (pregunta.imagenUrl) {
            const fullImgUrl = pregunta.imagenUrl.startsWith('http') ? pregunta.imagenUrl : `http://localhost:8080${pregunta.imagenUrl}`;
            imgPreviewSrc.src = fullImgUrl;
            imgPreviewSrc.classList.remove('hidden');
            fallbackNoImg.classList.add('hidden');
            btnQuitarImg.classList.remove('hidden');

            liveImgSrc.src = fullImgUrl;
            liveContenedorImg.classList.remove('hidden');
        } else {
            limpiarContenedoresImagen();
        }

        seccionListado.classList.add('hidden');
        seccionFormulario.classList.remove('hidden');

        actualizarColorEstado();
        actualizarLivePreview();

    } catch (error) {
        console.error(error);
        alert("No se pudo cargar la información detallada de la pregunta.");
    }
}

btnCambiarImg.addEventListener('click', () => inputFileImagen.click());
inputFileImagen.addEventListener('change', () => {
    const file = inputFileImagen.files[0];
    if (file) {
        imagenModificada = true;
        imagenEliminarPendiente = false;

        const reader = new FileReader();
        reader.onload = function(e) {
            imgPreviewSrc.src = e.target.result;
            imgPreviewSrc.classList.remove('hidden');
            fallbackNoImg.classList.add('hidden');
            btnQuitarImg.classList.remove('hidden');

            liveImgSrc.src = e.target.result;
            liveContenedorImg.classList.remove('hidden');
        }
        reader.readAsDataURL(file);
    }
});

btnQuitarImg.addEventListener('click', () => {
    imagenModificada = true;
    imagenEliminarPendiente = true;
    inputFileImagen.value = '';
    limpiarContenedoresImagen();
});

function limpiarContenedoresImagen() {
    imgPreviewSrc.src = '';
    imgPreviewSrc.classList.add('hidden');
    fallbackNoImg.classList.remove('hidden');
    btnQuitarImg.classList.add('hidden');

    liveImgSrc.src = '';
    liveContenedorImg.classList.add('hidden');
}

document.getElementById('btnVolver').addEventListener('click', regresarAListado);
document.getElementById('btnCancelarEdicion').addEventListener('click', regresarAListado);

function regresarAListado() {
    seccionFormulario.classList.add('hidden');
    seccionListado.classList.remove('hidden');
    formEditarPregunta.reset();
    cargarListaPreguntas();
}

formEditarPregunta.addEventListener('submit', async (e) => {
    e.preventDefault();

    const btnGuardar = document.getElementById('btnGuardarCambios');
    btnGuardar.disabled = true;
    const textoOriginalBtn = btnGuardar.innerText;
    btnGuardar.innerText = 'Actualizando...';

    const formData = new FormData();
    formData.append('enunciado', editEnunciado.value);


    const estadoString = (editEstado.value === "true") ? "ACTIVA" : "INACTIVA";
    formData.append('estado', estadoString);

    formData.append('respuestaCorrecta', editRespuestaCorrecta.value);

    formData.append('alternativaA', editAltA.value);
    formData.append('alternativaB', editAltB.value);
    formData.append('alternativaC', editAltC.value);
    formData.append('alternativaD', editAltD.value);
    formData.append('alternativaE', editAltE.value);

    formData.append('imagenModificada', imagenModificada);
    formData.append('eliminarImagen', imagenEliminarPendiente);

    if (imagenModificada && !imagenEliminarPendiente && inputFileImagen.files[0]) {
        formData.append('imagen', inputFileImagen.files[0]);
    }

    formData.append('componente', editComponente.value);
    formData.append('subcurso', editSubcurso.value);

    try {
        const response = await fetch(`${API_BASE_URL}/${preguntaSeleccionadaId}/con-imagen`, {
            method: 'PUT',
            body: formData
        });

        if (!response.ok) {
            throw new Error('Error al actualizar la pregunta');
        }

        await response.json();
        mostrarNotificacionExito("Pregunta actualizada correctamente.");
        regresarAListado();
    } catch (error) {
        console.error(error);
        alert("Hubo un problema al guardar los cambios.");
    } finally {
        btnGuardar.disabled = false;
        btnGuardar.innerText = textoOriginalBtn;
    }
});


function vincularEscuchasLivePreview() {
    const inputs = [editEnunciado, editAltA, editAltB, editAltC, editAltD, editAltE];
    inputs.forEach(input => input.addEventListener('input', actualizarLivePreview));
    editRespuestaCorrecta.addEventListener('change', actualizarLivePreview);
    editEstado.addEventListener('change', actualizarLivePreview);
}

function actualizarLivePreview() {
    const esPreguntaActiva = editEstado.value === "true";
    const liveCard = document.getElementById('liveCard') || liveEnunciado.closest('.bg-white') || liveEnunciado.parentElement;

    liveEnunciado.innerText = editEnunciado.value.trim() || 'Sin enunciado definido...';
    if (editEnunciado.value.trim()) liveEnunciado.classList.remove('italic', 'text-slate-400');
    else liveEnunciado.classList.add('italic', 'text-slate-400');

    previewOptA.querySelector('span:last-child').innerText = editAltA.value || 'Alternativa A';
    previewOptB.querySelector('span:last-child').innerText = editAltB.value || 'Alternativa B';
    previewOptC.querySelector('span:last-child').innerText = editAltC.value || 'Alternativa C';
    previewOptD.querySelector('span:last-child').innerText = editAltD.value || 'Alternativa D';
    previewOptE.querySelector('span:last-child').innerText = editAltE.value || 'Alternativa E';

    const correcta = editRespuestaCorrecta.value;
    const mapeoViews = { 'A': previewOptA, 'B': previewOptB, 'C': previewOptC, 'D': previewOptD, 'E': previewOptE };

    [previewOptA, previewOptB, previewOptC, previewOptD, previewOptE].forEach(opt => {
        opt.className = "flex items-center space-x-3 p-1.5 rounded-lg border border-transparent";
        const badge = opt.querySelector('span:first-child');
        badge.className = "w-6 h-6 rounded-full border border-slate-300 bg-white flex items-center justify-center text-xs font-medium text-slate-600";
    });

    const activa = mapeoViews[correcta];
    if (activa) {
        activa.className = "flex items-center space-x-3 p-1.5 rounded-lg border border-green-200 bg-green-50 text-green-800 font-medium";
        const badgeActivo = activa.querySelector('span:first-child');
        badgeActivo.className = "w-6 h-6 rounded-full bg-green-600 border border-green-600 flex items-center justify-center text-xs font-bold text-white shadow-sm";
    }

    if (liveCard) {
        const avisoPrevio = liveCard.parentElement.querySelector('.badge-inactivo-live');
        if (avisoPrevio) avisoPrevio.remove();

        if (!esPreguntaActiva) {
            liveCard.classList.add('opacity-60', 'grayscale-[20%]', 'pointer-events-none');
            const avisoInactivo = document.createElement('div');
            avisoInactivo.className = "badge-inactivo-live mt-2 text-center text-xs bg-red-100 text-red-700 font-bold py-1 px-3 rounded-lg border border-red-200 animate-pulse";
            avisoInactivo.innerText = "⚠️ VISTA PREVIA: Esta pregunta se guardará como INACTIVA y ocultada del alumno.";
            liveCard.after(avisoInactivo);
        } else {
            liveCard.classList.remove('opacity-60', 'grayscale-[20%]', 'pointer-events-none');
        }
    }
}

function mostrarNotificacionExito(mensaje) {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = `flex items-center p-4 mb-4 w-full max-w-xs text-gray-800 bg-white rounded-xl shadow-lg border-l-4 border-green-500 transform translate-x-full transition-all duration-300 ease-out pointer-events-auto opacity-0`;
    toast.innerHTML = `
        <div class="inline-flex flex-shrink-0 justify-center items-center w-8 h-8 text-green-500 bg-green-100 rounded-lg">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
            </svg>
        </div>
        <div class="ml-3 text-sm font-medium pr-2">${mensaje}</div>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.classList.remove('translate-x-full', 'opacity-0'), 10);
    setTimeout(() => {
        toast.classList.add('translate-x-full', 'opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function actualizarColorEstado() {
    const esActivo = editEstado.value === "true";
    editEstado.classList.remove('bg-green-50', 'border-green-500', 'text-green-700', 'focus:border-green-500', 'bg-red-50', 'border-red-500', 'text-red-700', 'focus:border-red-500');

    if (esActivo) {
        editEstado.classList.add('bg-green-50', 'border-green-500', 'text-green-700', 'focus:border-green-500');
    } else {
        editEstado.classList.add('bg-red-50', 'border-red-500', 'text-red-700', 'focus:border-red-500');
    }
}