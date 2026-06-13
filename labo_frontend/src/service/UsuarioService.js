import api from './api';

export default class UsuarioService {
    async findAll() {
        const { data } = await api.get('/usuarios');
        return data;
    }

    async update(rol, id, usuario) {
        const { data } = await api.put(`/usuarios/${rol}/${id}`, usuario);
        return data;
    }

    async createAdmin(usuario) {
        const { data } = await api.post('/usuarios/admin', usuario);
        return data;
    }

    async remove(rol, id) {
        await api.delete(`/usuarios/${rol}/${id}`);
        return true;
    }
}
