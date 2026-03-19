<template>
  <div class="detail-page-wrapper">
    <div class="header-section mb-6">
      <h2 class="forest-main-title">會員詳細資料</h2>
    </div>

    <v-card
      width="100%"
      max-width="800"
      class="pa-8 forest-card-border elevation-2 mx-auto rounded-xl"
    >
      <div class="d-flex flex-column align-center mt-4">
        <v-avatar size="120" class="elevation-3 border-sm bg-grey-lighten-4">
          <v-img 
            v-if="user && user.img"
            :src="user.img.startsWith('/uploads/') 
                  ? `http://localhost:8080${user.img}` 
                  : user.img" 
            cover
          >
            <template v-slot:placeholder>
              <div class="d-flex align-center justify-center fill-height bg-primary text-h3 text-white">
                {{ user.userName?.charAt(0).toUpperCase() }}
              </div>
            </template>
          </v-img>
          <v-icon v-else icon="mdi-account" size="80" color="grey-lighten-1"></v-icon>
        </v-avatar>
      </div>
      <v-card-item class="text-center mb-4">
      </v-card-item>

      <v-divider class="mb-8"></v-divider>

      <v-card-text v-if="user">
        <v-row v-for="(field, index) in displayFields" :key="index" class="mb-4" align="center">
          <v-col cols="4" class="text-right text-subtitle-1 font-weight-bold text-forest-label">
            {{ field.label }}
          </v-col>
          <v-col cols="8">
            <v-text-field
              :model-value="field.value"
              readonly
              variant="outlined"
              density="compact"
              hide-details
              bg-color="grey-lighten-5"
              color="primary"
              class="rounded-lg"
            ></v-text-field>
          </v-col>
        </v-row>
      </v-card-text>

      <v-card-text v-else class="text-center pa-10">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
        <div class="mt-4 text-grey">正在取得會員資料...</div>
      </v-card-text>

      <v-divider class="my-6"></v-divider>

      <v-card-actions class="justify-center pb-4">
        <v-btn
          v-if="user && canEdit(user)"
          color="primary"
          variant="elevated"
          prepend-icon="mdi-pencil"
          size="large"
          class="px-8 mr-4 rounded-lg font-weight-bold"
          @click="router.push(`/dev/admin/users/update/${user.userId}`)"
        >
          修改資料
        </v-btn>

        <v-btn
          variant="outlined"
          color="primary"
          prepend-icon="mdi-arrow-left"
          size="large"
          class="px-8 rounded-lg font-weight-bold"
          @click="handleBack"
        >
          返回
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const user = ref(null)

const currentUserRole = localStorage.getItem('userRole')
const currentUserId = localStorage.getItem('userId')

const displayFields = computed(() => {
  if (!user.value) return []
  return [
    { label: '會員編號', value: user.value.userId },
    { label: 'Email 帳號', value: user.value.email },
    { label: '會員姓名', value: user.value.userName },
    { label: '性別', value: formatGender(user.value.gender) },
    { label: '生日', value: formatDate(user.value.birth) },
    { label: '聯絡電話', value: user.value.phoneNum },
    { label: '地址', value: user.value.address },
    { label: '權限等級', value: formatUserType(user.value.userType) },
  ]
})

const canEdit = (u) => {
  return (
    currentUserRole === 'SUPER_ADMIN' ||
    (currentUserRole === 'ADMIN' && (u.userType === 2 || String(u.userId) === currentUserId))
  )
}

const formatGender = (code) => (code === 'M' ? '男' : code === 'F' ? '女' : '未設定')
const formatDate = (val) => (val ? new Date(val).toISOString().split('T')[0] : '未設定')
const formatUserType = (type) =>
  ({ 0: '系統管理員', 1: '營運專員', 2: '正式會員' })[type] || '未知'

const fetchUserDetail = async () => {
  try {
    const userId = route.params.id
    const response = await axios.get(`http://localhost:8080/api/data/get/${userId}`)
    user.value = response.data
  } catch (error) {
    Swal.fire({ icon: 'error', title: '讀取失敗', text: '無法取得會員詳細資料' })
  }
}

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/dev/admin/users')
  }
}

onMounted(fetchUserDetail)
</script>

<style scoped lang="scss">
.detail-page-wrapper {
  width: 100%;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2.25rem;
  font-weight: 800;
  letter-spacing: 1px;
}

.text-forest-label {
  color: #2e5c43 !important;
}

.forest-card-border {
  border-top: 6px solid #2e5c43 !important;
  background-color: white !important;
}

:deep(.v-field--disabled),
:deep(.v-field--readonly) {
  opacity: 1 !important;
  .v-field__outline {
    --v-field-border-opacity: 0.15;
  }
}
</style>
