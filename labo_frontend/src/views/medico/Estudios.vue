<script setup>
import EstudioService from '@/service/EstudioService';
import PacienteService from '@/service/PacienteService';
import { useAuthStore } from '@/stores/auth';
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';

const estudioService = new EstudioService();
const pacienteService = new PacienteService();
const authStore = useAuthStore();
const toast = useToast();

const medicoId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : null));
const estudios = ref([]);
const pacientes = ref([]);
const estudio = ref({});
const archivo = ref(null);
const estudioDialog = ref(false);
const submitted = ref(false);

onMounted(async () => {
    await Promise.all([cargarEstudios(), cargarPacientes()]);
});

const cargarEstudios = async () => {
    if (!medicoId.value) return;
    estudios.value = await estudioService.findAllByMedico({ idMedico: medicoId.value });
};

const cargarPacientes = async () => {
    pacientes.value = await pacienteService.findAll();
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

    if (!estudio.value.nombre || !estudio.value.pacienteId || !archivo.value) return;

    try {
        await estudioService.create({
            pacienteId: estudio.value.pacienteId,
            nombre: estudio.value.nombre,
            descripcion: estudio.value.descripcion,
            archivo: archivo.value
        });

        hideDialog();
        await cargarEstudios();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Estudio subido', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo subir el estudio', life: 3000 });
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

const downloadEstudio = async (est) => {
    try {
        const response = await estudioService.download(est.idEstudio);
        const url = window.URL.createObjectURL(new Blob([response.data], { type: response.headers['content-type'] }));
        const link = document.createElement('a');
        link.href = url;
        link.download = est.nombreArchivo || est.nombre || 'estudio.pdf';
        link.click();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo descargar el estudio', life: 3000 });
    }
};

function formatPaciente(paciente) {
    if (!paciente) return '';
    return `${paciente.apellido || ''}, ${paciente.nombre || ''}`.trim();
}
</script>

<template>
    <div class="card">
        <Toolbar class="mb-6">
            <template #start>
                <Button label="Subir estudio" icon="pi pi-upload" severity="secondary" @click="openNew" />
            </template>
        </Toolbar>

        <DataTable :value="estudios" dataKey="idEstudio" :paginator="true" :rows="10">
            <Column field="paciente.apellido" header="Paciente" sortable>
                <template #body="slotProps">
                    {{ formatPaciente(slotProps.data.paciente) }}
                </template>
            </Column>
            <Column field="nombre" header="Estudio" sortable />
            <Column field="descripcion" header="Descripción" />
            <Column field="nombreArchivo" header="Archivo" />
            <Column :exportable="false" style="min-width: 10rem">
                <template #body="slotProps">
                    <Button icon="pi pi-eye" outlined rounded class="mr-2" @click="openPdf(slotProps.data)" />
                    <Button icon="pi pi-download" outlined rounded @click="downloadEstudio(slotProps.data)" />
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="estudioDialog" :style="{ width: '520px' }" header="Subir estudio PDF" :modal="true">
            <div class="flex flex-col gap-5">
                <div>
                    <label class="block font-bold mb-2">Paciente</label>
                    <Dropdown v-model="estudio.pacienteId" :options="pacientes" optionLabel="apellido" optionValue="idPaciente" placeholder="Seleccionar paciente" class="w-full">
                        <template #option="slotProps">
                            {{ formatPaciente(slotProps.option) }}
                        </template>
                        <template #value="slotProps">
                            {{ formatPaciente(pacientes.find((paciente) => paciente.idPaciente === slotProps.value)) || 'Seleccionar paciente' }}
                        </template>
                    </Dropdown>
                    <small v-if="submitted && !estudio.pacienteId" class="text-red-500">Paciente es requerido.</small>
                </div>

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
                    <label class="block font-bold mb-2">Archivo PDF</label>
                    <FileUpload mode="basic" name="archivo" chooseLabel="Seleccionar PDF" accept="application/pdf,.pdf" :auto="false" customUpload @select="onFileSelect" />
                    <small v-if="archivo" class="block mt-2 text-gray-500">{{ archivo.name }}</small>
                    <small v-if="submitted && !archivo" class="text-red-500 block mt-2">Archivo es requerido.</small>
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="saveEstudio" />
            </template>
        </Dialog>
    </div>
</template>
