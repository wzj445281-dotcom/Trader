<template>
  <div style="padding:16px">
    <h2>Admin Dashboard</h2>
    <el-button type="danger" @click="loadUsers">Load Users</el-button>
    <el-table :data="users" style="width:100%;margin-top:12px">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="username" label="Username"/>
      <el-table-column prop="email" label="Email"/>
      <el-table-column prop="role" label="Role"/>
    </el-table>
  </div>
</template>
<script>
export default {
  data(){ return { users: [] }},
  methods:{
    async loadUsers(){
      const r = await window.api.get('/admin/users');
      if (r.data.code===0) this.users = r.data.data;
    }
  }
}
</script>
