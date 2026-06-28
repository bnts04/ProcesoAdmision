// --- ACTIVAR ITEM DEL SIDEBAR SEGÚN LA PÁGINA ACTUAL ---
const paginaActual = window.location.pathname.split("/").pop() || "index.html";
const enlacesSidebar = document.querySelectorAll("#sidebar nav a");

enlacesSidebar.forEach(enlace => {
    const rutaEnlace = enlace.getAttribute("href");

    if (paginaActual === rutaEnlace) {
        // Estilos para el enlace ACTIVO (Azul brillante)
        enlace.className = "flex items-center px-3 py-2.5 bg-blue-600 text-white rounded-lg group font-medium text-sm transition-colors";
    } else {
        // Estilos para los enlaces INACTIVOS (Gris/Azul oscuro)
        // Excepto el de Vista Previa si sigue bloqueado, claro
        if (enlace.id !== "linkVistaPrevia") {
            enlace.className = "flex items-center px-3 py-2.5 text-slate-300 hover:bg-white/10 hover:text-white rounded-lg group font-medium text-sm transition-colors";
        }
    }
});
// --------------------------------------------------------

const API_BASE_URL = 'http://localhost:8080/api/banco-preguntas';

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

const formPregunta = document.getElementById('formPregunta');
const alertContainer = document.getElementById('alertContainer');
const componenteSelect = document.getElementById('componente');
const subcursoSelect = document.getElementById('subcurso');
const respuestaCorrectaSelect = document.getElementById('respuestaCorrecta');
const badgeRespuesta = document.getElementById('badgeRespuesta');
const btnLimpiar = document.getElementById('btnLimpiar');
const btnGuardar = document.getElementById('btnGuardar');

const dropzone = document.getElementById('dropzone');
const inputImagen = document.getElementById('inputImagen');
const nombreArchivoImg = document.getElementById('nombreArchivoImg');
const previewImg = document.getElementById('previewImg');
const previewFallback = document.getElementById('previewFallback');
const btnRemoverImg = document.getElementById('btnRemoverImg');

const txtEnunciado = document.getElementById('enunciado');
const txtAltA = document.getElementById('alternativaA');
const txtAltB = document.getElementById('alternativaB');
const txtAltC = document.getElementById('alternativaC');
const txtAltD = document.getElementById('alternativaD');
const txtAltE = document.getElementById('alternativaE');

const viewEnunciado = document.getElementById('livePreviewEnunciado');
const containerLiveImg = document.getElementById('livePreviewContenedorImagen');
const srcLiveImg = document.getElementById('livePreviewImagenSrc');
const viewA = document.getElementById('livePreviewA');
const viewB = document.getElementById('livePreviewB');
const viewC = document.getElementById('livePreviewC');
const viewD = document.getElementById('livePreviewD');
const viewE = document.getElementById('livePreviewE');

document.addEventListener('DOMContentLoaded', async () => {
  actualizarOpcionesSubcurso();
  actualizarVistaPreviaCompleta();
  await cargarResumenBanco();
});

componenteSelect.addEventListener('change', actualizarOpcionesSubcurso);

function actualizarOpcionesSubcurso() {
  const componenteSeleccionado = componenteSelect.value;
  const subcursos = subcursosPorComponente[componenteSeleccionado] || [];
  subcursoSelect.innerHTML = '';
  subcursos.forEach(sub => {
    const option = document.createElement('option');
    option.value = sub.value;
    option.text = sub.label;
    subcursoSelect.appendChild(option);
  });
}

respuestaCorrectaSelect.addEventListener('change', (e) => {
  badgeRespuesta.innerText = e.target.value;
  marcarRespuestaCorrectaPreview();
});

dropzone.addEventListener('click', () => inputImagen.click());
inputImagen.addEventListener('change', mostrarVistaPreviaImagen);
btnRemoverImg.addEventListener('click', removerImagenSeleccionada);



function mostrarVistaPreviaImagen() {
  const file = inputImagen.files[0];
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
       mostrarAlerta(true, 'La imagen supera el tamaño máximo permitido de 5 MB.', 'error');
       removerImagenSeleccionada(null);
       return;
    }
    nombreArchivoImg.innerText = file.name;
    const reader = new FileReader();
    reader.onload = function(e) {
      previewImg.src = e.target.result;
      previewImg.classList.remove('hidden');
      previewFallback.classList.add('hidden');
      btnRemoverImg.classList.remove('hidden');

      srcLiveImg.src = e.target.result;
      containerLiveImg.classList.remove('hidden');
    }
    reader.readAsDataURL(file);
  }
}

