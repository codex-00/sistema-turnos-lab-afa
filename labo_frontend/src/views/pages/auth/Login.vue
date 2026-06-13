<script setup>
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue/usetoast';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const email = ref('');
const password = ref('');
const rol = ref('paciente');
const checked = ref(false);
const roles = ref([
    { label: 'Administrador', value: 'admin' },
    { label: 'Paciente', value: 'paciente' },
    { label: 'Médico', value: 'medico' }
]);

const authStore = useAuthStore();
const router = useRouter();
const toast = useToast();
const loginError = ref('');
const loading = ref(false);
const recoveryDialog = ref(false);
const recoveryEmail = ref('');
const recoveryRol = ref('paciente');
const recoveryLoading = ref(false);
const recoverySubmitted = ref(false);
const recoveryResult = ref('');
const recoveryDevToken = ref('');
const recoveryMailEnviado = ref(false);
const resetToken = ref('');
const newPassword = ref('');
const resetLoading = ref(false);
const resetSubmitted = ref(false);

const login = async () => {
    loginError.value = '';

    if (!email.value || !password.value || !rol.value) {
        loginError.value = 'Complete email, contraseña y rol.';
        return;
    }

    loading.value = true;

    try {
        const result = await authStore.login({
            email: email.value,
            password: password.value,
            rol: rol.value
        });

        if (!result.ok) {
            const message = result.type === 'invalid_credentials' ? 'Usuario o contraseña incorrectos' : 'No se pudo iniciar sesión. Intente nuevamente en unos minutos.';

            loginError.value = message;
            toast.add({ severity: 'error', summary: 'Error', detail: message, life: 4000 });
            return;
        }

        const homeByRole = {
            admin: '/admin/dashboard',
            medico: '/medico/dashboard',
            paciente: '/paciente/dashboard'
        };

        router.push(homeByRole[authStore.rol]);
    } finally {
        loading.value = false;
    }
};

const openRecoveryDialog = () => {
    recoveryEmail.value = email.value;
    recoveryRol.value = rol.value;
    recoverySubmitted.value = false;
    recoveryResult.value = '';
    recoveryDevToken.value = '';
    recoveryMailEnviado.value = false;
    resetToken.value = '';
    newPassword.value = '';
    resetSubmitted.value = false;
    recoveryDialog.value = true;
};

const recoverPassword = async () => {
    recoverySubmitted.value = true;
    recoveryResult.value = '';
    recoveryDevToken.value = '';
    recoveryMailEnviado.value = false;

    if (!recoveryEmail.value || !recoveryRol.value) {
        return;
    }

    recoveryLoading.value = true;

    try {
        const result = await authStore.recoverPassword({
            email: recoveryEmail.value,
            rol: recoveryRol.value
        });

        if (!result.ok) {
            toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo solicitar la recuperación', life: 4000 });
            return;
        }

        recoveryResult.value = result.data.message;
        recoveryMailEnviado.value = Boolean(result.data.mailEnviado);
        recoveryDevToken.value = result.data.devToken || '';
        toast.add({ severity: 'success', summary: 'Solicitud enviada', detail: result.data.message, life: 5000 });
    } finally {
        recoveryLoading.value = false;
    }
};

const resetPassword = async () => {
    resetSubmitted.value = true;

    if (!recoveryEmail.value || !recoveryRol.value || !resetToken.value || !newPassword.value) {
        return;
    }

    resetLoading.value = true;

    try {
        const result = await authStore.resetPassword({
            email: recoveryEmail.value,
            rol: recoveryRol.value,
            token: resetToken.value,
            nuevaPassword: newPassword.value
        });

        if (!result.ok) {
            toast.add({ severity: 'error', summary: 'Error', detail: 'Código inválido o vencido', life: 4000 });
            return;
        }

        password.value = newPassword.value;
        recoveryDialog.value = false;
        toast.add({ severity: 'success', summary: 'Contraseña actualizada', detail: result.data.message, life: 4000 });
    } finally {
        resetLoading.value = false;
    }
};
</script>

