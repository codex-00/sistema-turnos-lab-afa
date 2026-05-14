import axios from 'axios';

const api = axios.create({
    baseURL: `http://localhost:8081`,
    headers: {
        accept: 'application/json'
    }
});

export default class DisponibilidadService {
    async guardarDisponibilidad({ medico, dia, horaInicio, horaFin, duracionTurno }) {
        try {
            const { data } = await api.post(`/disponibilidad/crear/${medico.idMedico}`, {
                dia,
                horaInicio,
                horaFin,
                duracionTurno
            });

            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }

    async findAllByMedico({ idMedico }) {
        try {
            const { data } = await api.get(`/disponibilidad/medico/${idMedico}`);

            return data;
        } catch (error) {
            if (error.response?.data) throw error.response.data;
            throw error;
        }
    }
}
