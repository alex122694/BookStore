<template>
  <v-container class="fill-height d-flex justify-center align-center py-12" fluid>
    <v-card width="100%" max-width="500" class="mx-auto pa-12 rounded-xl text-center login-card-box shadow-lg">
      <v-card-item class="mb-6">
        <v-icon icon="mdi-book-open-variant" size="48" color="primary" class="mb-2"></v-icon>
        <v-card-title class="text-h4 font-weight-bold text-primary">會員登入</v-card-title>
      </v-card-item>

      <v-form @submit.prevent="handleLogin" class="mt-4">
        <v-text-field v-model="loginForm.email" label="電子信箱" prepend-inner-icon="mdi-email-outline" type="email"
          variant="outlined" color="primary" class="mb-4 rounded-lg" density="comfortable"></v-text-field>

        <v-text-field v-model="loginForm.password" label="密碼" prepend-inner-icon="mdi-lock-outline" type="password"
          variant="outlined" color="primary" class="mb-2 rounded-lg" density="comfortable"></v-text-field>

        <div class="d-flex justify-end mb-4">
          <span class="text-caption text-primary font-weight-bold hover-opacity cursor-pointer"
            @click="router.push('/dev/user/forgetpassword')">
            忘記密碼？
          </span>
        </div>

        <v-btn type="submit" block height="54" color="primary"
          class="text-h6 font-weight-bold rounded-lg elevation-1 mt-2">
          立即登入
        </v-btn>
      </v-form>

      <div class="d-flex justify-center mt-6">
        <button class="custom-google-circle-btn" @click="handleLoginClick" type="button">
          <div class="gsi-icon">
            <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" style="display: block">
              <path fill="#EA4335"
                d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z">
              </path>
              <path fill="#4285F4"
                d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z">
              </path>
              <path fill="#FBBC05"
                d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z">
              </path>
              <path fill="#34A853"
                d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z">
              </path>
              <path fill="none" d="M0 0h48v48H0z"></path>
            </svg>
          </div>
        </button>
      </div>

      <v-divider class="my-8">
        <span class="text-caption px-3 text-grey">快速測試</span>
      </v-divider>

      <v-row dense justify="center">
        <v-col v-for="type in ['SUPER_ADMIN', 'ADMIN', 'USER', 'BANNED']" :key="type" cols="3">
          <v-btn variant="tonal" icon color="primary" @click="quickLogin(type)" class="quick-btn rounded-circle">
            <v-icon :icon="getIcon(type)" size="20"></v-icon>
            <v-tooltip activator="parent" location="bottom">{{ getLabel(type) }}</v-tooltip>
          </v-btn>
        </v-col>
      </v-row>

      <div class="mt-6 text-body-2 text-grey-darken-1">
        還沒有帳號嗎？
        <router-link to="register" class="text-primary font-weight-bold text-decoration-none register-link">
          立即註冊新會員
        </router-link>
      </div>
    </v-card>
  </v-container>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { useUserStore } from '@/modules/user/store/userStore'
import { useTokenClient } from 'vue3-google-signin'

const router = useRouter()
const userStore = useUserStore()
const loginForm = reactive({ email: '', password: '' })

const getIcon = (type) => {
  const icons = {
    SUPER_ADMIN: 'mdi-shield-check',
    ADMIN: 'mdi-account-cog',
    USER: 'mdi-account',
    BANNED: 'mdi-account-off',
  }
  return icons[type]
}

const { login } = useTokenClient({
  onSuccess: (response) => handleGoogleSuccess(response),
  onError: () => handleGoogleError(),
})

const handleLoginClick = () => {
  login()
}

const getLabel = (type) => {
  const labels = { SUPER_ADMIN: '超管', ADMIN: '管理', USER: '會員', BANNED: '停權' }
  return labels[type]
}

const quickLogin = (type) => {
  const accounts = {
    SUPER_ADMIN: { email: 'TreeorTree@bookstore.com', pass: '12345' },
    ADMIN: { email: 'Maple@bookstore.com', pass: '12345' },
    USER: { email: 'user8@yahoo.com', pass: '123456' },
    BANNED: { email: 'user1@yahoo.com', pass: '123456' },
  }
  loginForm.email = accounts[type].email
  loginForm.password = accounts[type].pass
}

const handleGoogleSuccess = async (response) => {
  const { access_token } = response
  try {
    const res = await axios.post('http://localhost:8080/api/user/google-login', {
      accessToken: access_token,
    })

    if (res.data.success) {
      userStore.login(res.data)
      Swal.fire({
        icon: 'success',
        title: '登入成功',
        text: `歡迎回來，${res.data.userName}`,
        timer: 1500,
        showConfirmButton: false,
      }).then(() => {
        const redirectPath = router.currentRoute.value.query.redirect || '/dev/user/home'
        router.push(redirectPath)
      })
    } else if (res.data.isNewUser) {
      Swal.fire({
        title: '歡迎新朋友！',
        text: '請完成最後的註冊步驟',
        icon: 'info',
        confirmButtonText: '前往註冊',
      }).then(() => {
        router.push({
          path: 'register',
          query: {
            email: res.data.email,
            name: res.data.userName,
            from: 'google',
          },
        })
      })
    } else {
      Swal.fire({
        icon: 'error',
        title: '登入失敗',
        text: res.data.message || '無法完成 Google 登入',
      })
    }
  } catch (error) {
    console.error('Google Login Request Error:', error)
    Swal.fire({ icon: 'error', title: '系統錯誤', text: '伺服器連線失敗' })
  }
}

const handleGoogleError = () => {
  Swal.fire({
    icon: 'error',
    title: 'Google 驗證中斷',
    text: '請確認彈出視窗是否被攔截或網路是否正常',
  })
}

const handleLogin = async () => {
  if (!loginForm.email || !loginForm.password) {
    Swal.fire('提醒', '請輸入電子信箱與密碼', 'warning')
    return
  }

  try {
    const response = await axios.post('http://localhost:8080/api/user/login', {
      email: loginForm.email,
      userPwd: loginForm.password,
    })

    if (response.data.success) {
      userStore.login({
        token: response.data.token,
        role: response.data.role,
        userName: response.data.userName,
        userId: response.data.userId,
      })

      Swal.fire({
        icon: 'success',
        title: '登入成功',
        text: `歡迎回來，${response.data.userName}`,
        timer: 1500,
        showConfirmButton: false,
      }).then(() => {
        const redirectPath = router.currentRoute.value.query.redirect || '/dev/user/home'
        router.push(redirectPath)
      })
    } else {
      Swal.fire({
        icon: 'error',
        title: '登入失敗',
        text: response.data.message || '帳號或密碼錯誤'
      })
    }
  } catch (error) {
    console.error("Login Error:", error)
    Swal.fire({ icon: 'error', title: '連線失敗', text: '伺服器目前無法連線，請稍後再試' })
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.replace('/dev/user/home')
  }
})
</script>

<style scoped>
.register-link {
  position: relative;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #1b3d2c !important;
  text-decoration: underline !important;
}

.hover-opacity:hover {
  opacity: 0.7;
}

.v-divider :deep(.v-divider__content) {
  background-color: white;
}

.custom-google-circle-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0;
  overflow: hidden;
}

.custom-google-circle-btn:hover {
  background-color: #f8f9fa;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.gsi-icon svg {
  width: 24px;
  height: 24px;
}
</style>
