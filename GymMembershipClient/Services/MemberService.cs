using System.Threading.Tasks;
using GymMembershipClient.Models;

namespace GymMembershipClient.Services
{
    public class MemberService
    {
        private readonly ApiClient _apiClient;

        public MemberService(ApiClient apiClient)
        {
            _apiClient = apiClient;
        }

        public async Task<Member> GetMemberByEmailAsync(string email)
        {
            return await _apiClient.GetAsync<Member>($"members/email/{email}");
        }
    }
}
