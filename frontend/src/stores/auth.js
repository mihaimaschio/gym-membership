import { defineStore } from 'pinia';
import authService from '../services/authService';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        user: JSON.parse(localStorage.getItem('user') || 'null'),
    }),

    getters: {
        isAuthenticated: (state) => !!state.token,
        isAdmin: (state) => state.user?.role === 'ROLE_ADMIN',
        isMember: (state) => state.user?.role === 'ROLE_MEMBER',
    },

    actions: {
        async login(credentials) {
            const response = await authService.login(credentials);
            this.token = response.data.token;
            this.user = {
                email: response.data.email,
                role: response.data.role
            };
            localStorage.setItem('token', this.token);
            localStorage.setItem('user', JSON.stringify(this.user));
            return response.data;
        },

        async register(userData) {
            const response = await authService.register(userData);
            return response.data;
        },

        logout() {
            this.token = null;
            this.user = null;
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        }
    }
});
