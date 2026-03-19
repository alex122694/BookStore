<template>
  <div class="my-orders-page">
    <v-container class="py-10">
      <div class="text-center mb-8">
        <h2 class="forest-main-title">我的歷史訂單</h2>
        <v-divider class="mx-auto mt-4" length="60" thickness="4" color="primary"></v-divider>
      </div>

      <div v-if="loading" class="text-center py-10">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <div class="mt-4 text-grey">載入訂單中...</div>
      </div>

      <div v-else-if="orders.length === 0" class="text-center py-10">
        <v-icon icon="mdi-file-document-outline" size="64" color="grey-lighten-2" class="mb-4"></v-icon>
        <div class="text-h6 text-grey">目前沒有任何訂單紀錄</div>
        <v-btn color="primary" class="mt-4" to="/dev/user/store">去逛逛</v-btn>
      </div>

      <div v-else>
        <!-- 篩選與搜尋欄 -->
        <v-row class="mb-4">
            <v-col cols="12" md="6">
                <div class="d-flex gap-2">
                    <v-text-field
                        v-model="filters.startDate"
                        label="開始日期"
                        type="date"
                        variant="outlined"
                        density="compact"
                        hide-details
                        bg-color="white"
                        color="primary"
                        class="rounded-lg"
                        :max="filters.endDate || today"
                    ></v-text-field>
                    <v-text-field
                        v-model="filters.endDate"
                        label="結束日期"
                        type="date"
                        variant="outlined"
                        density="compact"
                        hide-details
                        bg-color="white"
                        color="primary"
                        class="rounded-lg"
                        :min="filters.startDate"
                        :max="today"
                    ></v-text-field>
                </div>
            </v-col>
            <v-col cols="12" md="6">
                <v-text-field
                  v-model="search"
                  prepend-inner-icon="mdi-magnify"
                  label="搜尋訂單..."
                  single-line
                  hide-details
                  variant="outlined"
                  class="rounded-lg"
                  density="compact"
                  bg-color="white"
                ></v-text-field>
            </v-col>
        </v-row>

        <v-data-iterator
          :items="filteredOrders"
          :items-per-page="itemsPerPage"
          :search="search"
        >
          <template v-slot:default="{ items }">
            <template v-for="item in items" :key="item.raw.orderId">
              <v-card 
                class="mb-6 forest-card rounded-lg cursor-pointer" 
                elevation="2"
                @click="openDetails(item.raw.orderId)"
                v-ripple
              >
                <v-card-title class="d-flex justify-space-between align-center bg-grey-lighten-4 py-3 px-4">
                  <div class="text-subtitle-1 font-weight-bold text-grey-darken-2">
                    訂單編號 #{{ item.raw.orderId }}
                  </div>
                  <v-chip 
                    :color="getStatusColor(item.raw.orderStatus)" 
                    size="small" 
                    label 
                    class="font-weight-bold cursor-pointer"
                    variant="elevated"
                    elevation="1"
                    @click.stop="openHistory(item.raw)"
                  >
                    {{ item.raw.orderStatus }}
                    <v-icon end icon="mdi-history" size="x-small"></v-icon>
                  </v-chip>
                </v-card-title>
                
                <v-card-text class="pt-4">
                  <v-row align="center">
                    <v-col cols="12" sm="6">
                      <div class="text-caption text-grey">下單日期</div>
                      <div class="text-body-1">{{ formatDate(item.raw.createdAt) }}</div>
                      
                      <div class="text-caption text-grey mt-2">配送方式</div>
                      <div class="text-body-1">{{ item.raw.deliveryMethod }}</div>
                    </v-col>
                    
                    <v-col cols="12" sm="6" class="text-right">
                      <div class="text-caption text-grey">實付金額</div>
                      <div class="text-h5 text-primary font-weight-bold">${{ item.raw.finalAmount }}</div>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
            </template>
          </template>

          <template v-slot:footer="{ page, pageCount, prevPage, nextPage }">
            <div class="d-flex align-center justify-center pa-4">
              <v-btn
                :disabled="page === 1"
                icon="mdi-arrow-left"
                density="comfortable"
                variant="text"
                rounded
                @click="prevPage"
              ></v-btn>

              <div class="mx-2 text-caption">
                第 {{ page }} 頁 / 共 {{ pageCount }} 頁
              </div>

              <v-btn
                :disabled="page === pageCount"
                icon="mdi-arrow-right"
                density="comfortable"
                variant="text"
                rounded
                @click="nextPage"
              ></v-btn>
            </div>
          </template>
        </v-data-iterator>

        <!-- 訂單狀態歷程 Dialog -->
        <v-dialog v-model="historyDialog" max-width="500px">
          <v-card class="rounded-lg">
            <v-card-title class="bg-forest-light text-forest-primary d-flex align-center px-6 py-4">
               <v-icon class="mr-2">mdi-history</v-icon>
               <span class="text-h6 font-weight-bold">訂單狀態歷程</span>
               <v-spacer></v-spacer>
               <v-btn icon="mdi-close" variant="text" size="small" @click="historyDialog = false"></v-btn>
            </v-card-title>
            
            <v-card-text class="pa-6">
                <v-timeline density="compact" align="start" side="end">
                    <v-timeline-item
                      v-for="(history, index) in selectedOrderHistory"
                      :key="index"
                      :dot-color="history.color"
                      size="x-small"
                    >
                      <div class="mb-4">
                        <div class="font-weight-bold text-subtitle-1">{{ history.title }}</div>
                        <div class="text-caption text-grey">{{ history.time }}</div>
                        <div class="text-body-2 mt-1">{{ history.description }}</div>
                      </div>
                    </v-timeline-item>
                </v-timeline>
            </v-card-text>
          </v-card>
        </v-dialog>

        <!-- 訂單明細 Dialog -->
        <v-dialog v-model="detailsDialog" max-width="1200px" scrollable>
          <v-card class="rounded-lg">
            <v-card-title class="bg-primary text-white d-flex justify-space-between align-center px-6 py-4">
              <span class="text-h6 font-weight-bold">訂單明細 #{{ selectedOrderId }}</span>
              <v-btn icon="mdi-close" variant="text" color="white" @click="detailsDialog = false"></v-btn>
            </v-card-title>
            
            <v-card-text class="pa-6 bg-grey-lighten-5" style="max-height: 80vh;">
              <div v-if="detailsLoading" class="text-center py-10">
                <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
                <div class="mt-4 text-grey-darken-1">載入中...</div>
              </div>
              
              <div v-else>
                <!-- 四大區塊 Grid -->
                <v-row class="mb-2">
                  <!-- 1. 訂單資料 -->
                  <v-col cols="12" md="6">
                    <v-card flat class="h-100 border rounded-lg">
                      <v-card-title class="text-subtitle-1 font-weight-bold d-flex align-center bg-white border-b py-3">
                        <v-icon color="primary" class="mr-2">mdi-file-document-outline</v-icon>
                        訂單資料
                      </v-card-title>
                      <v-card-text class="pt-4">
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">訂單編號</span>
                          <span class="font-weight-medium">#{{ currentOrder?.orderId }}</span>
                        </div>
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">訂購日期</span>
                          <span class="font-weight-medium">{{ formatDate(currentOrder?.createdAt) }}</span>
                        </div>
                        <div class="info-row d-flex justify-space-between align-center">
                          <span class="text-grey-darken-1">訂單狀態</span>
                          <v-chip :color="getStatusColor(currentOrder?.orderStatus)" size="small" label class="font-weight-bold">
                            {{ currentOrder?.orderStatus }}
                          </v-chip>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>

                  <!-- 2. 配送資料 -->
                  <v-col cols="12" md="6">
                    <v-card flat class="h-100 border rounded-lg">
                      <v-card-title class="text-subtitle-1 font-weight-bold d-flex align-center bg-white border-b py-3">
                        <v-icon color="primary" class="mr-2">mdi-truck-outline</v-icon>
                        配送資料
                      </v-card-title>
                      <v-card-text class="pt-4">
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">收件人</span>
                          <span class="font-weight-medium">{{ currentOrder?.recipientAt }}</span>
                        </div>
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">聯絡電話</span>
                          <span class="font-weight-medium">{{ currentOrder?.phone }}</span>
                        </div>
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1 mb-1">收件地址/門市</span>
                          <span class="font-weight-medium">{{ currentOrder?.address }}</span>
                        </div>
                         <div class="info-row d-flex justify-space-between">
                          <span class="text-grey-darken-1">配送方式</span>
                          <span class="font-weight-medium">{{ currentOrder?.deliveryMethod }}</span>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>

                  <!-- 3. 付款資料 -->
                  <v-col cols="12" md="6">
                    <v-card flat class="h-100 border rounded-lg">
                      <v-card-title class="text-subtitle-1 font-weight-bold d-flex align-center bg-white border-b py-3">
                        <v-icon color="primary" class="mr-2">mdi-credit-card-outline</v-icon>
                        付款資料
                      </v-card-title>
                      <v-card-text class="pt-4">
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">付款方式</span>
                          <span class="font-weight-medium">{{ currentOrder?.paymentMethod }}</span>
                        </div>
                        <div class="info-row d-flex justify-space-between align-center mb-2">
                          <span class="text-grey-darken-1">付款狀態</span>
                           <v-chip
                            :color="currentOrder?.paymentStatus === '已付款' ? 'success' : 'warning'"
                            size="x-small"
                            variant="outlined"
                            class="font-weight-bold"
                          >
                            {{ currentOrder?.paymentStatus }}
                          </v-chip>
                        </div>
                         <div class="info-row d-flex justify-space-between" v-if="currentOrder?.paidAt">
                          <span class="text-grey-darken-1">付款時間</span>
                          <span class="font-weight-medium">{{ formatDate(currentOrder?.paidAt) }}</span>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>

                  <!-- 4. 金額明細 -->
                  <v-col cols="12" md="6">
                    <v-card flat class="h-100 border rounded-lg">
                      <v-card-title class="text-subtitle-1 font-weight-bold d-flex align-center bg-white border-b py-3">
                        <v-icon color="primary" class="mr-2">mdi-currency-usd</v-icon>
                        金額明細
                      </v-card-title>
                      <v-card-text class="pt-4">
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">商品總額</span>
                          <!-- 若無 totalAmount 欄位，可改用計算屬性或後端補欄位，這裡暫時顯示 finalAmount 若無 totalAmount -->
                          <span class="font-weight-medium">${{ currentOrder?.totalAmount || currentOrder?.finalAmount }}</span>
                        </div>
                        <div class="info-row d-flex justify-space-between mb-2">
                          <span class="text-grey-darken-1">運費</span>
                          <span class="font-weight-medium">${{ currentOrder?.shippingFee || 0 }}</span>
                        </div>
                         <div class="info-row d-flex justify-space-between mb-2 text-error" v-if="currentOrder?.discount > 0">
                          <span class="text-grey-darken-1">優惠折扣</span>
                          <span class="font-weight-bold">-${{ currentOrder?.discount }}</span>
                        </div>
                        <v-divider class="my-3"></v-divider>
                        <div class="info-row d-flex justify-space-between align-center">
                          <span class="font-weight-bold text-h6">實付金額</span>
                          <span class="text-h5 text-primary font-weight-bold">${{ currentOrder?.finalAmount || 0 }}</span>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>
                </v-row>

                <!-- 商品明細列表 -->
                <div class="mt-6">
                  <h3 class="text-h6 text-grey-darken-3 font-weight-bold mb-3 d-flex align-center">
                    <v-icon class="mr-2">mdi-format-list-bulleted</v-icon>
                    訂單內容
                  </h3>
                  <v-card flat class="border rounded-lg overflow-hidden">
                    <v-table>
                      <thead class="bg-grey-lighten-4">
                        <tr>
                          <th class="text-left font-weight-bold text-grey-darken-2" style="width: 80px">圖片</th>
                          <th class="text-left font-weight-bold text-grey-darken-2">書籍名稱</th>
                          <th class="text-center font-weight-bold text-grey-darken-2" style="width: 100px">單價</th>
                          <th class="text-center font-weight-bold text-grey-darken-2" style="width: 80px">數量</th>
                          <th class="text-right font-weight-bold text-grey-darken-2" style="width: 120px">小計</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in currentOrderItems" :key="item.orderItemId" class="hover-row">
                          <td class="py-2">
                             <div class="rounded border overflow-hidden bg-white" style="width: 50px; height: 70px;">
                                <v-img
                                  :src="item.booksBean && item.booksBean.bookImageBean ? `http://localhost:8080/upload-images/${item.booksBean.bookImageBean.imageUrl}` : '/no-image.png'"
                                  cover
                                  height="100%"
                                ></v-img>
                             </div>
                          </td>
                          <td class="font-weight-bold text-body-2 text-grey-darken-3 py-3">
                            <span 
                              class="book-link cursor-pointer"
                              @click="goToBookDetail(item.booksBean ? item.booksBean.bookId : null)"
                            >
                              {{ item.booksBean ? item.booksBean.bookName : '未知書籍' }}
                            </span>
                            <div class="text-caption text-grey mt-1">{{ item.booksBean ? item.booksBean.author : '' }}</div>
                          </td>
                          <td class="text-center text-body-2">${{ item.price }}</td>
                          <td class="text-center text-body-2">{{ item.quantity }}</td>
                          <td class="text-right font-weight-bold text-body-1">${{ item.subtotal }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </v-card>
                </div>

                  <!-- Return Button -->
                  <div class="mt-4 text-center">
                    <v-btn
                      v-if="currentOrder && currentOrder.orderStatus !== '已取消'" 
                      color="error" 
                      variant="outlined" 
                      prepend-icon="mdi-keyboard-return"
                      @click="openReturnDialog(currentOrder)"
                    >
                      申請退貨 / 取消訂單
                    </v-btn>
                  </div>

              </div>
            </v-card-text>
          </v-card>
        </v-dialog>

        <!-- 退貨申請 Dialog -->
        <v-dialog v-model="returnDialog" max-width="500px">
            <v-card class="rounded-lg">
                <v-card-title class="bg-error text-white d-flex align-center px-6 py-4">
                    <v-icon class="mr-2">mdi-keyboard-return</v-icon>
                    <span class="text-h6 font-weight-bold">申請退貨</span>
                    <v-spacer></v-spacer>
                    <v-btn icon="mdi-close" variant="text" color="white" @click="returnDialog = false"></v-btn>
                </v-card-title>
                <v-card-text class="pa-6">
                    <v-alert type="info" variant="tonal" class="mb-4 text-body-2" density="compact">
                        請填寫退貨原因，送出後系統將自動取消訂單並進行後續處理。
                    </v-alert>
                    
                    <v-select
                        v-model="returnForm.reason"
                        :items="returnReasons"
                        label="退貨原因 *"
                        variant="outlined"
                        color="primary"
                        class="mb-2"
                    ></v-select>
                    
                    <v-textarea
                        v-model="returnForm.description"
                        label="詳細說明 (選填)"
                        variant="outlined"
                        color="primary"
                        rows="3"
                        placeholder="請補充說明退貨原因..."
                    ></v-textarea>
                </v-card-text>
                <v-card-actions class="pa-4 pt-0 justify-end">
                    <v-btn color="grey-darken-1" variant="text" @click="returnDialog = false">取消</v-btn>
                    <v-btn 
                        color="error" 
                        variant="elevated" 
                        :loading="returnLoading"
                        @click="submitReturn"
                    >
                        確認送出
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
      </div>

    </v-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'
import orderService from '@/modules/order/api'

const router = useRouter()
const orders = ref([])
const loading = ref(true)
const search = ref('')
const itemsPerPage = ref(5)
const detailsDialog = ref(false)
const selectedOrderId = ref(null)
const detailsLoading = ref(false)
const currentOrderItems = ref([])
const currentOrder = ref(null)

const filters = ref({
    startDate: '',
    endDate: ''
})

const today = new Date().toISOString().split('T')[0]

import { computed } from 'vue'

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit', 
    minute: '2-digit'
  })
}

