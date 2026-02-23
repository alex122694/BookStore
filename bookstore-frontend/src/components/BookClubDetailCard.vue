<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
    club: {
        type: Object,
        required: true
    },
    isAdmin: {
        type: Boolean,
        default: false
    },
    loading: {
        type: Boolean,
        default: false
    }
});

const showProofDialog = ref(false);
const API_BASE_URL = 'http://localhost:8080';

const getFileUrl = (path) => {
    if (!path) return '#';
    if (path.startsWith('http')) return path;
    return `${API_BASE_URL}${path}`;
};

const getImageUrl = (path) => {
    if (!path) return '/default-book-cover.png';
    if (path.startsWith('http')) return path;
    if (path.startsWith('/')) return `${API_BASE_URL}${path}`;
    return `${API_BASE_URL}/upload-images/${path}`;
};

const formatDate = (dateStr) => {
    if (!dateStr) return '未定';
    return new Date(dateStr).toLocaleString();
};

// Status and Difficulty Maps (Copied to be self-contained or importable)
const statusMap = {
    0: { text: '審核中', color: 'orange' },
    1: { text: '報名中', color: 'green' },
    2: { text: '已駁回', color: 'red' },
    3: { text: '進行中', color: 'blue' },
    4: { text: '已截止', color: 'grey' },
    5: { text: '已結束', color: 'grey' },
    6: { text: '已取消', color: 'grey' },
    7: { text: '草稿中', color: 'grey' }
};

const getStatusInfo = (status) => {
    return statusMap[status] || { text: '未知', color: 'grey' };
};

const getDifficultyLabel = (level) => {
    const map = { 1: '樹苗級', 2: '樹幹級', 3: '巨木級(須提供專業相關證明)' };
    return map[level] || '一般';
};

const getDifficultyColor = (level) => {
    const map = { 1: 'green', 2: 'orange', 3: 'red' };
    return map[level] || 'grey';
};
</script>

