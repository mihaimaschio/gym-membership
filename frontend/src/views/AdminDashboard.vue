<template>
  <div class="dashboard-layout">
    <nav class="topbar">
      <div class="container flex justify-between items-center h-full">
        <div class="flex items-center gap-4">
          <h1 class="dashboard-title">Admin Dashboard</h1>
        </div>
        <button @click="logout" class="btn btn-secondary btn-sm">
          Sign Out
        </button>
      </div>
    </nav>

    <main class="container py-8">
      <!-- Stats Overview -->
      <div class="stats-overview">
        <div class="stat-card card">
          <div class="stat-content">
            <div class="stat-label">Total Members</div>
            <div class="stat-value">{{ members.length }}</div>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-content">
            <div class="stat-label">Active Subscriptions</div>
            <div class="stat-value">{{ activeSubscriptions.length }}</div>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-content">
            <div class="stat-label">Total Classes</div>
            <div class="stat-value">{{ classes.length }}</div>
          </div>
        </div>
        <div class="stat-card card">
          <div class="stat-content">
            <div class="stat-label">Total Enrollments</div>
            <div class="stat-value">{{ totalEnrollments }}</div>
          </div>
        </div>
      </div>

      <div class="main-content-layout">
        <!-- Left Column -->
        <div class="main-column">
          <!-- Members Section -->
          <div class="card members-section">
            <div class="card-header">
              <div>
                <h2 class="card-title">Members Directory</h2>
                <p class="card-subtitle">Manage member subscriptions</p>
              </div>
            </div>
            
            <div v-if="errorMessage" class="alert error">
              {{ errorMessage }}
            </div>

            <div class="table-wrapper">
              <table class="modern-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Contact</th>
                    <th>Subscription</th>
                    <th>Status</th>
                    <th class="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="member in members" :key="member.id" class="table-row">
                    <td class="member-name">
                      <div class="name-text">{{ member.firstName }} {{ member.lastName }}</div>
                    </td>
                    <td class="member-contact">
                      <div class="contact-item">{{ member.email }}</div>
                      <div class="contact-item text-muted">{{ member.phoneNumber }}</div>
                    </td>
                    <td>
                      <span class="subscription-type">{{ member.subscription?.type || '-' }}</span>
                    </td>
                    <td>
                      <span :class="['badge', member.subscription?.active ? 'badge-success' : 'badge-danger']">
                        {{ member.subscription?.active ? 'Active' : 'Inactive' }}
                      </span>
                    </td>
                    <td class="text-right">
                      <div class="action-buttons">
                        <button 
                          v-if="!member.subscription?.active"
                          @click="openSubscriptionModal(member)" 
                          class="btn btn-primary btn-sm"
                        >
                          Assign
                        </button>
                        <button 
                          v-if="member.subscription?.active" 
                          @click="deleteSubscription(member.subscription.id)" 
                          class="btn btn-danger btn-sm"
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
          <div class="card classes-section">
            <div class="card-header">
              <div>
                <h2 class="card-title">Class Schedule</h2>
                <p class="card-subtitle">Manage gym classes</p>
              </div>
              <button @click="showCreateClass = !showCreateClass" class="btn btn-primary">
                {{ showCreateClass ? 'Cancel' : '+ Add Class' }}
              </button>
            </div>

            <!-- Create Class Form -->
            <div v-if="showCreateClass" class="create-class-form animate-fade-in">
              <h3 class="form-title">Create New Class</h3>
              <form @submit.prevent="createClass" class="form-grid">
                <div class="form-group">
                  <label class="label">Type</label>
                  <select v-model="newClass.type" required class="input-field">
                    <option value="YOGA">Yoga</option>
                    <option value="TRX">TRX</option>
                    <option value="CIRCUIT">Circuit</option>
                    <option value="INTERVAL_TRAINING">Interval</option>
                    <option value="KICK_BOX">Kick Box</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="label">Day</label>
                  <select v-model="newClass.dayOfWeek" required class="input-field">
                    <option value="MONDAY">Monday</option>
                    <option value="TUESDAY">Tuesday</option>
                    <option value="WEDNESDAY">Wednesday</option>
                    <option value="THURSDAY">Thursday</option>
                    <option value="FRIDAY">Friday</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="label">Time</label>
                  <input type="time" v-model="newClass.time" required class="input-field" />
                </div>
                <div class="form-group form-submit">
                  <button type="submit" class="btn btn-primary w-full">Create Class</button>
                </div>
              </form>
            </div>

            <!-- Day Tabs -->
            <div class="day-tabs-container">
              <div class="day-tabs">
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

            <!-- Classes Grid -->
            <div v-if="filteredClasses.length > 0" class="admin-classes-grid">
              <div v-for="gymClass in filteredClasses" :key="gymClass.id" class="admin-class-card">
                <div class="class-header">
                  <h3 class="class-name">{{ gymClass.type }}</h3>
                  <span class="class-time">{{ gymClass.time }}</span>
                </div>
                
                <div class="class-capacity">
                  <div class="capacity-info">
                    <span class="capacity-label">Capacity</span>
                    <span class="capacity-value">{{ gymClass.enrollmentCount || 0 }} / {{ gymClass.maxCapacity }}</span>
                  </div>
                  <div class="progress-bar">
                    <div 
                      class="progress-fill" 
                      :style="{ width: ((gymClass.enrollmentCount || 0) / gymClass.maxCapacity * 100) + '%' }"
                    ></div>
                  </div>
                </div>

                <button @click="deleteClass(gymClass.id)" class="btn btn-danger btn-sm w-full">
                  Remove Class
                </button>
              </div>
            </div>
            <div v-else class="empty-state">
              <p class="empty-text">No classes scheduled for {{ selectedDay }}</p>
            </div>
          </div>
        </div>

        <!-- Right Sidebar -->
        <div class="sidebar-column">
          <div class="card subscription-overview">
            <div class="card-header">
              <div>
                <h2 class="card-title">Subscription Overview</h2>
                <p class="card-subtitle">Active plans breakdown</p>
              </div>
            </div>
            
            <div class="subscription-stats">
              <div class="subscription-stat-item">
                <div class="stat-label-small">Full Monthly</div>
                <div class="stat-value-small">{{ countSubscriptionType('FULL_MONTHLY') }}</div>
              </div>
              <div class="subscription-stat-item">
                <div class="stat-label-small">Six Months</div>
                <div class="stat-value-small">{{ countSubscriptionType('SIX_MONTHS') }}</div>
              </div>
              <div class="subscription-stat-item">
                <div class="stat-label-small">One Year</div>
                <div class="stat-value-small">{{ countSubscriptionType('ONE_YEAR') }}</div>
              </div>
              <div class="subscription-stat-item">
                <div class="stat-label-small">Student</div>
                <div class="stat-value-small">{{ countSubscriptionType('STUDENT_MONTHLY') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Subscription Modal -->
    <div v-if="showSubscriptionModal" class="modal-overlay" @click.self="showSubscriptionModal = false">
      <div class="modal-container animate-scale-in">
        <div class="modal-header">
          <div>
            <h3 class="modal-title">Assign Subscription</h3>
            <p class="modal-subtitle">
              Select a plan for <span class="member-name-highlight">{{ selectedMember?.firstName }} {{ selectedMember?.lastName }}</span>
            </p>
          </div>
          <button @click="showSubscriptionModal = false" class="close-button">
            <span>×</span>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="subscription-grid">
            <div 
              v-for="type in subscriptionTypes" 
              :key="type.name"
              class="subscription-card-modal"
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
                    {{ type.name === 'STUDENT_MONTHLY' ? 'Student ID Required' : '' }}
                  </p>
                </div>
                
                <div class="sub-card-footer">
                  <div class="sub-price">
                    {{ type.price }} <span class="sub-currency">lei</span>
                  </div>
                  <div class="sub-icon">
                    <span>+</span>
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

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  transition: all var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  flex-shrink: 0;
}

.stat-icon-members {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
}

.stat-icon-subs {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
}

.stat-icon-classes {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.stat-icon-enrollments {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: var(--spacing-xs);
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-main);
  line-height: 1;
}

.main-content-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--spacing-xl);
}

