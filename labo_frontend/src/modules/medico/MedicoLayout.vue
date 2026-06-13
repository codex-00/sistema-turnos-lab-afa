<script setup>
import RoleLayout from '@/modules/shared/RoleLayout.vue';

const menu = [
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
    }
];
</script>

<template>
    <RoleLayout :menu="menu" title="CLÍNICA AFA" roleLabel="Médico" homePath="/medico/dashboard" />
</template>
