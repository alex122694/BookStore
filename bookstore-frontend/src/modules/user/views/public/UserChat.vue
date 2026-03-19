<template>
  <v-container class="py-10 d-flex flex-column align-center bg-transparent">
    
    <v-card width="600" height="700" class="rounded-xl elevation-4 d-flex flex-column border bg-white" @mousedown="triggerMarkRead">
      <v-toolbar color="#2D5A41" flat>
        <v-icon icon="mdi-book-open-variant" class="ms-4" color="white"></v-icon>
        <v-toolbar-title class="font-weight-bold text-white">森林書屋 - 客服專區</v-toolbar-title>
      </v-toolbar>

      <v-card-text id="userMsgContainer" class="messages-area flex-grow-1 overflow-y-auto pa-6 bg-white" ref="msgContainer">
        <div v-for="msg in messages" :key="msg.msgId"
             :class="['d-flex mb-6', Number(msg.senderId) === Number(userStore.userId) ? 'justify-end' : 'justify-start']">
          
          <v-avatar v-if="Number(msg.senderId) !== Number(userStore.userId)" size="36" class="me-2 mt-1">
            <v-img src="https://cdn-icons-png.flaticon.com/512/3649/3649460.png"></v-img>
          </v-avatar>

          <div :class="['max-width-70', Number(msg.senderId) === Number(userStore.userId) ? 'text-right' : 'text-left']">
            <v-card :color="Number(msg.senderId) === Number(userStore.userId) ? '#2D5A41' : 'white'" 
                    :class="['pa-3 rounded-lg d-inline-block text-left elevation-1', Number(msg.senderId) !== Number(userStore.userId) ? 'border' : '']">
              <div :class="Number(msg.senderId) === Number(userStore.userId) ? 'text-white' : 'text-grey-darken-4'">
                {{ msg.content }}
              </div>
            </v-card>
            <div class="text-caption text-grey mt-1">
              <span v-if="Number(msg.senderId) === Number(userStore.userId) && (msg.isRead || msg.read)" 
                    class="me-1 text-success font-weight-bold">已讀</span>
              {{ formatTime(msg.createdAt) }}
            </div>
          </div>
        </div>
      </v-card-text>

      <v-divider></v-divider>
      <v-card-actions class="pa-4 bg-white">
        <v-text-field 
          v-model="inputMsg" 
          placeholder="請問有什麼可以幫您的嗎？" 
          variant="outlined" 
          rounded="pill" 
          hide-details 
          append-inner-icon="mdi-send" 
          @click:append-inner="sendMessage" 
          @keyup.enter="sendMessage" 
          color="success"
        ></v-text-field>
      </v-card-actions>
    </v-card>

    <v-btn
      prepend-icon="mdi-arrow-left"
      variant="flat"
      color="#2D5A41"
      size="large"
      class="mt-8 rounded-pill text-white font-weight-bold px-12 elevation-2"
      @click="$router.go(-1)"
    >
      返回上一頁
    </v-btn>

  </v-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/modules/user/store/userStore'
import axios from 'axios'
import { useRouter } from 'vue-router'

axios.defaults.withCredentials = true;
const userStore = useUserStore()
const messages = ref([])
const inputMsg = ref('')
const msgContainer = ref(null)
const router = useRouter()
let socket = null

const connectWebSocket = () => {
  if (!userStore.userId) return;
  socket = new WebSocket(`ws://localhost:8080/chat/${userStore.userId}`);
  
  socket.onmessage = (event) => {
    const rawData = String(event.data).replace(/"/g, '').trim();
    if (rawData === "READ_SIGNAL") {
      messages.value = messages.value.map(m => {
        if (Number(m.senderId) === Number(userStore.userId)) {
          return { ...m, isRead: true };
        }
        return m;
      });
      return; 
    }

    try {
      const newMsg = JSON.parse(event.data);
      if (newMsg && newMsg.content) {
        messages.value.push(newMsg);
        scrollToBottom();
        triggerMarkRead(); 
      }
    } catch (e) { console.warn("數據攔截:", event.data); }
  };
};

const triggerMarkRead = async () => {
  if (!userStore.userId) return;
  try {
    await axios.get(`http://localhost:8080/api/chat/markRead/1/${userStore.userId}`);
    
    userStore.setUnreadCount(0); 
    
  } catch (e) {
    console.error("標記已讀失敗", e);
  }
};

const fetchHistory = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/chat/history/${userStore.userId}`);
    messages.value = Array.isArray(res.data) ? res.data.map(m => ({
      ...m,
      isRead: (m.isRead === true || m.isRead === 1)
    })) : [];
    scrollToBottom();
  } catch (error) { console.error("載入歷史失敗", error); }
};

const sendMessage = async () => {
  if (!inputMsg.value.trim()) return
  const payload = { senderId: userStore.userId, receiverId: 1, content: inputMsg.value, isRead: false }
  try {
    const res = await axios.post('http://localhost:8080/api/chat/send', payload)
    messages.value.push(res.data)
    inputMsg.value = ''
    scrollToBottom()
  } catch (error) { console.error("發送失敗", error); }
}

const scrollToBottom = async () => {
  await nextTick();
  const el = document.getElementById('userMsgContainer');
  if (el) el.scrollTop = el.scrollHeight;
};

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    router.push('/dev/user/login')
    return 
  }

  userStore.setUnreadCount(0) 
  await triggerMarkRead()
  await fetchHistory()
  connectWebSocket() 
})

onUnmounted(() => { if (socket) socket.close() });
</script>

<style scoped>
.messages-area {
  background-color: #ffffff !important;
  background-image: none !important;
}
.max-width-70 { max-width: 75%; }
.overflow-y-auto::-webkit-scrollbar { width: 4px; }
.overflow-y-auto::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 10px; }
</style>