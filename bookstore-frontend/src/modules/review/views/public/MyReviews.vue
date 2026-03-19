<template>
  <div class="my-reviews-page">
    <v-container class="py-10">
      <!-- 標題區 -->
      <div class="reviews-header mb-8">
        <div class="text-center">
          <h2 class="forest-main-title">我的評價</h2>
          <v-divider class="mx-auto mt-4" length="60" thickness="4" color="primary"></v-divider>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <div class="mt-4 text-grey">載入評價中...</div>
      </div>

      <div v-else-if="reviews.length === 0" class="text-center py-10">
        <v-sheet class="empty-state pa-10 rounded-xl bg-transparent">
          <v-icon icon="mdi-message-text-outline" size="80" color="grey-lighten-2" class="mb-4"></v-icon>
          <div class="text-h6 text-grey">您還沒有發表過任何評價</div>
          <v-btn color="primary" class="mt-6" to="/dev/user/reviews" size="large" variant="flat" rounded="pill">
            去逛逛書籍
          </v-btn>
        </v-sheet>
      </div>

      <div v-else class="reviews-list-container">
        <!-- 評價列表 -->
        <v-card
          v-for="review in reviews"
          :key="review.reviewId"
          class="review-item-card mb-4"
          border
          elevation="0"
        >
          <v-card-text class="pa-4">
            <div class="review-content-wrapper">
              
            <!-- 右側：內容區 -->
              <div class="content-col">
                        <!-- 標頭：書名 + 日期 -->
                <div class="meta-row d-flex justify-space-between align-start mb-2">
                            <div class="book-info-group">
                                <router-link 
                                :to="`/dev/user/books/${review.bookId}`" 
                                class="book-title-link text-h6 font-weight-bold text-decoration-none text-primary"
                                >
                                {{ review.bookName || '未知書籍' }}
                                </router-link>
                                
                                <div class="rating-date-row d-flex align-center mt-1">
                                <v-rating
                                    :model-value="review.rating"
                                    color="amber-darken-2"
                                    density="compact"
                                    readonly
                                    size="small"
                                    class="mr-2"
                                ></v-rating>
                                <span class="text-caption text-grey">
                                    {{ formatDate(review.createdAt) }}
                                </span>
                                </div>
                            </div>  

                        <!-- 操作按鈕區 (如需要編輯/刪除可放在這) -->
                        <div class="action-btn-group d-flex justify-end mt-2">
                            <v-btn
                            variant="text"
                            size="small"
                            color="primary"
                            icon
                            class="mr-2"
                            @click="openEditDialog(review)"
                            >
                            <v-icon>mdi-pencil</v-icon>
                            <v-tooltip activator="parent" location="top">編輯</v-tooltip>
                            </v-btn>
                            <v-btn
                            variant="text"
                            size="small"
                            color="error"
                            icon
                            @click="handleDelete(review)"
                            >
                            <v-icon>mdi-delete</v-icon>
                            <v-tooltip activator="parent" location="top">刪除</v-tooltip>
                            </v-btn>
                        </div>
                </div>

                <!-- 評論內容 -->
                <div class="comment-body text-body-1 text-grey-darken-3 mt-3 mb-2">
                  {{ review.comment || '沒有文字內容' }}
                </div>

               <!-- 狀態標籤 -->
              <div class="d-flex justify-end mt-2" v-if="review.status === 0">
                <v-chip
                  color="error"
                  size="small"
                  variant="tonal"
                  label
                >
                  已隱藏
                </v-chip>
              </div>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </div>

      <v-dialog v-model="editDialog" max-width="750">
      <v-card class="rounded-lg">
        <v-card-title class="bg-primary text-white px-6 py-4">編輯評價</v-card-title>
        <v-card-text class="pa-6">
          <v-form ref="editForm">
            <div class="mb-4">
              <div class="text-subtitle-1 mb-2">調整評分</div>
              <v-rating
                v-model="editingData.rating"
                color="amber-darken-2"
                hover
                density="comfortable"
              ></v-rating>
            </div>
            <v-textarea
              v-model="editingData.comment"
              label="評價內容"
              variant="outlined"
              rows="4"
              counter="250"
              auto-grow
              :error-messages="editErrorMsg"
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions class="px-6 pb-6">
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="elevated" @click="submitEdit" :loading="isUpdating">
            儲存
          </v-btn>
          <v-btn color="grey-darken-1" variant="text" @click="editDialog = false">取消</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    </v-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import reviewService from '@/modules/review/api'
