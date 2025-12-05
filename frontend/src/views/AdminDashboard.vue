<template>
  <div style="padding:16px">
    <h2>管理后台</h2>
    <el-button type="primary" plain @click="loadUsers">刷新用户列表</el-button>
    <el-table :data="users" style="width:100%;margin-top:16px" border stripe>
      <el-table-column prop="id" label="用户ID" width="120" sortable/>
      <el-table-column prop="username" label="用户名"/>
      <el-table-column prop="email" label="电子邮箱"/>
      <el-table-column prop="phone" label="手机号"/>
      <el-table-column prop="role" label="权限角色">
        <template #default="scope">
          <el-tag :type="scope.row.role==='ADMIN'?'danger':'info'">{{scope.row.role||'USER'}}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
export default {
  data(){ return { users: [] }},
  methods:{
    async loadUsers(){
      try {
        const r = await window.api.get('/admin/users'); // 需确认后端是否有此接口，原代码有此调用
        if (r.data.code===0) this.users = r.data.data;
      } catch(e){
        this.$message.warning("演示模式：无法获取用户列表");
      }
    }
  }
}
</script>