<template>
    <Toast />
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
        <div class="w-full max-w-[34rem] bg-surface-0 dark:bg-surface-900 py-12 px-8 sm:px-12 shadow-lg" style="border-radius: 18px">
            <div class="text-center mb-8">
                <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary text-primary-contrast mb-5">
                    <i class="pi pi-calendar-plus text-3xl"></i>
                </div>
                <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-3">Sistema de turnos AFA</div>
                <span class="text-muted-color font-medium">Iniciar sesión para continuar</span>
            </div>

            <form @submit.prevent="login">
                <Message v-if="loginError" severity="error" :closable="false" class="mb-5">{{ loginError }}</Message>

                <label for="rol" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Rol</label>
                <Select id="rol" v-model="rol" :options="roles" optionLabel="label" optionValue="value" class="w-full mb-5" />

                <label for="email1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">Email</label>
                <InputText id="email1" v-model.trim="email" type="email" placeholder="Email" class="w-full mb-5" autocomplete="username" />

                <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Contraseña</label>
                <Password id="password1" v-model="password" placeholder="Contraseña" :toggleMask="true" class="mb-4" fluid :feedback="false" autocomplete="current-password" />

                <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                    <div class="flex items-center">
                        <Checkbox v-model="checked" id="rememberme1" binary class="mr-2" />
                        <label for="rememberme1">Recordarme</label>
                    </div>
                    <button type="button" class="font-medium no-underline ml-2 text-right text-primary bg-transparent border-0 p-0 cursor-pointer" @click="openRecoveryDialog">Recuperar contraseña</button>
                </div>

                <Button label="Iniciar sesión" class="w-full" type="submit" :loading="loading" />
            </form>
        </div>
    </div>

    <Dialog v-model:visible="recoveryDialog" :style="{ width: '460px' }" header="Recuperar contraseña" :modal="true">
        <div class="flex flex-col gap-5">
            <Message v-if="recoveryResult" severity="info" :closable="false">{{ recoveryResult }}</Message>
            <Message v-if="recoveryDevToken && !recoveryMailEnviado" severity="warn" :closable="false"> Token local: {{ recoveryDevToken }} </Message>

            <div>
                <label for="recoveryRol" class="block text-surface-900 dark:text-surface-0 font-medium mb-2">Rol</label>
                <Select id="recoveryRol" v-model="recoveryRol" :options="roles" optionLabel="label" optionValue="value" class="w-full" />
            </div>

            <div>
                <label for="recoveryEmail" class="block text-surface-900 dark:text-surface-0 font-medium mb-2">Email</label>
                <InputText id="recoveryEmail" v-model.trim="recoveryEmail" type="email" placeholder="Email" fluid :invalid="recoverySubmitted && !recoveryEmail" />
                <small v-if="recoverySubmitted && !recoveryEmail" class="text-red-500">Email requerido.</small>
            </div>

            <Divider />

            <div>
                <label for="resetToken" class="block text-surface-900 dark:text-surface-0 font-medium mb-2">Código recibido</label>
                <InputText id="resetToken" v-model.trim="resetToken" placeholder="Código de recuperación" fluid :invalid="resetSubmitted && !resetToken" />
                <small v-if="resetSubmitted && !resetToken" class="text-red-500">Código requerido.</small>
            </div>

            <div>
                <label for="newPassword" class="block text-surface-900 dark:text-surface-0 font-medium mb-2">Nueva contraseña</label>
                <Password id="newPassword" v-model="newPassword" placeholder="Nueva contraseña" :toggleMask="true" fluid :feedback="false" :invalid="resetSubmitted && !newPassword" autocomplete="new-password" />
                <small v-if="resetSubmitted && !newPassword" class="text-red-500">Nueva contraseña requerida.</small>
            </div>
        </div>

        <template #footer>
            <Button label="Cancelar" icon="pi pi-times" text @click="recoveryDialog = false" />
            <Button label="Enviar" icon="pi pi-send" :loading="recoveryLoading" @click="recoverPassword" />
            <Button label="Cambiar" icon="pi pi-check" severity="success" :loading="resetLoading" @click="resetPassword" />
        </template>
    </Dialog>
</template>
