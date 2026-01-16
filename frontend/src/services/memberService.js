import api from './api';

export default {
    getAllMembers() {
        return api.get('/members');
    },

    getMemberById(id) {
        return api.get(`/members/${id}`);
    },

    getMemberByEmail(email) {
        return api.get(`/members/email/${email}`);
    },

    updateMember(id, data) {
        return api.put(`/members/${id}`, data);
    },

    getMembersWithActiveSubscription() {
        return api.get('/members/active');
    }
};
