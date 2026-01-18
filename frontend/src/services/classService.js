import api from './api';

export default {
    getAllClasses() {
        return api.get('/classes');
    },

    getClassById(id) {
        return api.get(`/classes/${id}`);
    },

    getClassesWithAvailableSpots() {
        return api.get('/classes/available');
    },

    getClassesByDay(dayOfWeek) {
        return api.get(`/classes/day/${dayOfWeek}`);
    },

    enrollMemberInClass(classId, memberId) {
        return api.post(`/classes/${classId}/enroll/${memberId}`);
    },

    cancelEnrollment(enrollmentId) {
        return api.delete(`/classes/enrollment/${enrollmentId}`);
    },

    getMemberEnrollments(memberId) {
        return api.get(`/classes/member/${memberId}/enrollments`);
    },

    getClassEnrollments(classId) {
        return api.get(`/classes/${classId}/enrollments`);
    },

    createClass(type, dayOfWeek, time) {
        return api.post(`/classes?type=${type}&dayOfWeek=${dayOfWeek}&time=${time}`);
    },

    deleteClass(id) {
        return api.delete(`/classes/${id}`);
    }
};
