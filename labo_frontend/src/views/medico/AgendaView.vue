<script setup>
import AgendaService from '@/service/AgendaService';
import { useAuthStore } from '@/stores/auth';
import { getTurnoEstadoClass, getTurnoEstadoSeverity, getTurnoEstadoTagClass, resolveTurnoEstado } from '@/utils/turnoEstado';
import { useToast } from 'primevue/usetoast';
import { computed, ref, onMounted, onBeforeUnmount } from 'vue';

const agendaService = new AgendaService();
const toast = useToast();
const authStore = useAuthStore();
const medicoId = computed(() => (authStore.rol === 'medico' && authStore.token ? authStore.token : 1));

const selectedMonth = ref(new Date());
const diaSeleccionado = ref(null);
const agenda = ref([]);
const mesAgenda = ref([]);
const dialogVisible = ref(false);
const descripcionEvento = ref('');
const horaEvento = ref('');
const loadingMes = ref(false);
const loadingDia = ref(false);

const semana = ['Dom', 'Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab'];

const formatFechaApi = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

const formatFechaLocal = (date) => {
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' });
};

const formatMonthLabel = (date) => {
    return date.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' });
};

const buildCalendarDays = (date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const primerDia = new Date(year, month, 1);
    const diaSemanaInicio = primerDia.getDay();
    const diasEnMes = new Date(year, month + 1, 0).getDate();
    const totalCeldas = 42;
    const dias = [];

    for (let i = 0; i < totalCeldas; i++) {
        const dayNumber = i - diaSemanaInicio + 1;
        if (dayNumber < 1 || dayNumber > diasEnMes) {
            dias.push({ key: `blank-${i}`, date: null });
        } else {
            dias.push({ key: `day-${dayNumber}`, date: new Date(year, month, dayNumber) });
        }
    }

    return dias;
};

const normalizeFechaKey = (fecha) => {
    if (!fecha) return '';

    if (typeof fecha === 'string') {
        if (/^\d{4}-\d{2}-\d{2}$/.test(fecha)) {
            return fecha;
        }
        const parsed = new Date(fecha);
        if (!Number.isNaN(parsed.getTime())) {
            return formatFechaApi(parsed);
        }
        return fecha;
    }

    if (typeof fecha === 'object' && fecha.year != null && fecha.monthValue != null && fecha.dayOfMonth != null) {
        return `${fecha.year}-${String(fecha.monthValue).padStart(2, '0')}-${String(fecha.dayOfMonth).padStart(2, '0')}`;
    }

    const parsed = new Date(fecha);
    return !Number.isNaN(parsed.getTime()) ? formatFechaApi(parsed) : String(fecha);
};

const agendaPorFecha = computed(() => {
    const map = new Map();

    mesAgenda.value.forEach((item) => {
        const key = normalizeFechaKey(item.fecha);
        if (!key) return;
        if (!map.has(key)) {
            map.set(key, []);
        }
        map.get(key).push(item);
    });

    return map;
});

const monthDays = computed(() => buildCalendarDays(selectedMonth.value));

const cargarMes = async () => {
    loadingMes.value = true;
    try {
        const data = await agendaService.findMes({
            idMedico: medicoId.value,
            anio: selectedMonth.value.getFullYear(),
            mes: selectedMonth.value.getMonth() + 1
        });

        mesAgenda.value = data;
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar la agenda del mes', life: 3000 });
    } finally {
        loadingMes.value = false;
    }
};

const cargarDia = async (date) => {
    if (!date) return;
    loadingDia.value = true;
    try {
        agenda.value = await agendaService.findDia({
            idMedico: medicoId.value,
            fecha: formatFechaApi(date)
        });
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar los detalles del dia', life: 3000 });
    } finally {
        loadingDia.value = false;
    }
};

const seleccionarDia = (day) => {
    if (!day?.date) return;
    diaSeleccionado.value = day.date;
    cargarDia(day.date);
};

const cambiarMes = (delta) => {
    selectedMonth.value = new Date(selectedMonth.value.getFullYear(), selectedMonth.value.getMonth() + delta, 1);
    diaSeleccionado.value = null;
    agenda.value = [];
    cargarMes();
};

