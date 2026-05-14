<script setup>
import EstudioService from '@/service/EstudioService';
import TurnoService from '@/service/TurnoService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const estudioService = new EstudioService();

onMounted(() => {
    // estudioService.findAllByPaciente({idPaciente: 1}).then((data) => (estudios.value = data));
    estudioService.findAll({}).then((data) => (estudios.value = data));
});

const toast = useToast();
const dt = ref();
const estudios = ref();
const medicoDialog = ref(false);
const deleteMedicoDialog = ref(false);
const deleteMedicosDialog = ref(false);
const medico = ref({});
const selectedMedicos = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});
const submitted = ref(false);
const especialidades = ref([
    { label: 'Pediatria', value: 'Pediatria' },
    { label: 'Traumatologia', value: 'Traumatologia' },
    { label: 'Clínico', value: 'Clínico' }
]);

// function formatCurrency(value) {
//     if (value) return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
//     return;
// }

function openNew() {
    medico.value = {};
    submitted.value = false;
    medicoDialog.value = true;
}

function hideDialog() {
    medicoDialog.value = false;
    submitted.value = false;
}

const saveMedico = async () => {
    submitted.value = true;
    if (
        (medico.value.descripcion, medico.value.apellido, medico.value.nombre, medico.value.direccion, medico.value.email, medico.value.especialidad, medico.value.password, medico.value.telefono)
        // medico.value.fechaRegistro,
    ) {
        try {
            const medicoSave = { ...medico.value };
            // Lógica para editar entidad
            if (medicoSave.idMedico) {
                const {
                    idMedico,
                    apellido,
                    nombre,
                    direccion,
                    email,
                    especialidad,
                    password,
                    telefono
                    // fechaRegistro,
                } = medicoSave;
                medicoSave.value = await medicoService.update(idMedico, {
                    apellido,
                    nombre,
                    direccion,
                    email,
                    especialidad,
                    password,
                    telefono
                    // fechaRegistro,
                });
                const index = medicos.value.findIndex((value) => value.idMedico == medico.value.idMedico);
                medicos.value[index] = medico.value;
                toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médico Actualizado', life: 3000 });
            } else {
                // Lógica para crear nueva entidad
                medico.value = await medicoService.create(medicoSave);
                medicos.value.push(medico.value);
                toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médico Creado', life: 3000 });
            }
        } catch (error) {
            // disabled.value = false;
            if (Array.isArray(error.message)) {
                for (const message of error.message) {
                    toast.add({ severity: 'error', summary: 'Error', detail: message, life: 6000 });
                }
            } else {
                toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al guardar Médico', life: 6000 });
            }
        }
        medicoDialog.value = false;
        medico.value = {};
    }
};

function editMedico(med) {
    medico.value = { ...med };
    medicoDialog.value = true;
}

function confirmDeleteMedico(med) {
    medico.value = med;
    deleteMedicoDialog.value = true;
}

function deleteMedico() {
    medicos.value = medicos.value.filter((val) => val.idMedico !== medico.value.idMedico);
    deleteMedicoDialog.value = false;
    medico.value = {};
    toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médico eliminado', life: 3000 });
}

function exportCSV() {
    dt.value.exportCSV();
}

function confirmDeleteSelected() {
    deleteMedicosDialog.value = true;
}

function deleteSelectedMedicos() {
    medicos.value = medicos.value.filter((val) => !selectedMedicos.value.includes(val));
    deleteMedicosDialog.value = false;
    selectedMedicos.value = null;
    toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médicos eliminados', life: 3000 });
}

function getStatusLabel(status) {
    switch (status) {
        case 'Pediatria':
            return 'success';

        case 'Traumatologia':
            return 'warn';

        case 'Clínico':
            return 'danger';

        default:
            return null;
    }
}

function formatFechaHora(fechaHora) {
    if (!fechaHora) return '';
    
    const fecha = new Date(fechaHora);
    
    // Formato: DD/MM/YYYY HH:mm
    return fecha.toLocaleString('es-AR', {
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
                <template #start>
                    <Button label="Nuevo Turno" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                    <Button label="Cancelar Médicos" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedMedicos || !selectedMedicos.length" />
                </template>

                <template #end>
                    <Button label="Exportar" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedMedicos"
                :value="estudios"
                dataKey="id"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} médicos"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Estudios</h4>
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
                <Column field="descripcion" header="Descripción" sortable style="min-width: 12rem"></Column>
                <Column field="nombre" header="Nombre" sortable style="min-width: 12rem"></Column>
                <!--para el color de los botones-->

                <Column :exportable="false" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Button label="Descargar" icon="pi pi-plus" outlined severity="primary" class="mr-2" @click="editMedico(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="medicoDialog" :style="{ width: '450px' }" header="Médico" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="nombre" class="block font-bold mb-3">Nombre</label>
                    <InputText id="nombre" v-model.trim="medico.nombre" required="true" autofocus :invalid="submitted && !medico.nombre" fluid />
                    <small v-if="submitted && !medico.nombre" class="text-red-500">Nombre es requerido.</small>
                </div>
                <div>
                    <label for="apellido" class="block font-bold mb-3">Apellido</label>
                    <InputText id="apellido" v-model.trim="medico.apellido" required="true" autofocus :invalid="submitted && !medico.apellido" fluid />
                    <small v-if="submitted && !medico.apellido" class="text-red-500">Apellido es requerido.</small>
                </div>
                <div>
                    <label for="email" class="block font-bold mb-3">Email</label>
                    <InputText id="email" v-model.trim="medico.email" required="true" autofocus :invalid="submitted && !medico.email" fluid />
                    <small v-if="submitted && !medico.email" class="text-red-500">Email es requerido.</small>
                </div>
                <div>
                    <label for="telefono" class="block font-bold mb-3">Teléfono</label>
                    <InputText id="telefono" v-model.trim="medico.telefono" required="true" autofocus :invalid="submitted && !medico.telefono" fluid />
                    <small v-if="submitted && !medico.telefono" class="text-red-500">Teléfono es requerido.</small>
                </div>
                <div>
                    <label for="direccion" class="block font-bold mb-3">Dirección</label>
                    <InputText id="direccion" v-model.trim="medico.direccion" required="true" autofocus :invalid="submitted && !medico.direccion" fluid />
                    <small v-if="submitted && !medico.direccion" class="text-red-500">Dirección es requerido.</small>
                </div>
                <div>
                    <label for="password" class="block font-bold mb-3">Contraseña</label>
                    <InputText id="password" v-model.trim="medico.password" required="true" autofocus :invalid="submitted && !medico.password" fluid />
                    <small v-if="submitted && !medico.password" class="text-red-500">Contraseña es requerido.</small>
                </div>
                <div>
                    <label for="especialidad" class="block font-bold mb-3">Especialidad</label>
                    <Select
                        id="especialidad"
                        v-model="medico.especialidad"
                        :options="especialidades"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Seleccionar especialidad"
                        autofocus
                        :invalid="submitted && !medico.especialidad"
                        fluid
                    ></Select>
                </div>
                <small v-if="submitted && !medico.especialidad" class="text-red-500">Especialidad es requerido.</small>
            </div>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Save" icon="pi pi-check" @click="saveMedico" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteMedicoDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product"
                    >Are you sure you want to delete <b>{{ medico.name }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteMedicoDialog = false" />
                <Button label="Yes" icon="pi pi-check" @click="deleteProduct" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteMedicosDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product">Are you sure you want to delete the selected products?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteMedicosDialog = false" />
                <Button label="Yes" icon="pi pi-check" text @click="deleteSelectedProducts" />
            </template>
        </Dialog>
    </div>
</template>
