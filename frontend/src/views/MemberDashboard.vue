<template>
  <div class="dashboard-layout">
    <nav class="topbar">
      <div class="container flex justify-between items-center h-full">
        <div class="flex items-center gap-4">
          <div class="brand-logo">🏋️</div>
          <h1 class="text-xl font-bold text-slate-800">Member Dashboard</h1>
        </div>
        <button @click="logout" class="btn btn-secondary text-sm">
          Sign Out
        </button>
      </div>
    </nav>

    <main class="container py-8 flex flex-col gap-8">
      <!-- Subscription Section -->
      <div class="card p-6">
        <div class="flex justify-between items-start mb-6 border-b border-slate-100 pb-4">
          <h2 class="text-lg font-bold text-slate-800">My Subscription</h2>
          <span :class="['badge', subscription?.active ? 'badge-success' : 'badge-danger']">
            {{ subscription?.active ? 'Active' : 'Expired / None' }}
          </span>
        </div>

        <div v-if="subscription">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="stat-box p-4 bg-slate-50 rounded-lg">
              <span class="text-xs text-slate-500 uppercase font-bold block mb-1">Plan Type</span>
              <span class="text-indigo-600 font-bold text-lg">{{ formatSubscriptionType(subscription.type) }}</span>
            </div>
            <div class="stat-box p-4 bg-slate-50 rounded-lg">
              <span class="text-xs text-slate-500 uppercase font-bold block mb-1">Price</span>
              <span class="text-slate-800 font-bold text-lg">{{ subscription.price }} lei</span>
            </div>
            <div class="stat-box p-4 bg-slate-50 rounded-lg">
              <span class="text-xs text-slate-500 uppercase font-bold block mb-1">Start Date</span>
              <span class="text-slate-800 font-medium">{{ formatDate(subscription.startDate) }}</span>
            </div>
            <div class="stat-box p-4 bg-slate-50 rounded-lg">
              <span class="text-xs text-slate-500 uppercase font-bold block mb-1">End Date</span>
              <span class="text-slate-800 font-medium">{{ formatDate(subscription.endDate) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="bg-yellow-50 border border-yellow-200 text-yellow-800 p-4 rounded-lg flex items-center gap-3">
          <span class="text-xl">⚠️</span>
          <div>
            <p class="font-bold">No Active Subscription</p>
            <p class="text-sm">You need an active subscription to enroll in classes. Please visit the reception.</p>
          </div>
        </div>
      </div>

      <div class="grid-layout gap-8">
        <!-- Main: Available Classes -->
        <div class="card p-6">
          <div class="mb-6">
            <h2 class="text-lg font-bold text-slate-800 mb-4">Available Classes</h2>
            <!-- Day Tabs -->
            <div class="flex gap-2 overflow-x-auto pb-2">
              <button 
                v-for="day in daysOfWeek" 
                :key="day"
                :class="['day-tab', { active: selectedDay === day }]"
                @click="selectedDay = day"
              >
                {{ day }}
              </button>
            </div>
          </div>

          <div v-if="filteredClasses.length > 0" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            <div 
              v-for="gymClass in filteredClasses" 
              :key="gymClass.id"
              class="border border-slate-200 rounded-lg p-5 hover:border-indigo-200 hover:shadow-sm transition-all flex flex-col justify-between h-full"
            >
              <div>
                <div class="flex justify-between items-start mb-2">
                  <h3 class="font-bold text-slate-800">{{ formatClassType(gymClass.type) }}</h3>
                  <span class="text-xs font-mono bg-slate-100 px-2 py-1 rounded">{{ gymClass.time }}</span>
                </div>
                
                <div class="mb-4">
                  <div class="flex justify-between text-xs text-slate-500 mb-1">
                    <span>Availability</span>
                    <span :class="gymClass.availableSpots > 0 ? 'text-emerald-600 font-bold' : 'text-red-500 font-bold'">
                      {{ gymClass.availableSpots > 0 ? gymClass.availableSpots + ' spots left' : 'Full' }}
                    </span>
                  </div>
                  <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                    <div 
                      class="bg-indigo-500 h-full rounded-full" 
                      :style="{ width: ((20 - gymClass.availableSpots) / 20 * 100) + '%' }"
                    ></div>
                  </div>
                </div>
              </div>

              <button 
                @click="enrollInClass(gymClass.id)" 
                class="btn w-full text-sm py-2"
                :class="hasActiveSubscription && gymClass.availableSpots > 0 ? 'btn-primary' : 'btn-secondary opacity-50 cursor-not-allowed'"
                :disabled="!hasActiveSubscription || gymClass.availableSpots === 0"
              >
                {{ !hasActiveSubscription ? 'Sub Required' : (gymClass.availableSpots === 0 ? 'Class Full' : 'Enroll Now') }}
              </button>
            </div>
          </div>
          
          <div v-else class="text-center py-12 text-slate-400 bg-slate-50 rounded-lg border border-dashed border-slate-200">
            <p>No classes available for {{ selectedDay }}</p>
          </div>
        </div>

        <!-- Sidebar: My Enrollments -->
        <div class="sidebar-col">
          <div class="card p-6 h-full">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <span>📅</span> My Schedule
            </h2>
            
            <div v-if="enrollments.length > 0" class="flex flex-col gap-3">
              <div 
                v-for="enrollment in enrollments" 
                :key="enrollment.id"
                class="p-3 bg-slate-50 rounded-lg border border-slate-100 relative group"
              >
                <div class="pr-8">
                  <strong class="block text-indigo-700 text-sm mb-1">{{ formatClassType(enrollment.gymClass.type) }}</strong>
                  <p class="text-xs text-slate-600">
                    {{ enrollment.gymClass.dayOfWeek }} • {{ enrollment.gymClass.time }}
                  </p>
                </div>
                <button 
                  @click="cancelEnrollment(enrollment.id)" 
                  class="absolute top-3 right-3 text-slate-400 hover:text-red-500 transition-colors"
                  title="Cancel Booking"
                >
                  ✕
                </button>
              </div>
            </div>
            <div v-else class="text-center py-8 text-slate-500 text-sm">
              <p>You haven't enrolled in any classes yet.</p>
            </div>
          </div>
        </div>
      </div>
    </main>
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

export default {
  name: 'MemberDashboard',
  components: {
    ChatAssistant
  },
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const member = ref(null);
    const subscription = ref(null);
    const availableClasses = ref([]);
    const enrollments = ref([]);
    const selectedDay = ref('MONDAY');

    const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];

    const hasActiveSubscription = computed(() => subscription.value?.active);

    const filteredClasses = computed(() => {
      // Assuming existing filtering logic is correct
      return availableClasses.value
        .filter(c => c.dayOfWeek === selectedDay.value)
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
      try {
        await classService.enrollMemberInClass(classId, member.value.id);
        await loadMemberData();
        await loadAvailableClasses();
        // Removed alert for better UX, maybe add a toast later?
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

    // Helper functions
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

    onMounted(() => {
      loadMemberData();
      loadAvailableClasses();
    });

    return {
      subscription,
      availableClasses,
      enrollments,
      daysOfWeek,
      selectedDay,
      filteredClasses,
      hasActiveSubscription,
      enrollInClass,
      cancelEnrollment,
      formatSubscriptionType,
      formatClassType,
      formatDate,
      logout
    };
  }
};
</script>

<style scoped>
.dashboard-layout {
  min-height: 100vh;
  background-color: var(--bg-body);
}

.topbar {
  background: white;
  height: 64px;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 40;
}

.brand-logo {
  width: 32px;
  height: 32px;
  background-color: var(--primary-light);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.grid-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
}

@media (max-width: 1024px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }
}

.day-tab {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  background: transparent;
  border: 1px solid transparent;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.day-tab:hover {
  background-color: var(--bg-body);
  color: var(--text-main);
}

.day-tab.active {
  background-color: var(--primary);
  color: white;
  box-shadow: var(--shadow-sm);
}

/* Utilities not in main.css yet or specific to this view */
.text-xs { font-size: 0.75rem; }
.text-sm { font-size: 0.875rem; }
.text-lg { font-size: 1.125rem; }
.text-xl { font-size: 1.25rem; }
.font-bold { font-weight: 700; }
.font-medium { font-weight: 500; }
.font-mono { font-family: monospace; }
.uppercase { text-transform: uppercase; }
.bg-yellow-50 { background-color: #fefce8; }
.border-yellow-200 { border-color: #fef08a; }
.text-yellow-800 { color: #854d0e; }
.bg-slate-50 { background-color: #f8fafc; }
.text-emerald-600 { color: #059669; }
.text-red-500 { color: #ef4444; }
.text-indigo-600 { color: #4f46e5; }
.text-indigo-700 { color: #4338ca; }
.text-slate-400 { color: #94a3b8; }
.text-slate-500 { color: #64748b; }
.text-slate-600 { color: #475569; }
.text-slate-800 { color: #1e293b; }
.p-4 { padding: 1rem; }
.p-5 { padding: 1.25rem; }
.p-6 { padding: 1.5rem; }
.py-8 { padding-top: 2rem; padding-bottom: 2rem; }
.py-2 { padding-top: 0.5rem; padding-bottom: 0.5rem; }
.px-2 { padding-left: 0.5rem; padding-right: 0.5rem; }
.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-6 { margin-bottom: 1.5rem; }
.block { display: block; }
.w-full { width: 100%; }
.h-full { height: 100%; }
.rounded { border-radius: 0.25rem; }
.rounded-lg { border-radius: 0.5rem; }
.rounded-full { border-radius: 9999px; }
.border { border-width: 1px; }
.border-b { border-bottom-width: 1px; }
.border-dashed { border-style: dashed; }
.border-slate-100 { border-color: #f1f5f9; }
.border-slate-200 { border-color: #e2e8f0; }
.opacity-50 { opacity: 0.5; }
.cursor-not-allowed { cursor: not-allowed; }
.overflow-hidden { overflow: hidden; }
.overflow-x-auto { overflow-x: auto; }
.relative { position: relative; }
.absolute { position: absolute; }
.top-3 { top: 0.75rem; }
.right-3 { right: 0.75rem; }
</style>
