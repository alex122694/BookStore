<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import bookClubService from '@/modules/bookclub/api';
import { useUserStore } from '@/modules/user/store/userStore';
import { useLoginCheck } from '@/common/hooks/useLoginCheck';
import Swal from 'sweetalert2';
// Import Registration Details Component
import RegistrationDetails from './RegistrationDetails.vue';
import ActionPageButton from '@/common/components/ActionPageButton.vue';
import BookClubNotice from './BookClubNotice.vue';

// Import Extracted Config
import { headers, statusMap, statusOptions } from './UserBookClubConfig.js';

const router = useRouter();
const userStore = useUserStore();
const { validateLogin } = useLoginCheck();
const tab = ref('all'); // all | hosted | participated
const loading = ref(false);
const allClubs = ref([]); // 所有讀書會
const hostedClubs = ref([]); // 我發起的
const participatedClubs = ref([]); // 我參加的
const myRegistrationIds = ref(new Set()); // 我報名的讀書會ID集合
const search = ref('');

// Modal Control
const showDetailsModal = ref(false);
const selectedClubId = ref(null);
const selectedClubEventDate = ref(null);

const statusFilter = ref(null);

// Helper: Check if user is the host
const isHost = (club) => {
    // 使用 == 寬鬆檢查以防 ID 型別差異 (String vs Number)
    return userStore.userId && club.host && club.host.userId == userStore.userId;
};

// 載入我參加的讀書會 (用於判斷狀態)
const loadMyRegistrations = async () => {
    if (!userStore.isLoggedIn) return;
    try {
        const response = await bookClubService.getMyRegistrations();
        if (response.data) {
            // Backend returns List<ClubRegistrationsBean>
            // Filter: 1=報名中, 3=已額滿, 4=已截止, 5=已結束
            const activeRegs = response.data.filter(r => [1, 3, 4, 5].includes(r.status));
            participatedClubs.value = activeRegs.map(r => {
                // Enhance club object with registration info if needed
                return { ...r.bookClub, regStatus: r.status };
            });

            // Update Set for O(1) lookup
            myRegistrationIds.value = new Set(activeRegs.map(r => r.bookClub.clubId));
        }
    } catch (error) {
        console.error('載入報名資料失敗', error);
    }
};

// 載入所有讀書會
const loadAllClubs = async () => {
    loading.value = true;
    try {
        // 先載入報名狀態
        await loadMyRegistrations();

        const response = await bookClubService.getAllClubs();
        // 只顯示狀態為 1(報名中), 3(已額滿), 4(已截止) 的讀書會
        allClubs.value = response.data.filter(club => [1, 3, 4, 5].includes(club.status));
    } catch (error) {
        console.error('載入所有讀書會失敗', error);
        Swal.fire('錯誤', '無法載入所有讀書會資料', 'error');
    } finally {
        loading.value = false;
    }
};

// 載入我發起的讀書會
const loadHostedClubs = async () => {
    loading.value = true;
    try {
        const response = await bookClubService.getMyHostedClubs();
        hostedClubs.value = response.data;
    } catch (error) {
        console.error('載入我發起的讀書會失敗', error);
    } finally {
        loading.value = false;
    }
};

// 報名動作
const handleRegister = async (club) => {
    // Check if user is logged in
    if (!await validateLogin('您必須先登入帳號才能報名參加讀書會。')) return;

    Swal.fire({
        title: '確認報名',
        text: `確定要報名「${club.clubName}」嗎？`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '確定報名',
        cancelButtonText: '取消',
        confirmButtonColor: '#2E5C43'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await bookClubService.register(club.clubId);
                Swal.fire('成功', '報名成功！', 'success');
                // Refresh data
                await loadAllClubs();
            } catch (error) {
                Swal.fire('失敗', error.response?.data?.message || '報名失敗', 'error');
            }
        }
    });
};

// 取消報名動作
const handleCancel = (club) => {
    Swal.fire({
        title: '取消報名',
        text: `確定要取消「${club.clubName}」的報名嗎？`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確定取消',
        cancelButtonText: '保留',
        confirmButtonColor: '#d33'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await bookClubService.cancelRegistration(club.clubId);
                Swal.fire('成功', '已取消報名', 'success');
                // Refresh data
                await loadAllClubs();
            } catch (error) {
                Swal.fire('失敗', error.response?.data?.message || '取消失敗', 'error');
            }
        }
    });
};

