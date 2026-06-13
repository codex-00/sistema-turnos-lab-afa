import api from './api';

export default class MedicoService {
    async findAll() {
        const { data } = await api.get('/medicos');
        return data;
    }

    async findAllByEspecialidad({ especialidad }) {
        const { data } = await api.get('/medicos/especialidades', { params: { especialidad } });
        return data;
    }

    async create({ apellido, nombre, dni, direccion, email, especialidad, especialidades, password, telefono, disponibilidades }) {
        const { data } = await api.post('/medicos', {
            apellido,
            nombre,
            dni,
            direccion,
            email,
            especialidad,
            especialidades,
            password,
            telefono,
            disponibilidades
        });
        return data;
    }

    async update(id, { apellido, nombre, dni, direccion, email, especialidad, especialidades, password, telefono, disponibilidades }) {
        const { data } = await api.put(`/medicos/${id}`, {
            apellido,
            nombre,
            dni,
            direccion,
            email,
            especialidad,
            especialidades,
            password,
            telefono,
            disponibilidades
        });
        return data;
    }

    async remove(id) {
        await api.delete(`/medicos/${id}`);
        return true;
    }
}
