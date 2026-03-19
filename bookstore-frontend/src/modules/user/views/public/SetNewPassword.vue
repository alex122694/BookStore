<template>
  <v-container class="fill-height d-flex justify-center align-center py-12" fluid>
    <v-card width="100%" max-width="500" class="mx-auto pa-10 rounded-xl forest-card">
      <v-card-item class="text-center mb-6">
        <v-icon icon="mdi-shield-lock-outline" size="48" color="primary" class="mb-2"></v-icon>
        <v-card-title class="text-h4 font-weight-bold text-primary">設定新密碼</v-card-title>
        <p class="text-subtitle-1 text-grey mt-2">請輸入您的新密碼並妥善保存</p>
      </v-card-item>

      <v-form ref="resetFormRef" @submit.prevent="confirmReset">
        <v-text-field
          v-model="passwordData.newPassword"
          label="新密碼"
          :type="showPwd ? 'text' : 'password'"
          prepend-inner-icon="mdi-lock-outline"
          :append-inner-icon="showPwd ? 'mdi-eye' : 'mdi-eye-off'"
          variant="outlined"
          @click:append-inner="showPwd = !showPwd"
          :rules="pwdRules"
          class="mb-2"
        ></v-text-field>

        <v-text-field
          v-model="passwordData.confirmPassword"
          label="確認新密碼"
          :type="showConfirmPwd ? 'text' : 'password'"
          prepend-inner-icon="mdi-lock-check-outline"
          :append-inner-icon="showConfirmPwd ? 'mdi-eye' : 'mdi-eye-off'"
          variant="outlined"
          @click:append-inner="showConfirmPwd = !showConfirmPwd"
          :rules="confirmPwdRules"
        ></v-text-field>

        <v-btn type="submit" block height="55" color="primary" class="font-weight-bold text-h6 mt-6 rounded-lg">
          重設密碼
        </v-btn>
      </v-form>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const resetFormRef = ref(null)

const userId = route.query.userId
const resetToken = route.query.resetToken
const showPwd = ref(false)
const showConfirmPwd = ref(false)

const passwordData = reactive({
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = [
  v => !!v || '請輸入新密碼',
  v => v.length >= 6 || '密碼長度至少需 6 個字元'
]
const confirmPwdRules = [
  v => !!v || '請再次輸入密碼',
  v => v === passwordData.newPassword || '兩次輸入的密碼不一致'
]

const confirmReset = async () => {
  const { valid } = await resetFormRef.value.validate()
  if (!valid) return

  const result = await Swal.fire({
    title: '確認修改密碼？',
    text: "重設後請使用新密碼重新登入",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#2E5C43',
    cancelButtonColor: '#aaa',
    confirmButtonText: '確定',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    executeReset()
  }
}

const executeReset = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/user/do-reset-password', {
      userId: userId,
      newPassword: passwordData.newPassword,
      resetToken: resetToken
    })

    if (response.data.success) {
      await Swal.fire({
        icon: 'success',
        title: '密碼重設成功！',
        text: '請使用新密碼進行登入',
        confirmButtonColor: '#2E5C43'
      })
      router.push('/dev/user/login') 
    } else {
      Swal.fire('失敗', response.data.message || '重設失敗，請重新操作', 'error')
    }
  } catch (error) {
    Swal.fire('錯誤', '系統連線異常', 'error')
  }
}
</script>