<script setup>
import RoleLayout from '@/modules/shared/RoleLayout.vue';

const menu = [
    {
        label: 'Administrador',
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-chart-bar', to: '/admin/dashboard' },
            { label: 'Médicos', icon: 'pi pi-fw pi-user-plus', to: '/admin/medicos' },
            { label: 'Pacientes', icon: 'pi pi-fw pi-users', to: '/admin/pacientes' },
            { label: 'Turnos', icon: 'pi pi-fw pi-calendar', to: '/admin/turnos' },
            { label: 'Usuarios y permisos', icon: 'pi pi-fw pi-lock', to: '/admin/usuarios' }
        ]
    }
];
</script>

<template>
    <RoleLayout :menu="menu" title="CLÍNICA AFA" roleLabel="Administrador" homePath="/admin/dashboard" />
</template>
