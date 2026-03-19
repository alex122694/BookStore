<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import bookService from '@/modules/book/api';
import Swal from 'sweetalert2';
import ActionPageButton from '@/common/components/ActionPageButton.vue';

const router = useRouter();
const books = ref([]);
const loading = ref(false);

const search = ref('');
const page = ref(1);

// 讀取已儲存的頁碼
const savedPage = sessionStorage.getItem('adminBookListPage');
if (savedPage) {
    page.value = parseInt(savedPage, 10);
}

// 監聽頁碼變動並儲存
watch(page, (newPage) => {
    sessionStorage.setItem('adminBookListPage', newPage);
});

// 圖片放大功能狀態
const zoomedImageUrl = ref(null);
const zoomPosition = ref({ x: 0, y: 0 });

// 定義表格欄位
const headers = [
    // { title: '圖片', key: 'bookImage', sortable: false, align: 'center', width: '80px' }, 
    { title: '書籍名稱', key: 'bookName', sortable: true, align: 'start', width: '20%' },
    { title: '作者', key: 'author', sortable: true, align: 'center' },
    { title: '譯者', key: 'translator', sortable: true, align: 'center' },
    { title: '出版社', key: 'press', sortable: true, align: 'center' },
    { title: '類型', key: 'genres', sortable: false, align: 'center' },
    { title: '售價', key: 'price', sortable: true, align: 'center' },
    { title: 'ISBN', key: 'isbn', sortable: true, align: 'center' },
    { title: '庫存', key: 'stock', sortable: true, align: 'center' },
    { title: '修改', key: 'actions_edit', sortable: false, align: 'center' },
    { title: '封存', key: 'actions_archive', sortable: false, align: 'center' },
    { title: '銷售狀態', key: 'onShelf', sortable: true, align: 'center' },
];

// 讀取書籍資料
const loadBooks = async () => {
    loading.value = true;
    try {
        const response = await bookService.getAllBooks();
        books.value = response.data;
    } catch (error) {
        console.error('讀取失敗:', error);
    } finally {
        loading.value = false;
    }
};

// 狀態切換處理 (上架/下架)
// 狀態切換處理 (上架/下架)
const toggleStatus = async (item, newValue) => {
    // 判斷動作名稱
    const actionName = newValue === 1 ? '上架' : '下架';

    try {
        const result = await Swal.fire({
            title: `確定要${actionName}嗎？`,
            text: `書籍狀態將變更為${actionName}`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#2E5C43',
            cancelButtonColor: '#d33',
            confirmButtonText: `是，${actionName}！`,
            cancelButtonText: '取消'
        });

        if (result.isConfirmed) {
            await bookService.updateStatus(item.bookId, newValue);
            item.onShelf = newValue;
            // 根據需求：確認後直接執行，不顯示成功 alert
        }
    } catch (error) {
        console.error('狀態更新失敗:', error);
        Swal.fire('錯誤', `${actionName}失敗`, 'error');
    }
};

// 跳轉到修改頁面
const handleEdit = (item) => {
    router.push(`/dev/admin/books/update/${item.bookId}`);
};

// 執行封存
// 執行封存/解封
const handleArchive = async (item) => {
    // 判斷當前是否為封存狀態 (2)
    const isArchived = item.onShelf === 2;
    const actionText = isArchived ? '解封' : '封存';
    const confirmButtonText = isArchived ? '是，解封！' : '是，封存！';
    const successTitle = isArchived ? '已解封' : '已封存';

    // 如果是解封，API 不同
    const apiCall = isArchived
        ? () => bookService.unarchiveBook(item.bookId)
        : () => bookService.archiveBook(item.bookId);

    try {
        const result = await Swal.fire({
            title: `確定要${actionText}嗎？`,
            text: isArchived ? "解封後書籍將回到下架狀態" : "封存後書籍將不出現在前台",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#2E5C43',
            cancelButtonColor: '#d33',
            confirmButtonText: confirmButtonText
        });

        if (result.isConfirmed) {
            await apiCall();
            await loadBooks(); // 重新載入以更新狀態
            Swal.fire(successTitle, '書籍狀態已更新', 'success');
        }
    } catch (error) {
        console.error(error);
        Swal.fire('錯誤', `${actionText}失敗`, 'error');
    }
};

// [新增] 圖片路徑處理
const getImageUrl = (imageBean) => {
    if (imageBean && imageBean.imageUrl) {
        // 假設後端圖片位於 http://localhost:8080/images/
        // 如果您的圖片路徑不同，請在此修改
        return `http://localhost:8080/upload-images/${imageBean.imageUrl}`;
    }
    return '';
};

// 顯示放大圖片
const showZoom = (event, imageBean) => {
    const url = getImageUrl(imageBean);
    if (!url) return;
    zoomedImageUrl.value = url;
    zoomPosition.value = { x: event.clientX, y: event.clientY };
};

// 關閉放大圖片
const closeZoom = () => {
    zoomedImageUrl.value = null;
};

onMounted(() => {
    loadBooks();
});
</script>

