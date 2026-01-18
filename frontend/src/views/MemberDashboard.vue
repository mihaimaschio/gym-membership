<template>
  <div class="dashboard-layout">
    <nav class="topbar">
      <div class="container flex justify-between items-center h-full">
        <div class="flex items-center gap-4">
          <h1 class="dashboard-title">Member Dashboard</h1>
        </div>
        <div class="flex gap-3">
          <button type="button" @click.prevent="openQRCodeModal" class="btn btn-primary btn-sm">
            Check In
          </button>
          <button @click="$router.push('/profile')" class="btn btn-secondary btn-sm">
            Profile
          </button>
          <button @click="logout" class="btn btn-secondary btn-sm">
            <span>Sign Out</span>
          </button>
        </div>
      </div>
    </nav>

    <main class="container py-8">
      <div class="dashboard-content">
        <!-- Subscription Card -->
        <div class="subscription-card card">
          <div class="card-header">
            <div>
              <h2 class="card-title">My Subscription</h2>
              <p class="card-subtitle">Manage your membership</p>
            </div>
            <span :class="['badge', subscription?.active ? 'badge-success' : 'badge-danger']">
              {{ subscription?.active ? 'Active' : 'Inactive' }}
            </span>
          </div>

          <div v-if="subscription" class="subscription-details">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-label">Plan Type</div>
                <div class="stat-value stat-primary">{{ formatSubscriptionType(subscription.type) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">Price</div>
                <div class="stat-value">{{ subscription.price }} lei</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">Start Date</div>
                <div class="stat-value stat-muted">{{ formatDate(subscription.startDate) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">End Date</div>
                <div class="stat-value stat-muted">{{ formatDate(subscription.endDate) }}</div>
              </div>
            </div>
          </div>
          <div v-else class="alert warning">
            <div>
              <p class="font-bold">No Active Subscription</p>
              <p class="text-sm">You need an active subscription to enroll in classes. Please visit the reception.</p>
            </div>
          </div>
        </div>

        <div class="main-layout">
          <!-- Available Classes Section -->
          <div class="classes-section card">
            <div class="section-header">
              <div>
                <h2 class="card-title">Available Classes</h2>
                <p class="card-subtitle">Book your fitness sessions</p>
              </div>
              <div class="week-navigation">
                <button 
                  @click="prevWeek" 
                  class="nav-button" 
                  :disabled="!canGoBack"
                  :class="{ 'disabled': !canGoBack }"
                >
                  <span>←</span>
                </button>
                <span class="week-display">{{ weekDisplay }}</span>
                <button @click="nextWeek" class="nav-button">
                  <span>→</span>
                </button>
              </div>
            </div>

            <!-- Day Tabs -->
            <div class="day-tabs-container">
              <div class="day-tabs">
                <button 
                  v-for="day in calendarDays" 
                  :key="day.date.toISOString()"
                  :class="['day-tab', { 
                    'active': isSameDay(selectedDate, day.date), 
                    'today': day.isToday && !isSameDay(selectedDate, day.date)
                  }]"
                  @click="selectDay(day)"
                >
                  <span class="day-label">{{ day.label }}</span>
                  <span class="day-number">{{ day.dayNumber }}</span>
                </button>
              </div>
            </div>

            <!-- Classes Grid -->
            <div v-if="filteredClasses.length > 0" class="classes-grid">
              <div 
                v-for="gymClass in filteredClasses" 
                :key="gymClass.id"
                class="class-card"
              >
                <div class="class-header">
                  <h3 class="class-name">{{ formatClassType(gymClass.type) }}</h3>
                  <span class="class-time">{{ gymClass.time }}</span>
                </div>
                
                <div class="class-availability">
                  <div class="availability-info">
                    <span class="availability-label">Availability</span>
                    <span :class="['availability-count', gymClass.availableSpots > 0 ? 'available' : 'full']">
                      {{ gymClass.availableSpots > 0 ? gymClass.availableSpots + ' spots' : 'Full' }}
                    </span>
                  </div>
                  <div class="progress-bar">
                    <div 
                      class="progress-fill" 
                      :class="{ 'full': gymClass.availableSpots === 0 }"
                      :style="{ width: ((20 - gymClass.availableSpots) / 20 * 100) + '%' }"
                    ></div>
                  </div>
                </div>

                <button 
                  @click="enrollInClass(gymClass.id)" 
                  class="btn enroll-button"
                  :class="{
                    'btn-success': isEnrolled(gymClass.id),
                    'btn-primary': !isEnrolled(gymClass.id) && !isClassInPast(gymClass) && hasActiveSubscription && gymClass.availableSpots > 0,
                    'btn-secondary': !isEnrolled(gymClass.id) && (isClassInPast(gymClass) || !hasActiveSubscription || gymClass.availableSpots === 0)
                  }"
                  :disabled="isEnrolled(gymClass.id) || isClassInPast(gymClass) || !hasActiveSubscription || gymClass.availableSpots === 0"
                >
                  <span v-if="isEnrolled(gymClass.id)" class="enrolled-text">
                    Enrolled
                  </span>
                  <span v-else-if="isClassInPast(gymClass)">
                    Class Passed
                  </span>
                  <span v-else>
                    {{ !hasActiveSubscription ? 'Subscription Required' : (gymClass.availableSpots === 0 ? 'Class Full' : 'Enroll Now') }}
                  </span>
                </button>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <div class="empty-icon">📅</div>
              <p class="empty-text">No classes available for {{ formatDate(selectedDate) }}</p>
            </div>
          </div>

          <!-- My Schedule Sidebar -->
          <div class="schedule-sidebar">
            <div class="card schedule-card">
              <div class="card-header">
                <div class="flex items-center gap-3">
                  <div class="icon-wrapper icon-secondary">
                    <span>📅</span>
                  </div>
                  <div>
                    <h2 class="card-title">My Schedule</h2>
                    <p class="card-subtitle">{{ enrollments.length }} class{{ enrollments.length !== 1 ? 'es' : '' }}</p>
                  </div>
                </div>
              </div>
              
              <div v-if="enrollments.length > 0" class="enrollments-list">
                <div 
                  v-for="enrollment in enrollments" 
                  :key="enrollment.id"
                  class="enrollment-item"
                >
                  <div class="enrollment-content">
                    <h4 class="enrollment-class">{{ formatClassType(enrollment.gymClass.type) }}</h4>
                    <p class="enrollment-time">
                      {{ enrollment.gymClass.dayOfWeek }} • {{ enrollment.gymClass.time }}
                    </p>
                  </div>
                  <button 
                    @click="cancelEnrollment(enrollment.id)" 
                    class="cancel-button"
                    title="Cancel Booking"
                  >
                    <span>×</span>
                  </button>
                </div>
              </div>
              <div v-else class="empty-state-small">
                <p>You haven't enrolled in any classes yet.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- QR Code Modal -->
    <div v-if="showQRCode" class="modal-overlay" @click.self="showQRCode = false">
      <div class="qr-modal-container animate-scale-in" @click.stop>
        <div class="qr-code-wrapper">
          <qrcode-vue 
            :value="qrCodeData" 
            :size="300"
            level="H"
            render-as="svg"
          />
        </div>
      </div>
    </div>

    <ChatAssistant />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import memberService from '../services/memberService';
import subscriptionService from '../services/subscriptionService';
import classService from '../services/classService';
import ChatAssistant from '../components/ChatAssistant.vue';
import QrcodeVue from 'qrcode.vue';

export default {
  name: 'MemberDashboard',
  components: {
    ChatAssistant,
    QrcodeVue
  },
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const member = ref(null);
    const subscription = ref(null);
    const availableClasses = ref([]);
    const enrollments = ref([]);
    const showQRCode = ref(false);
    
    const currentWeekStart = ref(new Date());
    const selectedDate = ref(new Date());

    const initWeek = () => {
      const now = new Date();
      const day = now.getDay();
      const diff = now.getDate() - day + (day === 0 ? -6 : 1);
      const monday = new Date(now.setDate(diff));
      monday.setHours(0, 0, 0, 0);
      currentWeekStart.value = monday;
      selectedDate.value = new Date();
    };

    const canGoBack = computed(() => {
        const now = new Date();
        const day = now.getDay();
        const diff = now.getDate() - day + (day === 0 ? -6 : 1);
        const thisWeekStart = new Date(now.setDate(diff));
        thisWeekStart.setHours(0, 0, 0, 0);
        return currentWeekStart.value > thisWeekStart;
    });

    const weekDisplay = computed(() => {
        const start = new Date(currentWeekStart.value);
        const end = new Date(start);
        end.setDate(start.getDate() + 6);
        
        const options = { day: 'numeric', month: 'short' };
        return `${start.toLocaleDateString('en-US', options)} - ${end.toLocaleDateString('en-US', options)}`;
    });

    const calendarDays = computed(() => {
        const days = [];
        const start = new Date(currentWeekStart.value);
        
        for(let i=0; i<7; i++) {
            const date = new Date(start);
            date.setDate(start.getDate() + i);
            days.push({
                date: date,
                dayName: getDayName(date),
                label: date.toLocaleDateString('en-US', { weekday: 'short' }),
                dayNumber: date.getDate(),
                isToday: isSameDay(date, new Date())
            });
        }
        return days;
    });

    const getDayName = (date) => {
        const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
        return days[date.getDay()];
    };

    const isSameDay = (d1, d2) => {
        return d1.getDate() === d2.getDate() && 
               d1.getMonth() === d2.getMonth() && 
               d1.getFullYear() === d2.getFullYear();
    };

    const prevWeek = () => {
        const now = new Date();
        const day = now.getDay();
        const diff = now.getDate() - day + (day === 0 ? -6 : 1);
        const thisWeekStart = new Date(now.setDate(diff));
        thisWeekStart.setHours(0, 0, 0, 0);

        if (currentWeekStart.value <= thisWeekStart) return;

        const newDate = new Date(currentWeekStart.value);
        newDate.setDate(newDate.getDate() - 7);
        currentWeekStart.value = newDate;
    };

    const nextWeek = () => {
        const newDate = new Date(currentWeekStart.value);
        newDate.setDate(newDate.getDate() + 7);
        currentWeekStart.value = newDate;
    };

    const selectDay = (dayObj) => {
        selectedDate.value = dayObj.date;
    };

    const hasActiveSubscription = computed(() => subscription.value?.active);

    const qrCodeData = computed(() => {
      if (!member.value) return '';
      const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD format
      return JSON.stringify({
        memberId: member.value.id,
        email: member.value.email,
        name: `${member.value.firstName} ${member.value.lastName}`,
        date: today,
        type: 'gym_checkin'
      });
    });

    const isEnrolled = (classId) => {
      return enrollments.value.some(enrollment => enrollment.gymClass.id === classId);
    };

    const isClassInPast = (gymClass) => {
      const targetDayName = getDayName(selectedDate.value);
      if (gymClass.dayOfWeek !== targetDayName) {
        return false;
      }

      const now = new Date();
      const classDate = new Date(selectedDate.value);
      
      // Parse the time string (format: "HH:mm:ss" or "HH:mm")
      const timeParts = gymClass.time.split(':');
      const hours = parseInt(timeParts[0], 10);
      const minutes = parseInt(timeParts[1], 10);
      
      classDate.setHours(hours, minutes, 0, 0);
      
      // Compare: if class date/time is before now, it's in the past
      return classDate < now;
    };

    const filteredClasses = computed(() => {
      const targetDayName = getDayName(selectedDate.value);
      return availableClasses.value
        .filter(c => c.dayOfWeek === targetDayName)
        .sort((a, b) => a.time.localeCompare(b.time));
    });

    const loadMemberData = async () => {
      try {
        const memberResponse = await memberService.getMemberByEmail(authStore.user.email);
        member.value = memberResponse.data;
        
        if (member.value.id) {
          try {
            const subResponse = await subscriptionService.getSubscriptionByMemberId(member.value.id);
            subscription.value = subResponse.data;
          } catch (err) {
            console.log('No subscription found');
          }

          const enrollResponse = await classService.getMemberEnrollments(member.value.id);
          enrollments.value = enrollResponse.data;
        }
      } catch (error) {
        console.error('Error loading member data:', error);
      }
    };

    const loadAvailableClasses = async () => {
      try {
        const response = await classService.getClassesWithAvailableSpots();
        availableClasses.value = response.data;
      } catch (error) {
        console.error('Error loading classes:', error);
      }
    };

    const enrollInClass = async (classId) => {
      // Find the class to check if it's in the past
      const gymClass = availableClasses.value.find(c => c.id === classId);
      if (gymClass && isClassInPast(gymClass)) {
        alert('Cannot enroll in a class that has already passed.');
        return;
      }

      try {
        await classService.enrollMemberInClass(classId, member.value.id);
        await loadMemberData();
        await loadAvailableClasses();
      } catch (error) {
        alert(error.response?.data?.message || 'Error enrolling in class');
      }
    };

    const cancelEnrollment = async (enrollmentId) => {
      try {
        if(!confirm('Cancel this class?')) return;
        
        await classService.cancelEnrollment(enrollmentId);
        await loadMemberData();
        await loadAvailableClasses();
      } catch (error) {
        alert('Error cancelling enrollment');
      }
    };

    const formatSubscriptionType = (type) => type?.replace(/_/g, ' ') || type;
    
    const formatClassType = (type) => {
      const types = {
        'TRX': 'TRX',
        'INTERVAL_TRAINING': 'Interval Training',
        'YOGA': 'Yoga',
        'CIRCUIT': 'Circuit Training',
        'KICK_BOX': 'Kickbox'
      };
      return types[type] || type;
    };

    const formatDate = (date) => {
      if (!date) return '-';
      return new Date(date).toLocaleDateString();
    };

    const logout = () => {
      authStore.logout();
      router.push('/login');
    };

    const openQRCodeModal = (event) => {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      console.log('Button clicked!');
      console.log('showQRCode before:', showQRCode.value);
      showQRCode.value = true;
      console.log('showQRCode after:', showQRCode.value);
    };

    onMounted(() => {
      initWeek();
      loadMemberData();
      loadAvailableClasses();
    });

    return {
      subscription,
      availableClasses,
      enrollments,
      filteredClasses,
      hasActiveSubscription,
      formatSubscriptionType,
      formatClassType,
      formatDate,
      logout,
      weekDisplay,
      calendarDays,
      prevWeek,
      nextWeek,
      selectDay,
      selectedDate,
      enrollInClass,
      cancelEnrollment,
      isSameDay,
      canGoBack,
      isEnrolled,
      isClassInPast,
      showQRCode,
      qrCodeData,
      openQRCodeModal
    };
  }
};
</script>