const filteredOrders = computed(() => {
    let source = orders.value

    // 日期篩選
    if (filters.value.startDate || filters.value.endDate) {
        source = source.filter(order => {
             // 處理 order.createdAt，可能是陣列 [y,m,d,h,min] 或 ISO 字串
             let orderTime = 0
             if (Array.isArray(order.createdAt)) {
                 // 注意月份要 -1
                 orderTime = new Date(order.createdAt[0], order.createdAt[1] - 1, order.createdAt[2]).getTime()
             } else if (order.createdAt) {
                 // 嘗試解析字串
                 const d = new Date(order.createdAt)
                 orderTime = d.setHours(0,0,0,0)
             } else {
                 return true // 無日期則不過濾
             }
             
             let isValid = true
             
             // 解析 filters.startDate (YYYY-MM-DD)
             if (filters.value.startDate) {
                 const [sy, sm, sd] = filters.value.startDate.split('-').map(Number)
                 const startTime = new Date(sy, sm - 1, sd).getTime()
                 if (orderTime < startTime) isValid = false
             }
             
             // 解析 filters.endDate (YYYY-MM-DD)
             if (filters.value.endDate && isValid) {
                 const [ey, em, ed] = filters.value.endDate.split('-').map(Number)
                 const endTime = new Date(ey, em - 1, ed).getTime()
                 if (orderTime > endTime) isValid = false
             }
             
             return isValid
        })
    }
    
    return source
})
const selectedOrderHistory = ref([])
const historyDialog = ref(false)

