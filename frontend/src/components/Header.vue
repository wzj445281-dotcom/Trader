<template>
  <el-header class="app-header">
    <div class="header-content">
      <div class="logo" @click="$router.push('/')">
        <el-icon class="logo-icon"><Goods /></el-icon>
        <span>Trader 校園二手</span>
      </div>
      <div class="nav-right">
        <template v-if="user">
          <el-button link @click="$router.push('/publish')">
            <el-icon><Plus /></el-icon> 發布閒置
          </el-button>
          <el-button link @click="$router.push('/cart')">
            <el-icon><ShoppingCart /></el-icon> 購物車
          </el-button>
          <el-button link @click="$router.push('/chat')">
            <el-icon><ChatDotRound /></el-icon> 消息
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="30" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <span class="username">{{ user.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">個人中心</el-dropdown-item>
                <el-dropdown-item command="notifications">通知中心</el-dropdown-item>
                <el-dropdown-item v-if="user.role === 'ADMIN'" command="admin">後台管理</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登入</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登入 / 註冊</el-button>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Goods, Plus, ShoppingCart, ChatDotRound, ArrowDown } from '@element-plus/icons-vue'
import { logoutApi } from '../api'

const router = useRouter()
const user = ref(null)

onMounted(() => {
  const u = localStorage.getItem('trader_user')
  if (u) {
    try {
      user.value = JSON.parse(u)
    } catch (e) {
      console.error('User parse error', e)
    }
  }
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    const ref = localStorage.getItem('trader_refresh')
    try {
      if (ref && window.api) await logoutApi(ref)
    } catch (e) {}
    localStorage.removeItem('trader_token')
    localStorage.removeItem('trader_refresh')
    localStorage.removeItem('trader_user')
    user.value = null
    router.push('/login')
    window.location.reload() // 簡單刷新狀態
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'profile') {
    router.push('/profile') // 待開發
  } else if (command === 'notifications') {
    router.push('/notifications')
  }
}
</script>

<style scoped>
.app-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
}
.header-content {
  width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
@media (max-width: 1200px) {
  .header-content {
    width: 100%;
    padding: 0 20px;
  }
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  display: flex;
  align-items: center;
  cursor: pointer;
}
.logo-icon {
  margin-right: 8px;
  font-size: 24px;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}
.username {
  margin: 0 8px;
  font-size: 14px;
}
</style>