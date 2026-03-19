<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import stockLogService from '@/modules/stocklog/api';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';
import SmartInputButtons from '@/common/components/SmartInputButtons.vue';

const router = useRouter();
const loading = ref(false);
const books = ref([]); // 所有書籍清單
const isBooksLoading = ref(false);

const logData = ref({
    wholesaler: '',
    stockType: 1, // 預設 1:進貨
    logItemBeans: [] // 明細列表
});

// 初始化第一筆明細
logData.value.logItemBeans.push({
    booksBean: null, // 用於 Autocomplete 綁定完整的 book 物件
    changeQty: 1,
    costPrice: 0
});

// 載入書籍選項
onMounted(async () => {
    isBooksLoading.value = true;
    try {
        const response = await bookService.getAllBooks();

        books.value = response.data;
    } catch (error) {
        console.error('載入書籍清單失敗:', error);
        Swal.fire('提示', '無法載入書籍選單，請確認網路連線', 'warning');
    } finally {
        isBooksLoading.value = false;
    }
});

// 新增明細列
const addItem = () => {
    logData.value.logItemBeans.push({
        booksBean: null,
        changeQty: 1,
        costPrice: 0
    });
};

// [新增] 隨機挑選陣列中的 n 個元素
const getRandomSubarray = (arr, size) => {
    const shuffled = arr.slice(0);
    let i = arr.length;
    let temp, index;
    while (i--) {
        index = Math.floor((i + 1) * Math.random());
        temp = shuffled[index];
        shuffled[index] = shuffled[i];
        shuffled[i] = temp;
    }
    return shuffled.slice(0, size);
};

// [新增] 一鍵輸入測試資料
const oneClickInput = () => {
    if (books.value.length < 5) {
        Swal.fire('提示', '書籍資料不足，無法執行一鍵輸入', 'warning');
        return;
    }

    // 1. 設定廠商
    logData.value.wholesaler = '資展批發商';

    // 2. 挑選書籍 (必須包含 "巴菲特寫給股東的信")
    const targetBookName = "巴菲特寫給股東的信";
    const targetBook = books.value.find(b => b.bookName.includes(targetBookName));

    let selectedBooks = [];

    if (targetBook) {
        // 如果有這本書，先加入
        selectedBooks.push(targetBook);
        // 再隨機挑選 4 本 (排除已選的)
        const otherBooks = books.value.filter(b => b.bookId !== targetBook.bookId);
        const randomOthers = getRandomSubarray(otherBooks, 4); // Total 5
        selectedBooks = selectedBooks.concat(randomOthers);
    } else {
        // 如果沒有，就隨機 5 本 (並提示?)
        // Swal.fire('提示', `找不到 "${targetBookName}"，已隨機挑選。`, 'info');
        selectedBooks = getRandomSubarray(books.value, 5);
    }

    // 3. 填入明細
    logData.value.logItemBeans = selectedBooks.map(book => ({
        booksBean: book, // 設定完整物件以配合 v-autocomplete
        changeQty: Math.floor(Math.random() * 20) + 1,
        costPrice: Math.floor(book.price * 0.6)
    }));
};

// 移除明細列
const removeItem = (index) => {
    if (logData.value.logItemBeans.length > 1) {
        logData.value.logItemBeans.splice(index, 1);
    } else {
        Swal.fire('提示', '至少需要一筆明細', 'info');
    }
};

// 當選擇書籍時，自動帶入該書籍的參考價格(若有的話)或保留 0
// 因為這是進貨成本，通常手動輸入，但可以帶入原價當參考
const onBookSelect = (item) => {
    // 這裡不做強制覆蓋，讓使用者自己輸入成本
};

const clearForm = () => {
    logData.value = {
        wholesaler: '',
        stockType: 1,
        logItemBeans: [{
            booksBean: null,
            changeQty: 1,
            costPrice: 0
        }]
    };
};

// 計算總金額 (前端預覽用)
const totalAmount = computed(() => {
    return logData.value.logItemBeans.reduce((sum, item) => {
        const qty = Number(item.changeQty) || 0;
        const cost = Number(item.costPrice) || 0;
        return sum + (qty * cost);
    }, 0);
});

// 驗證數量 (移除退貨驗證)
const validateQty = (item) => {
    return '';
};

