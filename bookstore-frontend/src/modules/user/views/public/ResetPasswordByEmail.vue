<template>
  <v-container class="fill-height d-flex justify-center align-center py-12" fluid>
    <v-card width="100%" max-width="500" class="mx-auto pa-10 rounded-xl forest-card">
      <v-card-item class="text-center mb-6">
        <v-icon icon="mdi-email-check-outline" size="48" color="primary" class="mb-2"></v-icon>
        <v-card-title class="text-h4 font-weight-bold text-primary mb-6">驗證碼確認</v-card-title>
        <p class="text-subtitle-1 text-grey mt-2">
          驗證碼已寄送到您的信箱：<br> <strong class="text-primary">{{ userEmail }}</strong>
        </p>
      </v-card-item>

      <v-form ref="verifyCodeFormRef" @submit.prevent="handleVerifyCode">
        <v-text-field
          v-model="verifyCode"
          label="請輸入 6 位數驗證碼"
          prepend-inner-icon="mdi-numeric"
          variant="outlined"
          color="primary"
          class="text-center"
          maxlength="6"
          :rules="verifyCodeRules"
        ></v-text-field>

        <v-row class="mt-6">
          <v-col cols="6">
            <v-btn
              variant="outlined"
              block
              height="50"
              color="primary"
              :disabled="isSending"
              @click="resendCode"
            >
              {{ isSending ? `重新傳送 (${countdown}s)` : '重新傳送' }}
            </v-btn>
          </v-col>
          <v-col cols="6">
            <v-btn type="submit" block height="50" color="primary" class="font-weight-bold">
              驗證並下一步
            </v-btn>
          </v-col>
        </v-row>

        <v-divider class="my-6">
          <span class="text-caption px-3 text-grey-lighten-1">或</span>
        </v-divider>

        <v-btn
          variant="text"
          block
          class="mt-4"
          color="secondary"
          prepend-icon="mdi-phone"
          @click="redirectToPhoneVerification"
        >
          Email 收不到？試試手機號碼驗證
        </v-btn>

        <v-btn variant="text" block class="mt-2" @click="router.push('/dev/user/login')">
          返回登入
        </v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const verifyCodeFormRef = ref(null)

const userEmail = ref('') 
const verifyCode = ref('')
const userId = ref(null) 

const isSending = ref(false) 
const countdown = ref(60) 
let countdownTimer = null

const verifyCodeRules = [
  v => !!v || '驗證碼為必填',
  v => (v && v.length === 6 && /^\d+$/.test(v)) || '請輸入 6 位數字驗證碼'
]

const startCountdown = () => {
  isSending.value = true
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      isSending.value = false
      countdown.value = 60
    }
  }, 1000)
}

const resendCode = async () => {
  if (isSending.value) return 

  try {
    const response = await axios.post('http://localhost:8080/api/user/send-code', {
      email: userEmail.value,
      type: 'forget' 
    })

    if (response.data.success) {
      Swal.fire({
        toast: true,
        position: 'top-end',
        icon: 'success',
        title: '密碼重設驗證碼已寄出',
        showConfirmButton: false,
        timer: 2000
      })
      startCountdown() 
    } else {
      Swal.fire('錯誤', response.data.message, 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', '郵件伺服器連線失敗', 'error')
  }
}

const handleVerifyCode = async () => {
  const { valid } = await verifyCodeFormRef.value.validate()
  if (!valid) return

  try {
    const response = await axios.post('http://localhost:8080/api/user/verify-reset-code', {
      email: userEmail.value,
      verifyCode: verifyCode.value,
      userId: userId.value 
    })

    if (response.data.success) {
      clearInterval(countdownTimer) 

      Swal.fire({
        icon: 'success',
        title: '驗證成功',
        text: '請輸入您的新密碼',
        confirmButtonColor: '#2E5C43'
      }).then(() => {
        router.push({ 
          path: '/dev/user/set-new-password', 
          query: { userId: userId.value, resetToken: response.data.resetToken } 
        })
      })
    } else {
      Swal.fire('驗證失敗', response.data.message || '驗證碼錯誤或已過期', 'error')
    }
  } catch (error) {
    console.error('驗證碼驗證失敗:', error)
    Swal.fire('錯誤', '系統連線異常，請稍後再試', 'error')
  }
}

const redirectToPhoneVerification = () => {
  Swal.fire({
    title: '系統通知',
    text: '簡訊驗證功能目前進行系統維護中，請繼續使用 Email 驗證完成密碼修改！',
    icon: 'info',
    confirmButtonColor: '#2E5C43',
    confirmButtonText: '我知道了'
  }) 
}

onMounted(() => {
  userEmail.value = route.query.email || ''
  userId.value = route.query.userId || null

  if (!userEmail.value) {
    userEmail.value = 'demo-user@example.com'
    userId.value = '999'
    console.warn('目前處於開發調試模式：已自動填入模擬 Email')
  }

  if (!userEmail.value || !userId.value) {
    Swal.fire('錯誤', '缺少必要的驗證資訊，將返回登入頁面', 'error').then(() => {
      router.push('/dev/user/login')
    })
    return
  }

  startCountdown(); 
})
</script>

<style scoped>
.forest-card {
  box-shadow: 0 8px 30px rgba(46, 92, 67, 0.1);
  border: 1px solid rgba(46, 92, 67, 0.05);
}
.v-btn.text-none {
  text-transform: none !important;
}
.v-divider :deep(.v-divider__content) {
  background-color: transparent !important;
  letter-spacing: 2px;
  font-family: 'Roboto', sans-serif;
}
</style>