<template>
  <v-container>
    <div class="header-section mb-6 text-left">
      <h2 class="forest-main-title">優惠券管理</h2>
    </div>

    <!-- 頂部操作與篩選區 -->
    <v-row class="mb-4" align="center">
      <v-col cols="auto">
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          elevation="2"
          class="rounded-lg font-weight-bold mr-2"
          @click="openDialog"
        >
          新增優惠券種類
        </v-btn>
      </v-col>

      <v-spacer></v-spacer>
      <v-col cols="12" md="6" lg="5">
        <v-text-field
          v-model="search"
          label="搜尋優惠券"
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

    <v-card class="forest-card-table elevation-2 rounded-lg">
      <v-tabs v-model="tab" color="primary" align-tabs="start" class="mb-4 forest-tabs">
        <v-tab value="definitions" class="text-h6 font-weight-bold">優惠券種類</v-tab>
        <v-tab value="claims" class="text-h6 font-weight-bold">領取與使用紀錄</v-tab>
      </v-tabs>

      <v-card-text>
        <v-window v-model="tab">
          <!-- 優惠券種類列表 -->
          <v-window-item value="definitions">
            <v-data-table
              :headers="definitionHeaders"
              :items="definitions"
              :loading="loading"
              :search="search"
              hover
              class="forest-table-style"
            >
              <template v-slot:item.discountAmount="{ item }">
                <span class="text-primary font-weight-bold">${{ item.discountAmount }}</span>
              </template>
              <template v-slot:item.minSpend="{ item }">
                <span>${{ item.minSpend }}</span>
              </template>
              <template v-slot:item.isAvailable="{ item }">
                <v-chip
                  :color="item.isAvailable === 1 ? 'success' : 'error'"
                  size="small"
                  class="cursor-pointer font-weight-bold"
                  variant="outlined"
                  @click="toggleStatus(item)"
                >
                  {{ item.isAvailable === 1 ? '開放中' : '已停用' }}
                </v-chip>
              </template>
              <template v-slot:item.createdAt="{ item }">
                {{ formatDate(item.createdAt) }}
              </template>
              <template v-slot:item.updatedAt="{ item }">
                {{ formatDate(item.updatedAt) }}
              </template>
              <!-- Actions -->
              <template v-slot:item.actions="{ item }">
                <div class="d-flex align-center justify-center">
                  <v-btn icon size="small" variant="text" color="primary" @click="editItem(item)">
                    <v-icon>mdi-pencil</v-icon>
                  </v-btn>
                </div>
              </template>
            </v-data-table>
          </v-window-item>

          <!-- 領取紀錄列表 -->
          <v-window-item value="claims">
            <v-data-table
              :headers="claimHeaders"
              :items="claims"
              :loading="loading"
              hover
              class="forest-table-style"
            >
              <template v-slot:item.userBean="{ item }">
                <div class="d-flex align-center">
                  <v-avatar size="32" color="secondary" class="mr-2">
                    <span class="text-white text-caption">{{
                      (item.userBean?.userName || 'U').charAt(0)
                    }}</span>
                  </v-avatar>
                  {{ item.userBean?.userName || item.userId }}
                </div>
              </template>
              <template v-slot:item.couponBean="{ item }">
                {{ item.couponBean?.couponName }} ({{ item.couponBean?.couponCode }})
              </template>
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="item.status === 1 ? 'grey' : 'success'"
                  size="small"
                  variant="outlined"
                  class="font-weight-bold"
                >
                  {{ item.status === 1 ? '已使用' : '未使用' }}
                </v-chip>
              </template>
              <template v-slot:item.receivedAt="{ item }">
                {{ formatDate(item.receivedAt) }}
              </template>
              <template v-slot:item.usedAt="{ item }">
                {{ item.usedAt ? formatDate(item.usedAt) : '-' }}
              </template>
            </v-data-table>
          </v-window-item>
        </v-window>
      </v-card-text>
    </v-card>

    <!-- 新增/編輯 優惠券 Dialog -->
    <v-dialog v-model="dialog" max-width="600px">
      <v-card>
        <v-card-title class="forest-bg-light">
          <span class="text-h5 font-weight-bold forest-text-dark">{{ formTitle }}</span>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="editedItem.couponName"
                  label="優惠券名稱"
                  required
                  variant="outlined"
                  color="primary"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="editedItem.couponCode"
                  label="優惠碼 (Code)"
                  required
                  :disabled="editedIndex > -1"
                  :error-messages="duplicateCodeError"
                  variant="outlined"
                  color="primary"
                ></v-text-field>
                <span v-if="editedIndex > -1" class="text-caption text-grey"
                  >優惠碼建立後不可修改</span
                >
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedItem.discountAmount"
                  label="折扣金額"
                  type="number"
                  prefix="$"
                  variant="outlined"
                  color="primary"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedItem.minSpend"
                  label="最低消費"
                  type="number"
                  prefix="$"
                  variant="outlined"
                  color="primary"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-switch
                  v-model="editedItem.isAvailable"
                  :label="`狀態: ${editedItem.isAvailable === 1 ? '開放領取' : '暫停領取'}`"
                  :true-value="1"
                  :false-value="0"
                  color="success"
                  hide-details
                ></v-switch>
              </v-col>
            </v-row>
            <div v-if="editedIndex > -1" class="text-caption text-grey mt-2">
              建立時間: {{ formatDate(editedItem.createdAt) }}<br />
              更新時間: {{ formatDate(editedItem.updatedAt) }}
            </div>
          </v-container>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-btn
            v-if="editedIndex === -1"
            color="success"
            variant="text"
            prepend-icon="mdi-magic-staff"
            @click="fillDemoData"
            >一鍵帶入 I</v-btn
          >
          <v-btn
            v-if="editedIndex === -1"
            color="success"
            variant="text"
            prepend-icon="mdi-magic-staff"
            @click="fillDemoData2"
            >一鍵帶入 II</v-btn
          >
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="closeDialog">取消</v-btn>
          <v-btn color="primary" variant="elevated" @click="saveCoupon" :disabled="isSaveDisabled"
            >儲存</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<style scoped lang="scss">