<template>
    <div>
        <!-- 放大圖片顯示層 (Overlay) -->
        <!-- <div v-if="zoomedImageUrl" class="zoomed-image-overlay elevation-10 rounded-lg" :style="{
            top: `${zoomPosition.y}px`,
            left: `${zoomPosition.x}px`
        }" @mouseleave="closeZoom">
            <v-img :src="zoomedImageUrl" cover width="100%" height="100%"></v-img>
        </div> -->

        <!-- 標題與搜尋區 -->
        <v-row class="mb-4" align="center">
            <v-col cols="12" md="4">
                <h2 class="text-h4 font-weight-bold text-primary">書籍管理列表</h2>
                <ActionPageButton class="mt-2" @click="router.push('/dev/admin/books/insert')">
                    新增書籍
                </ActionPageButton>
            </v-col>
            <v-col cols="12" md="4" offset-md="4">
                <v-text-field v-model="search" label="搜尋書籍..." prepend-inner-icon="mdi-magnify" variant="outlined"
                    density="compact" hide-details color="primary" class="bg-white rounded"></v-text-field>
            </v-col>
        </v-row>

        <!-- 書籍列表區 -->
        <v-card class="rounded-lg elevation-2 border-t-4 border-primary">
            <v-data-table v-model:page="page" :headers="headers" :items="books" :loading="loading" :search="search"
                class="forest-table" item-value="bookId" hover>

                <!-- [新增] 圖片顯示 Slot -->
                <template v-slot:item.bookImage="{ item }">
                    <div style="width: 50px; height: 50px; cursor: pointer;"
                        class="bg-grey-lighten-2 rounded my-1 overflow-hidden d-flex align-center justify-center">
                        <img v-if="getImageUrl(item.bookImageBean)" :src="getImageUrl(item.bookImageBean)"
                            style="width: 100%; height: 100%; object-fit: cover;"
                            @click="showZoom($event, item.bookImageBean)" alt="Book Image" />
                        <v-icon v-else icon="mdi-image-off" color="grey"></v-icon>
                    </div>
                </template>

                <!-- 書籍名稱超連結 -->
                <template v-slot:item.bookName="{ item }">
                    <router-link :to="`/dev/admin/books/get/${item.bookId}`"
                        class="text-decoration-none text-primary font-weight-bold" style="cursor: pointer;">
                        {{ item.bookName }}
                    </router-link>
                </template>

                <!-- [修正] 類型顯示 Slot (處理陣列) -->
                <template v-slot:item.genres="{ item }">
                    <div class="text-caption">
                        {{item.genres?.map(g => g.genreName).join(', ') || '-'}}
                    </div>
                </template>

                <!-- 修改按鈕 (上架中或已封存不可用) -->
                <template v-slot:item.actions_edit="{ item }">
                    <v-btn :color="(item.onShelf === 1 || item.onShelf === 2) ? 'grey-lighten-1' : 'primary'"
                        :class="{ 'text-white': (item.onShelf !== 1 && item.onShelf !== 2), 'cursor-not-allowed': (item.onShelf === 1 || item.onShelf === 2) }"
                        size="small" elevation="1" :disabled="item.onShelf === 1 || item.onShelf === 2"
                        :style="(item.onShelf === 1 || item.onShelf === 2) ? { cursor: 'not-allowed', pointerEvents: 'auto' } : {}"
                        @click="(item.onShelf !== 1 && item.onShelf !== 2) && handleEdit(item)">
                        修改
                    </v-btn>
                </template>

                <!-- 封存/解封按鈕 -->
                <template v-slot:item.actions_archive="{ item }">
                    <v-btn :color="item.onShelf === 2 ? 'info' : 'secondary'" class="text-white" size="small"
                        elevation="1" @click="handleArchive(item)">
                        {{ item.onShelf === 2 ? '解封' : '封存' }}
                    </v-btn>
                </template>

                <!-- 銷售狀態開關 (封存狀態下不可用) -->
                <template v-slot:item.onShelf="{ item }">
                    <div class="d-flex flex-column align-center"
                        :style="item.onShelf === 2 ? { cursor: 'not-allowed' } : {}">
                        <v-switch :model-value="item.onShelf" color="success" hide-details density="compact"
                            :true-value="1" :false-value="0" :disabled="item.onShelf === 2"
                            @update:model-value="(val) => toggleStatus(item, val)" class="ms-2"
                            :class="{ 'opacity-50': item.onShelf === 2 }"></v-switch>
                        <span class="text-caption text-grey-darken-1" style="line-height: 1;">
                            {{ item.onShelf === 1 ? '上架中' : (item.onShelf === 2 ? '已封存' : '下架中') }}
                        </span>
                    </div>
                </template>

                <!-- 處理空值顯示 -->
                <template v-slot:item.translator="{ value }">
                    {{ value || '-' }}
                </template>

            </v-data-table>
        </v-card>
    </div>
</template>

<style scoped lang="scss">
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

/* 放大圖片層樣式 */
.zoomed-image-overlay {
    position: fixed;
    width: 300px;
    height: 300px;
    z-index: 9999;
    transform: translate(-50%, -50%);
    background-color: white;
    border: 4px solid #F5F5DC;
}

.v-switch {
    transform: scale(0.8);
}
</style>