// 退貨相關
const returnDialog = ref(false)
const returnLoading = ref(false)
const returnForm = ref({
    orderId: null,
    reason: null,
    description: ''
})
const returnReasons = [
    '商品有瑕疵',
    '收到錯誤商品',
    '商品與描述不符',
    '改變心意',
    '其他'
]

const openReturnDialog = (order) => {
    returnForm.value = {
        orderId: order.orderId,
        reason: null,
        description: ''
    }
    returnDialog.value = true
}

const submitReturn = async () => {
    if (!returnForm.value.reason) {
        Swal.fire('提示', '請選擇退貨原因', 'warning')
        return
    }

    returnLoading.value = true
    try {
        const response = await orderService.returnOrder(returnForm.value)
        if (response.data === 'success') {
            returnDialog.value = false
            detailsDialog.value = false // 關閉詳細頁
            
            // 判斷成功訊息
            const order = orders.value.find(o => o.orderId === returnForm.value.orderId)
            let msg = '退貨申請已送出，訂單已取消。'
            if (order && (order.orderStatus === '已完成' || order.deliveredAt)) {
                msg = '退貨申請已送出！請將商品包裝好，我們將派人前往取件。'
            }
            
            await Swal.fire({
                icon: 'success',
                title: '申請成功',
                text: msg
            })
            
            fetchUserOrders() // 重整列表
        } else {
             throw new Error(response.data)
        }
    } catch (error) {
        console.error('Return error:', error)
        Swal.fire('錯誤', '退貨申請失敗: ' + error.message, 'error')
    } finally {
        returnLoading.value = false
    }
}

