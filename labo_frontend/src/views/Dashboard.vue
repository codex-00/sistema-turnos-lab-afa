<script setup>
import PacientesWidget from '@/components/dashboard/PacientesWidget.vue';
import NotificationsWidget from '@/components/dashboard/NotificationsWidget.vue';
import TurnosWidget from '@/components/dashboard/TurnosWidget.vue';
import RevenueStreamWidget from '@/components/dashboard/RevenueStreamWidget.vue';
import StatsWidget from '@/components/dashboard/Interfaz_laboratorio.vue';
import Interfaz_laboratorio from '@/components/dashboard/Interfaz_laboratorio.vue';
</script>

<template>
    <div class="grid grid-cols-12 gap-8">
        <Interfaz_laboratorio />

        <div class="col-span-12 xl:col-span-6">
            <TurnosWidget />
            <PacientesWidget />
        </div>
        <div class="col-span-12 xl:col-span-6">
            <RevenueStreamWidget />
            <NotificationsWidget />
        </div>
    </div>
</template>
