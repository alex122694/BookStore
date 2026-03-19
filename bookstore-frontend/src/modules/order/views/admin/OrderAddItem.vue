<template>
  <div class="page-wrapper px-6 py-8">
    <div class="header-section mb-6 text-left">
      <h2 class="forest-main-title">新增訂單明細</h2>
      <p class="text-subtitle-1 text-grey-darken-1 mt-2">訂單編號: #{{ orderId }}</p>
    </div>

    <v-card class="forest-card pa-6 rounded-xl">
      <div class="d-flex justify-space-between align-center mb-6">
        <h3 class="text-h5 font-weight-bold text-primary">
          <v-icon icon="mdi-book-plus" class="mr-2"></v-icon>
          商品資訊
        </h3>
        <v-btn
          color="secondary"
          variant="tonal"
          prepend-icon="mdi-arrow-left"
          @click="router.back()"
        >
          取消返回
        </v-btn>
      </div>

      <v-form @submit.prevent="handleSubmit">
        <div class="items-container">
          <transition-group name="list" tag="div">
            <div v-for="(item, index) in items" :key="index" class="mb-6 pb-6 border-b">
              <div class="d-flex justify-space-between align-center mb-4">
                <div class="d-flex align-center">
                   <v-avatar color="primary" size="28" class="mr-3">
                      <span class="text-white text-caption font-weight-bold">{{ index + 1 }}</span>
                   </v-avatar>
                   <span class="text-h6 text-grey-darken-2 font-weight-bold">商品項目</span>
                </div>
                <v-btn
                  v-if="items.length > 1"
                  color="error"
                  variant="text"
                  size="small"
                  prepend-icon="mdi-delete"
                  @click="removeItem(index)"
                >
                  移除
                </v-btn>
              </div>

              <v-row>
                <v-col cols="12" md="3">
                  <v-text-field
                    v-model="item.bookId"
                    label="書籍 ID"
                    placeholder="輸入 ID 自動帶入"
                    variant="outlined"
                    density="comfortable"
                    color="primary"
                    class="forest-input"
                    @change="fetchBookInfo(item)"
                    prepend-inner-icon="mdi-identifier"
                    hide-details="auto"
                  ></v-text-field>
                </v-col>
                
                <v-col cols="12" md="5">
                  <v-text-field
                    v-model="item.bookName"
                    label="書籍名稱"
                    variant="outlined"
                    density="comfortable"
                    color="primary"
                    class="forest-input readonly-field"
                    readonly
                    bg-color="grey-lighten-4"
                    prepend-inner-icon="mdi-book-open-page-variant"
                    hide-details="auto"
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="2">
                  <v-text-field
                    v-model="item.price"
                    label="單價"
                    prefix="$"
                    variant="outlined"
                    density="comfortable"
                    color="primary"
                    class="forest-input readonly-field"
                    readonly
                    bg-color="grey-lighten-4"
                    hide-details="auto"
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="2">
                  <v-text-field
                    v-model.number="item.quantity"
                    label="數量"
                    type="number"
                    min="1"
                    variant="outlined"
                    density="comfortable"
                    color="primary"
                    class="forest-input"
                    prepend-inner-icon="mdi-counter"
                    hide-details="auto"
                  ></v-text-field>
                </v-col>
              </v-row>
            </div>
          </transition-group>
        </div>

        <div class="d-flex align-center mt-4 mb-8">
           <v-btn
             color="primary"
             variant="tonal"
             prepend-icon="mdi-plus"
             class="mr-4 dashed-btn"
             @click="addItem"
             block
           >
             增加一筆商品
           </v-btn>
        </div>

        <v-divider class="mb-6"></v-divider>

        <div class="d-flex justify-end gap-3">
          <v-btn
            size="large"
            color="grey-lighten-1"
            variant="outlined"
            class="mr-4"
            @click="router.back()"
            width="120"
          >
            取消
          </v-btn>
          
          <v-btn
            type="submit"
            size="large"
            color="primary"
            variant="elevated"
            prepend-icon="mdi-content-save-plus"
            width="150"
            class="font-weight-bold"
          >
            確認新增
          </v-btn>
        </div>
      </v-form>
    </v-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Swal from 'sweetalert2'
