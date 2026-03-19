<template>
  <v-container class="reviews-container">
    <div class="reviews-header">
      <div class="header-left">
        <h2 class="main-title">書籍評價</h2>
        <v-chip color="primary" variant="outlined" class="count-chip">
          共 {{ reviews.length }} 則評價
        </v-chip>
      </div>

      <div class="rating-summary">
        <span class="score-text">
          {{ averageRating }}
        </span>
        <div class="stars-wrapper">
          <v-rating
            :model-value="Number(averageRating)"
            color="amber-darken-2"
            density="compact"
            half-increments
            readonly
            size="small"
          ></v-rating>
          <div class="caption-text">平均星等</div>
        </div>
      </div>
    </div>

    <v-card class="write-review-card" elevation="2" border>
      <v-card-title class="card-header"> 撰寫評價 </v-card-title>
      <v-card-text class="card-body">
        <v-form ref="form" v-model="valid">
          <div class="rating-input-group">
            <div class="input-label">您的評分 (必填)</div>
            <v-rating
              v-model="newReview.rating"
              color="amber-darken-2"
              hover
              density="comfortable"
            ></v-rating>
            <div v-if="errors.rating" class="error-msg">請點選星星給予評分</div>
          </div>

          <v-textarea
            v-model="newReview.comment"
            label="寫下您的心得(必填)..."
            placeholder="這本書最讓你印象深刻的是什麼？"
            variant="outlined"
            rows="3"
            auto-grow
            counter="250"
            :error="errors.comment"
            :error-messages="errors.comment ? errors.commentMsg : ''"
          ></v-textarea>
        </v-form>
      </v-card-text>

      <v-card-actions class="card-actions">
        <v-spacer></v-spacer>
        <v-btn
          color="primary"
          variant="elevated"
          @click="submitReview"
          prepend-icon="mdi-send"
          :loading="isSubmitting"
        >
          送出
        </v-btn>
      </v-card-actions>
    </v-card>

    <v-divider class="section-divider"></v-divider>

    <div v-if="reviews.length > 0" class="reviews-list">
      <v-card
        v-for="review in paginatedReviews"
        :key="review.reviewId"
        class="review-item-card"
        :class="{ 'is-hidden': review.status === 0 }"
        border
      >
        <v-card-text>
          <div class="review-content-wrapper">
            <div class="avatar-col">
              <v-avatar color="secondary" size="48">
                <span class="avatar-text">{{
                  (review.userName && review.userName[0]) || review.userId
                }}</span>
              </v-avatar>
            </div>

            <div class="content-col">
              <div class="meta-row">
                <div class="user-info-group">
                  <div class="user-name-row">
                    <div class="user-name">
                      {{ review.userName || `User ${review.userId}` }}
                    </div>

                    <v-chip
                      v-if="review.status === 0"
                      color="error"
                      size="x-small"
                      variant="flat"
                      class="hidden-tag"
                    >
                      已隱藏
                    </v-chip>
                  </div>

                  <div class="rating-date-row">
                    <v-rating
                      :model-value="review.rating"
                      color="amber-darken-2"
                      density="comfortable"
                      readonly
                      size="x-small"
                      class="mini-rating"
                    ></v-rating>

                    <div class="date-text">
                      {{ formatDate(review.createdAt) }}
                      <span
                        v-if="review.updatedAt && review.updatedAt !== review.createdAt"
                        class="edited-mark"
                      >
                        (已編輯)
                      </span>
                    </div>
                  </div>
                </div>

                <div class="action-btn-group">
                  <v-btn
                    v-if="isOwner(review)"
                    variant="text"
                    size="small"
                    color="primary"
                    icon
                    class="icon-btn"
                    @click="openEditDialog(review)"
                  >
                    <v-icon>mdi-pencil</v-icon>
                    <v-tooltip activator="parent" location="top">編輯</v-tooltip>
                  </v-btn>

                  <v-btn
                    v-if="canDelete(review)"
                    variant="text"
                    size="small"
                    color="error"
                    icon
                    class="icon-btn"
                    @click="handleDelete(review)"
                  >
                    <v-icon>mdi-delete</v-icon>
                    <v-tooltip activator="parent" location="top">刪除</v-tooltip>
                  </v-btn>

                  <v-btn
                    v-if="canToggleStatus(review)"
                    variant="text"
                    size="small"
                    :color="review.status === 1 ? 'warning' : 'success'"
                    icon
                    class="icon-btn status-btn"
                    @click="handleToggleStatus(review)"
                  >
                    <v-icon>{{ review.status === 1 ? 'mdi-eye-off' : 'mdi-eye' }}</v-icon>
                    <v-tooltip activator="parent" location="top">
                      {{ review.status === 1 ? '隱藏' : '顯示' }}
                    </v-tooltip>
                  </v-btn>

                  <v-btn
                    v-if="!isOwner(review)"
                    variant="text"
                    size="small"
                    color="grey-lighten-1"
                    icon
                    class="report-btn"
                    @click="handleReport(review)"
                  >
                    <v-icon>mdi-alert-circle-outline</v-icon>
                    <v-tooltip activator="parent" location="top">檢舉</v-tooltip>
                  </v-btn>
                </div>
              </div>

              <div class="comment-body">
                {{ review.comment || '此使用者未填寫文字評價。' }}
              </div>
            </div>
          </div>
        </v-card-text>
      </v-card>

      <div class="pagination-wrapper">
        <v-pagination
          v-model="page"
          :length="totalPages"
          :total-visible="7"
          color="primary"
          rounded="circle"
        ></v-pagination>
      </div>
    </div>

    <v-sheet v-else class="empty-state">
      <v-icon icon="mdi-message-text-outline" size="64" class="empty-icon"></v-icon>
      <div class="empty-text">目前還沒有評價，搶先成為第一個評價者吧！</div>
    </v-sheet>

    <v-dialog v-model="editDialog" max-width="750">
      <v-card>
        <v-card-title class="dialog-header">編輯評價</v-card-title>
        <v-card-text class="dialog-body">
          <v-form ref="editForm">
            <div class="rating-input-group">
              <div class="input-label">調整評分</div>
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
              rows="3"
              counter="250"
              auto-grow
              :error="editErrors.comment"
              :error-messages="editErrors.comment ? editErrors.commentMsg : ''"
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="elevated" @click="submitEdit" :loading="isUpdating"
            >儲存</v-btn
          >
          <v-btn color="grey" variant="text" @click="editDialog = false">取消</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="reportDialog" max-width="500">
      <v-card class="rounded-lg">
        <v-card-title class="dialog-header d-flex align-center">
          <v-icon icon="mdi-alert-octagon-outline" class="mr-2" size="small"></v-icon>
          檢舉評價
        </v-card-title>

        <v-card-text class="dialog-body pt-4">
          <div class="mb-4 text-body-1">
            您想要檢舉
            <span class="font-weight-bold text-primary">{{ reportData.reviewUserName }}</span>
            的評價嗎？
          </div>

          <v-select
            v-model="reportData.reason"
            :items="REPORT_OPTIONS"
            item-title="label"
            item-value="value"
            label="請選擇檢舉原因"
            placeholder="請點擊選擇..."
            variant="outlined"
            color="primary"
            density="comfortable"
            hide-details
            class="mb-2"
          >
            <template v-slot:item="{ props, item }">
              <v-list-item v-bind="props">
                <template v-slot:prepend></template>
              </v-list-item>
            </template>
          </v-select>
        </v-card-text>

        <v-card-actions class="px-4 pb-4">
          <v-spacer></v-spacer>
          <v-btn color="error" variant="flat" @click="submitReport" :loading="isReporting">
            送出
          </v-btn>
          <v-btn variant="text" color="grey-darken-1" @click="reportDialog = false">取消</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Swal from 'sweetalert2'
