<script setup>
import TurnoService from '@/service/TurnoService';
import { useAuthStore } from '@/stores/auth';
import { computed, onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const authStore = useAuthStore();
const medicoId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : 1));
const pacientes = ref([]);

onMounted(async () => {
    const turnos = await turnoService.findAllByMedico({ idMedico: medicoId.value });
    const map = new Map();
    turnos.forEach((turno) => {
        if (turno.paciente?.idPaciente) {
            map.set(turno.paciente.idPaciente, turno.paciente);
        }
    });
    pacientes.value = Array.from(map.values());
});
</script>

<template>
    <div class="card">
        <h4 class="mt-0">Pacientes asignados</h4>
        <DataTable :value="pacientes" dataKey="idPaciente" :paginator="true" :rows="10">
            <Column field="apellido" header="Apellido" sortable />
            <Column field="nombre" header="Nombre" sortable />
            <Column field="dni" header="DNI" sortable />
            <Column field="email" header="Email" sortable />
            <Column field="telefono" header="Teléfono" sortable />
        </DataTable>
    </div>
</template>
