<template>
  <v-container class="py-12">
    <v-card width="100%" max-width="900" class="mx-auto rounded-xl forest-card shadow-lg">
      <v-toolbar color="primary" flat>
        <v-toolbar-title class="text-h5 font-weight-bold">會員資料</v-toolbar-title>
      </v-toolbar>

      <v-tabs v-model="activeTab" color="primary" align-tabs="center" grow border-bottom>
        <v-tab value="profile"><v-icon start>mdi-account-edit</v-icon>個人資料</v-tab>
        <v-tab value="password"><v-icon start>mdi-lock-reset</v-icon>修改密碼</v-tab>
      </v-tabs>

      <v-window v-model="activeTab" class="pa-8">
        <v-window-item value="profile">
          <v-form ref="profileFormRef">
            <div class="d-flex flex-column align-center mb-10">
              <v-hover v-slot="{ isHovering, props }">
                <v-avatar size="150" class="avatar-picker elevation-4 cursor-pointer" v-bind="props"
                  @click="triggerFileInput">
                  <v-img :src="avatarPreview || '/default-avatar.png'" cover>
                    <v-overlay :model-value="isHovering" contained scrim="#2e5c43" class="align-center justify-center">
                      <div class="text-center">
                        <v-icon color="white" size="40">mdi-camera-flip-outline</v-icon>
                        <div class="text-white text-caption font-weight-bold">更換大頭貼</div>
                      </div>
                    </v-overlay>
                  </v-img>
                </v-avatar>
              </v-hover>
              <input type="file" ref="fileInput" class="d-none" accept="image/*" @change="onFileSelected">
            </div>

            <v-row>
              <v-col cols="12" md="6"><v-text-field v-model="user.userName" label="姓名" variant="outlined" readonly
                  bg-color="grey-lighten-4" prepend-inner-icon="mdi-account"></v-text-field></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="user.birth" label="生日" variant="outlined" readonly
                  bg-color="grey-lighten-4" prepend-inner-icon="mdi-calendar"></v-text-field></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="user.points" label="累積點數" variant="outlined" readonly
                  bg-color="grey-lighten-4" prepend-inner-icon="mdi-star-circle-outline"></v-text-field></v-col>
              <v-col cols="12" md="6"><v-select v-model="user.gender" label="性別"
                  :items="[{ title: '男', value: 'M' }, { title: '女', value: 'F' }]" variant="outlined"
                  prepend-inner-icon="mdi-gender-male-female" color="primary"></v-select></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="user.email" label="電子信箱" variant="outlined"
                  prepend-inner-icon="mdi-email-outline" :rules="emailRules" validate-on="blur"
                  color="primary"></v-text-field></v-col>
              <v-col cols="12" md="6"><v-text-field v-model="user.phoneNum" label="手機號碼" variant="outlined"
                  prepend-inner-icon="mdi-phone-outline" :rules="phoneRules" validate-on="blur" maxlength="10"
                  color="primary"></v-text-field></v-col>

              <v-col cols="12"><v-text-field v-model="user.address" label="聯絡地址" variant="outlined"
                  prepend-inner-icon="mdi-map-marker-outline" color="primary"></v-text-field></v-col>
            </v-row>

            <div class="d-flex justify-center mt-10 gap-4">
              <v-btn color="primary" class="px-10 rounded-lg shadow-sm font-weight-bold" height="50"
                @click="saveProfile">
                <v-icon start icon="mdi-content-save-outline"></v-icon>儲存修改
              </v-btn>
              <v-btn variant="outlined" color="grey-darken-1" class="px-10 rounded-lg" height="50"
                @click="router.push('/dev/user/user-menu')">
                返回選單
              </v-btn>
            </div>
          </v-form>
        </v-window-item>

        <v-window-item value="password">
          <v-form ref="pwdFormRef" class="mx-auto" style="max-width: 450px">
            <v-text-field v-model="pwdData.oldPwd" label="原登入密碼" type="password" variant="outlined"
              prepend-inner-icon="mdi-lock-outline" class="mb-4" color="primary"></v-text-field>
            <v-text-field v-model="pwdData.newPwd" label="設定新密碼" type="password" variant="outlined"
              prepend-inner-icon="mdi-lock-plus-outline" class="mb-4" color="primary"
              :rules="[v => !!v || '新密碼必填', v => v.length >= 6 || '新密碼至少 6 位']"></v-text-field>
            <v-text-field v-model="pwdData.confirmPwd" label="確認新密碼" type="password" variant="outlined"
              prepend-inner-icon="mdi-lock-check-outline" class="mb-8" color="primary"
              :rules="[v => v === pwdData.newPwd || '兩次密碼輸入不一致']"></v-text-field>

            <v-row dense>
              <v-col cols="12">
                <v-btn color="primary" block height="54" class="rounded-lg font-weight-bold mb-4 shadow-sm"
                  @click="updatePassword">
                  <v-icon start icon="mdi-shield-check-outline"></v-icon>確認修改密碼
                </v-btn>
              </v-col>
              <v-col cols="6">
                <v-btn variant="tonal" color="secondary" block height="48" class="rounded-lg"
                  @click="clearPasswordForm">
                  <v-icon start icon="mdi-refresh"></v-icon>清除內容
                </v-btn>
              </v-col>
              <v-col cols="6">
                <v-btn variant="text" color="grey-darken-1" block height="48" class="rounded-lg"
                  @click="activeTab = 'profile'">
                  取消返回
                </v-btn>
              </v-col>
            </v-row>
          </v-form>
        </v-window-item>
      </v-window>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'