<style scoped>
.dashboard-layout {
  min-height: 100vh;
  background: var(--bg-body);
}

.topbar {
  background: var(--bg-surface);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  height: 72px;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: var(--shadow-sm);
}

.logo-mini {
  font-size: 1.75rem;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--accent-light) 100%);
  border-radius: var(--radius-lg);
}

.dashboard-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.subscription-card {
  margin-bottom: var(--spacing-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  border-bottom: 2px solid var(--border-light);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 0.25rem 0;
}

.card-subtitle {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin: 0;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.icon-primary {
  background: var(--primary-light);
}

.icon-secondary {
  background: var(--secondary-light);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.stat-item {
  padding: var(--spacing-lg);
  background: var(--bg-surface-hover);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.stat-item:hover {
  background: var(--bg-surface);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--spacing-sm);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
}

.stat-primary {
  color: var(--primary);
}

.stat-muted {
  color: var(--text-secondary);
  font-size: 1.125rem;
}

.main-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: var(--spacing-xl);
}

@media (max-width: 1024px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
}

.classes-section {
  min-height: 600px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-xl);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.week-navigation {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: var(--bg-surface-hover);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-lg);
}

.nav-button {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  border: 2px solid var(--border);
  background: var(--bg-surface);
  color: var(--text-main);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: 1.25rem;
  font-weight: 600;
}

.nav-button:hover:not(.disabled) {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  transform: scale(1.1);
}

.nav-button.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.week-display {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
  white-space: nowrap;
}

.day-tabs-container {
  margin-bottom: var(--spacing-xl);
  overflow-x: auto;
  padding-bottom: var(--spacing-sm);
}

.day-tabs {
  display: flex;
  gap: var(--spacing-sm);
  min-width: max-content;
}

.day-tab {
  min-width: 70px;
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
  border: 2px solid var(--border);
  background: var(--bg-surface);
  cursor: pointer;
  text-align: center;
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.day-tab:hover {
  border-color: var(--primary);
  background: var(--primary-light);
  transform: translateY(-2px);
}

.day-tab.active {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  color: white;
  border-color: var(--primary);
  box-shadow: var(--shadow-primary);
}

.day-tab.today:not(.active) {
  border-color: var(--primary);
  background: var(--primary-light);
}

.day-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  opacity: 0.8;
}

.day-number {
  font-size: 1.25rem;
  font-weight: 700;
}

.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.class-card {
  background: var(--bg-surface);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
  transition: all var(--transition-slow);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.class-card:hover {
  border-color: var(--primary);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.class-name {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.class-time {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  background: var(--bg-surface-hover);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-md);
  font-family: 'Courier New', monospace;
}

.class-availability {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.availability-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.availability-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
}

.availability-count {
  font-size: 0.875rem;
  font-weight: 700;
}

.availability-count.available {
  color: var(--success);
}

.availability-count.full {
  color: var(--danger);
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: var(--bg-surface-hover);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary) 0%, var(--accent) 100%);
  border-radius: var(--radius-full);
  transition: width var(--transition-slow);
}

.progress-fill.full {
  background: var(--danger);
}

.enroll-button {
  width: 100%;
  margin-top: auto;
}

.btn-success {
  background: linear-gradient(135deg, var(--success) 0%, #059669 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  cursor: default;
}

.btn-success:hover:not(:disabled) {
  transform: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-success:disabled {
  opacity: 1;
  cursor: default;
}

.enrolled-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  font-weight: 600;
}

.enrolled-text span:first-child {
  font-size: 1.125rem;
  font-weight: 700;
}

.schedule-sidebar {
  position: sticky;
  top: 88px;
  height: fit-content;
  max-height: calc(100vh - 104px);
}

.schedule-card {
  position: relative;
}

.enrollments-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  max-height: 600px;
  overflow-y: auto;
}

.enrollment-item {
  padding: var(--spacing-md);
  background: var(--bg-surface-hover);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-lg);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
  transition: all var(--transition-base);
  position: relative;
}

.enrollment-item:hover {
  border-color: var(--primary);
  background: var(--bg-surface);
  transform: translateX(4px);
}

.enrollment-content {
  flex: 1;
}

.enrollment-class {
  font-size: 0.9375rem;
  font-weight: 700;
  color: var(--primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.enrollment-time {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0;
}

.cancel-button {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  border: none;
  background: var(--bg-surface);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: 1.5rem;
  line-height: 1;
  flex-shrink: 0;
}

.cancel-button:hover {
  background: var(--danger);
  color: white;
  transform: scale(1.1);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
  background: var(--bg-surface-hover);
  border: 2px dashed var(--border);
  border-radius: var(--radius-xl);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.empty-text {
  color: var(--text-muted);
  font-size: 1rem;
  margin: 0;
}

.empty-state-small {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-muted);
  font-size: 0.875rem;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
  background: var(--bg-overlay);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.qr-modal-container {
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
}

.qr-code-wrapper {
  padding: var(--spacing-xl);
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2xl);
  display: inline-block;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .classes-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
  }
  
  .schedule-sidebar {
    position: static;
  }

  .qr-code-wrapper {
    padding: var(--spacing-lg);
  }
}
</style>
