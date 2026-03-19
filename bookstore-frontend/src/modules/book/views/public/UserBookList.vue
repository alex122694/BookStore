<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import bookService from '@/modules/book/api'
import BookCard from './BookCard.vue'
import Swal from 'sweetalert2'
import axios from 'axios'
import AiAssistant from '@/modules/book/views/public/AiAssistant.vue'
import BookCategorySidebar from '@/common/components/BookCategorySidebar.vue'

const route = useRoute()
const router = useRouter()
const books = ref([])
const genres = ref([])
const loading = ref(false)
const search = ref('')
const viewMode = ref('grid')
const selectedGenreId = ref('all')

const userId = localStorage.getItem('userId');
const favoriteBookIds = ref(new Set());

// 載入書籍與分類
const loadData = async () => {
  loading.value = true
  try {
    const [booksRes, genresRes] = await Promise.all([
      bookService.getAllBooks(),
      bookService.getGenres(),
    ])

    // 只顯示上架中的書籍 (onShelf === 1)
    books.value = booksRes.data.filter((book) => book.onShelf === 1)
    genres.value = genresRes.data

    // 載入收藏清單 (如果已登入)
    if (userId) {
      try {
        const wishlistRes = await axios.get(`http://localhost:8080/api/wishlist/list/${userId}`);
        const ids = wishlistRes.data.map(item => item.book.bookId);
        favoriteBookIds.value = new Set(ids);
      } catch (e) {
        console.error('載入收藏清單失敗', e);
      }
    }

    // 檢查 URL query 是否有指定 genreId
    if (route.query.genreId) {
      // 確保該 genreId 存在於分類列表中
      const targetGenre = genresRes.data.find(
        (g) => String(g.genreId) === String(route.query.genreId),
      )
      if (targetGenre) {
        selectedGenreId.value = targetGenre.genreId
      }
    }
  } catch (error) {
    console.error('載入資料失敗:', error)
    Swal.fire('錯誤', '無法載入書籍資料', 'error')
  } finally {
    loading.value = false
  }
}

// 篩選邏輯
const filteredBooks = computed(() => {
  let result = books.value

  // 1. 分類篩選
  if (selectedGenreId.value !== 'all') {
    result = result.filter(
      (book) => book.genres && book.genres.some((g) => g.genreId === selectedGenreId.value),
    )
  }

  // 2. 搜尋篩選
  if (search.value) {
    const lowerSearch = search.value.toLowerCase()
    result = result.filter(
      (book) =>
        book.bookName.toLowerCase().includes(lowerSearch) ||
        (book.author && book.author.toLowerCase().includes(lowerSearch)),
    )
  }
  return result
})

// 取得圖片 URL helper
const getImageUrl = (book) => {
  if (book.bookImageBean && book.bookImageBean.imageUrl) {
    return `http://localhost:8080/upload-images/${book.bookImageBean.imageUrl}`
  }
  return ''
}

// 格式化金額 helper
const formatPrice = (price) => {
  return price ? `$${Number(price).toLocaleString()}` : '$0'
}

const goToDetail = (bookId) => {
  router.push({ name: 'user-book-detail', params: { id: bookId } })
}

const selectGenre = (genreId) => {
  selectedGenreId.value = genreId
}

