<script setup>
import MedicoService from '@/service/MedicoService';
import PacienteService from '@/service/PacienteService';
import TurnoService from '@/service/TurnoService';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, resolveTurnoEstado } from '@/utils/turnoEstado';
import { computed, onMounted, ref } from 'vue';

const medicoService = new MedicoService();
const pacienteService = new PacienteService();
const turnoService = new TurnoService();

const medicos = ref([]);
const pacientes = ref([]);
const turnos = ref([]);

onMounted(async () => {
    const [medicosData, pacientesData, turnosData] = await Promise.all([medicoService.findAll(), pacienteService.findAll(), turnoService.findAll()]);

    medicos.value = medicosData;
    pacientes.value = pacientesData;
    turnos.value = turnosData;
});

const turnosPendientes = computed(() => turnos.value.filter((turno) => resolveTurnoEstado(turno) === 'PENDIENTE').length);
</script>

<template>
    <div class="grid grid-cols-12 gap-6">
        <div class="col-span-12">
            <h2 class="m-0">Panel de administración</h2>
            <p class="text-gray-500 mt-2 mb-0">Vista ejecutiva del sistema médico.</p>
        </div>

        <div class="col-span-12 md:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Médicos</div>
                <div class="text-3xl font-bold">{{ medicos.length }}</div>
            </div>
        </div>
        <div class="col-span-12 md:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Pacientes</div>
                <div class="text-3xl font-bold">{{ pacientes.length }}</div>
            </div>
        </div>
        <div class="col-span-12 md:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Turnos pendientes</div>
                <div class="text-3xl font-bold">{{ turnosPendientes }}</div>
            </div>
        </div>
        <div class="col-span-12">
            <div class="card">
                <h4 class="mt-0">Últimos turnos</h4>
                <DataTable :value="turnos.slice(0, 6)" dataKey="idTurno">
                    <Column field="paciente.apellido" header="Paciente" />
                    <Column field="medico.apellido" header="Médico" />
                    <Column field="estado" header="Estado">
                        <template #body="slotProps">
                            <Tag :value="resolveTurnoEstado(slotProps.data)" :severity="getTurnoEstadoSeverity(resolveTurnoEstado(slotProps.data))" :class="getTurnoEstadoTagClass(resolveTurnoEstado(slotProps.data))" />
                        </template>
                    </Column>
                </DataTable>
            </div>
        </div>
    </div>
</template>
