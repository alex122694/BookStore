<template>
  <div class="cart-page-bg">
    <v-container class="py-10">
      <div class="text-center mb-8">
        <h2 class="forest-main-title">我的購物車</h2>
        <v-divider class="mx-auto mt-4" length="60" thickness="4" color="primary"></v-divider>
      </div>

      <div v-if="alertMessage" class="mb-6">
        <v-alert
          type="warning"
          variant="tonal"
          closable
          class="rounded-lg border-warning"
          @click:close="alertMessage = ''"
        >
          {{ alertMessage }}
        </v-alert>
      </div>

      <v-card class="forest-card rounded-xl overflow-hidden" elevation="2">
        <v-data-table
          :headers="headers"
          :items="cartItems"
          class="cart-table"
          hide-default-footer
          :items-per-page="-1"
          :item-class="rowClass"
        >
          <!-- 移除按鈕 (最左側) -->
          <template v-slot:item.remove="{ item }">
            <v-btn
              icon="mdi-minus-circle"
              variant="text"
              color="error"
              size="small"
              @click="removeItem(item)"
            ></v-btn>
          </template>

          <!-- 書籍名稱 -->
          <template v-slot:item.bookName="{ item }">
            <div class="d-flex align-center py-2">
              <div
                class="mr-4 rounded overflow-hidden border"
                style="width: 70px; height: 100px; flex-shrink: 0"
              >
                <v-img
                  :src="
                    item.booksBean && item.booksBean.bookImageBean
                      ? `http://localhost:8080/upload-images/${item.booksBean.bookImageBean.imageUrl}`
                      : '/no-image.png'
                  "
                  cover
                  height="100%"
                ></v-img>
              </div>
              <div>
                <div class="font-weight-bold text-h6 text-primary mb-1">
                  <span
                    class="book-link cursor-pointer"
                    @click="goToBookDetail(item.booksBean ? item.booksBean.bookId : null)"
                  >
                    {{ item.booksBean ? item.booksBean.bookName : '未知書籍' }}
                  </span>
                </div>
                <div class="text-caption text-grey">
                  {{ item.booksBean ? item.booksBean.author : '' }}
                </div>
                <!-- 顯示無庫存或下架警告 -->
                <v-chip
                  v-if="item.cartStatus === 'OFF_SHELF'"
                  color="error"
                  size="default"
                  class="mt-1 font-weight-bold"
                  variant="flat"
                >
                  本書籍暫不供應販售，請移除
                </v-chip>
                <v-chip
                  v-else-if="item.quantity === 0"
                  color="error"
                  size="default"
                  class="mt-1 font-weight-bold"
                  variant="flat"
                >
                  本書暫無庫存，請移除
                </v-chip>
              </div>
            </div>
          </template>

          <!-- 單價 -->
          <template v-slot:item.price="{ item }">
            <span class="text-body-1">${{ item.booksBean ? item.booksBean.price : 0 }}</span>
          </template>

          <!-- 數量控制 -->
          <template v-slot:item.quantity="{ item }">
            <div
              class="d-flex align-center justify-center gap-2"
              style="width: 140px; margin: 0 auto"
            >
              <!-- 商品數量扣除按鈕(數量少於1或書本未上架不能用)-->
              <v-btn
                icon="mdi-minus"
                size="x-small"
                variant="outlined"
                color="grey"
                :disabled="
                  item.quantity <= 1 || item.cartStatus === 'OFF_SHELF' || item.quantity === 0
                "
                @click="adjustQuantity(item, -1)"
              ></v-btn>

              <!-- 商品數量填寫框(庫存量為0或書本未上架不能用)-->
              <v-text-field
                v-model.number="item.quantity"
                type="number"
                variant="outlined"
                density="compact"
                hide-details
                class="centered-input"
                style="width: 60px; text-align: center"
                :disabled="item.cartStatus === 'OFF_SHELF' || item.quantity === 0"
                @change="updateQuantity(item)"
              ></v-text-field>

              <!-- 商品數量增加按鈕(數量高於庫存量或書本未上架不能用)-->
              <v-btn
                icon="mdi-plus"
                size="x-small"
                variant="outlined"
                color="primary"
                :disabled="
                  (item.booksBean && item.quantity >= item.booksBean.stock) ||
                  item.cartStatus === 'OFF_SHELF' ||
                  item.quantity === 0
                "
                @click="adjustQuantity(item, 1)"
              ></v-btn>
            </div>
          </template>

          <!-- 小計 -->
          <template v-slot:item.subtotal="{ item }">
            <span class="font-weight-bold text-primary text-h6">
              ${{ (item.booksBean ? item.booksBean.price : 0) * item.quantity }}
            </span>
          </template>

          <!-- 空購物車 -->
          <template v-slot:no-data>
            <div class="text-center py-10">
              <v-icon icon="mdi-cart-off" size="64" color="grey-lighten-2" class="mb-4"></v-icon>
              <div class="text-h6 text-grey">購物車目前是空的</div>
            </div>
          </template>
        </v-data-table>

        <!-- 底部結算區 -->
        <div v-if="cartItems.length > 0" class="pa-6 bg-grey-lighten-5 border-t">
          <v-row align="center" justify="end">
            <v-col cols="12" md="auto" class="text-right">
              <div class="text-subtitle-1 text-grey-darken-1 mb-1">總金額</div>
              <div class="text-h4 font-weight-bold text-error" style="color: #2e5a44">
                ${{ totalAmount }} <span class="text-h6 text-grey">元</span>
              </div>
            </v-col>
          </v-row>
        </div>
      </v-card>

      <!-- 按鈕區 -->
      <v-row class="mt-8 gap-4" justify="center">
        <v-col cols="12" sm="auto">
          <v-btn
            size="large"
            variant="outlined"
            color="secondary"
            class="px-8 rounded-lg font-weight-bold"
            prepend-icon="mdi-arrow-left"
            @click="router.push({ name: 'bookStore' })"
            height="50"
          >
            繼續購物
          </v-btn>
        </v-col>
        <v-col cols="12" sm="auto">
          <!-- 結帳按鈕(如果購物車沒有商品或有無效商品則無法結帳) -->
          <v-btn
            size="large"
            color="primary"
            class="px-10 rounded-lg font-weight-bold button-shadow"
            append-icon="mdi-arrow-right"
            @click="goToCheckout"
            :disabled="cartItems.length === 0 || hasInvalidItems"
            height="50"
          >
            前往結帳
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import orderService from '@/modules/order/api'
import { useCartStore } from '@/modules/cart/store/cartStore'