import axios from 'axios'
import reviewService from '@/modules/review/api'
import { useUserStore } from '@/modules/user/store/userStore'
import { REPORT_OPTIONS } from '@/common/utils/reportOptions.js'

const props = defineProps({
  bookId: {
    type: [Number, String],
    default: 1,
  },
})

const userStore = useUserStore()

// --- 狀態 ---
const reviews = ref([])
const isSubmitting = ref(false)
const valid = ref(false)

// --- 分頁 ---
const page = ref(1)
const itemsPerPage = 5

// --- 編輯 ---
const editDialog = ref(false)
const isUpdating = ref(false)
const editingData = ref({ reviewId: null, rating: 0, comment: '' })
const newReview = ref({ rating: 0, comment: '' })
const errors = ref({ rating: false, comment: false, commentMsg: '' })
const editErrors = ref({ comment: false, commentMsg: '' })

// --- 檢舉 ---
const reportDialog = ref(false)
const isReporting = ref(false)
const reportData = ref({ reviewId: null, reviewUserName: '', reason: null })

// --- JWT Helper ---
const parseJwt = (token) => {
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
    return JSON.parse(jsonPayload)
  } catch (e) {
    return null
  }
}

const currentUserId = computed(() => {
  let id = userStore.userId || localStorage.getItem('userId')
  if (!id) {
    const token = localStorage.getItem('userToken')
    if (token) {
      const decoded = parseJwt(token)
      if (decoded) id = decoded.userId || decoded.id || decoded.sub
    }
  }
  return id ? Number(id) : null
})

