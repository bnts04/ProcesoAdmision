/**
 * Servicios de comunicación directa con las consultas del Backend
 */

function cargarCarreras() {
    fetch("http://localhost:8080/api/resultados/proceso/1/resumen-carreras")
        .then(res => res.json())
        .then(carreras => {
            const selector = document.getElementById("selector-carrera");
            // Mantener la opción por defecto limpia antes de rellenar
            selector.innerHTML = '<option value=""> GENERAL </option>';

            carreras.forEach(c => {
                selector.innerHTML += `
                    <option value="${c.carrera}">
                        ${c.carrera}
                    </option>
                `;
            });
        })
        .catch(error => {
            console.log("Error cargando carreras:", error);
        });
}

function filtrarPorCarrera() {
    const carrera = document.getElementById("selector-carrera").value;

    fetch(`http://localhost:8080/api/resultados/proceso/1/carrera?nombre=${encodeURIComponent(carrera)}`)
        .then(res => res.json())
        .then(data => {
            // TARJETAS DE MÉTRICAS
            document.getElementById("vp-total").textContent = data.length;

            const ingresantes = data.filter(a => a.condicion === "INGRESO").length;
            const noIngresantes = data.filter(a => a.condicion === "NO_INGRESO").length;

            document.getElementById("vp-ingresantes").textContent = ingresantes;
            document.getElementById("vp-noingresantes").textContent = noIngresantes;

            const maximo = Math.max(...data.map(a => a.puntajeFinal));
            document.getElementById("vp-max").textContent = maximo.toFixed(4);

            const promedio = data.reduce((acc, a) => acc + a.puntajeFinal, 0) / data.length;
            document.getElementById("vp-promedio").textContent = promedio.toFixed(4);

            // RENDERIZADO DE TABLA VISTA PREVIA
            const tbody = document.getElementById("tabla-vista-previa");
            tbody.innerHTML = data.map((al, index) => `
                <tr class="border-b border-gray-100 hover:bg-gray-50">
                    <td class="p-3 text-center">${index + 1}</td>
                    <td class="p-3">${al.codigo}</td>
                    <td class="p-3">${al.apellidosNombres}</td>
                    <td class="p-3 text-center">${al.puntajeFinal}</td>
                    <td class="p-3 text-center">${al.ome}</td>
                    <td class="p-3 text-center">${al.omg}</td>
                    <td class="p-3 text-center">${al.condicion}</td>
                </tr>
            `).join('');
        })
        .catch(error => {
            console.log("Error:", error);
        });
}