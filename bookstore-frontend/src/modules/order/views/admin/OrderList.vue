<template>
  <div class="list-page-wrapper">
    <div class="header-section mb-6 text-left">
      <h2 class="forest-main-title">訂單管理列表</h2>
    </div>

    <!-- 頂部操作與篩選區 -->
    <v-row class="mb-4" align="center">
      <v-col cols="auto">
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          elevation="2"
          class="rounded-lg font-weight-bold mr-2"
          @click="router.push({ name: 'orderInsert' })"
        >
          新增訂單
        </v-btn>
        <v-btn
          color="secondary"
          prepend-icon="mdi-chart-bar"
          elevation="2"
          class="rounded-lg font-weight-bold"
          @click="router.push({ name: 'orderAnalysis' })"
        >
          訂單分析
        </v-btn>
      </v-col>

      <v-spacer></v-spacer>
      <v-col cols="12" md="6" lg="5">
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
      <v-col cols="12" md="6" lg="4">
        <v-text-field
          v-model="filters.keyword"
          label="搜尋訂單編號/訂購人"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          hide-details
          bg-color="white"
          color="primary"
          class="rounded-lg"
          clearable
        ></v-text-field>
      </v-col>
    </v-row>

    <!-- 頁籤切換 -->
    <v-tabs v-model="currentTab" color="primary" align-tabs="start" class="mb-4 forest-tabs">
      <v-tab value="active" class="text-h6 font-weight-bold">活動訂單</v-tab>
      <v-tab value="cancelled" class="text-h6 font-weight-bold">已取消/退款訂單</v-tab>
    </v-tabs>

    <!-- 訂單列表 (使用 v-data-table) -->
    <v-card class="forest-card-table">
      <v-data-table
        :headers="tableHeaders"
        :items="filteredOrders"
        :search="filters.keyword"
        :items-per-page="10"
        class="forest-table-style"
        hover
      >
        <!-- 訂單編號連結 -->
        <template v-slot:item.orderId="{ item }">
          <a href="#" @click.prevent="goToDetail(item.orderId)" class="order-link">
            #{{ item.orderId }}
          </a>
        </template>

        <!-- 訂購人 -->
        <template v-slot:item.userBean="{ item }">
          <div class="d-flex align-center">
            <v-avatar size="32" color="secondary" class="mr-2">
              <span class="text-white text-caption">{{
                (item.userBean?.userName || 'U').charAt(0)
              }}</span>
            </v-avatar>
            {{ item.userBean ? item.userBean.userName : '訪客' }}
          </div>
        </template>

        <!-- 金額格式化 -->
        <template v-slot:item.finalAmount="{ item }">
          <span class="text-primary font-weight-bold">${{ item.finalAmount }}</span>
        </template>

        <!-- 付款方式 -->
        <template v-slot:item.paymentMethod="{ item }">
          <v-chip
            size="small"
            :color="item.paymentMethod === 'CREDIT_CARD' ? 'info' : 'warning'"
            variant="flat"
            class="text-white font-weight-bold"
          >
            {{
              item.paymentMethod === 'CREDIT_CARD'
                ? '信用卡'
                : item.paymentMethod === 'COD'
                  ? '貨到付款'
                  : item.paymentMethod
            }}
          </v-chip>
        </template>

        <!-- 訂單狀態 -->
        <template v-slot:item.orderStatus="{ item }">
          <v-chip
            size="small"
            :color="getStatusColor(item.orderStatus)"
            variant="outlined"
            class="font-weight-bold"
          >
            {{ item.orderStatus }}
          </v-chip>
        </template>

        <!-- 日期格式化 -->
        <template v-slot:item.createdAt="{ item }">
          {{ formatDate(item.createdAt) }}
        </template>

        <!-- 最後修改日期 -->
        <template v-slot:item.updatedAt="{ item }">
          {{ formatDate(item.updatedAt) }}
        </template>

        <!-- 操作按鈕 -->
        <template v-slot:item.action="{ item }">
          <div class="d-flex justify-center gap-2">
              <v-tooltip text="查看明細" location="top">
                <template v-slot:activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon="mdi-eye-outline"
                    variant="text"
                    color="primary"
                    size="small"
                    @click="goToDetail(item.orderId)"
                  ></v-btn>
                </template>
              </v-tooltip>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <div class="d-flex justify-center mt-6">
      <v-btn
        variant="text"
        color="secondary"
        prepend-icon="mdi-arrow-left"
        @click="router.push({ name: 'home' })"
      >
        回後台首頁
      </v-btn>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import orderService from '@/modules/order/api'

