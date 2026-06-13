<script setup>
import MedicoService from '@/service/MedicoService';
import { direccionValida, dniValido as validarDni, emailValido as validarEmail, limpiarPersona, nombreValido, soloLetras, soloNumeros, telefonoValido as validarTelefono, textoBasico } from '@/utils/personaValidacion';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const medicoService = new MedicoService();
const toast = useToast();

const dt = ref();
const medicos = ref([]);
const medicoDialog = ref(false);
const deleteMedicoDialog = ref(false);
const medico = ref({});
const selectedMedicos = ref();
const submitted = ref(false);
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

const especialidades = ref([
    { label: 'Pediatría', value: 'Pediatria' },
    { label: 'Traumatología', value: 'Traumatologia' },
    { label: 'Clínico', value: 'Clinico' }
]);

const diasDisponibilidad = ref([
    { label: 'Lunes', value: 'MONDAY' },
    { label: 'Martes', value: 'TUESDAY' },
    { label: 'Miércoles', value: 'WEDNESDAY' },
    { label: 'Jueves', value: 'THURSDAY' },
    { label: 'Viernes', value: 'FRIDAY' },
    { label: 'Sábado', value: 'SATURDAY' }
]);

const horariosDisponibilidad = ref(
    Array.from({ length: 35 }, (_, index) => {
        const totalMinutos = 6 * 60 + index * 30;
        const hora = String(Math.floor(totalMinutos / 60)).padStart(2, '0');
        const minutos = String(totalMinutos % 60).padStart(2, '0');
        const value = `${hora}:${minutos}`;
        return { label: value, value };
    })
);

const nuevaDisponibilidad = () => ({
    especialidad: null,
    dia: null,
    horaInicio: '08:00',
    horaFin: '12:00',
    duracionTurno: 30
});

onMounted(async () => {
    await cargarMedicos();
});

const cargarMedicos = async () => {
    try {
        medicos.value = await medicoService.findAll();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudieron cargar los médicos', life: 4000 });
    }
};

function openNew() {
    medico.value = {
        dni: '',
        password: '',
        especialidades: [],
        disponibilidades: [nuevaDisponibilidad()]
    };
    submitted.value = false;
    medicoDialog.value = true;
}

function hideDialog() {
    medicoDialog.value = false;
    submitted.value = false;
}

function editMedico(med) {
    medico.value = {
        ...med,
        password: '',
        especialidades: med.especialidades?.length ? med.especialidades : (med.especialidad || '').split(',').map((item) => item.trim()).filter(Boolean),
        disponibilidades: med.disponibilidades?.length ? med.disponibilidades : [nuevaDisponibilidad()]
    };
    submitted.value = false;
    medicoDialog.value = true;
}

function confirmDeleteMedico(med) {
    medico.value = med;
    deleteMedicoDialog.value = true;
}

function agregarDisponibilidad() {
    if (!medico.value.disponibilidades) {
        medico.value.disponibilidades = [];
    }
    medico.value.disponibilidades.push(nuevaDisponibilidad());
}

function quitarDisponibilidad(index) {
    medico.value.disponibilidades.splice(index, 1);
    if (medico.value.disponibilidades.length === 0) {
        medico.value.disponibilidades.push(nuevaDisponibilidad());
    }
}

const dniValido = () => validarDni(medico.value.dni);
const emailValido = () => validarEmail(medico.value.email);
const telefonoValido = () => validarTelefono(medico.value.telefono);
const nombreCorrecto = () => nombreValido(medico.value.nombre);
const apellidoCorrecto = () => nombreValido(medico.value.apellido);
const direccionCorrecta = () => direccionValida(medico.value.direccion);
const passwordRequerida = () => !medico.value.idMedico;
const normalizarMedico = () => {
    medico.value = {
        ...limpiarPersona(medico.value),
        especialidades: (medico.value.especialidades || []).map((item) => soloLetras(item).trim()).filter(Boolean),
        disponibilidades: medico.value.disponibilidades
    };
    medico.value.especialidad = medico.value.especialidades.join(', ');
};
const disponibilidadCompleta = (disp) => disp.especialidad && disp.dia && disp.horaInicio && disp.horaFin && disp.duracionTurno;
const minutosDesdeHora = (hora) => {
    if (!hora) return null;
    const [horas, minutos] = hora.split(':').map(Number);
    return horas * 60 + minutos;
};
const disponibilidadValida = (disp) => {
    if (!disponibilidadCompleta(disp)) {
        return false;
    }

    const inicio = minutosDesdeHora(disp.horaInicio);
    const fin = minutosDesdeHora(disp.horaFin);
    return inicio < fin && disp.duracionTurno > 0 && disp.duracionTurno <= fin - inicio;
};
const disponibilidadesSinSolapamientos = () => {
    const disponibilidades = medico.value.disponibilidades || [];
    for (let i = 0; i < disponibilidades.length; i++) {
        for (let j = i + 1; j < disponibilidades.length; j++) {
            const primera = disponibilidades[i];
            const segunda = disponibilidades[j];
            const seSolapan = minutosDesdeHora(primera.horaInicio) < minutosDesdeHora(segunda.horaFin) && minutosDesdeHora(primera.horaFin) > minutosDesdeHora(segunda.horaInicio);
            if (primera.dia === segunda.dia && seSolapan) {
                return false;
            }
        }
    }
    return true;
};

