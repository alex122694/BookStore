<template>
  <div class="page-wrapper">
    <div class="header-section mb-6 text-left">
      <h2 class="forest-main-title">訂單明細</h2>
    </div>

    <div class="d-flex justify-space-between align-center mb-6">
       <v-btn
        color="secondary"
        variant="tonal"
        prepend-icon="mdi-arrow-left"
        class="font-weight-bold"
        @click="router.push({ name: 'orderList' })"
      >
        返回列表
      </v-btn>
    </div>

    <div v-if="loading" class="d-flex justify-center my-10">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <v-card v-else class="forest-card pa-6 rounded-xl">
      <!-- 訂單基本資訊 -->
      <v-row>
        <v-col cols="12" md="6">
          <h3 class="text-h5 font-weight-bold text-primary mb-4">
            <v-icon icon="mdi-information-outline" class="mr-2"></v-icon>
            基本資訊
          </h3>
          <v-list density="compact" class="info-list">
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-identifier" size="small" color="primary" class="mr-2"></v-icon>
                <span class="label">訂單編號：</span>
              </template>
              <span class="value">#{{ order.orderId }}</span>
            </v-list-item>
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-calendar-clock" size="small" color="primary" class="mr-2"></v-icon>
                <span class="label">訂購日期：</span>
              </template>
              <span class="value">{{ formatDate(order.createdAt) }}</span>
            </v-list-item>
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-list-status" size="small" color="primary" class="mr-2"></v-icon>
                <span class="label">目前狀態：</span>
              </template>
               <v-select
                v-model="order.orderStatus"
                :items="orderStatusOptions"
                variant="outlined"
                density="compact"
                hide-details
                color="primary"
                class="mt-1 forest-input"
                style="max-width: 200px;"
                :readonly="!isEditable"
              ></v-select>
            </v-list-item>
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-credit-card-settings-outline" size="small" color="primary" class="mr-2"></v-icon>
                <span class="label">付款方式：</span>
              </template>
               <v-select
                v-model="order.paymentMethod"
                :items="paymentMethodOptions"
                variant="outlined"
                density="compact"
                hide-details
                color="primary"
                class="mt-1 forest-input"
                style="max-width: 200px;"
                :readonly="!isEditable"
              ></v-select>
            </v-list-item>
             <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-truck-delivery" size="small" color="primary" class="mr-2"></v-icon>
                <span class="label">配送方式：</span>
              </template>
              <v-select
                v-model="order.deliveryMethod"
                :items="deliveryMethodOptions"
                variant="outlined"
                density="compact"
                hide-details
                color="primary"
                class="mt-1 forest-input"
                style="max-width: 200px;"
                @update:model-value="recalculateTotal"
                :readonly="!isEditable"
              ></v-select>
            </v-list-item>
          </v-list>
        </v-col>

        <v-col cols="12" md="6">
          <h3 class="text-h5 font-weight-bold text-primary mb-4">
            <v-icon icon="mdi-account-outline" class="mr-2"></v-icon>
            收件資訊
          </h3>
           <v-list density="compact" class="info-list">
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-account-circle" size="small" color="primary" class="mr-2"></v-icon>
                <span class="label">訂購人：</span>
              </template>
              <div class="d-flex align-center">
                 <v-avatar size="24" color="secondary" class="mr-2">
                   <span class="text-white text-caption">{{ (order.userBean?.userName || 'U').charAt(0) }}</span>
                 </v-avatar>
                <span class="value">{{ order.userBean ? order.userBean.userName : 'Unknown' }}</span>
              </div>
            </v-list-item>
            <v-list-item class="mb-2">
              <template v-slot:prepend>
                <v-icon icon="mdi-account-box" size="small" color="primary" class="mr-2 pt-2"></v-icon>
                <span class="label pt-2">收件人：</span>
              </template>
               <v-text-field
                v-model="order.recipientAt"
                variant="outlined"
                density="compact"
                hide-details
                color="primary"
                class="forest-input"
                :readonly="!isEditable"
              ></v-text-field>
            </v-list-item>
            <v-list-item class="mb-2">
              <template v-slot:prepend>
                <v-icon icon="mdi-phone" size="small" color="primary" class="mr-2 pt-2"></v-icon>
                <span class="label pt-2">聯絡電話：</span>
              </template>
               <v-text-field
                v-model="order.phone"
                variant="outlined"
                density="compact"
                hide-details
                color="primary"
                class="forest-input"
                :readonly="!isEditable"
              ></v-text-field>
            </v-list-item>
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-map-marker" size="small" color="primary" class="mr-2 pt-2"></v-icon>
                <span class="label pt-2">收件地址：</span>
              </template>
               <v-text-field
                v-model="order.address"
                variant="outlined"
                density="compact"
                hide-details
                color="primary"
                class="forest-input"
                :readonly="!isEditable"
              ></v-text-field>
            </v-list-item>
          </v-list>
        </v-col>
      </v-row>

      <v-divider class="my-6 border-opacity-50" color="primary"></v-divider>
      
      <!-- 詳細時間與狀態 -->
      <h3 class="text-h5 font-weight-bold text-primary mb-4">
        <v-icon icon="mdi-clock-outline" class="mr-2"></v-icon>
        詳細時間與狀態
      </h3>
      <v-row>
        <v-col cols="12" md="6">
            <v-list density="compact" class="info-list">
                <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-cash-register" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">付款狀態：</span>
                    </template>
                     <v-select
                        v-model="order.paymentStatus"
                        :items="paymentStatusOptions"
                        variant="outlined"
                        density="compact"
                        hide-details
                        color="primary"
                        class="mt-1 forest-input"
                        style="max-width: 200px;"
                        :readonly="!isEditable"
                    ></v-select>
                </v-list-item>
                <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-clock-check" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">付款時間：</span>
                    </template>
                    <span class="value">{{ formatDate(order.paidAt) || '-' }}</span>
                </v-list-item>
                <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-update" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">更新時間：</span>
                    </template>
                    <span class="value">{{ formatDate(order.updatedAt) || '-' }}</span>
                </v-list-item>
                 <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-ticket-confirmation" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">優惠券ID：</span>
                    </template>
                    <span class="value">{{ order.couponId || '未使用' }}</span>
                </v-list-item>
            </v-list>
        </v-col>
        <v-col cols="12" md="6">
            <v-list density="compact" class="info-list">
                 <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-truck-fast" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">出貨時間：</span>
                    </template>
                    <span class="value">{{ formatDate(order.shippedAt) || '-' }}</span>
                </v-list-item>
                 <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-home-map-marker" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">送達時間：</span>
                    </template>
                    <span class="value">{{ formatDate(order.deliveredAt) || '-' }}</span>
                </v-list-item>
                 <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-pen" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">簽收時間：</span>
                    </template>
                    <span class="value">{{ formatDate(order.receivedAt) || '-' }}</span>
                </v-list-item>
                 <v-list-item>
                    <template v-slot:prepend>
                        <v-icon icon="mdi-check-decagram" size="small" color="primary" class="mr-2"></v-icon>
                        <span class="label">完成時間：</span>
                    </template>
                    <span class="value">{{ formatDate(order.completedAt) || '-' }}</span>
                </v-list-item>
            </v-list>
        </v-col>
      </v-row>

      <v-divider class="my-6 border-opacity-50" color="primary"></v-divider>

      <!-- 訂單品項 -->
      <div class="d-flex justify-space-between align-center mb-4">
        <h3 class="text-h5 font-weight-bold text-primary mb-0">
          <v-icon icon="mdi-book-open-variant" class="mr-2"></v-icon>
          訂購商品
        </h3>
        <v-btn
           v-if="isEditable"
           color="primary"
           prepend-icon="mdi-plus"
           variant="elevated"
           size="small"
           @click="goToAddItem"
        >
           新增書籍
        </v-btn>
      </div>
      
      <v-table class="forest-table mb-6">
        <thead>
          <tr>
            <th class="text-left font-weight-bold text-primary" width="80"></th>
            <th class="text-left font-weight-bold text-primary">書籍名稱</th>
            <th class="text-center font-weight-bold text-primary">單價</th>
            <th class="text-center font-weight-bold text-primary">數量</th>
            <th class="text-right font-weight-bold text-primary">小計</th>
            <th class="text-center font-weight-bold text-primary">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in orderItems" :key="item.itemId">
            <td>
                <v-img
                  :src="item.booksBean && item.booksBean.bookImageBean ? `http://localhost:8080/upload-images/${item.booksBean.bookImageBean.imageUrl}` : '/no-image.png'"
                  width="50"
                  height="70"
                  cover
                  class="rounded"
                ></v-img>
            </td>
            <td>{{ item.booksBean ? item.booksBean.bookName : '未知書籍' }}</td>
            <td class="text-center">${{ item.price }}</td>
            <td class="text-center" style="width: 120px;">
                <v-text-field
                    v-model.number="item.quantity"
                    type="number"
                    variant="outlined"
                    density="compact"
                    hide-details
                    color="primary"
                    class="forest-input"
                    min="1"
                    @update:model-value="recalculateTotal"
                    :readonly="!isEditable"
                ></v-text-field>
            </td>
            <td class="text-right font-weight-bold">${{ item.price * item.quantity }}</td>
            <td class="text-center">

               <v-btn
                 icon="mdi-delete"
                 size="x-small"
                 color="error"
                 variant="text"
                 @click="deleteItem(item)"
                 :disabled="!isEditable"
               ></v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>

      <!-- 總計區域與退貨資訊 -->
      <v-row>
        <!-- 退貨資訊顯示區 -->
        <v-col cols="12" md="8">
            <v-alert
              v-if="returnInfo"
              type="error"
              variant="tonal"
              border="start"
              class="mb-4"
              prominent
            >
              <div class="text-subtitle-1 font-weight-bold mb-1">
                <v-icon start>mdi-keyboard-return</v-icon>
                使用者已申請退貨
              </div>
              <v-divider class="my-2 border-opacity-25"></v-divider>
              <div class="d-flex flex-column gap-1">
                 <div><strong>退貨原因：</strong> {{ returnInfo.reason }}</div>
                 <div v-if="returnInfo.description"><strong>詳細說明：</strong> {{ returnInfo.description }}</div>
                 <div class="text-caption text-grey-darken-1 mt-1">申請時間：{{ returnInfo.createdAt }}</div>
              </div>
            </v-alert>
        </v-col>

        <!-- 金額總計 -->
        <v-col cols="12" md="4">
          <v-card variant="tonal" color="primary" class="pa-4 rounded-lg">
            <div class="d-flex justify-space-between mb-2">
               <span class="text-grey-darken-2">商品小計</span>
               <span class="font-weight-bold">${{ order.totalAmount }}</span>
            </div>
            <div class="d-flex justify-space-between mb-2">
               <span class="text-grey-darken-2">運費</span>
               <span class="font-weight-bold">${{ order.shippingFee }}</span>
            </div>
             <div class="d-flex justify-space-between mb-2">
                <span class="text-grey-darken-2">折扣</span>
                <span class="font-weight-bold text-error">-${{ order.discount || 0 }}</span>
             </div>
            <v-divider class="my-2"></v-divider>
            <div class="d-flex justify-space-between text-h6 text-primary font-weight-bold">
               <span>總金額</span>
               <span>${{ order.finalAmount }}</span>
            </div>
          </v-card>
        </v-col>
      </v-row>
      
      <!-- 底部按鈕 -->
      <div class="d-flex justify-end gap-3 mt-6">
         <v-btn
           color="success"
           prepend-icon="mdi-content-save"
           variant="elevated"
           class="font-weight-bold mr-2"
           @click="handleSave"
           :loading="saving"
           v-if="isEditable"
         >
           保存修改
         </v-btn>
         <v-btn
           v-if="isEditable"
           color="error"
           variant="outlined"
           prepend-icon="mdi-close"
           @click="handleCancel"
         >
           取消訂單
         </v-btn>
      </div>

    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import orderService from '@/modules/order/api'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

