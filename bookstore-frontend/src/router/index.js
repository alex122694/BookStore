import { createRouter, createWebHistory } from 'vue-router'
import Swal from 'sweetalert2'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dev/user/home',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/admin/LoginView.vue'),
      meta: { title: '後台管理系統' },
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('@/views/admin/Home.vue'),
      meta: { title: '後台系統選單' },
    },
    // --- 後台管理區域 ---
    {
      path: '/dev/admin',
      component: () => import('@/views/Layout/AdminLayout.vue'),
      children: [
        // 1. 書籍管理
        {
          path: 'books',
          name: 'admin-books',
          component: () => import('@/modules/book/views/admin/BooksHome.vue'),
          meta: { title: '書籍管理列表' },
        },
        {
          path: 'books/insert',
          name: 'admin-books-insert',
          component: () => import('@/modules/book/views/admin/insertBook.vue'),
          meta: { title: '新增書籍資料' },
        },
        {
          path: 'books/update/:id',
          name: 'admin-books-update',
          component: () => import('@/modules/book/views/admin/updateBook.vue'),
          meta: { title: '修改書籍資料' },
        },
        {
          path: 'books/get/:id',
          name: 'admin-books-get',
          component: () => import('@/modules/book/views/admin/getBook.vue'),
          meta: { title: '書籍詳細資料' },
        },

        // 進貨管理
        {
          path: 'logs',
          name: 'admin-logs',
          component: () => import('@/modules/stocklog/views/admin/StockLogsHome.vue'),
          meta: { title: '進退貨管理' },
        },
        {
          path: 'logs/:id',
          name: 'admin-logs-detail',
          component: () => import('@/modules/stocklog/views/admin/StockLogDetail.vue'),
          meta: { title: '單據明細' },
        },
        {
          path: 'logs/insert',
          name: 'admin-logs-insert',
          component: () => import('@/modules/stocklog/views/admin/InsertStockLog.vue'),
          meta: { title: '新增進貨單' },
        },
        // 2. 用戶管理
        {
          path: 'users',
          name: 'userList',
          component: () => import('@/modules/user/views/admin/UserList.vue'),
          meta: { title: '會員管理列表' },
        },
        {
          path: 'users/get/:id',
          name: 'userDetail',
          component: () => import('@/modules/user/views/admin/GetUser.vue'),
          meta: { title: '會員詳細資料' },
        },
        {
          path: 'users/insert',
          name: 'userInsert',
          component: () => import('@/modules/user/views/admin/UserInsert.vue'),
          meta: { title: '新增會員資料' },
        },
        {
          path: 'users/update/:id',
          name: 'userUpdate',
          component: () => import('@/modules/user/views/admin/UserUpdate.vue'),
          meta: { title: '會員資料管理' },
        },
        {
          path: 'users/logs',
          name: 'admin-operation-logs',
          component: () => import('@/modules/user/views/admin/UserLogList.vue'),
          meta: { title: '管理員操作日誌' },
        },
        {
          path: 'users/admin-chat',
          name: 'admin-chat',
          component: () => import('@/modules/user/views/admin/AdminChatView.vue'),
          meta: { title: '管理員客服中心' },
        },
        // 2.5 優惠券管理
        {
          path: 'coupons',
          name: 'admin-coupons',
          component: () => import('@/modules/coupon/views/admin/AdminCoupon.vue'),
          meta: { title: '優惠券管理' },
        },
        // 3. 訂單管理
        {
          path: 'orders/list',
          name: 'orderList',
          component: () => import('@/modules/order/views/admin/OrderList.vue'),
          meta: { title: '訂單管理列表' },
        },
        {
          path: 'orders/analysis',
          name: 'orderAnalysis',
          component: () => import('@/modules/order/views/admin/OrderAnalysis.vue'),
          meta: { requiresAuth: true, role: 'ADMIN', title: '訂單數據分析'},
        },
        {
          path: 'orders/insert',
          name: 'orderInsert',
          component: () => import('@/modules/order/views/admin/OrderInsert.vue'),
          meta: { title: '新增訂單' },
        },
        {
          path: 'orders/detail/:id',
          name: 'orderDetail-admin',
          component: () => import('@/modules/order/views/admin/OrderDetail.vue'),
          meta: { title: '訂單明細' },
        },
        {
          path: 'logs/update/:id',
          name: 'admin-logs-update',
          component: () => import('@/modules/stocklog/views/admin/updateLogDetail.vue'),
          meta: { title: '修改進退貨單' },
        },
        {
          path: 'reports',
          name: 'admin-reports',
          component: () => import('@/modules/stocklog/views/admin/SalesData.vue'),
          meta: { title: '數據報表分析' },
        },
        {
          path: 'bookclubs/insert',
          name: 'admin-bookclubs-insert',
          component: () => import('@/modules/bookclub/views/admin/insertBookClub.vue'),
          meta: { title: '新增讀書會' },
        },
        {
          path: 'bookclubs',
          name: 'admin-bookclubs',
          component: () => import('@/modules/bookclub/views/admin/AdminBookClub.vue'),
          meta: { title: '讀書會管理' },
        },
        {
          path: 'bookclubs/review/:id',
          name: 'admin-bookclubs-review',
          component: () => import('@/modules/bookclub/views/admin/AdminBookClubDetail.vue'),
          meta: { title: '讀書會審核' },
        },
        {
          path: 'orders/items/add/:id',
          name: 'orderAddItem',
          component: () => import('@/modules/order/views/admin/OrderAddItem.vue'),
          meta: { title: '新增訂單明細' },
        },
        // 4.評價管理
        {
          path: 'reviews',
          name: 'admin-reviews',
          component: () => import('@/modules/review/views/admin/ReviewManagement.vue'),
          meta: { title: '書籍評價'},
        },
        {
          path: 'reviews/book/:bookId',
          name: 'admin-book-reviews',
          component: () => import('@/modules/review/views/admin/ReviewList.vue'),
          meta: { title: '評價資料' },
        },
        {
          path: 'reviews/:id',
          name: 'review-detail',
          component: () => import('@/modules/review/views/admin/GetReview.vue'),
          meta: { title: '評價詳細內容' },
        },
        {
          path: 'reviews/insert',
          name: 'review-insert',
          component: () => import('@/modules/review/views/admin/ReviewInsert.vue'),
          meta: { title: '新增評價' },
        },
        {
          path: 'reviews/:id/update',
          name: 'review-update',
          component: () => import('@/modules/review/views/admin/ReviewUpdate.vue'),
          meta: { title: '修改評價' },
        },
        // 5.檢舉管理
        {
          path: 'reviews/reports',
          name: 'admin-review-reports',
          component: () => import('@/modules/review/views/admin/ReportList.vue'),
          meta: { title: '檢舉列表' },
        },
      ],
    },
    // --- 前台網站區域 ---
    {
      path: '/dev/user',
      component: () => import('@/views/Layout/UserLayout.vue'),
      children: [
        {
          path: 'about-us',
          name: 'about-us',
          component: () => import('@/views/public/AboutUs.vue'),
          meta: { title: '關於我們' },
        },
        {
          path: 'login',
          name: 'user-login',
          component: () => import('@/modules/user/views/public/UserLogin.vue'),
          meta: { title: '會員登入' },
        },
        {
          path: 'user-menu',
          name: 'user-menu',
          component: () => import('@/modules/user/views/public/UserMenu.vue'),
          meta: { title: '會員中心' },
        },
        {
          path: 'register',
          name: 'user-register',
          component: () => import('@/modules/user/views/public/UserRegister.vue'),
          meta: { title: '註冊會員' },
        },
        {
          path: 'forgetpassword',
          name: 'user-forget-password',
          component: () => import('@/modules/user/views/public/UserForgetPwd.vue'),
          meta: { title: '重設密碼' },
        },
        {
          path: 'reset-password-by-email',
          name: 'user-reset-password-by-email',
          component: () => import('@/modules/user/views/public/ResetPasswordByEmail.vue'),
          meta: { title: '驗證碼確認' },
        },
        {
          path: 'set-new-password',
          name: 'set-new-password',
          component: () => import('@/modules/user/views/public/SetNewPassword.vue'),
          meta: { title: '設定新密碼' },
        },
        {
          path: 'profile/password-confirmation',
          name: 'password-confirmation',
          component: () => import('@/modules/user/views/public/PasswordConfirmation.vue'),
          meta: { title: '密碼確認' },
        },
        {
          path: 'profile-edit',
          name: 'profile-edit',
          component: () => import('@/modules/user/views/public/UserProfileEdit.vue'),
          meta: { title: '會員資料修改' },
        },
        {
          path: 'user-chat',
          name: 'user-chat',
          component: () => import('@/modules/user/views/public/UserChat.vue'),
          meta: { title: '客服專區' },
        },
        {
          path: 'history',
          name: 'view-history',
          component: () => import('@/modules/user/views/public/BrowsingHistory.vue'),
          meta: { title: '書籍瀏覽紀錄' },
        },
        {
          path: 'wishlist',
          name: 'wishlist',
          component: () => import('@/modules/user/views/public/WishList.vue'),
          meta: { title: '書籍收藏清單' },
        },
        {
          path: 'books',
          name: 'user-books',
          component: () => import('@/modules/book/views/public/UserBookList.vue'),
          meta: { title: '書籍專區' },
        },
        {
          path: 'books/:id',
          name: 'user-book-detail',
          component: () => import('@/modules/book/views/public/UserBookDetail.vue'),
          meta: { title: '書籍詳細資訊' },
        },
        {
          path: 'store',
          name: 'bookStore',
          component: () => import('@/modules/book/views/public/UserBookList.vue'), //測試購物車用，等宏孝加入書籍前台網頁後刪除
          meta: { title: '書籍專區' },
        },
        {
          path: 'home',
          name: 'userHome',
          component: () => import('@/views/public/HomePage.vue'),
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('@/modules/cart/views/Cart.vue'),
          meta: { title: '我的購物車' },
        },
        {
          path: 'checkout',
          name: 'checkout',
          component: () => import('@/modules/order/views/public/Checkout.vue'),
          meta: { title: '結帳確認' },
        },
        {
          path: 'orders/success',
          name: 'orderSuccess',
          component: () => import('@/modules/order/views/public/OrderSuccess.vue'),
          meta: { title: '訂單完成' },
        },
        {
          path: 'orders',
          name: 'myOrders',
          component: () => import('@/modules/order/views/public/MyOrders.vue'),
          meta: { title: '歷史訂單' },
        },
        {
          path: 'coupons',
          name: 'userCoupons',
          component: () => import('@/modules/user/views/public/UserCoupon.vue'),
          meta: { title: '我的優惠券' },
        },
        {
          path: 'reviews', 
          name: 'user-reviews',
          component: () => import('@/modules/review/views/public/BookReviewsHome.vue'),
          meta: { title: '讀者書評' }, 
        },
        {
          path: 'myreviews',
          name: 'my-reviews',
          component: () => import('@/modules/review/views/public/MyReviews.vue')
        },  
        // 讀書會專區
        {
          path: 'bookclubs',
          name: 'user-bookclubs',
          component: () => import('@/modules/bookclub/views/public/UserBookClub.vue'),
          meta: { title: '讀書會專區' },
        },
        {
          path: 'bookclubs/insert',
          name: 'user-bookclubs-insert',
          component: () => import('@/modules/bookclub/views/public/UserInsertBookClub.vue'),
          meta: { title: '新增讀書會' },
        },
        {
          path: 'bookclubs/detail/:id',
          name: 'user-bookclub-detail-page',
          component: () => import('@/modules/bookclub/views/public/UserBookClubDetail.vue'),
          meta: { title: '讀書會詳細資訊' },
        },
      ],
    },
  ],
})

