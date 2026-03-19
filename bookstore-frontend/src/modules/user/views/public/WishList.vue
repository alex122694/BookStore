<template>
  <v-container class="py-10">
    <v-row class="mb-4">
      <v-col cols="12" class="d-flex align-center">
        <v-btn variant="text" prepend-icon="mdi-arrow-left" color="primary" class="font-weight-bold" @click="router.back()">
          返回
        </v-btn>
        <v-spacer></v-spacer>
      </v-col>
    </v-row>

    <div class="d-flex align-center mb-8">
      <v-icon icon="mdi-heart" color="error" size="42" class="mr-3"></v-icon>
      <h2 class="forest-main-title text-h4">我的收藏清單</h2>
      <v-chip color="error" variant="tonal" class="ml-4 font-weight-bold" size="large">
        共 {{ wishlist.length }} 筆書籍
      </v-chip>
    </div>

    <v-card v-if="wishlist.length === 0" class="pa-10 text-center rounded-xl elevation-1 border-forest-top">
      <v-icon icon="mdi-heart-outline" size="80" color="grey-lighten-2" class="mb-4"></v-icon>
      <div class="text-h5 text-grey">目前收藏清單是空的</div>
      <v-btn color="primary" size="large" class="mt-6 rounded-lg font-weight-bold" to="/dev/user/books">去尋找心儀的書</v-btn>
    </v-card>

    <v-row v-else>
      <v-col cols="12" v-for="item in wishlist" :key="item.wishlistId" class="mb-6">
        <v-hover v-slot="{ isHovering, props }">
          <v-card 
            v-bind="props"
            :elevation="isHovering ? 8 : 2" 
            class="rounded-xl favorite-row-card transition-swing overflow-hidden"
          >
            <v-row no-gutters align="center">
              <v-col cols="12" sm="3" md="2" class="pa-6 d-flex justify-center bg-grey-lighten-5">
                <v-img
                  :src="getBookImg(item.book)"
                  width="150"
                  aspect-ratio="0.7"
                  cover
                  class="rounded-lg shadow-book bg-white"
                  @click="goToDetail(item.book.bookId)"
                  style="cursor: pointer;"
                ></v-img>
              </v-col>

              <v-col cols="12" sm="6" md="7" class="pa-8">
                <div class="d-flex flex-column h-100">
                  <h3 
                    class="text-h4 font-weight-black text-forest mb-4 clickable-title"
                    @click="goToDetail(item.book.bookId)"
                  >
                    {{ item.book.bookName }}
                  </h3>
                  
                  <div class="text-body-1 mb-2 text-grey-darken-3">
                    <span class="text-grey-darken-1 font-weight-bold mr-2">作者：</span>
                    <span class="info-content">{{ item.book.author }}</span>
                  </div>
                  <div class="text-body-1 mb-2 text-grey-darken-3">
                    <span class="text-grey-darken-1 font-weight-bold mr-2">出版社：</span>
                    <span class="info-content">{{ item.book.press }}</span>
                  </div>
                  <div class="text-body-1 mb-6">
                    <span class="text-grey-darken-1 font-weight-bold mr-2">庫存狀況：</span>
                    <v-chip 
                      size="default" 
                      :color="item.book.stock > 0 ? 'success' : 'error'"
                      variant="flat"
                      class="px-4 font-weight-bold"
                    >
                      {{ item.book.stock > 0 ? `現貨供應 ${item.book.stock} 本` : '暫無庫存' }}
                    </v-chip>
                  </div>

                  <v-spacer></v-spacer>

                  <div class="text-h3 font-weight-black price-text mt-2">
                    <small class="text-h6">NT$</small> {{ item.book.price }}
                  </div>
                </div>
              </v-col>

              <v-col cols="12" sm="3" md="3" class="pa-8 d-flex flex-column justify-center border-left">
                <v-btn
                  color="secondary"
                  prepend-icon="mdi-cart-plus"
                  size="x-large"
                  class="mb-4 rounded-xl font-weight-bold text-h6"
                  block
                  :disabled="item.book.stock <= 0"
                  @click="addToCart(item.book)" 
                >
                  加入購物車
                </v-btn>
                
                <v-btn
                  variant="outlined"
                  color="grey-darken-1"
                  prepend-icon="mdi-delete-outline"
                  size="large"
                  class="rounded-xl font-weight-bold"
                  block
                  @click="removeWishItem(item.wishlistId)"
                >
                  移除收藏
                </v-btn>
              </v-col>
            </v-row>
          </v-card>
        </v-hover>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import axios from 'axios';
import { useCartStore } from '@/modules/cart/store/cartStore';

const router = useRouter();
const cartStore = useCartStore();
const wishlist = ref([]); 
const userId = localStorage.getItem('userId');

const loadWishlist = async () => {
  if (!userId) return;
  try {
    const res = await axios.get(`http://localhost:8080/api/wishlist/list/${userId}`);
    wishlist.value = res.data;
  } catch (error) {
    console.error("載入收藏失敗", error);
  }
};

const removeWishItem = (wishlistId) => {
  Swal.fire({
    title: '確定移除收藏嗎？',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '移除',
    confirmButtonColor: '#d33',
    cancelButtonText: '取消'
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.delete(`http://localhost:8080/api/wishlist/remove/${wishlistId}`);
        wishlist.value = wishlist.value.filter(f => f.wishlistId !== wishlistId);
        Swal.fire({
          icon: 'success',
          title: '已移除收藏',
          toast: true,
          position: 'top-end',
          showConfirmButton: false,
          timer: 1500
        });
      } catch (e) {
        Swal.fire('錯誤', '移除失敗', 'error');
      }
    }
  });
};

const addToCart = async (book) => {
  try {
    const params = new URLSearchParams();
    params.append('bookId', book.bookId);
    params.append('quantity', 1);

    const response = await axios.post('http://localhost:8080/cart/api/add', params, {
      withCredentials: true 
    });

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '加入成功',
        text: `《${book.bookName}》已加入購物車`,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500,
      });

      if (response.data.cartCount !== undefined) {
        cartStore.setCartCount(response.data.cartCount);
      } else {
        cartStore.fetchCartCount();
      }
    } else {
      if (response.data.message === '請先登入') {
        Swal.fire('請先登入', '登入後即可開始購物', 'warning').then(() => {
          router.push('/login');
        });
      } else {
        Swal.fire('加入失敗', response.data.message, 'error');
      }
    }
  } catch (error) {
    console.error('購物車 API 異常:', error);
    Swal.fire('錯誤', '加入購物車失敗', 'error');
  }
};

const getBookImg = (book) => {
  const fileName = book?.bookImageBean?.imageUrl;
  return fileName ? `http://localhost:8080/upload-images/${fileName}` : '';
};

const goToDetail = (id) => {
  router.push(`/dev/user/books/${id}`);
};

onMounted(loadWishlist);
</script>

<style scoped>
.forest-main-title { color: #2e5c43; font-weight: 800; }
.text-forest { color: #2e5c43; }
.border-forest-top { border-top: 8px solid #2e5c43; }
.price-text { color: #800020; }

.info-content {
  font-size: 1.15rem; 
}

.shadow-book {
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.favorite-row-card {
  border: 1px solid #e0e0e0;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.clickable-title {
  line-height: 1.2;
}

.clickable-title:hover {
  text-decoration: underline;
  cursor: pointer;
  color: #1a3a2a;
}

@media (min-width: 600px) {
  .border-left {
    border-left: 1px dashed #d1d1d1;
    min-height: 200px;
  }
}

.transition-swing {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.favorite-row-card:hover {
  transform: translateY(-4px);
}
</style>