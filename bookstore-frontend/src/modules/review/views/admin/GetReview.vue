<template>
  <div class="admin-page">
    <h1>評價詳細內容</h1>

    <p v-if="loading">載入中...</p>
    <p v-else-if="error" style="color: red">{{ error }}</p>

    <table class="table" v-else>
      <tbody>
        <tr>
          <th>評價編號</th>
          <td>{{ review.reviewId }}</td>
        </tr>
        <tr>
          <th>會員編號</th>
          <td>{{ review.userId }}</td>
        </tr>
        <tr>
          <th>會員名稱</th>
          <td>{{ review.userName }}</td>
        </tr>
        <tr>
          <th>書本編號</th>
          <td>{{ review.bookId }}</td>
        </tr>
        <tr>
          <th>書本名稱</th>
          <td>{{ review.bookName }}</td>
        </tr>
        <tr>
          <th>評分</th>
          <td>{{ review.rating }}</td>
        </tr>
        <tr>
          <th>評論內容</th>
          <td>{{ review.comment }}</td>
        </tr>
        <tr>
          <th>建立時間</th>
          <td>{{ review.createdAt }}</td>
        </tr>
      </tbody>
    </table>

    <div style="margin-top: 20px">
      <button class="system-button back-button" @click="goBack">回到所有評價</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const review = ref(null)
const loading = ref(true)
const error = ref(null)

// 從網址抓 reviewId
const reviewId = route.params.id

onMounted(async () => {
  try {
    const res = await fetch(`/api/public/admin/reviews/${reviewId}`)

    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`)
    }

    review.value = await res.json()
  } catch (err) {
    console.error('取得評價失敗', err)
    error.value = '讀取評價資料失敗'
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.push('/dev/admin/reviews')
}
</script>
<style>
/* ===== 整頁背景與版型 ===== */
.admin-page {
  font-family: '微軟正黑體', 'Arial', sans-serif;
  background-color: #fcf8f0;
  color: #4a4a4a;

  min-height: 100vh;
  padding: 40px 0;

  display: flex;
  flex-direction: column;
  align-items: center;
}

/* ===== 標題 ===== */
.admin-page h1 {
  text-align: center;
  font-size: 26px;
  color: #7b5e47;
  margin-bottom: 25px;
  border-bottom: 1px solid #e0d9c9;
  padding-bottom: 10px;
  width: 90%;
  max-width: 900px;
}

/* ===== 表格 ===== */
.table {
  width: 90%;
  max-width: 900px;
  border-collapse: collapse;
  background-color: #ffffff;

  border: 1px solid #dcd5c7;
  border-radius: 6px;
  overflow: hidden;

  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.table td {
  border: 1px solid #e0d9c9;
  padding: 12px 10px;
  text-align: left;
  font-size: 15px;
}

.table th {
  background-color: #e8e4dc;
  color: #5d5d5d;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
}

.table tr:nth-child(even) {
  background-color: #f7f3f0;
}

/* ===== 按鈕區塊 ===== */
.system-button {
  margin-top: 25px;
  width: 260px;
  height: 44px;
  border: none;
  border-radius: 4px;

  font-size: 15px;
  font-weight: bold;
  cursor: pointer;

  transition:
    background-color 0.3s,
    transform 0.2s,
    box-shadow 0.3s;
}

/* 回到列表 */
.back-button {
  background-color: #e8e4dc;
  color: #4a4a4a;
}

.back-button:hover {
  background-color: #dcd5c7;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.back-button:active {
  transform: translateY(0);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
}
</style>