const currentUserRole = computed(() => {
  let role = userStore.role
  if (role === undefined || role === null || role === '') {
    role = localStorage.getItem('userRole')
  }
  if (
    role === undefined ||
    role === null ||
    role === '' ||
    role === 'undefined' ||
    role === 'null'
  ) {
    return null
  }
  if (role === 'SUPER_ADMIN') return 0
  if (role === 'ADMIN') return 1
  if (role === 'USER') return 2
  const roleNumber = Number(role)
  return isNaN(roleNumber) ? null : roleNumber
})

const isOwner = (review) => {
  if (!currentUserId.value) return false
  return review.userId === currentUserId.value
}

const canDelete = (review) => {
  if (currentUserId.value && review.userId === currentUserId.value) {
    return true
  }
  return false
}

// 狀態管理權限：判斷誰可以執行「隱藏/顯示」
const canToggleStatus = (review) => {
  if (currentUserRole.value === null) return false

  const myRole = Number(currentUserRole.value)
  const myUserId = Number(currentUserId.value) 
  const targetRole = Number(review.userType)
  const targetUserId = Number(review.userId)
  
  if (myRole === 0) return true
  if (myRole === 1) {
    if (targetRole === 2) return true
    if (targetUserId === myUserId) return true
    return false
  }
  return false
}

const fetchReviews = async () => {
  try {
    const response = await reviewService.getAllReviews();
    const allReviews = response.data;

    reviews.value = allReviews.filter((r) => {
      const isCurrentBook = r.bookId == props.bookId
      const isVisible = r.status === 1 ||canToggleStatus(r)
      return isCurrentBook && isVisible
    })

    reviews.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    page.value = 1
  } catch (error) {
    console.error('取得評價失敗:', error)
  }
}

watch(
  () => props.bookId,
  () => {
    fetchReviews()
  },
)

onMounted(() => {
  fetchReviews()
})

const averageRating = computed(() => {
  const activeReviews = reviews.value.filter((r) => r.status === 1)
  if (activeReviews.length === 0) return 0
  const sum = activeReviews.reduce((acc, curr) => acc + curr.rating, 0)
  return (sum / activeReviews.length).toFixed(1)
})

const totalPages = computed(() => {
  return Math.ceil(reviews.value.length / itemsPerPage)
})

