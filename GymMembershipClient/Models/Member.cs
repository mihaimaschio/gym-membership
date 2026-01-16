using System.Collections.Generic;

namespace GymMembershipClient.Models
{
    public class Member
    {
        public long Id { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Email { get; set; }
        public Subscription Subscription { get; set; }
        public List<ClassEnrollment> Enrollments { get; set; }
    }
}