function removerImagenSeleccionada(e) {
  if (e) e.stopPropagation();
  inputImagen.value = '';
  nombreArchivoImg.innerText = 'Ningún archivo seleccionado';
  previewImg.src = '';
  previewImg.classList.add('hidden');
  previewFallback.classList.remove('hidden');
  btnRemoverImg.classList.add('hidden');

  srcLiveImg.src = '';
  containerLiveImg.classList.add('hidden');
}




function actualizarVistaPreviaCompleta() {
  const escucharYCopiar = (input, elementoPreview, placeholder) => {
    input.addEventListener('input', () => {
      elementoPreview.querySelector('span:last-child').innerText = input.value.trim() || placeholder;
      if (!input.value.trim()) {
         elementoPreview.querySelector('span:last-child').classList.add('italic', 'text-slate-400');
      } else {
         elementoPreview.querySelector('span:last-child').classList.remove('italic', 'text-slate-400');
      }
    });
  };

  txtEnunciado.addEventListener('input', () => {
     viewEnunciado.innerText = txtEnunciado.value.trim() || 'Aquí se mostrará el enunciado de la pregunta...';
     if (!txtEnunciado.value.trim()) viewEnunciado.classList.add('italic', 'text-slate-400');
     else viewEnunciado.classList.remove('italic', 'text-slate-400');
  });

  escucharYCopiar(txtAltA, viewA, 'Alternativa A');
  escucharYCopiar(txtAltB, viewB, 'Alternativa B');
  escucharYCopiar(txtAltC, viewC, 'Alternativa C');
  escucharYCopiar(txtAltD, viewD, 'Alternativa D');
  escucharYCopiar(txtAltE, viewE, 'Alternativa E');
  marcarRespuestaCorrectaPreview();
}



function marcarRespuestaCorrectaPreview() {
  const seleccionada = respuestaCorrectaSelect.value;
  const mapeoViews = { 'A': viewA, 'B': viewB, 'C': viewC, 'D': viewD, 'E': viewE };

  [viewA, viewB, viewC, viewD, viewE].forEach(v => {
    v.classList.remove('border-green-500', 'bg-green-50/50');
    const badge = v.querySelector('span:first-child');
    badge.classList.remove('bg-green-600', 'text-white', 'border-green-600');
    badge.classList.add('bg-slate-50', 'text-slate-500', 'border-slate-300');
  });

  const viewActiva = mapeoViews[seleccionada];
  if (viewActiva) {
    viewActiva.classList.add('border-green-500', 'bg-green-50/50');
    const badgeActivo = viewActiva.querySelector('span:first-child');
    badgeActivo.classList.remove('bg-slate-50', 'text-slate-500', 'border-slate-300');
    badgeActivo.classList.add('bg-green-600', 'text-white', 'border-green-600');
  }
}



async function cargarResumenBanco() {
  try {
    const response = await fetch(`${API_BASE_URL}/resumen`);
    if (response.ok) {
      const data = await response.json();

      console.log("Datos del backend:", data);

      document.getElementById('resumenTotal').innerText = data.totalPreguntasActivas ?? 0;

      if (data.componentes && Array.isArray(data.componentes)) {
        data.componentes.forEach(comp => {

          switch (comp.componente) {
            case 'CTA':
              document.getElementById('resumenCTA').innerText = comp.totalPreguntas ?? 0;
              break;
            case 'HUMANIDADES':
              document.getElementById('resumenHUMANIDADES').innerText = comp.totalPreguntas ?? 0;
              break;
            case 'MATEMATICA':
              document.getElementById('resumenMATEMATICA').innerText = comp.totalPreguntas ?? 0;
              break;
            case 'RAZONAMIENTO_VERBAL':
              document.getElementById('resumenRV').innerText = comp.totalPreguntas ?? 0;
              break;
            case 'RAZONAMIENTO_MATEMATICO':
              document.getElementById('resumenRM').innerText = comp.totalPreguntas ?? 0;
              break;
          }
        });
      }
    } else {
      console.error(`Error en la respuesta del servidor: Código ${response.status}`);
    }
  } catch (error) {
    console.error('No se pudo conectar con el endpoint de resumen:', error);
  }
}