const router = useRouter()
const cartItems = ref([])
const totalAmount = ref(0)
const alertMessage = ref('')
const cartStore = useCartStore()

const hasInvalidItems = computed(() => {
  return cartItems.value.some((item) => item.cartStatus === 'OFF_SHELF' || item.quantity === 0)
})

const headers = [
  { title: '', key: 'remove', align: 'center', width: '50px' },
  { title: '書籍資訊', key: 'bookName', align: 'start', width: '40%' },
  { title: '單價', key: 'price', align: 'center' },
  { title: '數量', key: 'quantity', align: 'center', width: '200px' },
  { title: '小計', key: 'subtotal', align: 'center' },
]

const rowClass = (item) => {
  if (item.item.cartStatus === 'OFF_SHELF' || item.item.quantity === 0) {
    return 'disabled-row'
  }
  return ''
}

const fetchCart = async () => {
  try {
    const response = await orderService.getCartItems()

    if (response.data.success) {
      cartItems.value = response.data.cartItems || []
      totalAmount.value = response.data.totalAmount || 0
    } else {
      alertMessage.value = response.data.message || '無法讀取購物車'
      if (response.data.message === '請先登入') {
        router.push('/login')
      }
    }
  } catch (error) {
    if (error.response && error.response.status === 401) {
      Swal.fire('驗證失效', '請重新登入', 'error').then(() => {
        router.push('/login')
      })
    }
  }
}