const openHistory = (order) => {
    const history = []
    
    // 1. 訂單建立
    if (order.createdAt) {
        history.push({
            title: '訂單成立',
            time: formatDate(order.createdAt),
            color: 'primary',
            icon: 'mdi-file-document-outline',
            description: '您的訂單已成功建立。'
        })
    }
    
    // 2. 付款完成 (信用卡)
    if (order.paidAt&&order.paymentMethod=='信用卡') {
        history.push({
            title: '付款完成',
            time: formatDate(order.paidAt),
            color: 'success',
            icon: 'mdi-credit-card-check-outline',
            description: '感謝您的付款，我們正在處理您的訂單。'
        })
    }

    // 3. 處理中/出貨 
    if (order.shippedAt) {
        history.push({
            title: '商品已出貨',
            time: formatDate(order.shippedAt),
            color: 'info',
            icon: 'mdi-truck-delivery-outline',
            description: '商品已經寄出，正在運送途中。'
        })
    }
    
    // 4. 送達 
    if (order.deliveredAt) {
        history.push({
            title: '商品已送達',
            time: formatDate(order.deliveredAt),
            color: 'success',
            icon: 'mdi-package-variant-closed',
            description: '商品已送達指定地點/門市。'
        })
    }

    // 5. 收到 (貨到付款)，訂單完成
    if (order.receivedAt&&order.paymentMethod=='貨到付款') {
        history.push({
            title: '收到',
            time: formatDate(order.receivedAt),
            color: 'success',
            icon: 'mdi-package-variant-closed',
            description: '感謝您的購買，交易已完成。'
        })
    }

    // 6. 收到，訂單完成
    if (order.receivedAt && order.completedAt) {
        const time = order.receivedAt || order.completedAt
        history.push({
            title: '訂單已完成',
            time: formatDate(time),
            color: 'success',
            icon: 'mdi-check-circle-outline',
            description: '感謝您的購買，交易已完成。'
        })
    }
    
    // 7. 取消
    if (order.orderStatus === '已取消') {
        history.push({
            title: '訂單已取消',
            time: formatDate(order.updatedAt), // 用最後修改時間作為取消時間
            color: 'error',
            icon: 'mdi-close-circle-outline',
            description: '此訂單已被取消。'
        })
    }
    
    // 排序：時間越新的在越上面 (timeline 預設也是這樣比較好看，或反之)
    // 這裡我們讓時間舊的在上面 (流程由上往下)
    // 但因為我們是依序 push 的，若時間序正確應該這就是順序。
    // 為了保險，可以 sort 一下，但 array 本身就已經大致有序。
    // history.sort((a,b) => new Date(a.time) - new Date(b.time)) 
    // 因 formatDate 轉成字串了，直接用 push 順序通常是正確的邏輯順序

    selectedOrderHistory.value = history
    historyDialog.value = true
}


