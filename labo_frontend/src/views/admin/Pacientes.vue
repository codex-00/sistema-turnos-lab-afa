<script setup>
import PacienteService from '@/service/PacienteService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const pacienteService = new PacienteService();

onMounted(() => {
    pacienteService.findAllByPaciente({idPaciente: 1}).then((data) => (paciente.value = data));
});

const toast = useToast();
const dt = ref();
const paciente = ref();
const pacienteDialog = ref(false);
const deletePacienteDialog = ref(false);
const deletePacientesDialog = ref(false);
// const medico = ref({});
const selectedPacientes = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

// function formatCurrency(value) {
//     if (value) return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
//     return;
// }

function openNew() {
    paciente.value = {};
    submitted.value = false;
    pacienteDialog.value = true;
}

function hideDialog() {
    pacienteDialog.value = false;
    submitted.value = false;
}

// agregar, actualizar
const savePaciente = async () => {
    submitted.value = true;
    if (
        (paciente.value.descripcion, paciente.value.apellido, paciente.value.nombre, paciente.value.direccion, paciente.value.email, paciente.value.especialidad, paciente.value.password, paciente.value.telefono)
        // paciente.value.fechaRegistro,
    ) {
        try {
            const pacienteSave = { ...paciente.value };
            // Lógica para editar entidad
            if (pacienteSave.idPaciente) {
                const {
                    idPaciente,
                    apellido,
                    nombre,
                    direccion,
                    email,
                    especialidad,
                    password,
                    telefono
                    // fechaRegistro,
                } = pacienteSave;
                pacienteSave.value = await pacienteService.update(idPaciente, {
                    apellido,
                    nombre,
                    direccion,
                    email,
                    especialidad,
                    password,
                    telefono
                    // fechaRegistro,
                });
                const index = paciente.value.findIndex((value) => value.idPaciente == paciente.value.idPaciente);
                paciente.value[index] = paciente.value;
                toast.add({ severity: 'success', summary: 'Éxito', detail: 'Paciente Actualizado', life: 3000 });
            } else {
                // Lógica para crear nueva entidad
                paciente.value = await pacienteService.create(pacienteSave);
                paciente.value.push(medico.value);
                toast.add({ severity: 'success', summary: 'Éxito', detail: 'Paciente Creado', life: 3000 });
            }
        } catch (error) {
            // disabled.value = false;
            if (Array.isArray(error.message)) {
                for (const message of error.message) {
                    toast.add({ severity: 'error', summary: 'Error', detail: message, life: 6000 });
                }
            } else {
                toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al guardar Paciente', life: 6000 });
            }
        }
        pacienteDialog.value = false;
        paciente.value = {};
    }
};

function editMedico(med) {
    paciente.value = { ...med };
    pacienteDialog.value = true;
}

function confirmDeletePaciente(med) {
    paciente.value = med;
    deletePacienteDialog.value = true;
}

function deletePaciente() {
    paciente.value = paciente.value.filter((val) => val.idPaciente !== paciente.value.idPaciente);
    deletePacienteDialog.value = false;
    medico.value = {};
    toast.add({ severity: 'success', summary: 'Éxito', detail: 'Paciente eliminado', life: 3000 });
}

function exportCSV() {
    dt.value.exportCSV();
}

function confirmDeleteSelected() {
    deletePacienteDialog.value = true;
}

function deleteSelectedPacientes() {
    pacientes.value = pacientes.value.filter((val) => !selectedPacientes.value.includes(val));
    deletePacientesDialog.value = false;
    selectedPacientes.value = null;
    toast.add({ severity: 'success', summary: 'Éxito', detail: 'Pacientes eliminados', life: 3000 });
}

// function getStatusLabel(status) {
//     switch (status) {
//         case 'Pediatria':
//             return 'success';

//         case 'Traumatologia':
//             return 'warn';

//         case 'Clínico':
//             return 'danger';

