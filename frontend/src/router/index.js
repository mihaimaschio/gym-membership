import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import LoginView from '../views/LoginView.vue';
import MemberDashboard from '../views/MemberDashboard.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import ProfileView from '../views/ProfileView.vue';

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginView
    },
    {
        path: '/member',
        name: 'MemberDashboard',
        component: MemberDashboard,
        meta: { requiresAuth: true, role: 'ROLE_MEMBER' }
    },
    {
        path: '/admin',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { requiresAuth: true, role: 'ROLE_ADMIN' }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: ProfileView,
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login');
    } else if (to.meta.role && authStore.user?.role !== to.meta.role) {
        next('/login');
    } else {
        next();
    }
});

export default router;
