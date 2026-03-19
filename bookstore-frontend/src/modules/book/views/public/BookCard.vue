<script setup>
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import axios from 'axios';
import { useCartStore } from '@/modules/cart/store/cartStore';
import orderService from '@/modules/order/api';
import { useLoginCheck } from '@/common/hooks/useLoginCheck';

const props = defineProps({
    // ...
    book: {
        type: Object,
        required: true
    },
    mode: {
        type: String,
        default: 'grid', // 'grid' | 'list'
        validator: (value) => ['grid', 'list'].includes(value)
    },
    initialFavorited: {
        type: Boolean,
        default: false
    }
});

const router = useRouter();
const cartStore = useCartStore();
const { validateLogin } = useLoginCheck();
const userId = localStorage.getItem('userId');
const isFavorited = ref(props.initialFavorited);

watch(() => props.initialFavorited, (newVal) => {
    isFavorited.value = newVal;
});

// 格式化金額
const formattedPrice = computed(() => {
  return props.book.price ? `$${Number(props.book.price).toLocaleString()}` : '$0'
})

// 圖片路徑處理
const imageUrl = computed(() => {
  // 1. 優先檢查完整 Entity 結構 (BooksBean -> BookImageBean -> imageUrl)
  if (props.book.bookImageBean && props.book.bookImageBean.imageUrl) {
    return `http://localhost:8080/upload-images/${props.book.bookImageBean.imageUrl}`
  }
  // 2. 檢查 DTO(來自 BookSalesDTO 的 coverImage 或手動 mapping 的 imagePath)
  if (props.book.coverImage) {
    return `http://localhost:8080/upload-images/${props.book.coverImage}`
  }
  if (props.book.imagePath) {
    return `http://localhost:8080/upload-images/${props.book.imagePath}`
  }
  return ''
})

// 內容簡述 (50字)
const shortDescription = computed(() => {
    const desc = props.book.description || 
                 props.book.shortDesc || 
                 props.book.summary || 
                 props.book.bookDescription || 
                 props.book.intro ||           
                 '暫無簡介';
                 
    return desc.length > 50 ? desc.substring(0, 50) + '...' : desc;
});

const goToDetail = () => {
  router.push({ name: 'user-book-detail', params: { id: props.book.bookId } })
}

const addToCart = async () => {
    if (!await validateLogin('請先登入後再加入購物車')) return;

    try {
        const response = await orderService.addToCart(props.book.bookId, 1);

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '加入成功',
        text: `${props.book.bookName} 已加入購物車`,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500,
      })

            // 更新全域狀態
            if (response.data.cartCount !== undefined) {
                cartStore.setCartCount(response.data.cartCount);
            } else {
                cartStore.fetchCartCount();
            }
        } else {
            Swal.fire('加入失敗', response.data.message, 'error');
        }
    } catch (error) {
        if (error.response && error.response.status === 401) {
            await validateLogin('驗證失效，請重新登入');
        } else {
            console.error(error);
            Swal.fire('錯誤', '加入購物車失敗', 'error');
        }
    }
}

const toggleFavorite = async () => {
    if (!await validateLogin('登入後即可收藏書籍')) return;

    try {
        const response = await axios.post('http://localhost:8080/api/wishlist/toggle', {
            userId: parseInt(userId),
            bookId: props.book.bookId
        });

        if (response.data.success) {
            isFavorited.value = response.data.isFavorited;

            Swal.fire({
                icon: 'success',
                title: response.data.message,
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 1500
            });
        }
    } catch (error) {
        console.error('收藏操作失敗:', error);
        Swal.fire('錯誤', '操作失敗，請稍後再試', 'error');
    }
};
</script>