import { useUserStore } from '@/modules/user/store/userStore'

const router = useRouter()
const activeTab = ref('profile')
const isEmailChecking = ref(false)
const isPhoneChecking = ref(false)
const profileFormRef = ref(null)
const pwdFormRef = ref(null)
const userStore = useUserStore()

const fileInput = ref(null)
const avatarPreview = ref('')
let selectedFile = null

let originalData = {}
const user = reactive({
  userName: '', birth: '', gender: '', email: '', phoneNum: '', address: '', points: 0
})
const pwdData = reactive({ oldPwd: '', newPwd: '', confirmPwd: '' })

const emailRules = [
  v => !!v || 'Email 為必填',
  v => /.+@.+\..+/.test(v) || 'Email 格式不正確',
  async (v) => {
    if (v === originalData.email) return true
    isEmailChecking.value = true
    try {
      const res = await axios.get(`http://localhost:8080/api/user/check-unique?email=${v}`)
      return res.data.success || '此 Email 已被他人使用'
    } finally { isEmailChecking.value = false }
  }
]

const phoneRules = [
  v => !!v || '手機為必填',
  v => /^09\d{8}$/.test(v) || '格式不正確',
  async (v) => {
    if (v === originalData.phoneNum) return true
    isPhoneChecking.value = true
    try {
      const res = await axios.get(`http://localhost:8080/api/user/check-unique?phone=${v}`)
      return res.data.success || '此手機號碼已被他人使用'
    } finally { isPhoneChecking.value = false }
  }
]

const triggerFileInput = () => fileInput.value.click()

const onFileSelected = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 2 * 1024 * 1024) {
      Swal.fire('提醒', '圖片不能超過 2MB', 'warning')
      return
    }
    selectedFile = file
    const reader = new FileReader()
    reader.onload = (e) => avatarPreview.value = e.target.result
    reader.readAsDataURL(file)
  }
}

const saveProfile = async () => {
  const { valid } = await profileFormRef.value.validate()
  if (!valid) return

  const result = await Swal.fire({
    title: '確定儲存修改？',
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#2e5c43',
    cancelButtonColor: '#aaa',
    confirmButtonText: '確定儲存',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const formData = new FormData()
      formData.append('gender', user.gender)
      formData.append('email', user.email)
      formData.append('phoneNum', user.phoneNum)
      formData.append('address', user.address)
      if (selectedFile) formData.append('img', selectedFile)

      const response = await axios.post('http://localhost:8080/api/user/update-profile', formData, {
        withCredentials: true,
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      if (response.data.success) {
        Swal.fire({ icon: 'success', title: '修改成功', confirmButtonColor: '#2e5c43', timer: 1500 })
        const newPath = response.data.newImg;
        if (newPath) {
          userStore.img = newPath;
          localStorage.setItem('userImg', newPath);
          avatarPreview.value = `http://localhost:8080${newPath}`;
        }
        originalData = { ...user }
        selectedFile = null
      } else {
        Swal.fire('失敗', response.data.message, 'error')
      }
    } catch (error) {
      Swal.fire('錯誤', '系統連線失敗', 'error')
    }
  }
}

const updatePassword = async () => {
  const { valid } = await pwdFormRef.value.validate()
  if (!valid) return

  const result = await Swal.fire({
    title: '確定修改密碼？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#2e5c43',
    cancelButtonColor: '#aaa',
    confirmButtonText: '確認修改',
    cancelButtonText: '取消'
  })

  if (result.isConfirmed) {
    try {
      const response = await axios.post('http://localhost:8080/api/user/change-password', {
        oldPwd: pwdData.oldPwd,
        newPwd: pwdData.newPwd
      }, { withCredentials: true })

      if (response.data.success) {
        Swal.fire({ icon: 'success', title: '密碼已成功更新', confirmButtonColor: '#2e5c43' }).then(() => {
          clearPasswordForm()
          activeTab.value = 'profile'
        })
      } else {
        Swal.fire('失敗', response.data.message, 'error')
      }
    } catch (error) {
      Swal.fire('錯誤', '連線失敗', 'error')
    }
  }
}

const clearPasswordForm = () => {
  pwdData.oldPwd = ''
  pwdData.newPwd = ''
  pwdData.confirmPwd = ''
  pwdFormRef.value.resetValidation()
}

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/user/my-profile', { withCredentials: true });
    if (response.data.success) {
      const dbData = response.data.user;
      user.userName = dbData.userName;
      user.birth = dbData.birth ? dbData.birth.split('T')[0] : '';
      user.gender = dbData.gender;
      user.email = dbData.email;
      user.phoneNum = dbData.phoneNum;
      user.address = dbData.address;
      user.points = dbData.points || 0;

      const dbImg = dbData.img || '';
      avatarPreview.value = dbImg.startsWith('/uploads/')
        ? `http://localhost:8080${dbImg}`
        : dbImg;

      if (userStore.img !== dbImg) {
        userStore.img = dbImg;
        localStorage.setItem('userImg', dbImg);
      }
      originalData = { ...user };
    }
  } catch (error) {
    Swal.fire('錯誤', '無法載入資料，請重新登入', 'error').then(() => router.push('/dev/user/login'))
  }
})
</script>

<style scoped>
.forest-card {
  border: 1px solid rgba(46, 92, 67, 0.05);
}

.gap-4 {
  gap: 16px;
}

.avatar-picker {
  transition: all 0.3s ease;
  border: 4px solid white;
}

.avatar-picker:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(46, 92, 67, 0.2) !important;
}

.v-btn {
  text-transform: none !important;
  letter-spacing: 1px;
}
</style>