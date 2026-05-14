<script setup>

import { ref } from 'vue';
import { useToast } from 'primevue/usetoast';

const toast = useToast();

const agenda = ref([]);

const fecha = ref(null);

const medicoId = 1;

const cargarAgenda = async () => {

    if (!fecha.value) {

        return;
    }

    try {

        const fechaFormateada = new Date(fecha.value)
            .toISOString()
            .split('T')[0];

        const response = await fetch(

            `http://localhost:8080/agenda?medicoId=${medicoId}&fecha=${fechaFormateada}`
        );

        agenda.value = await response.json();

    } catch (error) {

        console.error(error);

        toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo cargar la agenda',
            life: 3000
        });
    }
};

const getSeverity = (disponible) => {

    return disponible ? 'success' : 'danger';
};

const getEstado = (disponible) => {

    return disponible ? 'Disponible' : 'Reservado';
};

</script>

<template>

    <div class="card">

        <div class="flex justify-content-between align-items-center mb-5">

            <h2>
                Agenda Médica
            </h2>

            <div class="flex gap-3 align-items-center">

                <DatePicker
                    v-model="fecha"
                    :showIcon="true"
                    placeholder="Seleccionar fecha"
                    @date-select="cargarAgenda"
                />

                <Button
                    label="Buscar"
                    icon="pi pi-search"
                    @click="cargarAgenda"
                />

            </div>

        </div>

        <DataTable
            :value="agenda"
            responsiveLayout="scroll"
            stripedRows
        >

            <Column
                field="hora"
                header="Hora"
            />

            <Column header="Estado">

                <template #body="slotProps">

                    <Tag
                        :value="getEstado(slotProps.data.disponible)"
                        :severity="getSeverity(slotProps.data.disponible)"
                    />

                </template>

            </Column>

            <Column header="Paciente">

                <template #body="slotProps">

                    <span v-if="slotProps.data.paciente">

                        {{ slotProps.data.paciente.nombre }}
                        {{ slotProps.data.paciente.apellido }}

                    </span>

                    <span v-else>

                        -

                    </span>

                </template>

            </Column>

        </DataTable>

    </div>

</template>