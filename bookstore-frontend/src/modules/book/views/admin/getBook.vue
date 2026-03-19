<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import bookService from '@/modules/book/api';
import bookClubService from '@/modules/bookclub/api';
import Swal from 'sweetalert2';
import BackPageButton from '@/common/components/BackPageButton.vue';

const route = useRoute();
const router = useRouter();
const genres = ref([]);
const loading = ref(false);
const bookId = route.params.id;

// Form data (Read-only)
const book = ref({
    bookName: '',
    author: '',
    translator: '',
    press: '',
    price: null,
    isbn: '',
    stock: null,
    shortDesc: '',
    // onShelf omitted as requested
    genres: []
});

const selectedGenreIds = ref([]);
const imagePreview = ref(null);
const relatedClubs = ref([]);

const clubHeaders = [
    { title: '讀書會名稱', key: 'clubName' },
    { title: '詳細資訊', key: 'details', sortable: false },
    { title: '時間', key: 'eventDate' },
    { title: '地點', key: 'location' },
    { title: '報名狀況', key: 'participants' },
];

onMounted(async () => {
    loading.value = true;
    try {
        // 1. 取得所有分類 (用於顯示正確的分類名稱)
        const genresResponse = await bookService.getGenres();
        genres.value = genresResponse.data;

        // 2. 取得書籍資料
        const bookResponse = await bookService.getBook(bookId);
        const bookData = bookResponse.data;

        // 填入資料
        book.value = { ...bookData };

        // 處理分類回填
        if (bookData.genres) {
            selectedGenreIds.value = bookData.genres.map(g => g.genreId);
        }

        // 處理圖片預覽
        if (bookData.bookImageBean && bookData.bookImageBean.imageUrl) {
            imagePreview.value = `http://localhost:8080/upload-images/${bookData.bookImageBean.imageUrl}`;
        }

        // 3. 取得並篩選相關讀書會 (前端篩選)
        try {
            const clubResponse = await bookClubService.getAllClubs();
            if (clubResponse.data) {
                relatedClubs.value = clubResponse.data.filter(club => 
                    club.book && club.book.bookId == bookId
                );
            }
        } catch (e) {
            console.error('相關讀書會載入失敗', e);
            // 不阻擋主流程
        }

    } catch (error) {
        console.error('資料載入失敗:', error);
        Swal.fire('錯誤', '無法載入書籍資料', 'error');
        router.back();
    } finally {
        loading.value = false;
    }
});
</script>

<template>
    <div class="pa-4">
        <div class="d-flex align-center mb-6">
            <BackPageButton />
            <h2 class="text-h4 font-weight-bold text-primary">書籍詳細資料</h2>
        </div>

        <v-card class="rounded-lg elevation-2 pa-6" :loading="loading">
            <v-row>
                <!-- Left Column: Image Display -->
                <v-col cols="12" md="4" class="text-center">
                    <v-card class="mx-auto mb-4 d-flex align-center justify-center bg-grey-lighten-4" width="100%"
                        height="300" variant="outlined">
                        <v-img v-if="imagePreview" :src="imagePreview" contain max-height="100%"></v-img>
                        <div v-else class="text-grey">
                            <v-icon icon="mdi-image-off" size="64" class="mb-2"></v-icon>
                            <div>無封面圖片</div>
                        </div>
                    </v-card>
                </v-col>

                <!-- Right Column: Details (Read-Only) -->
                <v-col cols="12" md="8">
                    <v-row dense>
                        <v-col cols="12">
                            <v-text-field v-model="book.bookName" label="書籍名稱" readonly variant="plain" color="primary"
                                class="font-weight-bold"></v-text-field>
                        </v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="book.author" label="作者" readonly variant="plain"
                                color="primary"></v-text-field>
                        </v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="book.translator" label="譯者" readonly variant="plain"
                                color="primary"></v-text-field>
                        </v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="book.press" label="出版社" readonly variant="plain"
                                color="primary"></v-text-field>
                        </v-col>
                        <v-col cols="12" md="6">
                            <v-select v-model="selectedGenreIds" :items="genres" item-title="genreName"
                                item-value="genreId" label="書籍類型" multiple chips readonly variant="plain"
                                color="primary" :menu-props="{ disabled: true }"></v-select>
                        </v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="book.price" label="價錢" prefix="$" readonly variant="plain"
                                color="primary"></v-text-field>
                        </v-col>
                        <v-col cols="12" md="6">
                            <v-text-field v-model="book.stock" label="庫存" readonly variant="plain"
                                color="primary"></v-text-field>
                        </v-col>
                        <v-col cols="12">
                            <v-text-field v-model="book.isbn" label="ISBN" readonly variant="plain"
                                color="primary"></v-text-field>
                        </v-col>
                        <v-col cols="12">
                            <v-textarea v-model="book.shortDesc" label="書籍簡介" rows="3" readonly variant="plain"
                                color="primary"></v-textarea>
                        </v-col>

                    </v-row>


                </v-col>
            </v-row>

            <v-divider class="my-6"></v-divider>

            <div class="d-flex align-center mb-4">
                <v-icon icon="mdi-account-group" class="mr-2" color="primary"></v-icon>
                <h3 class="text-h6 font-weight-bold text-primary">相關讀書會場次</h3>
            </div>

            <v-data-table :headers="clubHeaders" :items="relatedClubs" class="elevation-1 rounded" hide-default-footer
                v-if="relatedClubs.length > 0">
                
                <template v-slot:item.eventDate="{ item }">
                    {{ new Date(item.eventDate).toLocaleString() }}
                </template>

                <template v-slot:item.details="{ item }">
                    <!-- 使用 AdminBookClub 的檢視路徑或 Modal -->
                    <v-btn size="small" variant="text" color="info" 
                         @click="router.push({ name: 'admin-bookclubs-review', params: { id: item.clubId } })">
                        查看詳情
                    </v-btn>
                </template>

                <template v-slot:item.location="{ item }">
                    {{ item.location }}
                </template>

                 <template v-slot:item.participants="{ item }">
                     {{ item.currentParticipants || 0 }} / {{ item.maxParticipants }}
                     <v-chip size="x-small" class="ml-2" 
                        :color="item.currentParticipants >= item.maxParticipants ? 'error' : 'success'">
                        {{ item.currentParticipants >= item.maxParticipants ? '已滿' : '報名中' }}
                     </v-chip>
                </template>
            </v-data-table>

            <v-alert v-else type="info" variant="tonal" class="mt-2" density="compact">
                此書籍目前沒有相關的讀書會場次。
            </v-alert>

        </v-card>
    </div>
</template>

<style scoped>
.v-card {
    border-top: 4px solid #2E5C43;
}

/* 強制讓 readonly 欄位看起來更像純文字，去除多餘底線 */
:deep(.v-field__overlay) {
    display: none;
}
</style>
