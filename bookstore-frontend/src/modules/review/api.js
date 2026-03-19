import axios from 'axios'

const API_URL = '/api/public/admin/reviews'

export default {
  // 1. 取得所有評論
  getAllReviews() {
    return axios.get(API_URL)
  },

  // 2. 新增評論
  createReview(reviewData) {
    return axios.post(API_URL, reviewData)
  },

  // 3. 編輯評論
  updateReview(id, reviewData) {
    return axios.put(`${API_URL}/${id}`, reviewData)
  },

  // 4. 刪除評論
  deleteReview(id) {
    return axios.delete(`${API_URL}/${id}`)
  },

  // 取得書籍列表與評價統計
  getBooksWithStats() {
    return axios.get(`${API_URL}/books-stats`)
  },

  // 檢舉評論
  createReport(reportData) {
    return axios.post('/api/public/reports', reportData)
  },

  // (後台) 取得檢舉列表
  getAdminReports() {
    return axios.get('/api/admin/reports')
  },

  // (後台) 審核檢舉
  updateReportStatus(id, newStatus) {
    return axios.put(`/api/admin/reports/${id}`, { status: newStatus })
  },

  // (後台) 取得待處理檢舉數量
  getPendingReportCount() {
    return axios.get('/api/admin/reports/pending-count')
  },

  // 取得特定使用者的所有評價
  getUserReviews(userId) {
    return axios.get(`${API_URL}/user/${userId}`)
  },
}