const saveMedico = async () => {
    submitted.value = true;
    normalizarMedico();

    const camposCompletos = apellidoCorrecto() && nombreCorrecto() && dniValido() && direccionCorrecta() && emailValido() && medico.value.especialidades?.length > 0 && (!passwordRequerida() || medico.value.password) && telefonoValido();
    const disponibilidadesCompletas = medico.value.disponibilidades?.length > 0 && medico.value.disponibilidades.every(disponibilidadValida) && disponibilidadesSinSolapamientos();

    if (!camposCompletos || !disponibilidadesCompletas) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Complete los datos del médico, DNI válido y disponibilidad', life: 4000 });
        return;
    }

    try {
        const medicoSave = { ...medico.value };

        if (medicoSave.idMedico) {
            const medicoActualizado = await medicoService.update(medicoSave.idMedico, medicoSave);
            const index = medicos.value.findIndex((value) => value.idMedico === medicoSave.idMedico);
            if (index >= 0) {
                medicos.value[index] = medicoActualizado;
            }
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médico actualizado', life: 3000 });
        } else {
            const medicoCreado = await medicoService.create(medicoSave);
            medicos.value.push(medicoCreado);
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médico creado', life: 3000 });
        }

        medicoDialog.value = false;
        medico.value = {};
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al guardar medico', life: 6000 });
    }
};

const deleteMedico = async () => {
    try {
        await medicoService.remove(medico.value.idMedico);
        deleteMedicoDialog.value = false;
        medico.value = {};
        await cargarMedicos();
        toast.add({ severity: 'success', summary: 'Éxito', detail: 'Médico eliminado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo eliminar el médico', life: 6000 });
    }
};

function exportCSV() {
    dt.value.exportCSV();
}

