<template>
  <div class="insert-page-wrapper">
    <div class="header-section mb-6">
      <h2 class="forest-main-title">新增會員資料</h2>
    </div>

    <v-card
      width="100%"
      max-width="850"
      class="pa-8 forest-card-border elevation-2 mx-auto rounded-xl"
    >
      <v-card-item class="text-center mb-4">
        <v-icon icon="mdi-account-plus-outline" size="large" color="primary" class="mb-2"></v-icon>
        <v-card-title class="text-h5 font-weight-bold text-primary"> 填寫帳號資訊 </v-card-title>
      </v-card-item>

      <v-divider class="my-4"></v-divider>

      <v-form ref="insertForm" @submit.prevent="handleSubmit">
        <v-container>
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                v-model="formData.email"
                label="帳號 (Email)"
                variant="outlined"
                density="compact"
                color="primary"
                :error-messages="emailError"
                @blur="checkEmailUnique"
                @input="emailError = ''"
                :rules="[
                  (v) => !!v || 'Email 為必填項',
                  (v) => /.+@.+\..+/.test(v) || 'Email 格式不正確',
                ]"
              ></v-text-field>
            </v-col>

            <v-col cols="12" md="6">
              <v-text-field
                v-model="formData.userPwd"
                label="密碼"
                type="password"
                variant="outlined"
                density="compact"
                color="primary"
                :rules="[(v) => !!v || '密碼為必填項']"
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
                :rules="[(v) => !!v || '姓名為必填項']"
                required
              ></v-text-field>
            </v-col>

            <v-col cols="12" md="6">
              <v-text-field
                v-model="formData.phoneNum"
                label="手機號碼"
                variant="outlined"
                density="compact"
                color="primary"
                maxlength="10"
                counter="10"
                persistent-counter
                @blur="checkPhoneUnique"
                @input="phoneError = ''"
                :rules="[
                  (v) => !!v || '電話為必填項',
                  (v) => /^09\d{8}$/.test(v) || '必須是 09 開頭的 10 位數字',
                  () => !phoneError || phoneError,
                ]"
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
                v-model="formData.userType"
                label="權限等級"
                :items="[
                  { title: '系統管理員', value: 0 },
                  { title: '營運專員', value: 1 },
                ]"
                :rules="[(v) => (v !== null && v !== '') || '請選擇權限等級']"
                variant="outlined"
                density="compact"
                color="primary"
                required
              ></v-select>
            </v-col>
          </v-row>
        </v-container>

        <v-divider class="my-6"></v-divider>

        <v-card-actions class="justify-center">
          <v-btn
            type="submit"
            color="primary"
            variant="elevated"
            size="large"
            class="px-10 mr-4 rounded-lg font-weight-bold"
          >
            確認新增帳號
          </v-btn>
          <v-btn
            color="primary"
            variant="outlined"
            size="large"
            class="px-10 rounded-lg font-weight-bold"
            @click="router.push('/dev/admin/users')"
          >
            取消返回
          </v-btn>
        </v-card-actions>
      </v-form>
      <div class="test-data-zone mt-4 pt-4 border-top-dashed text-center">
        <div class="text-caption text-grey-darken-1 mb-2">Demo 快速測試</div>
        <div class="d-flex justify-center gap-2">
          <v-btn
            variant="tonal"
            size="small"
            color="success"
            prepend-icon="mdi-account-check"
            class="rounded-pill"
            @click="quickFill('SUCCESS')"
          >
            全新正確資料
          </v-btn>
          <v-btn
            variant="tonal"
            size="small"
            color="warning"
            prepend-icon="mdi-alert-circle-outline"
            class="rounded-pill"
            @click="quickFill('DUPLICATE')"
          >
            重複 Email/電話
          </v-btn>
        </div>
      </div>
    </v-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const router = useRouter()
const insertForm = ref(null)
const emailError = ref('')
const phoneError = ref('')

