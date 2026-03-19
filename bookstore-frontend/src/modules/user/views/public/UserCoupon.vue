<template>
  <div class="coupon-page-bg">
    <v-container class="py-10">
      <div class="text-center mb-8">
        <h2 class="forest-main-title">我的優惠券</h2>
        <!-- v-divider分隔線，標題下方的小灰線-->
        <v-divider class="mx-auto mt-4" length="60" thickness="4" color="primary"></v-divider>
      </div>

      <!-- 領取優惠券區塊 -->
      <v-card class="forest-card rounded-xl overflow-hidden mb-8 pa-6" elevation="2">
        <!-- elevation="2"陰影深度 -->
        <div class="d-flex align-center gap-4 flex-wrap justify-center">
          <v-text-field
            v-model="couponCode"
            label="輸入優惠碼 (例如: read50)"
            variant="outlined"
            density="comfortable"
            hide-details
            style="max-width: 300px; width: 100%"
          ></v-text-field>
          <v-btn
            color="primary"
            size="large"
            class="rounded-lg font-weight-bold"
            @click="claimCoupon"
            :loading="loading"
          >
            領取優惠券
          </v-btn>
        </div>
      </v-card>

      <!-- 優惠券列表 -->
      <v-row>
        <v-col v-for="coupon in coupons" :key="coupon.couponId" cols="12" sm="6" md="4">
          <v-card class="coupon-card rounded-lg" :class="{ 'used-coupon': coupon.status === 1 }">
            <div class="coupon-content pa-4">
              <div class="d-flex justify-space-between align-start">
                <div>
                  <div class="text-subtitle-1 font-weight-bold mb-1">
                    {{ coupon.couponBean?.couponName }}
                  </div>
                  <div class="text-h4 font-weight-black text-primary mb-1">
                    ${{ coupon.couponBean?.discountAmount }}
                  </div>
                  <div class="text-subtitle-2 text-grey-darken-2">
                    滿 ${{ coupon.couponBean?.minSpend }} 可用
                  </div>
                </div>
                <v-chip
                  :color="coupon.status === 1 ? 'grey' : 'secondary'"
                  label
                  class="font-weight-bold"
                >
                  {{ coupon.status === 1 ? '已使用' : '未使用' }}
                </v-chip>
              </div>
              <v-divider class="my-3"></v-divider>
              <div class="text-caption text-grey">
                優惠碼: <span class="font-weight-bold">{{ coupon.couponBean?.couponCode }}</span>
              </div>
              <div class="text-caption text-grey">
                領取時間: {{ formatDate(coupon.receivedAt) }}
              </div>
              <div v-if="coupon.status === 1" class="text-caption text-grey">
                使用時間: {{ formatDate(coupon.usedAt) }}
              </div>
            </div>
          </v-card>
        </v-col>

        <v-col v-if="coupons.length === 0" cols="12">
          <div class="text-center text-grey py-10">目前沒有優惠券</div>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Swal from 'sweetalert2'
import couponService from '@/modules/coupon/api'

const couponCode = ref('')
const coupons = ref([])
const loading = ref(false)

const fetchCoupons = async () => {
  try {
    const response = await couponService.getMyCoupons()
    if (response.data.success) {
      coupons.value = response.data.coupons || []
      // 排序：未使用的優先，然後按日期降序排列
      coupons.value.sort((a, b) => {
        if (a.status !== b.status) return a.status - b.status
        return new Date(b.receivedAt) - new Date(a.receivedAt)
      })
    }
  } catch (error) {
    console.error('Failed to fetch coupons', error)
  }
}

const claimCoupon = async () => {
  if (!couponCode.value) return

  loading.value = true
  try {
    const response = await couponService.claimCoupon(couponCode.value)
    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '領取成功',
        text: '優惠券已存入您的帳戶，立即下單享優惠！',
        timer: 1500,
        showConfirmButton: false,
      })
      couponCode.value = ''
      fetchCoupons() // 刷新列表
    } else {
      Swal.fire('領取失敗', response.data.message || '無法領取', 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', error.response?.data?.message || '系統錯誤', 'error')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.coupon-page-bg {
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
  border-top: 5px solid #2e5c43 !important;
}

.coupon-card {
  border-left: 6px solid #2e5c43;
  transition: transform 0.2s;
  background-color: white;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.used-coupon {
  border-left-color: #9e9e9e;
  background-color: #f5f5f5;
  opacity: 0.8;
}

.used-coupon .text-primary {
  color: #757575 !important;
}
</style>
