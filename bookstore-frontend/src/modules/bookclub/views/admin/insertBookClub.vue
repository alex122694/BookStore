<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import bookClubService from '@/modules/bookclub/api';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';

const router = useRouter();
const categories = ref([]);
const books = ref([]);
const loading = ref(false);

const club = ref({
    clubName: '',
    categoryId: null,
    bookId: null,
    eventDate: '',
    deadline: '',
    location: '',
    maxParticipants: null,
    externalBookInfo: '', // Optional
});

const proposalFile = ref(null);
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
    }
};

const form = ref(null);

onMounted(async () => {
    try {
        const [catResponse, bookResponse] = await Promise.all([
            bookClubService.getClubCategories(),
            bookService.getAllBooks()
        ]);
        categories.value = catResponse.data;
        books.value = bookResponse.data;
    } catch (error) {
        console.error('Failed to load initial data:', error);
        Swal.fire('錯誤', '載入初始資料失敗', 'error');
    }
});

const generateRandomClub = () => {
    const randomSuffix = Math.floor(Math.random() * 10000);

    // Future date
    const eventDate = new Date();
    eventDate.setDate(eventDate.getDate() + 30);
    const deadline = new Date();
    deadline.setDate(deadline.getDate() + 15);

    club.value = {
        clubName: `讀書會 ${randomSuffix}`,
        categoryId: categories.value.length > 0 ? categories.value[0].categoryId : null,
        bookId: books.value.length > 0 ? books.value[0].bookId : null,
        eventDate: eventDate.toISOString().slice(0, 16), // YYYY-MM-DDTHH:mm
        deadline: deadline.toISOString().slice(0, 16),
        location: `線上會議室 ${randomSuffix}`,
        maxParticipants: Math.floor(Math.random() * 20) + 5,
        externalBookInfo: '無'
    };
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
        externalBookInfo: ''
    };
    proposalFile.value = null;
    proofFile.value = null;
    form.value.resetValidation();
};

const submit = async () => {
    const { valid } = await form.value.validate();
    if (!valid) return;

    if (!proposalFile.value) {
        Swal.fire('提示', '請上傳活動企劃書', 'warning');
        return;
    }

    loading.value = true;

    // Prepare DTO structure matches backend expectation
    // Backend expects 'bookclub' part with nested objects: { categoriesBean: { categoryId: ... }, book: { bookId: ... } }
    const clubData = {
        clubName: club.value.clubName,
        location: club.value.location,
        eventDate: club.value.eventDate, // String format fine for LocalDateTime if ISO-like
        deadline: club.value.deadline,
        maxParticipants: club.value.maxParticipants,
        externalBookInfo: club.value.externalBookInfo,
        categoriesBean: { categoryId: club.value.categoryId },
        book: club.value.bookId ? { bookId: club.value.bookId } : null
    };

    try {
        await bookClubService.createClub(clubData, proposalFile.value, proofFile.value);

        await Swal.fire({
            title: '新增成功！',
            text: '讀書會已建立，請等待審核。',
            icon: 'success',
            confirmButtonColor: '#2E5C43'
        });

        router.push('/dev/admin/bookClubs');
    } catch (error) {
        console.error(error);
        Swal.fire({
            icon: 'error',
            title: '新增失敗',
            text: error.message || '發生錯誤，請稍後再試。',
            confirmButtonColor: '#d33'
        });
    } finally {
        loading.value = false;
    }
};
</script>

<template>
    <div class="pa-4">
        <h2 class="text-h4 font-weight-bold text-primary mb-6">新增讀書會</h2>

        <v-card class="rounded-lg elevation-2 pa-6">
            <v-alert type="info" variant="tonal" class="mb-6" border="start" closable>
                請填寫完整讀書會資訊並上傳企劃書以供審核。審核通過後即開放報名。
            </v-alert>

            <v-form ref="form" @submit.prevent="submit">
                <v-row>
                    <!-- Left Column: Basic Info -->
                    <v-col cols="12" md="6">
                        <v-text-field v-model="club.clubName" label="讀書會名稱" :rules="[rules.required]" variant="outlined"
                            color="primary"></v-text-field>

                        <v-select v-model="club.categoryId" :items="categories" item-title="categoryName"
                            item-value="categoryId" label="分類" :rules="[rules.required]" variant="outlined"
                            color="primary"></v-select>

                        <v-autocomplete v-model="club.bookId" :items="books" item-title="bookName" item-value="bookId"
                            label="選讀書籍 (選擇站內書籍)" variant="outlined" color="primary" clearable></v-autocomplete>

                        <v-text-field v-model="club.externalBookInfo" label="外部書籍資訊 (若非站內書籍請填寫)" variant="outlined"
                            color="primary"></v-text-field>

                        <v-text-field v-model.number="club.maxParticipants" label="人數上限" type="number"
                            :rules="[rules.required, rules.positive]" variant="outlined" color="primary"></v-text-field>
                    </v-col>

                    <!-- Right Column: Time & Files -->
                    <v-col cols="12" md="6">
                        <v-text-field v-model="club.location" label="活動地點 / 線上連結" :rules="[rules.required]"
                            variant="outlined" color="primary"></v-text-field>

                        <v-text-field v-model="club.eventDate" label="活動日期時間" type="datetime-local"
                            :rules="[rules.required, rules.dateFuture]" variant="outlined"
                            color="primary"></v-text-field>

                        <v-text-field v-model="club.deadline" label="報名截止時間" type="datetime-local"
                            :rules="[rules.required, rules.dateFuture, rules.deadlineBeforeEvent]" variant="outlined"
                            color="primary"></v-text-field>

                        <v-file-input v-model="proposalFile" label="活動企劃書 (PDF/Word)"
                            prepend-icon="mdi-file-document-outline" variant="outlined" :rules="[rules.required]"
                            show-size></v-file-input>

                        <v-file-input v-model="proofFile" label="相關證明文件 (選填)" prepend-icon="mdi-certificate-outline"
                            variant="outlined" show-size></v-file-input>
                    </v-col>
                </v-row>

                <div class="d-flex justify-space-between mt-6">
                    <div class="d-flex gap-2">
                        <v-btn color="info" variant="tonal" prepend-icon="mdi-flash" @click="generateRandomClub">
                            一鍵輸入
                        </v-btn>
                        <v-btn color="error" variant="tonal" prepend-icon="mdi-delete-sweep" class="ml-2"
                            @click="clearForm">
                            清空欄位
                        </v-btn>
                    </div>
                    <div>
                        <v-btn variant="text" class="mr-2" @click="router.back()">取消</v-btn>
                        <v-btn type="submit" color="primary" :loading="loading" elevation="2" size="large">確認新增</v-btn>
                    </div>
                </div>
            </v-form>
        </v-card>
    </div>
</template>

<style scoped>
.v-card {
    border-top: 4px solid #2E5C43;
    /* Forest theme color */
}
</style>