function getStatusLabel(status) {
    switch (status) {
        case 'Pediatria':
            return 'success';
        case 'Traumatologia':
            return 'warn';
        case 'Clinico':
            return 'danger';
        default:
            return 'info';
    }
}
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <Button label="Nuevo médico" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                </template>

                <template #end>
                    <Button label="Exportar" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedMedicos"
                :value="medicos"
                dataKey="idMedico"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} médicos"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Administración de médicos</h4>
                        <IconField class="w-full sm:w-80">
                            <InputIcon class="pi pi-search" />
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <Column selectionMode="multiple" style="width: 3rem" :exportable="false" />
                <Column field="apellido" header="Apellido" sortable style="min-width: 12rem" />
                <Column field="nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="dni" header="DNI" sortable style="min-width: 10rem" />
                <Column field="email" header="Email" sortable style="min-width: 12rem" />
                <Column field="telefono" header="Teléfono" sortable style="min-width: 12rem" />
                <Column field="especialidad" header="Especialidad" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <div class="flex flex-wrap gap-1">
                            <Tag v-for="esp in slotProps.data.especialidades || [slotProps.data.especialidad]" :key="esp" :value="esp" :severity="getStatusLabel(esp)" />
                        </div>
                    </template>
                </Column>
                <Column :exportable="false" style="min-width: 8rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editMedico(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" @click="confirmDeleteMedico(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="medicoDialog" :style="{ width: '760px' }" header="Médico" :modal="true">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                    <label for="nombre" class="block font-bold mb-2">Nombre</label>
                    <InputText id="nombre" v-model.trim="medico.nombre" :invalid="submitted && !nombreCorrecto()" fluid @input="medico.nombre = soloLetras(medico.nombre)" />
                    <small v-if="submitted && !nombreCorrecto()" class="text-red-500">Solo letras y espacios.</small>
                </div>

                <div>
                    <label for="apellido" class="block font-bold mb-2">Apellido</label>
                    <InputText id="apellido" v-model.trim="medico.apellido" :invalid="submitted && !apellidoCorrecto()" fluid @input="medico.apellido = soloLetras(medico.apellido)" />
                    <small v-if="submitted && !apellidoCorrecto()" class="text-red-500">Solo letras y espacios.</small>
                </div>

                <div>
                    <label for="email" class="block font-bold mb-2">Email</label>
                    <InputText id="email" v-model.trim="medico.email" :invalid="submitted && !emailValido()" fluid />
                    <small v-if="submitted && !emailValido()" class="text-red-500">Email válido requerido.</small>
                </div>

                <div>
                    <label for="dni" class="block font-bold mb-2">DNI</label>
                    <InputText id="dni" v-model.trim="medico.dni" :invalid="submitted && !dniValido()" fluid @input="medico.dni = soloNumeros(medico.dni)" />
                    <small v-if="submitted && !dniValido()" class="text-red-500">DNI requerido, entre 7 y 10 dígitos.</small>
                </div>

                <div>
                    <label for="telefono" class="block font-bold mb-2">Teléfono</label>
                    <InputText id="telefono" v-model.trim="medico.telefono" :invalid="submitted && !telefonoValido()" fluid @input="medico.telefono = soloNumeros(medico.telefono)" />
                    <small v-if="submitted && !telefonoValido()" class="text-red-500">Solo numeros, entre 6 y 15 digitos.</small>
                </div>

                <div>
                    <label for="direccion" class="block font-bold mb-2">Dirección</label>
                    <InputText id="direccion" v-model.trim="medico.direccion" :invalid="submitted && !direccionCorrecta()" fluid @input="medico.direccion = textoBasico(medico.direccion)" />
                    <small v-if="submitted && !direccionCorrecta()" class="text-red-500">Direccion requerida, sin caracteres especiales.</small>
                </div>

                <div>
                    <label for="password" class="block font-bold mb-2">Contraseña</label>
                    <InputText id="password" v-model.trim="medico.password" type="password" :invalid="submitted && passwordRequerida() && !medico.password" fluid />
                    <small class="text-gray-500">En edición, dejar vacío para conservar la contraseña actual.</small>
                    <small v-if="submitted && passwordRequerida() && !medico.password" class="text-red-500 block">Contraseña requerida.</small>
                </div>

                <div class="md:col-span-2">
                    <label for="especialidad" class="block font-bold mb-2">Especialidad</label>
                    <MultiSelect id="especialidad" v-model="medico.especialidades" :options="especialidades" optionLabel="label" optionValue="value" placeholder="Seleccionar especialidades" display="chip" :invalid="submitted && !medico.especialidades?.length" fluid />
                    <small v-if="submitted && !medico.especialidades?.length" class="text-red-500">Seleccione al menos una especialidad.</small>
                </div>
            </div>

            <div class="mt-6">
                <div class="flex items-center justify-between mb-3">
                    <h5 class="m-0">Disponibilidad semanal</h5>
                    <Button label="Agregar horario" icon="pi pi-plus" severity="secondary" outlined @click="agregarDisponibilidad" />
                </div>

                <div class="flex flex-col gap-3">
                    <div v-for="(disp, index) in medico.disponibilidades" :key="index" class="grid grid-cols-1 md:grid-cols-6 gap-3 items-end">
                        <div>
                            <label class="block font-bold mb-2">Especialidad</label>
                            <Select v-model="disp.especialidad" :options="medico.especialidades || []" placeholder="Especialidad" :invalid="submitted && !disp.especialidad" fluid />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Día</label>
                            <Select v-model="disp.dia" :options="diasDisponibilidad" optionLabel="label" optionValue="value" placeholder="Día" :invalid="submitted && !disp.dia" fluid />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Inicio</label>
                            <Select v-model="disp.horaInicio" :options="horariosDisponibilidad" optionLabel="label" optionValue="value" placeholder="Inicio" :invalid="submitted && !disp.horaInicio" fluid />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Fin</label>
                            <Select v-model="disp.horaFin" :options="horariosDisponibilidad" optionLabel="label" optionValue="value" placeholder="Fin" :invalid="submitted && !disp.horaFin" fluid />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Duración</label>
                            <InputNumber v-model="disp.duracionTurno" suffix=" min" :min="5" :step="5" :invalid="submitted && !disp.duracionTurno" fluid />
                        </div>

                        <Button icon="pi pi-trash" severity="danger" outlined @click="quitarDisponibilidad(index)" />
                    </div>
                </div>

                <small v-if="submitted && (!medico.disponibilidades?.every(disponibilidadValida) || !disponibilidadesSinSolapamientos())" class="text-red-500 block mt-2"> Complete al menos un dia y rango horario valido, sin horarios superpuestos. </small>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="saveMedico" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteMedicoDialog" :style="{ width: '450px' }" header="Confirmar" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span
                    >Desea eliminar a <b>{{ medico.nombre }} {{ medico.apellido }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteMedicoDialog = false" />
                <Button label="Sí" icon="pi pi-check" severity="danger" @click="deleteMedico" />
            </template>
        </Dialog>
    </div>
</template>
