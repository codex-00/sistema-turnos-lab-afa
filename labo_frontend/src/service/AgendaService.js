import api from './api';

export default class AgendaService {
    async findDisponibles({ idMedico, fecha, especialidad }) {
        const { data } = await api.get('/agenda', { params: { medicoId: idMedico, fecha, especialidad } });
        return data;
    }

    async findDia({ idMedico, fecha }) {
        const { data } = await api.get('/agenda/dia', { params: { medicoId: idMedico, fecha } });
        return data;
    }

    async findMes({ idMedico, anio, mes }) {
        const { data } = await api.get('/agenda/mes', { params: { medicoId: idMedico, anio, mes } });
        return data;
    }

    async bloquear({ medicoId, fecha, hora, descripcion }) {
        const { data } = await api.post('/agenda/bloquear', null, {
            params: { medicoId, fecha, hora, descripcion }
        });
        return data;
    }

    async reservar({ agendaId, pacienteId }) {
        const { data } = await api.post('/agenda/reservar', null, { params: { agendaId, pacienteId } });
        return data;
    }

    async reprogramar({ turnoId, agendaId }) {
        const { data } = await api.post('/agenda/reprogramar', null, { params: { turnoId, agendaId } });
        return data;
    }
}