// 發起人取消讀書會
const handleClubCancel = (club) => {
    Swal.fire({
        title: '取消讀書會',
        text: `確定要取消讀書會「${club.clubName}」嗎？取消後將無法復原及修改！`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確定取消',
        cancelButtonText: '保留',
        confirmButtonColor: '#d33'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await bookClubService.cancelClub(club.clubId);
                Swal.fire('成功', '讀書會已取消', 'success');
                // Refresh data
                if (tab.value === 'hosted') {
                    await loadHostedClubs();
                } else {
                    await loadAllClubs();
                }
            } catch (error) {
                Swal.fire('失敗', error.response?.data?.message || '取消失敗', 'error');
            }
        }
    });
};

// 發起人結束讀書會
const handleClubEnd = (club) => {
    Swal.fire({
        title: '結束讀書會',
        text: `確定要結束讀書會「${club.clubName}」嗎？結束後將結算積分給參與者！`,
        icon: 'info',
        showCancelButton: true,
        confirmButtonText: '確定結束',
        cancelButtonText: '取消',
        confirmButtonColor: '#2E5C43'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await bookClubService.endClub(club.clubId);
                Swal.fire('成功', '讀書會已結束，並已發放積分！', 'success');
                // Refresh data
                if (tab.value === 'hosted') {
                    await loadHostedClubs();
                } else {
                    await loadAllClubs();
                }
            } catch (error) {
                Swal.fire('失敗', error.response?.data?.message || '活動尚未開始不可結束!', 'error');
            }
        }
    });
};



// 發起人檢視明細
const openDetails = (club) => {
    selectedClubId.value = club.clubId;
    selectedClubEventDate.value = club.eventDate;
    showDetailsModal.value = true;
};

// 一般使用者查看詳細資訊
const viewClubInfo = (club) => {
    router.push({
        name: 'user-bookclub-detail-page',
        params: { id: club.clubId }
    });
};

// 根據目前 Tab 決定顯示的資料
const currentClubs = computed(() => {
    if (tab.value === 'hosted') return hostedClubs.value;
    if (tab.value === 'participated') return participatedClubs.value;
    return allClubs.value;
});

const filteredClubs = computed(() => {
    let data = currentClubs.value;
    if (statusFilter.value !== null) {
        data = data.filter(item => item.status === statusFilter.value);
    }
    return data;
});

const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString();
};

const isEventEnded = (club) => {
    // 檢查狀態是否已截止(4)、已結束(5) 或已取消(6)
    if ([4, 5, 6].includes(club.status)) return true;

    // 檢查時間是否已過
    if (club.eventDate) {
        return new Date(club.eventDate) < new Date();
    }
    return false;
};

const navigateToInsert = async () => {
    // Check if user is logged in
    if (!await validateLogin('您必須先登入帳號才能發起讀書會。')) return;

    router.push('/dev/user/bookclubs/insert');
};

// 監聽 Tab 切換以載入對應資料
watch(tab, (newVal) => {
    if (newVal === 'all') {
        if (allClubs.value.length === 0 || myRegistrationIds.value.size === 0) loadAllClubs();
    } else if (newVal === 'hosted') {
        if (hostedClubs.value.length === 0) loadHostedClubs();
    } else if (newVal === 'participated') {
        loadMyRegistrations();
    }
});

// 監聽 User ID 變化 (防止重新整理時 Pinia 還沒載入)
watch(() => userStore.userId, (newVal) => {
    if (newVal) {
        if (tab.value === 'all') loadAllClubs();
        else if (tab.value === 'hosted') loadHostedClubs();
        else if (tab.value === 'participated') loadMyRegistrations();
    }
});


const getDisplayStatus = (item) => {
    if (item.status === 1 && item.currentParticipants >= item.maxParticipants) {
        return statusMap[3];
    }
    return statusMap[item.status] || { text: '未知', color: 'grey' };
};

onMounted(() => {
    // 預設載入目前 Tab 的資料
    if (tab.value === 'all') loadAllClubs();
    else if (tab.value === 'hosted') loadHostedClubs();
    else if (tab.value === 'participated') loadMyRegistrations();
});
</script>