const order = ref({})
const orderItems = ref([])
const returnInfo = ref(null) // 新增 returnInfo ref
const loading = ref(true)
const saving = ref(false)

const isEditable = computed(() => {
    const status = order.value.orderStatus
    return status !== '已取消' && status !== '已退款'
})

const orderStatusOptions = ['待處理', '待付款', '待出貨', '已出貨', '已送達', '已收貨', '已完成', '已取消', '已退款']
const paymentStatusOptions = ['未付款', '已付款']
const paymentMethodOptions = ['信用卡', '貨到付款']
const deliveryMethodOptions = ['宅配到府', '超商取貨']



const formatDate = (dateArr) => {
  if (!dateArr) return ''
  if (Array.isArray(dateArr)) {
    const [y, m, d, h, min] = dateArr
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
  }
  return dateArr
}

const getStatusColor = (status) => {
    if (status === '已付款' || status === '完成') return 'success'
    if (status === '待付款' || status === '處理中') return 'warning'
    if (status === '已取消') return 'error'
    return 'grey'
}

const fetchOrderDetail = async () => {
  try {
    const response = await orderService.getOrderDetail(orderId)
    if (response.data) {
        order.value = response.data.order || {}
        orderItems.value = response.data.items || []
        returnInfo.value = response.data.returnInfo || null // 獲取退貨資訊
    }
  } catch (error) {
    console.error('Fetch detail failed', error)
    Swal.fire('錯誤', '無法讀取訂單明細', 'error')
    router.push({ name: 'orderList' })
  } finally {
    loading.value = false
  }
}

