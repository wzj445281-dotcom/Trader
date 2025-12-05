<template>
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
    }
  }
}
</script>