import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  
  state: () => ({
    email: null,
    rol: null,
    token: null,
  }),

  actions: {

    getEmail() {
      return this.email
    },

    getRol() {
      return this.rol
    },

    getToken() {
      return this.token
    },

    setToken(token) {
      this.token = token
      return this.token
    },

    async login({email, password, rol}) {
        try {
            let persona = null
            if (rol == 'medico') {
                persona = {idMedico: 1, email: "laura@clinica.com", password: "clave1"}// await authService.login({email, password})
                if (persona.email != email || persona.password != password) return false
                
                this.email = persona.email
                this.rol = rol
                this.token = persona.idMedico
            } else if (rol == 'paciente') {
                persona = {idPaciente: 1, email: "laura@clinica.com", password: "clave1"}// await authService.login({email, password})
                if (persona.email != email || persona.password != password) return false

                this.username = persona.email
                this.roles = rol
                this.token = persona.idPaciente
            } else {
                return false
            }
            
        } catch (error) {
            return false
        }
    },

    logout() {
        try {
            this.username = null
            this.roles = null
            this.token = null
            localStorage.removeItem('auth');
            return true
        } catch (error) {
            console.log(error);
            return false
        }
    },

  },
  
  persist: true,

})