using System.Collections.Generic;
using System.Threading.Tasks;
using GymMembershipClient.Models;

namespace GymMembershipClient.Services
{
    public class ClassService
    {
        private readonly ApiClient _apiClient;

        public ClassService(ApiClient apiClient)
        {
            _apiClient = apiClient;
        }

        public async Task<List<GymClass>> GetAvailableClassesAsync()
        {
            return await _apiClient.GetAsync<List<GymClass>>("classes/available");
        }

        public async Task EnrollMemberAsync(long classId, long memberId)
        {
            await _apiClient.PostAsync<object>($"classes/{classId}/enroll/{memberId}", null);
        }

        public async Task CancelEnrollmentAsync(long enrollmentId)
        {
            await _apiClient.DeleteAsync($"classes/enrollment/{enrollmentId}");
        }
    }
}
