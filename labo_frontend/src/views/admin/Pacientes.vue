<script setup>
import PacienteService from '@/service/PacienteService';
import { direccionValida, dniValido as validarDni, emailValido as validarEmail, limpiarPersona, nombreValido, soloLetras, soloNumeros, telefonoValido as validarTelefono, textoBasico } from '@/utils/personaValidacion';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const pacienteService = new PacienteService();
const toast = useToast();

const dt = ref();
const pacientes = ref([]);
const paciente = ref({});
const pacienteDialog = ref(false);
const deletePacienteDialog = ref(false);
const submitted = ref(false);
const selectedPacientes = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

onMounted(async () => {
    await cargarPacientes();
});

const cargarPacientes = async () => {
    try {
        pacientes.value = await pacienteService.findAll();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudieron cargar los pacientes', life: 3000 });
    }
};

function openNew() {
    paciente.value = {};
    submitted.value = false;
    pacienteDialog.value = true;
}

function hideDialog() {
    pacienteDialog.value = false;
    submitted.value = false;
}

function editPaciente(pac) {
    paciente.value = { ...pac, password: '' };
    submitted.value = false;
    pacienteDialog.value = true;
}

function confirmDeletePaciente(pac) {
    paciente.value = pac;
    deletePacienteDialog.value = true;
}

const dniValido = () => validarDni(paciente.value.dni);
const emailValido = () => validarEmail(paciente.value.email);
const telefonoValido = () => validarTelefono(paciente.value.telefono);
const nombreCorrecto = () => nombreValido(paciente.value.nombre);
const apellidoCorrecto = () => nombreValido(paciente.value.apellido);
const direccionCorrecta = () => direccionValida(paciente.value.direccion);
const passwordRequerida = () => !paciente.value.idPaciente;
const normalizarPaciente = () => {
    paciente.value = limpiarPersona(paciente.value);
};

const savePaciente = async () => {
    submitted.value = true;
    normalizarPaciente();

    const camposCompletos = dniValido() && nombreCorrecto() && apellidoCorrecto() && emailValido() && direccionCorrecta() && telefonoValido() && (!passwordRequerida() || paciente.value.password);

    if (!camposCompletos) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Complete los datos obligatorios del paciente', life: 3000 });
        return;
    }

    try {
        if (paciente.value.idPaciente) {
            await pacienteService.update(paciente.value.idPaciente, paciente.value);
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Paciente actualizado', life: 3000 });
        } else {
            await pacienteService.create(paciente.value);
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Paciente creado', life: 3000 });
        }

        pacienteDialog.value = false;
        paciente.value = {};
        await cargarPacientes();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al guardar paciente', life: 3000 });
    }
};

const deletePaciente = async () => {
    try {
        await pacienteService.remove(paciente.value.idPaciente);
        deletePacienteDialog.value = false;
        paciente.value = {};
        await cargarPacientes();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Paciente eliminado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo eliminar el paciente', life: 3000 });
    }
};

function exportCSV() {
    dt.value.exportCSV();
}
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <Button label="Agregar paciente" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                </template>

                <template #end>
                    <Button label="Exportar" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedPacientes"
                :value="pacientes"
                dataKey="idPaciente"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} pacientes"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Gestión de pacientes</h4>
                        <IconField class="w-full sm:w-80">
                            <InputIcon class="pi pi-search" />
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <Column field="dni" header="DNI" sortable style="min-width: 8rem" />
                <Column field="apellido" header="Apellido" sortable style="min-width: 12rem" />
                <Column field="nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="email" header="Email" sortable style="min-width: 12rem" />
                <Column field="telefono" header="Teléfono" sortable style="min-width: 12rem" />
                <Column field="direccion" header="Dirección" sortable style="min-width: 12rem" />
                <Column :exportable="false" style="min-width: 8rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editPaciente(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" @click="confirmDeletePaciente(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="pacienteDialog" :style="{ width: '450px' }" header="Paciente" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="dni" class="block font-bold mb-3">DNI</label>
                    <InputText id="dni" v-model.trim="paciente.dni" :invalid="submitted && !dniValido()" fluid @input="paciente.dni = soloNumeros(paciente.dni)" />
                    <small v-if="submitted && !dniValido()" class="text-red-500">DNI requerido, entre 7 y 10 dígitos.</small>
                </div>
                <div>
                    <label for="nombre" class="block font-bold mb-3">Nombre</label>
                    <InputText id="nombre" v-model.trim="paciente.nombre" :invalid="submitted && !nombreCorrecto()" fluid @input="paciente.nombre = soloLetras(paciente.nombre)" />
                    <small v-if="submitted && !nombreCorrecto()" class="text-red-500">Solo letras y espacios.</small>
                </div>
                <div>
                    <label for="apellido" class="block font-bold mb-3">Apellido</label>
                    <InputText id="apellido" v-model.trim="paciente.apellido" :invalid="submitted && !apellidoCorrecto()" fluid @input="paciente.apellido = soloLetras(paciente.apellido)" />
                    <small v-if="submitted && !apellidoCorrecto()" class="text-red-500">Solo letras y espacios.</small>
                </div>
                <div>
                    <label for="email" class="block font-bold mb-3">Email</label>
                    <InputText id="email" v-model.trim="paciente.email" :invalid="submitted && !emailValido()" fluid />
                    <small v-if="submitted && !emailValido()" class="text-red-500">Email válido requerido.</small>
                </div>
                <div>
                    <label for="telefono" class="block font-bold mb-3">Teléfono</label>
                    <InputText id="telefono" v-model.trim="paciente.telefono" :invalid="submitted && !telefonoValido()" fluid @input="paciente.telefono = soloNumeros(paciente.telefono)" />
                    <small v-if="submitted && !telefonoValido()" class="text-red-500">Solo numeros, entre 6 y 15 digitos.</small>
                </div>
                <div>
                    <label for="direccion" class="block font-bold mb-3">Dirección</label>
                    <InputText id="direccion" v-model.trim="paciente.direccion" :invalid="submitted && !direccionCorrecta()" fluid @input="paciente.direccion = textoBasico(paciente.direccion)" />
                    <small v-if="submitted && !direccionCorrecta()" class="text-red-500">Direccion requerida, sin caracteres especiales.</small>
                </div>
                <div>
                    <label for="password" class="block font-bold mb-3">Contraseña</label>
                    <InputText id="password" v-model.trim="paciente.password" type="password" :invalid="submitted && passwordRequerida() && !paciente.password" fluid />
                    <small class="text-gray-500">En edición, dejar vacío para conservar la contraseña actual.</small>
                    <small v-if="submitted && passwordRequerida() && !paciente.password" class="text-red-500 block">Contraseña requerida.</small>
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="savePaciente" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deletePacienteDialog" :style="{ width: '450px' }" header="Confirmar" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span
                    >Desea eliminar a <b>{{ paciente.nombre }} {{ paciente.apellido }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deletePacienteDialog = false" />
                <Button label="Sí" icon="pi pi-check" severity="danger" @click="deletePaciente" />
            </template>
        </Dialog>
    </div>
</template>
