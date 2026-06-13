<script setup>
import { ref } from 'vue';
import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: 'Administrador',
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-chart-bar', to: '/admin/dashboard' },
            { label: 'Médicos', icon: 'pi pi-fw pi-user-plus', to: '/admin/medicos' },
            { label: 'Pacientes', icon: 'pi pi-fw pi-users', to: '/admin/pacientes' },
            { label: 'Turnos', icon: 'pi pi-fw pi-calendar', to: '/admin/turnos' },
            { label: 'Usuarios y permisos', icon: 'pi pi-fw pi-lock', to: '/admin/usuarios' }
        ]
    },
    {
        label: 'Médico',
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-chart-line', to: '/medico/dashboard' },
            { label: 'Turnos recibidos', icon: 'pi pi-fw pi-calendar-clock', to: '/medico/turnos' },
            { label: 'Disponibilidad', icon: 'pi pi-fw pi-clock', to: '/medico/disponibilidad' },
            { label: 'Agenda', icon: 'pi pi-fw pi-calendar', to: '/medico/agenda' },
            { label: 'Pacientes asignados', icon: 'pi pi-fw pi-users', to: '/medico/pacientes' },
            { label: 'Estudios', icon: 'pi pi-fw pi-file', to: '/medico/estudios' }
        ]
    },
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
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item.label">
            <app-menu-item v-if="!item.separator" :item="item" :index="i" />
            <li v-else class="menu-separator"></li>
        </template>
    </ul>
</template>
