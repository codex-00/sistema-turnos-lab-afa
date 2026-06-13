import api, { getAuthHeaders } from './api';

export default class EstudioService {
    async findAll() {
        const auth = JSON.parse(localStorage.getItem('auth') || '{}');
        if (auth?.rol === 'medico') return await this.findAllByMedico({ idMedico: auth.token });
        if (auth?.rol === 'paciente') return await this.findAllByPaciente({ idPaciente: auth.token });
        return [];
    }

    async findAllByPaciente({ idPaciente }) {
        const { data } = await api.get(`/estudios/paciente/${idPaciente}`, { headers: getAuthHeaders() });
        return data;
    }

    async findAllByMedico({ idMedico }) {
        const { data } = await api.get(`/estudios/medico/${idMedico}`, { headers: getAuthHeaders() });
        return data;
    }

    async create({ pacienteId, nombre, descripcion, archivo }) {
        const formData = new FormData();
        formData.append('pacienteId', pacienteId);
        formData.append('nombre', nombre);
        formData.append('descripcion', descripcion || '');
        formData.append('archivo', archivo);

        const { data } = await api.post('/estudios', formData, {
            headers: { ...getAuthHeaders(), 'Content-Type': 'multipart/form-data' }
        });
        return data;
    }

    async download(id) {
        return await api.get(`/estudios/${id}/descargar`, { headers: getAuthHeaders(), responseType: 'blob' });
    }

    async view(id) {
        return await api.get(`/estudios/${id}/ver`, { headers: getAuthHeaders(), responseType: 'blob' });
    }

    async remove(id) {
        await api.delete(`/estudios/${id}`, { headers: getAuthHeaders() });
        return true;
    }
}
