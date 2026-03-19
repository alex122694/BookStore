<template>
  <v-container class="fill-height d-flex justify-center align-center py-12" fluid>
    <v-card width="100%" max-width="550" class="mx-auto pa-12 rounded-xl forest-card shadow-lg">
      
      <v-card-item class="text-center mb-8">
        <v-icon icon="mdi-lock-reset" size="56" color="primary" class="mb-4"></v-icon>
        <v-card-title class="text-h4 font-weight-bold text-primary">重設密碼</v-card-title>
        <p class="text-subtitle-1 text-grey-darken-1 mt-2">請選擇驗證方式以重核身分</p>
      </v-card-item>

      <v-tabs v-model="verifyMethod" color="primary" grow class="mb-10" border-bottom>
        <v-tab value="email"><v-icon start>mdi-email</v-icon>電子郵件</v-tab>
        <v-tab value="phone"><v-icon start>mdi-phone</v-icon>手機號碼</v-tab>
      </v-tabs>

      <v-form ref="forgotFormRef" @submit.prevent="handleVerify">
        <v-window 
          v-model="verifyMethod" 
          class="py-4" 
          style="overflow: hidden;" 
          :touch="false"
        > 
          <v-window-item value="email">
            <v-text-field
              v-model="formData.email"
              label="電子信箱"
              variant="outlined"
              prepend-inner-icon="mdi-email-outline" 
              class="mb-2"
              persistent-placeholder
              persistent-hint
              :rules="verifyMethod === 'email' ? [v => !!v || 'Email 為必填', v => /.+@.+\..+/.test(v) || 'E-mail 格式不正確'] : []"
            ></v-text-field>
          </v-window-item>

          <v-window-item value="phone">
            <v-text-field
              v-model="formData.phoneNum"
              label="手機號碼"
              prepend-inner-icon="mdi-phone-outline"
              variant="outlined"
              maxlength="10"
              class="mb-2"
              persistent-placeholder
              persistent-hint
              :rules="verifyMethod === 'phone' ? [v => !!v || '手機號碼為必填', v => v.length === 10 || '手機格式不正確'] : []"
            ></v-text-field>
          </v-window-item>
        </v-window>

        <v-text-field
          v-model="formData.birth"
          label="西元生日"
          type="date"
          prepend-inner-icon="mdi-calendar-range"
          variant="outlined"
          class="mb-8" 
          persistent-placeholder
          :rules="[(v) => !!v || '生日為必填']"
        ></v-text-field>

        <v-row class="mt-4">
          <v-col cols="6">
            <v-btn variant="outlined" block height="54" color="grey-darken-1" @click="resetForm" class="rounded-lg">
              清除重填
            </v-btn>
          </v-col>
          <v-col cols="6">
            <v-btn type="submit" block height="54" color="primary" class="font-weight-bold rounded-lg shadow-sm">
              送出
            </v-btn>
          </v-col>
        </v-row>

        <v-btn variant="text" block class="mt-6 text-grey-darken-1" @click="router.push('/dev/user/login')">
          返回登入
        </v-btn>

        <div class="mt-10">
          <v-divider class="mb-4"></v-divider>
          <v-btn
            variant="text"
            block
            color="grey-darken-1"
            class="text-none font-weight-regular demo-btn"
            prepend-icon="mdi-auto-fix"
            @click="fillDemoData"
          >
            一鍵輸入
          </v-btn>
        </div>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const forgotFormRef = ref(null)
const verifyMethod = ref('email')

const formData = reactive({
  email: '',
  phoneNum: '',
  birth: '',
})

const resetForm = () => {
  formData.email = ''
  formData.phoneNum = ''
  formData.birth = ''
  forgotFormRef.value.resetValidation()
}

const handleVerify = async () => {
  const { valid } = await forgotFormRef.value.validate()
  if (!valid) return

  const payload = {
    birth: formData.birth,
    method: verifyMethod.value 
  }

  if (verifyMethod.value === 'email') {
    payload.email = formData.email
  } else {
    payload.phoneNum = formData.phoneNum
  }

  if (verifyMethod.value === 'phone') {
    Swal.fire({
      title: '系統通知',
      text: '手機簡訊驗證功能目前進行系統維護中，請改用電子郵件驗證。',
      icon: 'info',
      confirmButtonColor: '#2E5C43'
    })
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/user/forget-password', payload)

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '身分驗證成功',
        text: '請進行下一步驗證',
        confirmButtonColor: '#2E5C43',
      }).then(() => {
        if (verifyMethod.value === 'email') {
          router.push({
            path: '/dev/user/reset-password-by-email',
            query: { userId: response.data.userId, email: formData.email }
          })
        } else {
          router.push({
            path: '/dev/user/reset-password-by-phone',
            query: { userId: response.data.userId, phone: formData.phoneNum }
          })
        }
      })
    } else {
      Swal.fire('驗證失敗', response.data.message || '資料不匹配', 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', '伺服器連線異常', 'error')
  }
}

const fillDemoData = () => {
  verifyMethod.value = 'email'
  formData.email = 'onlinebookstoreforjava@gmail.com'
  formData.birth = '2001-01-24'
}
</script>

<style scoped>
.forest-card {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08) !important;
}

:deep(.v-window) {
  overflow: hidden !important; 
}

:deep(.v-window__container) {
  display: flex !important; 
  transition: transform 0.3s ease-in-out !important;
}

:deep(.v-field__outline) {
  --v-field-border-width: 1.5px;
}
</style>