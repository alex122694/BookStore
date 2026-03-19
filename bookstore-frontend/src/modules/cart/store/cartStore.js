import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';

export const useCartStore = defineStore('cart', () => {
    const cartCount = ref(0);

    // 從後端獲取最新購物車數量
    const fetchCartCount = async () => {
        try {
            // 檢查是否有 token，避免未登入時發送請求
            const token = localStorage.getItem('userToken');
            if (!token) {
                cartCount.value = 0;
                return;
            }

            const response = await axios.get('/cart/api/items', {
                withCredentials: true
            });
            if (response.data.success) {
                const items = response.data.cartItems || [];
                cartCount.value = items.length; // 使用品項數量 (List size)
            }
        } catch (error) {
            console.error('無法取得購物車數量:', error);
        }
    };

    // 手動設定數量 (用於加入購物車後直接更新，減少 API 請求)
    const setCartCount = (count) => {
        cartCount.value = count;
    };

    return {
        cartCount,
        fetchCartCount,
        setCartCount
    };
});
