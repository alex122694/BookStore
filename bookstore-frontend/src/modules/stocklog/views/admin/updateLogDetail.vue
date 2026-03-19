<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import stockLogService from '@/modules/stocklog/api';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';
import BackPageButton from '@/common/components/BackPageButton.vue';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const books = ref([]); // 所有書籍清單
const isBooksLoading = ref(false);

const logData = ref({
    logId: null,
    wholesaler: '',
    stockType: 1, // 預設 1:進貨
    logItemBeans: [] // 明細列表
});

// 載入書籍選項與現有資料
onMounted(async () => {
    isBooksLoading.value = true;
    try {
        // 1. 先載入書籍清單
        const booksResponse = await bookService.getAllBooks();
        books.value = booksResponse.data;

        // 2. 載入現有單據資料 (若有 ID)
        const id = route.params.id;
        if (id) {
            loading.value = true;
            const logResponse = await stockLogService.getStockLog(id);
            const data = logResponse.data;

            // 對應資料欄位
            logData.value = {
                logId: data.logId,
                wholesaler: data.wholesaler,
                stockType: data.stockType,
                logItemBeans: data.logItemBeans.map(item => ({
                    ...item,
                    booksBean: books.value.find(b => b.bookId === item.booksBean?.bookId) || item.booksBean
                }))
            };
        } else {
            // 若無 ID (不該發生)，初始化第一筆
            logData.value.logItemBeans.push({
                booksBean: null,
                changeQty: 1,
                costPrice: 0
            });
        }

    } catch (error) {
        console.error('載入資料失敗:', error);
        Swal.fire('錯誤', '無法載入資料，請確認網路連線', 'error');
        router.back();
    } finally {
        isBooksLoading.value = false;
        loading.value = false;
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

// 移除明細列
const removeItem = (index) => {
    if (logData.value.logItemBeans.length > 1) {
        logData.value.logItemBeans.splice(index, 1);
    } else {
        Swal.fire('提示', '至少需要一筆明細', 'info');
    }
};

const onBookSelect = (item) => {
    // 這裡不做強制覆蓋，讓使用者自己輸入成本
};

// 計算總金額 (前端預覽用)
const totalAmount = computed(() => {
    return logData.value.logItemBeans.reduce((sum, item) => {
        const qty = Number(item.changeQty) || 0;
        const cost = Number(item.costPrice) || 0;
        return sum + (qty * cost);
    }, 0);
});

// 驗證數量 (退貨不可大於庫存)
const validateQty = (item) => {
    if (logData.value.stockType === 2 && item.booksBean) {
        const stock = item.booksBean.stock || 0;
        if (item.changeQty > stock) {
            return '退貨數量不可大於庫存';
        }
    }
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
        // 退貨庫存驗證
        if (logData.value.stockType === 2) {
            const stock = item.booksBean.stock || 0;
            if (item.changeQty > stock) return false;
        }
        return true;
    });

    if (!isValidItems) {
        Swal.fire('資料不完整或有誤', '請檢查明細：\n1. 必須選擇書籍\n2. 數量須大於 0\n3. 成本須大於等於 0\n4. 退貨數量不可大於現有庫存', 'warning');
        return;
    }

    loading.value = true;

    // 構造送出的 payload
    const payload = {
        logId: logData.value.logId, // 更新需要 ID
        wholesaler: logData.value.wholesaler,
        stockType: logData.value.stockType,
        logItemBeans: logData.value.logItemBeans.map(item => ({
            changeQty: item.changeQty,
            costPrice: item.costPrice,
            booksBean: { bookId: item.booksBean.bookId }
        }))
    };

    try {
        await stockLogService.updateStockLog(payload);
        await Swal.fire({
            title: '修改成功！',
            text: '進退貨單已更新，庫存已重新計算。',
            icon: 'success',
            confirmButtonColor: '#2E5C43'
        });
        router.push('/dev/admin/logs');
    } catch (error) {
        console.error(error);
        const msg = error.response?.data?.message || '發生未知錯誤';
        Swal.fire('修改失敗', msg, 'error');
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="pa-4">
        <div class="d-flex align-center mb-6">
            <BackPageButton />
            <h2 class="text-h4 font-weight-bold text-primary">修改進退貨單</h2>
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
                        <!-- 修改時通常鎖定單據類型，避免邏輯複雜，或允許修改視需求而定。這裡先允許修改，但需注意後端邏輯。 -->
                        <v-radio-group v-model="logData.stockType" inline hide-details label="單據類型">
                            <v-radio label="進貨單 (庫存增加)" :value="1" color="success"></v-radio>
                            <!-- <v-radio label="退貨單 (庫存減少)" :value="2" color="error"></v-radio> -->
                        </v-radio-group>
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

                <div class="d-flex justify-end mt-6">
                    <v-btn variant="text" class="mr-2" @click="router.back()">取消</v-btn>
                    <v-btn type="submit" color="primary" :loading="loading" elevation="2" size="large" width="150">
                        更新儲存
                    </v-btn>
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
