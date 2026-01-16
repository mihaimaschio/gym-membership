using System;

namespace GymMembershipClient.Models
{
    public class GymClass
    {
        public long Id { get; set; }
        public string Type { get; set; }
        public string DayOfWeek { get; set; }
        public string Time { get; set; }
        public int MaxCapacity { get; set; }
        public int EnrollmentCount { get; set; }
        
        public int AvailableSpots => MaxCapacity - EnrollmentCount;
    }
}
