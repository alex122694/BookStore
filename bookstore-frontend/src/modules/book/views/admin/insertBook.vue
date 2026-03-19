<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';
import SmartInputButtons from '@/common/components/SmartInputButtons.vue';

const router = useRouter();
const genres = ref([]);
const loading = ref(false);
// Form data
const book = ref({
    bookName: '',
    author: '',
    translator: '',
    press: '',
    price: null,
    isbn: '',
    stock: null,
    shortDesc: '',
    onShelf: 0 // Default off or on? Default 0 (off)
});

const selectedGenreIds = ref([]);
const imageFile = ref(null); // File object
const imagePreview = ref(null); // URL for preview

// Rules
const rules = {
    required: value => (value !== null && value !== undefined && value !== '') || '此欄位為必填',
    positive: value => value >= 0 || '數值必須大於等於 0',
    isbnFormat: value => /^\d{13}$/.test(value) || 'ISBN 必須為 13 位數字',
    isbnUnique: async (value) => {
        if (!value || !/^\d{13}$/.test(value)) return true; // 格式錯誤交由其他規則處理
        try {
            const response = await bookService.checkIsbn(value);
            // 假設後端回傳 true 代表存在 (重複)，false 代表可用
            // 原有API邏輯: checkIsbn -> bookService.existsByIsbn -> Returns true if exists
            return response.data === false || '此 ISBN 已存在，請檢查是否重複輸入';
        } catch (error) {
            console.error('ISBN 驗證失敗:', error);
            return '無法驗證 ISBN';
        }
    }
};

const form = ref(null);

onMounted(async () => {
    try {
        const response = await bookService.getGenres();
        genres.value = response.data;
    } catch (error) {
        console.error('Failed to load genres:', error);
    }
});

const handleFileUpdate = (files) => {
    // Vuetify v-file-input v-model is an array or single? 
    // Usually array in V3.
    const file = Array.isArray(files) ? files[0] : files;
    if (file) {
        imageFile.value = file;
        imagePreview.value = URL.createObjectURL(file);
    } else {
        imageFile.value = null;
        imagePreview.value = null;
    }
};


const generateRandomBook = () => {
    const randomSuffix = Math.floor(Math.random() * 10000);

    // Generate random ISBN 13 (starts with 978) to ensure uniqueness
    let isbn = '978';
    for (let i = 0; i < 10; i++) {
        isbn += Math.floor(Math.random() * 10);
    }

    book.value = {
        bookName: `巴菲特寫給股東的信`,
        author: `Warren Edward Buffett`,
        translator: `許晶晶`,
        press: `財信出版`,
        price: 600,
        isbn: isbn,
        stock: 0,
        shortDesc: `本書收錄了巴菲特寫給波克夏股東的信，闡述了他的投資哲學與管理智慧。是投資人必讀的經典之作。 (測試編號: ${randomSuffix})`,
        onShelf: 1
    };

    // Attempt to select "Business" or "Finance" related genres if available, otherwise random
    if (genres.value.length > 0) {
        // Try to find keywords like '理財', '投資', '商業'
        const targetKeywords = ['理財', '投資', '商業', '經濟'];
        const targetGenres = genres.value.filter(g => targetKeywords.some(k => g.genreName.includes(k)));

        if (targetGenres.length > 0) {
            selectedGenreIds.value = targetGenres.map(g => g.genreId);
        } else {
            // Fallback to random
            const numGenres = Math.floor(Math.random() * 3) + 1;
            const shuffled = [...genres.value].sort(() => 0.5 - Math.random());
            selectedGenreIds.value = shuffled.slice(0, numGenres).map(g => g.genreId);
        }
    }
};

const clearForm = () => {
    book.value = {
        bookName: '',
        author: '',
        translator: '',
        press: '',
        price: null,
        isbn: '',
        stock: null,
        shortDesc: '',
        onShelf: 0
    };
    selectedGenreIds.value = [];
    imageFile.value = null;
    imagePreview.value = null;
    form.value.resetValidation();
};

