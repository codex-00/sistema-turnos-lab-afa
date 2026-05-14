import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({

    history: createWebHistory(),

    routes: [

        {
            path: '/',
            component: AppLayout,

            children: [

                // DASHBOARD

                {
                    path: '/',
                    name: 'dashboard',
                    component: () => import('@/views/Dashboard.vue')
                },

                // =========================================
                // ADMIN
                // =========================================

                {
                    path: '/admin/medicos',
                    name: 'crud-medico',
                    component: () => import('@/views/admin/CrudMedico.vue')
                },

                {
                    path: '/admin/pacientes',
                    name: 'admin-pacientes',
                    component: () => import('@/views/admin/Pacientes.vue')
                },

                // =========================================
                // MEDICO
                // =========================================

                {
                    path: '/medico/turnos',
                    name: 'medico-turnos',
                    component: () => import('@/views/medico/Turnos.vue')
                },

                {
                    path: '/disponibilidad-medico',
                    name: 'disponibilidad-medico',
                    component: () => import('@/views/medico/DisponibilidadMedicos.vue')
                },

                // =========================================
                // PACIENTE
                // =========================================

                {
                    path: '/paciente/turnos',
                    name: 'paciente-turnos',
                    component: () => import('@/views/paciente/Turnos.vue')
                },

                {
                    path: '/paciente/estudios',
                    name: 'paciente-estudios',
                    component: () => import('@/views/paciente/Estudios.vue')
                },

                // =========================================
                // UIKIT
                // =========================================

                {
                    path: '/uikit/formlayout',
                    name: 'formlayout',
                    component: () => import('@/views/uikit/FormLayout.vue')
                },

                {
                    path: '/uikit/input',
                    name: 'input',
                    component: () => import('@/views/uikit/InputDoc.vue')
                },

                {
                    path: '/uikit/button',
                    name: 'button',
                    component: () => import('@/views/uikit/ButtonDoc.vue')
                },

                {
                    path: '/uikit/table',
                    name: 'table',
                    component: () => import('@/views/uikit/TableDoc.vue')
                },

                {
                    path: '/uikit/list',
                    name: 'list',
                    component: () => import('@/views/uikit/ListDoc.vue')
                },

                {
                    path: '/uikit/tree',
                    name: 'tree',
                    component: () => import('@/views/uikit/TreeDoc.vue')
                },

                {
                    path: '/uikit/panel',
                    name: 'panel',
                    component: () => import('@/views/uikit/PanelsDoc.vue')
                },

                {
                    path: '/uikit/overlay',
                    name: 'overlay',
                    component: () => import('@/views/uikit/OverlayDoc.vue')
                },

                {
                    path: '/uikit/media',
                    name: 'media',
                    component: () => import('@/views/uikit/MediaDoc.vue')
                },

                {
                    path: '/uikit/message',
                    name: 'message',
                    component: () => import('@/views/uikit/MessagesDoc.vue')
                },

                {
                    path: '/uikit/file',
                    name: 'file',
                    component: () => import('@/views/uikit/FileDoc.vue')
                },

                {
                    path: '/uikit/menu',
                    name: 'menu',
                    component: () => import('@/views/uikit/MenuDoc.vue')
                },

                {
                    path: '/uikit/charts',
                    name: 'charts',
                    component: () => import('@/views/uikit/ChartDoc.vue')
                },

                {
                    path: '/uikit/misc',
                    name: 'misc',
                    component: () => import('@/views/uikit/MiscDoc.vue')
                },

                {
                    path: '/uikit/timeline',
                    name: 'timeline',
                    component: () => import('@/views/uikit/TimelineDoc.vue')
                },

                // =========================================
                // PAGES
                // =========================================

                {
                    path: '/pages/empty',
                    name: 'empty',
                    component: () => import('@/views/pages/Empty.vue')
                },

                {
                    path: '/pages/crud',
                    name: 'crud',
                    component: () => import('@/views/pages/Crud.vue')
                },

                {
                    path: '/documentation',
                    name: 'documentation',
                    component: () => import('@/views/pages/Documentation.vue')
                }
            ]
        },

        // =========================================
        // LANDING
        // =========================================

        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue')
        },

        // =========================================
        // NOT FOUND
        // =========================================

        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        },

        // =========================================
        // AUTH
        // =========================================

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

        // =========================================
        // REDIRECT
        // =========================================

        {
            path: '/:pathMatch(.*)*',
            redirect: '/',
            meta: { requireAuth: true }
        }
    ]
});

export default router;
