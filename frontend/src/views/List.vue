<template>
<div style="padding:16px">
  <el-input v-model="q" placeholder="search" clearable @clear="load" style="width:300px"/>
  <el-button @click="load">Search</el-button>
  <div style="margin-top:12px"><h3>Recommended</h3><el-row :gutter="16"> <el-col :span="4" v-for="r in recs" :key="r.id"><el-card><div>{{r.title}}</div></el-card></el-col></el-row></div>\n<el-row :gutter="16" style="margin-top:16px">
    <el-col :span="6" v-for="p in prods" :key="p.id">
      <el-card>
        <img v-if="p.images" :src='fmt(p.images.split(",")[0])' style="width:100%;height:140px;object-fit:cover"/>
        <h3>{{p.title}}</h3>
        <p>¥{{p.price}}</p>
        <el-button type="primary" @click="go(p.id)">Detail</el-button>
      </el-card>
    </el-col>
  </el-row>
</div>
</template>
<script>
import { list } from '../api';
export default {
  data(){ return { prods: [], q: '', recs: [] }},
  async mounted(){ this.load(); const rr = await window.api.get('/prod/recommend/top'); if (rr.data.code===0) this.recs = rr.data.data },
  methods:{
    async load(){ const r = await list({ q: this.q }); if (r.data.code===0) this.prods = r.data.data },
    go(id){ this.$router.push('/p/' + id) },
    fmt(s){ if (!s) return ''; return (s.startsWith('/uploads/')? 'http://localhost:8080'+s: s) }
  }
}
</script>
