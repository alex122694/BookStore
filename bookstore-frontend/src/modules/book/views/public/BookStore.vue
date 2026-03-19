<template>
  <div class="center-body">
    <div class="shop-container">
      <h2>書籍商城</h2>

      <div class="header-actions">
        <button class="cart-btn" @click="router.push({ name: 'cart' })">前往購物車</button>
      </div>

      <div class="books-grid">
        <div v-for="book in availableBooks" :key="book.bookId" class="book-card">
          <div class="book-image">
            <img
              :src="
                book.bookImage
                  ? 'http://localhost:8080/images/' + book.bookImage
                  : 'https://via.placeholder.com/150'
              "
              alt="Book Cover"
            />
          </div>
          <div class="book-info">
            <h3 class="book-title">{{ book.bookName }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <p class="book-price">NT$ {{ book.price }}</p>
            <p class="book-stock" v-if="book.stock > 0">庫存: {{ book.stock }}</p>
            <p class="book-out-stock" v-else>缺貨中</p>

            <div class="actions">
              <input
                type="number"
                v-model.number="book.cartQty"
                min="1"
                :max="book.stock"
                class="qty-input"
                v-if="book.stock > 0"
              />
              <button class="add-btn" @click="addToCart(book)" :disabled="book.stock <= 0">
                加入購物車
              </button>
              <button class="add-btn" @click="addAndGoToCart(book)" :disabled="book.stock <= 0">
                直接購買
              </button>
            </div>
          </div>
        </div>

        <div v-if="availableBooks.length === 0" class="no-data">目前沒有上架書籍</div>
      </div>

      <div class="footer-actions">
        <button class="back-btn" @click="router.push('/home')">回後台首頁</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'

const router = useRouter()
const books = ref([])

import orderService from '@/modules/order/api'

// --- Removed getAuthHeaders as it is now inside orderService ---

const fetchBooks = async () => {
  try {
    const response = await orderService.getAllBooks()
    books.value = response.data.map((b) => ({ ...b, cartQty: 1 }))
  } catch (error) {
    console.error('Fetch books failed', error)
  }
}

const availableBooks = computed(() => {
  return books.value.filter((b) => b.onShelf === 1)
})

const addToCart = async (book) => {
  try {
    const response = await orderService.addToCart(book.bookId, book.cartQty)

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '加入成功',
        text: `${book.bookName} 已加入購物車`,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500,
      })
    } else {
      if (response.data.message === '請先登入') {
        router.push('/login')
      }
      Swal.fire('加入失敗', response.data.message, 'error')
    }
  } catch (error) {
    if (error.response && error.response.status === 401) {
      Swal.fire('驗證失效', '請重新登入', 'error').then(() => {
        router.push('/login')
      })
    } else {
      Swal.fire('錯誤', '加入購物車失敗', 'error')
    }
  }
}

const addAndGoToCart = async (book) => {
  try {
    const response = await orderService.addToCart(book.bookId, book.cartQty)

    if (response.data.success) {
      router.push({ name: 'cart' })
    } else {
      if (response.data.message === '請先登入') {
        router.push('/login')
      }
      Swal.fire('加入失敗', response.data.message, 'error')
    }
  } catch (error) {
    if (error.response && error.response.status === 401) {
      Swal.fire('驗證失效', '請重新登入', 'error').then(() => {
        router.push('/login')
      })
    } else {
      Swal.fire('錯誤', '加入購物車失敗', 'error')
    }
  }
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.center-body {
  font-family: '微軟正黑體', 'Arial', sans-serif;
  background-color: #fcf8f0;
  display: flex;
  justify-content: center;
  padding: 40px 0;
  min-height: 100vh;
}

.shop-container {
  width: 95%;
  max-width: 1200px;
  padding: 30px;
}

h2 {
  color: #7b5e47;
  text-align: center;
  border-bottom: 1px solid #e0d9c9;
  padding-bottom: 20px;
  margin-bottom: 30px;
}

.header-actions {
  text-align: right;
  margin-bottom: 20px;
}

.cart-btn {
  background-color: #a07d58;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 30px;
}

.book-card {
  background: white;
  border: 1px solid #dcd5c7;
  border-radius: 8px;
  overflow: hidden;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.book-image {
  height: 300px;
  background-color: #f9f9f9;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.book-image img {
  max-height: 100%;
  max-width: 100%;
}

.book-info {
  padding: 15px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 18px;
  margin: 0 0 10px 0;
  color: #4a4a4a;
  height: 50px; /* Fixed height for title alignment */
  overflow: hidden;
}

.book-author {
  color: #666;
  margin: 0 0 10px 0;
  font-size: 14px;
}

.book-price {
  font-size: 20px;
  font-weight: bold;
  color: #a07d58;
  margin: 10px 0;
}

.book-stock {
  font-size: 13px;
  color: #2d5a27;
}

.book-out-stock {
  color: #d33;
  font-weight: bold;
}

.actions {
  margin-top: auto;
  display: flex;
  gap: 10px;
  align-items: center;
}

.qty-input {
  width: 60px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.add-btn {
  flex-grow: 1;
  background-color: #9fb89e;
  color: #333;
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.add-btn:hover:not(:disabled) {
  background-color: #8da68c;
}

.add-btn:disabled {
  background-color: #eee;
  color: #aaa;
  cursor: not-allowed;
}

.no-data {
  grid-column: 1 / -1;
  text-align: center;
  color: #999;
  padding: 50px;
}

.footer-actions {
  margin-top: 40px;
  text-align: center;
}

.back-btn {
  background-color: #e8e4dc;
  color: #666;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
