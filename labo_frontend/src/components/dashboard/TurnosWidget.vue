<template>
    <div class="card shadow-md rounded-xl">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold">Turnos recientes</h2>
        </div>

    <table class="w-full border-collapse">
        <thead>
            <tr class="bg-gray-100 text-left">
                <th class="p-3">Paciente</th>
                <th class="p-3">Fecha</th>
                <th class="p-3">Hora</th>
                <th class="p-3">Estado</th>
            </tr>
        </thead>

        <tbody>
            <tr 
                v-for="turno in turnos" 
                :key="turno.id"
                class="border-b hover:bg-gray-50 transition"
            >
                <td class="p-3 font-medium">{{ turno.paciente?.nombre }}</td>

                <td class="p-3">
                    {{ new Date(turno.fechaDeTurno).toLocaleDateString() }}
                </td>

                <td class="p-3">
                    {{ new Date(turno.fechaDeTurno).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
                </td>

                <td class="p-3">
                    <span 
                        class="px-2 py-1 rounded text-sm font-semibold"
                        :class="{
                            'bg-green-100 text-green-600': new Date(turno.fechaDeTurno) > new Date(),
                            'bg-gray-200 text-gray-600': new Date(turno.fechaDeTurno) < new Date()
                        }"
                    >
                        {{ new Date(turno.fechaDeTurno) > new Date() ? 'Pendiente' : 'Finalizado' }}
                    </span>
                 </td>
             </tr>
            </tbody>
        </table>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import TurnoService from '@/service/TurnoService';

const turnos = ref([]);
const turnoService = new TurnoService();

onMounted(async () => {
    try {
        turnos.value = await turnoService.findAll();
    } catch (error) {
        console.error('Error cargando turnos:', error);
    }
});
</script>