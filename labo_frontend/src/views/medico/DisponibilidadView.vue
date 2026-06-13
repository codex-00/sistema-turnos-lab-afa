<script setup>
import DisponibilidadService from '@/service/DisponibilidadService';
import { useAuthStore } from '@/stores/auth';
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';

const disponibilidadService = new DisponibilidadService();
const authStore = useAuthStore();
const toast = useToast();

const medicoId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : 1));
const disponibilidades = ref([]);
const disponibilidad = ref({
    dia: null,
    horaInicio: null,
    horaFin: null,
    duracionTurno: 30
});

const dias = ref([
    { label: 'Lunes', value: 'MONDAY' },
    { label: 'Martes', value: 'TUESDAY' },
    { label: 'Miércoles', value: 'WEDNESDAY' },
    { label: 'Jueves', value: 'THURSDAY' },
    { label: 'Viernes', value: 'FRIDAY' },
    { label: 'Sábado', value: 'SATURDAY' }
]);

const formatDia = (dia) => {
    const diasMap = {
        MONDAY: 'Lunes',
        TUESDAY: 'Martes',
        WEDNESDAY: 'Miércoles',
        THURSDAY: 'Jueves',
        FRIDAY: 'Viernes',
        SATURDAY: 'Sábado',
        SUNDAY: 'Domingo'
    };
    return diasMap[dia] || dia;
};

const cargarDisponibilidades = async () => {
    try {
        disponibilidades.value = await disponibilidadService.findAllByMedico({ idMedico: medicoId.value });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo cargar la disponibilidad', life: 3000 });
    }
};

const guardarDisponibilidad = async () => {
    if (!disponibilidad.value.dia || !disponibilidad.value.horaInicio || !disponibilidad.value.horaFin || !disponibilidad.value.duracionTurno) {
        toast.add({ severity: 'warn', summary: 'Datos incompletos', detail: 'Complete día, horarios y duración', life: 3000 });
        return;
    }

    try {
        await disponibilidadService.guardarDisponibilidad({
            medico: { idMedico: medicoId.value },
            ...disponibilidad.value
        });

        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Disponibilidad guardada', life: 3000 });
        disponibilidad.value = { dia: null, horaInicio: null, horaFin: null, duracionTurno: 30 };
        await cargarDisponibilidades();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo guardar', life: 3000 });
    }
};

onMounted(cargarDisponibilidades);
</script>

<template>
    <div class="card">
        <h2 class="mb-5">Configurar disponibilidad</h2>

        <div class="grid">
            <div class="col-12 md:col-5">
                <div class="flex flex-column gap-4">
                    <div>
                        <label class="font-bold block mb-2">Día</label>
                        <Select v-model="disponibilidad.dia" :options="dias" optionLabel="label" optionValue="value" placeholder="Seleccionar día" class="w-full" />
                    </div>

                    <div>
                        <label class="font-bold block mb-2">Hora inicio</label>
                        <InputText v-model="disponibilidad.horaInicio" placeholder="08:00" class="w-full" />
                    </div>

                    <div>
                        <label class="font-bold block mb-2">Hora fin</label>
                        <InputText v-model="disponibilidad.horaFin" placeholder="12:00" class="w-full" />
                    </div>

                    <div>
                        <label class="font-bold block mb-2">Duración del turno (minutos)</label>
                        <InputNumber v-model="disponibilidad.duracionTurno" class="w-full" />
                    </div>

                    <Button label="Guardar disponibilidad" icon="pi pi-check" @click="guardarDisponibilidad" />
                </div>
            </div>

            <div class="col-12 md:col-7">
                <DataTable :value="disponibilidades" responsiveLayout="scroll">
                    <Column field="dia" header="Día">
                        <template #body="slotProps">
                            {{ formatDia(slotProps.data.dia) }}
                        </template>
                    </Column>
                    <Column field="horaInicio" header="Inicio" />
                    <Column field="horaFin" header="Fin" />
                    <Column field="duracionTurno" header="Duración" />
                </DataTable>
            </div>
        </div>
    </div>
</template>
