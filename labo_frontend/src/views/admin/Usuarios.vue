<script setup>
import UsuarioService from '@/service/UsuarioService';
import { direccionValida, emailValido, limpiarPersona, nombreValido, soloLetras, soloNumeros, telefonoValido, textoBasico } from '@/utils/personaValidacion';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';

const usuarioService = new UsuarioService();
const toast = useToast();
const usuarios = ref([]);
const globalFilter = ref('');
const adminDialog = ref(false);
const submitted = ref(false);
const administrador = ref({});

const roles = [
    { value: 'admin', label: 'Administradores', usuarioLabel: 'Administrador' },
    { value: 'medico', label: 'Medicos', usuarioLabel: 'Medico' },
    { value: 'paciente', label: 'Pacientes', usuarioLabel: 'Paciente' }
];

const cargarUsuarios = async () => {
    const data = await usuarioService.findAll();
    usuarios.value = data.map((usuario) => ({
        ...usuario,
        uid: `${usuario.rol}-${usuario.id}`
    }));
};

const formatRolUsuario = (rol) => roles.find((item) => item.value === rol)?.usuarioLabel || rol?.toUpperCase() || '';

const normalizarBusqueda = (valor) =>
    valor
        ?.toString()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase() || '';

const usuariosFiltrados = computed(() => {
    const filtro = normalizarBusqueda(globalFilter.value?.trim());
    if (!filtro) return usuarios.value;

    return usuarios.value.filter((usuario) => {
        return [usuario.nombre, usuario.apellido, usuario.dni, usuario.email, usuario.rol].filter(Boolean).some((valor) => normalizarBusqueda(valor).includes(filtro)) || normalizarBusqueda(formatRolUsuario(usuario.rol)).includes(filtro);
    });
});

const usuariosPorRol = computed(() =>
    roles.map((rol) => ({
        ...rol,
        usuarios: usuariosFiltrados.value.filter((usuario) => usuario.rol === rol.value)
    }))
);

const esAdminPrincipal = (usuario) => usuario?.rol === 'admin' && usuario?.principal;

const abrirNuevoAdmin = () => {
    administrador.value = { rol: 'admin', estado: 'ACTIVO', password: '' };
    submitted.value = false;
    adminDialog.value = true;
};

const editarAdmin = (usuario) => {
    if (esAdminPrincipal(usuario)) return;
    administrador.value = { ...usuario, password: '' };
    submitted.value = false;
    adminDialog.value = true;
};

const cerrarAdminDialog = () => {
    adminDialog.value = false;
    submitted.value = false;
};

const adminValido = () =>
    nombreValido(administrador.value.nombre) &&
    nombreValido(administrador.value.apellido) &&
    emailValido(administrador.value.email) &&
    telefonoValido(administrador.value.telefono) &&
    direccionValida(administrador.value.direccion) &&
    (administrador.value.id || administrador.value.password);

const guardarAdmin = async () => {
    submitted.value = true;
    administrador.value = limpiarPersona(administrador.value);

    if (!adminValido()) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Complete correctamente los datos del administrador', life: 3000 });
        return;
    }

    try {
        if (administrador.value.id) {
            await usuarioService.update('admin', administrador.value.id, administrador.value);
            toast.add({ severity: 'success', summary: 'Exito', detail: 'Administrador actualizado', life: 3000 });
        } else {
            await usuarioService.createAdmin(administrador.value);
            toast.add({ severity: 'success', summary: 'Exito', detail: 'Administrador creado', life: 3000 });
        }

        adminDialog.value = false;
        await cargarUsuarios();
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error.message || 'No se pudo guardar el administrador', life: 3000 });
    }
};

const toggleEstado = async (usuario) => {
    if (esAdminPrincipal(usuario)) return;
    const nuevoEstado = usuario.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    const actualizado = await usuarioService.update(usuario.rol, usuario.id, { ...usuario, estado: nuevoEstado });

    usuarios.value = usuarios.value.map((item) => (item.uid === usuario.uid ? { ...item, estado: actualizado.estado } : item));
};

const eliminarUsuario = async (usuario) => {
    if (esAdminPrincipal(usuario)) return;
    if (!window.confirm(`Eliminar a ${usuario.nombre} ${usuario.apellido}?`)) return;

    await usuarioService.remove(usuario.rol, usuario.id);
    usuarios.value = usuarios.value.filter((item) => item.uid !== usuario.uid);
};

onMounted(cargarUsuarios);
</script>

