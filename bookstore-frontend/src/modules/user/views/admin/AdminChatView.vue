<template>
  <div class="ultimate-wrapper bg-transparent">
    <v-card class="chat-main-container rounded-xl elevation-4 border bg-white">
      <v-row no-gutters class="fill-height">
        
        <v-col cols="12" md="4" lg="3" class="border-e d-flex flex-column h-100 bg-white">
          <v-toolbar color="#2D5A41" flat>
            <v-toolbar-title class="font-weight-bold text-white">客服對話中心</v-toolbar-title>
          </v-toolbar>
          <v-list lines="two" class="pa-0 overflow-y-auto flex-grow-1 bg-white">
            <v-list-item v-for="chat in chatList" :key="chat.userId" :active="activeUser?.userId === chat.userId" @click="selectUser(chat)" link class="border-b px-4 bg-white">
              <template v-slot:prepend><v-avatar size="48" color="grey-lighten-4">
            <v-img 
              :src="chat.userImg && chat.userImg.startsWith('/uploads/') 
                    ? `http://localhost:8080${chat.userImg}` 
                    : (chat.userImg || 'https://cdn-icons-png.flaticon.com/512/149/149071.png')"
            ></v-img>
          </v-avatar></template>
              <v-list-item-title class="font-weight-bold text-success">{{ chat.userName }}</v-list-item-title>
              <v-list-item-subtitle class="text-truncate">{{ chat.lastMsg }}</v-list-item-subtitle>
              <template v-slot:append>
                <div class="d-flex flex-column align-end">
                  <span class="text-caption text-grey mb-1">{{ formatTime(chat.lastTime) }}</span>
                  <v-badge v-if="chat.unreadCount > 0" color="error" :content="chat.unreadCount" inline></v-badge>
                </div>
              </template>
            </v-list-item>
          </v-list>
        </v-col>

        <v-col cols="12" md="8" lg="9" class="d-flex flex-column h-100 bg-white" @mousedown="triggerMarkRead">
          <template v-if="activeUser">
            <v-toolbar flat color="white" border-bottom class="px-4">
              <v-toolbar-title class="text-subtitle-1 font-weight-bold">與 {{ activeUser.userName }} 的對話</v-toolbar-title>
            </v-toolbar>

            <div id="msgContainer" class="messages-area pa-6 flex-grow-1 overflow-y-auto bg-white">
              <div v-for="msg in messages" :key="msg.msgId" :class="['d-flex mb-6', Number(msg.senderId) === 1 ? 'justify-end' : 'justify-start']">
                <div :class="['max-width-70', Number(msg.senderId) === 1 ? 'text-right' : 'text-left']">
                  <v-card :color="Number(msg.senderId) === 1 ? '#2D5A41' : 'white'" :class="['pa-3 rounded-lg d-inline-block elevation-1', Number(msg.senderId) !== 1 ? 'border' : '']">
                    <div :class="Number(msg.senderId) === 1 ? 'text-white' : 'text-grey-darken-4'" style="word-break: break-all; text-align: left;">{{ msg.content }}</div>
                  </v-card>
                  <div class="text-caption text-grey mt-1">
                    <span v-if="Number(msg.senderId) === 1 && msg.isRead" 
                          class="me-1 text-success font-weight-bold">
                      已讀
                    </span>
                    {{ formatTime(msg.createdAt) }}
                  </div>
                </div>
              </div>
            </div>

            <v-footer color="white" border-top class="pa-4 bg-white">
              <v-text-field v-model="inputMsg" placeholder="輸入回覆..." variant="outlined" rounded="pill" append-inner-icon="mdi-send" @click:append-inner="sendMessage" @keyup.enter="sendMessage" color="success" hide-details></v-text-field>
            </v-footer>
          </template>
          <div v-else class="h-100 d-flex align-center justify-center bg-white"><v-icon size="64" color="grey-lighten-3">mdi-forum-outline</v-icon></div>
        </v-col>
      </v-row>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'

axios.defaults.withCredentials = true;
const chatList = ref([]); const messages = ref([]); const activeUser = ref(null); const inputMsg = ref(''); let socket = null

const connectWebSocket = () => {
  socket = new WebSocket('ws://localhost:8080/chat/1');
  socket.onmessage = (event) => {
    const rawData = String(event.data).replace(/"/g, '').trim();
    if (rawData === "READ_SIGNAL") {
      // 💡 WebSocket 即時更新：強制將發送者的訊息設為已讀
      messages.value = messages.value.map(m => {
        if (Number(m.senderId) === 1) return { ...m, isRead: true };
        return m;
      });
      return;
    }
    try {
      const receivedMsg = JSON.parse(event.data);
      if (activeUser.value && Number(receivedMsg.senderId) === Number(activeUser.value.userId)) {
        messages.value.push(receivedMsg);
        scrollToBottom();
        triggerMarkRead(); 
      }
      fetchChatList();
    } catch (e) { console.warn("數據解析攔截"); }
  };
};

const triggerMarkRead = async () => {
  if (!activeUser.value) return;
  try { 
    await axios.get(`http://localhost:8080/api/chat/markRead/${activeUser.value.userId}/1`); 
    
    fetchChatList(); 
  } catch (e) { console.error(e); }
};

const selectUser = async (user) => {
  activeUser.value = user;
  
  user.unreadCount = 0; 
  
  await triggerMarkRead(); 
  
  const res = await axios.get(`http://localhost:8080/api/chat/history/${user.userId}?t=${new Date().getTime()}`);
  
  messages.value = Array.isArray(res.data) ? res.data.map(m => ({
    ...m,
    isRead: m.isRead === true || m.isRead === 1 || m.read === true || m.read === 1
  })) : [];
  
  scrollToBottom();

  fetchChatList();
};

const fetchChatList = async () => { 
  const res = await axios.get('http://localhost:8080/api/chat/admin/list'); 
  chatList.value = Array.isArray(res.data) ? res.data : []; 
}

const sendMessage = async () => {
  if (!inputMsg.value.trim() || !activeUser.value) return
  const payload = { senderId: 1, receiverId: activeUser.value.userId, content: inputMsg.value, isRead: false }
  const res = await axios.post('http://localhost:8080/api/chat/send', payload)
  messages.value.push(res.data);
  inputMsg.value = '';
  scrollToBottom();
  fetchChatList();
}

const scrollToBottom = async () => { 
  await nextTick(); 
  const el = document.getElementById('msgContainer');
  if (el) el.scrollTop = el.scrollHeight; 
}

const formatTime = (timeStr) => { 
  if (!timeStr) return ''; 
  const date = new Date(timeStr); 
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}` 
}

onMounted(() => { fetchChatList(); connectWebSocket() }); 
onUnmounted(() => { if (socket) socket.close() });
</script>

<style scoped>
:deep(.v-main), :deep(.v-application) { background: transparent !important; }

.ultimate-wrapper {
  background: transparent !important; 
  height: calc(100vh - 100px); 
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.chat-main-container { width: 100%; max-width: 1400px; height: 100%; background: white !important; }
.messages-area { background-color: #ffffff !important; background-image: none !important; }
.max-width-70 { max-width: 70%; }
.overflow-y-auto::-webkit-scrollbar { width: 4px; }
.overflow-y-auto::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 10px; }
</style>