<template>
    <v-container class="py-10" style="max-width: 1200px;">
        <!-- Hero Section -->
        <div class="club-hero">
            <div class="hero-overlay"></div>
            <div class="hero-content mx-4">
                <h1 class="hero-title">🌲森林讀書會📚</h1>
                <h2 class="text-h5 mb-6 opacity-80">在閱讀中相遇，在交流中成長</h2>
                <p class="hero-description">
                    專屬愛書人的交流園地！我們相信，閱讀是獨處的快樂，而分享能讓思想碰撞出更多火花。
                    歡迎您發起共讀，尋找志同道合的書友，或只是想聆聽不同的觀點，這裡都是您的最佳起點。
                    擇您所愛，一起探索書中的無限可能。
                </p>
                <div class="d-flex justify-center align-center">
                    <ActionPageButton color="white" :icon="null" rounded="pill" class="text-primary elevation-4"
                        @click="navigateToInsert">
                        發起讀書會
                    </ActionPageButton>
                    <BookClubNotice />
                </div>
            </div>
        </div>


        <v-card class="rounded-lg elevation-2">
            <v-tabs v-model="tab" color="primary" align-tabs="start">
                <v-tab value="all">所有讀書會</v-tab>
                <v-tab value="hosted">我發起的</v-tab>
                <v-tab value="participated">我參加的</v-tab>
            </v-tabs>

            <v-card-text>
                <!-- 共用篩選與搜尋列 -->
                <v-row class="mb-4">
                    <v-col cols="12" sm="4">
                        <v-select v-model="statusFilter" :items="statusOptions" label="狀態篩選" variant="outlined"
                            density="compact" hide-details></v-select>
                    </v-col>
                    <v-col cols="12" sm="4" offset-sm="4">
                        <v-text-field v-model="search" label="搜尋讀書會" prepend-inner-icon="mdi-magnify" variant="outlined"
                            density="compact" hide-details></v-text-field>
                    </v-col>
                </v-row>

                <v-window v-model="tab">
                    <!-- 所有讀書會 -->
                    <v-window-item value="all">
                        <div class="club-table-container">
                            <v-data-table :headers="headers" :items="filteredClubs" :loading="loading" :search="search"
                                class="forest-table club-data-table" hover>
                                <template v-slot:item.eventDate="{ item }">
                                    {{ formatDate(item.eventDate) }}
                                </template>
                                <template v-slot:item.status="{ item }">
                                    <v-chip :color="getDisplayStatus(item).color" size="small"
                                        class="font-weight-bold status-chip-text">
                                        {{ getDisplayStatus(item).text }}
                                    </v-chip>
                                </template>
                                <template v-slot:item.participants="{ item }">
                                    {{ item.currentParticipants || 0 }} / {{ item.maxParticipants }}
                                </template>
                                <template v-slot:item.details="{ item }">
                                    <div class="d-flex flex-column align-center py-2">
                                        <v-btn icon="mdi-eye" size="large" variant="text" color="primary"
                                            @click="viewClubInfo(item)"></v-btn>
                                        <div class="ai-comment-placeholder mt-1">
                                        </div>
                                    </div>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <!-- 
                                    1. 如果是 Host -> 顯示管理 (或跳轉)
                                    2. 如果已報名 -> 顯示已報名/取消 (Danger Color)
                                    3. 如果未報名 -> 顯示報名 (Primary Color)
                                -->
                                    <template v-if="isHost(item)">
                                        <v-chip color="info" size="small" variant="outlined" class="mr-2">我是發起人</v-chip>
                                        <v-btn size="small" variant="text" color="primary" @click="openDetails(item)">
                                            <v-icon start icon="mdi-clipboard-list"></v-icon>
                                            報名明細
                                        </v-btn>
                                    </template>
                                    <template v-else-if="myRegistrationIds.has(item.clubId)">
                                        <!-- 僅在活動尚未結束且未取消時顯示取消報名 -->
                                        <v-btn v-if="!isEventEnded(item)" size="small" color="error" variant="flat"
                                            @click="handleCancel(item)">
                                            取消報名
                                        </v-btn>
                                    </template>
                                    <template v-else>
                                        <!-- 僅限在活動尚未結束且狀態為報名中(1)或已額滿(3)時，才顯示報名按鈕 -->
                                        <v-btn v-if="!isEventEnded(item) && [1, 3].includes(item.status)" size="small"
                                            color="primary" variant="elevated" @click="handleRegister(item)"
                                            :disabled="item.status === 3 || item.currentParticipants >= item.maxParticipants">
                                            {{ (item.status === 3 || item.currentParticipants >= item.maxParticipants) ?
                                                '已額滿' : '報名' }}
                                        </v-btn>
                                    </template>
                                </template>
                                <template v-slot:no-data>
                                    <div class="text-center py-4 text-grey">目前沒有可報名的讀書會</div>
                                </template>
                            </v-data-table>
                        </div>
                    </v-window-item>

                    <!-- 我發起的 -->
                    <v-window-item value="hosted">
                        <div class="club-table-container">
                            <v-data-table :headers="headers" :items="filteredClubs" :loading="loading" :search="search"
                                class="forest-table club-data-table" hover>
                                <template v-slot:item.eventDate="{ item }">
                                    {{ formatDate(item.eventDate) }}
                                </template>
                                <template v-slot:item.status="{ item }">
                                    <v-chip :color="getDisplayStatus(item).color" size="small"
                                        class="font-weight-bold status-chip-text">
                                        {{ getDisplayStatus(item).text }}
                                    </v-chip>
                                </template>
                                <template v-slot:item.participants="{ item }">
                                    {{ item.currentParticipants || 0 }} / {{ item.maxParticipants }}
                                </template>
                                <template v-slot:item.details="{ item }">
                                    <div class="d-flex flex-column align-center py-2">
                                        <v-btn icon="mdi-eye" size="large" variant="text" color="primary"
                                            @click="viewClubInfo(item)"></v-btn>
                                        <div class="ai-comment-placeholder mt-1">
                                        </div>
                                    </div>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <template v-if="item.status === 7 || item.status === 2">
                                        <v-btn size="small" variant="text" color="success" class="mr-2"
                                            @click="router.push({ path: '/dev/user/bookclubs/insert', query: { id: item.clubId } })">
                                            <v-icon start icon="mdi-pencil"></v-icon>
                                            編輯
                                        </v-btn>
                                    </template>
                                    <template
                                        v-if="item.status === 1 || item.status === 3 || item.status === 4 || item.status === 5">
                                        <v-btn size="small" variant="text" color="primary" @click="openDetails(item)">
                                            <v-icon start icon="mdi-clipboard-list"></v-icon>
                                            報名明細
                                        </v-btn>
                                    </template>
                                    <!-- 僅允許狀態 1(報名中)、3(已額滿)、4(已截止) 進行取消 -->
                                    <template v-if="item.status === 1 || item.status === 3 || item.status === 4">
                                        <v-btn size="small" variant="text" color="error"
                                            @click="handleClubCancel(item)">
                                            <v-icon start icon="mdi-cancel"></v-icon>
                                            取消
                                        </v-btn>
                                    </template>
                                    <!-- 允許狀態 1(報名中)、3(已額滿)、4(已截止) 且 活動時間已過 進行結束 -->
                                    <template
                                        v-if="(item.status === 1 || item.status === 3 || item.status === 4) && isEventEnded(item)">
                                        <v-btn size="small" variant="text" color="orange" @click="handleClubEnd(item)">
                                            <v-icon start icon="mdi-check-circle-outline"></v-icon>
                                            結束活動
                                        </v-btn>
                                    </template>
                                </template>
                                <template v-slot:no-data>
                                    <div class="text-center py-4 text-grey">目前沒有發起的讀書會</div>
                                </template>
                            </v-data-table>
                        </div>
                    </v-window-item>

                    <!-- 我參加的 -->
                    <v-window-item value="participated">
                        <div class="club-table-container">
                            <v-data-table :headers="headers" :items="filteredClubs" :loading="loading" :search="search"
                                class="forest-table club-data-table" hover>
                                <template v-slot:item.eventDate="{ item }">
                                    {{ formatDate(item.eventDate) }}
                                </template>
                                <template v-slot:item.status="{ item }">
                                    <v-chip :color="getDisplayStatus(item).color" size="small"
                                        class="font-weight-bold status-chip-text">
                                        {{ getDisplayStatus(item).text }}
                                    </v-chip>
                                </template>
                                <template v-slot:item.participants="{ item }">
                                    {{ item.currentParticipants || 0 }} / {{ item.maxParticipants }}
                                </template>
                                <template v-slot:item.details="{ item }">
                                    <div class="d-flex flex-column align-center py-2">
                                        <v-btn icon="mdi-eye" size="large" variant="text" color="primary"
                                            @click="viewClubInfo(item)"></v-btn>
                                        <div class="ai-comment-placeholder mt-1">

                                        </div>
                                    </div>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <v-btn v-if="!isEventEnded(item)" size="small" color="error" variant="text"
                                        @click="handleCancel(item)">
                                        取消報名
                                    </v-btn>
                                </template>
                                <template v-slot:no-data>
                                    <div class="text-center py-10 text-grey">
                                        <v-icon icon="mdi-account-group-outline" size="64"
                                            class="mb-4 opacity-50"></v-icon>
                                        <h3 class="text-h6">目前尚無參與紀錄</h3>
                                        <p>快去參加感興趣的讀書會吧！</p>
                                    </div>
                                </template>
                            </v-data-table>
                        </div>
                    </v-window-item>
                </v-window>
            </v-card-text>
        </v-card>

        <!-- Registration Details Modal -->
        <RegistrationDetails v-if="showDetailsModal" v-model="showDetailsModal" :club-id="selectedClubId"
            :event-date="selectedClubEventDate" />

    </v-container>
</template>

<style src="./UserBookClub.css" scoped></style>
