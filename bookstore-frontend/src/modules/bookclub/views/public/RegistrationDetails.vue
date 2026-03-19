<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import bookClubService from '@/modules/bookclub/api';
import Swal from 'sweetalert2';

const props = defineProps({
    modelValue: Boolean,
    clubId: Number,
    eventDate: String
});

const emit = defineEmits(['update:modelValue']);

// Local visibility state synchronized with prop
const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
});

// Check if current time is after event start time
const canCheckIn = computed(() => {
    if (!props.eventDate) return false;
    return new Date() >= new Date(props.eventDate);
});

const loading = ref(false);
const registrations = ref([]);
const search = ref('');

const headers = [
    { title: '會員名稱', key: 'user.userName', sortable: true },
    { title: 'Email', key: 'user.email', sortable: true },
    { title:'電話', key: 'user.phoneNum', sortable: true },
    { title: '報名時間', key: 'registeredAt', sortable: true },
    { title: '狀態', key: 'status', sortable: true },
    { title: '報到', key: 'checkIn', sortable: true },
    { title: '操作', key: 'actions', sortable: false },
];

const loadRegistrations = async () => {
    if (!props.clubId) return;
    loading.value = true;
    loading.value = true;
    try {
        console.log('[RegistrationDetails] Requesting for ClubId:', props.clubId);
        const response = await bookClubService.getClubRegistrations(props.clubId);
        console.log('[RegistrationDetails] API Response:', response.data);
        registrations.value = response.data || [];
    } catch (error) {
        console.error('無法載入報名明細', error);
        Swal.fire('錯誤', '無法載入報名明細', 'error');
    } finally {
        loading.value = false;
    }
};

const handleCheckIn = async (reg) => {
    if (reg.checkIn) return; // 已經報到

    try {
        await bookClubService.checkInUser(props.clubId, reg.user.userId);
        Swal.fire('成功', `會員 ${reg.user.userName} 報到成功！`, 'success');
        // Update local state without reload
        reg.checkIn = true;
    } catch (error) {
        Swal.fire('失敗', error.response?.data?.message || '報到失敗', 'error');
    }
};

const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString();
};

const getStatusText = (status) => {
    // 0: 審核中, 1: 報名成功 / 已報名, 2: 已取消
    const map = {
        0: '審核中',
        1: '已報名',
        2: '已取消'
    };
    return map[status] || '未知';
};

const getStatusColor = (status) => {
    const map = {
        0: 'orange',
        1: 'success',
        2: 'grey'
    };
    return map[status] || 'grey';
};

// 監聽 Modal 開啟時載入資料
watch(() => props.modelValue, (newVal) => {
    if (newVal && props.clubId) {
        loadRegistrations();
    }
});

onMounted(() => {
    if (props.modelValue && props.clubId) {
        loadRegistrations();
    }
});
</script>

<template>
    <v-dialog v-model="visible" max-width="900px" scrollable>
        <v-card class="rounded-lg">
            <v-card-title class="d-flex justify-space-between align-center py-4 bg-primary text-white">
                <span class="text-h6 font-weight-bold">報名明細列表</span>
                <v-btn icon="mdi-close" variant="text" color="white" @click="visible = false"></v-btn>
            </v-card-title>

            <v-divider></v-divider>

            <v-card-text class="pa-4">
                <v-text-field v-model="search" label="搜尋報名者" prepend-inner-icon="mdi-magnify" variant="outlined"
                    density="compact" class="mb-4" hide-details></v-text-field>

                <v-data-table :headers="headers" :items="registrations" :loading="loading" :search="search"
                    class="forest-table" hover>
                    <template v-slot:item.registeredAt="{ item }">
                        {{ formatDate(item.registeredAt) }}
                    </template>

                    <template v-slot:item.status="{ item }">
                        <v-chip :color="getStatusColor(item.status)" size="small">
                            {{ getStatusText(item.status) }}
                        </v-chip>
                    </template>

                    <template v-slot:item.checkIn="{ item }">
                        <v-chip :color="item.checkIn ? 'success' : 'grey'" size="small" variant="flat">
                            {{ item.checkIn ? '已報到' : '未報到' }}
                        </v-chip>
                    </template>

                    <template v-slot:item.actions="{ item }">
                        <!-- 僅已報名且未取消者可報到 -->
                        <div v-if="item.status === 1">
                            <v-tooltip :text="!canCheckIn ? '活動尚未開始，不可報到' : ''" :disabled="canCheckIn" location="top">
                                <template v-slot:activator="{ props }">
                                    <div v-bind="props" class="d-inline-block">
                                        <v-btn size="small" :color="item.checkIn ? 'grey' : 'primary'"
                                            :disabled="item.checkIn || !canCheckIn" variant="elevated"
                                            @click="handleCheckIn(item)">
                                            {{ item.checkIn ? '已完成' : '報到' }}
                                        </v-btn>
                                    </div>
                                </template>
                            </v-tooltip>
                        </div>
                        <span v-else class="text-caption text-grey">該會員已取消報名</span>
                    </template>

                    <template v-slot:no-data>
                        <div class="text-center py-4 text-grey">目前沒有報名紀錄</div>
                    </template>
                </v-data-table>
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions class="pa-4">
                <v-spacer></v-spacer>
                <v-btn color="grey-darken-1" variant="text" @click="visible = false">關閉</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<style scoped>
.bg-primary {
    background-color: #2E5C43 !important;
}

.forest-table {
    --v-theme-primary: #2E5C43;
}
</style>
