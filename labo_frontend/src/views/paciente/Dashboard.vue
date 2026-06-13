<script setup>
import EstudioService from '@/service/EstudioService';
import TurnoService from '@/service/TurnoService';
import { useAuthStore } from '@/stores/auth';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, resolveTurnoEstado } from '@/utils/turnoEstado';
import { computed, onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const estudioService = new EstudioService();
const authStore = useAuthStore();
const pacienteId = computed(() => (authStore.rol === 'paciente' && authStore.token ? authStore.token : 1));
const turnos = ref([]);
const estudios = ref([]);

onMounted(async () => {
    const [turnosData, estudiosData] = await Promise.all([turnoService.findAllByPaciente({ idPaciente: pacienteId.value }), estudioService.findAllByPaciente({ idPaciente: pacienteId.value })]);
    turnos.value = turnosData;
    estudios.value = estudiosData;
});

const turnosActivos = computed(() => turnos.value.filter((turno) => !['CANCELADO', 'PERDIDO'].includes(resolveTurnoEstado(turno))).length);
</script>

<template>
    <div class="grid grid-cols-12 gap-6">
        <div class="col-span-12">
            <h2 class="m-0">Mi panel</h2>
            <p class="text-gray-500 mt-2 mb-0">Turnos, estudios e historial médico personal.</p>
        </div>
        <div class="col-span-12 md:col-span-6">
            <div class="card">
                <div class="text-gray-500 mb-2">Turnos activos</div>
                <div class="text-3xl font-bold">{{ turnosActivos }}</div>
            </div>
        </div>
        <div class="col-span-12 md:col-span-6">
            <div class="card">
                <div class="text-gray-500 mb-2">Estudios cargados</div>
                <div class="text-3xl font-bold">{{ estudios.length }}</div>
            </div>
        </div>
        <div class="col-span-12">
            <div class="card">
                <h4 class="mt-0">Mis ultimos turnos</h4>
                <DataTable :value="turnos.slice(0, 6)" dataKey="idTurno">
                    <Column field="medico.apellido" header="Médico" />
                    <Column field="medico.especialidad" header="Especialidad" />
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