// 路由守衛
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('userToken')
  const role = localStorage.getItem('userRole')

  const pageTitle = to.meta.title
  document.title = pageTitle ? `${pageTitle} | 森林書屋` : '森林書屋'

  const isAdminRoute = to.path.startsWith('/dev/admin') || to.name === 'home'
  const isUserArea = to.path.startsWith('/dev/user')
  const isUserProtectedRoute = [
    'myOrders',
    'checkout',
    'cart',
    'userCoupons',
    'profile-edit',
    'password-confirmation',
    'my-reviews',
  ].includes(to.name)
  const isLoginPage = to.name === 'user-login'

  if (isLoginPage && token) {
    return next({ name: 'userHome' })
  }

  if (isAdminRoute) {
    if (!token) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
    if (role === 'SUPER_ADMIN' || role === 'ADMIN') {
      return next()
    } else {
      Swal.fire('權限不足', '您沒有進入後台管理系統的權限', 'error')
      return next({ name: 'userHome' })
    }
  }

  if (isUserProtectedRoute && !token) {
    Swal.fire({
      title: '請先登入',
      text: '登入會員後即可使用此功能',
      icon: 'info',
      confirmButtonColor: '#2e5c43',
    })
    return next({ name: 'user-login', query: { redirect: to.fullPath } })
  }

  if (isUserArea && !token && isUserProtectedRoute) {
    return next({ name: 'user-login'})
  }

  next()
})

export default router
