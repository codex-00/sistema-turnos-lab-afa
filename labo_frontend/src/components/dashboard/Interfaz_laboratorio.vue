<script setup>
import TurnoService from '@/service/TurnoService';
import { onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const turnosHoy = ref(0);
const pendientes = ref(0);
const pacientes = ref([]);
const medicos = ref([]);

onMounted(async () => {
    try {
        const data = await turnoService.findAll();
        const hoy = new Date();

        data.forEach((turno) => {
            const fechaTurno = new Date(turno.fechaDeTurno);

            if (fechaTurno.getDate() === hoy.getDate() && fechaTurno.getMonth() === hoy.getMonth() && fechaTurno.getFullYear() === hoy.getFullYear()) {
                turnosHoy.value++;
            }

            if (fechaTurno > new Date()) {
                pendientes.value++;
            }

            if (turno.paciente?.nombre && !pacientes.value.includes(turno.paciente.nombre)) {
                pacientes.value.push(turno.paciente.nombre);
            }

            if (turno.medico?.nombre && !medicos.value.includes(turno.medico.nombre)) {
                medicos.value.push(turno.medico.nombre);
            }
        });
    } catch {
        turnosHoy.value = 0;
        pendientes.value = 0;
        pacientes.value = [];
        medicos.value = [];
    }
});
</script>

<template>
    <div class="col-span-12">
        <div class="grid grid-cols-12 gap-4">
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Turnos del dia</span>
                    <div class="text-900 font-medium text-xl">{{ turnosHoy }}</div>
                </div>
            </div>

            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Pendientes</span>
                    <div class="text-900 font-medium text-xl">{{ pendientes }}</div>
                </div>
            </div>

            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Pacientes</span>
                    <div class="text-900 font-medium text-xl">{{ pacientes.length }}</div>
                </div>
            </div>

            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="card">
                    <span class="block text-500 font-medium mb-3">Médicos</span>
                    <div class="text-900 font-medium text-xl">{{ medicos.length }}</div>
                </div>
            </div>
        </div>
    </div>
</template>