const getStatusColor = (status) => {
  switch (status) {
    case '待出貨': return 'warning'
    case '已出貨': return 'info'
    case '已完成': return 'success'
    case '已取消': return 'error'
    default: return 'grey'
  }
}

const goToBookDetail = (bookId) => {
  if (bookId) {
    router.push({ name: 'user-book-detail', params: { id: bookId } })
  }
}

const openDetails = async (orderId) => {
  selectedOrderId.value = orderId
  detailsDialog.value = true
  detailsLoading.value = true
  currentOrderItems.value = []
  currentOrder.value = null

  try {
    const response = await orderService.getOrderDetail(orderId)
    // 後端 OrderController 回傳的資料格式為: { order: {...}, items: [...] } 
    if (response.data) {
        currentOrder.value = response.data.order
        currentOrderItems.value = response.data.items || []
    }
  } catch (error) {
    console.error('Fetch detail error:', error)
    Swal.fire('錯誤', '無法讀取訂單明細', 'error')
  } finally {
    detailsLoading.value = false
  }
}

const fetchUserOrders = async () => {
    loading.value = true
    const token = localStorage.getItem('userToken')
    
    if (!token) {
        Swal.fire('提示', '請先登入', 'warning').then(() => {
            router.push('/login')
        })
        return
    }

    try {
        // 1. 解碼 Token 以取得用戶 ID 或獲取個人資料
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2) // 修正 Base64 解碼時可能出現的編碼問題
        }).join(''))
        const payload = JSON.parse(jsonPayload)

        if (!payload.sub) {
            throw new Error('Invalid Token')
        }

        // 2. 根據解碼出的帳號資訊，向後端獲取詳細的用戶資料（以取得真正的 userId）
        const userRes = await axios.get(`http://localhost:8080/api/data/get/${payload.sub}`, {
            withCredentials: true,
            headers: { Authorization: `Bearer ${token}` }
        })

        if (userRes.data && userRes.data.userId) {
            const userId = userRes.data.userId
            
            // 3. 取得真正的 userId 後，向後端請求該用戶的所有訂單
            const response = await orderService.getUserOrders(userId)
            orders.value = response.data || []
            
            // 將訂單依照日期降冪排序（最新的訂單排在最前面）
            orders.value.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

        } else {
            throw new Error('User Data Not Found')
        }

    } catch (error) {
        console.error('Fetch orders error:', error)
        Swal.fire('錯誤', '無法獲取訂單資料', 'error')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchUserOrders()
})

</script>

<style scoped>
.my-orders-page {
  min-height: 100vh;
  background-color: #fcf8f0;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2.2rem;
  font-weight: 800;
  letter-spacing: 2px;
}

.forest-card {
  border-left: 5px solid #2e5c43 !important;
  transition: transform 0.2s;
}

.forest-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 92, 67, 0.15) !important;
}

.book-link {
  transition: color 0.2s;
}

.book-link:hover {
  color: #2e5c43 !important; /* 主題綠色 */
  text-decoration: underline;
}

.bg-forest-light {
    background-color: #f1f8e9 !important;
}

.text-forest-primary {
    color: #2e5c43 !important;
}
</style>
