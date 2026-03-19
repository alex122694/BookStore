<template>
  <div class="center-body">
    <div class="success-container">
      <div class="icon-wrapper">
        <div class="success-icon">✔</div>
      </div>
      <h2>訂單完成</h2>
      <p class="message">
        感謝您的訂購！我們已成功收到您的訂單。<br />
        請至「會員中心」追蹤您的訂單後續進度。
      </p>
      
      <div class="action-buttons">
        <button class="btn-primary" @click="goToOrders">查看訂單</button>
        <button class="btn-secondary" @click="goToHome">回到首頁</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToOrders = () => {
  router.push({ name: 'myOrders' })
}

const goToHome = () => {
  router.push({ name: 'userHome' })
}
</script>

<style scoped>
.center-body {
  font-family: '微軟正黑體', 'Arial', sans-serif;
  background-color: #fcf8f0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.success-container {
  background-color: #ffffff;
  padding: 50px 40px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.icon-wrapper {
  margin-bottom: 25px;
}

.success-icon {
  width: 80px;
  height: 80px;
  background-color: #2E5A44;
  color: white;
  font-size: 50px;
  line-height: 80px;
  border-radius: 50%;
  margin: 0 auto;
}

h2 {
  color: #2E5A44;
  margin-bottom: 15px;
  font-size: 28px;
  font-weight: 900;
}

.message {
  color: #555;
  font-size: 16px;
  font-weight: bold;
  line-height: 1.6;
  margin-bottom: 35px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

button {
  padding: 12px 25px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 120px;
}

.btn-primary {
  background-color: #2E5A44;
  color: white;
  border: none;
}

.btn-primary:hover {
  background-color: #3E795B;
}

.btn-secondary {
  background-color: transparent;
  color: #666;
  border: 1px solid #ccc;
}

.btn-secondary:hover {
  background-color: #f5f5f5;
  border-color: #bbb;
}
</style>
