<template>
  <el-header class="app-header">
    <div class="header-content">
      <div class="logo" @click="$router.push('/')">
        <el-icon class="logo-icon"><Goods /></el-icon>
        <span>Trader 校园二手</span>
      </div>
      <div class="nav-right">
        <template v-if="userStore.userInfo">
          <el-button link @click="$router.push('/publish')">
            <el-icon><Plus /></el-icon> 发布闲置
          </el-button>
          <el-button link @click="$router.push('/cart')">
            <el-icon><ShoppingCart /></el-icon> 购物车
          </el-button>
          <el-button link @click="$router.push('/chat')">
            <el-icon><ChatDotRound /></el-icon> 消息
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="30" :src="userStore.userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <span class="username">{{ userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="notifications">通知中心</el-dropdown-item>
                <el-dropdown-item v-if="userStore.userInfo.role === 'ADMIN'" command="admin">后台管理</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录 / 注册</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Goods, Plus, ShoppingCart, ChatDotRound, ArrowDown } from '@element-plus/icons-vue'
import { logoutApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = async (command) => {
  if (command === 'logout') {
    // 1. 尝试调用后端接口使 Refresh Token 失效
    const ref = localStorage.getItem('trader_refresh')
    if(ref) {
      try {
        await logoutApi(ref)
      } catch(e){
        console.error('Logout API failed but proceeding with local logout:', e)
        // 即使后端调用失败，也必须清除本地状态，确保登出流程完整
      }
    }

    // 2. 清除本地状态和缓存
    userStore.logout()

    // 3. 跳转到登录页
    router.push('/login')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'notifications') {
    router.push('/notifications')
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.app-header { background-color: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 100; padding: 0; }
.header-content { width: 1200px; margin: 0 auto; height: 60px; display: flex; justify-content: space-between; align-items: center; }
@media (max-width: 1200px) { .header-content { width: 100%; padding: 0 20px; } }
.logo { font-size: 20px; font-weight: bold; color: #409EFF; display: flex; align-items: center; cursor: pointer; }
.logo-icon { margin-right: 8px; font-size: 24px; }
.nav-right { display: flex; align-items: center; gap: 20px; }
.user-info { display: flex; align-items: center; cursor: pointer; color: #606266; }
.username { margin: 0 8px; font-size: 14px; }
</style>