<template>
  <div class="chat-assistant" :class="{ 'open': isOpen }">
    <div class="chat-header" @click="toggleChat">
      <div class="header-content">
        <div class="chat-icon">
          <svg xmlns="http://www.w3.org/2000/svg" height="28" viewBox="0 0 24 24" width="28" fill="currentColor">
            <defs>
              <linearGradient id="geminiGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#EA4335;stop-opacity:1" />
                <stop offset="50%" style="stop-color:#4285F4;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#34A853;stop-opacity:1" />
              </linearGradient>
            </defs>
            <path :fill="isOpen ? 'currentColor' : 'url(#geminiGradient)'" d="M12 2L14.8 9.2L22 12L14.8 14.8L12 22L9.2 14.8L2 12L9.2 9.2L12 2Z"/>
          </svg>
        </div>
        <div class="header-text" v-show="isOpen">
          <h3>AI Gym Assistant</h3>
          <p v-if="!isOpen" class="header-subtitle">Ask me anything!</p>
        </div>
      </div>
      <button class="toggle-button" @click.stop="toggleChat" v-show="isOpen">
        <span class="toggle-icon">{{ isOpen ? '▼' : '▲' }}</span>
      </button>
    </div>
    
    <div class="chat-body" v-show="isOpen" ref="chatBody">
      <div v-if="messages.length === 0" class="welcome-message">
        <div class="welcome-icon">💬</div>
        <h4>Hi! I'm your AI Gym Assistant</h4>
        <p>Ask me about workouts, exercises, nutrition, or anything fitness-related!</p>
      </div>
      
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.sender]">
        <div class="message-avatar">
          <span v-if="msg.sender === 'user'">👤</span>
          <span v-else>
            <svg xmlns="http://www.w3.org/2000/svg" height="20" viewBox="0 0 24 24" width="20" fill="currentColor">
              <path d="M12 2L14.8 9.2L22 12L14.8 14.8L12 22L9.2 14.8L2 12L9.2 9.2L12 2Z"/>
            </svg>
          </span>
        </div>
        <div class="message-content" v-html="parseMarkdown(msg.text)"></div>
      </div>
      
      <div v-if="isLoading" class="message ai">
        <div class="message-avatar">
          <span>
            <svg xmlns="http://www.w3.org/2000/svg" height="20" viewBox="0 0 24 24" width="20" fill="currentColor">
              <path d="M12 2L14.8 9.2L22 12L14.8 14.8L12 22L9.2 14.8L2 12L9.2 9.2L12 2Z"/>
            </svg>
          </span>
        </div>
        <div class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>
    
    <div class="chat-footer" v-show="isOpen">
      <form @submit.prevent="sendMessage">
        <input 
          v-model="newMessage" 
          type="text" 
          placeholder="Ask a question..." 
          :disabled="isLoading"
          class="chat-input"
        />
        <button type="submit" :disabled="!newMessage.trim() || isLoading" class="send-button">
          <span v-if="!isLoading" class="send-icon">
            <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 -960 960 960" width="24" fill="currentColor">
              <path d="M120-160v-640l760 320-760 320Zm80-120 474-200-474-200v140l240 60-240 60v140Z"/>
            </svg>
          </span>
          <span v-else class="loading-spinner-small"></span>
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, watch } from 'vue';
import chatService from '../services/chatService';
import { marked } from 'marked';

export default {
  name: 'ChatAssistant',
  setup() {
    const isOpen = ref(false);
    const messages = ref([]);
    const newMessage = ref('');
    const isLoading = ref(false);
    const chatBody = ref(null);

    const toggleChat = () => {
      isOpen.value = !isOpen.value;
      if (isOpen.value) {
        scrollToBottom();
      }
    };

    const scrollToBottom = async () => {
      await nextTick();
      if (chatBody.value) {
        chatBody.value.scrollTop = chatBody.value.scrollHeight;
      }
    };

    const sendMessage = async () => {
      if (!newMessage.value.trim() || isLoading.value) return;

      const userMsg = newMessage.value;
      messages.value.push({ text: userMsg, sender: 'user' });
      newMessage.value = '';
      isLoading.value = true;
      scrollToBottom();

      try {
        const response = await chatService.sendMessage(userMsg);
        messages.value.push({ text: response.data.response, sender: 'ai' });
      } catch (error) {
        console.error('Chat error:', error);
        messages.value.push({ text: "Sorry, I'm having trouble connecting to the AI right now. Please try again later.", sender: 'ai' });
      } finally {
        isLoading.value = false;
        scrollToBottom();
      }
    };

    watch(messages, () => {
      scrollToBottom();
    }, { deep: true });

    return {
      isOpen,
      messages,
      newMessage,
      isLoading,
      toggleChat,
      sendMessage,
      chatBody,
      parseMarkdown: (text) => marked(text)
    };
  }
};
</script>

