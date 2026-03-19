<template>
  <div class="book-reviews-home">
    <!-- 左側：分類導覽列 -->
    <div class="category-sidebar d-none d-md-block">
      <v-card class="rounded-lg elevation-2 border-opacity-75" color="white">
        <v-list class="py-2" density="compact">
          <v-list-subheader class="text-h6 font-weight-bold text-primary mb-2">
            評價專區
          </v-list-subheader>

          <!-- 全部書籍 -->
          <v-list-item
            value="all"
            active-color="primary"
            :active="selectedGenreId === 'all'"
            @click="selectGenre('all')"
            rounded="lg"
            class="mb-2 mx-2"
            variant="tonal"
          >
            <template v-slot:prepend>
              <v-icon icon="mdi-star-circle-outline"></v-icon>
            </template>
            <v-list-item-title class="font-weight-bold">所有評價</v-list-item-title>
          </v-list-item>

          <v-divider class="my-2 mx-4"></v-divider>

          <!-- 分類列表 -->
          <div class="genre-list-container">
            <v-list-item
              v-for="genre in genres"
              :key="genre.genreId"
              :value="genre.genreId"
              active-color="primary"
              :active="selectedGenreId === genre.genreId"
              @click="selectGenre(genre.genreId)"
              rounded="lg"
              class="mb-1 mx-2 genre-item"
            >
              <template v-slot:prepend>
                <v-icon
                  :icon="selectedGenreId === genre.genreId ? 'mdi-tag' : 'mdi-tag-outline'"
                  size="small"
                  :color="selectedGenreId === genre.genreId ? 'primary' : 'grey'"
                ></v-icon>
              </template>
              <v-list-item-title :class="{ 'font-weight-bold': selectedGenreId === genre.genreId }">
                {{ genre.genreName }}
              </v-list-item-title>
            </v-list-item>
          </div>
        </v-list>
      </v-card>
    </div>

    <!-- 右側：書籍列表與工具列 -->
    <v-container>
      <v-row justify="center">
        <v-col cols="12" md="10" lg="10">
          <!-- Header 區域 -->
          <div class="d-flex flex-column flex-sm-row align-center justify-space-between mb-6 gap-4">
            <div>
              <h2 class="text-h4 font-weight-bold text-primary mb-1">讀者評價</h2>
              <div class="text-subtitle-1 text-grey-darken-1">探索最受歡迎的書籍與讀者心得</div>
            </div>

            <div class="d-flex align-center flex-wrap gap-2" style="width: 100%; max-width: 600px">
              <!-- 搜尋 -->
              <v-text-field
                v-model="search"
                label="搜尋書籍..."
                prepend-inner-icon="mdi-magnify"
                variant="outlined"
                density="compact"
                hide-details
                bg-color="white"
                class="flex-grow-1"
                style="min-width: 150px"
              ></v-text-field>

              <!-- 排序 -->
              <v-select
                v-model="sortBy"
                :items="sortOptions"
                item-title="title"
                item-value="value"
                label="排序"
                variant="outlined"
                density="compact"
                hide-details
                bg-color="white"
                style="width: 140px; flex-shrink: 0"
                prepend-inner-icon="mdi-sort"
              ></v-select>

              <!-- 顯示切換 -->
              <v-btn-toggle
                v-model="viewMode"
                mandatory
                color="primary"
                variant="outlined"
                density="compact"
                class="flex-shrink-0"
              >
                <v-btn value="grid" icon="mdi-view-grid"></v-btn>
                <v-btn value="list" icon="mdi-view-list"></v-btn>
              </v-btn-toggle>
            </div>
          </div>

          <!-- 載入中 -->
          <div v-if="loading" class="text-center pa-10">
            <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
            <div class="mt-4 text-h6 text-grey">讀取評價資料中...</div>
          </div>

          <!-- 列表顯示 -->
          <div v-else>
            <!-- GRID VIEW -->
            <v-row v-if="viewMode === 'grid' && filteredBooks.length > 0">
              <v-col
                v-for="book in filteredBooks"
                :key="book.bookId"
                cols="12"
                sm="6"
                md="4"
                lg="4"
                xl="3"
              >
                <!-- 卡片設計 -->
                <v-card
                  class="h-100 rounded-lg elevation-2 card-hover d-flex flex-column"
                  @click="goToDetail(book.bookId)"
                >
                  <div style="position: relative; padding-top: 140%; overflow: hidden">
                    <v-img
                      :src="getImageUrl(book)"
                      cover
                      class="book-cover-img"
                      style="position: absolute; top: 0; left: 0; width: 100%; height: 100%"
                    >
                      <template v-slot:placeholder>
                        <div
                          class="d-flex align-center justify-center fill-height bg-grey-lighten-3"
                        >
                          <v-icon icon="mdi-image-off" color="grey"></v-icon>
                        </div>
                      </template>
                    </v-img>
                    <!-- Badge: 評分 -->
                    <div class="rating-badge" v-if="book.avgRating > 0">
                      <v-icon icon="mdi-star" size="small" color="white" class="mr-1"></v-icon>
                      <span class="font-weight-bold text-white">{{
                        book.avgRating.toFixed(1)
                      }}</span>
                    </div>
                  </div>

                  <v-card-text class="flex-grow-1 d-flex flex-column pt-3">
                    <div
                      class="text-subtitle-1 font-weight-bold text-truncate-2 mb-1"
                      :title="book.bookName"
                    >
                      {{ book.bookName }}
                    </div>
                    <div class="text-caption text-grey mb-2">{{ book.author }}</div>

                    <v-spacer></v-spacer>

                    <div class="d-flex align-center justify-space-between mt-2">
                      <div class="d-flex align-center">
                        <span class="text-caption text-grey mr-1"
                          >({{ book.reviewCount }} 評價)</span
                        >
                      </div>
                      <div class="text-h6 font-weight-bold price-text">
                        {{ formatPrice(book.price) }}
                      </div>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>

            <!-- LIST VIEW -->
            <div
              v-else-if="viewMode === 'list' && filteredBooks.length > 0"
              class="d-flex flex-column gap-3"
            >
              <v-card
                v-for="book in filteredBooks"
                :key="book.bookId"
                class="d-flex flex-row align-center pa-4 rounded-lg elevation-2 card-hover"
                @click="goToDetail(book.bookId)"
              >
                <!-- 圖片 -->
                <div
                  class="me-4"
                  style="width: 80px; height: 110px; flex-shrink: 0; position: relative"
                >
                  <v-img :src="getImageUrl(book)" cover class="rounded" height="100%">
                    <template v-slot:placeholder>
                      <v-sheet
                        color="grey-lighten-3"
                        height="100%"
                        class="d-flex align-center justify-center"
                      >
                        <v-icon icon="mdi-book" color="grey"></v-icon>
                      </v-sheet>
                    </template>
                  </v-img>
                </div>

                <!-- 資訊 -->
                <div class="flex-grow-1">
                  <div class="text-h6 font-weight-bold text-primary">{{ book.bookName }}</div>
                  <div class="text-subtitle-2 text-grey-darken-1">{{ book.author }}</div>

                  <!-- 評分區塊 -->
                  <div class="d-flex align-center mt-1">
                    <v-rating
                      :model-value="book.avgRating"
                      color="amber-darken-2"
                      density="compact"
                      half-increments
                      readonly
                      size="small"
                      class="mr-2"
                    ></v-rating>
                    <span class="text-body-2 font-weight-bold mr-1">{{
                      book.avgRating > 0 ? book.avgRating.toFixed(1) : '尚無評分'
                    }}</span>
                    <span class="text-caption text-grey">({{ book.reviewCount }} 則評價)</span>
                  </div>

                  <div class="text-caption text-grey mt-2 text-truncate" style="max-width: 500px">
                    {{ book.shortDesc || '暫無簡介' }}
                  </div>
                </div>

                <!-- 價格與操作 -->
                <div class="d-flex flex-column align-end ms-4">
                  <div class="text-h5 font-weight-bold price-text mb-2">
                    {{ formatPrice(book.price) }}
                  </div>
                  <v-btn variant="flat" color="primary" size="small" class="text-white">
                    查看詳情
                  </v-btn>
                </div>
              </v-card>
            </div>

            <!-- 無資料 -->
            <div v-if="filteredBooks.length === 0" class="text-center pa-10 text-grey">
              <v-icon icon="mdi-book-off-outline" size="64" class="mb-4"></v-icon>
              <div class="text-h6">暫無相關書籍</div>
              <v-btn
                v-if="selectedGenreId !== 'all' || search"
                variant="text"
                color="primary"
                class="mt-2"
                @click="
                  () => {
                    selectedGenreId = 'all'
                    search = ''
                  }
                "
              >
                清除篩選條件
              </v-btn>
            </div>
          </div>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import bookService from '@/modules/book/api'
