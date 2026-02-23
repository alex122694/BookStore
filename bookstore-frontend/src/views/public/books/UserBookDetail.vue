<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import bookService from '@/api/bookService.js'
import Swal from 'sweetalert2'
import axios from 'axios'
import { useCartStore } from '@/stores/cartStore'
import orderService from '@/api/orderService.js'
import BookReviews from '@/views/public/reviews/BookReviews.vue'
import { useLoginCheck } from '@/composables/useLoginCheck'
import bookClubService from '@/api/bookClubService.js'
import { useUserStore } from '@/stores/userStore'
import { statusMap } from '@/views/public/club/UserBookClubConfig.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const book = ref({})
const loading = ref(false)
const cartStore = useCartStore()
const { validateLogin } = useLoginCheck()
const quantity = ref(1)
const isFavorited = ref(false)
const userId = localStorage.getItem('userId')

const bookId = route.params.id
const relatedClubs = ref([])
const myRegistrationIds = ref(new Set())

// 載入書籍詳情
const loadBookMsg = async () => {
  loading.value = true
  try {
    const response = await bookService.getBook(bookId)
    book.value = response.data

    // 載入相關讀書會
    try {
      const clubResponse = await bookClubService.getAllClubs()
      if (clubResponse.data) {
        // Filter: 1(Registering), 3(Full), 4(Deadline), 5(Ended)
        relatedClubs.value = clubResponse.data.filter(club =>
          club.book && club.book.bookId == bookId && [1, 3, 4, 5].includes(club.status)
        )
      }
      // Load registration status
      await loadMyRegistrations();
    } catch (e) {
      console.error('相關讀書會載入失敗', e)
    }

  } catch (error) {
    console.error('載入書籍詳情失敗:', error)
    Swal.fire('錯誤', '無法載入書籍資料', 'error')
  } finally {
    loading.value = false
  }
}

// 圖片路徑
const imageUrl = computed(() => {
  if (book.value.bookImageBean && book.value.bookImageBean.imageUrl) {
    return `http://localhost:8080/upload-images/${book.value.bookImageBean.imageUrl}`
  }
  return ''
})

// 格式化金額
const formattedPrice = computed(() => {
  return book.value.price ? `$${Number(book.value.price).toLocaleString()}` : '$0'
})

const addToCart = async () => {
  // Preemptive login check
  if (!await validateLogin('請先登入後再加入購物車')) return;

  try {
    const response = await orderService.addToCart(book.value.bookId, quantity.value)

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '加入成功',
        text: `${book.value.bookName} 已加入購物車`,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500,
      })

      // 更新全域狀態
      if (response.data.cartCount !== undefined) {
        cartStore.setCartCount(response.data.cartCount)
      } else {
        cartStore.fetchCartCount()
      }
    } else {
      Swal.fire('加入失敗', response.data.message, 'error')
    }
  } catch (error) {
    if (error.response && error.response.status === 401) {
      // Should be caught by validateLogin if state is correct, but double safety
      await validateLogin('驗證失效，請重新登入');
    } else {
      console.error(error)
      Swal.fire('錯誤', '加入購物車失敗', 'error')
    }
  }
}

// 立即購買
const buyNow = async () => {
  if (!await validateLogin('請先登入後再購買')) return;

  try {
    const response = await orderService.addToCart(book.value.bookId, quantity.value)

    if (response.data.success) {
      // 更新全域狀態
      if (response.data.cartCount !== undefined) {
        cartStore.setCartCount(response.data.cartCount)
      } else {
        cartStore.fetchCartCount()
      }
      router.push({ name: 'cart' })
    } else {
      Swal.fire('購買失敗', response.data.message, 'error')
    }
  } catch (error) {
    if (error.response && error.response.status === 401) {
      await validateLogin('驗證失效，請重新登入');
    } else {
      console.error(error)
      Swal.fire('錯誤', '無法加入購物車', 'error')
    }
  }
}

const checkFavoriteStatus = async () => {
  if (!userId || !bookId) return
  try {
    const response = await axios.get(`http://localhost:8080/api/wishlist/check`, {
      params: { userId: parseInt(userId), bookId: parseInt(bookId) }
    })
    isFavorited.value = response.data
  } catch (error) {
    console.error('檢查收藏狀態失敗:', error)
  }
}

// 收藏
const addToCollection = async () => {
  if (!await validateLogin('登入後即可收藏書籍')) return;

  try {
    const response = await axios.post('http://localhost:8080/api/wishlist/toggle', {
      userId: parseInt(userId),
      bookId: parseInt(bookId)
    })

    if (response.data.success) {
      isFavorited.value = response.data.isFavorited

      Swal.fire({
        icon: 'success',
        title: response.data.message,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500,
      })
    }
  } catch (error) {
    console.error('收藏操作失敗:', error)
    Swal.fire('錯誤', '操作失敗，請稍後再試', 'error')
  }
}

