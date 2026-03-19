<template>
  <v-container>
    <v-card class="pa-6 forest-card-border elevation-3 rounded-xl">
      <div class="d-flex align-center justify-space-between mb-6">
        <h2 class="text-h4 font-weight-black" style="color: #2e5c43">
          <v-icon icon="mdi-clipboard-text-clock-outline" class="mr-2"></v-icon>
          {{ currentUserRole === 'SUPER_ADMIN' ? '管理員操作日誌（全系統）' : '我的操作日誌' }}
        </h2>

        <div>
          <v-btn
            color="success"
            variant="elevated"
            prepend-icon="mdi-file-excel"
            class="rounded-lg font-weight-bold mr-2"
            @click="exportToExcel"
          >
            匯出 Excel
          </v-btn>

          <v-btn
            color="primary"
            variant="outlined"
            prepend-icon="mdi-arrow-left"
            class="rounded-lg font-weight-bold"
            @click="router.push('/dev/admin/users')"
          >
            返回列表
          </v-btn>
        </div>
      </div>

      <v-data-table :headers="headers" :items="logs" :loading="loading" class="forest-table" hover>
        <template v-slot:item.actionTime="{ item }">
          <span class="text-grey-darken-1">
            {{ formatDateTime(item.actionTime) }}
          </span>
        </template>

        <template v-slot:item.adminName="{ item }">
          <v-chip color="primary" variant="tonal" size="small" class="font-weight-bold">
            <v-icon start icon="mdi-account-tie" size="16"></v-icon>
            {{ item.adminName }}
          </v-chip>
        </template>

        <template v-slot:item.action="{ item }">
          <div class="py-2 text-body-2 font-weight-medium">
            <template v-for="(part, index) in parseAction(item.action)" :key="index">
              <span v-if="part.type === 'text'">{{ part.content }}</span>

              <a
                v-else-if="part.type === 'link'"
                href="#"
                class="name-link"
                @click.prevent="goToUser(item.targetId)"
              >
                {{ part.content }}
              </a>
            </template>
          </div>
        </template>
      </v-data-table>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import * as XLSX from 'xlsx'

const router = useRouter()
const logs = ref([])
const loading = ref(true)
const currentUserRole = localStorage.getItem('userRole')

const headers = [
  { title: '操作時間', key: 'actionTime', width: '25%', align: 'start' },
  { title: '執行者 (管理員)', key: 'adminName', width: '20%', align: 'start' },
  { title: '操作詳細內容', key: 'action', width: '55%', align: 'start' },
]

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
}

const parseAction = (text) => {
  if (!text) return []
  const regex = /「(.*?)」/g
  const parts = []
  let lastIndex = 0
  let match

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: 'text', content: text.substring(lastIndex, match.index) })
    }
    parts.push({ type: 'link', content: `「${match[1]}」` })
    lastIndex = regex.lastIndex
  }
  if (lastIndex < text.length) {
    parts.push({ type: 'text', content: text.substring(lastIndex) })
  }
  return parts
}

const goToUser = (id) => {
  if (!id) return
  router.push(`/dev/admin/users/get/${id}`)
}

const fetchLogs = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/logs/list')
    logs.value = res.data
  } catch (error) {
    console.error('日誌讀取失敗', error)
  } finally {
    loading.value = false
  }
}

const exportToExcel = () => {
  const exportData = logs.value.map((log) => ({
    操作時間: formatDateTime(log.actionTime),
    執行管理員: log.adminName,
    操作詳細內容: log.action,
    '目標 ID': log.targetId,
  }))

  const worksheet = XLSX.utils.json_to_sheet(exportData)

  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '操作日誌')

  const wscols = [{ wch: 20 }, { wch: 15 }, { wch: 60 }, { wch: 10 }]
  worksheet['!cols'] = wscols

  const fileName = `管理員操作日誌_${new Date().toISOString().split('T')[0]}.xlsx`
  XLSX.writeFile(workbook, fileName)
}

onMounted(fetchLogs)
</script>

<style scoped>
.forest-card-border {
  border-top: 8px solid #2e5c43 !important;
}

:deep(.v-data-table) {
  font-family: inherit !important;
}

:deep(.v-data-table .v-data-table__td) {
  white-space: normal !important;
  word-wrap: break-word;
  font-size: 0.95rem;
}

.name-link {
  color: #2e5c43;
  text-decoration: none;
  font-weight: 800;
  cursor: pointer;
  padding: 0 2px;
  border-radius: 4px;
  transition: all 0.2s;
}

.name-link:hover {
  background-color: #f1f8e9;
  color: #1b5e20;
  text-decoration: underline;
}
</style>
