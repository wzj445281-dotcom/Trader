<template>
  <div class="admin-container">
    <div class="header">
      <h2>后台管理控制台</h2>
      <el-tag type="danger">管理员模式</el-tag>
    </div>

    <el-tabs type="border-card" v-model="activeTab">

      <!-- 用户管理模块 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="toolbar">
          <el-button type="primary" icon="Refresh" @click="loadUsers">刷新列表</el-button>
        </div>
        <el-table :data="users" style="width:100%" stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="100" />
          <el-table-column prop="username" label="用户名" width="150">
            <template #default="scope">
              <div style="display:flex;align-items:center;gap:10px">
                <el-avatar :size="30" :src="scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                {{ scope.row.username }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : ''">{{ scope.row.role || 'USER' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="150" />
          <el-table-column prop="phone" label="手机" width="150" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" type="warning" @click="resetPwd(scope.row)">重置密码</el-button>
              <el-popconfirm title="确定删除该用户吗?" @confirm="delUser(scope.row)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 举报处理模块 -->
      <el-tab-pane label="举报处理" name="reports">
        <div class="toolbar">
          <el-button type="primary" icon="Refresh" @click="loadReports">刷新举报</el-button>
        </div>
        <el-table :data="reports" style="width:100%" stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="prodId" label="商品ID" width="100">
            <template #default="scope">
              <el-link type="primary" :href="'/p/'+scope.row.prodId" target="_blank">{{scope.row.prodId}}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="举报理由" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status==='OPEN'?'danger':'success'">{{scope.row.status}}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button v-if="scope.row.status==='OPEN'" size="small" type="success" @click="resolveReport(scope.row.id)">标记已处理</el-button>
              <el-button v-else size="small" type="info" disabled>已归档</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const activeTab = ref('users')
const users = ref([])
const reports = ref([])
const loading = ref(false)

onMounted(() => {
  loadUsers()
  loadReports()
})

// 用户管理逻辑
const loadUsers = async () => {
  loading.value = true
  try {
    const r = await window.api.get('/admin/users')
    if(r.data.code === 0) users.value = r.data.data
  } finally { loading.value = false }
}

const resetPwd = async (u) => {
  const r = await window.api.post(`/admin/users/${u.id}/reset-pwd`)
  if(r.data.code===0) ElMessage.success(`用户 ${u.username} 密码已重置为 123456`)
}

const delUser = async (u) => {
  if(u.role === 'ADMIN') return ElMessage.warning('无法删除管理员')
  const r = await window.api.delete(`/admin/users/${u.id}`)
  if(r.data.code===0) {
    ElMessage.success('删除成功')
    loadUsers()
  }
}

// 举报处理逻辑
const loadReports = async () => {
  try {
    const r = await window.api.get('/admin/report/list')
    if(r.data.code === 0) reports.value = r.data.data
  } catch(e){}
}

const resolveReport = async (id) => {
  // 这里演示直接删除举报记录作为"处理"
  const r = await window.api.delete(`/admin/report/${id}`)
  if(r.data.code===0) {
    ElMessage.success('处理成功')
    loadReports()
  }
}
</script>

<style scoped>
.admin-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.toolbar { margin-bottom: 15px; }
</style>