<template>
  <div class="update-page-wrapper">
    <div class="header-section mb-6">
      <h2 class="forest-main-title">會員資料管理</h2>
    </div>

    <v-card width="100%" max-width="850" class="forest-card-border elevation-2 mx-auto rounded-xl">
      <v-tabs v-model="tab" color="primary" align-tabs="center" class="mt-2">
        <v-tab value="profile">
          <v-icon start icon="mdi-account-details-outline"></v-icon>基本資料修改
        </v-tab>
        <v-tab value="password"> <v-icon start icon="mdi-lock-reset"></v-icon>修改帳號密碼 </v-tab>
      </v-tabs>

      <v-divider></v-divider>

      <v-window v-model="tab">
        <v-window-item value="profile">
          <v-form
            ref="profileForm"
            @submit.prevent="handleUpdate"
            v-if="formData.userId"
            class="pa-8"
          >
            <v-container>
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    label="會員編號 (ID)"
                    :model-value="formData.userId"
                    readonly
                    disabled
                    variant="filled"
                    density="compact"
                    bg-color="grey-lighten-4"
                    color="primary"
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.email"
                    label="帳號（Email）"
                    type="email"
                    variant="outlined"
                    density="compact"
                    color="primary"
                    :rules="[(v) => !!v || 'Email 必填', () => !emailError || emailError]"
                    @blur="validateUnique('email')"
                    @input="emailError = ''"
                    required
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.userName"
                    label="姓名"
                    variant="outlined"
                    density="compact"
                    color="primary"
                    :rules="[(v) => !!v || '姓名必填']"
                    required
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="6">
                  <v-select
                    v-model="formData.gender"
                    label="性別"
                    :items="[
                      { title: '男', value: 'M' },
                      { title: '女', value: 'F' },
                    ]"
                    variant="outlined"
                    density="compact"
                    color="primary"
                  ></v-select>
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.birth"
                    label="生日"
                    type="date"
                    variant="outlined"
                    density="compact"
                    color="primary"
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.phoneNum"
                    label="聯絡電話"
                    variant="outlined"
                    density="compact"
                    color="primary"
                    :rules="[() => !phoneError || phoneError]"
                    @blur="validateUnique('phone')"
                    @input="phoneError = ''"
                  ></v-text-field>
                </v-col>

                <v-col cols="12">
                  <v-text-field
                    v-model="formData.address"
                    label="地址"
                    variant="outlined"
                    density="compact"
                    color="primary"
                  ></v-text-field>
                </v-col>

                <v-col cols="12">
                  <v-select
                    v-if="
                      currentUserRole === 'SUPER_ADMIN' &&
                      String(formData.userId) !== currentUserId &&
                      formData.userType !== 2
                    "
                    v-model="formData.userType"
                    label="權限等級"
                    :items="roleSelectOptions"
                    variant="outlined"
                    density="compact"
                    color="primary"
                  ></v-select>

                  <v-text-field
                    v-else
                    label="權限等級"
                    :model-value="getRoleName(formData.userType)"
                    variant="filled"
                    density="compact"
                    readonly
                    disabled
                    bg-color="grey-lighten-4"
                    persistent-hint
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-container>

            <v-card-actions class="justify-center mt-4">
              <v-btn
                type="submit"
                color="primary"
                variant="elevated"
                size="large"
                class="px-10 mr-4 rounded-lg font-weight-bold"
                >確認修改資料</v-btn
              >
              <v-btn
                color="primary"
                variant="outlined"
                size="large"
                class="px-10 rounded-lg font-weight-bold"
                @click="router.push('/dev/admin/users')"
                >取消返回</v-btn
              >
            </v-card-actions>
          </v-form>
        </v-window-item>

        <v-window-item value="password">
          <v-form ref="passwordForm" @submit.prevent="handlePasswordUpdate" class="pa-10">
            <v-container style="max-width: 500px">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="passwordData.userPwd"
                    label="新密碼"
                    type="password"
                    variant="outlined"
                    color="primary"
                    prepend-inner-icon="mdi-lock-outline"
                    :rules="[(v) => !!v || '新密碼必填']"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="passwordData.confirmPwd"
                    label="確認新密碼"
                    type="password"
                    variant="outlined"
                    color="primary"
                    prepend-inner-icon="mdi-lock-check-outline"
                    :rules="[(v) => v === passwordData.userPwd || '兩次密碼不一致']"
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-container>

            <v-card-actions class="justify-center mt-6">
              <v-btn
                type="submit"
                color="primary"
                variant="elevated"
                size="large"
                class="px-10 mr-4 rounded-lg font-weight-bold"
                >確認變更密碼</v-btn
              >
              <v-btn
                variant="outlined"
                size="large"
                class="px-10 rounded-lg font-weight-bold"
                @click="tab = 'profile'"
                >返回基本資料</v-btn
              >
            </v-card-actions>
          </v-form>
        </v-window-item>
      </v-window>

      <v-card-text v-if="!formData.userId" class="text-center pa-10">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
        <div class="mt-4 text-grey">正在讀取會員資料...</div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const tab = ref('profile')
