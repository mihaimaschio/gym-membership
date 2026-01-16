using System;

namespace GymMembershipClient.Models
{
    public class ClassEnrollment
    {
        public long Id { get; set; }
        public GymClass GymClass { get; set; }
        public DateTime EnrollmentDate { get; set; }
    }
}
