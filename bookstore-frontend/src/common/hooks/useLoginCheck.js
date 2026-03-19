import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { useUserStore } from '@/modules/user/store/userStore'

export function useLoginCheck() {
  const router = useRouter()
  const userStore = useUserStore()

  /**
   * Checks if the user is logged in.
   * If logged in, executes the callback.
   * If not, shows a warning and redirects to login.
   *
   * @param {Function} action - The callback to execute if logged in.
   * @param {String} message - Custom warning message (optional).
   */
  const checkLogin = (action, message = '您必須先登入帳號才能繼續操作。') => {
    if (userStore.isLoggedIn) {
      if (action && typeof action === 'function') {
        action()
      }
      return true
    }

    Swal.fire({
      title: '請先登入',
      text: message,
      icon: 'warning',
      confirmButtonText: '前往登入',
      confirmButtonColor: '#2E5C43',
    }).then((result) => {
      if (result.isConfirmed) {
        router.push('/dev/user/login')
      }
    })
    return false
  }

  /**
   * Just checks login and returns boolean, handles redirect if false.
   * Useful for async flows or where callback style isn't preferred.
   */
  const validateLogin = async (message = '您必須先登入帳號才能繼續操作。') => {
    if (userStore.isLoggedIn) {
      return true
    }

    await Swal.fire({
      title: '請先登入',
      text: message,
      icon: 'warning',
      confirmButtonText: '前往登入',
      confirmButtonColor: '#2E5C43',
    }).then((result) => {
      if (result.isConfirmed) {
        router.push('/dev/user/login')
      }
    })
    return false
  }

  return {
    checkLogin,
    validateLogin,
  }
}
