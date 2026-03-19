<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue' 
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'
import { useUserStore } from '@/modules/user/store/userStore'
import reviewService from '@/modules/review/api'
import { useReportStore } from '@/modules/review/store/reportStore'



const router = useRouter()
const route = useRoute()
const drawer = ref(true)
const userStore = useUserStore()
const reportStore = useReportStore()
const unreadUserCount = ref(0)
let timer = null 

const items = ref([
  {
    title: '會員管理',
    icon: 'mdi-account-group',
    children: [
      { title: '會員資料列表', to: '/dev/admin/users', icon: 'mdi-account-details' },
      { 
        title: '客服對話中心', 
        to: '/dev/admin/users/admin-chat', 
        icon: 'mdi-chat-processing-outline',
        isChat: true 
      },
    ],
  },
  {
    title: '書籍管理',
    icon: 'mdi-book-open-page-variant',
    children: [
      { title: '書籍列表', to: '/dev/admin/books', icon: 'mdi-format-list-bulleted' },
      { title: '進退貨管理', to: '/dev/admin/logs', icon: 'mdi-swap-horizontal-bold' },
      // { title: '數據報表分析', to: '/dev/admin/reports', icon: 'mdi-chart-bar' },
    ],
  },
  {
    title: '優惠券管理',
    icon: 'mdi-ticket-percent-outline',
    to: '/dev/admin/coupons',
  },
  {
    title: '訂單管理',
    icon: 'mdi-clipboard-list-outline',
    children: [
      { title: '訂單列表', to: '/dev/admin/orders/list', icon: 'mdi-format-list-bulleted' },
      { title: '訂單分析', to: '/dev/admin/orders/analysis', icon: 'mdi-chart-bar' },
    ],
  },
  {
    title: '評價管理',
    icon: 'mdi-star-half-full',
    children: [
      { title: '書籍評價', to: '/dev/admin/reviews', icon: 'mdi-comment-text-multiple-outline' },
      { title: '檢舉列表', 
        to: '/dev/admin/reviews/reports',
        icon: 'mdi-alert-octagon-outline', // 或 mdi-flag-outline
        showBadge: true
      },
    ],
  },
  {
    title: '讀書會管理',
    icon: 'mdi-book-multiple',
    to: '/dev/admin/bookclubs',
  },
])

watch(
  () => route.path,
  () => {
    fetchUnreadUserCount()
  }
)

const fetchUnreadUserCount = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/chat/admin/unread-users');
    
    let count = (typeof res.data === 'object' && res.data !== null) 
                ? (res.data.count || res.data.unreadCount || 0) 
                : res.data;

    unreadUserCount.value = parseInt(count) || 0;
    console.log("當前未讀會員人數:", unreadUserCount.value); 
  } catch (error) {
    console.error('獲取客服未讀人次失敗', error);
  }
}

const handleLogout = () => {
  Swal.fire({
    title: '確定要登出嗎？',
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#2E5C43',
    confirmButtonText: '登出',
    cancelButtonText: '取消',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.get('http://localhost:8080/api/logout', { withCredentials: true })
      } catch (error) {
        console.error('後端登出失敗', error)
      } finally {
        localStorage.clear()
        router.push('/login')
        Swal.fire({ icon: 'success', title: '已成功登出', timer: 1500, showConfirmButton: false })
      }
    }
  })
}