import reviewService from '@/modules/review/api'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()

// --- State ---
const books = ref([])
const genres = ref([])
const reviewStats = ref([])
const loading = ref(false)
const search = ref('')
const viewMode = ref('grid')
const selectedGenreId = ref('all')
const sortBy = ref('rating_desc') // 排序依據: rating_desc, count_desc, newest

// 排序選項
const sortOptions = [
  { title: '最高評分', value: 'rating_desc' },
  { title: '最多評價', value: 'count_desc' },
  { title: '最新上架', value: 'newest' },
]

// --- 載入資料 ---
const loadData = async () => {
  loading.value = true
  try {
    // 同時呼叫書籍、分類、評價統計 API
    const [booksRes, genresRes, statsRes] = await Promise.all([
      bookService.getAllBooks(),
      bookService.getGenres(),
      reviewService.getBooksWithStats(),
    ])

    // 1. 處理基礎書籍資料 (只顯示上架中)
    const rawBooks = booksRes.data.filter((book) => book.onShelf === 1)

    // 2. 處理評價統計 (轉成 Map 以便快速查找)
    const statsMap = new Map()
    if (statsRes.data && Array.isArray(statsRes.data)) {
      statsRes.data.forEach((stat) => {
        statsMap.set(stat.bookId, stat)
      })
    }

    // 3. 合併資料
    books.value = rawBooks.map((book) => {
      const stat = statsMap.get(book.bookId)
      return {
        ...book,
        avgRating: stat ? stat.avgRating || 0 : 0,
        reviewCount: stat ? stat.reviewCount || 0 : 0,
      }
    })

    genres.value = genresRes.data

    // 檢查 URL query 是否有指定 genreId
    if (route.query.genreId) {
      const targetGenre = genresRes.data.find(
        (g) => String(g.genreId) === String(route.query.genreId),
      )
      if (targetGenre) {
        selectedGenreId.value = targetGenre.genreId
      }
    }
  } catch (error) {
    console.error('載入資料失敗:', error)
    Swal.fire('錯誤', '無法載入書籍評價資料', 'error')
  } finally {
    loading.value = false
  }
}