<template>
    <div v-if="loading" class="d-flex justify-center my-10">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <template v-else>
        <!-- Header Section -->
        <div class="d-flex align-center mb-6">
            <slot name="back-button"></slot>
            <h1 class="text-h4 font-weight-bold text-primary ml-2">
                {{ club.clubName }}
            </h1>
        </div>

        <v-row>
            <!-- Left Column: Book Info & Visuals -->
            <v-col cols="12" md="4">
                <v-card class="rounded-lg elevation-2 h-100">
                    <v-img :src="getImageUrl(club.book?.bookImageBean?.imageUrl)" contain height="400"
                        class="bg-grey-lighten-2">
                        <template v-slot:placeholder>
                            <div class="d-flex align-center justify-center fill-height">
                                <v-icon icon="mdi-book-open-page-variant" size="64" color="grey"></v-icon>
                            </div>
                        </template>
                    </v-img>
                    <v-card-text>
                        <h3 class="text-h6 font-weight-bold mb-1">{{ club.book?.bookName || '自選書籍/未指定' }}</h3>

                        <!-- External Link -->
                        <div class="text-body-2 text-grey-darken-1 mb-3" v-if="club.externalBookInfo">
                            <v-icon icon="mdi-link" start size="small"></v-icon>
                            外部資訊: <a :href="club.externalBookInfo" target="_blank">{{ club.externalBookInfo }}</a>
                        </div>

                        <div class="text-subtitle-2 mb-2 mt-4">主辦會員</div>
                        <v-list-item class="px-0">
                            <template v-slot:prepend>
                                <v-avatar color="primary" size="40">
                                    <v-img v-if="club.host?.img" :src="getImageUrl(club.host?.img)" cover></v-img>
                                    <span v-else class="text-white">{{ club.host?.userName?.charAt(0) || 'U' }}</span>
                                </v-avatar>
                            </template>
                            <v-list-item-title class="font-weight-bold">
                                {{ club.host?.userName || '未知用戶' }}
                                <!-- Admin sees User ID perhaps? -->
                                <!-- <span v-if="isAdmin" class="text-caption text-grey ml-2">(ID: {{ club.host?.userId
                                    }})</span> -->
                            </v-list-item-title>
                            <v-list-item-subtitle>
                                <v-icon icon="mdi-email" size="x-small" class="mr-1"></v-icon>
                                {{ club.host?.email }}
                            </v-list-item-subtitle>

                            <!-- Admin Only: Phone Number -->
                            <v-list-item-subtitle>
                                <v-icon icon="mdi-phone" size="x-small" class="mr-1"></v-icon>
                                {{ club.host?.phoneNum }}
                            </v-list-item-subtitle>
                        </v-list-item>

                        <v-divider class="my-3"></v-divider>

                        <!-- Proof Button -->
                        <div v-if="club.clubDetail?.proofPath" class="mt-2">
                            <v-btn block color="info" variant="tonal" prepend-icon="mdi-file-document"
                                @click="showProofDialog = true">
                                查看佐證資料
                            </v-btn>
                        </div>

                        <v-divider class="my-3"></v-divider>

                        <div class="d-flex gap-2 mb-4">
                            <v-chip :color="getStatusInfo(club.status).color" class="flex-grow-1 justify-center" label>
                                {{ getStatusInfo(club.status).text }}
                            </v-chip>
                            <v-chip variant="outlined" color="secondary" class="flex-grow-1 justify-center">
                                {{ club.categoriesBean?.categoryName || '未分類' }}
                            </v-chip>
                        </div>

                        <div class="d-flex flex-column gap-2">
                            <slot name="header-actions"></slot>
                        </div>
                    </v-card-text>
                </v-card>
            </v-col>

            <!-- Right Column: Club Details -->
            <v-col cols="12" md="8">
                <v-card class="rounded-lg elevation-2 h-100">
                    <v-card-text class="pa-6">
                        <!-- Key Info Grid (Unified) -->
                        <v-row class="mb-6">
                            <v-col cols="12" sm="6">
                                <div class="d-flex align-start mb-4">
                                    <v-icon icon="mdi-calendar-clock" color="primary" class="mt-1 mr-3"
                                        size="large"></v-icon>
                                    <div>
                                        <div class="text-caption text-grey">活動時間</div>
                                        <div class="text-subtitle-1 font-weight-medium">
                                            {{ formatDate(club.eventDate) }}
                                        </div>
                                    </div>
                                </div>
                            </v-col>
                            <v-col cols="12" sm="6">
                                <div class="d-flex align-start mb-4">
                                    <v-icon icon="mdi-clock-alert" color="error" class="mt-1 mr-3"
                                        size="large"></v-icon>
                                    <div>
                                        <div class="text-caption text-grey">報名截止</div>
                                        <div class="text-subtitle-1 font-weight-medium">
                                            {{ formatDate(club.deadline) }}
                                        </div>
                                    </div>
                                </div>
                            </v-col>
                            <v-col cols="12" sm="6">
                                <div class="d-flex align-start mb-4">
                                    <v-icon icon="mdi-map-marker" color="primary" class="mt-1 mr-3"
                                        size="large"></v-icon>
                                    <div>
                                        <div class="text-caption text-grey">活動地點</div>
                                        <div class="text-subtitle-1 font-weight-medium">
                                            {{ club.location || '線上/未定' }}
                                        </div>
                                    </div>
                                </div>
                            </v-col>
                            <v-col cols="12" sm="6">
                                <div class="d-flex align-start mb-4">
                                    <v-icon icon="mdi-account-group" color="primary" class="mt-1 mr-3"
                                        size="large"></v-icon>
                                    <div>
                                        <div class="text-caption text-grey">人數限制</div>
                                        <div class="text-subtitle-1 font-weight-medium">
                                            {{ club.currentParticipants || 0 }} / {{ club.maxParticipants }} 人
                                        </div>
                                    </div>
                                </div>
                            </v-col>
                        </v-row>

                        <v-divider class="mb-6"></v-divider>

                        <!-- Rich Content -->
                        <div class="mb-6">
                            <h3 class="text-h6 font-weight-bold text-primary mb-3">
                                <v-icon icon="mdi-target" start></v-icon>
                                活動宗旨
                            </h3>
                            <div class="text-body-1 text-grey-darken-3 bg-grey-lighten-4 pa-4 rounded">
                                {{ club.clubDetail?.purpose || '暫無宗旨' }}
                            </div>
                        </div>

                        <div class="mb-6">
                            <h3 class="text-h6 font-weight-bold text-primary mb-3">
                                <v-icon icon="mdi-format-list-bulleted" start></v-icon>
                                活動議程
                            </h3>
                            <div class="text-body-1 text-pre-wrap">{{ club.clubDetail?.agenda || '暫無議程' }}</div>
                        </div>

                        <!-- Extra Alerts Slot (e.g. Rejection Reason) -->
                        <slot name="extra-alerts"></slot>

                        <v-row class="mt-2">
                            <v-col cols="12" md="6">
                                <v-alert icon="mdi-alert-circle-outline" border="start" border-color="primary"
                                    class="h-100">
                                    <div class="text-subtitle-2 font-weight-bold mb-1">參加條件</div>
                                    <div class="text-body-2">{{ club.clubDetail?.requirement || '無特殊限制' }}</div>
                                </v-alert>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-alert icon="mdi-stairs" :color="getDifficultyColor(club.clubDetail?.diffultLevel)"
                                    variant="tonal" class="h-100">
                                    <div class="text-subtitle-2 font-weight-bold mb-1">難度等級</div>
                                    <div class="text-body-2">
                                        {{ getDifficultyLabel(club.clubDetail?.diffultLevel) }}
                                    </div>
                                </v-alert>
                            </v-col>
                        </v-row>

                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </template>

    <!-- Proof Dialog -->
    <v-dialog v-model="showProofDialog" max-width="800px">
        <v-card>
            <v-card-title class="d-flex justify-space-between align-center">
                <span>佐證資料</span>
                <v-btn icon="mdi-close" variant="text" @click="showProofDialog = false"></v-btn>
            </v-card-title>
            <v-card-text class="pa-4 bg-grey-lighten-3 d-flex justify-center">
                <img :src="getFileUrl(club.clubDetail?.proofPath)" style="max-width: 100%; max-height: 80vh;" />
            </v-card-text>
        </v-card>
    </v-dialog>
</template>

<style scoped>
.text-pre-wrap {
    white-space: pre-wrap;
}

.gap-2 {
    gap: 8px;
}

.gap-4 {
    gap: 16px;
}
</style>
