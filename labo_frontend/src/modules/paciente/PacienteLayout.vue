<script setup>
import RoleLayout from '@/modules/shared/RoleLayout.vue';

const menu = [
    {
        label: 'Paciente',
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/paciente/dashboard' },
            { label: 'Mis turnos', icon: 'pi pi-fw pi-calendar-plus', to: '/paciente/turnos' },
            { label: 'Mis estudios', icon: 'pi pi-fw pi-file', to: '/paciente/estudios' },
            { label: 'Historial médico', icon: 'pi pi-fw pi-history', to: '/paciente/historial' },
            { label: 'Mi perfil', icon: 'pi pi-fw pi-user', to: '/paciente/perfil' }
        ]
    }
];
</script>

<template>
    <RoleLayout :menu="menu" title="CLÍNICA AFA" roleLabel="Paciente" homePath="/paciente/dashboard" />
</template>
