<script setup>
import AppFooter from '@/layout/AppFooter.vue';
import AppMenuItem from '@/layout/AppMenuItem.vue';
import { useLayout } from '@/layout/composables/layout';
import { useAuthStore } from '@/stores/auth';
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
    menu: {
        type: Array,
        required: true
    },
    title: {
        type: String,
        required: true
    },
    roleLabel: {
        type: String,
        required: true
    },
    homePath: {
        type: String,
        required: true
    }
});

const { layoutConfig, layoutState, isSidebarActive, toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();
const authStore = useAuthStore();
const router = useRouter();
const outsideClickListener = ref(null);

const containerClass = computed(() => ({
    'layout-overlay': layoutConfig.menuMode === 'overlay',
    'layout-static': layoutConfig.menuMode === 'static',
    'layout-static-inactive': layoutState.staticMenuDesktopInactive && layoutConfig.menuMode === 'static',
    'layout-overlay-active': layoutState.overlayMenuActive,
    'layout-mobile-active': layoutState.staticMenuMobileActive
}));

watch(isSidebarActive, (newVal) => {
    if (newVal) {
        bindOutsideClickListener();
    } else {
        unbindOutsideClickListener();
    }
});

function bindOutsideClickListener() {
    if (!outsideClickListener.value) {
        outsideClickListener.value = (event) => {
            if (isOutsideClicked(event)) {
                layoutState.overlayMenuActive = false;
                layoutState.staticMenuMobileActive = false;
                layoutState.menuHoverActive = false;
            }
        };
        document.addEventListener('click', outsideClickListener.value);
    }
}

function unbindOutsideClickListener() {
    if (outsideClickListener.value) {
        document.removeEventListener('click', outsideClickListener.value);
        outsideClickListener.value = null;
    }
}

function isOutsideClicked(event) {
    const sidebarEl = document.querySelector('.layout-sidebar');
    const topbarEl = document.querySelector('.layout-menu-button');
    return sidebarEl && topbarEl && !(sidebarEl.isSameNode(event.target) || sidebarEl.contains(event.target) || topbarEl.isSameNode(event.target) || topbarEl.contains(event.target));
}

function logout() {
    authStore.logout();
    router.push('/auth/login');
}
</script>

<template>
    <div class="layout-wrapper" :class="containerClass">
        <div class="layout-topbar">
            <div class="layout-topbar-logo-container">
                <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                    <i class="pi pi-bars"></i>
                </button>
                <router-link :to="homePath" class="layout-topbar-logo">
                    <span>{{ title }}</span>
                </router-link>
            </div>

            <div class="layout-topbar-actions">
                <Tag :value="roleLabel" severity="info" />
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <button type="button" class="layout-topbar-action" @click="logout">
                    <i class="pi pi-sign-out"></i>
                    <span>Salir</span>
                </button>
            </div>
        </div>

        <div class="layout-sidebar">
            <ul class="layout-menu">
                <template v-for="(item, i) in props.menu" :key="item.label">
                    <app-menu-item v-if="!item.separator" :item="item" :index="i" />
                    <li v-else class="menu-separator"></li>
                </template>
            </ul>
        </div>

        <div class="layout-main-container">
            <div class="layout-main">
                <router-view />
            </div>
            <app-footer />
        </div>

        <div class="layout-mask animate-fadein"></div>
    </div>
    <Toast />
</template>
