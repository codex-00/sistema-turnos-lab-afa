<script setup>

import MedicoService from '@/service/MedicoService';
import TurnoService from '@/service/TurnoService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const medicoService = new MedicoService();

onMounted(() => {
    turnoService.findAllByPaciente({idPaciente: 1}).then((data) => (turnos.value = data));
});

const toast = useToast();
const dt = ref();
const turnos = ref();
const medicos = ref();
const turnoDialog = ref(false);
const deleteTurnoDialog = ref(false);
const deleteTurnosDialog = ref(false);
const turno = ref({});
const selectedTurnos = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});
const submitted = ref(false);
const especialidades = ref([
    { label: 'Pediatria', value: 'Pediatria' },
    { label: 'Traumatologia', value: 'Traumatologia' },
    { label: 'Clínico', value: 'Clínico' }
]);

const loadingMedicos = ref(false);

// Función que se ejecuta cuando cambia la especialidad
const onEspecialidadChange = async (event) => {
  const especialidadSeleccionada = event.value;
  
  if (especialidadSeleccionada) {
    // Limpiar médico seleccionado previo
    turno.value.medico = null;
    
    // Mostrar loading
    loadingMedicos.value = true;
    
    try {
      // Ejecutar tu función existente (corrigiendo el typo)
      await obtenerMedicosPorEspecialidad({ especialidad: especialidadSeleccionada });
      
      // Transformar los datos para mostrar nombre completo
      medicos.value = medicos.value.map(medico => ({
        ...medico,
        nombreCompleto: `${medico.nombre} ${medico.apellido}`
      }));
      
    } catch (error) {
      console.error('Error al obtener médicos:', error);
      // Opcional: mostrar mensaje de error al usuario
    } finally {
      loadingMedicos.value = false;
    }
  } else {
    // Si no hay especialidad seleccionada, limpiar médicos
    medicos.value = [];
    turno.value.medico = null;
  }
};

// función existente
const obtenerMedicosPorEspecialidad = async ({ especialidad }) => {
  medicos.value = await medicoService.findAllByEspecialidad({ especialidad: especialidad });
};

// Actualizar la validación en saveTurno
// const saveTurno = () => {
//   submitted.value = true;
  
//   // Validar que todos los campos requeridos estén completos
//   if (turno.value.especialidad && turno.value.medico && calendarValue.value) {
//     // Aquí va tu lógica para guardar el turno
//     console.log('Guardando turno:', {
//       fecha: calendarValue.value,
//       especialidad: turno.value.especialidad,
//       medico: turno.value.medico
//     });
    
//     hideDialog();
//   }
// };

function openNew() {
    turno.value = {};
    submitted.value = false;
    turnoDialog.value = true;
}

function hideDialog() {
    turnoDialog.value = false;
    submitted.value = false;
}

const saveTurno = async () => {
    submitted.value = true;
    if (
        (turno.value.medico, turno.value.fechaDeTurno)
        // turno.value.fechaRegistro,
    ) {
        try {
            const medico =  { idMedico: turno.value.medico}
            console.log('turnos.value[0].paciente --------');
            console.log(turnos.value[0].paciente);
            
            
            // Lógica para crear nueva entidad
            turno.value = await turnoService.create({ 
                fechaDeTurno: turno.value.fechaDeTurno,
                medico: medico,
                paciente: turnos.value[0].paciente,
            });
            await turnoService.findAllByPaciente({idPaciente: 1}).then((data) => (turnos.value = data));
            toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno Creado', life: 3000 });
        
        } catch (error) {
            // disabled.value = false;
            if (Array.isArray(error.message)) {
                for (const message of error.message) {
                    toast.add({ severity: 'error', summary: 'Error', detail: message, life: 6000 });
                }
            } else {
                toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'Error al guardar Turno', life: 6000 });
            }
        }
        turnoDialog.value = false;
        turno.value = {};
    }
};

function editTurno(tur) {
    turno.value = { ...tur };
    turnoDialog.value = true;
}

function confirmDeleteTurno(med) {
    turno.value = med;
    deleteTurnoDialog.value = true;
}

function deleteTurno() {
    turnos.value = turnos.value.filter((val) => val.idTurno !== turno.value.idTurno);
    deleteTurnoDialog.value = false;
    turno.value = {};
    toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turno eliminado', life: 3000 });
}

function exportCSV() {
    dt.value.exportCSV();
}

function confirmDeleteSelected() {
    deleteTurnosDialog.value = true;
}

