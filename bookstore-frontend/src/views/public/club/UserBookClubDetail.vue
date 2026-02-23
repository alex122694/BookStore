<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import bookClubService from '@/api/bookClubService.js';
import Swal from 'sweetalert2';
import BackPageButton from '@/components/BackPageButton.vue';
import BookClubDetailCard from '@/components/BookClubDetailCard.vue';

import { useUserStore } from '@/stores/userStore';
import { useLoginCheck } from '@/composables/useLoginCheck';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const { validateLogin } = useLoginCheck();
const loading = ref(true);
const club = ref({});

// Helper: Check if user is the host
const isHost = computed(() => {
    return userStore.userId && club.value.host && club.value.host.userId == userStore.userId;
});

// Helper: Check if user has registered
const isRegistered = computed(() => {
    return myRegistrationStatus.value === 1;
});

const isEventEnded = computed(() => {
    // 檢查狀態是否已截止(4)、已結束(5) 或已取消(6)
    if ([4, 5, 6].includes(club.value.status)) return true;

    // 檢查時間是否已過
    if (club.value.eventDate) {
        return new Date(club.value.eventDate) < new Date();
    }
    return false;
});

const myRegistrationStatus = ref(null);

const loadMyRegistrationStatus = async () => {
    if (!userStore.isLoggedIn) return;
    try {
        const response = await bookClubService.getMyRegistrations();
        const myReg = response.data.find(r => r.bookClub.clubId === club.value.clubId && r.status === 1);
        if (myReg) {
            myRegistrationStatus.value = 1;
        } else {
            myRegistrationStatus.value = null;
        }
    } catch (e) {
        console.error('Failed to load my registration', e);
    }
}

const handleRegister = async () => {
    if (!await validateLogin('您必須先登入帳號才能報名參加讀書會。')) return;

    Swal.fire({
        title: '確認報名',
        text: `確定要報名「${club.value.clubName}」嗎？`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '確定報名',
        cancelButtonText: '取消',
        confirmButtonColor: '#2E5C43'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await bookClubService.register(club.value.clubId);
                Swal.fire('成功', '報名成功！', 'success');
                loadMyRegistrationStatus();
                // We might want to reload club to update participant count
                loadClub();
            } catch (error) {
                Swal.fire('失敗', error.response?.data?.message || '報名失敗', 'error');
            }
        }
    });
};

const handleCancel = () => {
    Swal.fire({
        title: '取消報名',
        text: `確定要取消「${club.value.clubName}」的報名嗎？`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確定取消',
        cancelButtonText: '保留',
        confirmButtonColor: '#d33'
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await bookClubService.cancelRegistration(club.value.clubId);
                Swal.fire('成功', '已取消報名', 'success');
                myRegistrationStatus.value = null;
                loadClub();
            } catch (error) {
                Swal.fire('失敗', error.response?.data?.message || '取消失敗', 'error');
            }
        }
    });
};

const loadClub = async () => {
    const clubId = route.params.id;
    if (!clubId) return;
    try {
        const response = await bookClubService.getClub(clubId);
        club.value = response.data;
        if (!club.value.clubDetail) {
            club.value.clubDetail = {};
        }
        await loadMyRegistrationStatus();
    } catch (error) {
        // Error handling
    }
};

onMounted(async () => {
    const clubId = route.params.id;
    if (!clubId) {
        Swal.fire('錯誤', '無效的讀書會ID', 'error');
        router.push('/dev/user/bookclubs');
        return;
    }

    try {
        await loadClub();
    } catch (error) {
        console.error('Failed to load club details:', error);
        Swal.fire('錯誤', '無法載入讀書會資料', 'error');
        router.push('/dev/user/bookclubs');
    } finally {
        loading.value = false;
    }
});
</script>

<template>
    <v-container class="py-10">
        <BookClubDetailCard :club="club" :loading="loading" :isAdmin="isHost">
            <template #back-button>
                <BackPageButton />
            </template>
            <template #header-actions>
                <template v-if="isHost">
                    <v-chip color="info" variant="outlined">我是發起人</v-chip>
                </template>
                <template v-else-if="myRegistrationStatus === 1">
                    <v-btn v-if="!isEventEnded" color="error" variant="flat" @click="handleCancel">取消報名</v-btn>
                </template>
                <template v-else>
                    <v-btn v-if="!isEventEnded" color="primary" variant="elevated" @click="handleRegister"
                        :disabled="club.status === 3 || club.currentParticipants >= club.maxParticipants">
                        {{ (club.status === 3 || club.currentParticipants >= club.maxParticipants) ? '已額滿' : '報名參加' }}
                    </v-btn>
                </template>
            </template>
        </BookClubDetailCard>
    </v-container>
</template>
