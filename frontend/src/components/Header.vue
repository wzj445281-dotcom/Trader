<template>
  <el-header style="display:flex;justify-content:space-between;align-items:center;padding:8px 16px">
    <div style="font-weight:bold">Trader</div>
    <div>
      <template v-if="user">
        <span style="margin-right:12px">Hello, {{user.username}}</span>
        <el-button size="small" @click="toPublish">Publish</el-button>\n        <el-button size="small" @click="$router.push('/analytics')">Analytics</el-button>\n        <el-button size="small" v-if="user && user.role==='ADMIN'" @click="$router.push('/admin')">Admin</el-button>
        <el-button size="small" type="danger" @click="logout">Logout</el-button>
      </template>
      <template v-else>
        <el-button size="small" @click="$router.push('/login')">Login</el-button>
        <el-button size="small" @click="$router.push('/publish')">Publish</el-button>
      </template>
    </div>
  </el-header>
</template>
<script>
export default {
  data(){ return { user: null }},
  mounted(){ const u = localStorage.getItem('trader_user'); if (u) this.user = JSON.parse(u); },
  methods:{
    async logout(){
      const ref = localStorage.getItem('trader_refresh');
      try { if (ref && window.api) await window.api.post('/auth/logout', { refresh: ref }); } catch(e){}
      localStorage.removeItem('trader_token');
      localStorage.removeItem('trader_refresh');
      localStorage.removeItem('trader_user');
      this.user = null;
      this.$router.push('/');
    },
    toPublish(){ this.$router.push('/publish') }
  }
}
</script>