.cursor-pointer {
  cursor: pointer;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0;
}

.forest-subtitle {
  color: #5d7a66;
  letter-spacing: 1px;
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
.forest-bg-light {
  background-color: #f1f8e9;
}
.forest-text-dark {
  color: #2e5c43;
}
</style>
<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import Swal from 'sweetalert2'
import couponService from '@/modules/coupon/api'

const tab = ref('definitions')
const loading = ref(false)
const search = ref('')
const definitions = ref([])
const claims = ref([])
const dialog = ref(false)
const editedIndex = ref(-1)

const defaultItem = {
  couponName: '',
  couponCode: '',
  discountAmount: 0,
  minSpend: 0,
  isAvailable: 1,
}

const editedItem = ref({ ...defaultItem })

const definitionHeaders = [
  { title: '優惠券ID', key: 'couponId', sortable: true },
  { title: '優惠券名稱', key: 'couponName', sortable: true },
  { title: '優惠碼', key: 'couponCode', sortable: true },
  { title: '折扣金額', key: 'discountAmount', sortable: true },
  { title: '消費門檻', key: 'minSpend', sortable: true },
  { title: '建立時間', key: 'createdAt', sortable: true },
  { title: '更新時間', key: 'updatedAt', sortable: true },
  { title: '狀態', key: 'isAvailable', sortable: true },
  { title: '修改', key: 'actions', sortable: false },
]

const claimHeaders = [
  { title: 'ID', key: 'id', sortable: true },
  { title: '使用者', key: 'userBean', sortable: true },
  { title: '優惠券', key: 'couponBean', sortable: true },
  { title: '狀態', key: 'status', sortable: true },
  { title: '領取時間', key: 'receivedAt', sortable: true },
  { title: '使用時間', key: 'usedAt', sortable: true },
]

const formTitle = computed(() => {
  return editedIndex.value === -1 ? '新增優惠券種類' : '編輯優惠券種類'
})

// 檢查優惠碼是否重複
const duplicateCodeError = computed(() => {
  if (editedIndex.value > -1) return '' // 更新時不檢查
  if (!editedItem.value.couponCode) return ''

  const code = editedItem.value.couponCode.trim()
  const exists = definitions.value && definitions.value.some((d) => d.couponCode === code)
  return exists ? '此優惠碼已存在，請使用其他代碼' : ''
})

const isSaveDisabled = computed(() => {
  return !!duplicateCodeError.value || !editedItem.value.couponName || !editedItem.value.couponCode
})

const fetchDefinitions = async () => {
  loading.value = true
  try {
    const response = await couponService.getAllCouponsAdmin()
    if (response.data.success) {
      definitions.value = response.data.coupons || []
    }
  } catch (error) {
    console.error('Error fetching definitions:', error)
  } finally {
    loading.value = false
  }
}

const fetchClaims = async () => {
  loading.value = true
  try {
    const response = await couponService.getAllUserClaimsAdmin()
    if (response.data.success) {
      claims.value = response.data.claims || []
    }
  } catch (error) {
    console.error('Error fetching claims:', error)
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  editedIndex.value = -1
  editedItem.value = { ...defaultItem }
  dialog.value = true
}

const editItem = (item) => {
  editedIndex.value = definitions.value.indexOf(item)
  editedItem.value = { ...item }
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
  editedIndex.value = -1
  editedItem.value = { ...defaultItem }
}

const fillDemoData = () => {
  editedItem.value = {
    couponName: '森呼吸．讀書趣',
    couponCode: 'read50',
    discountAmount: 50,
    minSpend: 499,
    isAvailable: 1,
  }
}

const fillDemoData2 = () => {
  editedItem.value = {
    couponName: '森呼吸．享讀書',
    couponCode: 'read500',
    discountAmount: 500,
    minSpend: 4999,
    isAvailable: 0,
  }
}


const saveCoupon = async () => {
  if (isSaveDisabled.value) return

  try {
    editedItem.value.discountAmount = Number(editedItem.value.discountAmount)
    editedItem.value.minSpend = Number(editedItem.value.minSpend)

    let response
    if (editedIndex.value > -1) {
      response = await couponService.updateCoupon(editedItem.value)
    } else {
      response = await couponService.createCoupon(editedItem.value)
    }

    if (response.data.success) {
      Swal.fire('成功', `優惠券種類已${editedIndex.value > -1 ? '更新' : '新增'}`, 'success')
      closeDialog()
      fetchDefinitions()
    } else {
      Swal.fire('失敗', response.data.message, 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', '操作失敗: ' + error.message, 'error')
  }
}

const toggleStatus = async (item) => {
  try {
    const newStatus = item.isAvailable === 1 ? 0 : 1
    // Create a copy to update
    const updateData = { ...item, isAvailable: newStatus }

    // Optimistic update (optional, but UI feels faster)
    // item.isAvailable = newStatus

    const response = await couponService.updateCoupon(updateData)

    if (response.data.success) {
      // Update local data
      item.isAvailable = newStatus
      item.updatedAt = response.data.coupon.updatedAt // Update timestamp locally
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: `已${newStatus === 1 ? '開放' : '停用'}`,
        showConfirmButton: false,
        timer: 1500,
      })
    } else {
      Swal.fire('失敗', response.data.message, 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', '狀態更新失敗: ' + error.message, 'error')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

watch(tab, (newVal) => {
  if (newVal === 'definitions') fetchDefinitions()
  if (newVal === 'claims') fetchClaims()
})

onMounted(() => {
  fetchDefinitions()
})
</script>