const paginatedReviews = computed(() => {
  const start = (page.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return reviews.value.slice(start, end)
})

const formatDate = (dateArrayOrString) => {
  if (!dateArrayOrString) return ''
  let date
  if (Array.isArray(dateArrayOrString)) {
    date = new Date(
      dateArrayOrString[0],
      dateArrayOrString[1] - 1,
      dateArrayOrString[2],
      dateArrayOrString[3],
      dateArrayOrString[4],
      dateArrayOrString[5] || 0,
    )
  } else {
    date = new Date(dateArrayOrString)
  }
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const submitReview = async () => {
  if (!currentUserId.value) {
    Swal.fire({
      icon: 'warning',
      title: '請先登入',
      text: '登入後才能發表評價',
      confirmButtonText: '前往登入',
      confirmButtonColor: '#2E5C43',
    }).then((result) => {
      if (result.isConfirmed) {
        window.location.href = '/dev/user/login'
      }
    })
    isSubmitting.value = false
    return
  }

  errors.value.rating = false
  errors.value.comment = false
  errors.value.commentMsg = ''

  if (newReview.value.rating === 0) {
    errors.value.rating = true
  }

  const comment = newReview.value.comment

  if (!comment || !comment.trim()) {
    errors.value.comment = true
    errors.value.commentMsg = '請填寫評價內容'
  } else if (comment.length > 250) {
    errors.value.comment = true
    errors.value.commentMsg = '評價內容不能超過 250 字'
  }

  if (errors.value.rating || errors.value.comment) return

  isSubmitting.value = true
  let userName = userStore.userName || localStorage.getItem('userName')

  const payload = {
    userId: Number(currentUserId.value),
    bookId: props.bookId,
    rating: newReview.value.rating,
    comment: newReview.value.comment,
  }

  try {
    const response = await reviewService.createReview(payload)
    const newReviewData = response.data

    if (!newReviewData.userName && userName) newReviewData.userName = userName
    if (newReviewData.userType === undefined || newReviewData.userType === null) {
      newReviewData.userType = currentUserRole.value
    }
    if (newReviewData.status === undefined) newReviewData.status = 1

    reviews.value.unshift(newReviewData)
    page.value = 1

    Swal.fire({
      icon: 'success',
      title: '評價已送出',
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 1500,
    })
    newReview.value.rating = 0
    newReview.value.comment = ''
  } catch (error) {
    console.error(error)
    Swal.fire({ icon: 'error', title: '評價失敗', text: '系統發生錯誤，請稍後再試' })
  } finally {
    isSubmitting.value = false
  }
}

const openEditDialog = (review) => {
  editingData.value = {
    reviewId: review.reviewId,
    rating: review.rating,
    comment: review.comment,
  }
  editErrors.value.comment = false
  editErrors.value.commentMsg = ''
  editDialog.value = true
}

const submitEdit = async () => {
  editErrors.value.comment = false
  editErrors.value.commentMsg = ''
  const comment = editingData.value.comment

  if (!comment || !comment.trim()) {
    editErrors.value.comment = true
    editErrors.value.commentMsg = '請填寫評價內容'
  } else if (comment.length > 250) {
    editErrors.value.comment = true
    editErrors.value.commentMsg = '評價內容不能超過 250 字'
  }

  if (editErrors.value.comment) return

  isUpdating.value = true
  try {
    const payload = {
      bookId: props.bookId,
      userId: currentUserId.value,
      rating: editingData.value.rating,
      comment: editingData.value.comment,
    }
    await reviewService.updateReview(editingData.value.reviewId, payload)
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
        const index = reviews.value.findIndex((r) => r.reviewId === review.reviewId)
        if (index !== -1) reviews.value.splice(index, 1)
        Swal.fire('已刪除', '您的評價已成功刪除。', 'success')
        if (paginatedReviews.value.length === 0 && page.value > 1) page.value--
      } catch (error) {
        console.error(error)
        Swal.fire('錯誤', '刪除失敗，請稍後再試。', 'error')
      }
    }
  })
}

