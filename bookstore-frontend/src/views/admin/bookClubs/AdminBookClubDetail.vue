<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import bookClubService from '@/api/bookClubService.js';
import Swal from 'sweetalert2';
import SmartInputButtons from '@/components/SmartInputButtons.vue';
import BackPageButton from '@/components/BackPageButton.vue';
import BookClubDetailCard from '@/components/BookClubDetailCard.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const club = ref({});

// Reject Dialog
const showRejectDialog = ref(false);
const rejectionReason = ref('');

const loadClub = async () => {
    const clubId = route.params.id;
    if (!clubId) return;

    try {
        const response = await bookClubService.getClub(clubId);
        club.value = response.data;
        if (!club.value.clubDetail) {
            club.value.clubDetail = {};
        }
    } catch (error) {
        console.error('Failed to load club details:', error);
        Swal.fire('錯誤', '無法載入讀書會資料', 'error');
        router.push('/dev/admin/bookclubs');
    } finally {
        loading.value = false;
    }
};

const handleApprove = async () => {
    const result = await Swal.fire({
        title: '確定核准？',
        text: "此讀書會將開放報名",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#2E5C43',
        confirmButtonText: '確定核准'
    });

    if (result.isConfirmed) {
        try {
            loading.value = true;
            await bookClubService.approveClub(club.value.clubId);
            await Swal.fire('成功', '讀書會已核准', 'success');
            loadClub(); // Reload to show new status
        } catch (error) {
            Swal.fire('錯誤', '核准失敗', 'error');
        } finally {
            loading.value = false;
        }
    }
};

const openRejectDialog = () => {
    rejectionReason.value = '';
    showRejectDialog.value = true;
};

const confirmReject = async () => {
    if (!rejectionReason.value.trim()) {
        Swal.fire('提示', '請填寫駁回原因', 'warning');
        return;
    }

    try {
        loading.value = true;
        await bookClubService.rejectClub(club.value.clubId, rejectionReason.value);
        showRejectDialog.value = false;
        await Swal.fire('已駁回', '讀書會已駁回', 'success');
        loadClub(); // Reload
    } catch (error) {
        Swal.fire('錯誤', '駁回失敗', 'error');
    } finally {
        loading.value = false;
    }
};

const fillRejectReason = () => {
    rejectionReason.value = `議程與宗旨說明過於模糊。
為了保障參與者的權益，請補充詳細的活動流程、討論主題及預期收穫後，再次送出申請。`;
};

const clearRejectReason = () => {
    rejectionReason.value = '';
};

onMounted(() => {
    loadClub();
});
</script>

<template>
    <v-container class="py-10">
        <BookClubDetailCard :club="club" :loading="loading" :is-admin="true">
            <!-- Back Button Slot -->
            <template #back-button>
                <BackPageButton />
            </template>

            <!-- Header Actions Slot (Approve/Reject) -->
            <template #header-actions>
                <div v-if="club.status === 0" class="d-flex gap-4 justify-center">
                    <v-btn color="error" variant="outlined" size="large" prepend-icon="mdi-close"
                        @click="openRejectDialog">
                        駁回申請
                    </v-btn>
                    <v-btn color="success" size="large" prepend-icon="mdi-check" @click="handleApprove" class="ml-4">
                        核准申請
                    </v-btn>
                </div>
            </template>

            <!-- Extra Alerts Slot (Rejection Reason) -->
            <template #extra-alerts>
                <div v-if="club.rejectionReason" class="mb-6">
                    <v-alert type="error" variant="tonal" border="start" title="駁回原因">
                        {{ club.rejectionReason }}
                    </v-alert>
                </div>
            </template>
        </BookClubDetailCard>

        <!-- Reject Dialog -->
        <v-dialog v-model="showRejectDialog" max-width="500px">
            <v-card>
                <v-card-title class="bg-error text-white">駁回申請</v-card-title>
                <v-card-text class="pt-4">
                    <v-textarea v-model="rejectionReason" label="請輸入駁回原因" hint="此原因將顯示給申請者" persistent-hint
                        variant="outlined" rows="3" auto-grow></v-textarea>

                    <SmartInputButtons :presets="[
                        { title: '一鍵輸入', handler: fillRejectReason, icon: 'mdi-text-box-remove-outline', color: 'green' }
                    ]" :showClear="true" :clearHandler="clearRejectReason" class="mt-2" />
                </v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="grey" variant="text" @click="showRejectDialog = false">取消</v-btn>
                    <v-btn color="error" @click="confirmReject">確認駁回</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-container>
</template>