const goBack = () => {
  router.go(-1)
}

const recordView = async () => {
  const userId = localStorage.getItem('userId')

  if (userId && bookId) {
    try {
      await axios.post('http://localhost:8080/api/history/record', {
        userId: parseInt(userId),
        bookId: parseInt(bookId)
      })
      console.log('瀏覽紀錄已更新')
    } catch (error) {
      console.error('更新瀏覽紀錄失敗:', error)
    }
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const navigateToClub = (clubId) => {
  router.push({
    name: 'user-bookclub-detail-page',
    params: { id: clubId }
  })
}

// 載入我報名的讀書會
const loadMyRegistrations = async () => {
  if (!userStore.isLoggedIn) return;
  try {
    const response = await bookClubService.getMyRegistrations();
    if (response.data) {
      // Filter active registrations (1=Registering, 3=Full, 4=Deadline)
      const activeRegs = response.data.filter(r => [1, 3, 4].includes(r.status));
      myRegistrationIds.value = new Set(activeRegs.map(r => r.bookClub.clubId));
    }
  } catch (error) {
    console.error('載入報名資料失敗', error);
  }
};

// 快速報名
const handleRegister = async (club) => {
  // Check if user is logged in
  if (!await validateLogin('您必須先登入帳號才能報名參加讀書會。')) return;

  Swal.fire({
    title: '確認報名',
    text: `確定要報名「${club.clubName}」嗎？`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '確定報名',
    cancelButtonText: '取消',
    confirmButtonColor: '#2E5C43'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await bookClubService.register(club.clubId);
        Swal.fire('成功', '報名成功！', 'success');
        // Refresh data
        await loadMyRegistrations();
        await loadBookMsg(); // Refresh club list (participants count)
      } catch (error) {
        Swal.fire('失敗', error.response?.data?.message || '報名失敗', 'error');
      }
    }
  });
};

// 取消報名
const handleCancel = (club) => {
  Swal.fire({
    title: '取消報名',
    text: `確定要取消「${club.clubName}」的報名嗎？`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定取消',
    cancelButtonText: '保留',
    confirmButtonColor: '#d33'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await bookClubService.cancelRegistration(club.clubId);
        Swal.fire('成功', '已取消報名', 'success');
        // Refresh data
        await loadMyRegistrations();
        await loadBookMsg();
      } catch (error) {
        Swal.fire('失敗', error.response?.data?.message || '取消失敗', 'error');
      }
    }
  });
};

const getDisplayStatus = (item) => {
  if (item.status === 1 && item.currentParticipants >= item.maxParticipants) {
    return statusMap[3];
  }
  return statusMap[item.status] || { text: '未知', color: 'grey' };
};

onMounted(() => {
  if (bookId) {
    loadBookMsg()
    recordView()
    checkFavoriteStatus()
  }
})
</script>

