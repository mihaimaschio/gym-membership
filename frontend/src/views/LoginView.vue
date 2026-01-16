<template>
  <div class="login-page">
    <div class="card login-card animate-fade-in">
      <div class="header">
        <h1>🏋️ Gym Membership</h1>
        <p class="subtitle">Welcome back! Please access your account.</p>
      </div>

      <div class="tabs">
        <button 
          :class="['tab-btn', { active: !isRegister }]" 
          @click="isRegister = false"
        >
          Login
        </button>
        <button 
          :class="['tab-btn', { active: isRegister }]" 
          @click="isRegister = true"
        >
          Register
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="flex flex-col gap-4">
        <!-- Common Field: Email -->
        <div>
          <label class="label">Email Address</label>
          <input 
            type="email" 
            v-model="form.email" 
            required 
            pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$"
            placeholder="name@example.com"
            class="input-field"
          />
        </div>

        <!-- Common Field: Password -->
        <div>
          <label class="label">Password</label>
          <input 
            type="password" 
            v-model="form.password" 
            required 
            minlength="4"
            placeholder="••••••••"
            class="input-field"
          />
          <p v-if="isRegister" class="hint">At least 4 characters</p>
        </div>

        <!-- Register Only Fields -->
        <div v-if="isRegister" class="grid-2-col animate-fade-in">
          <div>
            <label class="label">First Name</label>
            <input 
              type="text" 
              v-model="form.firstName" 
              required 
              minlength="2"
              placeholder="John"
              class="input-field"
            />
          </div>
          <div>
            <label class="label">Last Name</label>
            <input 
              type="text" 
              v-model="form.lastName" 
              required 
              minlength="2"
              placeholder="Doe"
              class="input-field"
            />
          </div>
        </div>

        <div v-if="isRegister" class="animate-fade-in">
          <label class="label">Phone Number</label>
          <input 
            type="tel" 
            v-model="form.phoneNumber" 
            required 
            pattern="07[0-9]{8}"
            placeholder="0712345678"
            class="input-field"
          />
        </div>

        <div v-if="isRegister" class="animate-fade-in">
          <label class="label">Role</label>
          <select v-model="form.role" required class="input-field">
            <option value="MEMBER">Member</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>

        <button type="submit" class="btn btn-primary mt-4" :disabled="isLoading">
          {{ isRegister ? 'Create Account' : 'Sign In' }}
        </button>

        <div v-if="error" class="alert error animate-fade-in">{{ error }}</div>
        <div v-if="success" class="alert success animate-fade-in">{{ success }}</div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();
    const isRegister = ref(false);
    const error = ref('');
    const success = ref('');
    const isLoading = ref(false);
    const form = ref({
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phoneNumber: '',
      role: 'MEMBER'
    });

    const handleSubmit = async () => {
      error.value = '';
      success.value = '';
      isLoading.value = true;
      
      try {
        if (isRegister.value) {
          await authStore.register(form.value);
          success.value = 'Registration successful! Please login.';
          // Optional: automatically switch to login tab or wait for user
          setTimeout(() => {
            isRegister.value = false;
            success.value = '';
          }, 2000);
          form.value = { ...form.value, password: '' }; // Clear password only
        } else {
          const response = await authStore.login({
            email: form.value.email,
            password: form.value.password
          });
          
          if (response.role === 'ROLE_ADMIN') {
            router.push('/admin');
          } else {
            router.push('/member');
          }
        }
      } catch (err) {
        error.value = err.response?.data?.message || 'Authentication failed. Please check your credentials.';
      } finally {
        isLoading.value = false;
      }
    };

    return {
      isRegister,
      form,
      error,
      success,
      isLoading,
      handleSubmit
    };
  }
};
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 1rem;
  background-color: var(--bg-body);
}

.login-card {
  width: 100%;
  max-width: 480px;
  padding: 2.5rem;
  box-shadow: var(--shadow-lg);
}

.header {
  text-align: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 1.75rem;
  color: var(--primary);
  margin-bottom: 0.5rem;
}

.subtitle {
  color: var(--text-muted);
  font-size: 0.95rem;
}

.tabs {
  display: flex;
  background: var(--bg-body);
  padding: 0.25rem;
  border-radius: var(--radius-md);
  margin-bottom: 1.5rem;
}

.tab-btn {
  flex: 1;
  padding: 0.5rem;
  border: none;
  background: transparent;
  border-radius: calc(var(--radius-md) - 2px);
  color: var(--text-muted);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.tab-btn.active {
  background: white;
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-main);
  margin-bottom: 0.375rem;
}

.hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.grid-2-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.alert {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  text-align: center;
}

.alert.error {
  background-color: #fee2e2;
  color: #ef4444;
}

.alert.success {
  background-color: #d1fae5;
  color: #059669;
}
</style>
