/* src/services/bookService.js */
import axios from 'axios';
// 設定 Base URL (與 Vite Proxy 配合)
const API_URL = '/api/books';
const apiClient = axios.create({
    baseURL: API_URL,
    withCredentials: true, // 保持 Session
});

// Request interceptor: 自動帶入 JWT Token
apiClient.interceptors.request.use(
    config => {
        const token = localStorage.getItem('userToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 攔截器：統一處理後端 GlobalExceptionHandler 回傳的格式
apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.data) {
            const { code, message } = error.response.data;
            if (code && message) {
                // 將後端錯誤代碼與訊息組裝，讓前端組件可以直接顯示
                console.error(`[GlobalHandler] Error Code: ${code}, Message: ${message}`);
                const formattedError = new Error(`[${code}] ${message}`);
                formattedError.originalError = error;
                return Promise.reject(formattedError);
            }
        }
        return Promise.reject(error);
    }
);

export default {
    // 取得全列表
    getAllBooks() {
        return apiClient.get('/getAllBooks');
    },
    // 取得單筆 (用於編輯)
    getBook(id) {
        return apiClient.get(`/getBook/${id}`);
    },
    // 取得分類 (用於下拉選單)
    getGenres() {
        return apiClient.get('/genres');
    },
    // ISBN 重複檢查
    checkIsbn(isbn) {
        return apiClient.get(`/isbnCheck?isbn=${isbn}`);
    },
    // 新增書籍 (Multipart: JSON + File)
    createBook(bookData, file) {
        const formData = new FormData();
        // 將書籍資料轉為 JSON Blob，以符合後端 @RequestPart("book")
        const jsonBlob = new Blob([JSON.stringify(bookData)], { type: 'application/json' });
        formData.append('book', jsonBlob);

        if (file) {
            formData.append('file', file);
        }
        return apiClient.post('/insert', formData, {
            headers: { 'Content-Type': 'multipart/form-data' } // axios 會自動設定 boundary
        });
    },
    // 更新書籍
    updateBook(id, bookData, file) {
        const formData = new FormData();
        const jsonBlob = new Blob([JSON.stringify(bookData)], { type: 'application/json' });
        formData.append('book', jsonBlob);

        if (file) {
            formData.append('file', file);
        }
        return apiClient.put(`/update/${id}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    // 快速更新狀態
    updateStatus(id, status) {
        return apiClient.put(`/updateStatus/${id}/${status}`);
    },
    // 封存 (軟刪除)
    archiveBook(id) {
        return apiClient.put(`/archiveBook/${id}`);
    },
    // 解除封存
    unarchiveBook(id) {
        return apiClient.put(`/unarchiveBook/${id}`);
    },
    // 物理刪除
    deleteBook(id) {
        return apiClient.delete(`/delete/${id}`);
    }
};