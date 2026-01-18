<template>
  <div class="login-page">
    <div class="login-background">
      <div class="gradient-overlay"></div>
    </div>
    
    <div class="login-container">
      <div class="login-card animate-scale-in">
        <div class="login-header">
          <div class="logo-container">
            <h1 class="logo-text">GymFit</h1>
          </div>
          <p class="login-subtitle">
            {{ isForgotPassword ? 'Reset your password' : (isRegister ? 'Create your account' : 'Welcome back!') }}
          </p>
        </div>

        <form v-if="!isForgotPassword" @submit.prevent="handleSubmit" class="login-form">
          <div class="form-group">
            <label class="label">Email Address</label>
            <input 
              type="email" 
              v-model="form.email" 
              required 
              class="input-field"
              placeholder="Enter your email"
            />
          </div>

          <div class="form-group">
            <label class="label">Password</label>
            <input 
              type="password" 
              v-model="form.password" 
              required 
              minlength="4"
              class="input-field"
              placeholder="Enter your password"
            />
            <div v-if="!isRegister" class="forgot-password-link">
              <a href="#" @click.prevent="isForgotPassword = true; success = ''; error = ''" class="link-text">
                Forgot Password?
              </a>
            </div>
          </div>

          <div v-if="isRegister" class="register-fields animate-fade-in">
            <div class="form-row">
              <div class="form-group">
                <label class="label">First Name</label>
                <input 
                  type="text" 
                  v-model="form.firstName" 
                  required 
                  minlength="2"
                  class="input-field"
                />
              </div>
              <div class="form-group">
                <label class="label">Last Name</label>
                <input 
                  type="text" 
                  v-model="form.lastName" 
                  required 
                  minlength="2"
                  class="input-field"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="label">Phone Number</label>
              <div class="phone-input-wrapper">
                <select v-model="selectedCountryCode" class="country-select">
                  <option v-for="code in countryCodes" :key="code.value" :value="code.value">
                    {{ code.label }} ({{ code.value }})
                  </option>
                </select>
                <input 
                  type="tel" 
                  v-model="form.phoneNumber" 
                  required 
                  pattern="[0-9]{8,15}"
                  class="input-field phone-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="label">Role</label>
              <select v-model="form.role" required class="input-field">
                <option value="MEMBER">Member</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          <div v-if="error" class="alert error animate-fade-in">
            {{ error }}
          </div>
          <div v-if="success" class="alert success animate-fade-in">
            {{ success }}
          </div>

          <button type="submit" class="btn btn-primary btn-lg w-full" :disabled="isLoading">
            <span v-if="isLoading" class="loading-spinner"></span>
            <span v-else>{{ isRegister ? 'Create Account' : 'Sign In' }}</span>
          </button>

          <div class="form-footer">
            <p class="footer-text">
              <span v-if="!isRegister">
                Don't have an account? 
                <a href="#" @click.prevent="isRegister = true; error = ''; success = ''" class="link-text link-primary">
                  Create one
                </a>
              </span>
              <span v-else>
                Already have an account? 
                <a href="#" @click.prevent="isRegister = false; error = ''; success = ''" class="link-text link-primary">
                  Log in
                </a>
              </span>
            </p>
          </div>
        </form>

        <form v-else @submit.prevent="handleForgotPassword" class="login-form animate-fade-in">
          <div class="form-group">
            <label class="label">Email Address</label>
            <input 
              type="email" 
              v-model="form.email" 
              required 
              class="input-field"
              placeholder="Enter your email"
            />
          </div>
          
          <div v-if="error" class="alert error animate-fade-in">
            {{ error }}
          </div>
          <div v-if="success" class="alert success animate-fade-in">
            {{ success }}
          </div>

          <button type="submit" class="btn btn-primary btn-lg w-full" :disabled="isLoading">
            <span v-if="isLoading" class="loading-spinner"></span>
            <span v-else>Send Reset Link</span>
          </button>
          
          <button type="button" class="btn btn-secondary w-full mt-4" @click="isForgotPassword = false; success = ''; error = ''">
            Back to Login
          </button>
        </form>
      </div>
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
    const isForgotPassword = ref(false);

    const selectedCountryCode = ref('+40');
    const countryCodes = [
      { label: 'RO', value: '+40' },
      { label: 'US', value: '+1' },
      { label: 'UK', value: '+44' },
      { label: 'DE', value: '+49' },
      { label: 'FR', value: '+33' },
      { label: 'IT', value: '+39' },
      { label: 'ES', value: '+34' }
    ];

    const handleSubmit = async () => {
      error.value = '';
      success.value = '';
      isLoading.value = true;
      
      try {
        if (isRegister.value) {
          await authStore.register(form.value);
          success.value = 'Registration successful! Please login.';
          setTimeout(() => {
            isRegister.value = false;
            success.value = '';
          }, 2000);
          form.value = { ...form.value, password: '' };
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

    const handleForgotPassword = async () => {
      error.value = '';
      success.value = '';
      isLoading.value = true;

      setTimeout(() => {
        isLoading.value = false;
        success.value = `Password reset link sent to ${form.value.email}`;
      }, 1500);
    };

    return {
      isRegister,
      form,
      error,
      success,
      isLoading,
      handleSubmit,
      isForgotPassword,
      handleForgotPassword,
      selectedCountryCode,
      countryCodes
    };
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 50%, #004e89 100%);
  opacity: 0.1;
  z-index: 0;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 107, 53, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(247, 147, 30, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, rgba(0, 78, 137, 0.1) 0%, transparent 50%);
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: var(--spacing-2xl);
  position: relative;
}


.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.logo-icon {
  font-size: 3rem;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.logo-text {
  font-size: 2.5rem;
  background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.login-subtitle {
  color: var(--text-muted);
  font-size: 1rem;
  font-weight: 500;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

.forgot-password-link {
  text-align: right;
  margin-top: var(--spacing-xs);
}

.link-text {
  color: var(--text-muted);
  font-size: 0.875rem;
  font-weight: 500;
  transition: color var(--transition-base);
}

.link-text:hover {
  color: var(--primary);
}

.link-primary {
  color: var(--primary);
  font-weight: 600;
}

.phone-input-wrapper {
  display: flex;
  gap: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.country-select {
  width: 140px;
  flex-shrink: 0;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  border-right: none;
  background-color: var(--bg-surface-hover);
  font-size: 0.875rem;
}

.phone-input {
  flex: 1;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.country-select:focus,
.phone-input:focus {
  z-index: 1;
  position: relative;
}

.form-footer {
  text-align: center;
  margin-top: var(--spacing-md);
}

.footer-text {
  color: var(--text-muted);
  font-size: 0.875rem;
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

.w-full {
  width: 100%;
}

@media (max-width: 640px) {
  .login-card {
    padding: var(--spacing-xl);
  }
  
  .logo-text {
    font-size: 2rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .country-select {
    width: 120px;
  }
}
</style>