const adjustQuantity = (item, delta) => {
  if (item.cartStatus === 'OFF_SHELF') return

  const stock = item.booksBean ? item.booksBean.stock : 9999
  const newQty = item.quantity + delta

  if (newQty < 1) return

  if (newQty > stock) {
    Swal.fire({
      icon: 'warning',
      title: '庫存不足',
      text: `目前庫存僅剩 ${stock} 本`,
      toast: true,
      position: 'center',
      showConfirmButton: false,
      timer: 1500,
    })
    return
  }

  item.quantity = newQty
  updateQuantity(item)
}

const updateQuantity = async (item) => {
  // 如果前端暫時無法取得庫存量，先假定庫存量充足(9999)，實際後端執行邏輯發現庫存不足時會將結果傳回前端並讓前端更新為正確庫存量
  const stock = item.booksBean ? item.booksBean.stock : 9999
  // 使用本地邏輯進行預檢查，但依賴後端進行實際更新
  if (item.quantity < 1) item.quantity = 1
  if (item.quantity > stock) {
    Swal.fire({
      icon: 'warning',
      title: '庫存不足',
      text: `目前庫存僅剩 ${stock} 本`,
      toast: true,
      position: 'center',
      showConfirmButton: false,
      timer: 1500,
    })
    item.quantity = stock
  }

  try {
    const response = await orderService.updateCartItem(item.cartId, item.quantity)

    if (response.data.success) {
      item.quantity = response.data.quantity
      totalAmount.value = response.data.totalAmount
    } else {
      Swal.fire('錯誤', response.data.message, 'error')
      fetchCart()
    }
  } catch (error) {
    Swal.fire('錯誤', '更新失敗', 'error')
    fetchCart()
  }
}

const removeItem = (item) => {
  Swal.fire({
    title: '確定要將本書籍移出購物車？',
    text: item.booksBean ? item.booksBean.bookName : '',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#e8e4dc',
    confirmButtonText: '移除',
    cancelButtonText: '保留',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const response = await orderService.removeCartItem(item.cartId)

        if (response.data.success) {
          Swal.fire({
            icon: 'success',
            title: '已移除',
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 1500,
          })
          await cartStore.fetchCartCount()
          fetchCart()
        } else {
          Swal.fire('錯誤', '移除失敗', 'error')
        }
      } catch (error) {
        Swal.fire('錯誤', '移除失敗', 'error')
      }
    }
  })
}

const goToBookDetail = (bookId) => {
  if (bookId) {
    router.push({ name: 'user-book-detail', params: { id: bookId } })
  }
}

const goToCheckout = () => {
  router.push({ name: 'checkout' })
}

onMounted(() => {
  fetchCart()
})
</script>

<style scoped>
.cart-page-bg {
  min-height: 100vh;
  background-color: #fcf8f0; /* 類似後台的奶油色背景 */
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2.2rem;
  font-weight: 800;
  letter-spacing: 2px;
}

.forest-card {
  border-top: 5px solid #2e5c43 !important;
}

.cart-table :deep(.v-data-table-header) {
  background-color: #f9fbe7 !important;
}

.cart-table :deep(.v-data-table-header__content) {
  font-weight: 800;
  color: #2e5c43;
  font-size: 1.1rem;
}

.centered-input :deep(input) {
  text-align: center;
}

.button-shadow {
  box-shadow: 0 4px 14px rgba(46, 92, 67, 0.3);
}

.cart-table :deep(.disabled-row) {
  background-color: #f5f5f5 !important; /* 淺灰色背景 */
  color: #9e9e9e !important;
  opacity: 1; /* 恢復不透明度以確保文字可讀但為灰色 */
}

/* 強制指定元素變灰 */
.cart-table :deep(.disabled-row .text-primary),
.cart-table :deep(.disabled-row .text-h6),
.cart-table :deep(.disabled-row .text-body-1),
.cart-table :deep(.disabled-row .text-caption),
.cart-table :deep(.disabled-row .v-chip) {
  color: #9e9e9e !important;
}

.cart-table :deep(.disabled-row .v-chip) {
  color: white !important; /* 恢復 Chip 文字顏色（如果有被影響） */
}

.book-link {
  transition: color 0.2s;
}

.book-link:hover {
  color: #2e5a44 !important; /* 主題綠色 */
  text-decoration: underline;
}
</style>
