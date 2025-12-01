<template>
  <div class="login-wrapper">
    <div class="login-box">
      <div class="login-header">
        <h2>欢迎来到 Trader</h2>
        <p>校园二手好物交易平台</p>
      </div>
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" prefix-icon="User" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" prefix-icon="Lock" show-password placeholder="请输入密码" @keyup.enter="handleLogin" />
            </el-form-item>
            <el-button type="primary" class="full-btn" :loading="loading" @click="handleLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="regForm" :rules="rules" ref="regFormRef" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="regForm.username" prefix-icon="User" placeholder="设置用户名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="regForm.email" prefix-icon="Message" placeholder="常用邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="regForm.phone" prefix-icon="Iphone" placeholder="手机号码" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="regForm.password" type="password" prefix-icon="Lock" show-password placeholder="设置密码" />
            </el-form-item>
            <el-button type="success" class="full-btn" :loading="loading" @click="handleRegister">注册账号</el-button>
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
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
          ElMessage.success('登录成功')
          router.push('/')
          setTimeout(() => window.location.reload(), 100)
        } else {
          ElMessage.error(r.data.msg || '登录失败')
        }
      } catch (e) {
        ElMessage.error('网络错误')
      } finally {
        loading.value = false
      }
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
          ElMessage.success('注册成功，已自动登录')
          router.push('/')
          setTimeout(() => window.location.reload(), 100)
        } else {
          ElMessage.error(r.data.msg || '注册失败')
        }
      } catch (e) {
        ElMessage.error('网络错误')
      } finally {
        loading.value = false
      }
    }
  })
}
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