<template>
  <div class="center-body">
    <div class="form-container">
      <h2>新增訂單</h2>

      <form @submit.prevent="handleSubmit">
        <h3>收件資訊</h3>
        <div class="form-group">
          <label>選擇使用者</label>
          <div style="display: flex; gap: 10px;">
            <select v-model="form.userId" required style="flex: 1;">
              <option value="" disabled>請選擇使用者</option>
              <option v-for="user in userList" :key="user.userId" :value="user.userId">
                {{ user.userName }} (會員編號: {{ user.userId }})
              </option>
            </select>
            <button 
                type="button" 
                class="fill-btn" 
                @click="fillUserInfo" 
                :disabled="!form.userId"
                title="自動帶入會員姓名、電話與地址"
            >
                帶入會員資料
            </button>
          </div>
        </div>

        <div class="form-group">
          <label>使用者姓名</label>
          <input type="text" v-model="form.userName" readonly class="readonly-input" placeholder="選擇使用者後自動顯示" />
        </div>

        <div class="form-group">
          <label>收件人姓名</label>
          <input type="text" v-model="form.recipientName" required />
        </div>

        <div class="form-group">
          <label>電話</label>
          <input type="text" v-model="form.phone" required />
        </div>

        <div class="form-group">
          <label>地址</label>
          <input type="text" v-model="form.address" required />
        </div>

        <div class="form-group">
          <label>配送方式</label>
          <select v-model="form.deliveryMethod" required>
            <option value="宅配到府">宅配到府</option>
            <option value="超商取貨">超商取貨</option>
          </select>
        </div>

        <div class="form-group">
          <label>付款方式</label>
          <select v-model="form.paymentMethod" required>
            <option value="信用卡">信用卡</option>
            <option value="貨到付款">貨到付款</option>
            <option value="轉帳">轉帳</option>
          </select>
        </div>

        <h3>商品資訊</h3>
        <div class="items-container">
          <div v-for="(item, index) in items" :key="index" class="item-row">
            <div class="item-header">
              <h4>商品 {{ index + 1 }}</h4>
              <button
                type="button"
                class="remove-btn"
                @click="removeItem(index)"
                v-if="items.length > 1"
              >
                移除
              </button>
            </div>

            <div class="form-group">
              <label>書籍ID:</label>
              <input
                type="text"
                v-model="item.bookId"
                @change="fetchBookInfo(item)"
                placeholder="輸入書籍 ID"
                required
              />
            </div>

            <div class="form-group">
              <label>書籍名稱:</label>
              <input type="text" v-model="item.bookName" readonly class="readonly-input" />
            </div>

            <div class="form-group">
              <label>單價:</label>
              <input type="number" v-model="item.price" readonly class="readonly-input" />
            </div>

            <div class="form-group">
              <label>數量:</label>
              <input type="number" v-model="item.quantity" min="1" required />
            </div>
          </div>
        </div>

        <button type="button" class="add-btn" @click="addItem">+ 增加一筆商品</button>

        <div class="button-group">
          <button type="submit" class="submit-btn" :disabled="isSubmitting">送出訂單</button>
          <button type="button" class="cancel-btn" @click="router.back()">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Swal from 'sweetalert2'
import orderService from '@/modules/order/api'

const router = useRouter()
const isSubmitting = ref(false)

const form = reactive({
  userId: '',
  userName: '',
  recipientName: '',
  phone: '',
  address: '',
  deliveryMethod: '宅配到府',
  paymentMethod: '信用卡',
})

const userList = ref([]) // 儲存使用者列表

const items = ref([{ bookId: '', bookName: '', price: '', quantity: 1 }])

const addItem = () => {
  items.value.push({ bookId: '', bookName: '', price: '', quantity: 1 })
}

const removeItem = (index) => {
  items.value.splice(index, 1)
}

const fetchBookInfo = async (item) => {
  if (!item.bookId) return

  try {
    const response = await orderService.getBookInfo(item.bookId)
    const book = response.data
    if (book && book.bookId) {
      if (book.onShelf !== 1) {
          Swal.fire('警告', '本此書籍已下架，無法新增', 'warning')
          item.bookName = ''
          item.price = ''
          item.bookId = '' // 清空 ID 避免誤送
          item.stock = 0
          item.onShelf = 0
          return
      }
      item.bookName = book.bookName
      item.price = book.price
      item.stock = book.stock
      item.onShelf = book.onShelf
    } else {
      Swal.fire('錯誤', '找不到此書籍ID', 'error')
      item.bookName = ''
      item.price = ''
      item.bookId = ''
      item.stock = 0
      item.onShelf = 0
    }
  } catch (error) {
    console.error(error)
    Swal.fire('錯誤', '查詢書籍失敗', 'error')
  }
}

