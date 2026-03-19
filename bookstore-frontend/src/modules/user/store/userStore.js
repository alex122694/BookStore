import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useUserStore = defineStore('user', () => {
  const name = ref(localStorage.getItem('userName') || '訪客')
  const role = ref(localStorage.getItem('userRole') || '')
  const isLoggedIn = ref(!!localStorage.getItem('userToken'))
  const userId = ref(localStorage.getItem('userId') || null)
  const img = ref(localStorage.getItem('userImg') || '')
  const unreadCount = ref(0)

  const updateUserImg = (newImg) => {
    img.value = newImg || ''
    if (newImg) {
      localStorage.setItem('userImg', newImg)
    } else {
      localStorage.removeItem('userImg')
    }
  }

  const login = async (userData) => {
    img.value = ''
    localStorage.removeItem('userImg')

    name.value = userData.userName
    role.value = userData.role
    userId.value = userData.userId
    isLoggedIn.value = true

    localStorage.setItem('userToken', userData.token)
    localStorage.setItem('userRole', userData.role)
    localStorage.setItem('userName', userData.userName)
    localStorage.setItem('userId', userData.userId)
    
    if (userData.img) {
      updateUserImg(userData.img)
    }

    await syncUserProfile()
    fetchUnreadCount()
  }

const syncUserProfile = async () => {
    if (!userId.value) return
    try {
      const res = await axios.get(`http://localhost:8080/api/user/my-profile?t=${Date.now()}`)
      if (res.data.success && res.data.user) {
        updateUserImg(res.data.user.img)
      }
    } catch (error) {
      console.error("同步個人資料失敗", error)
    }
  }

  const logout = () => {
    name.value = '訪客'
    role.value = ''
    isLoggedIn.value = false
    userId.value = null
    img.value = ''
    unreadCount.value = 0 
    localStorage.clear()
    window.location.href = '/dev/user/home?logout=true'
  }

  const fetchUnreadCount = async () => {
    if (!userId.value || !isLoggedIn.value) return
    try {
      const res = await axios.get(`http://localhost:8080/api/chat/unread/${userId.value}`)
      const count = typeof res.data === 'object' ? res.data.count : res.data
      unreadCount.value = parseInt(count) || 0
    } catch (error) {
      console.error("更新未讀數失敗", error)
    }
  }

  const incrementUnreadCount = async () => {
    await fetchUnreadCount();
  }

  const setUnreadCount = (count) => {
    unreadCount.value = count
  }

  return { 
    name, role, isLoggedIn, userId, img, 
    unreadCount, 
    login, logout, fetchUnreadCount, incrementUnreadCount, setUnreadCount, syncUserProfile, updateUserImg
  }
})