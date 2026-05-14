<template>
    <div class="col-span-12">
        <div class="grid grid-cols-12 gap-4">

            <!-- Turnos del día -->
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Turnos del día</span>
                    <div class="text-900 font-medium text-xl">{{ turnosHoy }}</div>
                </div>
            </div>

            <!-- Turnos pendientes -->
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Pendientes</span>
                    <div class="text-900 font-medium text-xl">{{ pendientes }}</div>
                </div>
            </div>

            <!-- Pacientes -->
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Pacientes</span>
                    <div class="text-900 font-medium text-xl">{{ pacientes.length }}</div>
                </div>
            </div>

            <!-- Médicos -->
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Médicos</span>
                    <div class="text-900 font-medium text-xl">{{ medicos.length }}</div>
                </div>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import TurnoService from '@/service/TurnoService';

const turnoService = new TurnoService();

// Variables
const turnosHoy = ref(0);
const pendientes = ref(0);
const pacientes = ref([]);
const medicos = ref([]);

onMounted(async () => {
    try {
        const data = await turnoService.findAll();

        console.log("TURNOS:", data); // DEBUG

        const hoy = new Date();

        data.forEach(turno => {
            const fechaTurno = new Date(turno.fechaDeTurno);

            // Turnos del día
            const hoy = new Date();

            if (
                fechaTurno.getDate() === hoy.getDate() &&
                fechaTurno.getMonth() === hoy.getMonth() &&
                fechaTurno.getFullYear() === hoy.getFullYear()
            ) {
                turnosHoy.value++;
            }

            // Pendientes (futuros)
            if (fechaTurno > new Date()) {
                pendientes.value++;
            }

            // Pacientes únicos
            if (turno.paciente?.nombre && !pacientes.value.includes(turno.paciente.nombre)) {
                pacientes.value.push(turno.paciente.nombre);
            }

            if (turno.medico?.nombre && !medicos.value.includes(turno.medico.nombre)) {
                medicos.value.push(turno.medico.nombre);
            }
        });

    } catch (error) {
        console.error("Error cargando estadísticas:", error);
    }
});
</script>