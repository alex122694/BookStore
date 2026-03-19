<template>
  <v-container class="review-container">
    <v-row class="header-section" align="center">
      <v-col>
        <div class="title-wrapper">
          <h2 class="page-title">評價資料</h2>
          <span v-if="currentBookName" class="sub-info">
            (目前查看書籍: {{ currentBookName }})
          </span>
        </div>
      </v-col>
    </v-row>

    <v-row class="mb-4" align="center">
      <v-spacer></v-spacer>
      <v-col cols="12" md="4">
        <div class="d-flex align-center">
          <v-text-field
            v-model="search"
            label="搜尋評價內容、會員..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            bg-color="white"
            color="primary"
            class="rounded-lg"
          ></v-text-field>
        </div>
      </v-col>
    </v-row>

    <v-card class="table-card">
      <v-data-table
        :headers="headers"
        :items="filteredReviews"
        :loading="loading"
        :search="search"
        item-value="reviewId"
        class="forest-table"
        hover
      >
        <template v-slot:item.reviewId="{ item }">
          {{ filteredReviews.indexOf(item) + 1 }}
        </template>

        <template v-slot:item.userName="{ item }">
          <a href="#" class="user-link" @click.prevent="goToUserDetail(item.userId)">
            {{ item.userName || item.userId }}
          </a>
        </template>

        <template v-slot:item.rating="{ item }">
          <v-rating
            :model-value="item.rating"
            color="amber"
            density="compact"
            half-increments
            readonly
            size="small"
          ></v-rating>
        </template>

        <template v-slot:item.status="{ item }">
          <div class="d-flex align-center justify-center">
            <v-switch
              :model-value="item.status === 1"
              :disabled="!canToggleStatus(item)"
              color="success"
              hide-details
              density="compact"
              @click.prevent="handleToggleStatus(item)"
            ></v-switch>
            <span
              :class="item.status === 1 ? 'text-success' : 'text-error'"
              class="ml-2 font-weight-bold text-caption"
            >
              {{ item.status === 1 ? '顯示中' : '已隱藏' }}
            </span>
          </div>
        </template>

        <template v-slot:item.actions="{ item }">
          <v-tooltip location="top" :text="Number(item.userId) === Number(currentUserId.value) ? '無法檢舉自己的評價' : '檢舉此評價'">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                icon
                size="small"
                variant="text"
                color="error"
                class="action-btn"
                :disabled="!canReport(item)"
                @click="handleReport(item)"
              >
                <v-icon>mdi-alert-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-tooltip>
        </template>

        <template v-slot:no-data>
          <div class="no-data-text">目前沒有評價資料</div>
        </template>
      </v-data-table>
    </v-card>
    <div class="d-flex justify-center mt-6 mb-4">
      <v-btn
        color="primary"
        variant="outlined"
        size="large"
        prepend-icon="mdi-arrow-left"
        class="rounded-lg font-weight-bold px-6"
        @click="router.push({ name: 'admin-reviews' })"
      >
        返回書籍評價
      </v-btn>
    </div>
  </v-container>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { REPORT_OPTIONS } from '@/common/utils/reportOptions.js'
import axios from 'axios'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const reviews = ref([])
const loading = ref(false)
const search = ref('')

const currentUserId = computed(() => {
  const id = getUserIdFromToken()
  return id ? Number(id) : null 
})

const currentUserRole = computed(() => {
  const role = localStorage.getItem('userRole')
  if (role === 'SUPER_ADMIN' || role === '0') return 0
  if (role === 'ADMIN' || role === '1') return 1
  return 2 
})

// 判斷目前是查看哪本書
const currentBookId = ref(route.params.bookId)