@media (max-width: 1024px) {
  .main-content-layout {
    grid-template-columns: 1fr;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-xl);
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

.table-wrapper {
  overflow-x: auto;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.modern-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-surface);
}

.modern-table thead {
  background: var(--bg-surface-hover);
}

.modern-table th {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.modern-table td {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--border-light);
  vertical-align: middle;
}

.table-row {
  transition: all var(--transition-base);
}

.table-row:hover {
  background: var(--bg-surface-hover);
}

.member-name {
  font-weight: 600;
  color: var(--text-main);
}

.member-contact {
  font-size: 0.875rem;
}

.contact-item {
  margin-bottom: var(--spacing-xs);
}

.subscription-type {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
}

.create-class-form {
  background: var(--bg-surface-hover);
  padding: var(--spacing-xl);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-xl);
  border: 2px dashed var(--border);
}

.form-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: var(--spacing-lg);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
  align-items: end;
}

.form-submit {
  grid-column: 1 / -1;
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
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--radius-lg);
  border: 2px solid var(--border);
  background: var(--bg-surface);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  transition: all var(--transition-base);
  white-space: nowrap;
}

.day-tab:hover {
  border-color: var(--primary);
  background: var(--primary-light);
  color: var(--primary);
}

.day-tab.active {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  color: white;
  border-color: var(--primary);
  box-shadow: var(--shadow-primary);
}

