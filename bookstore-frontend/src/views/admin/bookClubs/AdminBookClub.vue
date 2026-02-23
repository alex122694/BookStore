<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import bookClubService from '@/api/bookClubService.js';
import Swal from 'sweetalert2';
import RegistrationDetails from '@/views/public/club/RegistrationDetails.vue';

const router = useRouter();
const clubs = ref([]);
const loading = ref(false);
const search = ref('');
const statusFilter = ref(null);

// Registration Details Modal Control
const showDetailsModal = ref(false);
const selectedClubId = ref(null);
const selectedClubEventDate = ref(null);

const openDetails = (club) => {
    selectedClubId.value = club.clubId;
    selectedClubEventDate.value = club.eventDate;
    showDetailsModal.value = true;
};

const statusOptions = [
    { title: '全部', value: null },
    { title: '報名中', value: 1 },
    { title: '已額滿', value: 3 },
    { title: '已截止', value: 4 },
    { title: '已結束', value: 5 },
    { title: '審核中', value: 0 },
    { title: '草稿', value: 7 },
    { title: '已駁回', value: 2 },
    { title: '已取消', value: 6 },
];

// 表格標頭定義
const headers = [
    { title: 'ID', key: 'clubId', sortable: true, align: 'start', width: '80px' },
    { title: '讀書會名稱', key: 'clubName', sortable: true, align: 'start' },
    { title: '主辦人', key: 'host', sortable: true, align: 'center' },
    // { title: '類型', key: 'categoriesBean', sortable: true, align: 'center' },
    { title: '目前/最大人數', key: 'participants', sortable: true, align: 'center' },
    { title: '狀態', key: 'status', sortable: true, align: 'center' },
    { title: '活動時間', key: 'eventDate', sortable: true, align: 'center' },
    // { title: '截止時間', key: 'deadline', sortable: true, align: 'center' },
    { title: '操作', key: 'actions', sortable: false, align: 'center' },
];

const statusMap = {
    0: { text: '審核中', color: 'orange' },
    1: { text: '報名中', color: 'success' },
    2: { text: '已駁回', color: 'error' },
    3: { text: '已額滿', color: 'purple' },
    4: { text: '已截止', color: 'grey' },
    5: { text: '已結束', color: 'brown' },
    6: { text: '已取消', color: 'grey-darken-1' }
};

const loadClubs = async () => {
    loading.value = true;
    try {
        const response = await bookClubService.getAllClubs();
        clubs.value = response.data;
    } catch (error) {
        console.error('讀取失敗:', error);
        Swal.fire('錯誤', '讀書會列表載入失敗', 'error');
    } finally {
        loading.value = false;
    }
};

const filteredClubs = computed(() => {
    let data = clubs.value;
    if (statusFilter.value !== null) {
        data = data.filter(item => item.status === statusFilter.value);
    }
    return data;
});

const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString();
};

// 審核與管理操作 (簡單實作：目前僅支援刪除，審核需實作完整 Update 邏輯，此處僅預留按鈕)
// 若需變更狀態，應呼叫 updateClub 帶入改變後的 status
const handleReview = async (item, newStatus) => {
    // 這裡需要注意：Service 的 updateClub 需要完整 Bean 與檔案 Part
    // 若後端不支援單純透過 API 修改狀態 (Patch)，則需要完整資料更新。
    // 在此我們先建立對話框確認，然後嘗試用原資料 + 新狀態回傳。

    // 實際上 Admin 若要審核，通常只需改 Status
    // 但 BookClubService.updateBookclub 會驗證欄位。
    // 假設我們將現有 item 修改 status 後回傳 (不帶檔案)

    const statusText = statusMap[newStatus]?.text || '未知狀態';

    const result = await Swal.fire({
        title: `確定變更為${statusText}？`,
        text: "此操作將更新讀書會狀態",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#2E5C43',
        cancelButtonColor: '#d33',
        confirmButtonText: '確定',
        cancelButtonText: '取消'
    });

    if (result.isConfirmed) {
        try {
            // 深拷貝物件以避免直接修改 UI
            const updateData = { ...item };
            updateData.status = newStatus;

            // 呼叫 Service (不帶檔案)
            await bookClubService.updateClub(item.clubId, updateData, null, null);

            Swal.fire('成功', `狀態已更新為${statusText}`, 'success');
            loadClubs(); // 重整列表
        } catch (error) {
            console.error(error);
            Swal.fire('錯誤', '狀態更新失敗', 'error');
        }
    }
};