const handleToggleStatus = (review) => {
  const newStatus = review.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '隱藏' : '顯示'
  const confirmColor = newStatus === 0 ? '#d33' : '#4CAF50'

  Swal.fire({
    title: `確定要${actionText}這則評價嗎？`,
    text: newStatus === 0 ? '隱藏後，一般使用者將無法看到此評價' : '該評價將重新顯示於前台',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: `確定`,
    cancelButtonText: '取消',
    confirmButtonColor: confirmColor,
    cancelButtonColor: '#aaa',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const payload = {
          ...review,
          status: newStatus,
        }

        await reviewService.updateReview(review.reviewId, payload)

        review.status = newStatus

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

const handleReport = async (review) => {

  if (review.status === 0) {
    Swal.fire({
      icon: 'warning',
      title: '無法檢舉',
      text: '此評論已被隱藏，無法檢舉',
    })
    return
  }

  if (!currentUserId.value) {
    Swal.fire({
      icon: 'warning',
      title: '請先登入',
      text: '登入後才能檢舉',
      confirmButtonText: '前往登入',
      confirmButtonColor: '#2E5C43',
    }).then((result) => {
      if (result.isConfirmed) window.location.href = '/dev/user/login'
    })
    return
  }

  reportData.value = {
    reviewId: review.reviewId,
    reviewUserName: review.userName || `User ${review.userId}`,
    reason: null,
  }

  reportDialog.value = true
}

// --- 4. 新增 submitReport 方法 (用來處理彈窗送出) ---
const submitReport = async () => {
  if (!reportData.value.reason) {
    Swal.fire({ icon: 'warning', title: '請選擇原因', text: '請選擇一個檢舉原因', timer: 1500 })
    return
  }

  isReporting.value = true
  try {
    const payload = {
      reviewId: reportData.value.reviewId,
      userId: currentUserId.value, // 檢舉人 ID
      reason: reportData.value.reason, // 傳送標準代碼 (如 SPAM)
      status: '待處理',
    }

    // 發送 API
    await reviewService.createReport(payload)

    reportDialog.value = false
    Swal.fire({
      icon: 'success',
      title: '檢舉已送出',
      text: '我們會盡快審核您的回報，謝謝！',
      timer: 2000,
      showConfirmButton: false,
    })
  } catch (error) {
    console.error(error)
    // 處理重複檢舉錯誤
    if (error.response && error.response.status === 409) {
      Swal.fire({ icon: 'warning', title: '重複檢舉', text: '您已經檢舉過這則評價了。' })
      reportDialog.value = false
    } else {
      Swal.fire({ icon: 'error', title: '檢舉失敗', text: '系統忙碌中，請稍後再試' })
    }
  } finally {
    isReporting.value = false
  }
}
</script>

<style scoped lang="scss">
// ===== 變數定義 =====
$primary-color: #2e5c43;
$secondary-color: #4caf50;
$error-color: #ff5252;
$warning-color: #fb8c00;
$text-grey: #757575;
$text-light-grey: #bdbdbd;
$bg-light: #f5f5f5;
$amber-color: #f57c00;

.reviews-container {
  /* padding */
}

// ===== Header 區域 =====
.reviews-header {
  margin-bottom: 1.5rem;

  .header-left {
    display: flex;
    align-items: center;
    margin-bottom: 0.5rem;

    .main-title {
      font-size: 2.125rem;
      font-weight: 700;
      margin-right: 1rem;
    }

    .count-chip {
      font-weight: 500;
    }
  }

  .rating-summary {
    display: flex;
    align-items: center;
    margin-top: 0.5rem;

    .score-text {
      font-size: 3rem;
      font-weight: 700;
      color: $amber-color;
      margin-right: 0.5rem;
    }

    .stars-wrapper {
      .caption-text {
        font-size: 0.75rem;
        color: $text-grey;
      }
    }
  }
}

// ===== 撰寫評價卡片 =====
.write-review-card {
  margin-bottom: 1.5rem;

  .card-header {
    background-color: $bg-light;
    font-weight: 600;
  }

  .card-body {
    padding-top: 1rem;

    .rating-input-group {
      margin-bottom: 0.5rem;

      .input-label {
        font-size: 1rem;
        margin-bottom: 0.25rem;
        font-weight: 500;
      }

      .error-msg {
        font-size: 0.75rem;
        color: $error-color;
        margin-top: 0.25rem;
      }
    }
  }

  .card-actions {
    padding: 0 1rem 1rem 1rem;
  }
}

.section-divider {
  margin: 1.5rem 0;
}

// ===== 評價列表區 =====
.reviews-list {
  .review-item-card {
    margin-bottom: 1rem;

    &.is-hidden {
      border-color: $error-color !important;
      background-color: transparent !important;
      opacity: 0.9;
    }

    .review-content-wrapper {
      display: flex;
      flex-direction: row;

      .avatar-col {
        margin-right: 1rem;

        .avatar-text {
          font-size: 1.25rem;
          color: white;
        }
      }

      .content-col {
        flex-grow: 1;

        .meta-row {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;

          .user-info-group {
            .user-name-row {
              display: flex;
              align-items: center;

              .user-name {
                font-size: 1rem;
                font-weight: 700;
              }

              .user-role {
                font-size: 0.75rem;
                color: $text-grey;
                margin-left: 0.5rem;
              }

              .hidden-tag {
                margin-left: 0.5rem;
                font-weight: 700;
              }
            }

            .rating-date-row {
              display: flex;
              align-items: center;
              margin-top: 0.25rem;

              .mini-rating {
                margin-left: -0.25rem;
              }

              .date-text {
                font-size: 0.75rem;
                color: $text-grey;
                margin-left: 0.5rem;

                .edited-mark {
                  margin-left: 0.25rem;
                  color: $text-light-grey;
                }
              }
            }
          }

          .action-btn-group {
            display: flex;
            align-items: center;

            .icon-btn {
              margin-right: 0.25rem;
            }

            .status-btn {
            }

            .report-btn {
              .v-icon {
                font-size: 1.25rem;
              }
            }
          }
        }

        .comment-body {
          margin-top: 0.75rem;
          font-size: 1rem;
          line-height: 1.5;
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 1.5rem;
  }
}

// ===== 空狀態與 Dialog =====
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
  color: $text-grey;

  .empty-icon {
    margin-bottom: 0.5rem;
  }
}

.dialog-header {
  background-color: $primary-color;
  color: white;
}

.dialog-body {
  padding-top: 1rem;
}
</style>
