<template>
  <div class="admin-page">
    <h1 class="page-title">新增評價</h1>

    <table class="detail-table">
      <tbody>
        <tr>
          <td class="label-cell">會員編號</td>
          <td class="value-cell">
            <input class="form-input" v-model="form.userId" placeholder="必填" />
          </td>
        </tr>

        <tr>
          <td class="label-cell">書籍編號</td>
          <td class="value-cell">
            <input class="form-input" v-model="form.bookId" placeholder="必填" />
          </td>
        </tr>

        <tr>
          <td class="label-cell">書本評分</td>
          <td class="value-cell">
            <select class="form-select" v-model="form.rating">
              <option value="">請選擇</option>
              <option v-for="n in 5" :key="n" :value="n">{{ n }}</option>
            </select>
          </td>
        </tr>

        <tr>
          <td class="label-cell">評價</td>
          <td class="value-cell">
            <textarea class="form-textarea" v-model="form.comment" rows="4"></textarea>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="button-group">
      <button class="system-button add-button" @click.prevent="submit">新增評價</button>
      <button class="system-button back-button" @click="goBack">返回所有評價</button>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({
  userId: '',
  bookId: '',
  rating: '',
  comment: '',
})

const submit = async () => {
  // ✅ reactive → 直接用 form.xxx
  if (!form.userId || !form.bookId || !form.rating || !form.comment) {
    alert('欄位不能為空')
    return
  }

  try {
    const res = await fetch('/api/public/admin/reviews', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        userId: Number(form.userId),
        bookId: Number(form.bookId),
        rating: Number(form.rating),
        comment: form.comment,
      }),
    })

    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`)
    }

    alert('新增成功')
    router.push('/dev/admin/reviews')
  } catch (err) {
    console.error('新增評價失敗', err)
    alert('新增失敗，無此會員或書籍')
  }
}

const goBack = () => {
  router.push('/dev/admin/reviews')
}
</script>

<style>
/* ===== 表格本體 ===== */
.detail-table {
  width: 100%; /* 🔥 關鍵 1：一定要有 */
  max-width: 900px;
  margin: 0 auto;
  border-collapse: collapse;
  background-color: #ffffff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

/* ===== 左欄（標題） ===== */
.label-cell {
  width: 180px; /* 🔥 關鍵 2：固定左欄寬度 */
  background-color: #e9e5dd;
  color: #4a4a4a;
  font-weight: bold;
  padding: 14px 16px;
  border-bottom: 1px solid #ddd;
  vertical-align: middle;
}

/* ===== 右欄（內容） ===== */
.value-cell {
  padding: 14px 16px;
  border-bottom: 1px solid #eee;
  background-color: #fff;
}

/* ===== 表單元件一定要撐滿 ===== */
.form-input,
.form-select,
.form-textarea {
  width: 100%; /* 🔥 關鍵 3：沒有這行一定炸 */
  box-sizing: border-box;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}
/* 頁面整體 */
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

/* 標題 */
.page-title {
  text-align: center;
  font-size: 26px;
  color: #7b5e47;
  margin-bottom: 25px;
  border-bottom: 1px solid #e0d9c9;
  padding-bottom: 10px;
  width: 90%;
  max-width: 900px;
}

/* 中央卡片 */
.center-container {
  width: 720px;
  margin: 0 auto;
  padding: 25px;
  background-color: #ffffff;
  border: 1px solid #dcd5c7;
  border-radius: 6px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

/* 表單 table */
.form-table {
  width: 100%;
  border-collapse: collapse;
}

/* 左側欄位 */
.label-cell {
  width: 180px;
  background-color: #e8e4dc;
  padding: 14px;
  font-weight: bold;
  color: #5d5d5d;
  vertical-align: top;
  text-align: center;
}

/* 右側輸入 */
.input-cell {
  padding: 14px;
}

/* input / select / textarea */
.input-cell input,
.input-cell select,
.input-cell textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #dcd5c7;
  border-radius: 4px;
  font-size: 14px;
}

/* textarea 高度 */
.input-cell textarea {
  resize: vertical;
  min-height: 90px;
}

/* 按鈕區 */
.button-group {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 系統按鈕 */
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

/* 新增 */
.add-button {
  background-color: #a07d58;
  color: white;
}

/* 返回 */
.back-button {
  background-color: #e8e4dc;
  color: #4a4a4a;
}
</style>