// --- Computed: 篩選與排序 ---
const filteredBooks = computed(() => {
  let result = [...books.value]

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

  // 3. 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'rating_desc':
        // 先比分數，分數相同比評價數
        if (b.avgRating !== a.avgRating) return b.avgRating - a.avgRating
        return b.reviewCount - a.reviewCount
      case 'count_desc':
        // 先比評價數，相同比分數
        if (b.reviewCount !== a.reviewCount) return b.reviewCount - a.reviewCount
        return b.avgRating - a.avgRating
      case 'newest':
      default:
        // 假設 bookId 越大越新
        return b.bookId - a.bookId
    }
  })

  return result
})

// --- Helpers ---
const getImageUrl = (book) => {
  if (book.bookImageBean && book.bookImageBean.imageUrl) {
    return `http://localhost:8080/upload-images/${book.bookImageBean.imageUrl}`
  }
  return ''
}

const formatPrice = (price) => {
  return price ? `$${Number(price).toLocaleString()}` : '$0'
}

const selectGenre = (genreId) => {
  selectedGenreId.value = genreId
}

// 跳轉到評價詳情 (這裡可以設定跳轉到書籍詳情頁，並帶 param 讓該頁面滾動到評價區，或是獨立評價頁)
// 目前先跳轉到一般的書籍詳情頁
const goToDetail = (bookId) => {
  router.push({ name: 'user-book-detail', params: { id: bookId } })
}

// --- Watch ---
watch(
  () => route.query.genreId,
  (newGenreId) => {
    if (newGenreId && genres.value.length > 0) {
      const targetGenre = genres.value.find((g) => String(g.genreId) === String(newGenreId))
      selectedGenreId.value = targetGenre ? targetGenre.genreId : 'all'
    } else if (!newGenreId) {
      selectedGenreId.value = 'all'
    }
  },
)

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.book-reviews-home {
  position: relative;
}

/* 左側固定導覽列 */
.category-sidebar {
  position: fixed;
  left: 20px;
  top: 100px;
  max-height: calc(100vh - 120px);
  width: 260px;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.category-sidebar > .v-card {
  display: flex;
  flex-direction: column;
  max-height: 100%;
}

.genre-list-container {
  overflow-y: auto;
  flex: 1;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
}

.gap-2 {
  gap: 8px;
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

.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 3rem; /* 確保兩行的高度一致 */
  line-height: 1.5rem;
}

/* 卡片互動效果 */
.card-hover {
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  cursor: pointer;
}
.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12) !important;
}

/* 圖片 hover 放大 */
.book-cover-img {
  transition: transform 0.3s ease;
}
.card-hover:hover .book-cover-img {
  transform: scale(1.05); /* 輕微放大 */
}

/* 評分 Badge */
.rating-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  padding: 4px 8px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  backdrop-filter: blur(4px);
  z-index: 2;
}
</style>
