import axios from 'axios'

const API_URL = '/api'
const apiClient = axios.create({
  baseURL: API_URL,
  withCredentials: true,
})

// Request interceptor: 自動帶入 JWT Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('userToken')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.data) {
      const { code, message } = error.response.data
      if (code && message) {
        console.error(`[BookClubService] Error Code: ${code}, Message: ${message}`)
        const formattedError = new Error(`[${code}] ${message}`)
        formattedError.originalError = error
        return Promise.reject(formattedError)
      }
    }
    return Promise.reject(error)
  },
)

export default {
  // 取得所有讀書會
  getAllClubs() {
    return apiClient.get('/clubs/allClubs')
  },

  // 取得我發起的讀書會
  getMyHostedClubs() {
    return apiClient.get('/clubs/my-hosted')
  },

  // 取得讀書會分類
  getClubCategories() {
    return apiClient.get('/clubs/categories')
  },

  // 取得單筆讀書會
  getClub(id) {
    return apiClient.get(`/clubs/clubs/${id}`)
  },

  // 管理員審核
  approveClub(id) {
    return apiClient.put(`/clubs/approve/${id}`)
  },

  rejectClub(id, reason) {
    return apiClient.put(`/clubs/reject/${id}`, { reason })
  },

  // 刪除讀書會
  deleteClub(id) {
    return apiClient.delete(`/clubs/delete/${id}`)
  },

  // 取消讀書會 (發起人)
  cancelClub(id) {
    return apiClient.put(`/clubs/cancel/${id}`)
  },

  // 結束讀書會 (發起人)
  endClub(id) {
    return apiClient.put(`/clubs/end/${id}`)
  },

  // 新增讀書會
  createClub(clubData, proofFile) {
    const formData = new FormData()
    const jsonBlob = new Blob([JSON.stringify(clubData)], { type: 'application/json' })

    formData.append('bookclub', jsonBlob)

    // proofFile / Proposal Upload
    if (proofFile) {
      formData.append('proofFile', proofFile)
    }

    return apiClient.post('/clubs/insert', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // 更新讀書會 (包含狀態審核，需帶入完整 Bean 與 檔案)
  // 注意：後端 Controller 需要 Multipart 格式
  updateClub(id, clubData, proofFile) {
    const formData = new FormData()
    const jsonBlob = new Blob([JSON.stringify(clubData)], { type: 'application/json' })

    // 注意後端 @RequestPart("data")
    formData.append('data', jsonBlob)

    if (proofFile) {
      formData.append('proof', proofFile)
    }

    // 使用 PUT 方法
    return apiClient.put(`/clubs/update/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // -------------------------
  // 報名相關 API
  // -------------------------

  // 報名讀書會
  register(clubId) {
    return apiClient.post(`/reg/register/${clubId}`)
  },

  // 取消報名
  cancelRegistration(clubId) {
    return apiClient.put(`/reg/cancel/${clubId}`)
  },

  // 取得我參加的讀書會
  getMyRegistrations() {
    return apiClient.get(`/reg/my-registrations`)
  },

  // 取得某讀書會的報名名單 (發起人檢視)
  getClubRegistrations(clubId) {
    return apiClient.get(`/reg/club/${clubId}`)
  },

  // 發起人幫會員報到
  checkInUser(clubId, targetUserId) {
    return apiClient.put(`/reg/checkin/${clubId}/${targetUserId}`)
  },
}
