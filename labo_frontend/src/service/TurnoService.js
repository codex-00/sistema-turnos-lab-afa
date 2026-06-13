import api from './api';

export default class TurnoService {
    async findAll() {
        const { data } = await api.get('/turnos');
        return data;
    }

    async findAllByMedico({ idMedico }) {
        const { data } = await api.get('/turnos', { params: { medico: idMedico } });
        return data;
    }

    async findAllByPaciente({ idPaciente }) {
        const { data } = await api.get('/turnos', { params: { paciente: idPaciente } });
        return data;
    }

    async create({ fechaDeTurno, medico, paciente }) {
        const { data } = await api.post('/turnos', {
            fechaDeTurno,
            medico,
            paciente
        });
        return data;
    }

    async update(id, { fechaDeTurno, fechaCreacion, estado, medico, paciente }) {
        const { data } = await api.put(`/turnos/${id}`, {
            fechaDeTurno,
            fechaCreacion,
            estado,
            medico,
            paciente
        });
        return data;
    }

    async cambiarEstado(id, estado) {
        const { data } = await api.patch(`/turnos/${id}/estado`, null, { params: { estado } });
        return data;
    }

    async remove(id) {
        await api.delete(`/turnos/${id}`);
        return true;
    }
}
