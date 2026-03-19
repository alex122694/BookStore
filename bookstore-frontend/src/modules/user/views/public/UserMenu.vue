<template>
  <v-container class="user-profile-wrapper py-10">
    <v-card class="profile-header-card mb-8 rounded-xl elevation-4" overflow-hidden>
      <v-img src="https://images.unsplash.com/photo-1516979187457-637abb4f9353?q=80&w=2070" height="200" cover
        class="align-end text-white">
        <div class="header-overlay pa-6 d-flex align-center">
          <v-avatar size="100" class="elevation-4 profile-avatar mr-6">
            <v-img :src="userStore.img && userStore.img.includes('/uploads/')
              ? `http://localhost:8080${userStore.img}`
              : userStore.img" cover>
              <template v-slot:placeholder>
                <div class="bg-accent fill-height d-flex align-center justify-center text-h3 text-white">
                  {{ userStore.name?.charAt(0) }}
                </div>
              </template>
            </v-img>
          </v-avatar>
          <div>
            <h1 class="text-h4 font-weight-bold mb-1">
              嗨～{{ userStore.name }}！歡迎進入閱讀的世界！
            </h1>
            <p class="text-subtitle-1 opacity-80">{{ randomQuote }}</p>
          </div>
        </div>
      </v-img>
    </v-card>

    <v-row>
      <v-col v-for="(item, index) in menuItems" :key="index" cols="12" sm="6" md="4" lg="3">
        <v-hover v-slot="{ isHovering, props }">
          <v-card v-bind="props" :elevation="isHovering ? 8 : 2"
            class="menu-card rounded-lg transition-swing text-center pa-6 h-100" @click="navigateTo(item.to)">
            <v-badge :content="item.unreadCount" :model-value="item.unreadCount > 0" color="error" overlap offset-x="10"
              offset-y="10">
              <v-avatar :color="item.color" size="64" class="mb-4">
                <v-icon :icon="item.icon" color="white" size="32"></v-icon>
              </v-avatar>
            </v-badge>
            <v-card-title class="font-weight-bold justify-center">{{ item.title }}</v-card-title>
            <v-card-subtitle>{{ item.desc }}</v-card-subtitle>
          </v-card>
        </v-hover>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/modules/user/store/userStore'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()
const randomQuote = ref('')
const userAvatar = ref(null)

const quotes = [
  '探索文字的溫度，享受今日的悠閒讀書時光。',
  '每一本書都是一場遠行，準備好今天要在哪裡降落了嗎？',
  '文字是靈魂的避風港，讓心靈在書海中靜靜停泊。',
  '打開書頁，就像打開了另一種人生的可能性。',
  '在這個快節奏的世界裡，給自己留一段與書對話的慢時光。',
  '書架上的每一本書，都是通往智慧之門的鑰匙。',
  '閱讀，是為了遇見那個更好的自己。',
  '沉浸在墨香之中，尋找生活裡被遺忘的小確幸。',
  '萬物皆有裂痕，那是光照進來的地方，而書就是那束光。',
  '願你能在書中讀到遠方，也能在生活中過得精彩。',
]

const getRandomQuote = () => {
  const index = Math.floor(Math.random() * quotes.length)
  randomQuote.value = quotes[index]
}

const menuItems = ref([
  {
    title: '會員資料',
    icon: 'mdi-account-cog-outline',
    color: 'primary',
    to: '/dev/user/profile/password-confirmation',
    desc: '修改基本資料與密碼',
    unreadCount: 0,
  },
  {
    title: '書本收藏清單',
    icon: 'mdi-heart-outline',
    color: 'secondary',
    to: '/dev/user/wishlist',
    desc: '查看已收藏的心儀書籍',
    unreadCount: 0,
  },
  {
    title: '瀏覽紀錄',
    icon: 'mdi-history',
    color: 'brown-lighten-1',
    to: '/dev/user/history',
    desc: '最近看過的書籍清單',
    unreadCount: 0,
  },
  {
    title: '優惠券',
    icon: 'mdi-ticket-percent-outline',
    color: 'orange-darken-2',
    to: '/dev/user/coupons',
    desc: '領取與查詢您的專屬折扣',
    unreadCount: 0,
  },
  {
    title: '歷史訂單',
    icon: 'mdi-package-variant-closed',
    color: 'teal-darken-1',
    to: '/dev/user/orders',
    desc: '追蹤訂單進度與紀錄',
    unreadCount: 0,
  },
  {
    title: '個人評價歷史',
    icon: 'mdi-star-face',
    color: 'yellow-darken-3',
    to: '/dev/user/myreviews',
    desc: '管理您留下的讀後感想',
    unreadCount: 0,
  },
  {
    title: '讀書會專區',
    //將icon改成書本圖案將icon改成書本圖案
    icon: 'mdi-book-open-variant',
    color: 'blue-grey-darken-1',
    to: '/dev/user/bookclubs',
    desc: '查看您參加與發起的讀書會',
  },
  {
    title: '客服專區',
    icon: 'mdi-chat-question-outline',
    color: 'blue-grey-darken-1',
    to: '/dev/user/user-chat',
    desc: '聯絡我們或查看常見問題',
    unreadCount: 0,
  },
])

watch(
  () => userStore.unreadCount,
  (newCount) => {
    const chatItem = menuItems.value.find(item => item.title === '客服專區')
    if (chatItem) {
      chatItem.unreadCount = newCount
    }
  },
  { immediate: true }
)

const navigateTo = (path) => {
  router.push(path)
}

const fetchUnreadCount = async () => {
  if (!userStore.userId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/chat/unread/${userStore.userId}`)

    const count = typeof res.data === 'object' ? res.data.count : res.data;

    const chatItem = menuItems.value.find(item => item.title === '客服專區')
    if (chatItem) {
      chatItem.unreadCount = parseInt(count) || 0; // 強制轉為數字
    }
  } catch (error) {
    console.error("獲取未讀訊息失敗", error)
  }
}

onMounted(() => {
  getRandomQuote()
  if (userStore.isLoggedIn) {
    userStore.fetchUnreadCount()
  }
})
</script>

<style scoped lang="scss">
.user-profile-wrapper {
  max-width: 1200px;
}

.profile-header-card {
  position: relative;

  .header-overlay {
    background: linear-gradient(to right, rgba(46, 92, 67, 0.95), rgba(46, 92, 67, 0.4));
    height: 100%;
    width: 100%;
  }
}

.profile-avatar {
  border: 4px solid white;
}

.menu-card {
  cursor: pointer;
  border: 1px solid rgba(46, 92, 67, 0.1);
  background-color: #fdfbf7;

  &:hover {
    background-color: white;
    transform: translateY(-5px);
    border-color: #2e5c43;
  }

  .v-card-title {
    color: #2e5c43;
  }
}

.opacity-80 {
  opacity: 0.8;
}

.bg-accent {
  background-color: #b05252 !important;
}

.text-primary {
  color: #2e5c43 !important;
}
</style>
