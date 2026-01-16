<template>
  <div class="dashboard-layout">
    <nav class="topbar">
      <div class="container flex justify-between items-center h-full">
        <div class="flex items-center gap-4">
          <div class="brand-logo">🏋️</div>
          <h1 class="text-xl font-bold text-slate-800">Admin Dashboard</h1>
        </div>
        <button @click="logout" class="btn btn-secondary text-sm">
          Sign Out
        </button>
      </div>
    </nav>

    <main class="container py-8">
      <!-- Stats Grid -->
      <div class="stats-grid mb-8">
        <div class="card stat-card">
          <div class="stat-icon bg-indigo-100 text-indigo-600">👥</div>
          <div>
            <p class="stat-label">Total Members</p>
            <p class="stat-value">{{ members.length }}</p>
          </div>
        </div>
        <div class="card stat-card">
          <div class="stat-icon bg-emerald-100 text-emerald-600">💳</div>
          <div>
            <p class="stat-label">Active Subs</p>
            <p class="stat-value">{{ activeSubscriptions.length }}</p>
          </div>
        </div>
        <div class="card stat-card">
          <div class="stat-icon bg-blue-100 text-blue-600">🧘</div>
          <div>
            <p class="stat-label">Total Classes</p>
            <p class="stat-value">{{ classes.length }}</p>
          </div>
        </div>
        <div class="card stat-card">
          <div class="stat-icon bg-orange-100 text-orange-600">📝</div>
          <div>
            <p class="stat-label">Enrollments</p>
            <p class="stat-value">{{ totalEnrollments }}</p>
          </div>
        </div>
      </div>

      <div class="grid-layout gap-8">
        <!-- Main Content Column -->
        <div class="flex flex-col gap-8">
          
          <!-- Members Section -->
          <div class="card">
            <div class="card-header border-b border-slate-100 p-6">
              <h2 class="text-lg font-bold">Members Directory</h2>
            </div>
            
            <div v-if="errorMessage" class="p-4 m-6 bg-red-50 text-red-600 rounded-md">
              {{ errorMessage }}
            </div>

            <div class="table-container">
              <table class="w-full text-left">
                <thead class="bg-slate-50 text-slate-500 uppercase text-xs font-semibold">
                  <tr>
                    <th class="p-4">Name</th>
                    <th class="p-4">Contact</th>
                    <th class="p-4">Subscription</th>
                    <th class="p-4">Status</th>
                    <th class="p-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="member in members" :key="member.id" class="hover:bg-slate-50/50 transition-colors">
                    <td class="p-4 font-medium">{{ member.firstName }} {{ member.lastName }}</td>
                    <td class="p-4 text-sm text-slate-500">
                      <div>{{ member.email }}</div>
                      <div>{{ member.phoneNumber }}</div>
                    </td>
                    <td class="p-4 text-sm">{{ member.subscription?.type || '-' }}</td>
                    <td class="p-4">
                      <span :class="['badge', member.subscription?.active ? 'badge-success' : 'badge-danger']">
                        {{ member.subscription?.active ? 'Active' : 'Inactive' }}
                      </span>
                    </td>
                    <td class="p-4 text-right">
                      <div class="flex justify-end gap-2">
                        <button 
                          v-if="!member.subscription?.active"
                          @click="openSubscriptionModal(member)" 
                          class="btn btn-secondary text-xs py-1 px-3"
                        >
                          Assign
                        </button>
                        <button 
                          v-if="member.subscription?.active" 
                          @click="deleteSubscription(member.subscription.id)" 
                          class="btn btn-danger text-xs py-1 px-3"
                        >
                          Cancel
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Classes Section -->
          <div class="card">
            <div class="card-header border-b border-slate-100 p-6 flex justify-between items-center">
              <h2 class="text-lg font-bold">Class Schedule</h2>
              <button @click="showCreateClass = !showCreateClass" class="btn btn-primary text-sm">
                {{ showCreateClass ? 'Cancel' : 'Add New Class' }}
              </button>
            </div>

            <!-- Create Class Form -->
            <div v-if="showCreateClass" class="p-6 bg-slate-50 border-b border-slate-100 animate-fade-in">
              <h3 class="text-sm font-semibold mb-4 text-slate-500 uppercase">Create New Class</h3>
              <form @submit.prevent="createClass" class="flex flex-wrap gap-4 items-end">
                <div class="w-full sm:w-auto flex-1">
                  <label class="label text-xs mb-1 block">Type</label>
                  <select v-model="newClass.type" required class="input-field py-2">
                    <option value="YOGA">Yoga</option>
                    <option value="TRX">TRX</option>
                    <option value="CIRCUIT">Circuit</option>
                    <option value="INTERVAL_TRAINING">Interval</option>
                    <option value="KICK_BOX">Kick Box</option>
                  </select>
                </div>
                <div class="w-full sm:w-auto flex-1">
                  <label class="label text-xs mb-1 block">Day</label>
                  <select v-model="newClass.dayOfWeek" required class="input-field py-2">
                    <option value="MONDAY">Monday</option>
                    <option value="TUESDAY">Tuesday</option>
                    <option value="WEDNESDAY">Wednesday</option>
                    <option value="THURSDAY">Thursday</option>
                    <option value="FRIDAY">Friday</option>
                  </select>
                </div>
                <div class="w-full sm:w-auto flex-1">
                  <label class="label text-xs mb-1 block">Time</label>
                  <input type="time" v-model="newClass.time" required class="input-field py-2" />
                </div>
                <button type="submit" class="btn btn-primary">Save Class</button>
              </form>
            </div>

            <div class="p-6">
              <!-- Day Tabs -->
              <div class="flex gap-2 mb-6 overflow-x-auto pb-2">
                <button 
                  v-for="day in daysOfWeek" 
                  :key="day"
                  :class="['day-tab', { active: selectedDay === day }]"
                  @click="selectedDay = day"
                >
                  {{ day }}
                </button>
              </div>

              <!-- Classes Grid -->
              <div v-if="filteredClasses.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div v-for="gymClass in filteredClasses" :key="gymClass.id" class="class-item border border-slate-200 rounded-lg p-4 hover:border-indigo-200 transition-colors">
                  <div class="flex justify-between items-start mb-2">
                    <h3 class="font-bold text-slate-800">{{ gymClass.type }}</h3>
                    <span class="text-xs bg-slate-100 px-2 py-1 rounded text-slate-600">{{ gymClass.time }}</span>
                  </div>
                  
                  <div class="mb-3">
                    <div class="flex justify-between text-xs text-slate-500 mb-1">
                      <span>Capacity</span>
                      <span>{{ gymClass.enrollmentCount || 0 }} / {{ gymClass.maxCapacity }}</span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                      <div 
                        class="bg-indigo-500 h-full rounded-full" 
                        :style="{ width: ((gymClass.enrollmentCount || 0) / gymClass.maxCapacity * 100) + '%' }"
                      ></div>
                    </div>
                  </div>

                  <button @click="deleteClass(gymClass.id)" class="text-red-500 text-xs font-medium hover:text-red-700">
                    Remove Class
                  </button>
                </div>
              </div>
              <div v-else class="text-center py-12 text-slate-400">
                <p>No classes scheduled for {{ selectedDay }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar / Secondary Content -->
        <div class="sidebar-col flex flex-col gap-6">
          <div class="card p-6">
            <h3 class="font-bold text-slate-800 mb-4">Subscription Overview</h3>
            <div class="space-y-4">
              <div class="flex justify-between items-center p-3 bg-slate-50 rounded-lg">
                <span class="text-sm text-slate-600">Full Monthly</span>
                <span class="font-bold text-indigo-600">{{ countSubscriptionType('FULL_MONTHLY') }}</span>
              </div>
              <div class="flex justify-between items-center p-3 bg-slate-50 rounded-lg">
                <span class="text-sm text-slate-600">Six Months</span>
                <span class="font-bold text-indigo-600">{{ countSubscriptionType('SIX_MONTHS') }}</span>
              </div>
              <div class="flex justify-between items-center p-3 bg-slate-50 rounded-lg">
                <span class="text-sm text-slate-600">One Year</span>
                <span class="font-bold text-indigo-600">{{ countSubscriptionType('ONE_YEAR') }}</span>
              </div>
              <div class="flex justify-between items-center p-3 bg-slate-50 rounded-lg">
                <span class="text-sm text-slate-600">Student</span>
                <span class="font-bold text-indigo-600">{{ countSubscriptionType('STUDENT_MONTHLY') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal -->
    <div v-if="showSubscriptionModal" class="modal-overlay animate-fade-in">
      <div class="modal-container">
        <div class="modal-header">
          <div>
            <h3 class="modal-title">Assign Subscription</h3>
            <p class="modal-subtitle">Select a plan for <span class="font-semibold">{{ selectedMember?.firstName }} {{ selectedMember?.lastName }}</span></p>
          </div>
          <button @click="showSubscriptionModal = false" class="close-btn">
            <span class="sr-only">Close</span>
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="subscription-grid">
            <div 
              v-for="type in subscriptionTypes" 
              :key="type.name"
              class="sub-card group"
              @click="assignSubscription(type.name)"
            >
              <div class="sub-card-content">
                <div>
                  <h4 class="sub-card-title">{{ type.label }}</h4>
                  <p class="sub-card-desc">
                    {{ type.name === 'ONE_DAY' ? '24 Hours Access' : '' }}
                    {{ type.name === 'FULL_MONTHLY' ? 'All Access • 30 Days' : '' }}
                    {{ type.name === 'SIX_MONTHS' ? 'Best Value • 6 Months' : '' }}
                    {{ type.name === 'ONE_YEAR' ? 'Premium • 12 Months' : '' }}
                    {{ type.name === 'STUDENT_MONTHLY' ? 'Student ID Reqd' : '' }}
                  </p>
                </div>
                
                <div class="sub-card-footer">
                  <div class="sub-price">
                    {{ type.price }} <span class="sub-currency">lei</span>
                  </div>
                  <div class="sub-icon">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showSubscriptionModal = false" class="btn btn-secondary">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// No changes to logic
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import memberService from '../services/memberService';
import subscriptionService from '../services/subscriptionService';
import classService from '../services/classService';

export default {
  name: 'AdminDashboard',
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const members = ref([]);
    const activeSubscriptions = ref([]);
    const classes = ref([]);
    const showCreateClass = ref(false);
    const newClass = ref({
      type: 'YOGA',
      dayOfWeek: 'MONDAY',
      time: '10:00'
    });
    const showSubscriptionModal = ref(false);
    const selectedMember = ref(null);
    const isLoading = ref(false);
    const errorMessage = ref('');
    const selectedDay = ref('MONDAY');
    const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];

    const subscriptionTypes = [
      { name: 'ONE_DAY', label: 'One Day Pass', price: 30 },
      { name: 'FULL_MONTHLY', label: 'Full Monthly', price: 200 },
      { name: 'SIX_MONTHS', label: 'Six Months', price: 1000 },
      { name: 'ONE_YEAR', label: 'One Year Access', price: 1800 },
      { name: 'STUDENT_MONTHLY', label: 'Student Monthly', price: 150 }
    ];

    const totalEnrollments = computed(() => {
      return classes.value.reduce((sum, c) => sum + (c.enrollmentCount || 0), 0);
    });

    const filteredClasses = computed(() => {
      return classes.value
        .filter(c => c.dayOfWeek === selectedDay.value)
        .sort((a, b) => a.time.localeCompare(b.time));
    });

    const loadData = async () => {
      isLoading.value = true;
      errorMessage.value = '';
      try {
        const [membersRes, subsRes, classesRes] = await Promise.all([
          memberService.getAllMembers(),
          subscriptionService.getActiveSubscriptions(),
          classService.getAllClasses()
        ]);
        
        members.value = Array.isArray(membersRes.data) ? membersRes.data : [];
        activeSubscriptions.value = Array.isArray(subsRes.data) ? subsRes.data : [];
        classes.value = Array.isArray(classesRes.data) ? classesRes.data : [];
      } catch (error) {
        console.error('Error loading data:', error);
        errorMessage.value = 'Failed to load data. Please try again.';
      } finally {
        isLoading.value = false;
      }
    };

    const openSubscriptionModal = (member) => {
      selectedMember.value = member;
      showSubscriptionModal.value = true;
    };

    const assignSubscription = async (type) => {
      try {
        await subscriptionService.createSubscription(selectedMember.value.id, type);
        showSubscriptionModal.value = false;
        selectedMember.value = null;
        await loadData();
        // Removed alert for cleaner UX, could add toast
      } catch (error) {
        alert('Error assigning subscription');
      }
    };

    const createClass = async () => {
      try {
        await classService.createClass(
          newClass.value.type,
          newClass.value.dayOfWeek,
          newClass.value.time
        );
        showCreateClass.value = false;
        newClass.value = { type: 'YOGA', dayOfWeek: 'MONDAY', time: '10:00' };
        await loadData();
      } catch (error) {
        alert('Error creating class');
      }
    };

    const deleteClass = async (id) => {
      if (!confirm('Are you sure you want to delete this class?')) return;
      try {
        await classService.deleteClass(id);
        await loadData();
      } catch (error) {
        alert('Error deleting class');
      }
    };

    const deleteSubscription = async (id) => {
      if (!confirm('Are you sure you want to delete this subscription?')) return;
      try {
        await subscriptionService.deleteSubscription(id);
        await loadData();
      } catch (error) {
        alert('Error deleting subscription');
      }
    };

    const countSubscriptionType = (type) => {
      if (!Array.isArray(activeSubscriptions.value)) return 0;
      return activeSubscriptions.value.filter(s => s.type === type).length;
    };

    const logout = () => {
      authStore.logout();
      router.push('/login');
    };

    onMounted(loadData);

    return {
      members,
      activeSubscriptions,
      classes,
      showCreateClass,
      newClass,
      totalEnrollments,
      createClass,
      openSubscriptionModal,
      assignSubscription,
      showSubscriptionModal,
      selectedMember,
      subscriptionTypes,
      countSubscriptionType,
      logout,
      isLoading,
      errorMessage,
      deleteClass,
      deleteSubscription,
      selectedDay,
      daysOfWeek,
      filteredClasses
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.stat-label {
  color: var(--text-muted);
  font-size: 0.875rem;
  font-weight: 500;
}

.stat-value {
  color: var(--text-main);
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.grid-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
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

/* Custom quick styles for table */
td {
  vertical-align: middle;
}

/* Tailwind-ish utility for quick alignment not in main.css */
.text-right { text-align: right; }
.text-left { text-align: left; }
.text-xs { font-size: 0.75rem; }
.text-sm { font-size: 0.875rem; }
.text-lg { font-size: 1.125rem; }
.text-xl { font-size: 1.25rem; }
.font-medium { font-weight: 500; }
.font-bold { font-weight: 700; }
.uppercase { text-transform: uppercase; }
.py-8 { padding-top: 2rem; padding-bottom: 2rem; }
.p-4 { padding: 1rem; }
.p-6 { padding: 1.5rem; }
.px-2 { padding-left: 0.5rem; padding-right: 0.5rem; }
.py-1 { padding-top: 0.25rem; padding-bottom: 0.25rem; }
.mb-8 { margin-bottom: 2rem; }
.mb-4 { margin-bottom: 1rem; }
.w-full { width: 100%; }
.h-full { height: 100%; }
.border-b { border-bottom-width: 1px; }
.rounded { border-radius: 0.25rem; }
.rounded-md { border-radius: 0.375rem; }
.rounded-lg { border-radius: 0.5rem; }
.rounded-full { border-radius: 9999px; }
.bg-slate-50 { background-color: #f8fafc; }
.bg-indigo-100 { background-color: #e0e7ff; }
.bg-indigo-50 { background-color: #eef2ff; }
.bg-emerald-100 { background-color: #d1fae5; }
.bg-blue-100 { background-color: #dbeafe; }
.bg-orange-100 { background-color: #ffedd5; }
.text-slate-500 { color: #64748b; }
.text-slate-600 { color: #475569; }
.text-slate-800 { color: #1e293b; }
.text-indigo-600 { color: #4f46e5; }
.text-emerald-600 { color: #059669; }
.text-blue-600 { color: #2563eb; }
.text-orange-600 { color: #ea580c; }
.text-red-500 { color: #ef4444; }
.divide-y > :not([hidden]) ~ :not([hidden]) { border-top-width: 1px; }
.divide-slate-100 > :not([hidden]) ~ :not([hidden]) { border-color: #f1f5f9; }
.fixed { position: fixed; }
.inset-0 { top: 0; right: 0; bottom: 0; left: 0; }
.z-50 { z-index: 50; }
.backdrop-blur-sm { backdrop-filter: blur(4px); }
.bg-slate-900\/50 { background-color: rgb(15 23 42 / 0.5); }
.overflow-hidden { overflow: hidden; }

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background-color: rgba(15, 23, 42, 0.5); /* Slate 900 / 0.5 */
  backdrop-filter: blur(4px);
}

.modal-container {
  background-color: white;
  border-radius: 1rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 100%;
  max-width: 42rem; /* 2xl */
  overflow: hidden;
  transform: scale(1);
  transition: all 0.2s;
}

.modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--border);
  background-color: #f8fafc; /* Slate 50 */
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b; /* Slate 800 */
}

.modal-subtitle {
  color: var(--text-muted);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.close-btn {
  color: #94a3b8;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  transition: color 0.1s;
}

.close-btn:hover {
  color: #475569;
}

.modal-body {
  padding: 2rem;
}

.subscription-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 1rem;
}

@media (min-width: 768px) {
  .subscription-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.sub-card {
  position: relative;
  cursor: pointer;
  border-radius: 0.75rem;
  border: 2px solid #e2e8f0; /* slate 200 */
  padding: 1.25rem;
  transition: all 0.2s ease-in-out;
  background-color: white;
}

.sub-card:hover {
  border-color: var(--primary);
  background-color: #f8fafc;
}

.sub-card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
}

.sub-card-title {
  font-weight: 700;
  color: #1e293b;
  font-size: 1.125rem; /* lg */
  margin-bottom: 0.25rem;
  transition: color 0.2s;
}

.sub-card:hover .sub-card-title {
  color: var(--primary-hover);
}

.sub-card-desc {
  font-size: 0.75rem; /* xs */
  font-weight: 500;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.025em;
  margin-bottom: 0.75rem;
}

.sub-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 0.5rem;
}

.sub-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a; /* Slate 900 */
  transition: color 0.2s;
}

.sub-card:hover .sub-price {
  color: var(--primary);
}

.sub-currency {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-muted);
  margin-left: 0.125rem;
}

.sub-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 9999px;
  background-color: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.sub-card:hover .sub-icon {
  background-color: var(--primary);
  color: white;
}

.modal-footer {
  padding: 1rem 2rem;
  background-color: #f8fafc;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
}

/* Icons */
.w-6 { width: 1.5rem; }
.h-6 { height: 1.5rem; }
.w-5 { width: 1.25rem; }
.h-5 { height: 1.25rem; }
</style>

