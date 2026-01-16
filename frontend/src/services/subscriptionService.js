import api from './api';

export default {
    getAllSubscriptions() {
        return api.get('/subscriptions');
    },

    getSubscriptionByMemberId(memberId) {
        return api.get(`/subscriptions/member/${memberId}`);
    },

    createSubscription(memberId, type) {
        return api.post(`/subscriptions/member/${memberId}?type=${type}`);
    },

    renewSubscription(id) {
        return api.put(`/subscriptions/${id}/renew`);
    },

    getActiveSubscriptions() {
        return api.get('/subscriptions/active');
    },

    deleteSubscription(id) {
        return api.delete(`/subscriptions/${id}`);
    }
};
