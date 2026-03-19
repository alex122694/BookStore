<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import stockLogService from '@/modules/stocklog/api';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';
import BackPageButton from '@/common/components/BackPageButton.vue';

const route = useRoute();
const router = useRouter();
const logId = route.params.id;

const logData = ref(null);
const loading = ref(true);
const booksMap = ref({}); // Map: bookId -> bookName

// 載入資料
const loadData = async () => {
    loading.value = true;
    try {
        // 平行載入單據詳情與書籍列表 (用於對照書名)
        const [logResponse, booksResponse] = await Promise.all([
            stockLogService.getStockLog(logId),
            bookService.getAllBooks()
        ]);

        logData.value = logResponse.data;

        // 建立書籍 ID 對照表
        if (booksResponse.data && Array.isArray(booksResponse.data)) {
            booksResponse.data.forEach(book => {
                booksMap.value[book.bookId] = book.bookName;
            });
        }

    } catch (error) {
        console.error('載入失敗:', error);
        Swal.fire('錯誤', '無法載入單據資料', 'error');
        router.push('/dev/admin/logs'); // 錯誤時返回列表
    } finally {
        loading.value = false;
    }
};

const formatDateTime = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('zh-TW');
};

const formatCurrency = (value) => {
    if (value === null || value === undefined) return '';
    return `$${Number(value).toLocaleString()}`;
};

// 取得書名 (優先從單據資料取，若無則從對照表取)
const getBookName = (item) => {
    // Debug: 檢查 item 結構
    console.log('LogItem:', item);

    // 1. 嘗試從 item.booksBean.bookName 拿
    if (item.booksBean && item.booksBean.bookName) {
        return item.booksBean.bookName;
    }

    // 2. 嘗試從 booksMap 透過 ID 查 (轉字串比對，以防型別差異)
    // Case A: item.booksBean.bookId
    if (item.booksBean && item.booksBean.bookId) {
        const id = String(item.booksBean.bookId);
        if (booksMap.value[id]) return booksMap.value[id];
    }

    // Case B: item.bookId (若結構扁平化)
    if (item.bookId) {
        const id = String(item.bookId);
        if (booksMap.value[id]) return booksMap.value[id];
    }

    return '未知名稱 (ID: ' + (item.booksBean?.bookId || item.bookId || 'Unknown') + ')';
};

onMounted(() => {
    if (logId) {
        loadData();
    } else {
        router.push('/dev/admin/logs');
    }
});
</script>

<template>
    <div class="pa-4 container-max-width">
        <!-- 頂部導航 -->
        <div class="d-flex align-center mb-6">
            <BackPageButton />
            <h2 class="text-h4 font-weight-bold text-primary">單據明細 #{{ logId }}</h2>
        </div>

        <!-- 載入中 -->
        <div v-if="loading" class="d-flex justify-center align-center" style="height: 400px;">
            <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        </div>

        <div v-else-if="logData">
            <v-card class="rounded-lg elevation-2 pa-6 mb-6">
                <!-- 主檔資訊 -->
                <v-row>
                    <v-col cols="12" md="4">
                        <div class="text-subtitle-2 text-grey-darken-1 mb-1">廠商名稱</div>
                        <div class="text-h6 font-weight-bold">{{ logData.wholesaler }}</div>
                    </v-col>
                    <v-col cols="12" md="4">
                        <div class="text-subtitle-2 text-grey-darken-1 mb-1">建立時間</div>
                        <div class="text-body-1">{{ formatDateTime(logData.logTime) }}</div>
                    </v-col>
                    <v-col cols="12" md="4" class="text-md-right">
                        <div class="text-subtitle-2 text-grey-darken-1 mb-1">單據類型</div>
                        <v-chip :color="logData.stockType === 1 ? 'success' : 'error'" label size="large"
                            class="font-weight-bold">
                            {{ logData.stockType === 1 ? '進貨單' : '退貨單' }}
                        </v-chip>
                    </v-col>
                </v-row>
            </v-card>

            <!-- 明細列表 -->
            <v-card class="rounded-lg elevation-2">
                <v-table class="bg-white">
                    <thead>
                        <tr class="bg-grey-lighten-4">
                            <th class="text-left py-3 font-weight-bold">書籍名稱</th>
                            <th class="text-center font-weight-bold">數量</th>
                            <th class="text-right font-weight-bold">成本/單價</th>
                            <th class="text-right font-weight-bold">小計</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in logData.logItemBeans" :key="index" class="hover-row">
                            <td class="py-3">
                                <div class="font-weight-medium">{{ getBookName(item) }}</div>
                                <div class="text-caption text-grey" v-if="item.booksBean?.isbn">
                                    ISBN: {{ item.booksBean.isbn }}
                                </div>
                            </td>
                            <td class="text-center text-subtitle-1"
                                :class="logData.stockType === 1 ? 'text-success' : 'text-error'">
                                {{ item.changeQty }}
                            </td>
                            <td class="text-right">{{ formatCurrency(item.costPrice) }}</td>
                            <td class="text-right font-weight-bold text-subtitle-1">
                                {{ formatCurrency(item.changeQty * item.costPrice) }}
                            </td>
                        </tr>
                    </tbody>
                    <tfoot class="bg-grey-lighten-5">
                        <tr>
                            <td colspan="3" class="text-right font-weight-bold pr-6 text-h6">總金額:</td>
                            <td class="text-right text-h5 font-weight-bold text-primary py-4">
                                {{ formatCurrency(logData.totalAmount) }}
                            </td>
                        </tr>
                    </tfoot>
                </v-table>
            </v-card>
        </div>
    </div>
</template>

<style scoped>
.v-card {
    border-top: 4px solid #2E5C43;
}

.hover-row:hover {
    background-color: #FAFAFA;
}

.container-max-width {
    max-width: 1200px;
    margin: 0 auto;
}
</style>