import Swal from 'sweetalert2'

const reviews = ref([])
const loading = ref(true)
const editDialog = ref(false)
const isUpdating = ref(false)
const editingData = ref({ reviewId: null, rating: 0, comment: '', bookId: null, userId: null })
const editErrorMsg = ref('')

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const fetchReviews = async () => {
    loading.value = true
    const token = localStorage.getItem('userToken')

    if (!token) {
        loading.value = false
        return
    }

    try {
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join(''))
        const payload = JSON.parse(jsonPayload)

        if (!payload.sub) throw new Error('Invalid Token')

        const userRes = await axios.get(`http://localhost:8080/api/data/get/${payload.sub}`, {
            withCredentials: true,
            headers: { Authorization: `Bearer ${token}` }
        })

        if (userRes.data && userRes.data.userId) {
            const userId = userRes.data.userId
            const res = await reviewService.getUserReviews(userId)

            let role = localStorage.getItem('userRole')
            const isNormalUser = (role === 'USER' || role === '2')
            let userReviews = res.data || []
            if (isNormalUser) {
                userReviews = userReviews.filter(r => r.status === 1);
            };
            reviews.value = userReviews;

            reviews.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        } 
    } catch (error) {
        console.error('Fetch reviews error:', error)
    } finally {
        loading.value = false
    }
}

// 開啟編輯視窗
const openEditDialog = (review) => {
  editingData.value = {
    reviewId: review.reviewId,
    bookId: review.bookId,  // 記得要有這兩個 ID
    userId: review.userId,
    rating: review.rating,
    comment: review.comment,
  }
  editErrorMsg.value = ''
  editDialog.value = true
}
// 送出編輯
const submitEdit = async () => {
  editErrorMsg.value = ''
  const comment = editingData.value.comment
  if (!comment || !comment.trim()) {
    editErrorMsg.value = '請填寫評價內容'
    return
  } else if (comment.length > 250) {
    editErrorMsg.value = '評價內容不能超過 250 字'
    return
  }
  isUpdating.value = true
  try {
    const payload = {
      bookId: editingData.value.bookId,
      userId: editingData.value.userId,
      rating: editingData.value.rating,
      comment: editingData.value.comment,
    }
    
    // 呼叫 API 更新
    await reviewService.updateReview(editingData.value.reviewId, payload)
    // 更新前端列表顯示
    const targetReview = reviews.value.find((r) => r.reviewId === editingData.value.reviewId)
    if (targetReview) {
      targetReview.rating = editingData.value.rating
      targetReview.comment = editingData.value.comment
      targetReview.updatedAt = new Date().toISOString()
    }
    editDialog.value = false
    Swal.fire({
      icon: 'success',
      title: '修改成功',
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 1500,
    })
  } catch (error) {
    console.error(error)
    Swal.fire('錯誤', '編輯失敗，請稍後再試。', 'error')
  } finally {
    isUpdating.value = false
  }
}
// 刪除評價
const handleDelete = (review) => {
  Swal.fire({
    title: '確定要刪除嗎？',
    text: '刪除後將無法復原這則評價！',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#aaa',
    confirmButtonText: '刪除',
    cancelButtonText: '取消',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await reviewService.deleteReview(review.reviewId)
        
        // 從列表中移除
        const index = reviews.value.findIndex((r) => r.reviewId === review.reviewId)
        if (index !== -1) reviews.value.splice(index, 1) // 直接移除陣列元素
        
        Swal.fire('已刪除', '您的評價已成功刪除。', 'success')
      } catch (error) {
        console.error(error)
        Swal.fire('錯誤', '刪除失敗，請稍後再試。', 'error')
      }
    }
  })
}

onMounted(() => {
    fetchReviews()
})
</script>

<style scoped lang="scss">
.my-reviews-page {
  min-height: 100vh;
  background-color: #fcf8f0;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2.2rem;
  font-weight: 800;
  letter-spacing: 2px;
}

.reviews-list-container {
    max-width: 900px;
    margin: 0 auto;
}

/* 仿照 BookReviews.vue 的卡片樣式 */
.review-item-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background-color: white;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.08) !important;
    border-color: #2e5c43;
  }
}

.review-content-wrapper {
  display: flex;
  gap: 20px;
}

.avatar-col {
  flex-shrink: 0;
}

.content-col {
  flex-grow: 1;
}

.book-title-link {
    transition: color 0.2s;
    &:hover {
        color: #1b3b29 !important; /* 深綠色 */
        text-decoration: underline !important;
    }
}

.comment-body {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>