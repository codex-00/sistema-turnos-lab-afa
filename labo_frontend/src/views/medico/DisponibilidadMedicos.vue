<script setup>
import DisponibilidadService from '@/service/DisponibilidadService';
import MedicoService from '@/service/MedicoService';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const toast = useToast();
const authStore = useAuthStore();
const disponibilidadService = new DisponibilidadService();
const medicoService = new MedicoService();

const medicoActualId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : null));
const medicos = ref([]);
const disponibilidades = ref([]);
const disponibilidad = ref(nuevaDisponibilidad());
const submitted = ref(false);
const medicoSeleccionado = computed(() => medicos.value.find((medico) => medico.idMedico === disponibilidad.value.medico));
const especialidadesMedico = computed(() => medicoSeleccionado.value?.especialidades?.length ? medicoSeleccionado.value.especialidades : (medicoSeleccionado.value?.especialidad || '').split(',').map((item) => item.trim()).filter(Boolean));

const dias = ref([
    { label: 'Lunes', value: 'MONDAY' },
    { label: 'Martes', value: 'TUESDAY' },
    { label: 'Miércoles', value: 'WEDNESDAY' },
    { label: 'Jueves', value: 'THURSDAY' },
    { label: 'Viernes', value: 'FRIDAY' },
    { label: 'Sábado', value: 'SATURDAY' },
    { label: 'Domingo', value: 'SUNDAY' }
]);

const horarios = ref(
    Array.from({ length: 35 }, (_, index) => {
        const totalMinutos = 6 * 60 + index * 30;
        const hora = String(Math.floor(totalMinutos / 60)).padStart(2, '0');
        const minutos = String(totalMinutos % 60).padStart(2, '0');
        const value = `${hora}:${minutos}`;
        return { label: value, value };
    })
);

function nuevaDisponibilidad() {
    return {
        id: null,
        medico: null,
        especialidad: null,
        dia: null,
        horaInicio: '08:00',
        horaFin: '12:00',
        duracionTurno: 30
    };
}

onMounted(async () => {
    await cargarMedicos();

    if (medicoActualId.value) {
        disponibilidad.value.medico = medicoActualId.value;
        await cargarDisponibilidades();
    }
});

const cargarMedicos = async () => {
    try {
        const data = await medicoService.findAll();
        medicos.value = data.map((m) => ({
            ...m,
            nombreCompleto: `${m.nombre} ${m.apellido}`,
            especialidades: m.especialidades?.length ? m.especialidades : (m.especialidad || '').split(',').map((item) => item.trim()).filter(Boolean)
        }));
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los médicos', life: 3000 });
    }
};

const cargarDisponibilidades = async () => {
    if (!disponibilidad.value.medico) {
        disponibilidades.value = [];
        return;
    }

    try {
        disponibilidades.value = await disponibilidadService.findAllByMedico({ idMedico: disponibilidad.value.medico });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar la disponibilidad', life: 3000 });
    }
};

const minutosDesdeHora = (hora) => {
    if (!hora) return null;
    const [horas, minutos] = hora.split(':').map(Number);
    return horas * 60 + minutos;
};

const rangosSeSolapan = (primera, segunda) => minutosDesdeHora(primera.horaInicio) < minutosDesdeHora(segunda.horaFin) && minutosDesdeHora(primera.horaFin) > minutosDesdeHora(segunda.horaInicio);

const validarDisponibilidadFormulario = () => {
    if (!disponibilidad.value.medico || !disponibilidad.value.especialidad || !disponibilidad.value.dia || !disponibilidad.value.horaInicio || !disponibilidad.value.horaFin || !disponibilidad.value.duracionTurno) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Complete médico, especialidad, día, horarios y duración', life: 3000 });
        return false;
    }

    const inicio = minutosDesdeHora(disponibilidad.value.horaInicio);
    const fin = minutosDesdeHora(disponibilidad.value.horaFin);
    if (inicio >= fin) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'La hora de inicio debe ser anterior a la hora de fin', life: 3000 });
        return false;
    }

    if (disponibilidad.value.duracionTurno <= 0 || disponibilidad.value.duracionTurno > fin - inicio) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'La duración debe ser mayor a cero y no superar el rango horario', life: 3000 });
        return false;
    }

    const solapada = disponibilidades.value.some((existente) => existente.id !== disponibilidad.value.id && existente.dia === disponibilidad.value.dia && rangosSeSolapan(disponibilidad.value, existente));
    if (solapada) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Ya existe disponibilidad para ese día y rango horario', life: 3000 });
        return false;
    }

    return true;
};

