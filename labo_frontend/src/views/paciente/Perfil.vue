<script setup>
import PacienteService from '@/service/PacienteService';
import { useAuthStore } from '@/stores/auth';
import { direccionValida, emailValido, limpiarPersona, nombreValido, soloLetras, soloNumeros, telefonoValido, textoBasico } from '@/utils/personaValidacion';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const pacienteService = new PacienteService();
const authStore = useAuthStore();
const toast = useToast();
const pacienteId = computed(() => (authStore.rol === 'paciente' && authStore.token ? authStore.token : 1));
const paciente = ref({});
const submitted = ref(false);

onMounted(async () => {
    const pacientes = await pacienteService.findAll();
    paciente.value = pacientes.find((item) => item.idPaciente === pacienteId.value) || {};
});

const guardarPerfil = async () => {
    submitted.value = true;
    paciente.value = limpiarPersona(paciente.value);

    if (!nombreValido(paciente.value.nombre) || !nombreValido(paciente.value.apellido) || !emailValido(paciente.value.email) || !telefonoValido(paciente.value.telefono) || !direccionValida(paciente.value.direccion)) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Revise los datos personales ingresados', life: 3000 });
        return;
    }

    try {
        await pacienteService.update(paciente.value.idPaciente, paciente.value);
        toast.add({ severity: 'success', summary: 'Exito', detail: 'Perfil actualizado', life: 3000 });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo actualizar el perfil', life: 3000 });
    }
};
</script>

<template>
    <div class="card">
        <h4 class="mt-0">Mi perfil</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
                <label class="block font-bold mb-2">Nombre</label>
                <InputText v-model.trim="paciente.nombre" :invalid="submitted && !nombreValido(paciente.nombre)" fluid @input="paciente.nombre = soloLetras(paciente.nombre)" />
                <small v-if="submitted && !nombreValido(paciente.nombre)" class="text-red-500">Solo letras y espacios.</small>
            </div>
            <div>
                <label class="block font-bold mb-2">Apellido</label>
                <InputText v-model.trim="paciente.apellido" :invalid="submitted && !nombreValido(paciente.apellido)" fluid @input="paciente.apellido = soloLetras(paciente.apellido)" />
                <small v-if="submitted && !nombreValido(paciente.apellido)" class="text-red-500">Solo letras y espacios.</small>
            </div>
            <div>
                <label class="block font-bold mb-2">Email</label>
                <InputText v-model.trim="paciente.email" :invalid="submitted && !emailValido(paciente.email)" fluid />
                <small v-if="submitted && !emailValido(paciente.email)" class="text-red-500">Email valido requerido.</small>
            </div>
            <div>
                <label class="block font-bold mb-2">Telefono</label>
                <InputText v-model.trim="paciente.telefono" :invalid="submitted && !telefonoValido(paciente.telefono)" fluid @input="paciente.telefono = soloNumeros(paciente.telefono)" />
                <small v-if="submitted && !telefonoValido(paciente.telefono)" class="text-red-500">Solo numeros, entre 6 y 15 digitos.</small>
            </div>
            <div class="md:col-span-2">
                <label class="block font-bold mb-2">Direccion</label>
                <InputText v-model.trim="paciente.direccion" :invalid="submitted && !direccionValida(paciente.direccion)" fluid @input="paciente.direccion = textoBasico(paciente.direccion)" />
                <small v-if="submitted && !direccionValida(paciente.direccion)" class="text-red-500">Direccion requerida, sin caracteres especiales.</small>
            </div>
        </div>
        <Button label="Guardar cambios" icon="pi pi-save" class="mt-5" @click="guardarPerfil" />
    </div>
</template>
