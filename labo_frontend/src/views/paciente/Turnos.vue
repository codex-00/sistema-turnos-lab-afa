<script setup>
import AgendaService from '@/service/AgendaService';
import MedicoService from '@/service/MedicoService';
import TurnoService from '@/service/TurnoService';
import { useAuthStore } from '@/stores/auth';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, isTurnoCancelado, isTurnoPerdido, isTurnoRealizado, isTurnoReprogramable, resolveTurnoEstado } from '@/utils/turnoEstado';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const agendaService = new AgendaService();
const medicoService = new MedicoService();
const turnoService = new TurnoService();
const toast = useToast();
const authStore = useAuthStore();

const pacienteId = computed(() => (authStore.rol === 'paciente' && authStore.token ? authStore.token : 1));
const minFecha = ref(new Date());

const dt = ref();
const turnos = ref([]);
const medicos = ref([]);
const agenda = ref([]);
const turnoDialog = ref(false);
const reprogramarDialog = ref(false);
const turno = ref({});
const selectedTurnos = ref();
const submitted = ref(false);
const loadingMedicos = ref(false);
const loadingAgenda = ref(false);

const filters = ref({
    global: {
        value: null,
        matchMode: FilterMatchMode.CONTAINS
    }
});

const especialidades = ref([
    { label: 'Pediatría', value: 'Pediatria' },
    { label: 'Traumatología', value: 'Traumatologia' },
    { label: 'Clínico', value: 'Clinico' }
]);

onMounted(async () => {
    await cargarTurnos();
});

const cargarTurnos = async () => {
    try {
        turnos.value = await turnoService.findAllByPaciente({ idPaciente: pacienteId.value });
    } catch (error) {
        turnos.value = [];
    }
};

function openNew() {
    turno.value = {};
    medicos.value = [];
    agenda.value = [];
    submitted.value = false;
    turnoDialog.value = true;
}

function hideDialog() {
    turnoDialog.value = false;
    reprogramarDialog.value = false;
    submitted.value = false;
}

const onEspecialidadChange = async (event) => {
    const especialidadSeleccionada = event.value;

    turno.value.medico = null;
    turno.value.fecha = null;
    turno.value.agendaSeleccionada = null;
    medicos.value = [];
    agenda.value = [];

    if (!especialidadSeleccionada) {
        return;
    }

    loadingMedicos.value = true;

    try {
        const data = await medicoService.findAllByEspecialidad({ especialidad: especialidadSeleccionada });
        medicos.value = data.map((medico) => ({
            ...medico,
            nombreCompleto: `${medico.nombre} ${medico.apellido}`
        }));
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los médicos', life: 3000 });
    } finally {
        loadingMedicos.value = false;
    }
};

const onMedicoChange = async () => {
    turno.value.fecha = null;
    turno.value.agendaSeleccionada = null;
    agenda.value = [];
};

const formatFechaApi = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
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
            fecha: formatFechaApi(new Date(turno.value.fecha)),
            especialidad: turno.value.especialidad
        });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar la agenda', life: 3000 });
    } finally {
        loadingAgenda.value = false;
    }
};

const seleccionarTurno = (turnoAgenda) => {
    turno.value.agendaSeleccionada = turnoAgenda;
};

const saveTurno = async () => {
    submitted.value = true;

    if (!turno.value.especialidad || !turno.value.medico || !turno.value.fecha || !turno.value.agendaSeleccionada) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Seleccione especialidad, médico, fecha y horario', life: 3000 });
        return;
    }

    try {
        await agendaService.reservar({
            agendaId: turno.value.agendaSeleccionada.id,
            pacienteId: pacienteId.value
        });

        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno reservado', life: 3000 });
        turnoDialog.value = false;
        turno.value = {};
        agenda.value = [];
        await cargarTurnos();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al reservar turno', life: 3000 });
    }
};

function openReprogramar(tur) {
    if (isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede reprogramar un turno realizado', life: 3000 });
        return;
    }
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
        especialidad: tur.medico?.especialidades?.[0] || (tur.medico?.especialidad || '').split(',')[0]?.trim(),
        medico: tur.medico?.idMedico,
        fecha: null,
        agendaSeleccionada: null
    };
    agenda.value = [];
    submitted.value = false;
    reprogramarDialog.value = true;
}

