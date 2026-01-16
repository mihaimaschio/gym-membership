using Newtonsoft.Json;

namespace GymMembershipClient.Models
{
    public class LoginResponse
    {
        [JsonProperty("token")]
        public string Token { get; set; }

        [JsonProperty("email")]
        public string Email { get; set; }

        [JsonProperty("role")]
        public string Role { get; set; }
    }
}
