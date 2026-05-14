<script setup>
import MedicoService from '@/service/MedicoService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const medicoService = new MedicoService();
const toast = useToast();

const dt = ref();
const medicos = ref([]);
const medicoDialog = ref(false);
const medico = ref({});
const selectedMedicos = ref();
const submitted = ref(false);
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});

const especialidades = ref([
    { label: 'Pediatria', value: 'Pediatria' },
    { label: 'Traumatologia', value: 'Traumatologia' },
    { label: 'Clinico', value: 'Clinico' }
]);

const diasDisponibilidad = ref([
    { label: 'Lunes', value: 'MONDAY' },
    { label: 'Martes', value: 'TUESDAY' },
    { label: 'Miercoles', value: 'WEDNESDAY' },
    { label: 'Jueves', value: 'THURSDAY' },
    { label: 'Viernes', value: 'FRIDAY' },
    { label: 'Sabado', value: 'SATURDAY' }
]);

const nuevaDisponibilidad = () => ({
    dia: null,
    horaInicio: '08:00',
    horaFin: '12:00',
    duracionTurno: 30
});

onMounted(async () => {
    await cargarMedicos();
});

const cargarMedicos = async () => {
    medicos.value = await medicoService.findAll();
};

function openNew() {
    medico.value = {
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
        disponibilidades: med.disponibilidades?.length ? med.disponibilidades : [nuevaDisponibilidad()]
    };
    submitted.value = false;
    medicoDialog.value = true;
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

const disponibilidadCompleta = (disp) => disp.dia && disp.horaInicio && disp.horaFin && disp.duracionTurno;

const saveMedico = async () => {
    submitted.value = true;

    const camposCompletos =
        medico.value.apellido &&
        medico.value.nombre &&
        medico.value.direccion &&
        medico.value.email &&
        medico.value.especialidad &&
        medico.value.password &&
        medico.value.telefono;
    const disponibilidadesCompletas = medico.value.disponibilidades?.length > 0 && medico.value.disponibilidades.every(disponibilidadCompleta);

    if (!camposCompletos || !disponibilidadesCompletas) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Complete los datos del medico y su disponibilidad', life: 4000 });
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
            toast.add({ severity: 'success', summary: 'Exito', detail: 'Medico actualizado', life: 3000 });
        } else {
            const medicoCreado = await medicoService.create(medicoSave);
            medicos.value.push(medicoCreado);
            toast.add({ severity: 'success', summary: 'Exito', detail: 'Medico creado', life: 3000 });
        }

        medicoDialog.value = false;
        medico.value = {};
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al guardar medico', life: 6000 });
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
            return null;
    }
}
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <Button label="Nuevo Medico" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
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
                currentPageReportTemplate="Mostrando de {first} a {last} de {totalRecords} medicos"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0">Administracion de Medicos</h4>
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText v-model="filters['global'].value" placeholder="Buscar..." />
                        </IconField>
                    </div>
                </template>

                <Column selectionMode="multiple" style="width: 3rem" :exportable="false" />
                <Column field="apellido" header="Apellido" sortable style="min-width: 12rem" />
                <Column field="nombre" header="Nombre" sortable style="min-width: 12rem" />
                <Column field="email" header="Email" sortable style="min-width: 12rem" />
                <Column field="telefono" header="Telefono" sortable style="min-width: 12rem" />
                <Column field="especialidad" header="Especialidad" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <Tag :value="slotProps.data.especialidad" :severity="getStatusLabel(slotProps.data.especialidad)" />
                    </template>
                </Column>
                <Column :exportable="false" style="min-width: 8rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editMedico(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>

        <Dialog v-model:visible="medicoDialog" :style="{ width: '760px' }" header="Medico" :modal="true">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                    <label for="nombre" class="block font-bold mb-2">Nombre</label>
                    <InputText id="nombre" v-model.trim="medico.nombre" :invalid="submitted && !medico.nombre" fluid />
                    <small v-if="submitted && !medico.nombre" class="text-red-500">Nombre es requerido.</small>
                </div>

                <div>
                    <label for="apellido" class="block font-bold mb-2">Apellido</label>
                    <InputText id="apellido" v-model.trim="medico.apellido" :invalid="submitted && !medico.apellido" fluid />
                    <small v-if="submitted && !medico.apellido" class="text-red-500">Apellido es requerido.</small>
                </div>

                <div>
                    <label for="email" class="block font-bold mb-2">Email</label>
                    <InputText id="email" v-model.trim="medico.email" :invalid="submitted && !medico.email" fluid />
                    <small v-if="submitted && !medico.email" class="text-red-500">Email es requerido.</small>
                </div>

                <div>
                    <label for="telefono" class="block font-bold mb-2">Telefono</label>
                    <InputText id="telefono" v-model.trim="medico.telefono" :invalid="submitted && !medico.telefono" fluid />
                    <small v-if="submitted && !medico.telefono" class="text-red-500">Telefono es requerido.</small>
                </div>

                <div>
                    <label for="direccion" class="block font-bold mb-2">Direccion</label>
                    <InputText id="direccion" v-model.trim="medico.direccion" :invalid="submitted && !medico.direccion" fluid />
                    <small v-if="submitted && !medico.direccion" class="text-red-500">Direccion es requerida.</small>
                </div>

                <div>
                    <label for="password" class="block font-bold mb-2">Contrasena</label>
                    <InputText id="password" v-model.trim="medico.password" :invalid="submitted && !medico.password" fluid />
                    <small v-if="submitted && !medico.password" class="text-red-500">Contrasena es requerida.</small>
                </div>

                <div class="md:col-span-2">
                    <label for="especialidad" class="block font-bold mb-2">Especialidad</label>
                    <Select
                        id="especialidad"
                        v-model="medico.especialidad"
                        :options="especialidades"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Seleccionar especialidad"
                        :invalid="submitted && !medico.especialidad"
                        fluid
                    />
                    <small v-if="submitted && !medico.especialidad" class="text-red-500">Especialidad es requerida.</small>
                </div>
            </div>

            <div class="mt-6">
                <div class="flex items-center justify-between mb-3">
                    <h5 class="m-0">Disponibilidad semanal</h5>
                    <Button label="Agregar horario" icon="pi pi-plus" severity="secondary" outlined @click="agregarDisponibilidad" />
                </div>

                <div class="flex flex-col gap-3">
                    <div
                        v-for="(disp, index) in medico.disponibilidades"
                        :key="index"
                        class="grid grid-cols-1 md:grid-cols-5 gap-3 items-end"
                    >
                        <div>
                            <label class="block font-bold mb-2">Dia</label>
                            <Select
                                v-model="disp.dia"
                                :options="diasDisponibilidad"
                                optionLabel="label"
                                optionValue="value"
                                placeholder="Dia"
                                :invalid="submitted && !disp.dia"
                                fluid
                            />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Inicio</label>
                            <InputText v-model="disp.horaInicio" placeholder="08:00" :invalid="submitted && !disp.horaInicio" fluid />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Fin</label>
                            <InputText v-model="disp.horaFin" placeholder="12:00" :invalid="submitted && !disp.horaFin" fluid />
                        </div>

                        <div>
                            <label class="block font-bold mb-2">Duracion</label>
                            <InputNumber v-model="disp.duracionTurno" suffix=" min" :min="5" :step="5" :invalid="submitted && !disp.duracionTurno" fluid />
                        </div>

                        <Button icon="pi pi-trash" severity="danger" outlined @click="quitarDisponibilidad(index)" />
                    </div>
                </div>

                <small v-if="submitted && !medico.disponibilidades?.every(disponibilidadCompleta)" class="text-red-500 block mt-2">
                    Complete al menos un dia y rango horario de atencion.
                </small>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="saveMedico" />
            </template>
        </Dialog>
    </div>
</template>
