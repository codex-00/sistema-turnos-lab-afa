<script setup>

import { onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';

const toast = useToast();

const disponibilidades = ref([]);

const disponibilidad = ref({

    dia: null,

    horaInicio: null,

    horaFin: null,

    duracionTurno: 30
});

const dias = ref([
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY'
]);

const medicoId = 1;

onMounted(async () => {

    await cargarDisponibilidades();

});

const cargarDisponibilidades = async () => {

    try {

        const response = await fetch(

            `http://localhost:8080/disponibilidad?medicoId=${medicoId}`
        );

        disponibilidades.value = await response.json();

    } catch (error) {

        console.error(error);
    }
};

const guardarDisponibilidad = async () => {

    try {

        await fetch(

            `http://localhost:8080/disponibilidad?medicoId=${medicoId}`,

            {
                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify(disponibilidad.value)
            }
        );

        toast.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Disponibilidad guardada',
            life: 3000
        });

        disponibilidad.value = {

            dia: null,

            horaInicio: null,

            horaFin: null,

            duracionTurno: 30
        };

        await cargarDisponibilidades();

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

</script>

<template>

    <div class="card">

        <h2 class="mb-5">
            Configurar Disponibilidad
        </h2>

        <div class="grid">

            <!-- FORMULARIO -->

            <div class="col-12 md:col-5">

                <div class="flex flex-column gap-4">

                    <!-- DIA -->

                    <div>

                        <label class="font-bold block mb-2">
                            Día
                        </label>

                        <Select
                            v-model="disponibilidad.dia"
                            :options="dias"
                            placeholder="Seleccionar día"
                            class="w-full"
                        />

                    </div>

                    <!-- HORA INICIO -->

                    <div>

                        <label class="font-bold block mb-2">
                            Hora inicio
                        </label>

                        <InputText
                            v-model="disponibilidad.horaInicio"
                            placeholder="08:00"
                            class="w-full"
                        />

                    </div>

                    <!-- HORA FIN -->

                    <div>

                        <label class="font-bold block mb-2">
                            Hora fin
                        </label>

                        <InputText
                            v-model="disponibilidad.horaFin"
                            placeholder="12:00"
                            class="w-full"
                        />

                    </div>

                    <!-- DURACION -->

                    <div>

                        <label class="font-bold block mb-2">
                            Duración del turno (minutos)
                        </label>

                        <InputNumber
                            v-model="disponibilidad.duracionTurno"
                            class="w-full"
                        />

                    </div>

                    <Button
                        label="Guardar disponibilidad"
                        icon="pi pi-check"
                        @click="guardarDisponibilidad"
                    />

                </div>

            </div>

            <!-- TABLA -->

            <div class="col-12 md:col-7">

                <DataTable
                    :value="disponibilidades"
                    responsiveLayout="scroll"
                >

                    <Column
                        field="dia"
                        header="Día"
                    />

                    <Column
                        field="horaInicio"
                        header="Inicio"
                    />

                    <Column
                        field="horaFin"
                        header="Fin"
                    />

                    <Column
                        field="duracionTurno"
                        header="Duración"
                    />

                </DataTable>

            </div>

        </div>

    </div>

</template>