const obtenerResumenDia = (date) => {
    const key = formatFechaApi(date);
    const items = agendaPorFecha.value.get(key) || [];

    if (items.length === 0) {
        return 'No laborable';
    }

    const turnos = items.filter((item) => item.paciente && !item.disponible);
    if (turnos.length > 0) {
        const resumen = turnos
            .sort((a, b) => (a.hora || '').localeCompare(b.hora || ''))
            .map((turno) => {
                const apellido = turno.paciente?.apellido || turno.paciente?.nombre || '';
                const estadoRaw = resolveAgendaTurnoEstado(turno);
                const estado = `${estadoRaw.charAt(0).toUpperCase()}${estadoRaw.slice(1).toLowerCase()}`;
                return `Turno ${apellido} ${estado}`;
            });
        return resumen.join(' - ');
    }

    const bloqueos = items.filter((item) => !item.disponible && !item.paciente);
    if (bloqueos.length > 0) {
        return `Bloqueado ${bloqueos[0].descripcion || ''}`.trim();
    }

    return 'Disponible';
};

const obtenerTurnosDia = (date) => {
    const key = formatFechaApi(date);
    const items = agendaPorFecha.value.get(key) || [];
    return items.filter((item) => item.paciente && !item.disponible).sort((a, b) => (a.hora || '').localeCompare(b.hora || ''));
};

const resolveAgendaTurnoEstado = (item) => {
    if (!item?.paciente) {
        return item?.estado || null;
    }

    const fecha = normalizeFechaKey(item.fecha);
    const hora = typeof item.hora === 'string' ? item.hora.slice(0, 5) : '';
    return resolveTurnoEstado({
        estado: item.estado,
        fechaDeTurno: fecha && hora ? `${fecha}T${hora}` : null
    });
};

const getAgendaEstadoSeverity = (data) => {
    if (data.disponible) {
        return 'success';
    }
    const estado = resolveAgendaTurnoEstado(data);
    if (estado) {
        return getTurnoEstadoSeverity(estado);
    }
    return data.paciente ? 'success' : 'danger';
};

const obtenerEstadoDia = (date) => {
    const key = formatFechaApi(date);
    const items = agendaPorFecha.value.get(key) || [];

    if (items.length === 0) {
        return 'no-laborable';
    }

    const tieneDisponible = items.some((item) => item.disponible === true);
    const tieneOcupado = items.some((item) => item.disponible === false);

    if (tieneOcupado && !tieneDisponible) {
        return 'lleno';
    }
    if (tieneOcupado && tieneDisponible) {
        return 'parcial';
    }
    if (tieneDisponible) {
        return 'disponible';
    }
    return 'no-laborable';
};

const esDiaSeleccionado = (date) => diaSeleccionado.value && date && formatFechaApi(date) === formatFechaApi(diaSeleccionado.value);

const onWindowFocus = () => cargarMes();

onMounted(() => window.addEventListener('focus', onWindowFocus));
onBeforeUnmount(() => window.removeEventListener('focus', onWindowFocus));

const abrirDialogo = () => {
    if (!diaSeleccionado.value) {
        toast.add({ severity: 'warn', summary: 'Seleccione un dia', detail: 'Debe seleccionar un dia del calendario para agregar un evento', life: 3000 });
        return;
    }
    descripcionEvento.value = '';
    horaEvento.value = '';
    dialogVisible.value = true;
};

const guardarBloqueo = async () => {
    if (!diaSeleccionado.value || !horaEvento.value) {
        toast.add({ severity: 'warn', summary: 'Datos incompletos', detail: 'Debe ingresar la hora del bloqueo', life: 3000 });
        return;
    }

    try {
        await agendaService.bloquear({
            medicoId: medicoId.value,
            fecha: formatFechaApi(diaSeleccionado.value),
            hora: horaEvento.value,
            descripcion: descripcionEvento.value
        });

        toast.add({ severity: 'success', summary: 'Evento agregado', detail: 'El bloqueo se guardo en la agenda', life: 3000 });
        dialogVisible.value = false;
        descripcionEvento.value = '';
        horaEvento.value = '';
        cargarMes();
        cargarDia(diaSeleccionado.value);
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: error?.message || 'No se pudo guardar el evento', life: 3000 });
    }
};

cargarMes();
</script>

