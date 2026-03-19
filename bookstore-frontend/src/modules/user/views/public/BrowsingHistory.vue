<template>
  <v-container class="py-10">
    <v-row class="mb-2">
      <v-col cols="12">
        <v-btn
          variant="text"
          prepend-icon="mdi-arrow-left"
          color="primary"
          class="font-weight-bold"
          @click="goBack"
        >
          返回上一頁
        </v-btn>
      </v-col>
    </v-row>

    <div class="d-flex align-center mb-8">
      <v-icon icon="mdi-history" color="primary" size="36" class="mr-3"></v-icon>
      <h2 class="forest-main-title">我的瀏覽紀錄</h2>
      <v-spacer></v-spacer>
      <v-btn 
        variant="outlined" color="error" prepend-icon="mdi-trash-can-outline"
        @click="clearHistory" v-if="historyList.length > 0"
        class="rounded-lg"
      >
        清空紀錄
      </v-btn>
    </div>

    <v-card v-if="historyList.length === 0" class="pa-10 text-center rounded-xl elevation-1 border-forest-top">
      <v-icon icon="mdi-book-open-variant" size="64" color="grey-lighten-2" class="mb-4"></v-icon>
      <div class="text-h6 text-grey">目前沒有任何瀏覽紀錄</div>
      <v-btn color="primary" class="mt-6 rounded-lg" to="/dev/user/books">去逛逛書屋</v-btn>
    </v-card>

    <v-row v-else>
      <v-col 
        v-for="item in historyList" 
        :key="item.historyId" 
        cols="12" sm="6" md="4" lg="3" xl="2"
      >
        <v-hover v-slot="{ isHovering, props }">
          <v-card
            v-bind="props"
            :elevation="isHovering ? 8 : 2"
            class="history-card h-100 d-flex flex-column rounded-lg transition-swing"
            @click="goToBook(item.book.bookId)"
          >
            <v-img
              :src="getBookImg(item)"
              height="250"
              cover
              class="bg-grey-lighten-4"
            >
              <template v-slot:placeholder>
                <div class="d-flex align-center justify-center fill-height">
                  <v-icon color="grey-lighten-2" size="48">mdi-book-image</v-icon>
                </div>
              </template>
            </v-img>

            <v-card-text class="pa-4 flex-grow-1">
              <div class="text-subtitle-1 font-weight-bold text-forest text-truncate-2 mb-2">
                {{ item.book.bookName }}
              </div>
              <div class="d-flex align-center mb-2">
                <v-chip size="x-small" color="secondary" variant="flat" class="font-weight-bold">
                  {{ item.book.author }}
                </v-chip>
              </div>
              <div class="text-caption text-grey d-flex align-center">
                <v-icon size="14" class="mr-1">mdi-clock-outline</v-icon>
                {{ formatDateTime(item.browsedAt) }}
              </div>
            </v-card-text>

            <v-divider></v-divider>
            
            <v-card-actions class="bg-grey-lighten-5 py-1 justify-center">
              <span class="text-caption text-primary font-weight-bold">再次查看</span>
            </v-card-actions>
          </v-card>
        </v-hover>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import axios from 'axios'; 

const router = useRouter();
const historyList = ref([]);
const userId = localStorage.getItem('userId'); 

const loadHistory = async () => {
  if (!userId) return;
  try {
    const res = await axios.get(`http://localhost:8080/api/history/list/${userId}`);
    historyList.value = res.data; 
  } catch (error) {
    console.error("載入紀錄失敗", error);
  }
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/dev/user/books');
  }
};

const getBookImg = (item) => {
  const fileName = item.book?.bookImageBean?.imageUrl;
  
  if (!fileName) return '';

  return `http://localhost:8080/upload-images/${fileName}`;
};

const formatDateTime = (val) => {
  if (!val) return '未知時間';
  const date = new Date(val);
  // 只顯示日期和小時，網格較擠，不建議顯示太長
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
};

const goToBook = (id) => {
  router.push(`/dev/user/books/${id}`); 
};

const clearHistory = () => {
  Swal.fire({
    title: '確定要清空紀錄嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確定清空',
    cancelButtonColor: '#aaa',
    confirmButtonColor: '#d33',
  }).then(async (result) => {
    if (result.isConfirmed) {
      try {
        await axios.delete(`http://localhost:8080/api/history/clear/${userId}`);
        historyList.value = []; 
        Swal.fire('已清空', '', 'success');
      } catch (e) {
        Swal.fire('錯誤', '資料庫連線失敗', 'error');
      }
    }
  });
};

onMounted(loadHistory);
</script>

<style scoped>
.forest-main-title {
  color: #2e5c43;
  font-weight: 800;
}
.text-forest {
  color: #2e5c43;
}
.border-forest-top {
  border-top: 5px solid #2e5c43;
}
.history-card {
  cursor: pointer;
  border: 1px solid #f0f0f0;
}
.history-card:hover {
  border-color: #2e5c43;
}

/* 讓書名最多顯示兩行，超過顯示省略號 */
.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;  
  overflow: hidden;
  line-height: 1.4;
  height: 2.8em; /* 確保高度一致 */
}

.transition-swing {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}
</style>