<script setup>
import { ref, watch, nextTick, computed } from 'vue';
import MarkdownIt from 'markdown-it';
import axios from 'axios';

// 初始化 Markdown 解析器
const md = new MarkdownIt({
    html: false, // 安全性考量，不允許 HTML 標籤
    linkify: true, // 自動識別連結
    breaks: true   // 換行轉為 <br>
});

// 定義 Props，接收外部書籍資料
const props = defineProps({
    books: {
        type: Array,
        default: () => []
    }
});

// 狀態管理
const isOpen = ref(false); // 聊天視窗開關
const inputMessage = ref(''); // 使用者輸入
const messages = ref([ // 對話紀錄
    {
        id: 1,
        role: 'ai',
        content: '您好！我是您的閱讀顧問 🤖\n請問今天想找什麼類型的書？\n\n您可以試著問我：\n- "推薦 500 元以內的書"\n- "有沒有村上春樹的書？"\n- "推薦一本好書"'
    }
]);
const isTyping = ref(false); // AI 正在輸入狀態

// 滾動到底部 helper
// 滾動到底部 helper
const messagesContainer = ref(null);
const scrollToBottom = async () => {
    await nextTick();
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
};

// --- AI 核心邏輯 (REAL API PHASE) ---
const generateResponse = async (query) => {
    isTyping.value = true;
    try {
        // 先顯示思考中
        await nextTick();

        // 構建 Context Json
        const contextBooks = props.books.map(b => ({
            bookName: b.bookName,
            author: b.author,
            price: b.price,
            shortDesc: b.shortDesc || ''
        }));

        // 呼叫後端 API
        const response = await axios.post('http://localhost:8080/api/ai/chat', {
            message: query,
            books: contextBooks
        });

        isTyping.value = false;
        return response.data;

    } catch (error) {
        console.error('AI API Error:', error);
        isTyping.value = false;
        return "抱歉，目前連線不穩定，請稍後再試。";
    }
};

// 發送訊息
const sendMessage = async () => {
    const text = inputMessage.value.trim();
    if (!text) return;

    // 1. 新增使用者訊息
    messages.value.push({
        id: Date.now(),
        role: 'user',
        content: text
    });
    inputMessage.value = '';
    scrollToBottom();

    // 2. 獲取 AI 回應
    const aiResponseContent = await generateResponse(text);

    // 3. 新增 AI 訊息
    messages.value.push({
        id: Date.now() + 1,
        role: 'ai',
        content: aiResponseContent
    });
    scrollToBottom();
};
</script>

<template>
    <!-- 懸浮按鈕與視窗容器 -->
    <div class="ai-assistant-container">
        <!-- 聊天視窗 -->
        <v-expand-transition>
            <v-card v-show="isOpen" class="chat-window elevation-12 rounded-lg d-flex flex-column" width="350"
                height="500">
                <!-- Header -->
                <v-toolbar color="primary" density="compact" class="px-2">
                    <v-icon icon="mdi-robot-excited" class="me-2"></v-icon>
                    <v-toolbar-title class="text-subtitle-1 font-weight-bold">
                        AI 購書顧問
                    </v-toolbar-title>
                    <v-spacer></v-spacer>
                    <v-btn icon="mdi-close" size="small" variant="text" @click="isOpen = false"></v-btn>
                </v-toolbar>

                <!-- 訊息列表區域 -->
                <div class="messages-area flex-grow-1 pa-4 overflow-y-auto bg-grey-lighten-4" ref="messagesContainer">
                    <div v-for="msg in messages" :key="msg.id"
                        :class="['d-flex mb-4', msg.role === 'user' ? 'justify-end' : 'justify-start']">
                        <!-- Avatar (AI Only) -->
                        <v-avatar v-if="msg.role === 'ai'" color="primary" size="32" class="me-2 align-self-start">
                            <v-icon icon="mdi-robot" size="20"></v-icon>
                        </v-avatar>

                        <!-- Bubble -->
                        <v-card :color="msg.role === 'user' ? 'primary' : 'white'"
                            :class="['pa-3 rounded-lg', msg.role === 'user' ? 'text-white' : 'text-body-1']"
                            style="max-width: 85%;" elevation="1">
                            <!-- Markdown Render -->
                            <div class="markdown-body" :class="{ 'text-white': msg.role === 'user' }"
                                v-html="md.render(msg.content)"></div>
                        </v-card>
                    </div>

                    <!-- Typing Indicator -->
                    <div v-if="isTyping" class="d-flex justify-start mb-4">
                        <v-avatar color="primary" size="32" class="me-2">
                            <v-icon icon="mdi-robot" size="20"></v-icon>
                        </v-avatar>
                        <v-card color="white" class="pa-3 rounded-lg" elevation="1">
                            <span class="typing-dots">Thinking...</span>
                        </v-card>
                    </div>
                </div>

                <!-- 輸入區域 -->
                <div class="input-area pa-2 bg-white border-t">
                    <v-text-field v-model="inputMessage" placeholder="輸入訊息..." variant="outlined" density="compact"
                        hide-details rounded="xl" append-inner-icon="mdi-send" @click:append-inner="sendMessage"
                        @keyup.enter="sendMessage" color="primary"></v-text-field>
                </div>
            </v-card>
        </v-expand-transition>

        <!-- 懸浮按鈕 (FAB) -->
        <v-fab-transition>
            <v-btn v-show="!isOpen" color="primary" icon="mdi-robot-excited" size="large" elevation="8" class="fab-btn"
                @click="isOpen = true"></v-btn>
        </v-fab-transition>
    </div>
</template>

<style scoped>
.ai-assistant-container {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
}

.chat-window {
    margin-bottom: 16px;
    /* 確保在手機上不會超出螢幕 */
    max-width: calc(100vw - 32px);
    max-height: 80vh;
}

/* Markdown Styles Override */
.markdown-body :deep(p) {
    margin-bottom: 8px;
}

.markdown-body :deep(p:last-child) {
    margin-bottom: 0;
}

.markdown-body :deep(ul) {
    padding-left: 20px;
    margin-bottom: 8px;
}

.typing-dots {
    animation: pulse 1.5s infinite;
    opacity: 0.6;
    font-size: 0.8rem;
}

@keyframes pulse {
    0% {
        opacity: 0.4;
    }

    50% {
        opacity: 1;
    }

    100% {
        opacity: 0.4;
    }
}
</style>