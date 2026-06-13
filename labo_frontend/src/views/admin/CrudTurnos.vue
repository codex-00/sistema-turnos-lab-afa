<script setup>
import AgendaService from '@/service/AgendaService';
import MedicoService from '@/service/MedicoService';
import PacienteService from '@/service/PacienteService';
import TurnoService from '@/service/TurnoService';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, isTurnoPerdido, isTurnoRealizado, resolveTurnoEstado } from '@/utils/turnoEstado';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const agendaService = new AgendaService();
const medicoService = new MedicoService();
const pacienteService = new PacienteService();
const turnoService = new TurnoService();
const toast = useToast();

const turnos = ref([]);
const pacientes = ref([]);
const medicos = ref([]);
const agenda = ref([]);
const turno = ref({});
const turnoDialog = ref(false);
const reprogramarDialog = ref(false);
const deleteTurnoDialog = ref(false);
const submitted = ref(false);
const loadingPacientes = ref(false);
const loadingMedicos = ref(false);
const loadingAgenda = ref(false);
const minFecha = ref(new Date());

const especialidades = ref([
    { label: 'Pediatría', value: 'Pediatria' },
    { label: 'Traumatología', value: 'Traumatologia' },
    { label: 'Clínico', value: 'Clinico' }
]);

onMounted(async () => {
    await Promise.all([cargarTurnos(), cargarPacientes()]);
});

const cargarTurnos = async () => {
    try {
        turnos.value = await turnoService.findAll();
    } catch (error) {
        turnos.value = [];
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudieron cargar los turnos', life: 3000 });
    }
};

