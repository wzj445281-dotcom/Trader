<template>
  <el-container class="app-container">
    <Header />
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    <el-footer class="app-footer">
      <p>&copy; 2025 校園二手交易平台 Trader. All rights reserved.</p>
    </el-footer>
  </el-container>
</template>

<script>
import Header from './components/Header.vue';
export default { components: { Header } }
</script>

<style>
body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f7fa;
}
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
@media (max-width: 1200px) {
  .main-content {
    width: 100%;
  }
}
.app-footer {
  text-align: center;
  color: #909399;
  padding: 20px 0;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>