import axios from 'axios';

const api = axios.create({
    baseURL: `http://localhost:8081`,
    headers: {
        accept: 'application/json'
    }
});

export default class AgendaService {
    async findDisponibles({ idMedico, fecha }) {
        try {
            const { data } = await api.get(`/agenda?medicoId=${idMedico}&fecha=${fecha}`);
            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }

    async reservar({ agendaId, pacienteId }) {
        try {
            const { data } = await api.post(`/agenda/reservar?agendaId=${agendaId}&pacienteId=${pacienteId}`);
            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }
}
