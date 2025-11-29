<template>
  <el-container>
    <Header />
    <el-main>
      <router-view/>
    </el-main>
  </el-container>
</template>
<script>
import Header from './components/Header.vue';
export default { components: { Header } }
</script>
