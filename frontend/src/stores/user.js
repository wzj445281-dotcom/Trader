import { defineStore } from 'pinia'
import { ref } from 'vue'

// 报告 2.3.2 节：Pinia UserStore
export const useUserStore = defineStore('user', () => {
    // 从 localStorage 初始化状态
    const token = ref(localStorage.getItem('trader_token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('trader_user') || 'null'))

    function login(data) {
        token.value = data.token
        userInfo.value = data.user
        // 存储到 localStorage
        localStorage.setItem('trader_token', data.token)
        localStorage.setItem('trader_refresh', data.refresh) // 假设后端返回 refresh token
        localStorage.setItem('trader_user', JSON.stringify(data.user))
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        // 清除 localStorage
        localStorage.removeItem('trader_token')
        localStorage.removeItem('trader_refresh')
        localStorage.removeItem('trader_user')
    }

    // 暴露状态和方法
    return { token, userInfo, login, logout }
})