<template>
    <div class="card">
        <div class="flex flex-wrap gap-3 items-center justify-between mb-5">
            <div>
                <h2 class="m-0">Agenda medica</h2>
                <p class="text-sm text-gray-600 m-0">Calendario mensual del medico, mostrando dias ocupados y permitiendo agregar bloqueos.</p>
            </div>
            <div class="flex items-center gap-2">
                <Button icon="pi pi-angle-left" class="p-button-text" @click="cambiarMes(-1)" />
                <span class="text-lg font-semibold">{{ formatMonthLabel(selectedMonth) }}</span>
                <Button icon="pi pi-angle-right" class="p-button-text" @click="cambiarMes(1)" />
                <Button label="Actualizar" icon="pi pi-refresh" class="p-button-outlined" @click="cargarMes" />
            </div>
        </div>

        <div class="grid grid-cols-7 gap-2 text-center font-semibold mb-2">
            <div v-for="nombre in semana" :key="nombre" class="py-2 border rounded-lg bg-slate-50">{{ nombre }}</div>
        </div>

        <div class="grid grid-cols-7 gap-2">
            <div
                v-for="day in monthDays"
                :key="day.key"
                class="min-h-[100px] p-3 border rounded-lg cursor-pointer transition hover:shadow-sm"
                :class="{
                    'bg-red-50 border-red-300': day.date && obtenerEstadoDia(day.date) === 'lleno',
                    'bg-sky-50 border-sky-300': day.date && obtenerEstadoDia(day.date) === 'parcial',
                    'bg-green-50 border-green-300': day.date && obtenerEstadoDia(day.date) === 'disponible',
                    'bg-gray-100 border-gray-300 text-gray-500': !day.date || (day.date && obtenerEstadoDia(day.date) === 'no-laborable'),
                    'ring-2 ring-blue-400 ring-offset-1': esDiaSeleccionado(day.date)
                }"
                @click="seleccionarDia(day)"
            >
                <div class="flex items-center justify-between mb-2">
                    <span class="font-semibold">{{ day.date ? day.date.getDate() : '' }}</span>
                </div>
                <div class="text-xs text-gray-600 min-h-[42px]">
                    <div v-if="day.date">{{ obtenerResumenDia(day.date) }}</div>
                </div>
                <div v-if="day.date" class="flex flex-wrap gap-1 mt-2">
                    <span v-for="turnoDia in obtenerTurnosDia(day.date).slice(0, 2)" :key="turnoDia.id" class="text-[10px] leading-none px-2 py-1 rounded-full border" :class="getTurnoEstadoClass(resolveAgendaTurnoEstado(turnoDia))">
                        {{ resolveAgendaTurnoEstado(turnoDia) }}
                    </span>
                </div>
            </div>
        </div>

        <div class="mt-6">
            <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
                <div>
                    <h3 class="m-0">Detalles del dia</h3>
                    <p class="text-sm text-gray-600 m-0">{{ diaSeleccionado ? formatFechaLocal(diaSeleccionado) : 'Seleccione un dia para ver horarios y ocupaciones.' }}</p>
                </div>
                <Button label="Agregar evento" icon="pi pi-plus" @click="abrirDialogo" :disabled="!diaSeleccionado" />
            </div>

            <div v-if="loadingDia" class="py-5">Cargando detalles...</div>

            <div v-else-if="diaSeleccionado">
                <DataTable :value="agenda" dataKey="id" :paginator="true" :rows="10" emptyMessage="No existen turnos para este dia">
                    <Column field="hora" header="Hora" />
                    <Column header="Estado">
                        <template #body="{ data }">
                            <Tag :value="data.disponible ? 'Disponible' : resolveAgendaTurnoEstado(data) || (data.paciente ? 'Reservado' : 'Bloqueado')" :severity="getAgendaEstadoSeverity(data)" :class="!data.disponible && resolveAgendaTurnoEstado(data) ? getTurnoEstadoTagClass(resolveAgendaTurnoEstado(data)) : ''" />
                        </template>
                    </Column>
                    <Column header="Paciente">
                        <template #body="{ data }">
                            {{ data.paciente ? `${data.paciente.apellido || ''} ${data.paciente.nombre || ''}`.trim() : '-' }}
                        </template>
                    </Column>
                    <Column field="descripcion" header="Descripción" />
                </DataTable>
            </div>
        </div>

        <Dialog header="Agregar evento en la agenda" v-model:visible="dialogVisible" :modal="true" :style="{ width: '560px' }">
            <div class="flex flex-col gap-4">
                <div>
                    <label class="block font-bold mb-2">Día</label>
                    <InputText :modelValue="diaSeleccionado ? formatFechaLocal(diaSeleccionado) : ''" disabled fluid />
                </div>
                <div>
                    <label class="block font-bold mb-2">Hora</label>
                    <input type="time" v-model="horaEvento" class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="block font-bold mb-2">Descripción</label>
                    <Textarea v-model="descripcionEvento" rows="4" autoResize />
                </div>
            </div>
            <template #footer>
                <Button label="Cancelar" icon="pi pi-times" text @click="dialogVisible = false" />
                <Button label="Guardar" icon="pi pi-check" @click="guardarBloqueo" />
            </template>
        </Dialog>
    </div>
</template>