const formData = reactive({
  email: '',
  userPwd: '',
  userName: '',
  gender: '',
  birth: '',
  phoneNum: '',
  address: '',
  userType: '',
})

const checkEmailUnique = async () => {
  if (!formData.email || !/.+@.+\..+/.test(formData.email)) return

  try {
    const res = await axios.get('http://localhost:8080/api/users/check-unique', {
      params: {
        email: formData.email,
        userId: formData.userId || null,
      },
    })

    if (!res.data.success && res.data.message.includes('Email')) {
      emailError.value = res.data.message
    } else {
      emailError.value = ''
    }
  } catch (error) {
    console.error('檢查 Email 失敗', error)
  }
}

const checkPhoneUnique = async () => {
  if (!formData.phoneNum) {
    phoneError.value = ''
    return
  }

  if (/^09\d{8}$/.test(formData.phoneNum)) {
    try {
      const res = await axios.get('http://localhost:8080/api/users/check-unique', {
        params: {
          phoneNum: formData.phoneNum,
          userId: formData.userId || null,
        },
      })

      if (!res.data.success) {
        phoneError.value = res.data.message
      } else {
        phoneError.value = ''
      }

      if (insertForm.value) {
        await insertForm.value.validate()
      }
    } catch (error) {
      console.error('檢查手機失敗', error)
    }
  }
}

const handleSubmit = async () => {
  const { valid } = await insertForm.value.validate()

  if (!valid) return

  try {
    const checkRes = await axios.get('http://localhost:8080/api/users/check-unique', {
      params: {
        email: formData.email,
        phoneNum: formData.phoneNum,
      },
    })

    if (!checkRes.data.success) {
      Swal.fire({
        icon: 'warning',
        title: '資料重複',
        text: checkRes.data.message,
        confirmButtonColor: '#2E5C43',
      })
      return
    }

    const response = await axios.post('http://localhost:8080/api/users/insert', formData)

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '新增成功！',
        text: `會員 ${formData.userName} 已成功加入`,
        confirmButtonColor: '#2E5C43',
      }).then(() => {
        router.push('/dev/admin/users')
      })
    } else {
      Swal.fire('失敗', response.data.message, 'error')
    }
  } catch (error) {
    console.error('API 請求失敗:', error)
    Swal.fire('系統錯誤', '新增失敗，請檢查資料是否重複或伺服器狀態', 'error')
  }
}

const quickFill = (mode) => {
  emailError.value = ''
  phoneError.value = ''

  if (mode === 'SUCCESS') {
    const randomTag = Math.floor(Math.random() * 900) + 100 // 產生 100-999
    formData.email = `demo.user${randomTag}@example.com`
    formData.userPwd = 'password123'
    formData.userName = `新會員${randomTag}`
    formData.phoneNum = `09${Math.floor(Math.random() * 90000000) + 10000000}` // 隨機手機號
    formData.gender = 'M'
    formData.birth = '1995-05-20'
    formData.address = '桃園市中壢區中大路300號'
    formData.userType = 1
  } else {
    formData.email = 'okingopen@gmail.com'
    formData.userPwd = 'password123'
    formData.userName = '測試重複員'
    formData.phoneNum = '0989322413'
    formData.gender = 'F'
    formData.birth = '1990-01-01'
    formData.address = '重複資料測試地址'
    formData.userType = 1

    setTimeout(() => {
      checkEmailUnique()
      checkPhoneUnique()
    }, 100)
  }
}
</script>

<style scoped lang="scss">
.insert-page-wrapper {
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

:deep(.v-label.v-field-label--floating) {
  color: #2e5c43 !important;
  opacity: 1;
}

.border-top-dashed {
  border-top: 1px dashed #ccc;
}

.gap-2 {
  gap: 12px;
}

.rounded-pill {
  border-radius: 50px !important;
}

.test-data-zone {
  opacity: 0.7;
  transition: opacity 0.3s;
  &:hover {
    opacity: 1;
  }
}
</style>