const reprogramarTurno = async () => {
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

        reprogramarDialog.value = false;
        turno.value = {};
        agenda.value = [];
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno reprogramado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al reprogramar turno', life: 3000 });
    }
};

const cancelarTurno = async (tur) => {
    if (isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede cancelar un turno realizado', life: 3000 });
        return;
    }
    if (isTurnoPerdido(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede cancelar un turno perdido', life: 3000 });
        return;
    }

    try {
        await turnoService.cambiarEstado(tur.idTurno, 'CANCELADO');
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno cancelado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al cancelar turno', life: 3000 });
    }
};

function exportCSV() {
    dt.value.exportCSV();
}

function getStatusLabel(status) {
    switch (status) {
        case 'Pediatria':
            return 'success';
        case 'Traumatologia':
            return 'warn';
        case 'Clinico':
            return 'danger';
        default:
            return 'info';
    }
}

function formatFechaHora(fechaHora) {
    if (!fechaHora) return '';

    const fecha = new Date(fechaHora);

    return fecha.toLocaleString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
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
                    <Button label="Nuevo Turno" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
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
                        <h4 class="m-0">Turnos</h4>
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
                <Column field="medico.apellido" header="Apellido" sortable style="min-width: 12rem" />
                <Column field="medico.nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="medico.email" header="Email" sortable style="min-width: 12rem" />
                <Column field="medico.telefono" header="Teléfono" sortable style="min-width: 12rem" />
                <Column field="medico.especialidad" header="Especialidad" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <div class="flex flex-wrap gap-1">
                            <Tag v-for="esp in slotProps.data.medico.especialidades || [slotProps.data.medico.especialidad]" :key="esp" :value="esp" :severity="getStatusLabel(esp)" />
                        </div>
                    </template>
                </Column>
                <Column :exportable="false" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-calendar" outlined rounded class="mr-2" :disabled="!isTurnoReprogramable(slotProps.data)" @click="openReprogramar(slotProps.data)" />
                        <Button icon="pi pi-ban" outlined rounded severity="danger" :disabled="isTurnoRealizado(slotProps.data) || isTurnoPerdido(slotProps.data)" @click="cancelarTurno(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="turnoDialog" :style="{ width: '560px' }" header="Solicitar turno" :modal="true">
            <div class="flex flex-col gap-5">
                <div>
                    <label class="block font-bold mb-2">Especialidad</label>
                    <Select v-model="turno.especialidad" :options="especialidades" optionLabel="label" optionValue="value" placeholder="Seleccionar especialidad" :invalid="submitted && !turno.especialidad" fluid @change="onEspecialidadChange" />
                </div>

                <div v-if="loadingMedicos" class="flex items-center gap-3">
                    <ProgressSpinner style="width: 28px; height: 28px" strokeWidth="4" />
                    <span>Cargando médicos...</span>
                </div>

                <div v-if="turno.especialidad && medicos.length > 0">
                    <label class="block font-bold mb-2">Médico</label>
                    <Select v-model="turno.medico" :options="medicos" optionLabel="nombreCompleto" optionValue="idMedico" placeholder="Seleccionar médico" :invalid="submitted && !turno.medico" fluid @change="onMedicoChange" />
                </div>

                <div v-if="turno.especialidad && !loadingMedicos && medicos.length === 0" class="text-gray-500">No hay médicos cargados para esta especialidad.</div>

                <div v-if="turno.medico">
                    <label class="block font-bold mb-2">Fecha</label>
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
                            @click="seleccionarTurno(turnoAgenda)"
                        />
                    </div>
                </div>

                <div v-else-if="turno.medico && turno.fecha && !loadingAgenda" class="text-gray-500">No hay horarios disponibles para esa fecha.</div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Reservar" icon="pi pi-check" @click="saveTurno" />
            </template>
        </Dialog>

        <Dialog v-model:visible="reprogramarDialog" :style="{ width: '560px' }" header="Reprogramar turno" :modal="true">
            <div class="flex flex-col gap-5">
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
                            @click="seleccionarTurno(turnoAgenda)"
                        />
                    </div>
                </div>

                <div v-else-if="turno.fecha && !loadingAgenda" class="text-gray-500">No hay horarios disponibles para esa fecha.</div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="reprogramarTurno" />
            </template>
        </Dialog>
    </div>
</template>
