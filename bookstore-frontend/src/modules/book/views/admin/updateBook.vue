<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';

const route = useRoute();
const router = useRouter();
const genres = ref([]);
const loading = ref(false);
const bookId = route.params.id;

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
    onShelf: 0,
    genres: []
});

const selectedGenreIds = ref([]);
const imageFile = ref(null);
const imagePreview = ref(null);
const originalIsbn = ref(''); // 儲存原始 ISBN 用於比對

// Rules
const rules = {
    required: value => (value !== null && value !== undefined && value !== '') || '此欄位為必填',
    positive: value => value >= 0 || '數值必須大於等於 0',
    isbnFormat: value => /^\d{13}$/.test(value) || 'ISBN 必須為 13 位數字',
    isbnUnique: async (value) => {
        if (!value || !/^\d{13}$/.test(value)) return true;
        // 如果 ISBN 沒有變更，則不視為重複 (視為有效)
        if (value === originalIsbn.value) return true;

        try {
            const response = await bookService.checkIsbn(value);
            return response.data === false || '此 ISBN 已存在，請檢查是否重複輸入';
        } catch (error) {
            console.error('ISBN 驗證失敗:', error);
            return '無法驗證 ISBN';
        }
    }
};

const form = ref(null);

onMounted(async () => {
    loading.value = true;
    try {
        // 1. 取得所有分類
        const genresResponse = await bookService.getGenres();
        genres.value = genresResponse.data;

        // 2. 取得書籍資料
        const bookResponse = await bookService.getBook(bookId);
        const bookData = bookResponse.data;

        // 填入表單
        book.value = { ...bookData };
        originalIsbn.value = bookData.isbn; // 紀錄原始 ISBN

        // 處理分類回填
        if (bookData.genres) {
            selectedGenreIds.value = bookData.genres.map(g => g.genreId);
        }

        // 處理圖片預覽
        if (bookData.bookImageBean && bookData.bookImageBean.imageUrl) {
            imagePreview.value = `http://localhost:8080/upload-images/${bookData.bookImageBean.imageUrl}`;
        }

    } catch (error) {
        console.error('資料載入失敗:', error);
        Swal.fire('錯誤', '無法載入書籍資料', 'error');
        router.back();
    } finally {
        loading.value = false;
    }
});

const handleFileUpdate = (files) => {
    const file = Array.isArray(files) ? files[0] : files;
    if (file) {
        imageFile.value = file;
        imagePreview.value = URL.createObjectURL(file);
    }
    // 注意：若使用者未選擇新檔案，不應清空 imagePreview (因為可能原本就有圖)
    // 但如果使用者明確清除檔案... Vuetify file input 行為需注意
    // 這裡簡單處理：如果有選檔案就更新預覽
};

const submit = async () => {
    const { valid } = await form.value.validate();
    if (!valid) return;

    loading.value = true;

    // Prepare data
    // genre mapping
    book.value.genres = selectedGenreIds.value.map(id => ({ genreId: id }));

    try {
        const response = await bookService.updateBook(bookId, book.value, imageFile.value);

        await Swal.fire({
            title: '修改成功！',
            icon: 'success',
            confirmButtonText: '確定',
            confirmButtonColor: '#2E5C43'
        });

        router.push('/dev/admin/books');

    } catch (error) {
        console.error(error);
        Swal.fire({
            icon: 'error',
            title: '修改失敗',
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
        <h2 class="text-h4 font-weight-bold text-primary mb-6">修改書籍資料</h2>

        <v-card class="rounded-lg elevation-2 pa-6" :loading="loading">
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
                        <v-file-input label="更換封面圖片" accept="image/*" prepend-icon="mdi-camera" variant="outlined"
                            density="compact" @update:model-value="handleFileUpdate"
                            show-sizeHint="若不修改圖片請留空"></v-file-input>
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
                                <v-text-field v-model.number="book.price" label="價錢" type="number"
                                    :rules="[rules.required, rules.positive]" prefix="$" variant="outlined"
                                    color="primary"></v-text-field>
                            </v-col>
                            <!-- <v-col cols="12" md="6">
                                <v-text-field v-model.number="book.stock" label="庫存數量" type="number"
                                    :rules="[rules.required, rules.positive]" variant="outlined"
                                    color="primary"></v-text-field>
                            </v-col> -->

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

                        <div class="d-flex justify-end mt-4">
                            <v-btn variant="text" class="mr-2" @click="router.back()">取消</v-btn>
                            <v-btn type="submit" color="primary" :loading="loading" elevation="2"
                                size="large">確認修改</v-btn>
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
