<script setup>
import EstudioService from '@/service/EstudioService';
import TurnoService from '@/service/TurnoService';
import { useAuthStore } from '@/stores/auth';
import { getTurnoEstadoSeverity, getTurnoEstadoTagClass, resolveTurnoEstado } from '@/utils/turnoEstado';
import { computed, onMounted, ref } from 'vue';

const turnoService = new TurnoService();
const estudioService = new EstudioService();
const authStore = useAuthStore();
const pacienteId = computed(() => (authStore.rol === 'paciente' && authStore.token ? authStore.token : 1));
const eventos = ref([]);

onMounted(async () => {
    const [turnos, estudios] = await Promise.all([turnoService.findAllByPaciente({ idPaciente: pacienteId.value }), estudioService.findAllByPaciente({ idPaciente: pacienteId.value })]);

    eventos.value = [
        ...turnos.map((turno) => ({
            tipo: 'Turno',
            fecha: turno.fechaDeTurno,
            detalle: `${turno.medico?.apellido || ''} - ${turno.medico?.especialidad || ''}`,
            estado: resolveTurnoEstado(turno)
        })),
        ...estudios.map((estudio) => ({
            tipo: 'Estudio',
            fecha: estudio.fechaCreacion,
            detalle: estudio.nombre,
            estado: 'CARGADO'
        }))
    ].sort((a, b) => new Date(b.fecha || 0) - new Date(a.fecha || 0));
});
</script>

<template>
    <div class="card">
        <h4 class="mt-0">Historial médico</h4>
        <DataTable :value="eventos" :paginator="true" :rows="10">
            <Column field="tipo" header="Tipo" sortable />
            <Column field="fecha" header="Fecha" sortable />
            <Column field="detalle" header="Detalle" />
            <Column field="estado" header="Estado">
                <template #body="slotProps">
                    <Tag :value="slotProps.data.estado" :severity="slotProps.data.tipo === 'Turno' ? getTurnoEstadoSeverity(slotProps.data.estado) : 'info'" :class="slotProps.data.tipo === 'Turno' ? getTurnoEstadoTagClass(slotProps.data.estado) : ''" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>