const guardarDisponibilidad = async () => {
    submitted.value = true;

    if (!validarDisponibilidadFormulario()) {
        return;
    }

    try {
        const body = {
            medico: { idMedico: disponibilidad.value.medico },
            especialidad: disponibilidad.value.especialidad,
            dia: disponibilidad.value.dia,
            horaInicio: disponibilidad.value.horaInicio,
            horaFin: disponibilidad.value.horaFin,
            duracionTurno: disponibilidad.value.duracionTurno
        };

        if (disponibilidad.value.id) {
            await disponibilidadService.update(disponibilidad.value.id, body);
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Disponibilidad actualizada', life: 3000 });
        } else {
            await disponibilidadService.guardarDisponibilidad(body);
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Disponibilidad guardada', life: 3000 });
        }

        const medico = disponibilidad.value.medico;
        limpiarFormulario();
        disponibilidad.value.medico = medico;
        await cargarDisponibilidades();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo guardar', life: 3000 });
    }
};

function editarDisponibilidad(disp) {
    disponibilidad.value = {
        ...disp,
        medico: disponibilidad.value.medico
    };
    submitted.value = false;
}

const eliminarDisponibilidad = async (disp) => {
    try {
        await disponibilidadService.remove(disp.id);
        await cargarDisponibilidades();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Disponibilidad eliminada', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo eliminar', life: 3000 });
    }
};

const limpiarFormulario = () => {
    const medico = disponibilidad.value.medico;
    disponibilidad.value = nuevaDisponibilidad();
    disponibilidad.value.medico = medicoActualId.value || medico;
    submitted.value = false;
};

const formatDia = (dia) => dias.value.find((item) => item.value === dia)?.label || dia;
</script>

<template>
    <div class="card">
        <div class="flex flex-wrap gap-3 items-center justify-between mb-5">
            <h2 class="m-0">Disponibilidad médica</h2>
            <Button label="Limpiar" icon="pi pi-refresh" severity="secondary" outlined @click="limpiarFormulario" />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-6 gap-4 items-end mb-6">
            <div>
                <label class="block mb-2 font-bold">Médico</label>
                <Select
                    v-model="disponibilidad.medico"
                    :options="medicos"
                    optionLabel="nombreCompleto"
                    optionValue="idMedico"
                    placeholder="Seleccionar médico"
                    :disabled="!!medicoActualId"
                    :invalid="submitted && !disponibilidad.medico"
                    fluid
                    @change="cargarDisponibilidades"
                />
            </div>

            <div>
                <label class="block mb-2 font-bold">Día</label>
                <Select v-model="disponibilidad.dia" :options="dias" optionLabel="label" optionValue="value" placeholder="Día" :invalid="submitted && !disponibilidad.dia" fluid />
            </div>

            <div>
                <label class="block mb-2 font-bold">Especialidad</label>
                <Select v-model="disponibilidad.especialidad" :options="especialidadesMedico" placeholder="Especialidad" :invalid="submitted && !disponibilidad.especialidad" fluid />
            </div>

            <div>
                <label class="block mb-2 font-bold">Hora de inicio</label>
                <Select v-model="disponibilidad.horaInicio" :options="horarios" optionLabel="label" optionValue="value" placeholder="Inicio" :invalid="submitted && !disponibilidad.horaInicio" fluid />
            </div>

            <div>
                <label class="block mb-2 font-bold">Hora de fin</label>
                <Select v-model="disponibilidad.horaFin" :options="horarios" optionLabel="label" optionValue="value" placeholder="Fin" :invalid="submitted && !disponibilidad.horaFin" fluid />
            </div>

            <div>
                <label class="block mb-2 font-bold">Duración del turno</label>
                <InputNumber v-model="disponibilidad.duracionTurno" suffix=" min" :min="5" :step="5" :invalid="submitted && !disponibilidad.duracionTurno" fluid />
            </div>
        </div>

        <Button :label="disponibilidad.id ? 'Actualizar' : 'Guardar'" icon="pi pi-save" @click="guardarDisponibilidad" />
    </div>

    <DataTable :value="disponibilidades" dataKey="id" :paginator="true" :rows="10">
        <Column field="dia" header="Día" sortable>
            <template #body="slotProps">
                {{ formatDia(slotProps.data.dia) }}
            </template>
        </Column>
        <Column field="especialidad" header="Especialidad" sortable />
        <Column field="horaInicio" header="Inicio" sortable />
        <Column field="horaFin" header="Fin" sortable />
        <Column field="duracionTurno" header="Duración" sortable>
            <template #body="slotProps"> {{ slotProps.data.duracionTurno }} min </template>
        </Column>
        <Column :exportable="false" style="width: 10rem">
            <template #body="slotProps">
                <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editarDisponibilidad(slotProps.data)" />
                <Button icon="pi pi-trash" outlined rounded severity="danger" @click="eliminarDisponibilidad(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>
