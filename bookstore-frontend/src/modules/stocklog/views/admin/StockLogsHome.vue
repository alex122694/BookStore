<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import stockLogService from '@/modules/stocklog/api';
import Swal from 'sweetalert2';
import ActionPageButton from '@/common/components/ActionPageButton.vue';

const router = useRouter();
const logs = ref([]);
const loading = ref(false);
const search = ref('');

// 篩選條件
const filterType = ref(null); // null: 全部, 1: 進貨, 2: 退貨
const startDate = ref(null);
const endDate = ref(null);


const headers = [
    { title: '單號', key: 'logId', align: 'start', sortable: true },
    { title: '日期', key: 'logTime', align: 'start', sortable: true },
    { title: '進/退貨', key: 'stockType', align: 'center', sortable: true },
    { title: '廠商', key: 'wholesaler', align: 'start', sortable: true },
    { title: '總金額', key: 'totalAmount', align: 'end', sortable: true },
    { title: '明細', key: 'actions', align: 'center', sortable: false },
    { title: '修改', key: 'actions_edit', align: 'center', sortable: false },
    { title: '退貨', key: 'actions_return', align: 'center', sortable: false },
];

const loadLogs = async () => {
    loading.value = true;
    try {
        const response = await stockLogService.getAllStockLogs();
        logs.value = response.data;
    } catch (error) {
        console.error('載入貨單失敗:', error);
        Swal.fire('錯誤', '無法載入進退貨單資料', 'error');
    } finally {
        loading.value = false;
    }
};

// 篩選邏輯
const filteredLogs = computed(() => {
    return logs.value.filter(item => {
        // 1. 類型篩選
        if (filterType.value !== null && item.stockType !== filterType.value) {
            return false;
        }
        // 2. 日期篩選
        if (startDate.value) {
            const itemDate = new Date(item.logTime).setHours(0, 0, 0, 0);
            const start = new Date(startDate.value).setHours(0, 0, 0, 0);
            if (itemDate < start) return false;
        }
        if (endDate.value) {
            const itemDate = new Date(item.logTime).setHours(0, 0, 0, 0);
            const end = new Date(endDate.value).setHours(0, 0, 0, 0);
            if (itemDate > end) return false;
        }
        return true;
    });
});

const openDetail = (logId) => {
    router.push({ name: 'admin-logs-detail', params: { id: logId } });
};

// 退貨處理
const handleReturn = async (item) => {
    try {
        const result = await Swal.fire({
            title: '確定要退貨嗎？',
            text: '退貨為不可返回作業，確認是否要進行退貨',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: '確認退貨',
            cancelButtonText: '取消'
        });

        if (result.isConfirmed) {
            await stockLogService.returnStockLog(item.logId);
            await Swal.fire('已退貨', '單據狀態已更新為退貨', 'success');
            loadLogs(); // 重新載入
        }
    } catch (error) {
        console.error('退貨失敗:', error);
        Swal.fire('錯誤', '退貨作業失敗', 'error');
    }
};

const editLog = (logId) => {
    router.push({ name: 'admin-logs-update', params: { id: logId } });
};

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('zh-TW');
};

const formatDateTime = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('zh-TW');
};

const formatCurrency = (value) => {
    if (value === null || value === undefined) return '';
    return `$${Number(value).toLocaleString()}`;
};

onMounted(() => {
    loadLogs();
});
</script>