//         default:
//             return null;
//     }
// }
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
                    <Button label="Agregar paciente" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                    <Button label="Eliminar paciente" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                </template>

                <template #end>
                    <Button label="Exportar" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedPacientes"
                :value="turnos"
                dataKey="id"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} pacientes"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Gestión de tpacientes</h4>
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <!-- <Column field="fechaDeTurno" header="Fecha y Hora" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        {{ formatFechaHora(slotProps.data.fechaDeTurno) }}
                    </template>            
                </Column> -->
                <Column field="paciente.apellido" header="Apellido" sortable style="min-width: 12rem"></Column>
                <Column field="paciente.nombre" header="Nombre" sortable style="min-width: 12rem"></Column>
                <Column field="paciente.email" header="Email" sortable style="min-width: 12rem"></Column>
                <Column field="paciente.telefono" header="Teléfono" sortable style="min-width: 12rem"></Column>

                <!-- <Column :exportable="false" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Button label="Atender" icon="pi pi-plus" severity="primary" class="mr-2" @click="editMedico(slotProps.data)" />
                        <Button Label="Cancelar" icon="pi pi-trash" outlined severity="danger" class="mr-2"  @click="confirmDeleteMedico(slotProps.data)" />
                    </template>
                </Column> -->
            </DataTable>
        </div>

        <Dialog v-model:visible="pacienteDialog" :style="{ width: '450px' }" header="Paciente" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="nombre" class="block font-bold mb-3">Nombre</label>
                    <InputText id="nombre" v-model.trim="paciente.nombre" required="true" autofocus :invalid="submitted && !paciente.nombre" fluid />
                    <small v-if="submitted && !paciente.nombre" class="text-red-500">Nombre es requerido.</small>
                </div>
                <div>
                    <label for="apellido" class="block font-bold mb-3">Apellido</label>
                    <InputText id="apellido" v-model.trim="paciente.apellido" required="true" autofocus :invalid="submitted && !paciente.apellido" fluid />
                    <small v-if="submitted && !paciente.apellido" class="text-red-500">Apellido es requerido.</small>
                </div>
                <div>
                    <label for="email" class="block font-bold mb-3">Email</label>
                    <InputText id="email" v-model.trim="paciente.email" required="true" autofocus :invalid="submitted && !paciente.email" fluid />
                    <small v-if="submitted && !paciente.email" class="text-red-500">Email es requerido.</small>
                </div>
                <div>
                    <label for="telefono" class="block font-bold mb-3">Teléfono</label>
                    <InputText id="telefono" v-model.trim="paciente.telefono" required="true" autofocus :invalid="submitted && !paciente.telefono" fluid />
                    <small v-if="submitted && !paciente.telefono" class="text-red-500">Teléfono es requerido.</small>
                </div>
                <div>
                    <label for="direccion" class="block font-bold mb-3">Dirección</label>
                    <InputText id="direccion" v-model.trim="paciente.direccion" required="true" autofocus :invalid="submitted && !paciente.direccion" fluid />
                    <small v-if="submitted && !paciente.direccion" class="text-red-500">Dirección es requerido.</small>
                </div>
                <div>
                    <label for="password" class="block font-bold mb-3">Contraseña</label>
                    <InputText id="password" v-model.trim="paicente.password" required="true" autofocus :invalid="submitted && !paciente.password" fluid />
                    <small v-if="submitted && !paciente.password" class="text-red-500">Contraseña es requerido.</small>
                </div>
                <div>
                    <label for="especialidad" class="block font-bold mb-3">Especialidad</label>
                    <Select
                        id="especialidad"
                        v-model="paciente.especialidad"
                        :options="especialidades"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Seleccionar especialidad"
                        autofocus
                        :invalid="submitted && !paciente.especialidad"
                        fluid
                    ></Select>
                </div>
                <small v-if="submitted && !paciente.especialidad" class="text-red-500">Especialidad es requerido.</small>
            </div>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Save" icon="pi pi-check" @click="savePaciente" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deletePacienteDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product"
                    >Are you sure you want to delete <b>{{ paciente.name }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deletePacienteDialog = false" />
                <Button label="Yes" icon="pi pi-check" @click="deleteProduct" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deletePacientesDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product">Are you sure you want to delete the selected products?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deletePaientesDialog = false" />
                <Button label="Yes" icon="pi pi-check" text @click="deleteSelectedProducts" />
            </template>
        </Dialog>
    </div>
</template>