// --- 明細管理邏輯 ---

const goToAddItem = () => {
    router.push({ name: 'orderAddItem', params: { id: orderId } })
}



const deleteItem = async (item) => {
    Swal.fire({
        title: '確定刪除此明細？',
        text: item.booksBean?.bookName,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: '刪除'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await orderService.deleteOrderItem(orderId, item.orderItemId)
                 Swal.fire('成功', '已刪除', 'success')
                 fetchOrderDetail()
            } catch (e) {
                 Swal.fire('錯誤', '刪除失敗', 'error')
            }
        }
    })
}



// 重新計算總金額
const recalculateTotal = () => {
    let total = 0
    orderItems.value.forEach(item => {
        total += item.price * (item.quantity || 0)
    })
    order.value.totalAmount = total
    
    // 計算運費
    let shipping = 0
    const delivery = order.value.deliveryMethod
    if (delivery === '宅配到府' || delivery === 'HOME') {
        if (total < 1000) shipping = 50
    } else if (delivery === '超商取貨' || delivery === 'STORE') {
         if (total < 350) shipping = 50
    }
    order.value.shippingFee = shipping
    
    // 計算總金額
    let final = total + shipping - (order.value.discount || 0)
    if (final < 0) final = 0
    order.value.finalAmount = final
}

