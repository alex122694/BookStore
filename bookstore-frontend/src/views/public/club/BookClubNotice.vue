<script setup>
import { ref } from 'vue';

const showNoticeModal = ref(false);
</script>

<template>
    <div class="d-inline-block">
        <!-- 資訊按鈕 -->
        <v-btn color="white" variant="elevated" class="ml-2 notice-btn" size="small" icon
            @click="showNoticeModal = true">
            <v-icon color="primary" size="24">mdi-leaf</v-icon>
            <v-tooltip activator="parent" location="bottom">申請須知</v-tooltip>
        </v-btn>

        <!-- 注意事項彈出視窗 -->
        <v-dialog v-model="showNoticeModal" max-width="500">
            <v-card class="rounded-xl">
                <v-card-title class="bg-primary text-white py-4 d-flex align-center">
                    <v-icon icon="mdi-book-open-variant" class="mr-2"></v-icon>
                    讀書會申請注意事項
                </v-card-title>
                <v-card-text class="pa-6">
                    <h3 class="text-h6 font-weight-bold mb-4 text-primary">申請流程說明</h3>
                    <v-list density="comfortable">
                        <v-list-item prepend-icon="mdi-numeric-1-circle" class="px-0">
                            填寫活動資訊後，您可以選擇「儲存草稿」稍後再編輯。
                        </v-list-item>
                        <v-list-item prepend-icon="mdi-numeric-2-circle" class="px-0">
                            若確認資料無誤，請點擊「送出審核」。
                        </v-list-item>
                        <v-list-item prepend-icon="mdi-numeric-3-circle" class="px-0">
                            送出後將進入審核階段，審核期間無法修改資料。需約 3 個工作天。
                        </v-list-item>
                        <v-divider class="my-4"></v-divider>
                        <v-list-item prepend-icon="mdi-star-circle" color="orange" class="px-0">
                            每參與一場讀書會可獲得 <strong>100 點</strong>積分。
                        </v-list-item>
                        <v-list-item prepend-icon="mdi-shield-check" color="success" class="px-0">
                            需參與滿 <strong>5 場</strong>讀書會才可發起讀書會。
                        </v-list-item>
                    </v-list>
                </v-card-text>
                <v-card-actions class="pa-4 pt-0">
                    <v-spacer></v-spacer>
                    <v-btn color="primary" variant="flat" block rounded="lg" @click="showNoticeModal = false">
                        我瞭解了
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </div>
</template>

<style scoped>
.notice-btn {
    background-color: white !important;
    opacity: 1 !important;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1) !important;
}

.notice-btn :deep(.v-icon) {
    opacity: 1 !important;
    color: #2E5C43 !important;
}
</style>
