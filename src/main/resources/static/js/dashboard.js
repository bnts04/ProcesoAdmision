/**
 * Lógica de la pantalla de Dashboard Global
 */
function jalarDatosDashboard() {
    fetch("http://localhost:8080/api/resultados/proceso/1")
        .then(res => res.json())
        .then(data => {
            // TOTAL POSTULANTES
            document.getElementById('dash-total').textContent = data.length;

            // INGRESANTES
            const ingresantes = data.filter(a => a.condicion === "INGRESO").length;
            document.getElementById('dash-ingresantes').textContent = ingresantes;

            // NO INGRESANTES
            const noIngresantes = data.length - ingresantes;
            document.getElementById('dash-noingresantes').textContent = noIngresantes;

            // MAYOR PUNTAJE
            const maxPuntaje = Math.max(...data.map(a => a.puntajeFinal));
            document.getElementById('dash-max').textContent = maxPuntaje.toFixed(2);

            // TABLA DE LISTADO PRELIMINAR
            const tbody = document.getElementById('dash-tabla-body');
            tbody.innerHTML = data.map((al, index) => `
                <tr class="border-b border-gray-100 hover:bg-gray-50">
                    <td class="p-3 text-center">${index + 1}</td>
                    <td class="p-3">${al.codigo}</td>
                    <td class="p-3">${al.apellidosNombres}</td>
                    <td class="p-3 text-center font-bold">${al.puntajeFinal}</td>
                    <td class="p-3 text-center">${al.ome}</td>
                    <td class="p-3 text-center">${al.omg}</td>
                    <td class="p-3 text-center">${al.condicion}</td>
                </tr>
            `).join('');
        })
        .catch(err => {
            console.log("ERROR:", err);
        });
}