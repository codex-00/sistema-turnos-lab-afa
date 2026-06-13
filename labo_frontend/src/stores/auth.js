import { defineStore } from 'pinia';
import api from '@/service/api';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        email: null,
        rol: null,
        token: null,
        usuario: null
    }),

    actions: {
        getEmail() {
            return this.email;
        },

        getRol() {
            return this.rol;
        },

        getToken() {
            return this.token;
        },

        setToken(token) {
            this.token = token;
            return this.token;
        },

        async login({ email, password, rol }) {
            try {
                const { data } = await api.post('/auth/login', { email, password, rol });
                const usuario = data.usuario;

                this.email = usuario.email;
                this.rol = data.rol;
                this.token = data.usuarioId || data.token;
                this.usuario = usuario;

                return { ok: true };
            } catch (error) {
                return { ok: false, type: error?.code === 'INVALID_CREDENTIALS' ? 'invalid_credentials' : 'server_error' };
            }
        },

        async recoverPassword({ email, rol }) {
            try {
                const { data } = await api.post('/auth/recuperar-password', { email, rol });
                return { ok: true, data };
            } catch (error) {
                return { ok: false, type: 'server_error' };
            }
        },

        async resetPassword({ email, rol, token, nuevaPassword }) {
            try {
                const { data } = await api.post('/auth/restablecer-password', { email, rol, token, nuevaPassword });
                return { ok: true, data };
            } catch (error) {
                return { ok: false, type: 'server_error' };
            }
        },

        logout() {
            try {
                this.email = null;
                this.rol = null;
                this.token = null;
                this.usuario = null;
                localStorage.removeItem('auth');
                return true;
            } catch (error) {
                return false;
            }
        }
    },

    persist: true
});
