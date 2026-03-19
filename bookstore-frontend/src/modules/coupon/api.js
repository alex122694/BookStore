import axios from 'axios';

// 定義後端controller的基礎路徑
const API_URL = 'http://localhost:8080/api/coupons';

// 建立一個自定義的 Axios 實例
const apiClient = axios.create({
    // 設定所有請求的共同網址前綴
    baseURL: 'http://localhost:8080',
    // 允許瀏覽器在跨域請求（CORS）時傳送Cookie，一定要加
    // 因後端使用Session機制（JSESSIONID）
    // 不加前端(5173)的請求將無法帶上登入狀態給後端(8080)，導致驗證失敗
    withCredentials: true,
});

//header資料
const getAuthHeaders = () => {
    // 從瀏覽器的本地儲存空間中取出登入時存下的 JWT
    const token = localStorage.getItem('userToken');
    return {
        headers: {
            'Authorization': token ? `Bearer ${token}` : '',
        }
    };
};

export default {
    claimCoupon(code) {
        //Post請求(後端對應網址,Request Body資料,Request Config資料)
        return apiClient.post(`${API_URL}/claim`, { code }, getAuthHeaders());
    },

    getMyCoupons() {
        return apiClient.get(`${API_URL}/my`, getAuthHeaders());
    },

    getAllCouponsAdmin() {
        return apiClient.get(`${API_URL}/admin/all`, getAuthHeaders());
    },

    getAllUserClaimsAdmin() {
        return apiClient.get(`${API_URL}/admin/claims`, getAuthHeaders());
    },

    createCoupon(couponData) {
        return apiClient.post(`${API_URL}/admin/create`, couponData, getAuthHeaders());
    },

    updateCoupon(couponData) {
        return apiClient.post(`${API_URL}/admin/update`, couponData, getAuthHeaders());
    }
};
