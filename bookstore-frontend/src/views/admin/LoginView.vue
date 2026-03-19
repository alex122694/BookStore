<template>
  <v-app>
    <v-main class="forest-login-bg">
      <v-container class="fill-height d-flex justify-center align-center" fluid>
        <v-hover v-slot="{ isHovering, props }">
          <v-card v-bind="props" width="100%" max-width="520" :elevation="isHovering ? 12 : 4"
            class="pa-8 rounded-xl transition-swing forest-card-border">
            <v-card-item class="text-center">
              <v-icon icon="mdi mdi-book-open-blank-variant-outline" size="x-large" color="primary"
                class="mb-4"></v-icon>
              <v-card-title class="text-h4 font-weight-bold text-primary mb-1">
                森林書屋
              </v-card-title>
              <v-card-subtitle class="text-subtitle-1">後台管理系統</v-card-subtitle>
            </v-card-item>

            <v-divider class="my-6"></v-divider>

            <v-form @submit.prevent="handleLogin">
              <v-text-field v-model="loginForm.email" label="管理員帳號" prepend-inner-icon="mdi-account-circle-outline"
                variant="outlined" color="primary" class="mb-3" hide-details="auto"></v-text-field>

              <v-text-field v-model="loginForm.password" label="密碼" prepend-inner-icon="mdi-lock-open-outline"
                type="password" variant="outlined" color="primary" class="mb-6" hide-details="auto"></v-text-field>

              <v-btn type="submit" block height="50" color="primary"
                class="text-h6 font-weight-bold elevation-2 rounded-lg">
                登入系統
              </v-btn>
            </v-form>

            <v-divider class="my-8"></v-divider>

            <div class="text-subtitle-2 text-grey-darken-1 mb-5 text-center font-weight-bold">
              測試帳號快選
            </div>

            <v-row dense class="px-2">
              <v-col cols="3">
                <v-btn block variant="outlined" color="primary" class="quick-login-btn rounded-lg"
                  @click="quickLogin('SUPER_ADMIN')">
                  <v-icon icon="mdi-shield-check" size="small"></v-icon>
                  <span class="btn-label">系統管理員</span>
                </v-btn>
              </v-col>

              <v-col cols="3">
                <v-btn block variant="outlined" color="secondary" class="quick-login-btn rounded-lg"
                  @click="quickLogin('ADMIN')">
                  <v-icon icon="mdi-account-cog" size="small"></v-icon>
                  <span class="btn-label">營運專員</span>
                </v-btn>
              </v-col>

              <v-col cols="3">
                <v-btn block variant="outlined" color="grey-darken-1" class="quick-login-btn rounded-lg"
                  @click="quickLogin('USER')">
                  <v-icon icon="mdi-account" size="small"></v-icon>
                  <span class="btn-label">正式會員</span>
                </v-btn>
              </v-col>

              <v-col cols="3">
                <v-btn block variant="outlined" color="error" class="quick-login-btn rounded-lg"
                  @click="quickLogin('BANNED')">
                  <v-icon icon="mdi-account-off" size="small"></v-icon>
                  <span class="btn-label">被停權者</span>
                </v-btn>
              </v-col>
            </v-row>

            <div class="text-center text-caption text-grey-lighten-1 mt-8">
              © 2026 森林書屋管理後台
            </div>
          </v-card>
        </v-hover>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { useUserStore } from '@/modules/user/store/userStore'

const router = useRouter()
const userStore = useUserStore()
const loginForm = reactive({
  email: '',
  password: '',
})

const quickLogin = (type) => {
  const accounts = {
    SUPER_ADMIN: { email: 'TreeorTree@bookstore.com', pass: '12345' },
    ADMIN: { email: 'Maple@bookstore.com', pass: '12345' },
    USER: { email: 'leemei122694@gmail.com', pass: 'alex74586' },
    BANNED: { email: 'user1@yahoo.com', pass: '123456' },
  }
  loginForm.email = accounts[type].email
  loginForm.password = accounts[type].pass
}

const handleLogin = async () => {
  if (!loginForm.email || !loginForm.password) return

  try {
    const response = await axios.post('http://localhost:8080/api/login', {
      email: loginForm.email,
      password: loginForm.password,
    })

    if (response.data.success) {
      if (response.data.role === 'USER') {
        Swal.fire({
          icon: 'error',
          title: '權限不足',
          text: '您沒有權限進入後台管理系統！',
          confirmButtonColor: '#B05252',
        })
        return
      }

      localStorage.clear()

      await userStore.login(response.data)

      Swal.fire({
        icon: 'success',
        title: '登入成功',
        text: `歡迎回來，${response.data.userName || '管理員'}`,
        confirmButtonColor: '#2E5C43',
        timer: 1500,
        timerProgressBar: true,
      }).then(() => {
        window.location.href = '/home'
      })
    } else {
      Swal.fire({
        icon: 'error',
        title: '無法登入',
        text: response.data.message,
        confirmButtonColor: '#B05252',
      })
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: '連線失敗',
      text: '請檢查伺服器連線狀態',
      confirmButtonColor: '#B05252',
    })
  }
}

onMounted(() => {
  const token = localStorage.getItem('userToken')
  const role = localStorage.getItem('userRole')

  if (token && (role === 'SUPER_ADMIN' || role === 'ADMIN')) {
    router.push('/home')
    return
  }

  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('logout')) {
    localStorage.clear()
    Swal.fire({
      icon: 'info',
      title: '您已登出',
      confirmButtonColor: '#2E5C43',
      timer: 1500,
      timerProgressBar: true,
      showConfirmButton: true,
    })
    window.history.replaceState({}, document.title, window.location.pathname)
  }
})
</script>

<style scoped lang="scss">
.forest-login-bg {
  background: linear-gradient(135deg, #fcf8f0 0%, #ede0d4 100%);
}

.forest-card-border {
  border-top: 6px solid #2e5c43 !important;
}

.text-primary {
  color: #2e5c43 !important;
}

.quick-login-btn {
  height: 68px !important;
  padding: 8px 0 !important;
  border-width: 1px !important;

  :deep(.v-btn__content) {
    flex-direction: column !important;
    font-size: 0.75rem;
  }

  .btn-label {
    margin-top: 4px;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 0;
  }
}

:deep(.v-btn) {
  text-transform: none !important;
}

.quick-login-btn {
  height: 72px !important;
  padding: 12px 0 !important;
  border-width: 1.5px !important;

  :deep(.v-btn__content) {
    flex-direction: column !important;
    font-size: 0.85rem;
  }

  .btn-label {
    margin-top: 4px;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }
}

:deep(.v-btn) {
  text-transform: none !important;
}
</style>
