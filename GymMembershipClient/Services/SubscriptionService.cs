using System.Threading.Tasks;
using GymMembershipClient.Models;

namespace GymMembershipClient.Services
{
    public class SubscriptionService
    {
        private readonly ApiClient _apiClient;

        public SubscriptionService(ApiClient apiClient)
        {
            _apiClient = apiClient;
        }

        public async Task<Subscription> GetSubscriptionByMemberIdAsync(long memberId)
        {
            return await _apiClient.GetAsync<Subscription>($"subscriptions/member/{memberId}");
        }
    }
}
