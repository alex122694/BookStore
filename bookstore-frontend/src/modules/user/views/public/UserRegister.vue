<template>
  <v-container class="register-wrapper py-12">
    <v-card width="100%" max-width="700" class="mx-auto pa-10 rounded-xl forest-card">
      <v-card-item class="text-center mb-6">
        <v-icon :icon="step === 1 ? 'mdi-account-plus-outline' : 'mdi-email-check-outline'" size="48" color="primary" class="mb-2"></v-icon>
        <v-card-title class="text-h4 font-weight-bold text-primary">
          {{ step === 1 ? '加入森林書屋' : '信箱驗證' }}
        </v-card-title>
        <p class="text-subtitle-1 text-grey mt-2">
          {{ step === 1 ? '開啟您的閱讀之旅，探索文字的溫度。' : `驗證碼已寄送到 ${formData.email}` }}
        </p>
      </v-card-item>

      <v-form v-if="step === 1" ref="registerFormRef" @submit.prevent="goToVerifyStep">
        <v-row>
          <v-col cols="12">
            <v-text-field
              v-model="formData.email"
              label="註冊帳號 (Email)"
              :rules="emailRules"
              :loading="isEmailChecking"
              validate-on="blur" 
              prepend-inner-icon="mdi-email-outline"
              variant="outlined"
              color="primary"
            ></v-text-field>
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field v-model="formData.userPwd" label="密碼" :rules="passwordRules" type="password" prepend-inner-icon="mdi-lock-outline" variant="outlined" color="primary" required></v-text-field>
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field v-model="formData.confirmPwd" label="確認密碼" type="password" prepend-inner-icon="mdi-lock-check-outline" variant="outlined" color="primary" :rules="[v => v === formData.userPwd || '密碼不一致']" required></v-text-field>
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field v-model="formData.userName" label="姓名" :rules="requiredRules"  prepend-inner-icon="mdi-account-outline" variant="outlined" color="primary" required></v-text-field>
          </v-col>
          <v-col cols="12" md="6">
            <v-select v-model="formData.gender" label="性別" :items="[{title:'男', value:'M'}, {title:'女', value:'F'}]" prepend-inner-icon="mdi-gender-male-female" variant="outlined" color="primary"></v-select>
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field
              v-model="formData.phoneNum"
              label="手機號碼"
              placeholder="例如：0912345678"
              :rules="phoneRules"
              :loading="isPhoneChecking"
              maxlength="10" 
              counter="10"
              validate-on="blur"
              prepend-inner-icon="mdi-phone-outline"
              variant="outlined"
              color="primary"
              required
            ></v-text-field>
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field v-model="formData.birth" label="西元生日" type="date" :rules="requiredRules" prepend-inner-icon="mdi-calendar-range" variant="outlined" color="primary"></v-text-field>
          </v-col>
          <v-col cols="12">
            <v-text-field v-model="formData.address" label="通訊地址" :rules="requiredRules" prepend-inner-icon="mdi-map-marker-outline" variant="outlined" color="primary"></v-text-field>
          </v-col>
        </v-row>

        <v-btn 
          type="submit" 
          block 
          height="54" 
          color="primary" 
          class="text-h6 font-weight-bold rounded-lg mt-6"
        >
          寄送驗證碼
        </v-btn>
      </v-form>

      <v-form v-else @submit.prevent="handleFinalRegister">
        <v-alert type="info" variant="tonal" class="mb-6">
          請檢查您的電子郵件信箱，並在下方輸入 6 位數驗證碼。
        </v-alert>

        <v-text-field
          v-model="formData.verifyCode"
          label="請輸入 6 位數驗證碼"
          prepend-inner-icon="mdi-shield-check-outline"
          variant="outlined"
          color="primary"
          class="text-center"
          maxlength="6"
          required
        ></v-text-field>

        <div class="text-center mb-6">
          <v-btn variant="text" color="primary" :disabled="isSending" @click="resendCode">
            {{ isSending ? `重新傳送 (${countdown}s)` : '沒收到信？重新傳送' }}
          </v-btn>
        </div>

        <v-btn type="submit" block height="54" color="primary" class="text-h6 font-weight-bold rounded-lg">
          完成註冊
        </v-btn>

        <v-btn variant="text" block class="mt-4" @click="step = 1">
          返回修改資料
        </v-btn>
      </v-form>

      <v-btn v-if="step === 1" variant="text" block class="mt-2" @click="router.push('/dev/user/login')">
        已有帳號？立即登入
      </v-btn>

      <div v-if="step === 1" class="mt-10">
          <v-divider class="mb-4">
          </v-divider>
          <v-btn
            variant="text"
            block
            color="grey-darken-1"
            class="text-none font-weight-regular"
            prepend-icon="mdi-auto-fix"
            @click="fillDemoData"
          >
            一鍵輸入
          </v-btn>
        </div>
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
const registerFormRef = ref(null)