function deleteSelectedTurnos() {
    turnos.value = turnos.value.filter((val) => !selectedTurnos.value.includes(val));
    deleteTurnosDialog.value = false;
    selectedTurnos.value = null;
    toast.add({ severity: 'success', summary: 'Éxito', detail: 'Turnos eliminados', life: 3000 });
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
                    <Button label="Cancelar Médicos" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedTurnos || !selectedTurnos.length" />
                </template>

                <template #end>
                    <Button label="Exportar" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedTurnos"
                :value="turnos"
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
                        <h4 class="m-0">Turnos</h4>
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
                <Column field="medico.apellido" header="Apellido" sortable style="min-width: 12rem"></Column>
                <Column field="medico.nombre" header="Nombre" sortable style="min-width: 12rem"></Column>
                <Column field="medico.email" header="Email" sortable style="min-width: 12rem"></Column>
                <Column field="medico.telefono" header="Teléfono" sortable style="min-width: 12rem"></Column>
                <Column field="medico.especialidad" header="Especialidad" sortable style="min-width: 12rem">
                    <template #body="slotProps">
                        <Tag :value="slotProps.data.medico.especialidad" :severity="getStatusLabel(slotProps.data.medico.especialidad)" />
                    </template>
                </Column>
                <!--para el color de los botones-->

                <Column :exportable="false" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Button label="Atender" icon="pi pi-plus" severity="primary" class="mr-2" @click="editTurno(slotProps.data)" />
                        <Button Label="Cancelar" icon="pi pi-trash" outlined severity="danger" class="mr-2"  @click="confirmDeleteTurno(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </div>
<!--modificar nevo turno-->
        <!-- <Dialog v-model:visible="turnoDialog" :style="{ width: '450px' }" header="Solicitar turno" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="nombre" class="block font-bold mb-3">Seleccionar fecha</label>
                    <DatePicker :showIcon="true" :showButtonBar="true" v-model="calendarValue"></DatePicker>
                </div>
                 <div>
                    <label for="especialidad" class="block font-bold mb-3">Especialidad</label>
                    <Select
                        id="especialidad"
                        v-model="turno.especialidad"
                        :options="especialidades"
                        optionLabel="label"
                        optionValue="value"
                        placeholder="Seleccionar especialidad"
                        autofocus
                        :invalid="submitted && !turno.especialidad"
                        fluid
                    ></Select>
                </div>
                <small v-if="submitted && !turno.especialidad" class="text-red-500">Especialidad es requerido.</small>
            </div>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Save" icon="pi pi-check" @click="saveTurno" />
            </template>
        </Dialog> -->


        <Dialog v-model:visible="turnoDialog" :style="{ width: '450px' }" header="Solicitar turno" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                <label for="nombre" class="block font-bold mb-3">Seleccionar fecha</label>
                    <DatePicker :showIcon="true" :showButtonBar="true" v-model="turno.fechaDeTurno"></DatePicker>
                </div>
                
                <div>
                <label for="especialidad" class="block font-bold mb-3">Especialidad</label>
                <Select
                    id="especialidad"
                    v-model="turno.especialidad"
                    :options="especialidades"
                    optionLabel="label"
                    optionValue="value"
                    placeholder="Seleccionar especialidad"
                    autofocus
                    :invalid="submitted && !turno.especialidad"
                    fluid
                    @change="onEspecialidadChange"
                ></Select>
                <small v-if="submitted && !turno.especialidad" class="text-red-500">Especialidad es requerido.</small>
                </div>
                
                <!-- Nuevo Select para Médicos -->
                <div v-if="turno.especialidad && medicos.length > 0">
                <label for="medico" class="block font-bold mb-3">Médico</label>
                <Select
                    id="medico"
                    v-model="turno.medico"
                    :options="medicos"
                    optionLabel="nombreCompleto"
                    optionValue="idMedico"
                    placeholder="Seleccionar médico"
                    :invalid="submitted && !turno.medico"
                    fluid
                ></Select>
                <small v-if="submitted && !turno.medico" class="text-red-500">Médico es requerido.</small>
                </div>
                
                <!-- Loading indicator para médicos -->
                <div v-if="turno.especialidad && loadingMedicos" class="flex justify-center">
                <ProgressSpinner style="width: 30px; height: 30px" strokeWidth="4" />
                <span class="ml-2">Cargando médicos...</span>
                </div>
            </div>
            
            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Save" icon="pi pi-check" @click="saveTurno" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteTurnoDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product"
                    >Are you sure you want to delete <b>{{ turno.name }}</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteTurnoDialog = false" />
                <Button label="Yes" icon="pi pi-check" @click="deleteProduct" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteTurnosDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="product">Are you sure you want to delete the selected products?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteTurnosDialog = false" />
                <Button label="Yes" icon="pi pi-check" text @click="deleteSelectedProducts" />
            </template>
        </Dialog>
    </div>
</template>
