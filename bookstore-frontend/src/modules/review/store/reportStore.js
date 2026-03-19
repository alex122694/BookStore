import { defineStore } from 'pinia'
import { ref } from 'vue'
import reviewService from '@/modules/review/api'
export const useReportStore = defineStore('report', () => {
  const pendingCount = ref(0) 

  const fetchPendingCount = async () => {
    try {
      const res = await reviewService.getPendingReportCount()
      if (res.data && res.data.count !== undefined) {
        pendingCount.value = res.data.count
      }
    } catch (error) {
      console.error('更新檢舉數量失敗', error)
    }
  }
  return {
    pendingCount,
    fetchPendingCount
  }
})