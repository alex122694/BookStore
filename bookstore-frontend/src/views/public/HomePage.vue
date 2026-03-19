<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import bookService from '@/modules/book/api'
import orderService from '@/modules/order/api'
import BookCard from '@/modules/book/views/public/BookCard.vue'

const router = useRouter()
const featuredBooks = ref([])
const categories = ref([])

// 圖示與顏色庫
const iconPool = [
  { icon: 'mdi-book-open-page-variant', color: '#C8E6C9' },
  { icon: 'mdi-chart-line', color: '#F1F8E9' },
  { icon: 'mdi-account-group', color: '#DCEDC8' },
  { icon: 'mdi-palette', color: '#E8F5E9' },
  { icon: 'mdi-school', color: '#FFF9C4' },
  { icon: 'mdi-briefcase', color: '#D1C4E9' },
  { icon: 'mdi-earth', color: '#B3E5FC' },
  { icon: 'mdi-flask', color: '#FFCCBC' },
  { icon: 'mdi-music', color: '#F8BBD0' },
  { icon: 'mdi-camera', color: '#CFD8DC' },
]

const goToBooks = () => {
  router.push({ name: 'user-books' })
}

const goToCategory = (genreId) => {
  router.push({ name: 'user-books', query: { genreId: genreId } })
}

const getFeaturedBooks = async () => {
  try {
    const response = await bookService.getAllBooks()
    let allBooks = response.data

    // 1. 只選擇上架中的書籍
    allBooks = allBooks.filter((book) => book.onShelf === 1)

    // 2. 隨機打亂陣列 (Fisher-Yates Shuffle)
    for (let i = allBooks.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[allBooks[i], allBooks[j]] = [allBooks[j], allBooks[i]]
    }

    // 3. 取前 4 筆
    featuredBooks.value = allBooks.slice(0, 4)
  } catch (error) {
    console.error('Fetching books failed:', error)
  }
}

const topSellers = ref([])
const getTopSellers = async () => {
  try {
    const res = await orderService.getHomepageTopSellers()
    topSellers.value = res.data.map((item) => {
      return {
        ...item,
        imagePath: item.coverImage, 
        shortDesc: item.description || item.bookDescription || item.shortDesc || '',
        genres: item.genres || [] 
      }
    })
  } catch (e) {
    console.error('Fetch top sellers failed', e)
  }
}

const getRandomCategories = async () => {
  try {
    const response = await bookService.getGenres()
    let allGenres = response.data

    // 隨機打亂
    for (let i = allGenres.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[allGenres[i], allGenres[j]] = [allGenres[j], allGenres[i]]
    }

    // 取前 4 個，並分配圖示與顏色
    categories.value = allGenres.slice(0, 4).map((genre, index) => {
      const style = iconPool[index % iconPool.length] // 循環使用樣式
      return {
        title: genre.genreName,
        genreId: genre.genreId,
        icon: style.icon,
        color: style.color,
      }
    })
  } catch (error) {
    console.error('Fetching genres failed:', error)
  }
}

onMounted(() => {
  getFeaturedBooks()
  getRandomCategories()
  getTopSellers()
})
</script>
<template>
  <div class="home-wrapper">
    <v-parallax src="https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&q=80&w=2070" height="600">
      <div class="d-flex flex-column fill-height justify-center align-center text-white hero-overlay">
        <h1 class="text-h2 font-weight-black mb-4 hero-title">森林書屋</h1>
        <h4 class="text-h5 font-weight-light mb-8 hero-subtitle">在喧囂的城市中，為您找尋一隅靜謐的書香。</h4>
        <v-btn color="primary" size="x-large" class="rounded-pill px-10 login-btn-cute shadow-lg" elevation="4" @click="goToBooks">
          <v-icon start icon="mdi-compass-outline"></v-icon>
          與書相遇
        </v-btn>
      </div>
    </v-parallax>


    <v-container class="my-12">
      <div class="d-flex justify-space-between align-center mb-6">
        <h2 class="text-h4 font-weight-bold" style="color: #2e5c43">本月熱銷排行</h2>
        <v-btn variant="text" color="primary" @click="goToBooks"
          >查看全部 <v-icon icon="mdi-chevron-right"></v-icon
        ></v-btn>
      </div>
      <v-row>
        <v-col v-for="(book, index) in topSellers" :key="book.bookId" cols="12" sm="6" md="3">
          <div class="h-100">
            <BookCard :book="book">
              <template #extra-info>
                <div class="text-center text-red-darken-4 font-weight-bold my-2 text-body-1 bg-red-lighten-5 py-1">
                   No.{{ index + 1 }} &nbsp;|&nbsp; 熱銷 {{ book.totalQuantity }} 本
                </div>
              </template>
            </BookCard>
          </div>
        </v-col>
      </v-row>
    </v-container>

    <v-container class="my-12">
      <div class="d-flex justify-space-between align-center mb-6">
        <h2 class="text-h4 font-weight-bold" style="color: #2e5c43">本月精選</h2>
        <v-btn variant="text" color="primary" @click="goToBooks"
          >查看更多 <v-icon icon="mdi-chevron-right"></v-icon
        ></v-btn>
      </div>

      <v-row>
        <v-col v-for="book in featuredBooks" :key="book.bookId" cols="12" sm="6" md="3">
          <BookCard :book="book" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<style scoped>
.hero-overlay {
  background: rgba(0, 0, 0, 0.4);
}

.category-card {
  transition: all 0.3s ease;
  cursor: pointer;
}

.book-card {
  transition: transform 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
}
</style>