const submit = async () => {
    const { valid } = await form.value.validate();
    if (!valid) return;

    loading.value = true;

    // Prepare data
    // genre mapping
    book.value.genres = selectedGenreIds.value.map(id => ({ genreId: id }));

    try {
        const response = await bookService.createBook(book.value, imageFile.value);
        const newBook = response.data;

        // Custom SweetAlert
        const displayImage = imagePreview.value || 'https://via.placeholder.com/150?text=No+Image';

        const htmlContent = `
            <div style="display: flex; align-items: flex-start; gap: 20px; text-align: left; padding: 10px;">
                <div style="flex-shrink: 0;">
                    <img src="${displayImage}" style="width: 120px; height: auto; border-radius: 4px; border: 1px solid #ddd; object-fit: cover;">
                </div>
                <div style="flex-grow: 1; font-size: 0.95rem; line-height: 1.6;">
                    <div style="margin-bottom: 4px;"><strong style="color: #2E5C43;">書名：</strong> ${newBook.bookName}</div>
                    <div style="margin-bottom: 4px;"><strong>作者：</strong> ${newBook.author}</div>
                    <div style="margin-bottom: 4px;"><strong>出版社：</strong> ${newBook.press}</div>
                    <div style="margin-bottom: 4px;"><strong>定價：</strong> $${newBook.price}</div>
                    <div style="margin-bottom: 4px;"><strong>庫存：</strong> ${newBook.stock}</div>
                    <div validate-on="blur"><strong>ISBN：</strong> ${newBook.isbn}</div>
                </div>
            </div>
        `;

        await Swal.fire({
            title: '<h3 style="color: #2E5C43; margin: 0;">新增成功！</h3>',
            html: htmlContent,
            icon: 'success',
            confirmButtonText: '確定',
            confirmButtonColor: '#2E5C43',
            width: '600px',
            padding: '2em',
            background: '#fff url(/images/trees.png)', // Optional theme touch? Keep simple
            backdrop: `
                rgba(0,0,123,0.1)
                left top
                no-repeat
            `
        });

        router.push('/dev/admin/books');

    } catch (error) {
        console.error(`[GlobalHandler] Error Code: ${code}, Message: ${message}`);

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
        <h2 class="text-h4 font-weight-bold text-primary mb-6">新增書籍資料</h2>

        <v-card class="rounded-lg elevation-2 pa-6">
            <v-form ref="form" @submit.prevent="submit">
                <v-row>
                    <!-- Left Column: Image Upload -->
                    <v-col cols="12" md="4" class="text-center">
                        <v-card class="mx-auto mb-4 d-flex align-center justify-center bg-grey-lighten-4" width="100%"
                            height="300" variant="outlined" style="border-style: dashed !important;">
                            <v-img v-if="imagePreview" :src="imagePreview" contain max-height="100%"></v-img>
                            <div v-else class="text-grey">
                                <v-icon icon="mdi-image-plus" size="64" class="mb-2"></v-icon>
                                <div>預覽圖片</div>
                            </div>
                        </v-card>
                        <v-file-input label="上傳封面圖片" accept="image/*" prepend-icon="mdi-camera" variant="outlined"
                            density="compact" @update:model-value="handleFileUpdate" show-size></v-file-input>
                    </v-col>

                    <!-- Right Column: Details -->
                    <v-col cols="12" md="8">
                        <v-row dense>
                            <v-col cols="12">
                                <v-text-field v-model="book.bookName" label="書籍名稱" :rules="[rules.required]"
                                    variant="outlined" color="primary"></v-text-field>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field v-model="book.author" label="作者" :rules="[rules.required]"
                                    variant="outlined" color="primary"></v-text-field>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field v-model="book.translator" label="譯者" variant="outlined"
                                    color="primary"></v-text-field>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field v-model="book.press" label="出版社" :rules="[rules.required]"
                                    variant="outlined" color="primary"></v-text-field>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-select v-model="selectedGenreIds" :items="genres" item-title="genreName"
                                    item-value="genreId" label="書籍類型 (多選)" multiple chips closable-chips
                                    variant="outlined" color="primary"></v-select>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field v-model.number="book.price" label="售價" type="number"
                                    :rules="[rules.required, rules.positive]" prefix="$" variant="outlined"
                                    color="primary"></v-text-field>
                            </v-col>

                            <v-col cols="12">
                                <v-text-field v-model="book.isbn" label="ISBN (13碼)"
                                    :rules="[rules.required, rules.isbnFormat, rules.isbnUnique]" counter="13"
                                    variant="outlined" color="primary"></v-text-field>
                            </v-col>
                            <v-col cols="12">
                                <v-textarea v-model="book.shortDesc" label="書籍簡介" rows="3" variant="outlined"
                                    color="primary"></v-textarea>
                            </v-col>

                        </v-row>

                        <div class="d-flex justify-space-between mt-4">
                            <SmartInputButtons :presets="[
                                { title: '一鍵輸入', handler: generateRandomBook, icon: 'mdi-flash', color: 'info' }
                            ]" :showClear="true" :clearHandler="clearForm" />
                            <div>
                                <v-btn variant="text" class="mr-2" @click="router.back()">取消</v-btn>
                                <v-btn type="submit" color="primary" :loading="loading" elevation="2"
                                    size="large">確認新增</v-btn>
                            </div>
                        </div>
                    </v-col>
                </v-row>
            </v-form>
        </v-card>
    </div>
</template>

<style scoped>
.v-card {
    border-top: 4px solid #2E5C43;
}
</style>
