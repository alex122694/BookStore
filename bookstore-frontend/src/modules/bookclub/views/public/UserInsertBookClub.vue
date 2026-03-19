<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import bookClubService from '@/modules/bookclub/api';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';
import SmartInputButtons from '@/common/components/SmartInputButtons.vue';
import BackPageButton from '@/common/components/BackPageButton.vue';

const router = useRouter();
const categories = ref([]);
const books = ref([]);
const loading = ref(false);
const termsDialog = ref(false);
const termsAgreed = ref(false);

const club = ref({
    clubName: '',
    categoryId: null,
    bookId: null,
    eventDate: '',
    deadline: '',
    location: '',
    maxParticipants: null,
    externalBookInfo: '',
    purpose: '',
    agenda: '',
    difficulty: 1, // Default to Entry Level
});

// const proposalFile = ref(null); // Removed
const proofFile = ref(null);

const rules = {
    required: value => !!value || '此欄位為必填',
    positive: value => value > 0 || '數值必須大於 0',
    dateFuture: value => {
        if (!value) return true;
        return new Date(value) > new Date() || '日期必須在未來';
    },
    deadlineBeforeEvent: () => {
        if (!club.value.eventDate || !club.value.deadline) return true;
        return new Date(club.value.deadline) < new Date(club.value.eventDate) || '截止日期必須在活動日期之前';
    },
    requiredIfExpert: value => {
        if (club.value.difficulty === 3) {
            return !!value || value?.length > 0 || '巨木級需上傳佐證資料';
        }
        return true;
    }
};

const form = ref(null);

const route = useRoute(); // Need to import useRoute
const isEditMode = computed(() => !!route.query.id); // Need computed

import { useUserStore } from '@/modules/user/store/userStore';
import { useLoginCheck } from '@/common/hooks/useLoginCheck';

const userStore = useUserStore();
const { validateLogin } = useLoginCheck();

// ...

onMounted(async () => {
    // Check login status first
    if (!await validateLogin('您必須先登入帳號才能發起讀書會。')) return;

    try {
        const [catResponse, bookResponse] = await Promise.all([
            bookClubService.getClubCategories(),
            bookService.getAllBooks()
        ]);
        categories.value = catResponse.data;
        books.value = bookResponse.data;

        // Check for edit mode
        if (route.query.id) {
            loading.value = true;
            const clubRes = await bookClubService.getClub(route.query.id);
            const data = clubRes.data;

            // Populate form
            club.value = {
                clubName: data.clubName,
                categoryId: data.categoriesBean?.categoryId,
                bookId: data.book?.bookId,
                eventDate: data.eventDate,
                deadline: data.deadline,
                location: data.location,
                maxParticipants: data.maxParticipants,
                externalBookInfo: data.externalBookInfo,

                // Map from clubDetail if available
                purpose: data.clubDetail?.purpose || '',
                agenda: data.clubDetail?.agenda || '',
                difficulty: data.clubDetail?.diffultLevel || 1, // Default to 1 (Entry)
            };

            // Map proof file path (for display/logic, though file input handles files)
            // Just knowing we have one might be enough, or pre-fill if possible (usually not for file inputs)
            // Ideally, we show "Current file: ..."
            // But for now, just mapping data is key.
            if (data.clubDetail?.proofPath) {
                // Store it somewhere if needed, but file-input requires File object.
                // We can't pre-fill file input. Maybe verify logic knows we have one?
                // For now, focus on text fields.
            }

            // Handle date format if needed (remove seconds)
            if (club.value.eventDate && club.value.eventDate.length > 16) club.value.eventDate = club.value.eventDate.substring(0, 16);
            if (club.value.deadline && club.value.deadline.length > 16) club.value.deadline = club.value.deadline.substring(0, 16);

            loading.value = false;
        }

    } catch (error) {
        console.error('Failed to load initial data:', error);
        Swal.fire('錯誤', '載入初始資料失敗', 'error');
    }
});

