<template>
<div style="padding:16px">
  <el-form :model="f">
    <el-form-item label="Title"><el-input v-model="f.title"/></el-form-item>
    <el-form-item label="Descr"><el-input type="textarea" v-model="f.descr"/></el-form-item>
    <el-form-item label="Price"><el-input-number v-model="f.price"/></el-form-item>
    <el-form-item label="Category"><el-input v-model="f.category"/></el-form-item>
    <el-form-item label="Imgs">
      <input type="file" @change="up" multiple/>
      <div v-for="u in ups" :key="u">{{u}}</div>
    </el-form-item>
    <el-button type="primary" @click="pub">Publish</el-button>
  </el-form>
</div>
</template>
<script>
import { uploadImg, publish } from '../api';
export default {
  data(){ return { f:{ title:'', descr:'', price:0, category:'', images:'' }, ups: [] }},
  methods:{
    async up(e){
      for (const file of e.target.files){
        const fd = new FormData(); fd.append('file', file);
        const r = await uploadImg(fd);
        if (r.data.code===0) this.ups.push(r.data.data);
      }
      this.f.images = this.ups.join(',');
    },
    async pub(){
      // demo userId=1
      const r = await publish(this.f);
      if (r.data.code===0) { this.$message.success('ok'); this.$router.push('/') }
      else this.$message.error(r.data.msg);
    }
  }
}
</script>