const fetchUsers = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/data/list')
    userList.value = response.data
  } catch (error) {
    console.error('Failed to fetch users', error)
    Swal.fire('錯誤', '無法取得使用者列表', 'error')
  }
}

const fillUserInfo = async () => {
    if (!form.userId) return

    try {
        const response = await axios.get(`http://localhost:8080/api/data/get/${form.userId}`)
        const user = response.data
        if (user) {
            form.userName = user.userName // Also update displayed user name
            form.recipientName = user.userName || ''
            form.phone = user.phoneNum || ''
            form.address = user.address || ''
            
            Swal.fire({
                icon: 'success',
                title: '已帶入會員資料',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 1500
            })
        }
    } catch (error) {
        console.error(error)
        Swal.fire('錯誤', '無法取得會員詳細資料', 'error')
    }
}

const handleSubmit = async () => {
  isSubmitting.value = true
  
  // 確保所有有 bookId 的項目都已獲取資訊
  for (const item of items.value) {
      if (item.bookId && (!item.bookName || item.onShelf === undefined)) {
          await fetchBookInfo(item);
      }
  }

  // 前端驗證
  for (const item of items.value) {
      if (!item.bookId) continue;
      
      if (item.onShelf !== 1) {
          Swal.fire('錯誤', `書籍 ${item.bookName || item.bookId} 已下架或無效，無法建立訂單`, 'error')
          isSubmitting.value = false
          return
      }
      
      if (item.quantity > item.stock) {
          Swal.fire('錯誤', `書籍 ${item.bookName} 庫存不足 (剩餘 ${item.stock})`, 'error')
          isSubmitting.value = false
          return
      }
  }

  try {
    // 建立訂單資料物件
    const orderData = {
        userId: form.userId,
        recipientName: form.recipientName,
        phone: form.phone,
        address: form.address,
        deliveryMethod: form.deliveryMethod,
        paymentMethod: form.paymentMethod
    }

    // 過濾掉空項目
    const validItems = items.value.filter(item => item.bookId);
    
    if (validItems.length === 0) {
        Swal.fire('提示', '請至少加入一項商品', 'warning');
        isSubmitting.value = false;
        return;
    }

    const response = await orderService.createOrder(orderData, validItems)

    if (response.data.success) {
      Swal.fire({
        icon: 'success',
        title: '訂單建立成功',
        timer: 1500,
      }).then(() => {
        router.push({ name: 'orderList' })
      })
    } else {
      Swal.fire('錯誤', '建立失敗: ' + (response.data.message || '未知錯誤'), 'error')
    }
  } catch (error) {
    console.error(error);
    Swal.fire('錯誤', '系統錯誤: ' + error.message, 'error')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.center-body {
  font-family: '微軟正黑體', 'Arial', sans-serif;
  background-color: #fcf8f0;
  display: flex;
  justify-content: center;
  padding: 40px 0;
  min-height: 100vh;
}

.form-container {
  width: 600px;
  max-width: 90%;
  padding: 30px;
  border: 1px solid #dcd5c7;
  border-radius: 6px;
  background-color: #ffffff;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

h2 {
  color: #7b5e47;
  text-align: center;
  margin-bottom: 25px;
  border-bottom: 1px solid #e0d9c9;
  padding-bottom: 10px;
}

h3 {
  color: #7b5e47;
  margin-top: 20px;
  margin-bottom: 15px;
  border-bottom: 1px dashed #e0d9c9;
  padding-bottom: 5px;
}

.form-group {
  margin-bottom: 15px;
  text-align: left;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #5d5d5d;
}

input[type='text'],
input[type='number'],
select {
  width: 100%;
  padding: 10px;
  border: 1px solid #d0c8b9;
  border-radius: 4px;
  background-color: #fefcf9;
  box-sizing: border-box;
}

.readonly-input {
  background-color: #eee !important;
  color: #555;
  cursor: not-allowed;
}

.item-row {
  border: 1px solid #dcd5c7;
  background-color: #fffaf0;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 4px;
  position: relative;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: 1px dashed #ccc;
  padding-bottom: 5px;
}

h4 {
  margin: 0;
  color: #4a4a4a;
}

.remove-btn {
  background-color: #d89696;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.fill-btn {
  background-color: #7a9c84;
  color: white;
  border: none;
  padding: 0 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}
.fill-btn:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}
.fill-btn:hover:not(:disabled) {
    background-color: #5d7a66;
}

.add-btn {
  width: 100%;
  padding: 10px;
  background-color: #9fb89e;
  color: #4a4a4a;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 25px;
}

button.submit-btn,
button.cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  min-width: 100px;
}

.submit-btn {
  background-color: #a07d58;
  color: white;
}
.cancel-btn {
  background-color: #e8e4dc;
  color: #4a4a4a;
}
</style>