const submit = async (actionType) => {
    // 若是送審，需進行完整驗證
    if (actionType === 'SUBMIT') {
        const { valid } = await form.value.validate();
        if (!valid) return;

        if (!termsAgreed.value) {
            Swal.fire('提示', '請先詳閱並同意讀書會活動規範', 'warning');
            return;
        }
    } else {
        // 草稿模式：至少要有讀書會名稱
        if (!club.value.clubName) {
            Swal.fire('提示', '儲存草稿請至少填寫讀書會名稱', 'warning');
            return;
        }
    }

    loading.value = true;

    const clubData = {
        action: actionType, // 當前操作: DRAFT or SUBMIT
        clubName: club.value.clubName,
        location: club.value.location,
        eventDate: club.value.eventDate,
        deadline: club.value.deadline,
        maxParticipants: club.value.maxParticipants,
        externalBookInfo: club.value.externalBookInfo,
        purpose: club.value.purpose,
        agenda: club.value.agenda,
        difficulty: club.value.difficulty,
        categoriesBean: { categoryId: club.value.categoryId }, // 前端 DTO 結構需確認是否需調整，目前 Service 使用 DTO 接收 categoryId
        categoryId: club.value.categoryId, // Direct ID for DTO
        bookId: club.value.bookId, // Direct ID for DTO
    };

    try {
        if (isEditMode.value) {
            await bookClubService.updateClub(route.query.id, clubData, proofFile.value);
        } else {
            await bookClubService.createClub(clubData, proofFile.value);
        }

        const msgTitle = actionType === 'DRAFT' ? '草稿已儲存' : (isEditMode.value ? '更新成功！' : '申請成功！');
        const msgText = actionType === 'DRAFT'
            ? '您可以隨時至「我的讀書會」繼續編輯。'
            : '您的讀書會申請已送出，管理員審核通過後即可開始報名。';

        await Swal.fire({
            title: msgTitle,
            text: msgText,
            icon: 'success',
            confirmButtonColor: '#2E5C43'
        });

        router.push('/dev/user/bookclubs');
    } catch (error) {
        console.error(error);
        Swal.fire({
            icon: 'error',
            title: actionType === 'DRAFT' ? '儲存失敗' : '申請失敗',
            text: error.message || '發生錯誤，請稍後再試。',
            confirmButtonColor: '#d33'
        });
    } finally {
        loading.value = false;
    }
};