formPregunta.addEventListener('submit', async (e) => {
  e.preventDefault();
  mostrarAlerta(false);
  btnGuardar.disabled = true;
  btnGuardar.innerText = 'Guardando...';

  const formData = new FormData();
  formData.append('componente', componenteSelect.value);
  formData.append('subcurso', subcursoSelect.value);
  formData.append('enunciado', txtEnunciado.value);
  formData.append('alternativaA', txtAltA.value);
  formData.append('alternativaB', txtAltB.value);
  formData.append('alternativaC', txtAltC.value);
  formData.append('alternativaD', txtAltD.value);
  formData.append('alternativaE', txtAltE.value);
  formData.append('respuestaCorrecta', respuestaCorrectaSelect.value);

  if (inputImagen.files[0]) {
    formData.append('imagen', inputImagen.files[0]);
  }

  try {
    const response = await fetch(`${API_BASE_URL}/con-imagen`, {
      method: 'POST',
      body: formData
    });

    if (response.ok || response.status === 201) {
      mostrarNotificacionExito('¡Pregunta ingresada de forma exitosa en el banco!');
      limpiarFormulario();
      await cargarResumenBanco();
    } else {
      const resultado = await response.json();
      mostrarAlerta(true, resultado.mensaje || 'Error al procesar la solicitud.', 'error');
    }
  } catch (error) {
    console.error(error);
    mostrarAlerta(true, 'Error crítico de comunicación con el backend.', 'error');
  } finally {
    btnGuardar.disabled = false;
    btnGuardar.innerText = 'Guardar en banco';
  }
});

btnLimpiar.addEventListener('click', limpiarFormulario);




function limpiarFormulario() {
  formPregunta.reset();
  removerImagenSeleccionada(null);
  actualizarOpcionesSubcurso();
  badgeRespuesta.innerText = respuestaCorrectaSelect.value;
  mostrarAlerta(false);

  viewEnunciado.innerText = 'Aquí se mostrará el enunciado de la pregunta...';
  viewEnunciado.classList.add('italic', 'text-slate-400');

  const reinicios = [
    { v: viewA, txt: 'Alternativa A' },
    { v: viewB, txt: 'Alternativa B' },
    { v: viewC, txt: 'Alternativa C' },
    { v: viewD, txt: 'Alternativa D' },
    { v: viewE, txt: 'Alternativa E' }
  ];
  reinicios.forEach(item => {
    item.v.querySelector('span:last-child').innerText = item.txt;
    item.v.querySelector('span:last-child').classList.add('italic', 'text-slate-400');
  });
  marcarRespuestaCorrectaPreview();
}

function mostrarAlerta(visible, mensaje = '', tipo = 'success') {
  if (!visible) {
    alertContainer.classList.add('hidden');
    return;
  }
  alertContainer.innerText = mensaje;
  alertContainer.classList.remove('hidden', 'bg-green-100', 'text-green-700', 'bg-red-100', 'text-red-700');
  if (tipo === 'success') {
    alertContainer.classList.add('bg-green-100', 'text-green-700');
  } else {
    alertContainer.classList.add('bg-red-100', 'text-red-700');
  }
}


function mostrarNotificacionExito(mensaje) {
  const container = document.getElementById('toastContainer');

  const toast = document.createElement('div');
  toast.className = `flex items-center p-4 mb-4 w-full max-w-xs text-gray-800 bg-white rounded-xl shadow-lg border-l-4 border-green-500 transform translate-x-full transition-all duration-300 ease-out pointer-events-auto opacity-0`;

  toast.innerHTML = `
    <div class="inline-flex flex-shrink-0 justify-center items-center w-8 h-8 text-green-500 bg-green-100 rounded-lg">
      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
      </svg>
    </div>
    <div class="ml-3 text-sm font-medium pr-2">${mensaje}</div>
    <button type="button" class="ml-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 rounded-lg p-1.5 inline-flex h-8 w-8" onclick="this.parentElement.remove()">
      <span class="sr-only">Cerrar</span>
      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
    </button>
  `;

  container.appendChild(toast);



//Alert de confirmacion de pregunta ingresada
  setTimeout(() => {
    toast.classList.remove('translate-x-full', 'opacity-0');
  }, 10);

  setTimeout(() => {
    toast.classList.add('translate-x-full', 'opacity-0');
    setTimeout(() => { toast.remove(); }, 300);
  }, 4000);
}