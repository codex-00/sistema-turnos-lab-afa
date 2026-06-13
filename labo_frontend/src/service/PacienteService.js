import api from './api';

export default class PacienteService {
    async findAll() {
        const { data } = await api.get('/pacientes');
        return data;
    }

    async create({ apellido, dni, nombre, direccion, email, password, telefono }) {
        const { data } = await api.post('/pacientes', {
            apellido,
            dni,
            nombre,
            direccion,
            email,
            password,
            telefono
        });
        return data;
    }

    async update(id, { apellido, dni, nombre, direccion, email, password, telefono }) {
        const { data } = await api.put(`/pacientes/${id}`, {
            apellido,
            dni,
            nombre,
            direccion,
            email,
            password,
            telefono
        });
        return data;
    }

    async remove(id) {
        await api.delete(`/pacientes/${id}`);
        return true;
    }
}
