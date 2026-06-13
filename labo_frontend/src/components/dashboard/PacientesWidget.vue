<script setup>
import TurnoService from '@/service/TurnoService';
import { onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const pacientes = ref([]);

onMounted(async () => {
    try {
        const data = await turnoService.findAll();
        pacientes.value = [...new Set(data.map((turno) => turno.paciente?.nombre).filter(Boolean))];
    } catch {
        pacientes.value = [];
    }
});
</script>

<template>
    <div class="card shadow-md rounded-xl">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold">Pacientes recientes</h2>
        </div>

        <ul class="divide-y">
            <li v-for="paciente in pacientes" :key="paciente" class="py-3 flex justify-between items-center">
                <span class="font-medium">{{ paciente }}</span>
                <span class="text-sm text-green-500 font-semibold">Activo</span>
            </li>
        </ul>
    </div>
</template>