<template>
    <div class="card">
        <div class="flex flex-column md:flex-row md:align-items-center gap-4 mb-4">
            <div>
                <h4 class="mt-0">Usuarios y permisos</h4>
                <p class="text-gray-500 mt-2 mb-0">Visualiza usuarios por rol y administra perfiles de administrador.</p>
            </div>
            <div class="flex flex-wrap gap-2 justify-content-end w-full md:w-1/2 md:ml-auto">
                <Button label="Nuevo administrador" icon="pi pi-plus" severity="secondary" @click="abrirNuevoAdmin" />
                <IconField class="w-full">
                    <InputText v-model="globalFilter" placeholder="Buscar por nombre, apellido, email o DNI" fluid />
                </IconField>
            </div>
        </div>

        <section v-for="grupo in usuariosPorRol" :key="grupo.value" class="mb-5">
            <div class="flex align-items-center gap-2 mb-3">
                <h5 class="m-0">{{ grupo.label }}</h5>
                <Tag :value="`${grupo.usuarios.length} usuarios`" severity="secondary" />
            </div>

            <DataTable :value="grupo.usuarios" dataKey="uid" responsiveLayout="scroll" tableStyle="min-width: 960px; table-layout: fixed;">
                <template #empty> No hay usuarios con rol {{ grupo.usuarioLabel }}. </template>

                <Column field="nombre" header="Nombre" sortable style="width: 150px" />
                <Column field="apellido" header="Apellido" sortable style="width: 150px" />
                <Column field="email" header="Email" sortable style="width: 240px" />
                <Column field="rol" header="Rol" sortable style="width: 150px">
                    <template #body="slotProps">
                        <Tag :value="formatRolUsuario(slotProps.data.rol)" severity="info" />
                    </template>
                </Column>
                <Column field="dni" header="DNI" sortable style="width: 130px" />
                <Column field="estado" header="Estado" style="width: 130px">
                    <template #body="slotProps">
                        <Tag :value="slotProps.data.estado?.toUpperCase()" :severity="slotProps.data.estado === 'INACTIVO' ? 'danger' : 'success'" />
                    </template>
                </Column>
                <Column header="Acciones" style="width: 250px">
                    <template #body="slotProps">
                        <Button
                            icon="pi pi-refresh"
                            class="p-button-sm"
                            text
                            :severity="slotProps.data.estado === 'ACTIVO' ? 'danger' : 'success'"
                            :label="slotProps.data.estado === 'ACTIVO' ? 'Inactivar' : 'Activar'"
                            @click="toggleEstado(slotProps.data)"
                            :disabled="esAdminPrincipal(slotProps.data)"
                        />
                        <Button v-if="slotProps.data.rol === 'admin'" icon="pi pi-pencil" class="p-button-text p-button-sm" @click="editarAdmin(slotProps.data)" :disabled="esAdminPrincipal(slotProps.data)" />
                        <Button icon="pi pi-trash" class="p-button-text p-button-sm" severity="danger" @click="eliminarUsuario(slotProps.data)" :disabled="esAdminPrincipal(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </section>

        <Dialog v-model:visible="adminDialog" :style="{ width: '520px' }" header="Administrador" :modal="true">
            <div class="flex flex-col gap-4">
                <div>
                    <label class="block font-bold mb-2">Nombre</label>
                    <InputText v-model.trim="administrador.nombre" :invalid="submitted && !nombreValido(administrador.nombre)" fluid @input="administrador.nombre = soloLetras(administrador.nombre)" />
                    <small v-if="submitted && !nombreValido(administrador.nombre)" class="text-red-500">Solo letras y espacios.</small>
                </div>
                <div>
                    <label class="block font-bold mb-2">Apellido</label>
                    <InputText v-model.trim="administrador.apellido" :invalid="submitted && !nombreValido(administrador.apellido)" fluid @input="administrador.apellido = soloLetras(administrador.apellido)" />
                    <small v-if="submitted && !nombreValido(administrador.apellido)" class="text-red-500">Solo letras y espacios.</small>
                </div>
                <div>
                    <label class="block font-bold mb-2">Email</label>
                    <InputText v-model.trim="administrador.email" :invalid="submitted && !emailValido(administrador.email)" fluid />
                    <small v-if="submitted && !emailValido(administrador.email)" class="text-red-500">Email valido requerido.</small>
                </div>
                <div>
                    <label class="block font-bold mb-2">Telefono</label>
                    <InputText v-model.trim="administrador.telefono" :invalid="submitted && !telefonoValido(administrador.telefono)" fluid @input="administrador.telefono = soloNumeros(administrador.telefono)" />
                    <small v-if="submitted && !telefonoValido(administrador.telefono)" class="text-red-500">Solo numeros, entre 6 y 15 digitos.</small>
                </div>
                <div>
                    <label class="block font-bold mb-2">Direccion</label>
                    <InputText v-model.trim="administrador.direccion" :invalid="submitted && !direccionValida(administrador.direccion)" fluid @input="administrador.direccion = textoBasico(administrador.direccion)" />
                    <small v-if="submitted && !direccionValida(administrador.direccion)" class="text-red-500">Direccion requerida, sin caracteres especiales.</small>
                </div>
                <div>
                    <label class="block font-bold mb-2">Contraseña</label>
                    <InputText v-model.trim="administrador.password" type="password" :invalid="submitted && !administrador.id && !administrador.password" fluid />
                    <small class="text-gray-500">En edicion, dejar vacio para conservar la contraseña actual.</small>
                    <small v-if="submitted && !administrador.id && !administrador.password" class="text-red-500 block">Contraseña requerida.</small>
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="cerrarAdminDialog" />
                <Button label="Guardar" icon="pi pi-check" @click="guardarAdmin" />
            </template>
        </Dialog>
    </div>
</template>
