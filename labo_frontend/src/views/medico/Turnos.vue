<script setup>
import AgendaService from '@/service/AgendaService';
import TurnoService from '@/service/TurnoService';
import { useAuthStore } from '@/stores/auth';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, isTurnoCancelado, isTurnoPerdido, isTurnoRealizado, isTurnoReprogramable, resolveTurnoEstado } from '@/utils/turnoEstado';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const agendaService = new AgendaService();
const turnoService = new TurnoService();
const toast = useToast();
const authStore = useAuthStore();

const medicoId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : 1));
const dt = ref();
const turnos = ref([]);
const turno = ref({});
const agenda = ref([]);
const turnoDialog = ref(false);
const submitted = ref(false);
const loadingAgenda = ref(false);
const minFecha = ref(new Date());
const selectedTurnos = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

onMounted(async () => {
    await cargarTurnos();
});

const cargarTurnos = async () => {
    try {
        turnos.value = await turnoService.findAllByMedico({ idMedico: medicoId.value });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los turnos', life: 3000 });
    }
};

function editTurno(tur) {
    if (isTurnoCancelado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede reprogramar un turno cancelado', life: 3000 });
        return;
    }
    if (isTurnoPerdido(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede reprogramar un turno perdido', life: 3000 });
        return;
    }

    turno.value = {
        ...tur,
        medicoActual: `${tur.medico?.nombre || ''} ${tur.medico?.apellido || ''}`.trim(),
        medico: tur.medico?.idMedico || medicoId.value,
        fecha: null,
        agendaSeleccionada: null
    };
    agenda.value = [];
    submitted.value = false;
    turnoDialog.value = true;
}

function hideDialog() {
    turnoDialog.value = false;
    submitted.value = false;
    agenda.value = [];
}

const saveTurno = async () => {
    submitted.value = true;

    if (!turno.value.medico || !turno.value.fecha || !turno.value.agendaSeleccionada) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Seleccione fecha y horario', life: 3000 });
        return;
    }

    try {
        await agendaService.reprogramar({
            turnoId: turno.value.idTurno,
            agendaId: turno.value.agendaSeleccionada.id
        });
        turnoDialog.value = false;
        turno.value = {};
        agenda.value = [];
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno reprogramado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo reprogramar el turno', life: 3000 });
    }
};

const cargarAgenda = async () => {
    turno.value.agendaSeleccionada = null;
    agenda.value = [];

    if (!turno.value.medico || !turno.value.fecha) {
        return;
    }

    loadingAgenda.value = true;

    try {
        agenda.value = await agendaService.findDisponibles({
            idMedico: turno.value.medico,
            fecha: formatFechaApi(new Date(turno.value.fecha))
        });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar la agenda', life: 3000 });
    } finally {
        loadingAgenda.value = false;
    }
};

const seleccionarHorario = (turnoAgenda) => {
    turno.value.agendaSeleccionada = turnoAgenda;
};

const cambiarEstado = async (tur, estado) => {
    if (estado === 'REALIZADO' && isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'El turno ya ha sido aprobado', life: 3000 });
        return;
    }
    if (estado === 'CANCELADO' && isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede cancelar un turno realizado', life: 3000 });
        return;
    }
    if (estado === 'REALIZADO' && isTurnoCancelado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede aprobar un turno cancelado', life: 3000 });
        return;
    }
    if (isTurnoPerdido(tur)) {
        const detail = estado === 'REALIZADO' ? 'No se puede aprobar un turno perdido' : 'No se puede modificar un turno perdido';
        toast.add({ severity: 'error', summary: 'Error', detail, life: 3000 });
        return;
    }

    try {
        await turnoService.cambiarEstado(tur.idTurno, estado);
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: `Turno ${estado.toLowerCase()}`, life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo cambiar el estado', life: 3000 });
    }
};

const cancelarTurno = async (tur) => {
    await cambiarEstado(tur, 'CANCELADO');
};

function exportCSV() {
    dt.value.exportCSV();
}