const router = useRouter()
const currentTab = ref('active')
const activeOrders = ref([])
const cancelledOrders = ref([])

const filters = reactive({
  keyword: '',
  startDate: '',
  endDate: ''
})

const today = new Date().toISOString().split('T')[0]

const filteredOrders = computed(() => {
    let source = currentTab.value === 'active' ? activeOrders.value : cancelledOrders.value
    
    // 日期篩選
    if (filters.startDate || filters.endDate) {
        source = source.filter(order => {
             // 處理 order.createdAt，可能是陣列 [y,m,d,h,min] 或 ISO 字串
             let orderTime = 0
             if (Array.isArray(order.createdAt)) {
                 // 注意月份要 -1
                 orderTime = new Date(order.createdAt[0], order.createdAt[1] - 1, order.createdAt[2]).getTime()
             } else if (order.createdAt) {
                 // 嘗試解析字串，若為 ISO 字串需注意時區，這裡假設是 UTC 或 ISO
                 const d = new Date(order.createdAt)
                 orderTime = d.setHours(0,0,0,0)
             } else {
                 return true // 無日期則不過濾
             }
             
             let isValid = true
             
             // 解析 filters.startDate (YYYY-MM-DD) 為本地時間 00:00:00
             if (filters.startDate) {
                 const [sy, sm, sd] = filters.startDate.split('-').map(Number)
                 const startTime = new Date(sy, sm - 1, sd).getTime()
                 if (orderTime < startTime) isValid = false
             }
             
             // 解析 filters.endDate (YYYY-MM-DD) 為本地時間 00:00:00
             if (filters.endDate && isValid) {
                 const [ey, em, ed] = filters.endDate.split('-').map(Number)
                 const endTime = new Date(ey, em - 1, ed).getTime()
                 if (orderTime > endTime) isValid = false
             }
             
             return isValid
        })
    }
    
    return source
})

const tableHeaders = [
  { title: '訂單編號', key: 'orderId', align: 'start', sortable: true },
  { title: '訂購人', key: 'userBean', align: 'start' },
  { title: '訂單金額', key: 'finalAmount', align: 'end', sortable: true },
  { title: '付款方式', key: 'paymentMethod', align: 'center' },
  { title: '訂單狀態', key: 'orderStatus', align: 'center' },
  { title: '訂單時間', key: 'createdAt', align: 'center', sortable: true },
  { title: '異動時間', key: 'updatedAt', align: 'center', sortable: true },
  { title: '操作', key: 'action', align: 'center', sortable: false },
]

const formatDate = (dateArr) => {
  if (!dateArr) return ''
  if (Array.isArray(dateArr)) {
    const [y, m, d, h, min] = dateArr
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
  }
  return dateArr
}

const getStatusColor = (status) => {
  if (status === '已付款') return 'success'
  if (status === '待付款') return 'warning'
  if (status === '已取消') return 'error'
  return 'grey'
}

const fetchActiveOrders = async () => {
  try {
    const response = await orderService.getAllOrders('Active')
    activeOrders.value = response.data
  } catch (error) {
    console.error('Fetch active orders failed', error)
  }
}

const fetchCancelledOrders = async () => {
  try {
    const response = await orderService.getAllOrders('Cancelled')
    cancelledOrders.value = response.data
  } catch (error) {
    console.error('Fetch cancelled orders failed', error)
  }
}

const goToDetail = (id) => router.push({ name: 'orderDetail-admin', params: { id } })

watch(currentTab, (newTab) => {
  if (newTab === 'active') fetchActiveOrders()
  else fetchCancelledOrders()
})

onMounted(() => {
  fetchActiveOrders()
})
</script>

<style scoped lang="scss">
.list-page-wrapper {
  padding: 0;
  width: 100%;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0;
}

.order-link {
  color: #2e5c43;
  text-decoration: none;
  font-weight: 800;
  &:hover {
    color: #1b5e20;
    text-decoration: underline;
  }
}

.forest-card-table {
  background-color: white !important;
  border-radius: 12px !important;
  border-top: 5px solid #2e5c43 !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05) !important;
}

.forest-table-style {
  :deep(.v-data-table-header) {
    background-color: #f9fbe7 !important;
  }
  :deep(.v-data-table-header__content) {
    font-weight: 800;
    color: #2e5c43;
  }
  :deep(.v-data-table__tr:hover) {
    background-color: #f1f8e9 !important;
  }
}

.forest-tabs {
  :deep(.v-tab--selected) {
    color: #2e5c43 !important;
  }
  :deep(.v-slide-group__content) {
    border-bottom: 2px solid #e0e0e0;
  }
}
</style>