<template>
  <v-container class="book-detail-container py-8">
    <!-- 麵包屑/返回 -->
    <v-btn variant="text" prepend-icon="mdi-arrow-left" @click="goBack" class="mb-4">
      返回列表
    </v-btn>

    <v-card v-if="!loading && book.bookId" class="pa-6 rounded-lg elevation-2">
      <v-row>
        <!-- 左側：圖片區 -->
        <v-col cols="12" md="4" class="d-flex justify-center align-start">
          <div class="image-wrapper elevation-3 rounded">
            <v-img v-if="imageUrl" :src="imageUrl" cover max-width="100%" aspect-ratio="0.7"></v-img>
            <v-sheet v-else color="grey-lighten-3" class="d-flex align-center justify-center" height="400" width="300">
              <v-icon icon="mdi-book-open-page-variant" size="80" color="grey"></v-icon>
            </v-sheet>
          </div>
        </v-col>

        <!-- 右側：資訊區 -->
        <v-col cols="12" md="8">
          <div class="d-flex flex-column h-100">
            <!-- 書名 -->
            <h1 class="text-h4 font-weight-bold text-primary mb-2">{{ book.bookName }}</h1>

            <!-- 其他資訊 -->
            <div class="text-subtitle-1 mb-4 text-grey-darken-2">
              <div class="mb-1"><strong class="me-2">作者:</strong> {{ book.author }}</div>
              <div class="mb-1">
                <strong class="me-2">譯者:</strong> {{ book.translator || '無' }}
              </div>
              <div class="mb-1"><strong class="me-2">出版社:</strong> {{ book.press }}</div>
              <div class="mb-1">
                <strong class="me-2">庫存:</strong>
                <span :class="book.stock > 0 ? 'text-success' : 'text-error'">
                  {{ book.stock > 0 ? `現貨 ${book.stock} 本` : '暫無庫存' }}
                </span>
              </div>
            </div>

            <v-divider class="mb-4"></v-divider>

            <!-- 價格 -->
            <div class="d-flex align-baseline mb-6">
              <span class="text-h6 text-grey me-2">售價:</span>
              <span class="text-h3 font-weight-bold price-text">{{ formattedPrice }}</span>
            </div>

            <v-spacer></v-spacer>

            <!-- 按鈕區 -->
            <div class="d-flex gap-3 mt-4">
              <v-btn variant="outlined" :color="isFavorited ? 'error' : 'primary'" size="large"
                :prepend-icon="isFavorited ? 'mdi-heart' : 'mdi-heart-outline'" @click="addToCollection">
                {{ isFavorited ? '已收藏' : '收藏' }}
              </v-btn>
              <v-btn variant="tonal" color="secondary" size="large" prepend-icon="mdi-cart-plus" class="ms-3"
                @click="addToCart" :disabled="book.stock <= 0">
                加入購物車
              </v-btn>
              <v-btn variant="flat" color="primary" size="large" class="ms-3 flex-grow-1" @click="buyNow"
                :disabled="book.stock <= 0">
                立即購買
              </v-btn>
            </div>
          </div>
        </v-col>
      </v-row>

      <v-divider class="my-6"></v-divider>

      <!-- 下方：簡介區 -->
      <div class="description-section">
        <h3 class="text-h5 font-weight-bold text-primary mb-4">內容簡介</h3>
        <div class="text-body-1 text-grey-darken-3 lh-loose">
          {{ book.shortDesc || '暫無簡介' }}
        </div>
      </div>

      <v-divider class="my-6"></v-divider>

      <!-- 讀書會場次區塊 -->
      <div class="club-section mb-6" v-if="relatedClubs.length > 0">
        <h3 class="text-h5 font-weight-bold text-primary mb-4">相關讀書會場次</h3>

        <v-card variant="outlined" class="rounded-lg overflow-hidden border-primary">
          <v-table hover>
            <thead>
              <tr class="bg-grey-lighten-4">
                <th class="text-left font-weight-bold">讀書會名稱</th>
                <th class="text-center font-weight-bold">活動時間</th>
                <th class="text-center font-weight-bold">地點</th>
                <th class="text-center font-weight-bold">報名狀況</th>
                <th class="text-center font-weight-bold">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="club in relatedClubs" :key="club.clubId">
                <td>{{ club.clubName }}</td>
                <td class="text-center">{{ formatDate(club.eventDate) }}</td>
                <td class="text-center">{{ club.location }}</td>
                <td class="text-center">
                  {{ club.currentParticipants || 0 }} / {{ club.maxParticipants }}
                  <v-chip size="small" class="ml-2 font-weight-bold" :color="getDisplayStatus(club).color">
                    {{ getDisplayStatus(club).text }}
                  </v-chip>
                </td>
                <td class="text-center">
                  <div class="d-flex justify-center gap-2">
                    <v-btn size="small" color="info" variant="text" @click="navigateToClub(club.clubId)">
                      詳情
                    </v-btn>

                    <template v-if="myRegistrationIds.has(club.clubId)">
                      <!-- <v-btn size="small" color="error" variant="flat" @click="handleCancel(club)">
                        取消報名
                      </v-btn> -->
                    </template>
                    <template v-else>
                      <!-- <v-btn size="small" color="primary" variant="elevated" @click="handleRegister(club)"
                        :disabled="club.currentParticipants >= club.maxParticipants">
                        {{ club.currentParticipants >= club.maxParticipants ? '已滿' : '報名' }}
                      </v-btn> -->
                    </template>
                  </div>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card>
      </div>

      <div class="review-section-wrapper">
        <BookReviews v-if="bookId" :book-id="bookId" />
      </div>
    </v-card>

    <!-- 載入中 -->
    <div v-else-if="loading" class="text-center pa-10">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>
  </v-container>
</template>

<style scoped>
.book-detail-container {
  max-width: 1000px;
}

.image-wrapper {
  width: 100%;
  max-width: 350px;
  overflow: hidden;
}

.price-text {
  color: #800020;
}

.lh-loose {
  line-height: 1.8;
  white-space: pre-wrap;
  /* 保留換行 */
}

.review-section-wrapper {
  margin-top: 50px;
  padding-top: 30px;
  border-top: 1px solid #e0d9c9;
}
</style>
