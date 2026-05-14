<template>
    <div class="card shadow-md rounded-xl">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold">Pacientes recientes</h2>
        </div>

        <ul class="divide-y">
            <li 
                v-for="paciente in pacientes" 
                :key="paciente"
                class="py-3 flex justify-between items-center"
            >
                <span class="font-medium">{{ paciente }}</span>
                <span class="text-sm text-green-500 font-semibold">Activo</span>
            </li>
        </ul>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import TurnoService from '@/service/TurnoService';

const turnoService = new TurnoService();
const pacientes = ref([]);

onMounted(async () => {
    try {
        const data = await turnoService.findAll();

        const pacientesUnicos = [];

        data.forEach(turno => {
            if (turno.paciente && turno.paciente.nombre) {
                const nombre = turno.paciente.nombre;

                if (!pacientesUnicos.includes(nombre)) {
                    pacientesUnicos.push(nombre);
                }
            }
        });

        pacientes.value = pacientesUnicos;

    } catch (error) {
        console.error("Error cargando pacientes:", error);
    }
});
</script>