import orderService from '@/modules/order/api'

const router = useRouter()
const route = useRoute()
const orderId = ref(route.params.id)

const items = ref([
    { bookId: '', bookName: '', price: '', quantity: 1 }
])

const addItem = () => {
    items.value.push({ bookId: '', bookName: '', price: '', quantity: 1 })
}

const removeItem = (index) => {
    items.value.splice(index, 1)
}

const fetchBookInfo = async (item) => {
    if (!item.bookId) return
    
    try {
        const response = await orderService.getBookInfo(item.bookId)
        const book = response.data
        if (book && book.bookId) {
            if (book.onShelf !== 1) { // 檢查是否上架 (0:下架, 1:上架, 2:封存)
                Swal.fire('警告', '本書目前未上架，無法新增', 'warning')
                item.bookName = ''
                item.price = ''
                item.bookId = ''
                return
            }
            item.bookName = book.bookName
            item.price = book.price
            item.onShelf = book.onShelf
        } else {
            Swal.fire('錯誤', '查無此書籍', 'error')
            item.bookName = ''
            item.price = ''
            item.bookId = ''
        }
    } catch (error) {
        console.error(error)
        Swal.fire('錯誤', '查無此書籍', 'error')
        item.bookName = ''
        item.price = ''
        item.bookId = ''
    }
}

const handleSubmit = async () => {
    const invalidItems = items.value.filter(item => item.bookId && item.onShelf !== 1 && item.onShelf !== undefined)
    if (invalidItems.length > 0) {
         Swal.fire('錯誤', '包含未上架書籍，請檢查後重試', 'error')
         return
    }
    
    // 檢查是否有空的基本資料
    if (items.value.some(i => !i.bookId || !i.bookName)) {
         Swal.fire('提示', '請填寫完整書籍資訊', 'warning')
         return
    }

    try {
        const response = await orderService.addOrderItems(orderId.value, items.value)
        
        if (response.data === 'success') {
             Swal.fire({
                icon: 'success',
                title: '新增成功',
                timer: 1500
            }).then(() => {
                router.push({ name: 'orderDetail-admin', params: { id: orderId.value } })
            })
        } else {
            Swal.fire('錯誤', '新增失敗: ' + response.data, 'error')
        }
    } catch (error) {
        Swal.fire('錯誤', '系統錯誤', 'error')
    }
}
</script>

<style scoped lang="scss">
.page-wrapper {
  min-height: 100vh;
  background-color: #fcf8f0;
}

.forest-main-title {
  color: #2e5c43; // 此為 forest-primary
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0;
}

.forest-card {
   background-color: white !important;
   border: 1px solid #e0e0e0;
   box-shadow: 0 4px 20px rgba(0,0,0,0.05) !important;
}

.forest-input {
  :deep(.v-field__outline) {
    color: #bdbdbd !important; // 預設邊框淺灰
  }
  
  :deep(.v-field--focused .v-field__outline) {
    color: #2e5c43 !important; // 聚焦時森林綠
    opacity: 1;
  }
  
  // 針對唯讀欄位的樣式調整
  &.readonly-field {
      :deep(.v-field__input) {
          color: #666;
          cursor: default;
      }
  }
}

.dashed-btn {
    border: 2px dashed #a5d6a7;
    background-color: rgba(232, 245, 233, 0.5) !important;
    
    &:hover {
        background-color: rgba(232, 245, 233, 1) !important;
        border-color: #2e5c43;
    }
}

// 列表過渡動畫
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.border-b {
    border-bottom: 1px dashed #e0e0e0;
    
    &:last-child {
        border-bottom: none;
    }
}
</style>