<style scoped>
.chat-assistant {
  position: fixed;
  bottom: var(--spacing-lg);
  right: var(--spacing-lg);
  width: 380px;
  background: var(--bg-surface);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  z-index: 1000;
  overflow: hidden;
  transition: all var(--transition-slow);
  border: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 2rem);
}

.chat-assistant:not(.open) {
  height: 64px;
  width: 64px;
  cursor: pointer;
  border-radius: 16px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0,0,0,0.05);
}

.chat-assistant:not(.open) .chat-header {
  height: 100%;
  width: 100%;
  padding: 0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-assistant:not(.open) .chat-header::before {
  display: none;
}

.chat-assistant:not(.open) .header-content {
  justify-content: center;
  flex: 1;
  width: 100%;
  height: 100%;
}

.chat-assistant:not(.open) .chat-icon {
  width: 100%;
  height: 100%;
  background: transparent;
  backdrop-filter: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-assistant:not(.open) .chat-icon svg {
  width: 42px !important;
  height: 42px !important;
}

.chat-assistant:not(.open) .header-content {
  justify-content: center;
  flex: 0;
}

.chat-assistant:not(.open) .chat-header {
  padding: 0;
  justify-content: center;
}

.chat-assistant:not(.open) .chat-icon {
  background: transparent;
  backdrop-filter: none;
  width: 100%;
  height: 100%;
}

.chat-header {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  color: white;
  padding: var(--spacing-lg) var(--spacing-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.chat-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

.chat-header:hover {
  box-shadow: var(--shadow-primary);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex: 1;
  z-index: 1;
}

.chat-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.header-text {
  flex: 1;
}

.header-text h3 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: 1.125rem;
  font-weight: 700;
  color: white;
}

.header-subtitle {
  margin: 0;
  font-size: 0.75rem;
  opacity: 0.9;
  font-weight: 500;
}

.toggle-button {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-base);
  z-index: 1;
  flex-shrink: 0;
}

.toggle-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
}

.toggle-icon {
  font-size: 1rem;
  font-weight: 700;
}

.chat-body {
  flex: 1;
  height: 450px;
  padding: var(--spacing-xl);
  overflow-y: auto;
  background: var(--bg-body);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  min-height: 0;
}

.welcome-message {
  text-align: center;
  padding: var(--spacing-2xl) var(--spacing-lg);
  color: var(--text-muted);
  margin: auto 0;
}

.welcome-icon {
  font-size: 3rem;
  margin-bottom: var(--spacing-md);
  opacity: 0.6;
}

.welcome-message h4 {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 var(--spacing-sm) 0;
}

.welcome-message p {
  font-size: 0.875rem;
  margin: 0;
  line-height: 1.6;
}

.message {
  display: flex;
  gap: var(--spacing-sm);
  max-width: 85%;
  animation: fadeIn 0.3s ease-out;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.ai {
  align-self: flex-start;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  flex-shrink: 0;
  background: var(--bg-surface-hover);
  border: 2px solid var(--border-light);
}

.message-avatar span {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.send-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.message.user .message-avatar {
  background: var(--primary-light);
  border-color: var(--primary);
}

.message.ai .message-avatar {
  background: var(--secondary-light);
  border-color: var(--secondary);
}

.message-content {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-xl);
  font-size: 0.9375rem;
  line-height: 1.6;
  word-wrap: break-word;
}

.message.user .message-content {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  color: white;
  border-bottom-right-radius: var(--radius-sm);
  box-shadow: var(--shadow-md);
}

.message.ai .message-content {
  background: var(--bg-surface);
  color: var(--text-main);
  border: 2px solid var(--border);
  border-bottom-left-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

.chat-footer {
  padding: var(--spacing-lg);
  background: var(--bg-surface);
  border-top: 1px solid var(--border);
}

.chat-footer form {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

.chat-input {
  flex: 1;
  padding: var(--spacing-md) var(--spacing-lg);
  border: 2px solid var(--border);
  border-radius: var(--radius-full);
  outline: none;
  font-size: 0.9375rem;
  background: var(--bg-surface);
  color: var(--text-main);
  transition: all var(--transition-base);
}

.chat-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-light);
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-button {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all var(--transition-base);
  font-size: 1.25rem;
  flex-shrink: 0;
  box-shadow: var(--shadow-primary);
}

.send-button:hover:not(:disabled) {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.4);
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner-small {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.typing-indicator {
  display: flex;
  gap: 6px;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-surface);
  border: 2px solid var(--border);
  border-radius: var(--radius-xl);
  border-bottom-left-radius: var(--radius-sm);
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { 
    transform: scale(0);
    opacity: 0.5;
  }
  40% { 
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 640px) {
  .chat-assistant {
    width: calc(100vw - 2rem);
    right: var(--spacing-md);
    bottom: var(--spacing-md);
  }
  
  .chat-assistant:not(.open) {
    width: calc(100vw - 2rem);
  }
  
  .chat-body {
    height: 400px;
  }
}
</style>
