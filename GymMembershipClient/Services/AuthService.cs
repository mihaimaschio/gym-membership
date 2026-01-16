using System.Threading.Tasks;
using GymMembershipClient.Models;

namespace GymMembershipClient.Services
{
    public class AuthService
    {
        private readonly ApiClient _apiClient;

        public AuthService(ApiClient apiClient)
        {
            _apiClient = apiClient;
        }

        public async Task<LoginResponse> LoginAsync(string email, string password)
        {
            var request = new LoginRequest { Email = email, Password = password };
            var response = await _apiClient.PostAsync<LoginResponse>("auth/login", request);
            
            if (response != null && !string.IsNullOrEmpty(response.Token))
            {
                _apiClient.SetToken(response.Token);
            }

            return response;
        }
    }
}
