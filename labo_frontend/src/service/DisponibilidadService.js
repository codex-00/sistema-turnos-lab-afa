import api from './api';

export default class DisponibilidadService {
    async guardarDisponibilidad({ medico, dia, horaInicio, horaFin, duracionTurno, especialidad }) {
        const { data } = await api.post(`/disponibilidad/crear/${medico.idMedico}`, {
            dia,
            horaInicio,
            horaFin,
            duracionTurno,
            especialidad
        });
        return data;
    }

    async findAllByMedico({ idMedico }) {
        const { data } = await api.get(`/disponibilidad/medico/${idMedico}`);
        return data;
    }

    async update(id, { medico, dia, horaInicio, horaFin, duracionTurno, especialidad }) {
        const { data } = await api.put(`/disponibilidad/${id}`, {
            medico,
            dia,
            horaInicio,
            horaFin,
            duracionTurno,
            especialidad
        });
        return data;
    }

    async remove(id) {
        await api.delete(`/disponibilidad/${id}`);
        return true;
    }
}
