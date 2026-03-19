<template>
    <div class="list-page-wrapper">
        <div class="header-section mb-6 text-left">
            <h2 class="forest-main-title">書籍評價</h2>
        </div>

        <v-row class="mb-4" align="center">
            <v-col cols="auto"></v-col>
            <v-spacer></v-spacer>

            <v-col cols="12" md="4">
                <div class="d-flex align-center">
                   <v-text-field 
                    v-model="search" 
                    label="搜尋書籍..." 
                    prepend-inner-icon="mdi-magnify" 
                    variant="outlined"
                    density="compact" 
                    hide-details 
                    clearable
                    bg-color="white"
                    color="primary" 
                    class="rounded-lg flex-grow-1"
                ></v-text-field> 
                </div>
            </v-col>
        </v-row>

        <v-card class="forest-card-table">
            <v-data-table 
                :headers="headers" 
                :items="books" 
                :loading="loading" 
                :search="search" 
                class="forest-table-style"
                item-value="bookId" 
                hover
            >

                <template v-slot:item.bookName="{ item }">
                    <a href="#" 
                       @click.prevent="bookDetails(item)"
                       class="text-decoration-none text-primary font-weight-bold" 
                       style="cursor: pointer;">
                        {{ item.bookName }}
                    </a>
                </template>
                
                <template v-slot:item.reviewCount="{ item }">
                    <v-chip size="small" :color="item.reviewCount > 0 ? 'primary' : 'grey'">
                        {{ item.reviewCount || 0 }} 筆
                    </v-chip>
                </template>

                <template v-slot:item.avgRating="{ item }">
                    <div class="d-flex align-center justify-center">
                        <v-icon color="amber" size="small" class="me-1">mdi-star</v-icon>
                        <span class="font-weight-bold">
                            {{ item.avgRating ? item.avgRating.toFixed(1) : '0.0' }}
                        </span>
                    </div>
                </template>

                <template v-slot:item.actions="{ item }">
                    <v-btn 
                        color="primary" 
                        variant="outlined"
                        size="small" 
                        prepend-icon="mdi-comment-text-outline"
                        @click="bookReviews(item)"
                    >
                        查看評價
                    </v-btn>
                </template>

                <template v-slot:item.translator="{ value }">
                    {{ value || '-' }}
                </template>
                
                <template v-slot:item.press="{ value }">
                    {{ value || '-' }}
                </template>

            </v-data-table>
        </v-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import reviewService from '@/modules/review/api'; // 假設你用同一個 Service，或者換成 reviewService
import Swal from 'sweetalert2';

const router = useRouter();
const books = ref([]);
const loading = ref(false);
const search = ref('');

// 定義表格欄位 (專為評價管理設計)
const headers = [
    { title: '書籍名稱', key: 'bookName', sortable: true, align: 'start', width: '25%' },
    { title: '作者', key: 'author', sortable: true, align: 'center' },
    { title: '出版社', key: 'press', sortable: true, align: 'center' },
    { title: '總評價數', key: 'reviewCount', sortable: true, align: 'center' }, 
    { title: '平均評分', key: 'avgRating', sortable: true, align: 'center' },
    { title: '評價資料', key: 'actions', sortable: false, align: 'center' },
];

// 讀取書籍資料
const loadBooks = async () => {
    loading.value = true;
    try {
        const response = await reviewService.getBooksWithStats(); 
        books.value = response.data;
    } catch (error) {
        console.error('讀取失敗:', error);
        Swal.fire('錯誤', '無法讀取書籍列表', 'error');
    } finally {
        loading.value = false;
    }
};

// 跳轉到該書籍的評價列表
const bookReviews = (item) => {
    // router 的 name: 'BookReviews' 或是路徑
    router.push({ 
        name: 'admin-book-reviews', 
        params: { bookId: item.bookId }, 
        query: { bookName: item.bookName }
    });
};

const bookDetails = (item) => {
    router.push({
        name: 'admin-books-get', // 假設你有這個 name
        params: { id: item.bookId }
    });
};

onMounted(() => {
    loadBooks();
});
</script>

<style scoped lang="scss">
.list-page-wrapper {
  padding: 0;
  width: 100%;
}

.forest-main-title {
  color: #2e5c43;
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 0;
}

.forest-card-table {
  background-color: white !important;
  border-radius: 12px !important;
  border-top: 5px solid #2e5c43 !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05) !important;
}
/* 完全沿用原本的樣式設定 */
.forest-table-style {
:deep(.v-data-table-header) {
    background-color: #F9FBE7 !important;
}

:deep(.v-data-table-header__content) {
    font-weight: bold;
    color: #2E5C43;
    font-size: 1rem;
}

:deep(.v-data-table__tr:nth-child(even)) {
    background-color: #FAFAFA;
}

:deep(.v-data-table__tr:hover) {
    background-color: #F1F8E9 !important;
}
}
</style>