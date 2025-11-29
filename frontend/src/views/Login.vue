<template>
<div style="padding:16px">
  <el-form :model="f">
    <el-form-item label="username"><el-input v-model="f.username"/></el-form-item>
    <el-form-item label="password"><el-input type="password" v-model="f.password"/></el-form-item>
    <el-button type="primary" @click="login">Login</el-button>
    <el-button @click="reg">Register demo</el-button>
  </el-form>
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
        const token = r.data.data.token;
        localStorage.setItem('token', token);
        this.$message.success('ok');
        this.$router.push('/');
      } else this.$message.error(r.data.msg);
    },
    async reg(){
      const r = await register(this.f);
      if (r.data.code===0){
        const token = r.data.data.token;
        localStorage.setItem('token', token);
        this.$message.success('registered');
        this.$router.push('/');
      } else this.$message.error(r.data.msg);
    }
  }
}
</script>
