<template>
  <div class="chat-assistant" :class="{ 'open': isOpen }">
    <div class="chat-header" @click="toggleChat">
      <div class="header-content">
        <span class="icon">🤖</span>
        <h3>Gym Assistant</h3>
      </div>
      <span class="toggle-icon">{{ isOpen ? '▼' : '▲' }}</span>
    </div>
    
    <div class="chat-body" v-show="isOpen" ref="chatBody">
      <div v-if="messages.length === 0" class="welcome-message">
        <p>Hi! I'm your AI Gym Assistant.</p>
        <p>Ask me about workouts, exercises, or nutrition!</p>
      </div>
      
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.sender]">
        <div class="message-content">
          {{ msg.text }}
        </div>
      </div>
      
      <div v-if="isLoading" class="message ai">
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
        />
        <button type="submit" :disabled="!newMessage.trim() || isLoading">
          ➤
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, watch } from 'vue';
import chatService from '../services/chatService';

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
        messages.value.push({ text: "Sorry, I'm having trouble connecting to the AI right now.", sender: 'ai' });
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
      chatBody
    };
  }
};
</script>

<style scoped>
.chat-assistant {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 350px;
  background: white;
  border-radius: 15px;
  box-shadow: 0 5px 25px rgba(0,0,0,0.2);
  z-index: 1000;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.chat-assistant:not(.open) {
  height: 60px;
  width: 200px;
  cursor: pointer;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-content h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.chat-body {
  height: 400px;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.welcome-message {
  text-align: center;
  color: #666;
  margin-top: 50px;
  font-size: 14px;
}

.message {
  display: flex;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
}

.message.ai {
  align-self: flex-start;
}

.message-content {
  padding: 10px 15px;
  border-radius: 15px;
  font-size: 14px;
  line-height: 1.4;
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 5px;
}

.message.ai .message-content {
  background: white;
  color: #333;
  border: 1px solid #eee;
  border-bottom-left-radius: 5px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.chat-footer {
  padding: 15px;
  background: white;
  border-top: 1px solid #eee;
}

.chat-footer form {
  display: flex;
  gap: 10px;
}

.chat-footer input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
}

.chat-footer input:focus {
  border-color: #667eea;
}

.chat-footer button {
  background: #667eea;
  color: white;
  border: none;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.2s;
}

.chat-footer button:hover:not(:disabled) {
  transform: scale(1.1);
}

.chat-footer button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 5px 10px;
  background: white;
  border-radius: 15px;
  border: 1px solid #eee;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #ccc;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