onMounted(async () => {
  
  reportStore.fetchPendingCount()

  if (userStore.isLoggedIn) {
    try {
      await userStore.syncUserProfile();
      console.log("管理員頭貼同步成功:", userStore.img);
    } catch (error) {
      console.error("Layout 初始化頭貼失敗", error);
    }
  }

  
  fetchUnreadUserCount();
  reportStore.fetchPendingCount();
  timer = setInterval(() => { fetchUnreadUserCount(); reportStore.fetchPendingCount() }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
})
</script>

<template>
  <v-layout class="rounded rounded-md">
    <v-navigation-drawer v-model="drawer" color="primary">
      <v-list-item class="py-4" to="/home">
        <template v-slot:prepend>
          <v-avatar color="accent" size="40" class="me-2 elevation-2 border-sm">
            <v-img 
              v-if="userStore.img" 
              :src="(userStore.img.startsWith('data:') || userStore.img.startsWith('http')) 
                    ? userStore.img 
                    : `http://localhost:8080${userStore.img}`" 
              cover
            >
              <template v-slot:placeholder>
                <div class="d-flex align-center justify-center fill-height bg-accent">
                  {{ userStore.name?.charAt(0) || 'A' }}
                </div>
              </template>
            </v-img>
            
            <span v-else class="text-white font-weight-bold">
              {{ userStore.name?.charAt(0) || 'A' }}
            </span>
          </v-avatar>
        </template>
        <v-list-item-title class="font-weight-bold">{{ userStore.name }}</v-list-item-title>
        <v-list-item-subtitle>
          {{ userStore.role === 'SUPER_ADMIN' ? '系統管理員' : '營運專員' }}
        </v-list-item-subtitle>
      </v-list-item>

      <v-divider></v-divider>

      <v-list density="compact" nav>
        <template v-for="(item, i) in items" :key="i">
          
          <v-list-group v-if="item.children" :value="item.title">
            <template v-slot:activator="{ props }">
              <v-list-item v-bind="props" :prepend-icon="item.icon" :title="item.title">
                <template v-slot:append>
                  <v-badge
                    v-if="item.title === '評價管理' && reportStore.pendingCount > 0"
                    color="error"
                    :content="reportStore.pendingCount"
                    inline
                  ></v-badge>
                  
                  <v-badge 
                    v-if="item.title === '會員管理' && unreadUserCount > 0" 
                    color="error" 
                    :content="unreadUserCount" 
                    inline>
                  </v-badge>
                </template>
              </v-list-item>
            </template>

            <v-list-item
              v-for="(child, k) in item.children"
              :key="k"
              :title="child.title"
              :prepend-icon="child.icon"
              :to="child.to"
              :value="child.title"
              color="accent"
            >
              <template v-slot:append>
                  <v-badge
                    v-if="child.showBadge && reportStore.pendingCount > 0"
                    color="error"
                    :content="reportStore.pendingCount"
                    inline
                  ></v-badge>
                  
                  <v-badge 
                  v-if="child.isChat && unreadUserCount > 0" 
                  color="error" 
                  :content="unreadUserCount" 
                  inline>
                  </v-badge>
              </template>
            </v-list-item>
          </v-list-group>

          <v-list-item
            v-else
            :prepend-icon="item.icon"
            :title="item.title"
            :to="item.to"
            :value="item.title"
            color="accent"
          ></v-list-item>

        </template>
      </v-list>

      <template v-slot:append>
        <div class="pa-2">
          <v-btn 
  block 
  color="accent" 
  variant="tonal" 
  prepend-icon="mdi-storefront-outline" 
  class="mb-2"
  href="/dev/user/home"
  target="_blank"
>
  <span class="text-white">前台網頁</span>
</v-btn>
          <v-btn block color="secondary" @click="handleLogout" prepend-icon="mdi-logout">
            登出
          </v-btn>
        </div>
      </template>
    </v-navigation-drawer>

    <v-app-bar elevation="0" class="bg-background" density="compact">
      <v-app-bar-nav-icon variant="text" color="primary" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-spacer></v-spacer>
    </v-app-bar>

    <v-main class="bg-background" style="min-height: 100vh">
      <v-container fluid class="pa-6">
        <router-view></router-view>
      </v-container>
    </v-main>
  </v-layout>
</template>

<style scoped>
:deep(.v-list-group__items .v-list-item) {
  padding-inline-start: 16px !important;
}
</style>