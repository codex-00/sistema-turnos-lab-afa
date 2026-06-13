<script setup>
import TurnoService from '@/service/TurnoService';
import { useAuthStore } from '@/stores/auth';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, resolveTurnoEstado } from '@/utils/turnoEstado';
import { computed, onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const authStore = useAuthStore();
const medicoId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : 1));
const turnos = ref([]);

onMounted(async () => {
    turnos.value = await turnoService.findAllByMedico({ idMedico: medicoId.value });
});

const contarTurnosPorEstado = (estados) => turnos.value.filter((turno) => estados.includes(resolveTurnoEstado(turno))).length;

const turnosActivos = computed(() => contarTurnosPorEstado(['PENDIENTE', 'REPROGRAMADO']));
const turnosPerdidos = computed(() => contarTurnosPorEstado(['PERDIDO']));
const turnosCancelados = computed(() => contarTurnosPorEstado(['CANCELADO']));
const turnosRealizados = computed(() => contarTurnosPorEstado(['REALIZADO']));
const pacientes = computed(() => new Set(turnos.value.map((turno) => turno.paciente?.idPaciente).filter(Boolean)).size);
</script>

<template>
    <div class="grid grid-cols-12 gap-6">
        <div class="col-span-12">
            <h2 class="m-0">Panel médico</h2>
            <p class="text-gray-500 mt-2 mb-0">Gestión diaria de agenda, turnos y pacientes.</p>
        </div>
        <div class="col-span-12 sm:col-span-6 xl:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Turnos activos</div>
                <div class="text-3xl font-bold text-orange-600">{{ turnosActivos }}</div>
            </div>
        </div>
        <div class="col-span-12 sm:col-span-6 xl:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Turnos perdidos</div>
                <div class="text-3xl font-bold text-gray-600">{{ turnosPerdidos }}</div>
            </div>
        </div>
        <div class="col-span-12 sm:col-span-6 xl:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Turnos cancelados</div>
                <div class="text-3xl font-bold text-red-600">{{ turnosCancelados }}</div>
            </div>
        </div>
        <div class="col-span-12 sm:col-span-6 xl:col-span-3">
            <div class="card">
                <div class="text-gray-500 mb-2">Turnos realizados</div>
                <div class="text-3xl font-bold text-green-600">{{ turnosRealizados }}</div>
            </div>
        </div>
        <div class="col-span-12">
            <div class="card">
                <div class="text-gray-500 mb-2">Pacientes asignados</div>
                <div class="text-3xl font-bold">{{ pacientes }}</div>
            </div>
        </div>
        <div class="col-span-12">
            <div class="card">
                <h4 class="mt-0">Próximos turnos</h4>
                <DataTable :value="turnos.slice(0, 8)" dataKey="idTurno">
                    <Column field="paciente.apellido" header="Apellido" />
                    <Column field="paciente.nombre" header="Nombre" />
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
