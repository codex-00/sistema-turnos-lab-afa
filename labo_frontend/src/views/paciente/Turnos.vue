<script setup>
import AgendaService from '@/service/AgendaService';
import MedicoService from '@/service/MedicoService';
import TurnoService from '@/service/TurnoService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const agendaService = new AgendaService();
const medicoService = new MedicoService();
const turnoService = new TurnoService();
const toast = useToast();

const pacienteId = 1;

const dt = ref();
const turnos = ref([]);
const medicos = ref([]);
const agenda = ref([]);
const turnoDialog = ref(false);
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
    { label: 'Pediatria', value: 'Pediatria' },
    { label: 'Traumatologia', value: 'Traumatologia' },
    { label: 'Clinico', value: 'Clinico' }
]);

onMounted(async () => {
    await cargarTurnos();
});

const cargarTurnos = async () => {
    try {
        turnos.value = await turnoService.findAllByPaciente({ idPaciente: pacienteId });
    } catch (error) {
        console.error(error);
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
        console.error(error);
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los medicos', life: 3000 });
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
            fecha: formatFechaApi(new Date(turno.value.fecha))
        });
    } catch (error) {
        console.error(error);
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
        toast.add({ severity: 'error', summary: 'Error', detail: 'Seleccione especialidad, medico, fecha y horario', life: 3000 });
        return;
    }

    try {
        await agendaService.reservar({
            agendaId: turno.value.agendaSeleccionada.id,
            pacienteId
        });

        toast.add({ severity: 'success', summary: 'Exito', detail: 'Turno reservado', life: 3000 });
        turnoDialog.value = false;
        turno.value = {};
        agenda.value = [];
        await cargarTurnos();
    } catch (error) {
        console.error(error);
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al reservar turno', life: 3000 });
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
            return null;
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
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <Column field="fechaDeTurno" header="Fecha y Hora" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ formatFechaHora(slotProps.data.fechaDeTurno) }}
                    </template>
                </Column>
                <Column field="medico.apellido" header="Apellido" sortable style="min-width: 12rem" />
                <Column field="medico.nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="medico.email" header="Email" sortable style="min-width: 12rem" />
                <Column field="medico.telefono" header="Telefono" sortable style="min-width: 12rem" />
                <Column field="medico.especialidad" header="Especialidad" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <Tag :value="slotProps.data.medico.especialidad" :severity="getStatusLabel(slotProps.data.medico.especialidad)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="turnoDialog" :style="{ width: '560px' }" header="Solicitar turno" :modal="true">
            <div class="flex flex-col gap-5">
                <div>
                    <label class="block font-bold mb-2">Especialidad</label>
                    <Select
                        v-model="turno.especialidad"
                        :options="especialidades"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Seleccionar especialidad"
                        :invalid="submitted && !turno.especialidad"
                        fluid
                        @change="onEspecialidadChange"
                    />
                </div>

                <div v-if="loadingMedicos" class="flex items-center gap-3">
                    <ProgressSpinner style="width: 28px; height: 28px" strokeWidth="4" />
                    <span>Cargando medicos...</span>
                </div>

                <div v-if="turno.especialidad && medicos.length > 0">
                    <label class="block font-bold mb-2">Medico</label>
                    <Select
                        v-model="turno.medico"
                        :options="medicos"
                        optionLabel="nombreCompleto"
                        optionValue="idMedico"
                        placeholder="Seleccionar medico"
                        :invalid="submitted && !turno.medico"
                        fluid
                        @change="onMedicoChange"
                    />
                </div>

                <div v-if="turno.especialidad && !loadingMedicos && medicos.length === 0" class="text-gray-500">
                    No hay medicos cargados para esta especialidad.
                </div>

                <div v-if="turno.medico">
                    <label class="block font-bold mb-2">Fecha</label>
                    <DatePicker
                        v-model="turno.fecha"
                        :showIcon="true"
                        :showButtonBar="true"
                        :invalid="submitted && !turno.fecha"
                        fluid
                        @date-select="cargarAgenda"
                        @update:modelValue="cargarAgenda"
                    />
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

                <div v-else-if="turno.medico && turno.fecha && !loadingAgenda" class="text-gray-500">
                    No hay horarios disponibles para esa fecha.
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Reservar" icon="pi pi-check" @click="saveTurno" />
            </template>
        </Dialog>
    </div>
</template>