.admin-classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.admin-class-card {
  background: var(--bg-surface);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
  transition: all var(--transition-slow);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.admin-class-card:hover {
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

.class-capacity {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.capacity-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.capacity-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
}

.capacity-value {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--text-main);
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

.sidebar-column {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.subscription-overview {
  position: relative;
}

.subscription-stats {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.subscription-stat-item {
  padding: var(--spacing-md);
  background: var(--bg-surface-hover);
  border-radius: var(--radius-lg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all var(--transition-base);
}

.subscription-stat-item:hover {
  background: var(--bg-surface);
  transform: translateX(4px);
}

.stat-label-small {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.stat-value-small {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
  background: var(--bg-overlay);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.modal-container {
  background: var(--bg-surface);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  width: 100%;
  max-width: 48rem;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: var(--spacing-xl) var(--spacing-2xl);
  border-bottom: 1px solid var(--border);
  background: var(--bg-surface-hover);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 var(--spacing-xs) 0;
}

.modal-subtitle {
  color: var(--text-muted);
  font-size: 0.9375rem;
  margin: 0;
}

.member-name-highlight {
  font-weight: 700;
  color: var(--primary);
}

.close-button {
  width: 36px;
  height: 36px;
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

.close-button:hover {
  background: var(--danger);
  color: white;
  transform: scale(1.1);
}

.modal-body {
  padding: var(--spacing-2xl);
  overflow-y: auto;
  flex: 1;
}

.subscription-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.subscription-card-modal {
  cursor: pointer;
  border-radius: var(--radius-xl);
  border: 2px solid var(--border);
  padding: var(--spacing-lg);
  transition: all var(--transition-base);
  background: var(--bg-surface);
}

.subscription-card-modal:hover {
  border-color: var(--primary);
  background: var(--bg-surface-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.sub-card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.sub-card-title {
  font-weight: 700;
  color: var(--text-main);
  font-size: 1.125rem;
  margin: 0 0 var(--spacing-xs) 0;
}

.sub-card-desc {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.025em;
  margin: 0;
}

.sub-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: var(--spacing-md);
}

.sub-price {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--text-main);
}

.sub-currency {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-muted);
  margin-left: var(--spacing-xs);
}

.sub-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--bg-surface-hover);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
  font-size: 1.5rem;
  font-weight: 300;
}

.subscription-card-modal:hover .sub-icon {
  background: var(--primary);
  color: white;
}

.modal-footer {
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--bg-surface-hover);
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
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

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }
  
  .admin-classes-grid {
    grid-template-columns: 1fr;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .subscription-grid {
    grid-template-columns: 1fr;
  }
}
</style>
