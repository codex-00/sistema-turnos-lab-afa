<script setup>
import EstudioService from '@/service/EstudioService';
import { useAuthStore } from '@/stores/auth';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const estudioService = new EstudioService();
const toast = useToast();
const authStore = useAuthStore();

const pacienteId = computed(() => (authStore.rol === 'paciente' && authStore.token ? authStore.token : 1));
const dt = ref();
const estudios = ref([]);
const estudio = ref({});
const archivo = ref(null);
const estudioDialog = ref(false);
const deleteEstudioDialog = ref(false);
const selectedEstudios = ref();
const submitted = ref(false);
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

onMounted(async () => {
    await cargarEstudios();
});

const cargarEstudios = async () => {
    try {
        estudios.value = await estudioService.findAllByPaciente({ idPaciente: pacienteId.value });
    } catch (error) {
        estudios.value = [];
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los estudios', life: 3000 });
    }
};

function openNew() {
    estudio.value = {};
    archivo.value = null;
    submitted.value = false;
    estudioDialog.value = true;
}

function hideDialog() {
    estudioDialog.value = false;
    submitted.value = false;
}

function onFileSelect(event) {
    archivo.value = event.files?.[0] || null;
}

const saveEstudio = async () => {
    submitted.value = true;

    if (!estudio.value.nombre || !archivo.value) {
        return;
    }

    try {
        await estudioService.create({
            pacienteId: pacienteId.value,
            nombre: estudio.value.nombre,
            descripcion: estudio.value.descripcion,
            archivo: archivo.value
        });

        estudioDialog.value = false;
        estudio.value = {};
        archivo.value = null;
        await cargarEstudios();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Estudio subido', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo subir el estudio', life: 3000 });
    }
};

const downloadEstudio = async (est) => {
    try {
        const response = await estudioService.download(est.idEstudio);
        const url = window.URL.createObjectURL(new Blob([response.data], { type: response.headers['content-type'] }));
        const link = document.createElement('a');
        link.href = url;
        link.download = est.nombreArchivo || est.nombre || 'estudio';
        link.click();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo descargar el estudio', life: 3000 });
    }
};

const openPdf = async (est) => {
    try {
        const response = await estudioService.view(est.idEstudio);
        const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
        window.open(url, '_blank', 'noopener,noreferrer');
        setTimeout(() => window.URL.revokeObjectURL(url), 10000);
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo visualizar el estudio', life: 3000 });
    }
};

function confirmDeleteEstudio(est) {
    estudio.value = est;
    deleteEstudioDialog.value = true;
}

const deleteEstudio = async () => {
    try {
        await estudioService.remove(estudio.value.idEstudio);
        deleteEstudioDialog.value = false;
        estudio.value = {};
        await cargarEstudios();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Estudio eliminado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo eliminar el estudio', life: 3000 });
    }
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
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #end>
                    <Button label="Exportar" icon="pi pi-file-export" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedEstudios"
                :value="estudios"
                dataKey="idEstudio"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} estudios"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Mis estudios</h4>
                        <IconField class="w-full sm:w-80">
                            <InputIcon class="pi pi-search" />
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <Column field="fechaCreacion" header="Fecha" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ formatFechaHora(slotProps.data.fechaCreacion) }}
                    </template>
                </Column>
                <Column field="nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="descripcion" header="Descripción" sortable style="min-width: 16rem" />
                <Column field="nombreArchivo" header="Archivo" sortable style="min-width: 12rem" />

                <Column :exportable="false" style="min-width: 10rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-eye" outlined rounded class="mr-2" @click="openPdf(slotProps.data)" />
                        <Button icon="pi pi-download" outlined rounded class="mr-2" @click="downloadEstudio(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="estudioDialog" :style="{ width: '520px' }" header="Subir estudio" :modal="true">
            <div class="flex flex-col gap-5">
                <div>
                    <label class="block font-bold mb-2">Nombre</label>
                    <InputText v-model.trim="estudio.nombre" :invalid="submitted && !estudio.nombre" fluid />
                    <small v-if="submitted && !estudio.nombre" class="text-red-500">Nombre es requerido.</small>
                </div>

                <div>
                    <label class="block font-bold mb-2">Descripción</label>
                    <Textarea v-model.trim="estudio.descripcion" rows="3" fluid />
                </div>

                <div>
                    <label class="block font-bold mb-2">Archivo</label>
                    <FileUpload mode="basic" name="archivo" chooseLabel="Seleccionar archivo" :auto="false" customUpload @select="onFileSelect" />
                    <small v-if="archivo" class="block mt-2 text-gray-500">{{ archivo.name }}</small>
                    <small v-if="submitted && !archivo" class="text-red-500 block mt-2">Archivo es requerido.</small>
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="saveEstudio" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteEstudioDialog" :style="{ width: '450px' }" header="Confirmar" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span>
                    ¿Desea eliminar el estudio <b>{{ estudio.nombre }}</b
                    >?
                </span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteEstudioDialog = false" />
                <Button label="Sí" icon="pi pi-check" severity="danger" @click="deleteEstudio" />
            </template>
        </Dialog>
    </div>
</template>