<template>
    <!-- GRID VIEW -->
    <v-card v-if="mode === 'grid'" class="mx-auto rounded-lg elevation-3 card-hover h-100 d-flex flex-column"
        @click="goToDetail">
        <!-- 圖片區 -->
        <div class="image-container bg-grey-lighten-4 d-flex align-center justify-center position-relative">
            <v-img v-if="imageUrl" :src="imageUrl" contain aspect-ratio="0.7"></v-img>
            <v-icon v-else icon="mdi-book-open-page-variant" size="64" color="grey-lighten-1"></v-icon>

            <!-- 收藏按鈕 (絕對定位) -->
            <v-btn icon variant="text" color="red" class="position-absolute top-0 right-0 ma-2 bg-white elevation-1"
                @click.stop="toggleFavorite" density="comfortable">
                <v-icon :icon="isFavorited ? 'mdi-heart' : 'mdi-heart-outline'"></v-icon>
            </v-btn>
        </div>

        <v-card-item class="flex-grow-1">
            <!-- 書名 -->
            <v-card-title class="text-h6 font-weight-bold text-truncate mb-1" :title="book.bookName">
                {{ book.bookName }}
            </v-card-title>

            <!-- 作者 -->
            <v-card-subtitle class="mb-2">
                <v-icon icon="mdi-account-edit" size="small" class="me-1"></v-icon>
                {{ book.author || '未知作者' }}
            </v-card-subtitle>

            <!-- 分類 Chips -->
            <div class="mb-2" style="height: 24px; overflow: hidden;">
                <v-chip v-for="g in book.genres" :key="g.genreId" size="x-small" color="secondary" variant="flat"
                    class="me-1">
                    {{ g.genreName }}
                </v-chip>
            </div>

            <!-- 簡介 -->
            <div class="text-caption text-grey text-truncate-2">
                {{ shortDescription }}
            </div>
        </v-card-item>

        <v-divider></v-divider>

        <v-card-text class="pt-2 pb-0">
            <!-- 價格 (酒紅色) -->
            <div class="text-h5 font-weight-bold price-text">
                {{ formattedPrice }}
            </div>
        </v-card-text>

        <v-card-actions class="pt-0">
            <v-btn block variant="flat" color="primary" class="text-white mt-2" @click.stop="addToCart">
                <v-icon start>mdi-cart-plus</v-icon>
                加入購物車
            </v-btn>
        </v-card-actions>
    </v-card>

    <!-- LIST VIEW -->
    <v-card v-else class="d-flex flex-row align-center pa-4 rounded-lg elevation-2 card-hover w-100"
        @click="goToDetail">
        <!-- 圖片 -->
        <div class="me-4 position-relative" style="width: 100px; height: 140px; flex-shrink: 0">
            <v-img v-if="imageUrl" :src="imageUrl" contain class="rounded" height="100%"></v-img>
            <v-sheet v-else color="grey-lighten-3" height="100%" class="d-flex align-center justify-center rounded">
                <v-icon icon="mdi-book-open-page-variant" color="grey"></v-icon>
            </v-sheet>
        </div>

        <!-- 資訊 -->
        <div class="flex-grow-1">
            <div class="d-flex justify-space-between align-start">
                <div>
                    <div class="text-h6 font-weight-bold text-primary">{{ book.bookName }}</div>
                    <div class="text-subtitle-2 text-grey-darken-1">{{ book.author }}</div>
                </div>
                <!-- 收藏按鈕 -->
                <v-btn icon variant="text" color="red" density="compact" @click.stop="toggleFavorite">
                    <v-icon :icon="isFavorited ? 'mdi-heart' : 'mdi-heart-outline'"></v-icon>
                </v-btn>
            </div>

            <div class="mt-2 text-body-2 text-grey-darken-2" style="max-width: 600px">
                {{ shortDescription }}
            </div>

            <div class="mt-2">
                <v-chip v-for="g in book.genres" :key="g.genreId" size="x-small" color="secondary" variant="flat"
                    class="me-1">
                    {{ g.genreName }}
                </v-chip>
            </div>
        </div>

        <!-- 價格與操作 -->
        <div class="d-flex flex-column align-end ms-4" style="min-width: 120px;">
            <div class="text-h5 font-weight-bold price-text mb-2">
                {{ formattedPrice }}
            </div>
            <v-btn variant="flat" color="primary" size="small" class="text-white mb-2 w-100" @click.stop="addToCart">
                <v-icon start size="small">mdi-cart-plus</v-icon>
                加入購物車
            </v-btn>
            <v-btn variant="outlined" color="primary" size="small" class="w-100">
                詳情
            </v-btn>
        </div>
    </v-card>
</template>

<style scoped>
.image-container {
    height: 200px;
    overflow: hidden;
}

/* 酒紅色文字 */
.price-text {
    color: #800020;
}

/* 卡片懸停效果 */
.card-hover {
    transition: transform 0.2s, box-shadow 0.2s;
    cursor: pointer;
}

.card-hover:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 25px 0 rgba(0, 0, 0, 0.1) !important;
}

.text-truncate-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    min-height: 40px;
    /* 約兩行高度 */
}
</style>
