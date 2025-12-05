<template>
  <div style="padding:16px;max-width:1200px;margin:0 auto">
    <div style="display:flex;gap:10px;margin-bottom:20px">
      <el-input v-model="q" placeholder="搜索商品名称或描述..." clearable @clear="load" style="width:400px"/>
      <el-button type="primary" @click="load">搜索</el-button>
    </div>

    <div style="margin-top:12px" v-if="recs.length">
      <h3 style="border-left:4px solid #409EFF;padding-left:10px">热门推荐</h3>
      <el-row :gutter="16">
        <el-col :span="4" v-for="r in recs" :key="r.id" style="margin-bottom:16px">
          <el-card shadow="hover" :body-style="{padding:'0px'}" @click.native="go(r.id)" style="cursor:pointer">
            <div style="background:#f5f5f5;height:100px;display:flex;align-items:center;justify-content:center;overflow:hidden">
              <img v-if="r.images" :src='fmt(r.images.split(",")[0])' style="width:100%;height:100%;object-fit:cover"/>
              <span v-else style="color:#999">无图</span>
            </div>
            <div style="padding:10px">
              <div style="font-size:14px;font-weight:bold;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{r.title}}</div>
              <div style="color:red;margin-top:5px">¥{{r.price}}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <h3 style="margin-top:20px;border-left:4px solid #67C23A;padding-left:10px">最新发布</h3>
    <el-row :gutter="16">
      <el-col :span="6" v-for="p in prods" :key="p.id" style="margin-bottom:16px">
        <el-card shadow="hover" :body-style="{padding:'0px'}">
          <div style="height:160px;background:#f9f9f9;display:flex;align-items:center;justify-content:center;overflow:hidden">
            <img v-if="p.images" :src='fmt(p.images.split(",")[0])' style="width:100%;height:100%;object-fit:cover"/>
            <span v-else style="color:#ccc">暂无图片</span>
          </div>
          <div style="padding:14px">
            <h3 style="margin:0;font-size:16px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{p.title}}</h3>
            <p style="color:#666;font-size:12px;margin:5px 0;height:36px;overflow:hidden;text-overflow:ellipsis">{{p.descr}}</p>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-top:10px">
              <span style="color:#F56C6C;font-size:18px;font-weight:bold">¥{{p.price}}</span>
              <el-button type="primary" size="small" @click="go(p.id)">查看详情</el-button>
            </div>
          </div>
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