<template>
  <div class="list-page-wrapper">
    <div class="header-section mb-6 text-left">
      <h2 class="forest-main-title">會員資料列表</h2>
    </div>

    <v-row class="mb-4" align="center">
      <v-col cols="auto" class="d-flex ga-3">
        <v-btn
          v-if="currentUserRole === 'SUPER_ADMIN'"
          color="primary"
          prepend-icon="mdi-account-plus"
          elevation="2"
          class="rounded-lg font-weight-bold"
          @click="router.push('/dev/admin/users/insert')"
        >
          新增管理帳號
        </v-btn>
        <v-btn
          color="secondary"
          prepend-icon="mdi-clipboard-text-clock-outline"
          elevation="2"
          variant="elevated"
          class="rounded-lg font-weight-bold"
          @click="router.push('/dev/admin/users/logs')"
        >
          查看操作日誌
        </v-btn>
      </v-col>

      <v-spacer></v-spacer>
      <v-col cols="12" md="8" lg="7">
        <div class="d-flex align-center">
          <v-text-field
            v-model="filters.keyword"
            label="搜尋姓名/電話/Email"
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
            bg-color="white"
            color="primary"
            class="rounded-lg flex-grow-1"
            @keydown.enter="fetchUsers"
            @click:clear="resetFilters"
          ></v-text-field>

          <v-select
            v-model="filters.userTypeFilter"
            label="權限篩選"
            :items="roleOptions"
            variant="outlined"
            density="compact"
            hide-details
            bg-color="white"
            color="primary"
            class="rounded-lg ml-3"
            style="max-width: 200px"
            @update:model-value="fetchUsers"
          ></v-select>

          <v-btn
            color="primary"
            variant="flat"
            @click="fetchUsers"
            class="rounded-lg px-6 ml-3 font-weight-bold"
            height="40"
          >
            查詢
          </v-btn>
        </div>
      </v-col>
    </v-row>

    <v-card class="forest-card-table">
      <v-data-table
        :headers="headers"
        :items="users"
        :items-per-page="10"
        class="forest-table-style"
        hover
      >
        <template v-slot:item.img="{ item }">
          <v-avatar size="40" class="elevation-1 border-sm bg-grey-lighten-4">
            <v-img 
              v-if="item.img"
              :src="item.img.startsWith('/uploads/') 
                    ? `http://localhost:8080${item.img}` 
                    : item.img" 
              cover
            >
              <template v-slot:placeholder>
                <div class="d-flex align-center justify-center fill-height bg-primary text-caption text-white">
                  {{ item.userName?.charAt(0) }}
                </div>
              </template>
            </v-img>
            <v-icon v-else icon="mdi-account-circle" color="grey-lighten-1" size="32"></v-icon>
          </v-avatar>
        </template>

        <template v-slot:item.userName="{ item }">
          <a href="#" @click.prevent="goToDetail(item.userId)" class="user-detail-link">
            {{ item.userName }}
          </a>
        </template>

        <template v-slot:item.userType="{ item }">
          <v-chip
            :color="getRoleColor(item.userType)"
            size="small"
            variant="flat"
            class="text-white font-weight-bold"
          >
            {{ formatUserType(item.userType) }}
          </v-chip>
        </template>

        <template v-slot:item.action="{ item }">
          <v-btn
            v-if="canEdit(item)"
            icon="mdi-pencil-outline"
            variant="text"
            color="primary"
            @click="goToUpdate(item.userId)"
          ></v-btn>
        </template>

        <template v-slot:item.status="{ item }">
          <div class="d-flex align-center justify-center">
            <v-switch
              :model-value="item.status === 1"
              color="success"
              hide-details
              density="compact"
              @click.prevent="handleToggleStatus(item)"
            ></v-switch>
            <span
              :class="item.status === 1 ? 'text-success' : 'text-error'"
              class="ml-2 font-weight-bold text-caption"
            >
              {{ item.status === 1 ? '啟用中' : '已停權' }}
            </span>
          </div>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { useRouter, useRoute } from 'vue-router'
import Swal from 'sweetalert2'