// 送出表單
const submit = async () => {
    // 簡單驗證
    if (!logData.value.wholesaler.trim()) {
        Swal.fire('欄位未填', '請輸入廠商名稱', 'warning');
        return;
    }

    // 驗證明細
    const isValidItems = logData.value.logItemBeans.every(item => {
        // 基本驗證
        if (!item.booksBean || item.changeQty <= 0 || item.costPrice < 0) return false;
        return true;
    });

    if (!isValidItems) {
        Swal.fire('資料不完整或有誤', '請檢查明細：\n1. 必須選擇書籍\n2. 數量須大於 0\n3. 成本須大於等於 0\n4. 退貨數量不可大於現有庫存', 'warning');
        return;
    }

    loading.value = true;

    // 構造送出的 payload (符合後端 StockLogBean 結構)
    // 後端 LogItemBean 需要 booksBean (內含 bookId)
    // 前端 Autocomplete 綁定的是 booksBean 物件，所以直接傳即可
    // 但要確保結構乾淨

    const payload = {
        wholesaler: logData.value.wholesaler,
        stockType: logData.value.stockType,
        logItemBeans: logData.value.logItemBeans.map(item => ({
            changeQty: item.changeQty,
            costPrice: item.costPrice,
            booksBean: { bookId: item.booksBean.bookId } // 只需傳 ID 下去讓後端查
        }))
    };

    try {
        await stockLogService.insertStockLog(payload);
        await Swal.fire({
            title: '新增成功！',
            text: '進退貨單已建立，庫存已更新。',
            icon: 'success',
            confirmButtonColor: '#2E5C43'
        });
        router.push('/dev/admin/logs');
    } catch (error) {
        console.error(error);
        const msg = error.message || '發生未知錯誤';
        Swal.fire('新增失敗', msg, 'error');
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="pa-4">
        <div class="d-flex align-center mb-6">
            <h2 class="text-h4 font-weight-bold text-primary">新增進貨單</h2>
        </div>

        <v-card class="rounded-lg elevation-2 pa-6" :loading="loading">
            <v-form @submit.prevent="submit">
                <!-- 主檔資訊 -->
                <v-row>
                    <v-col cols="12" md="6">
                        <v-text-field v-model="logData.wholesaler" label="廠商名稱" variant="outlined" color="primary"
                            placeholder="請輸入廠商名稱"></v-text-field>
                    </v-col>
                    <v-col cols="12" md="6">
                        <!-- 預設為進貨單，隱藏選項但保留欄位結構以維持版面 -->
                        <v-text-field model-value="進貨單 (庫存增加)" label="單據類型" variant="outlined" readonly hide-details
                            bg-color="grey-lighten-4"></v-text-field>
                    </v-col>
                </v-row>

                <v-divider class="my-4"></v-divider>

                <!-- 明細列表 -->
                <div class="d-flex justify-space-between align-center mb-2">
                    <h3 class="text-h6 text-grey-darken-1">單據明細</h3>
                    <v-btn color="secondary" variant="tonal" prepend-icon="mdi-plus" size="small" @click="addItem">
                        加入書籍
                    </v-btn>
                </div>

                <div v-for="(item, index) in logData.logItemBeans" :key="index"
                    class="mb-2 pa-3 bg-grey-lighten-4 rounded item-row">
                    <v-row dense align="center">
                        <v-col cols="12" md="3">
                            <v-autocomplete v-model="item.booksBean" :items="books" item-title="bookName" return-object
                                label="選擇書籍" variant="outlined" density="compact" hide-details :loading="isBooksLoading"
                                placeholder="輸入書名搜尋">
                                <template v-slot:item="{ props, item }">
                                    <v-list-item v-bind="props"
                                        :subtitle="'ISBN: ' + (item?.raw?.isbn || item?.isbn || '') + ' | 現有庫存: ' + (item?.raw?.stock || item?.stock || 0)"></v-list-item>
                                </template>
                            </v-autocomplete>
                        </v-col>
                        <v-col cols="6" md="2">
                            <v-text-field :model-value="item.booksBean?.stock || 0" label="現有庫存" type="number"
                                variant="outlined" density="compact" hide-details readonly
                                bg-color="grey-lighten-4"></v-text-field>
                        </v-col>
                        <v-col cols="6" md="2">
                            <v-text-field v-model.number="item.changeQty" label="數量" type="number" variant="outlined"
                                density="compact" hide-details min="1"
                                :error-messages="validateQty(item)"></v-text-field>
                        </v-col>
                        <v-col cols="6" md="2">
                            <v-text-field v-model.number="item.costPrice" label="成本/單價" type="number" variant="outlined"
                                density="compact" hide-details prefix="$"></v-text-field>
                        </v-col>
                        <v-col cols="10" md="2" class="text-right d-flex justify-end align-center">
                            <span class="text-subtitle-2 mr-2">小計:</span>
                            <span class="font-weight-bold text-primary">${{ Number(item.changeQty *
                                item.costPrice).toLocaleString() }}</span>
                        </v-col>
                        <v-col cols="2" md="1" class="text-center">
                            <v-btn icon="mdi-delete" size="small" variant="text" color="grey"
                                @click="removeItem(index)"></v-btn>
                        </v-col>
                    </v-row>
                </div>

                <div class="d-flex justify-end align-center mt-6 pa-4 bg-grey-lighten-3 rounded">
                    <span class="text-h6 mr-4">總金額:</span>
                    <span class="text-h4 font-weight-bold text-primary">${{ totalAmount.toLocaleString() }}</span>
                </div>

                <div class="d-flex justify-space-between mt-6">
                    <SmartInputButtons :presets="[
                        { title: '一鍵輸入', handler: oneClickInput, icon: 'mdi-flash', color: 'success' }
                    ]" :showClear="true" :clearHandler="clearForm" />
                    <div>
                        <v-btn variant="text" class="mr-2" @click="router.back()">取消</v-btn>
                        <v-btn type="submit" color="primary" :loading="loading" elevation="2" size="large" width="150">
                            確認送出
                        </v-btn>
                    </div>
                </div>
            </v-form>
        </v-card>
    </div>
</template>

<style scoped>
.v-card {
    border-top: 4px solid #2E5C43;
}

.item-row {
    transition: background-color 0.2s;
}

.item-row:hover {
    background-color: #E0E0E0 !important;
}
</style>
