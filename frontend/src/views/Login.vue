<template>
<<<<<<< HEAD
  <div style="display:flex;justify-content:center;align-items:center;height:80vh;background-color:#f5f7fa">
    <el-card style="width:400px;padding:20px">
      <h2 style="text-align:center;margin-bottom:24px">欢迎登录 Trader</h2>
      <el-form :model="f" label-width="70px">
        <el-form-item label="用户名"><el-input v-model="f.username" placeholder="请输入用户名"/></el-form-item>
        <el-form-item label="密码"><el-input type="password" v-model="f.password" placeholder="请输入密码" @keydown.enter="login"/></el-form-item>
        <div style="text-align:center;margin-top:20px">
          <el-button type="primary" style="width:100px" @click="login">登录</el-button>
          <el-button style="width:100px" @click="reg">注册账号</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
<script>
import { login, register } from '../api';
export default {
  data(){ return { f:{ username:'', password:'' } }},
  methods:{
    async login(){
      const r = await login(this.f);
      if (r.data.code===0){
        const data = r.data.data;
        // 修复：统一使用 trader_token
        localStorage.setItem('trader_token', data.token);
        if(data.refresh) localStorage.setItem('trader_refresh', data.refresh);
        localStorage.setItem('trader_user', JSON.stringify(data.user));

        this.$message.success('登录成功');
        window.location.href = '/'; // 刷新以更新 Header 状态
      } else this.$message.error(r.data.msg || '登录失败');
    },
    async reg(){
      const r = await register(this.f);
      if (r.data.code===0){
        const data = r.data.data;
        localStorage.setItem('trader_token', data.token);
        if(data.refresh) localStorage.setItem('trader_refresh', data.refresh);
        localStorage.setItem('trader_user', JSON.stringify(data.user));
        this.$message.success('注册成功，已自动登录');
        window.location.href = '/';
      } else this.$message.error(r.data.msg || '注册失败');
=======
  <div class="login-wrapper">
    <div class="login-box">
      <div class="login-header">
        <h2>歡迎來到 Trader</h2>
        <p>校園二手好物交易平台</p>
      </div>
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登入" name="login">
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
            <el-form-item label="用戶名" prop="username">
              <el-input v-model="loginForm.username" prefix-icon="User" placeholder="請輸入用戶名" />
            </el-form-item>
            <el-form-item label="密碼" prop="password">
              <el-input v-model="loginForm.password" type="password" prefix-icon="Lock" show-password placeholder="請輸入密碼" @keyup.enter="handleLogin" />
            </el-form-item>
            <el-button type="primary" class="full-btn" :loading="loading" @click="handleLogin">登入</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="註冊" name="register">
          <el-form :model="regForm" :rules="rules" ref="regFormRef" label-position="top">
            <el-form-item label="用戶名" prop="username">
              <el-input v-model="regForm.username" prefix-icon="User" placeholder="設置用戶名" />
            </el-form-item>
            <el-form-item label="郵箱" prop="email">
              <el-input v-model="regForm.email" prefix-icon="Message" placeholder="常用郵箱" />
            </el-form-item>
            <el-form-item label="手機號" prop="phone">
              <el-input v-model="regForm.phone" prefix-icon="Iphone" placeholder="手機號碼" />
            </el-form-item>
            <el-form-item label="密碼" prop="password">
              <el-input v-model="regForm.password" type="password" prefix-icon="Lock" show-password placeholder="設置密碼" />
            </el-form-item>
            <el-button type="success" class="full-btn" :loading="loading" @click="handleRegister">註冊帳號</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Iphone } from '@element-plus/icons-vue'
import { login, register } from '../api'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const regFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', email: '', phone: '' })

const rules = {
  username: [{ required: true, message: '請輸入用戶名', trigger: 'blur' }],
  password: [{ required: true, message: '請輸入密碼', trigger: 'blur' }],
  email: [
    { required: true, message: '請輸入郵箱', trigger: 'blur' },
    { type: 'email', message: '郵箱格式不正確', trigger: ['blur', 'change'] }
  ],
  phone: [{ required: true, message: '請輸入手機號', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const r = await login(loginForm)
        if (r.data.code === 0) {
          localStorage.setItem('trader_token', r.data.data.token)
          localStorage.setItem('trader_refresh', r.data.data.refresh)
          localStorage.setItem('trader_user', JSON.stringify(r.data.data.user))
          ElMessage.success('登入成功')
          router.push('/')
          // 為了確保 Header 組件更新狀態，可以刷新頁面或使用 EventBus/Pinia，這裡簡單處理
          setTimeout(() => window.location.reload(), 100)
        } else {
          ElMessage.error(r.data.msg || '登入失敗')
        }
      } catch (e) {
        ElMessage.error('網絡錯誤')
      } finally {
        loading.value = false
      }
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
    }
  })
}

const handleRegister = async () => {
  if (!regFormRef.value) return
  await regFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const r = await register(regForm)
        if (r.data.code === 0) {
          localStorage.setItem('trader_token', r.data.data.token)
          localStorage.setItem('trader_user', JSON.stringify(r.data.data.user))
          ElMessage.success('註冊成功，已自動登入')
          router.push('/')
          setTimeout(() => window.location.reload(), 100)
        } else {
          ElMessage.error(r.data.msg || '註冊失敗')
        }
      } catch (e) {
        ElMessage.error('網絡錯誤')
      } finally {
        loading.value = false
      }
    }
  })
}
<<<<<<< HEAD
</script>
=======
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}
.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.login-header {
  text-align: center;
  margin-bottom: 20px;
}
.login-header h2 {
  color: #303133;
  margin-bottom: 8px;
}
.login-header p {
  color: #909399;
  font-size: 14px;
}
.full-btn {
  width: 100%;
  margin-top: 10px;
}
</style>
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