const profileForm = ref(null)
const passwordForm = ref(null)

const emailError = ref('')
const phoneError = ref('')
const currentUserRole = localStorage.getItem('userRole')
const currentUserId = localStorage.getItem('userId')

const formData = ref({
  userId: null,
  email: '',
  userName: '',
  gender: '',
  birth: '',
  phoneNum: '',
  address: '',
  userType: null,
})

const passwordData = reactive({
  userPwd: '',
  confirmPwd: '',
})

const roleSelectOptions = computed(() => {
  const options = [
    { title: '系統管理員', value: 0 },
    { title: '營運專員', value: 1 },
  ]
  if (formData.value.userType === 2) {
    options.unshift({ title: '正式會員 (禁止修改權限)', value: 2, props: { disabled: true } })
  }
  return options
})

const fetchUser = async () => {
  try {
    const userId = route.params.id
    const response = await axios.get(`http://localhost:8080/api/data/get/${userId}`)
    if (response.data) {
      const userData = response.data
      if (
        currentUserRole === 'ADMIN' &&
        (userData.userType === 0 || userData.userType === 1) &&
        String(userData.userId) !== String(currentUserId)
      ) {
        Swal.fire({ icon: 'error', title: '權限不足', text: '您無權修改管理員等級的資料！' })
        router.push('/dev/admin/users')
        return
      }
      formData.value = userData
      if (formData.value.birth) {
        formData.value.birth = new Date(formData.value.birth).toISOString().split('T')[0]
      }
    }
  } catch (error) {
    Swal.fire('錯誤', '讀取會員資料失敗', 'error')
  }
}

const handleUpdate = async () => {
  const { valid } = await profileForm.value.validate()
  if (!valid || emailError.value || phoneError.value) return

  try {
    const response = await axios.put('http://localhost:8080/api/data/update', formData.value)
    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '資料更新成功！',
        confirmButtonColor: '#2E5C43',
      }).then(() => {
        router.push('/dev/admin/users')
      })
    }
  } catch (error) {
    Swal.fire('錯誤', '連線異常', 'error')
  }
}

const handlePasswordUpdate = async () => {
  const { valid } = await passwordForm.value.validate()
  if (!valid) return

  Swal.fire({
    title: '確定要修改密碼嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#2E5C43',
    cancelButtonColor: '#aaa',
    confirmButtonText: '確定',
    cancelButtonText: '取消',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        const updateData = {
          ...formData.value,
          userPwd: passwordData.userPwd,
        }

        const response = await axios.put('http://localhost:8080/api/data/update', updateData)

        if (response.data.success) {
          Swal.fire({
            icon: 'success',
            title: '密碼更新成功！',
            confirmButtonColor: '#2E5C43',
          }).then(() => {
            passwordData.userPwd = ''
            passwordData.confirmPwd = ''
            router.push('/dev/admin/users')
          })
        } else {
          Swal.fire('失敗', response.data.message, 'error')
        }
      } catch (error) {
        console.error('API Error:', error)
        Swal.fire('錯誤', '伺服器連線異常，請稍後再試', 'error')
      }
    }
  })
}

const validateUnique = async (type) => {
  const params = { userId: formData.value.userId }
  if (type === 'email' && formData.value.email) params.email = formData.value.email
  if (type === 'phone' && formData.value.phoneNum) params.phoneNum = formData.value.phoneNum
  try {
    const res = await axios.get('http://localhost:8080/api/users/check-unique', { params })
    if (!res.data.success) {
      if (type === 'email') emailError.value = res.data.message
      if (type === 'phone') phoneError.value = res.data.message
    } else {
      if (type === 'email') emailError.value = ''
      if (type === 'phone') phoneError.value = ''
    }
  } catch (e) {
    console.error(e)
  }
}

const getRoleName = (type) => {
  const roles = {
    0: '系統管理員',
    1: '營運專員',
    2: '正式會員',
  }
  return roles[type] || '未知等級'
}

onMounted(fetchUser)
</script>

<style scoped lang="scss">
.update-page-wrapper {
  width: 100%;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2.25rem;
  font-weight: 800;
  letter-spacing: 1px;
}

.forest-card-border {
  border-top: 6px solid #2e5c43 !important;
  background-color: white !important;
}

:deep(.v-field--disabled) {
  background-color: #f8f9f0 !important;
  opacity: 0.8;
}

:deep(.v-label.v-field-label--floating) {
  color: #2e5c43 !important;
  opacity: 1;
}
</style>
