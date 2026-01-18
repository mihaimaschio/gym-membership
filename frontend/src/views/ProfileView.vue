<template>
  <div class="profile-page">
    <nav class="topbar">
      <div class="container flex justify-between items-center h-full">
        <div class="flex items-center gap-4">
          <h1 class="dashboard-title">My Profile</h1>
        </div>
        <div class="flex gap-3">
          <button @click="$router.push('/member')" class="btn btn-secondary btn-sm">
            Dashboard
          </button>
          <button @click="logout" class="btn btn-secondary btn-sm">
            Sign Out
          </button>
        </div>
      </div>
    </nav>

    <main class="container py-8">
      <div class="profile-layout">
        <!-- Profile Card -->
        <div class="profile-card card">
          <div class="profile-header">
            <div class="avatar-container">
              <img 
                :src="getAvatarUrl" 
                alt="Profile" 
                class="profile-avatar"
                @error="handleImageError"
              />
            </div>
            <div class="profile-info">
              <h2 class="profile-name">{{ member?.firstName }} {{ member?.lastName }}</h2>
              <p class="profile-email">{{ member?.email }}</p>
            </div>
            <button 
              v-if="!isEditing" 
              @click="startEditing" 
              class="btn btn-primary edit-button"
            >
              Edit Profile
            </button>
          </div>

          <!-- View Mode -->
          <div v-if="!isEditing" class="profile-details">
            <div class="detail-section">
              <div class="detail-item">
                <div class="detail-label">
                  Phone Number
                </div>
                <div class="detail-value">{{ member?.phoneNumber || 'Not set' }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">
                  Account Role
                </div>
                <div class="detail-value">
                  <span class="badge badge-info">{{ member?.role || 'MEMBER' }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Edit Mode -->
          <form v-else @submit.prevent="saveProfile" class="edit-form animate-fade-in">
            <div class="form-section">
              <div class="form-row">
                <div class="form-group">
                  <label class="label">First Name</label>
                  <input 
                    v-model="form.firstName" 
                    type="text" 
                    class="input-field" 
                    required 
                    minlength="2"
                    placeholder="John"
                  />
                </div>
                <div class="form-group">
                  <label class="label">Last Name</label>
                  <input 
                    v-model="form.lastName" 
                    type="text" 
                    class="input-field" 
                    required 
                    minlength="2"
                    placeholder="Doe"
                  />
                </div>
              </div>
              <div class="form-group">
                <label class="label">Phone Number</label>
                <input 
                  v-model="form.phoneNumber" 
                  type="tel" 
                  class="input-field" 
                  required
                  placeholder="+40 123 456 789"
                />
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="cancelEditing" class="btn btn-secondary">
                Cancel
              </button>
              <button type="submit" class="btn btn-primary" :disabled="isLoading">
                <span v-if="isLoading" class="loading-spinner"></span>
                <span v-else>Save Changes</span>
              </button>
            </div>
          </form>
        </div>

        <!-- Subscription Card -->
        <div class="subscription-card card">
          <div class="card-header">
            <div>
              <h2 class="card-title">Subscription Details</h2>
              <p class="card-subtitle">Your membership information</p>
            </div>
            <span :class="['badge', subscription?.active ? 'badge-success' : 'badge-danger']">
              {{ subscription?.active ? 'Active' : 'Inactive' }}
            </span>
          </div>
        
          <div v-if="subscription" class="subscription-details">
            <div class="subscription-stats">
              <div class="subscription-stat-item">
                <div class="stat-label-small">Plan Type</div>
                <div class="stat-value-primary">{{ formatSubscriptionType(subscription.type) }}</div>
              </div>
              <div class="subscription-stat-item">
                <div class="stat-label-small">Price</div>
                <div class="stat-value">{{ subscription.price }} lei</div>
              </div>
              <div class="subscription-stat-item">
                <div class="stat-label-small">Start Date</div>
                <div class="stat-value-muted">{{ formatDate(subscription.startDate) }}</div>
              </div>
              <div class="subscription-stat-item">
                <div class="stat-label-small">End Date</div>
                <div class="stat-value-muted">{{ formatDate(subscription.endDate) }}</div>
              </div>
            </div>
          </div>
          <div v-else class="empty-subscription">
            <p class="empty-text">You do not have an active subscription.</p>
            <button class="btn btn-primary mt-4" @click="$router.push('/member')">
              View Classes
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import memberService from '../services/memberService';
import subscriptionService from '../services/subscriptionService';

export default {
    name: 'ProfileView',
    setup() {
        const router = useRouter();
        const authStore = useAuthStore();
        const member = ref(null);
        const subscription = ref(null);
        const isEditing = ref(false);
        const isLoading = ref(false);

        const form = ref({
            firstName: '',
            lastName: '',
            phoneNumber: '',
            email: ''
        });

        const getAvatarUrl = computed(() => {
            const firstName = form.value.firstName || member.value?.firstName || 'User';
            const lastName = form.value.lastName || member.value?.lastName || '';
            const name = `${firstName} ${lastName}`.trim();
            return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&size=128`;
        });

        const loadData = async () => {
             try {
                const memberResponse = await memberService.getMemberByEmail(authStore.user.email);
                member.value = memberResponse.data;
                
                form.value = { 
                    ...member.value
                };

                if (member.value.id) {
                     try {
                        const subResponse = await subscriptionService.getSubscriptionByMemberId(member.value.id);
                        subscription.value = subResponse.data;
                     } catch (err) {
                        subscription.value = null;
                     }
                }
             } catch (error) {
                 console.error('Error loading profile:', error);
             }
        };

        const startEditing = () => {
            isEditing.value = true;
        };

        const cancelEditing = () => {
            isEditing.value = false;
            form.value = { 
                ...member.value
            };
        };

        const saveProfile = async () => {
            isLoading.value = true;
            try {
                const updatedMember = await memberService.updateMember(member.value.id, form.value);
                member.value = updatedMember.data;
                isEditing.value = false;
            } catch (error) {
                alert('Failed to update profile.');
                console.error(error);
            } finally {
                isLoading.value = false;
            }
        };

        const handleImageError = (e) => {
            e.target.src = getAvatarUrl.value; 
        };

        const logout = () => {
            authStore.logout();
            router.push('/login');
        };

        const formatSubscriptionType = (type) => type?.replace(/_/g, ' ') || type;
        const formatDate = (date) => {
             if (!date) return '-';
             return new Date(date).toLocaleDateString();
        };

        onMounted(() => {
            loadData();
        });

        return {
            member,
            subscription,
            form,
            isEditing,
            isLoading,
            getAvatarUrl,
            loadData,
            startEditing,
            cancelEditing,
            saveProfile,
            handleImageError,
            formatSubscriptionType,
            formatDate,
            logout
        };
    }
};
</script>

<style scoped>
.profile-page {
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

.profile-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-xl);
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 1024px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  position: relative;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-xl);
  border-bottom: 2px solid var(--border-light);
  position: relative;
}

.avatar-container {
  position: relative;
  margin-bottom: var(--spacing-lg);
}

.profile-avatar {
  width: 140px;
  height: 140px;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 4px solid var(--primary-light);
  box-shadow: var(--shadow-lg);
  transition: all var(--transition-slow);
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-xl);
}

.avatar-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 36px;
  height: 36px;
  background: var(--success);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
  border: 3px solid var(--bg-surface);
  box-shadow: var(--shadow-md);
}

.profile-info {
  margin-bottom: var(--spacing-lg);
}

.profile-name {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 var(--spacing-xs) 0;
}

.profile-email {
  font-size: 1rem;
  color: var(--text-muted);
  margin: 0;
}

.edit-button {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.profile-details {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.detail-item {
  padding: var(--spacing-lg);
  background: var(--bg-surface-hover);
  border-radius: var(--radius-lg);
  border: 2px solid var(--border-light);
  transition: all var(--transition-base);
}

.detail-item:hover {
  border-color: var(--primary);
  background: var(--bg-surface);
  transform: translateX(4px);
}

.detail-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: var(--spacing-sm);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.detail-icon {
  font-size: 1.125rem;
}

.detail-value {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-main);
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: flex-end;
  padding-top: var(--spacing-lg);
  border-top: 2px solid var(--border-light);
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

.subscription-details {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.subscription-stats {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.subscription-stat-item {
  padding: var(--spacing-lg);
  background: var(--bg-surface-hover);
  border-radius: var(--radius-lg);
  border: 2px solid var(--border-light);
  transition: all var(--transition-base);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subscription-stat-item:hover {
  border-color: var(--primary);
  background: var(--bg-surface);
  transform: translateX(4px);
}

.stat-label-small {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value-primary {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--primary);
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-main);
}

.stat-value-muted {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.empty-subscription {
  text-align: center;
  padding: var(--spacing-2xl);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

.empty-text {
  color: var(--text-muted);
  font-size: 1rem;
  margin-bottom: var(--spacing-lg);
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
