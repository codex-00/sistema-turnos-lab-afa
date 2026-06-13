<script setup>
import EstudioService from '@/service/EstudioService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const estudioService = new EstudioService();
const toast = useToast();
const estudios = ref([]);
const dt = ref();
const filters = ref({ global: { value: null, matchMode: FilterMatchMode.CONTAINS } });

onMounted(async () => {
    estudios.value = await estudioService.findAll();
});

const descargar = async (estudio) => {
    try {
        const response = await estudioService.download(estudio.idEstudio);
        const url = window.URL.createObjectURL(new Blob([response.data], { type: response.headers['content-type'] }));
        const link = document.createElement('a');
        link.href = url;
        link.download = estudio.nombreArchivo || estudio.nombre || 'estudio';
        link.click();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo descargar el estudio', life: 3000 });
    }
};

function exportCSV() {
    dt.value.exportCSV();
}
</script>

<template>
    <div class="card">
        <Toolbar class="mb-6">
            <template #start>
                <h4 class="m-0">Todos los estudios</h4>
            </template>
            <template #end>
                <Button label="Exportar" icon="pi pi-file-export" severity="secondary" @click="exportCSV" />
            </template>
        </Toolbar>

        <DataTable ref="dt" :value="estudios" dataKey="idEstudio" :paginator="true" :rows="10" :filters="filters">
            <template #header>
                <IconField class="w-full sm:w-80">
                    <InputIcon class="pi pi-search" />
                    <InputText v-model="filters.global.value" placeholder="Buscar..." />
                </IconField>
            </template>
            <Column field="paciente.apellido" header="Paciente" sortable />
            <Column field="nombre" header="Nombre" sortable />
            <Column field="descripción" header="Descripción" sortable />
            <Column field="nombreArchivo" header="Archivo" sortable />
            <Column :exportable="false">
                <template #body="slotProps">
                    <Button icon="pi pi-download" outlined rounded @click="descargar(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>