<template>
    <div class="pa-4">
        <!-- 頂部操作區：標題 + 按鈕  -->
        <div class="d-flex flex-column align-start mb-6">
            <h2 class="text-h4 font-weight-bold text-primary mb-4">進貨管理</h2>
            <ActionPageButton @click="router.push('/dev/admin/logs/insert')">
                新增進貨單
            </ActionPageButton>
        </div>

        <!-- 篩選區塊 -->
        <v-card class="mb-6 pa-4 bg-grey-lighten-5 rounded-lg" variant="outlined">
            <v-row align="center" dense>
                <v-col cols="12" md="3">
                    <v-select v-model="filterType" label="單據類型"
                        :items="[{ title: '全部', value: null }, { title: '進貨單', value: 1 }, { title: '退貨單', value: 2 }]"
                        item-title="title" item-value="value" variant="outlined" density="compact" hide-details
                        prepend-inner-icon="mdi-filter" bg-color="white"></v-select>
                </v-col>
                <v-col cols="12" md="3">
                    <v-text-field v-model="startDate" label="開始日期" type="date" variant="outlined" density="compact"
                        hide-details bg-color="white"></v-text-field>
                </v-col>
                <v-col cols="12" md="3">
                    <v-text-field v-model="endDate" label="結束日期" type="date" variant="outlined" density="compact"
                        hide-details bg-color="white"></v-text-field>
                </v-col>
                <v-col cols="12" md="3">
                    <v-text-field v-model="search" label="搜尋關鍵字..." prepend-inner-icon="mdi-magnify" variant="outlined"
                        density="compact" hide-details color="primary" bg-color="white"></v-text-field>
                </v-col>
            </v-row>
        </v-card>

        <!-- 列表區 -->
        <v-card class="rounded-lg elevation-2">
            <v-data-table :headers="headers" :items="filteredLogs" :search="search" :loading="loading" hover
                class="log-table" item-value="logId">

                <!-- 單號 (可點擊) -->
                <template v-slot:item.logId="{ item }">
                    <a href="#" @click.prevent="openDetail(item.logId)"
                        class="text-primary font-weight-bold text-decoration-underline">
                        {{ item.logId }}
                    </a>
                </template>

                <!-- 日期格式化 -->
                <template v-slot:item.logTime="{ item }">
                    {{ formatDate(item.logTime) }}
                </template>

                <!-- 類型顯示 (顏色區分) -->
                <template v-slot:item.stockType="{ item }">
                    <v-chip :color="item.stockType === 1 ? 'success' : 'error'"
                        :text="item.stockType === 1 ? '進貨' : '退貨'" label size="small" class="font-weight-bold"></v-chip>
                </template>

                <!-- 金額格式化 -->
                <template v-slot:item.totalAmount="{ item }">
                    <span class="font-weight-bold" :class="item.stockType === 1 ? 'text-success' : 'text-error'">
                        {{ formatCurrency(item.totalAmount) }}
                    </span>
                </template>

                <!-- 操作按鈕 -->
                <!-- 明細按鈕 -->
                <template v-slot:item.actions="{ item }">
                    <v-btn icon="mdi-eye" size="small" variant="text" color="primary" title="查看詳情"
                        @click="openDetail(item.logId)">
                    </v-btn>
                </template>

                <!-- 退貨按鈕 (獨立一欄) -->
                <template v-slot:item.actions_return="{ item }">
                    <v-btn v-if="item.stockType === 1" color="error" size="small" variant="tonal"
                        @click="handleReturn(item)">
                        退貨
                    </v-btn>
                    <span v-else class="text-grey text-caption">已退貨</span>
                </template>

                <!-- 修改按鈕 (獨立一欄) -->
                <template v-slot:item.actions_edit="{ item }">
                    <v-btn color="primary" size="small" elevation="1" @click="editLog(item.logId)">
                        修改
                    </v-btn>
                </template>

                <!-- 無資料顯示 -->
                <template v-slot:no-data>
                    <div class="d-flex flex-column align-center pa-8 text-grey">
                        <v-icon icon="mdi-file-document-outline" size="64" class="mb-4"></v-icon>
                        <div class="text-h6">目前沒有進退貨紀錄</div>
                    </div>
                </template>

            </v-data-table>
        </v-card>
    </div>
</template>

<style scoped>
.v-card {
    border-top: 4px solid #2E5C43;
}

.log-table :deep(.v-data-table-header__content) {
    font-weight: bold;
    color: #2E5C43;
}
</style>