const cargarPacientes = async () => {
    loadingPacientes.value = true;

    try {
        const data = await pacienteService.findAll();
        pacientes.value = data.map((paciente) => ({
            ...paciente,
            nombreCompleto: `${paciente.apellido} ${paciente.nombre} - DNI ${paciente.dni}`
        }));
    } catch (error) {
        pacientes.value = [];
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudieron cargar los pacientes', life: 3000 });
    } finally {
        loadingPacientes.value = false;
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
            nombreCompleto: `${medico.apellido} ${medico.nombre}`
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

const seleccionarHorario = (turnoAgenda) => {
    turno.value.agendaSeleccionada = turnoAgenda;
};

const saveTurno = async () => {
    submitted.value = true;

    if (!turno.value.paciente || !turno.value.especialidad || !turno.value.medico || !turno.value.fecha || !turno.value.agendaSeleccionada) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Seleccione paciente, especialidad, médico, fecha y horario', life: 3000 });
        return;
    }

    try {
        await agendaService.reservar({
            agendaId: turno.value.agendaSeleccionada.id,
            pacienteId: turno.value.paciente
        });

        turnoDialog.value = false;
        turno.value = {};
        agenda.value = [];
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno creado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al crear el turno', life: 3000 });
    }
};

function openReprogramar(tur) {
    if (isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede reprogramar un turno realizado', life: 3000 });
        return;
    }
    if (isTurnoPerdido(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede reprogramar un turno perdido', life: 3000 });
        return;
    }

    turno.value = {
        ...tur,
        medicoActual: `${tur.medico?.apellido || ''} ${tur.medico?.nombre || ''}`.trim(),
        medico: tur.medico?.idMedico,
        especialidad: tur.medico?.especialidades?.[0] || (tur.medico?.especialidad || '').split(',')[0]?.trim(),
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

const aprobarTurno = async (tur) => {
    if (isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'El turno ya ha sido aprobado', life: 3000 });
        return;
    }
    if (isTurnoPerdido(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede aprobar un turno perdido', life: 3000 });
        return;
    }

    try {
        await turnoService.cambiarEstado(tur.idTurno, 'REALIZADO');
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno realizado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo cambiar el estado', life: 3000 });
    }
};

function confirmDeleteTurno(tur) {
    if (isTurnoRealizado(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede cancelar un turno realizado', life: 3000 });
        return;
    }
    if (isTurnoPerdido(tur)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se puede cancelar un turno perdido', life: 3000 });
        return;
    }

    turno.value = tur;
    deleteTurnoDialog.value = true;
}

const eliminarTurno = async () => {
    try {
        await turnoService.remove(turno.value.idTurno);
        deleteTurnoDialog.value = false;
        turno.value = {};
        await cargarTurnos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno eliminado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo eliminar el turno', life: 3000 });
    }
};

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

function formatHora(hora) {
    if (!hora) return '';
    return hora.slice(0, 5);
}

</script>

<template>
    <div>
        <div class="card">
            <div class="flex flex-wrap gap-3 items-center justify-between mb-6">
                <h4 class="m-0">Gestión de turnos</h4>
                <Button label="Nuevo turno" icon="pi pi-plus" severity="secondary" @click="openNew" />
            </div>

            <DataTable :value="turnos" dataKey="idTurno" :paginator="true" :rows="10">
                <template #header>
                    <span class="font-semibold">Turnos registrados</span>
                </template>

                <Column field="fechaDeTurno" header="Fecha y hora" sortable>
                    <template #body="slotProps">
                        {{ formatFechaHora(slotProps.data.fechaDeTurno) }}
                    </template>
                </Column>
                <Column field="paciente.apellido" header="Paciente" sortable>
                    <template #body="slotProps"> {{ slotProps.data.paciente?.apellido }} {{ slotProps.data.paciente?.nombre }} </template>
                </Column>
                <Column field="medico.apellido" header="Médico" sortable>
                    <template #body="slotProps"> {{ slotProps.data.medico?.apellido }} {{ slotProps.data.medico?.nombre }} </template>
                </Column>
                <Column field="medico.especialidad" header="Especialidad" sortable>
                    <template #body="slotProps">
                        {{ (slotProps.data.medico?.especialidades || [slotProps.data.medico?.especialidad]).filter(Boolean).join(', ') }}
                    </template>
                </Column>
                <Column field="estado" header="Estado" sortable style="min-width: 14rem">
                    <template #body="slotProps">
                        <Tag :value="resolveTurnoEstado(slotProps.data)" :severity="getTurnoEstadoSeverity(resolveTurnoEstado(slotProps.data))" :class="getTurnoEstadoTagClass(resolveTurnoEstado(slotProps.data))" />
                    </template>
                </Column>
                <Column :exportable="false" style="min-width: 9rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-check" outlined rounded severity="success" class="mr-2" title="Aprobar turno" aria-label="Aprobar" :disabled="isTurnoRealizado(slotProps.data) || isTurnoPerdido(slotProps.data)" @click="aprobarTurno(slotProps.data)" />
                        <Button icon="pi pi-calendar" outlined rounded class="mr-2" title="Reprogramar turno" aria-label="Reprogramar" :disabled="isTurnoRealizado(slotProps.data) || isTurnoPerdido(slotProps.data)" @click="openReprogramar(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" :disabled="isTurnoRealizado(slotProps.data) || isTurnoPerdido(slotProps.data)" @click="confirmDeleteTurno(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="turnoDialog" :style="{ width: '600px' }" header="Crear turno" :modal="true">
            <div class="flex flex-col gap-5">
                <div>
                    <label class="block font-bold mb-2">Paciente</label>
                    <Select v-model="turno.paciente" :options="pacientes" optionLabel="nombreCompleto" optionValue="idPaciente" placeholder="Seleccionar paciente" :loading="loadingPacientes" :invalid="submitted && !turno.paciente" filter fluid />
                </div>

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
                    <Select v-model="turno.medico" :options="medicos" optionLabel="nombreCompleto" optionValue="idMedico" placeholder="Seleccionar médico" :invalid="submitted && !turno.medico" filter fluid @change="onMedicoChange" />
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
                            @click="seleccionarHorario(turnoAgenda)"
                        />
                    </div>
                </div>

                <div v-else-if="turno.medico && turno.fecha && !loadingAgenda" class="text-gray-500">No hay horarios disponibles para esa fecha.</div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Crear" icon="pi pi-check" @click="saveTurno" />
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
                            @click="seleccionarHorario(turnoAgenda)"
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

        <Dialog v-model:visible="deleteTurnoDialog" :style="{ width: '450px' }" header="Confirmar" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>¿Desea eliminar el turno de <b>{{ turno.paciente?.nombre }} {{ turno.paciente?.apellido }}</b>?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteTurnoDialog = false" />
                <Button label="Sí" icon="pi pi-check" severity="danger" @click="eliminarTurno" />
            </template>
        </Dialog>
    </div>
</template>
