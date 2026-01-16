using System;

namespace GymMembershipClient.Models
{
    public class Subscription
    {
        public long Id { get; set; }
        public string Type { get; set; }
        public double Price { get; set; }
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }
        public bool Active { get; set; }
    }
}