// 監聽路由參數變化 (支援同一頁面切換分類)
watch(
  () => route.query.genreId,
  (newGenreId) => {
    if (newGenreId && genres.value.length > 0) {
      const targetGenre = genres.value.find((g) => String(g.genreId) === String(newGenreId))
      if (targetGenre) {
        selectedGenreId.value = targetGenre.genreId
      } else {
        selectedGenreId.value = 'all'
      }
    } else if (!newGenreId) {
      selectedGenreId.value = 'all'
    }
  },
)

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="user-book-list">


    <v-container fluid class="pa-4 pa-md-8 container-max-width">
      <v-row>
        <!-- 左側：分類導覽列 (Desktop only) -->
        <v-col cols="12" md="3" lg="2" class="d-none d-md-block">
          <BookCategorySidebar :genres="genres" :selected-genre-id="selectedGenreId" @select="selectGenre" />
        </v-col>

        <!-- 右側：書籍列表與工具列 -->
        <v-col cols="12" md="9" lg="10">
          <!-- 標題與工具列 -->
          <v-row class="mb-6" align="center">
            <v-col cols="12" sm="5">
              <h2 class="text-h4 font-weight-bold text-primary">書籍專區</h2>
              <p class="text-subtitle-1 text-grey-darken-1">
                <span v-if="selectedGenreId === 'all'">探索我們精心挑選的好書</span>
                <span v-else>
                  分類：{{genres.find((g) => g.genreId === selectedGenreId)?.genreName}}
                </span>
              </p>
            </v-col>

            <v-col cols="12" sm="7" class="d-flex align-center justify-end flex-wrap gap-4">
              <!-- 搜尋 -->
              <v-text-field v-model="search" label="搜尋書籍或作者..." prepend-inner-icon="mdi-magnify" variant="outlined"
                density="compact" hide-details bg-color="white" class="flex-grow-1"
                style="max-width: 300px; min-width: 200px"></v-text-field>

              <!-- 顯示切換 -->
              <v-btn-toggle v-model="viewMode" mandatory color="primary" variant="outlined" density="compact"
                class="ms-2">
                <v-btn value="grid" icon="mdi-view-grid"></v-btn>
                <v-btn value="list" icon="mdi-view-list"></v-btn>
              </v-btn-toggle>

              <!-- 手機版篩選按鈕 (當螢幕小顯示) -->
              <v-menu v-if="$vuetify.display.smAndDown">
                <template v-slot:activator="{ props }">
                  <v-btn v-bind="props" variant="tonal" color="primary" prepend-icon="mdi-filter-variant"
                    class="d-md-none ms-2">
                    分類
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item @click="selectGenre('all')" :active="selectedGenreId === 'all'"
                    title="全部書籍"></v-list-item>
                  <v-list-item v-for="g in genres" :key="g.genreId" @click="selectGenre(g.genreId)"
                    :active="selectedGenreId === g.genreId" :title="g.genreName"></v-list-item>
                </v-list>
              </v-menu>
            </v-col>
          </v-row>

          <!-- 載入中 -->
          <div v-if="loading" class="text-center pa-10">
            <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
            <div class="mt-4 text-h6 text-grey">載入好書中...</div>
          </div>

          <!-- 書籍列表 -->
          <div v-else>
            <!-- GRID VIEW -->
            <v-row v-if="viewMode === 'grid' && filteredBooks.length > 0">
              <v-col v-for="book in filteredBooks" :key="book.bookId" cols="12" sm="6" md="4" lg="3" xl="2">
                <BookCard :book="book" mode="grid" :initial-favorited="favoriteBookIds.has(book.bookId)" />
              </v-col>
            </v-row>

            <!-- LIST VIEW -->
            <div v-else-if="viewMode === 'list' && filteredBooks.length > 0" class="d-flex flex-column gap-3">
              <BookCard v-for="book in filteredBooks" :key="book.bookId" :book="book" mode="list"
                :initial-favorited="favoriteBookIds.has(book.bookId)" />
            </div>

            <!-- 無資料 -->
            <div v-if="filteredBooks.length === 0" class="text-center pa-10 text-grey">
              <v-icon icon="mdi-book-off-outline" size="64" class="mb-4"></v-icon>
              <div class="text-h6">暫無相關書籍</div>
              <v-btn v-if="selectedGenreId !== 'all'" variant="text" color="primary" class="mt-2"
                @click="selectGenre('all')">
                查看所有書籍
              </v-btn>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>

    <!-- <AiAssistant :books="filteredBooks.length > 0 ? filteredBooks : books" /> -->
  </div>
</template>

<style scoped>
.user-book-list {
  max-width: 1400px;
  margin: 0 auto;
}

.container-max-width {
  max-width: 1400px;
  margin: 0 auto;
}

.gap-3 {
  gap: 12px;
}

.gap-4 {
  gap: 16px;
}

.price-text {
  color: #800020;
}

.card-hover {
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  cursor: pointer;
}

.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}
</style>
