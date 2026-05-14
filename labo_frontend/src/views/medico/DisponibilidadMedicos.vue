<script setup>

import DisponibilidadService from '@/service/DisponibilidadService';
import MedicoService from '@/service/MedicoService';

import { onMounted, ref } from 'vue';

import { useToast } from 'primevue/usetoast';

const toast = useToast();

const disponibilidadService = new DisponibilidadService();
const medicoService = new MedicoService();

const medicos = ref([]);

const disponibilidad = ref({

    medico: null,
    dia: null,
    horaInicio: null,
    horaFin: null,
    duracionTurno: 30
});

const disponibilidades = ref([]);

const dias = ref([
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY'
]);

onMounted(async () => {

    await cargarMedicos();
});

const cargarMedicos = async () => {

    try {

        medicos.value = await medicoService.findAll();

        medicos.value = medicos.value.map((m) => ({
            ...m,
            nombreCompleto: `${m.nombre} ${m.apellido}`
        }));

    } catch (error) {

        console.error(error);
    }
};

const guardarDisponibilidad = async () => {

    try {

        const body = {

            medico: {
                idMedico: disponibilidad.value.medico
            },

            dia: disponibilidad.value.dia,

            horaInicio: disponibilidad.value.horaInicio,

            horaFin: disponibilidad.value.horaFin,

            duracionTurno: disponibilidad.value.duracionTurno
        };

        await disponibilidadService.guardarDisponibilidad(body);

        toast.add({

            severity: 'success',
            summary: 'Éxito',
            detail: 'Disponibilidad guardada',
            life: 3000
        });

        limpiarFormulario();

    } catch (error) {

        console.error(error);

        toast.add({

            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo guardar',
            life: 3000
        });
    }
};

const limpiarFormulario = () => {

    disponibilidad.value = {

        medico: null,
        dia: null,
        horaInicio: null,
        horaFin: null,
        duracionTurno: 30
    };
};

</script>

<template>

    <div class="card">

        <h2 class="mb-4">
            Configurar Disponibilidad Médica
        </h2>

        <div class="flex flex-col gap-4">

            <!-- MEDICO -->

            <div>

                <label class="block mb-2 font-bold">
                    Médico
                </label>

                <Select
                    v-model="disponibilidad.medico"
                    :options="medicos"
                    optionLabel="nombreCompleto"
                    optionValue="idMedico"
                    placeholder="Seleccionar médico"
                    fluid
                />

            </div>

            <!-- DIA -->

            <div>

                <label class="block mb-2 font-bold">
                    Día
                </label>

                <Select
                    v-model="disponibilidad.dia"
                    :options="dias"
                    placeholder="Seleccionar día"
                    fluid
                />

            </div>

            <!-- HORA INICIO -->

            <div>

                <label class="block mb-2 font-bold">
                    Hora inicio
                </label>

                <InputText
                    v-model="disponibilidad.horaInicio"
                    placeholder="08:00"
                />

            </div>

            <!-- HORA FIN -->

            <div>

                <label class="block mb-2 font-bold">
                    Hora fin
                </label>

                <InputText
                    v-model="disponibilidad.horaFin"
                    placeholder="12:00"
                />

            </div>

            <!-- DURACION -->

            <div>

                <label class="block mb-2 font-bold">
                    Duración turno (minutos)
                </label>

                <InputNumber
                    v-model="disponibilidad.duracionTurno"
                />

            </div>

            <!-- BOTON -->

            <Button
                label="Guardar Disponibilidad"
                icon="pi pi-save"
                @click="guardarDisponibilidad"
            />

        </div>

    </div>

</template>