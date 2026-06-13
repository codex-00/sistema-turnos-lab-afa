import { useAuthStore } from '@/stores/auth';
import { createRouter, createWebHistory } from 'vue-router';

const roleHome = {
    admin: '/admin/dashboard',
    medico: '/medico/dashboard',
    paciente: '/paciente/dashboard'
};

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: () => {
                const auth = useAuthStore();
                return auth.rol && roleHome[auth.rol] ? roleHome[auth.rol] : '/auth/login';
            }
        },
        {
            path: '/admin',
            component: () => import('@/modules/admin/AdminLayout.vue'),
            meta: { requireAuth: true, roles: ['admin'] },
            children: [
                { path: '', redirect: '/admin/dashboard' },
                { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/Dashboard.vue') },
                { path: 'medicos', name: 'admin-medicos', component: () => import('@/views/admin/CrudMedico.vue') },
                { path: 'pacientes', name: 'admin-pacientes', component: () => import('@/views/admin/Pacientes.vue') },
                { path: 'turnos', name: 'admin-turnos', component: () => import('@/views/admin/CrudTurnos.vue') },
                { path: 'usuarios', name: 'admin-usuarios', component: () => import('@/views/admin/Usuarios.vue') }
            ]
        },
        {
            path: '/medico',
            component: () => import('@/modules/medico/MedicoLayout.vue'),
            meta: { requireAuth: true, roles: ['medico'] },
            children: [
                { path: '', redirect: '/medico/dashboard' },
                { path: 'dashboard', name: 'medico-dashboard', component: () => import('@/views/medico/Dashboard.vue') },
                { path: 'turnos', name: 'medico-turnos', component: () => import('@/views/medico/Turnos.vue') },
                { path: 'disponibilidad', name: 'medico-disponibilidad', component: () => import('@/views/medico/DisponibilidadMedicos.vue') },
                { path: 'agenda', name: 'medico-agenda', component: () => import('@/views/medico/AgendaView.vue') },
                { path: 'pacientes', name: 'medico-pacientes', component: () => import('@/views/medico/Pacientes.vue') },
                { path: 'estudios', name: 'medico-estudios', component: () => import('@/views/medico/Estudios.vue') }
            ]
        },
        {
            path: '/paciente',
            component: () => import('@/modules/paciente/PacienteLayout.vue'),
            meta: { requireAuth: true, roles: ['paciente'] },
            children: [
                { path: '', redirect: '/paciente/dashboard' },
                { path: 'dashboard', name: 'paciente-dashboard', component: () => import('@/views/paciente/Dashboard.vue') },
                { path: 'turnos', name: 'paciente-turnos', component: () => import('@/views/paciente/Turnos.vue') },
                { path: 'estudios', name: 'paciente-estudios', component: () => import('@/views/paciente/Estudios.vue') },
                { path: 'historial', name: 'paciente-historial', component: () => import('@/views/paciente/Historial.vue') },
                { path: 'perfil', name: 'paciente-perfil', component: () => import('@/views/paciente/Perfil.vue') }
            ]
        },
        {
            path: '/auth/login',
            name: 'login',
            meta: { requireAuth: false },
            component: () => import('@/views/pages/auth/Login.vue')
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue')
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue')
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/pages/notfound'
        }
    ]
});

router.beforeEach((to) => {
    const auth = useAuthStore();

    if (to.meta.requireAuth === false && auth.rol && roleHome[auth.rol]) {
        return roleHome[auth.rol];
    }

    const matchedWithAuth = to.matched.find((route) => route.meta.requireAuth);
    if (!matchedWithAuth) {
        return true;
    }

    if (!auth.rol || !auth.token) {
        return '/auth/login';
    }

    const allowedRoles = matchedWithAuth.meta.roles || [];
    if (allowedRoles.length && !allowedRoles.includes(auth.rol)) {
        return '/auth/access';
    }

    return true;
});

export default router;