const step = ref(1) 
const isSending = ref(false)
const countdown = ref(60)

const isEmailChecking = ref(false);
const isPhoneChecking = ref(false);

const requiredRules = [v => !!v || '此欄位為必填'];
const passwordRules = [
  v => !!v || '密碼為必填',
  v => (v && v.length >= 6) || '密碼長度至少需要 6 位'
];
const confirmPasswordRules = [
  v => !!v || '請再次輸入密碼',
  v => v === formData.userPwd || '密碼不一致'
];

const formData = reactive({
  email: '',
  verifyCode: '',
  userPwd: '',
  confirmPwd: '',
  userName: '',
  birth: '',
  gender: 'M',
  phoneNum: '',
  address: '',
})

const emailRules = [
  v => !!v || 'Email 為必填',
  v => /.+@.+\..+/.test(v) || 'Email 格式不正確',
  async (v) => {
    if (!v || !/.+@.+\..+/.test(v)) return true;
    isEmailChecking.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/api/user/check-unique?email=${v}`);
      return res.data.success || '此 Email 已被註冊使用';
    } catch (e) {
      return '無法驗證 Email 狀態';
    } finally {
      isEmailChecking.value = false;
    }
  }
];

const phoneRules = [
  v => !!v || '手機號碼為必填',
  v => (v && v.length === 10) || '手機號碼必須正好為 10 位數',
  v => /^09\d{8}$/.test(v) || '手機格式應為 09 開頭的數字',
  async (v) => {
    if (!v || !/^09\d{8}$/.test(v)) return true;
    
    isPhoneChecking.value = true;
    try {
      const res = await axios.get(`http://localhost:8080/api/user/check-unique?phone=${v}`);
      return res.data.success || '此手機號碼已被使用';
    } catch (e) {
      return '無法驗證手機狀態';
    } finally {
      isPhoneChecking.value = false;
    }
  }
];

const fillDemoData = () => {
  formData.userPwd = '123123'
  formData.confirmPwd = '123123'

  formData.birth = '2001-01-24'

  const randomSuffix = Math.floor(10000000 + Math.random() * 90000000)
  formData.phoneNum = `09${randomSuffix}`

  const roads = ['中正路', '復興路', '成功路', '大興西路', '延平路']
  const randomRoad = roads[Math.floor(Math.random() * roads.length)]
  const randomNum = Math.floor(Math.random() * 300) + 1
  formData.address = `桃園市中壢區${randomRoad}${randomNum}號`
  
  formData.gender = 'M'
}

const goToVerifyStep = async (isResendParam = false) => {
  const isActualResend = isResendParam === true;

  if (!isActualResend) {
    if (!registerFormRef.value) return;

    const { valid } = await registerFormRef.value.validate();
    
    if (!valid) {
      Swal.fire('提醒', '請檢查欄位是否填寫正確', 'warning');
      return;
    }
  }

  try {
    const res = await axios.post('http://localhost:8080/api/user/send-code', { 
      email: formData.email 
    });
    
    if (res.data.success) {
      Swal.fire(isActualResend ? '已重新寄送' : '驗證碼已寄出', '請查看您的信箱', 'success');
      
      if (isActualResend) {
        isSending.value = false;
        countdown.value = 60;
      }
      
      step.value = 2; 
      startCountdown();
    } else {
      Swal.fire('失敗', res.data.message, 'error');
    }
  } catch (error) {
    Swal.fire('錯誤', '寄送驗證碼時發生異常', 'error');
  }
};

const handleFinalRegister = async () => {
  if (!formData.verifyCode) {
    Swal.fire('提醒', '請輸入驗證碼', 'warning')
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/user/register', formData)
    
    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '註冊成功',
        text: '歡迎來到森林書屋',
        confirmButtonColor: '#2e5c43'
      }).then(() => {
        router.push('/dev/user/login')
      })
    } else {
      Swal.fire('註冊失敗', response.data.message, 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', '伺服器連線異常', 'error')
  }
}

const startCountdown = () => {
  isSending.value = true
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      isSending.value = false
      countdown.value = 60
    }
  }, 1000)
}

const resendCode = () => {
  if (isSending.value) return 
  goToVerifyStep(true)
}

onMounted(() => {
  if (route.query.from === 'google') {
    formData.email = route.query.email || ''
    formData.userName = route.query.name || ''
  }
})
</script>

<style scoped>
.v-divider :deep(.v-divider__content) {
  background-color: transparent !important;
  letter-spacing: 2px;
  font-family: 'Roboto', sans-serif;
}

.v-btn.text-none {
  opacity: 0.5;
  transition: opacity 0.3s;
}

.v-btn.text-none:hover {
  opacity: 1;
}
</style>