function formatFechaHora(fechaHora) {
    if (!fechaHora) return '';

    return new Date(fechaHora).toLocaleString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatFechaApi(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function formatHora(hora) {
    if (!hora) return '';
    return hora.slice(0, 5);
}
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <h4 class="m-0">Gestión de turnos solicitados</h4>
                </template>

                <template #end>
                    <Button label="Exportar" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedTurnos"
                :value="turnos"
                dataKey="idTurno"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} turnos"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <span class="text-sm text-gray-500">Pacientes asociados a la agenda del medico</span>
                        <IconField class="w-full sm:w-80">
                            <InputIcon class="pi pi-search" />
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <Column field="fechaDeTurno" header="Fecha y Hora" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ formatFechaHora(slotProps.data.fechaDeTurno) }}
                    </template>
                </Column>
                <Column field="estado" header="Estado" sortable style="min-width: 9rem">
                    <template #body="slotProps">
                        <Tag :value="resolveTurnoEstado(slotProps.data)" :severity="getTurnoEstadoSeverity(resolveTurnoEstado(slotProps.data))" :class="getTurnoEstadoTagClass(resolveTurnoEstado(slotProps.data))" />
                    </template>
                </Column>
                <Column field="paciente.apellido" header="Apellido" sortable style="min-width: 12rem" />
                <Column field="paciente.nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="paciente.email" header="Email" sortable style="min-width: 12rem" />
                <Column field="paciente.telefono" header="Teléfono" sortable style="min-width: 12rem" />

                <Column header="Acciones" :exportable="false" style="min-width: 18rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-check" severity="success" outlined rounded class="mr-2" title="Aprobar turno" aria-label="Aprobar" :disabled="isTurnoCancelado(slotProps.data) || isTurnoPerdido(slotProps.data)" @click="cambiarEstado(slotProps.data, 'REALIZADO')" />
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" title="Reprogramar turno" aria-label="Reprogramar" :disabled="!isTurnoReprogramable(slotProps.data)" @click="editTurno(slotProps.data)" />
                        <Button icon="pi pi-ban" severity="danger" outlined rounded title="Cancelar turno" aria-label="Cancelar" :disabled="isTurnoRealizado(slotProps.data) || isTurnoPerdido(slotProps.data)" @click="cancelarTurno(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="turnoDialog" :style="{ width: '560px' }" header="Reprogramar turno" :modal="true">
            <div class="flex flex-col gap-5">
                <div>
                    <label class="block font-bold mb-2">Paciente</label>
                    <InputText :modelValue="`${turno.paciente?.nombre || ''} ${turno.paciente?.apellido || ''}`" disabled fluid />
                </div>

                <div>
                    <label class="block font-bold mb-2">Médico</label>
                    <InputText :modelValue="turno.medicoActual" disabled fluid />
                </div>

                <div>
                    <label class="block font-bold mb-2">Nueva fecha</label>
                    <DatePicker v-model="turno.fecha" dateFormat="dd/mm/yy" :showIcon="true" :showButtonBar="true" :minDate="minFecha" :invalid="submitted && !turno.fecha" fluid @date-select="cargarAgenda" @update:modelValue="cargarAgenda" />
                </div>

                <div v-if="loadingAgenda" class="flex items-center gap-3">
                    <ProgressSpinner style="width: 28px; height: 28px" strokeWidth="4" />
                    <span>Cargando horarios...</span>
                </div>

                <div v-if="agenda.length > 0">
                    <label class="block font-bold mb-2">Horarios disponibles</label>
                    <div class="flex flex-wrap gap-2">
                        <Button
                            v-for="turnoAgenda in agenda"
                            :key="turnoAgenda.id"
                            :label="formatHora(turnoAgenda.hora)"
                            :severity="turno.agendaSeleccionada?.id === turnoAgenda.id ? 'info' : 'success'"
                            :outlined="turno.agendaSeleccionada?.id !== turnoAgenda.id"
                            @click="seleccionarHorario(turnoAgenda)"
                        />
                    </div>
                </div>

                <div v-else-if="turno.fecha && !loadingAgenda" class="text-gray-500">No hay horarios disponibles para esa fecha.</div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="saveTurno" />
            </template>
        </Dialog>
    </div>
</template>