// 定義表格欄位
const headers = [
  { title: '編號', key: 'reviewId', align: 'start', sortable: true },
  { title: '會員 ID', key: 'userId', align: 'start', sortable: true },
  { title: '會員姓名', key: 'userName', align: 'start', sortable: true },
  { title: '評分', key: 'rating', align: 'center', sortable: true },
  { title: '評價內容', key: 'comment', align: 'start', sortable: false, width: '40%' },
  { title: '評價時間', key: 'createdAt', align: 'center', sortable: true },
  { title: '狀態', key: 'status', align: 'center', sortable: true },
  { title: '檢舉', key: 'actions', align: 'center', sortable: false },
]

const filteredReviews = computed(() => {
  if (!currentBookId.value) return reviews.value
  return reviews.value.filter((r) => r.bookId == currentBookId.value)
})

// 讀取資料函式
const loadReviews = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/public/admin/reviews')
    reviews.value = response.data
  } catch (error) {
    console.error('讀取評價失敗:', error)
  } finally {
    loading.value = false
  }
}

// 取得書名
const currentBookName = computed(() => {
  if (route.query.bookName) {
    return route.query.bookName
  }

  if (filteredReviews.value.length > 0 && filteredReviews.value[0].bookName) {
    return filteredReviews.value[0].bookName
  }

  return null
})

// 跳轉到會員詳細頁面
const goToUserDetail = (userId) => {
  router.push({
    name: 'userDetail',
    params: { id: userId },
  })
}

// 權限判斷邏輯
const canToggleStatus = (item) => {
  const myRole = Number(currentUserRole.value)
  const myUserId = Number(currentUserId.value)
  const targetRole = Number(item.userType)
  const targetUserId = Number(item.userId)
  if (isNaN(myRole)) return false
  if (myRole === 0) return true
  
  if (myRole === 1) {
    if (targetRole === 2) return true
    if (targetUserId === myUserId) return true
    return false
  }
  return false
}

// 狀態切換邏輯
const handleToggleStatus = (item) => {
  // 1(顯示)， 0(隱藏)
  const newStatus = item.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '隱藏' : '顯示'
  const confirmColor = newStatus === 0 ? '#d33' : '#4CAF50'

  Swal.fire({
    title: `確定要 ${actionText} 這則評價嗎？`,
    text: newStatus === 0 ? '隱藏後，前台將無法看到此評價' : '該評價將重新顯示於前台',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: `確定`,
    cancelButtonText: '取消',
    confirmButtonColor: confirmColor,
    cancelButtonColor: '#aaa',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const updatedReview = { ...item, status: newStatus }

        // 呼叫後端 API
        await axios.put(`/api/public/admin/reviews/${item.reviewId}`, updatedReview)

        // 更新前端畫面
        item.status = newStatus

        Swal.fire({
          icon: 'success',
          title: '更新成功',
          timer: 1000,
          showConfirmButton: false,
        })
      } catch (error) {
        console.error('更新狀態失敗:', error)
        Swal.fire({
          icon: 'error',
          title: '更新失敗',
          text: '請稍後再試',
        })
      }
    }
  })
}

const getUserIdFromToken = () => {
  const token = localStorage.getItem('userToken')
  if (!token) return null

  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map(function (c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        })
        .join(''),
    )

    const payload = JSON.parse(jsonPayload)

    // ★ 重要：後端 JWT 欄位名稱
    // 通常是 'userId', 'id', 或 'sub'
    // 如果你的後端把 ID 放在 'sub' (Subject)，就回傳 payload.sub
    return payload.userId || payload.id || payload.sub
  } catch (e) {
    console.error('Token 解析失敗', e)
    return null
  }
}

