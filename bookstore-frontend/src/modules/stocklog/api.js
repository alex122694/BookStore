import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
    baseURL: API_URL + '/api/log',
    headers: {
        'Content-Type': 'application/json',
    },
});

// 攔截器：統一處理後端 GlobalExceptionHandler 回傳的格式
apiClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.data) {
            const { code, message } = error.response.data;
            // 檢查是否符合 GlobalExceptionHandler 的 BusinessException 格式
            if (code && message) {
                console.error(`[GlobalHandler] StockLog Error Code: ${code}, Message: ${message}`);
                const formattedError = new Error(`[錯誤代碼:${code}] ${message}`);
                formattedError.originalError = error;
                return Promise.reject(formattedError);
            }
        }
        return Promise.reject(error);
    }
);

export default {
    getAllStockLogs() {
        return apiClient.get('/getAllStockLogs');
    },
    getStockLog(logId) {
        return apiClient.get('/getStockLog', { params: { logid: logId } });
    },
    insertStockLog(stockLogData) {
        return apiClient.post('/insert', stockLogData);
    },
    updateStockLog(stockLogData) {
        return apiClient.post('/update', stockLogData);
    },
    returnStockLog(logId) {
        return apiClient.post('/return', { logId });
    }
};