const router = useRouter()
const route = useRoute()
const users = ref([])
const currentUserRole = localStorage.getItem('userRole')
const currentUserId = localStorage.getItem('userId')

const filters = reactive({ keyword: '', userTypeFilter: '' })
const roleOptions = [
  { title: '顯示所有使用者', value: '' },
  { title: '系統管理員', value: '0' },
  { title: '營運專員', value: '1' },
  { title: '正式會員', value: '2' },
]

const headers = [
  { title: '編號', key: 'userId', sortable: true, width: '100px', align: 'center' },
  { title: '頭像', key: 'img', sortable: false, width: '80px', align: 'center' },
  { title: '姓名', key: 'userName', sortable: true, align: 'center' },
  { title: 'Email', key: 'email', align: 'center' },
  { title: '電話', key: 'phoneNum', align: 'center' },
  { title: '權限等級', key: 'userType', align: 'center' },
  { title: '修改', key: 'action', sortable: false, align: 'center' },
  { title: '帳號狀態', key: 'status', sortable: false, align: 'center' },
]

const formatUserType = (type) =>
  ({ 0: '系統管理員', 1: '營運專員', 2: '正式會員' })[type] || '未知'

const getRoleColor = (type) => ({ 0: 'primary', 1: 'secondary' })[type] || 'grey'

const canEdit = (u) =>
  currentUserRole === 'SUPER_ADMIN' ||
  (currentUserRole === 'ADMIN' && (u.userType === 2 || String(u.userId) === currentUserId))

const fetchUsers = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/data/list', {
      params: {
        keyword: filters.keyword || null,
        userTypeFilter: filters.userTypeFilter || null,
      },
    })
    users.value = res.data
  } catch (error) {
    console.error('查詢失敗', error)
  }
}

const resetFilters = () => {
  filters.keyword = ''
  filters.userTypeFilter = ''
  fetchUsers()
}

const handleToggleStatus = (user) => {
  const newStatus = user.status === 1 ? 2 : 1
  const actionText = newStatus === 2 ? '停權' : '恢復啟用'

  if (String(user.userId) === currentUserId && newStatus === 2) {
    return Swal.fire({ icon: 'error', title: '操作禁止', text: '您不能停權自己的帳號！' })
  }
  if (
    currentUserRole === 'ADMIN' &&
    (user.userType === 0 || user.userType === 1) &&
    String(user.userId) !== currentUserId
  ) {
    return Swal.fire({ icon: 'error', title: '權限不足', text: '您無權管理其他管理員的權限！' })
  }

  Swal.fire({
    title: `確定要${actionText}會員「${user.userName}」嗎？`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: actionText,
    cancelButtonText: '取消',
    confirmButtonColor: newStatus === 2 ? '#d33' : '#4CAF50',
    cancelButtonColor: '#aaa',
  }).then(async (result) => {
    if (result.isConfirmed) {
      const res = await axios.put(`http://localhost:8080/api/data/status/${user.userId}`, {
        status: newStatus,
      })
      if (res.data.success) {
        user.status = newStatus
        Swal.fire({ icon: 'success', title: '更新成功', timer: 1000, showConfirmButton: false })
      }
    }
  })
}

const goToUpdate = (id) => router.push(`/dev/admin/users/update/${id}`)
const goToDetail = (id) => router.push(`/dev/admin/users/get/${id}`)

onMounted(fetchUsers)
</script>

<style>
.list-page-wrapper {
  padding: 0;
  width: 100%;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0;
}

.user-detail-link {
  color: #2e5c43;
  text-decoration: none;
  font-weight: 700;
  &:hover {
    color: #1b5e20;
    text-decoration: underline;
  }
}

.forest-card-table {
  background-color: white !important;
  border-radius: 12px !important;
  border-top: 5px solid #2e5c43 !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05) !important;
}

.forest-table-style {
  :deep(.v-data-table-header) {
    background-color: #f9fbe7 !important;
  }
  :deep(.v-data-table-header__content) {
    font-weight: 800;
    color: #2e5c43;
  }
}

.v-switch {
  transform: scale(0.8);
}
</style>
