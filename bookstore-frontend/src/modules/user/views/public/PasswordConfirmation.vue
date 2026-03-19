<template>
  <v-container class="fill-height d-flex justify-center align-center py-12" fluid>
    <v-card width="100%" max-width="500" class="mx-auto pa-12 rounded-xl forest-card shadow-lg">
      <v-card-item class="text-center mb-8">
        <v-icon icon="mdi-shield-lock-outline" size="56" color="primary" class="mb-4"></v-icon>
        <v-card-title class="text-h4 font-weight-bold text-primary">身分安全驗證</v-card-title>
        <p class="text-subtitle-1 text-grey-darken-1 mt-2">為了保護您的隱私，請再次輸入密碼</p>
      </v-card-item>

      <v-form ref="confirmFormRef" @submit.prevent="handleConfirm">
        <v-text-field
          v-model="password"
          label="請輸入您的登入密碼"
          :type="showPwd ? 'text' : 'password'"
          prepend-inner-icon="mdi-lock-outline"
          :append-inner-icon="showPwd ? 'mdi-eye' : 'mdi-eye-off'"
          variant="outlined"
          color="primary"
          @click:append-inner="showPwd = !showPwd"
          :rules="[v => !!v || '請輸入密碼']"
          class="mb-6"
        ></v-text-field>

        <v-btn type="submit" block height="54" color="primary" class="font-weight-bold rounded-lg shadow-sm">
          驗證並繼續
        </v-btn>

        <v-btn variant="text" block class="mt-6 text-grey-darken-1" @click="router.back()">
          返回上一頁
        </v-btn>

      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const confirmFormRef = ref(null)
const password = ref('')
const showPwd = ref(false)

const handleConfirm = async () => {
  const { valid } = await confirmFormRef.value.validate()
  if (!valid) return

  try {
    const response = await axios.post('http://localhost:8080/api/user/confirm-password', {
      password: password.value
    }, { 
      withCredentials: true
    })

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '身分核實成功',
        timer: 1000,
        showConfirmButton: false
      }).then(() => {
        router.push('/dev/user/profile-edit')
      })
    } else {
      Swal.fire('驗證失敗', response.data.message, 'error')
    }
  } catch (error) {
    console.error(error)
    Swal.fire('錯誤', '系統連線異常', 'error')
  }
}
</script>

<style scoped>
.forest-card {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid rgba(46, 92, 67, 0.05);
}
.demo-btn {
  opacity: 0.5;
  transition: opacity 0.3s;
}
.demo-btn:hover {
  opacity: 1;
}
:deep(.v-field__outline) {
  --v-field-border-width: 1.5px;
}
</style>