// 保存修改
const handleSave = async () => {
    saving.value = true
    try {
        const updateData = {
            orderId: orderId,
            recipientAt: order.value.recipientAt || order.value.recipientName,
            phone: order.value.phone,
            address: order.value.address,
            paymentMethod: order.value.paymentMethod,
            deliveryMethod: order.value.deliveryMethod,
            paymentStatus: order.value.paymentStatus,
            orderStatus: order.value.orderStatus,
            items: orderItems.value.map(item => ({
                orderItemId: item.orderItemId,
                quantity: item.quantity
            }))
        }
        
        await orderService.updateFullOrder(updateData)
        Swal.fire('成功', '訂單已保存', 'success')
        fetchOrderDetail()
    } catch (error) {
        console.error(error)
        Swal.fire('錯誤', '保存失敗: ' + (error.response?.data?.message || error.message), 'error')
    } finally {
        saving.value = false
    }
}

// 取消訂單
const handleCancel = () => {
    Swal.fire({
        title: '確定取消此訂單？',
        text: "取消後將無法復原，且庫存將會回補。",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#2e5c43',
        confirmButtonText: '確定取消',
        cancelButtonText: '暫不取消'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await orderService.cancelOrder(orderId)
                Swal.fire(
                    '已取消',
                    '訂單已成功取消',
                    'success'
                )
                fetchOrderDetail()
            } catch (error) {
                console.error(error)
                Swal.fire(
                    '錯誤',
                    '取消失敗: ' + (error.response?.data?.message || '未知錯誤'),
                    'error'
                )
            }
        }
    })
}

onMounted(() => {
  if (orderId) {
    fetchOrderDetail()
  } else {
    router.push({ name: 'orderList' })
  }
})
</script>

<style scoped lang="scss">


.forest-main-title {
  color: #2e5c43;
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0;
}

.forest-card {
   background-color: white !important;
   border: 1px solid #e0e0e0;
   box-shadow: 0 4px 20px rgba(0,0,0,0.05) !important;
}

.info-list {
  background-color: transparent;
  
  .v-list-item {
    padding-left: 0;
  }
  
  .label {
    min-width: 80px;
    display: inline-block;
    color: #666;
    font-weight: bold;
  }
  
  .value {
    color: #333;
    font-size: 1rem;
  }
}

.forest-table {
   background-color: #fafafa;
   border-radius: 8px;
   overflow: hidden;
   
   th {
     background-color: #f1f8e9 !important;
     font-size: 0.95rem;
   }
}

.forest-input {
  :deep(.v-field__outline) {
    color: #bdbdbd !important;
  }
  
  :deep(.v-field--focused .v-field__outline) {
    color: #2e5c43 !important;
  }

  :deep(input) {
    font-size: 0.95rem;
    color: #333;
  }
}
</style>