// 檢舉邏輯
const handleReport = async (item) => {
  const currentUserId = getUserIdFromToken()

  if (!currentUserId) {
    Swal.fire({
      icon: 'error',
      title: '驗證錯誤',
      text: '無法讀取使用者資訊，請重新登入。',
    })
    return
  }

  const swalOptions = REPORT_OPTIONS.reduce((acc, curr) => {
    acc[curr.value] = curr.label
    return acc
  }, {})

  // 1. 彈出輸入視窗
  const { value: reason } = await Swal.fire({
    title: '檢舉評價',
    input: 'select',
    inputOptions: swalOptions,
    inputPlaceholder: '請選擇檢舉原因',
    showCancelButton: true,
    confirmButtonText: '送出',
    cancelButtonText: '取消',
    confirmButtonColor: '#d33',
    inputValidator: (value) => {
      if (!value) {
        return '請選擇一個原因！'
      }
    },
  })

  // 2. 如果使用者有選擇原因並送出
  if (reason) {
    try {
      const reportData = {
        reviewId: item.reviewId,
        userId: currentUserId, // 這裡帶入當前登入者 ID
        reason: reason,
      }

      await axios.post('/api/public/reports', reportData)

      // 成功提示
      Swal.fire({
        icon: 'success',
        title: '檢舉已送出',
        text: '管理員將會盡快審核您的檢舉。',
        timer: 2000,
        showConfirmButton: false,
      })
    } catch (error) {
      // 處理錯誤 (例如重複檢舉)
      if (error.response && error.response.status === 409) {
        Swal.fire({
          icon: 'warning',
          title: '重複檢舉',
          text: '您已經檢舉過此評價了。',
        })
      } else {
        console.error('檢舉失敗:', error)
        Swal.fire({
          icon: 'error',
          title: '檢舉失敗',
          text: '系統發生錯誤，請稍後再試。',
        })
      }
    }
  }
}

const canReport = (item) => {
  // 隱藏無法檢舉
  if (item.status !== 1) return false 
  // 自己的評價不能檢舉
  if (Number(item.userId) === Number(currentUserId.value)) {
    return false
  }
  return true
}

// 監聽路由變化
watch(
  () => route.params.bookId,
  (newId) => {
    currentBookId.value = newId
  },
)

onMounted(() => {
  loadReviews()
})
</script>

<style scoped lang="scss">
/* 變數定義 */
$primary-color: #2e5c43;
$bg-color-header: #f9fbe7;
$bg-color-hover: #f1f8e9;
$bg-color-even: #fafafa;
$text-grey: #757575;

/* 複製 UserList 的 switch 縮放樣式 */
.v-switch {
  transform: scale(0.8);
}

/* 定義狀態文字顏色 (如果全域沒有定義的話，這裡補上保險) */
.text-success {
  color: #4caf50 !important;
}
.text-error {
  color: #ff5252 !important;
}

.review-container {
  /* padding */
}

.user-link {
  text-decoration: none;
  color: $primary-color;
  font-weight: bold;
  transition: all 0.2s;

  &:hover {
    color: #1b5e20;
    text-decoration: underline;
  }
}

.header-section {
  margin-bottom: 1.5rem;
}

.back-btn {
  color: $text-grey !important;
  margin-bottom: 0.5rem;
}

.title-wrapper {
}

.page-title {
  display: inline-block;
  font-size: 2.125rem;
  font-weight: 700;
  color: $primary-color;
  line-height: 1.2;
}

.sub-info {
  font-size: 1rem;
  color: $text-grey;
  margin-left: 0.75rem;
}

.table-card {
  border-radius: 8px;
  border-top: 4px solid $primary-color !important;
  box-shadow:
    0px 3px 1px -2px rgba(0, 0, 0, 0.2),
    0px 2px 2px 0px rgba(0, 0, 0, 0.14),
    0px 1px 5px 0px rgba(0, 0, 0, 0.12);
}

.forest-table {
  :deep(.v-data-table-header) {
    background-color: $bg-color-header !important;
  }

  :deep(.v-data-table-header__content) {
    font-weight: bold;
    color: $primary-color;
    font-size: 1rem;
  }

  :deep(.v-data-table__tr:nth-child(even)) {
    background-color: $bg-color-even;
  }

  :deep(.v-data-table__tr:hover) {
    background-color: $bg-color-hover !important;
  }
}

.action-btn {
  // color: $primary-color !important;
}

.no-data-text {
  padding-top: 1.25rem;
  padding-bottom: 1.25rem;
  color: $text-grey;
}
</style>
