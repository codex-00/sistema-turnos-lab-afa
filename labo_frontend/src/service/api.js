import axios from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        accept: 'application/json;charset=UTF-8',
        'Content-Type': 'application/json;charset=UTF-8'
    }
});

api.interceptors.request.use((config) => {
    const auth = JSON.parse(localStorage.getItem('auth') || '{}');
    if (auth?.token) {
        config.headers['X-Usuario-Id'] = auth.token;
    }
    if (auth?.rol) {
        config.headers['X-Usuario-Rol'] = auth.rol;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.data) {
            return Promise.reject(error.response.data);
        }
        return Promise.reject(error);
    }
);

export const getAuthHeaders = () => {
    const auth = JSON.parse(localStorage.getItem('auth') || '{}');
    return {
        'X-Usuario-Id': auth?.token,
        'X-Usuario-Rol': auth?.rol
    };
};

export default api;