const getCalculatedDates = () => {
    const today = new Date();
    const eventDate = new Date(today);
    eventDate.setDate(today.getDate() + 30); // 30天後活動
    const deadline = new Date(today);
    deadline.setDate(today.getDate() + 25); // 25天後截止

    const formatDateTime = (date, timeStr = '14:00') => {
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}T${timeStr}`;
    };

    return {
        eventDate: formatDateTime(eventDate, '14:00'),
        deadline: formatDateTime(deadline, '23:59')
    };
};

const getRandomBook = () => {
    if (books.value.length === 0) return null;
    return books.value[Math.floor(Math.random() * books.value.length)];
};

const fillFirstReview = () => {
    const randomBook = getRandomBook();
    if (!randomBook) {
        Swal.fire('提示', '目前無可選書籍', 'info');
        return;
    }

    const dates = getCalculatedDates();

    club.value = {
        clubName: `輕鬆讀：巴菲特寫給股東的信`,
        categoryId: categories.value.length > 0 ? categories.value[0].categoryId : null,
        bookId: 17,
        eventDate: dates.eventDate,
        deadline: dates.deadline,
        location: '台北市信義區信義路五段7號 (101大樓 35F 會議室)',
        maxParticipants: 5,
        externalBookInfo: '',
        purpose: '大家一起輕鬆聊聊這本書，沒有壓力。',
        agenda: '1. 自我介紹\n2. 書籍分享\n3. 自由交流',
        difficulty: 1, // 入門
    };
    termsAgreed.value = true;
};

const fillSecondReview = () => {
    const randomBook = getRandomBook();
    if (!randomBook) {
        Swal.fire('提示', '目前無可選書籍', 'info');
        return;
    }

    const dates = getCalculatedDates();

    club.value = {
        clubName: `【深度解析】巴菲特寫給股東的信 - 專業導讀會`,
        categoryId: categories.value.length > 0 ? categories.value[0].categoryId : null,
        bookId: 17,
        eventDate: dates.eventDate,
        deadline: dates.deadline,
        location: '台北市信義區信義路五段7號 (101大樓 35F 會議室)',
        maxParticipants: 20,
        externalBookInfo: '',
        purpose: '本讀書會旨在匯聚行業菁英，透過深度閱讀與批判性思考，剖析書籍核心論點。我們將結合實際案例，探討書中理論在當代商業環境中的應用與挑戰，期能為參與者帶來實質的職涯啟發與專業成長。',
        agenda: `13:30 - 14:00 | 報到與聯誼 (Networking)
14:00 - 14:10 | 開場致詞與流程說明
14:10 - 15:00 | 深度導讀：核心章節解析 (由資深講師主講)
15:00 - 15:15 | 中場休息 (Tea Break)
15:15 - 16:15 | 分組專題研討 (Case Study Workshop)
16:15 - 16:45 | 各組成果發表與講評
16:45 - 17:00 | 結語與大合照`,
        difficulty: 3, // 專家
    };
    termsAgreed.value = true;
};

const clearForm = () => {
    club.value = {
        clubName: '',
        categoryId: null,
        bookId: null,
        eventDate: '',
        deadline: '',
        location: '',
        maxParticipants: null,
        externalBookInfo: '',
        purpose: '',
        agenda: '',
        difficulty: 1,
    };
    termsAgreed.value = false;
    proofFile.value = null; // Note: binding might not clear file input visually if not handling key or similar, but clears model
};
</script>

<template>
    <v-container class="py-10" style="max-width: 900px;">
        <div class="d-flex align-center mb-6">
            <BackPageButton />
            <h2 class="text-h4 font-weight-bold text-primary">{{ isEditMode ? '編輯讀書會' : '發起讀書會' }}</h2>
        </div>

        <div class="mb-6">
            <SmartInputButtons :presets="[
                { title: '初審 (一鍵輸入)', handler: fillFirstReview, icon: 'mdi-flash-outline', color: 'info' },
                { title: '二審 (專業詳細)', handler: fillSecondReview, icon: 'mdi-flash', color: 'success' }
            ]" :showClear="true" :clearHandler="clearForm" />
        </div>

        <v-card class="rounded-lg elevation-2 pa-6">
            <v-alert type="info" variant="tonal" class="mb-6" border="start">
                <div class="text-subtitle-1 font-weight-bold mb-1">申請流程說明</div>
                <div class="text-body-2">
                    1. 填寫活動資訊後，您可以選擇「<span class="font-weight-bold text-primary">儲存草稿</span>」稍後再編輯。<br>
                    2. 若確認資料無誤，請點擊「<span class="font-weight-bold text-primary">送出審核</span>」。<br>
                    3. 送出後將進入審核階段，審核期間無法修改資料。需約 3 個工作天。<br>
                    4. 每參與一場讀書會可獲得100點積分。<br>
                    5. 需參與5場讀書會才可發起讀書會。
                </div>
            </v-alert>

            <v-form ref="form">
                <v-row>
                    <v-col cols="12">
                        <v-text-field v-model="club.clubName" label="讀書會名稱" :rules="[rules.required]" variant="outlined"
                            color="primary"></v-text-field>
                    </v-col>

                    <v-col cols="12" md="6">
                        <v-select v-model="club.categoryId" :items="categories" item-title="categoryName"
                            item-value="categoryId" label="分類" :rules="[rules.required]" variant="outlined"
                            color="primary"></v-select>
                    </v-col>

                    <v-col cols="12" md="6">
                        <v-autocomplete v-model="club.bookId" :items="books" item-title="bookName" item-value="bookId"
                            label="選讀書籍 (可搜尋)" variant="outlined" color="primary" clearable></v-autocomplete>
                    </v-col>

                    <v-col cols="12">
                        <v-text-field v-model="club.externalBookInfo" label="外部書籍資訊 (若非站內書籍請在此說明)" variant="outlined"
                            color="primary"></v-text-field>
                    </v-col>

                    <v-col cols="12">
                        <v-textarea v-model="club.purpose" label="活動宗旨" rows="3" :rules="[rules.required]"
                            variant="outlined" color="primary" hint="請簡述舉辦此讀書會的目的與期望"></v-textarea>
                    </v-col>

                    <v-col cols="12">
                        <v-textarea v-model="club.agenda" label="活動議程" rows="3" :rules="[rules.required]"
                            variant="outlined" color="primary" hint="列出活動的時間安排與流程"></v-textarea>
                    </v-col>

                    <v-col cols="12" md="6">
                        <v-text-field v-model="club.eventDate" label="活動日期時間" type="datetime-local"
                            :rules="[rules.required, rules.dateFuture]" variant="outlined"
                            color="primary"></v-text-field>
                    </v-col>
                    <v-col cols="12" md="6">
                        <v-text-field v-model="club.deadline" label="報名截止時間" type="datetime-local"
                            :rules="[rules.required, rules.dateFuture, rules.deadlineBeforeEvent]" variant="outlined"
                            color="primary"></v-text-field>
                    </v-col>

                    <v-col cols="12" md="8">
                        <v-text-field v-model="club.location" label="活動地點 / 線上連結" :rules="[rules.required]"
                            variant="outlined" color="primary"></v-text-field>
                    </v-col>
                    <v-col cols="12" md="4">
                        <v-text-field v-model.number="club.maxParticipants" label="人數上限" type="number"
                            :rules="[rules.required, rules.positive]" variant="outlined" color="primary"></v-text-field>
                    </v-col>

                    <v-col cols="12">
                        <div class="text-subtitle-1 mb-2">難度等級</div>
                        <v-radio-group v-model="club.difficulty" inline :rules="[rules.required]" color="primary">
                            <v-radio label="🌱 樹苗級 (入門)：無須基礎" :value="1"></v-radio>
                            <v-radio label="🌿 樹幹級 (進階)：需具備基礎" :value="2"></v-radio>
                            <v-radio label="🌳 巨木級 (專家)：深度研討" :value="3"></v-radio>
                        </v-radio-group>
                    </v-col>

                    <v-col cols="12">
                        <v-file-input v-model="proofFile" label="上傳佐證資料/企劃書 (巨木級必填)"
                            prepend-icon="mdi-file-document-outline" variant="outlined"
                            :rules="[rules.requiredIfExpert]" show-size hint="專家級(巨木)需上傳詳細流程或證明，其他等級可選填"></v-file-input>
                    </v-col>
                </v-row>

                <v-divider class="my-6"></v-divider>

                <div class="d-flex align-center mb-6">
                    <v-checkbox v-model="termsAgreed" color="primary" hide-details class="mr-2">
                        <template v-slot:label>
                            <div>
                                我已閱讀並同意
                                <a href="#" @click.prevent="termsDialog = true" class="text-primary font-weight-bold">
                                    覆書會活動規範與條款
                                </a>
                            </div>
                        </template>
                    </v-checkbox>
                </div>

                <v-row>
                    <v-col cols="6">
                        <v-btn color="grey-lighten-1" size="large" block variant="outlined" :loading="loading"
                            @click="submit('DRAFT')">
                            儲存草稿
                        </v-btn>
                    </v-col>
                    <v-col cols="6">
                        <v-btn color="primary" size="large" block :loading="loading" :disabled="!termsAgreed"
                            @click="submit('SUBMIT')">
                            送出審核
                        </v-btn>
                    </v-col>
                </v-row>
            </v-form>
        </v-card>

        <!-- 條款 Dialog -->
        <v-dialog v-model="termsDialog" max-width="600px" scrollable>
            <!-- ... terms content same as before ... -->
            <v-card>
                <v-card-title class="text-h5 font-weight-bold pa-4 bg-primary text-white">
                    讀書會活動規範
                </v-card-title>
                <v-card-text class="pa-4" style="max-height: 400px;">
                    <ol class="pl-4">
                        <li class="mb-2">發起人需確保活動內容不違反法律法規及社會善良風俗。</li>
                        <li class="mb-2">活動期間需尊重所有參與者，禁止任何形式的騷擾與歧視。</li>
                        <li class="mb-2">如需取消活動，請至少於活動前三天通知所有參與者並取消活動。</li>
                        <li class="mb-2">為了維護良好的閱讀環境，請準時開始與結束活動。</li>
                        <li class="mb-2">平台僅提供媒合服務，不介入線下活動的糾紛處理。</li>
                        <li class="mb-2">嚴禁利用讀書會進行推銷、直銷或其他商業營利行為。</li>
                    </ol>
                </v-card-text>
                <v-card-actions class="pa-4 pt-0">
                    <v-spacer></v-spacer>
                    <v-btn color="primary" variant="text" @click="termsDialog = false">關閉</v-btn>
                    <v-btn color="primary" variant="elevated" @click="termsDialog = false; termsAgreed = true">
                        我同意
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-container>
</template>

<style scoped>
.text-primary {
    color: #2E5C43 !important;
}

.bg-primary {
    background-color: #2E5C43 !important;
}
</style>
