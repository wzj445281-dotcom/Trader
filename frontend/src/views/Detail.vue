<template>
<div style="padding:16px">
  <el-button @click="$router.back()">Back</el-button>
  <div v-if="p">
    <h2>{{p.title}}</h2>
    <div v-if="p.images">
      <img v-for="s in p.images.split(',')" :src="fmt(s)" :key="s" style="width:120px;height:80px;object-fit:cover;margin-right:8px"/>
    </div>
    <p>{{p.descr}}</p>\n    <div style=\"margin-top:16px\">\n      <h3>Comments</h3>\n      <div v-for=\"c in comments\" :key=\"c.id\">{{c.content}} - {{c.rating}}</div>\n      <el-input type=\"textarea\" v-model=\"newComment\" placeholder=\"Write comment\"></el-input>\n      <el-input-number v-model=\"newRating\" :min=1 :max=5></el-input-number>\n      <el-button @click=\"postComment\">Post</el-button>\n    </div>
    <p>¥{{p.price}}</p>
    <el-button type="primary" @click="favIt">Fav</el-button>
  </div>
</div>
</template>
<script>
import { detail, fav } from '../api';
export default {
  data(){ return { p: null, comments: [], newComment: '', newRating:5 }},
  async mounted(){ const r = await detail(this.$route.params.id); if (r.data.code===0) this.p = r.data.data; const rc = await window.api.get('/prod/comments/' + this.$route.params.id); if (rc.data.code===0) this.comments = rc.data.data },
  methods:{\n    async postComment(){ const user = JSON.parse(localStorage.getItem('trader_user')||'null'); if (!user) { this.$message.error('login'); return;} const payload = { userId: user.id, prodId: this.p.id, content: this.newComment, rating: this.newRating }; const r = await window.api.post('/prod/comment', payload); if (r.data.code===0){ this.$message.success('posted'); this.comments.push(payload); this.newComment=''; } },
    fmt(s){ if (!s) return ''; return s.startsWith('/uploads/')? 'http://localhost:8080'+s: s },
    async favIt(){ const r = await fav({ prodId: this.p.id }); if (r.data.code===0) this.$message.success('fav'); else this.$message.error(r.data.msg) }
  }
}
</script>