const handleDelete = async (item) => {
    const result = await Swal.fire({
        title: '確定刪除？',
        text: "此操作無法復原！",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '是，刪除！',
        cancelButtonText: '取消'
    });

    if (result.isConfirmed) {
        try {
            await bookClubService.deleteClub(item.clubId);
            Swal.fire('已刪除', '讀書會資料已刪除', 'success');
            loadClubs();
        } catch (error) {
            console.error(error);
            Swal.fire('錯誤', '刪除失敗', 'error');
        }
    }
};

onMounted(() => {
    loadClubs();
});
</script>

<template>
    <div>
        <h2 class="text-h4 font-weight-bold text-primary mb-4">讀書會管理</h2>

        <v-row class="mb-4" align="center" justify="space-between">
            <v-col cols="12" md="3">
                <v-select v-model="statusFilter" :items="statusOptions" label="狀態篩選" variant="outlined"
                    density="compact" hide-details color="primary" class="bg-white rounded"></v-select>
            </v-col>
            <v-col cols="12" md="4">
                <v-text-field v-model="search" label="搜尋讀書會..." prepend-inner-icon="mdi-magnify" variant="outlined"
                    density="compact" hide-details color="primary" class="bg-white rounded"></v-text-field>
            </v-col>
        </v-row>

        <v-card class="rounded-lg elevation-2 border-t-4 border-primary">
            <v-data-table :headers="headers" :items="filteredClubs" :loading="loading" :search="search"
                class="forest-table" item-value="clubId" hover>

                <!-- 主辦人 -->
                <template v-slot:item.host="{ item }">
                    <div v-if="item.host">
                        <v-icon icon="mdi-account" size="small" class="mr-1"></v-icon>
                        {{ item.host.userName }} ({{ item.host.userId }})
                    </div>
                    <span v-else>-</span>
                </template>

                <!-- 人數 -->
                <template v-slot:item.participants="{ item }">
                    {{ item.currentParticipants || 0 }} / {{ item.maxParticipants }}
                </template>

                <!-- 狀態 -->
                <template v-slot:item.status="{ item }">
                    <v-chip :color="statusMap[item.status]?.color" size="small" class="font-weight-bold">
                        {{ statusMap[item.status]?.text || '未知' }}
                    </v-chip>
                </template>

                <!-- 日期 -->
                <template v-slot:item.eventDate="{ item }">
                    {{ formatDate(item.eventDate) }}
                </template>

                <!-- 操作 -->
                <template v-slot:item.actions="{ item }">
                    <!-- 審核按鈕 (僅在審核中狀態顯示) -->
                    <div v-if="item.status === 0" class="d-flex justify-center">
                        <v-btn color="primary" size="small" variant="elevated"
                            @click="router.push({ name: 'admin-bookclubs-review', params: { id: item.clubId } })">
                            <v-icon start icon="mdi-file-document-edit"></v-icon> 審核
                        </v-btn>
                    </div>
                    <!-- 其他狀態顯示檢視與刪除 -->
                    <div v-else class="d-flex justify-center align-center">
                        <v-btn color="primary" variant="text" size="small" icon="mdi-clipboard-account" class="mr-2"
                            @click="openDetails(item)" title="報名明細"></v-btn>
                        <v-btn color="info" variant="text" size="small" icon="mdi-eye" class="mr-2"
                            @click="router.push({ name: 'admin-bookclubs-review', params: { id: item.clubId } })"
                            title="詳覽/審核"></v-btn>
                        <v-btn color="grey" variant="text" size="small" icon="mdi-delete" @click="handleDelete(item)"
                            title="刪除"></v-btn>
                    </div>
                </template>

            </v-data-table>
        </v-card>

        <!-- Registration Details Modal -->
        <RegistrationDetails v-if="showDetailsModal" v-model="showDetailsModal" :club-id="selectedClubId"
            :event-date="selectedClubEventDate" />
    </div>
</template>

<style scoped lang="scss">
:deep(.v-data-table-header) {
    background-color: #F9FBE7 !important;
}

:deep(.v-data-table-header__content) {
    font-weight: bold;
    color: #2E5C43;
    font-size: 1rem;
}

:deep(.v-data-table__tr:nth-child(even)) {
    background-color: #FAFAFA;
}

:deep(.v-data-table__tr:hover) {
    background-color: #F1F8E9 !important;
}
</style>
