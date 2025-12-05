<template>
  <div style="padding:16px;max-width:800px;margin:0 auto">
    <el-page-header @back="$router.back()" content="商品详情" style="margin-bottom:20px"></el-page-header>

    <div v-if="p">
      <el-card>
        <h1 style="margin:0 0 16px 0">{{p.title}}</h1>
        <div style="display:flex;flex-wrap:wrap;gap:10px;margin-bottom:20px" v-if="p.images">
          <img v-for="s in p.images.split(',')" :src="fmt(s)" :key="s" style="max-width:100%;max-height:400px;border-radius:4px;border:1px solid #eee"/>
        </div>

        <div style="background:#f9f9f9;padding:16px;border-radius:8px;margin-bottom:20px">
          <div style="font-size:24px;color:#F56C6C;font-weight:bold;margin-bottom:10px">¥{{p.price}}</div>
          <div style="color:#666;line-height:1.6">{{p.descr}}</div>
          <div style="margin-top:10px;color:#999;font-size:12px">分类: <el-tag size="small">{{p.category}}</el-tag></div>
        </div>

        <div style="display:flex;gap:10px;margin-bottom:30px">
          <el-button type="warning" icon="el-icon-star-off" @click="favIt">收藏商品</el-button>
          <el-button type="success" icon="el-icon-chat-dot-round" @click="$router.push('/chat')">联系卖家</el-button>
        </div>

        <el-divider></el-divider>

        <h3>商品评论 ({{comments.length}})</h3>
        <div v-if="comments.length===0" style="color:#999;margin-bottom:20px">暂无评论</div>
        <div v-for="c in comments" :key="c.id" style="border-bottom:1px solid #eee;padding:12px 0">
          <div style="display:flex;align-items:center;margin-bottom:5px">
            <el-rate v-model="c.rating" disabled show-score text-color="#ff9900"></el-rate>
            <span style="color:#999;font-size:12px;margin-left:10px">{{new Date(c.createdAt).toLocaleString()}}</span>
          </div>
          <div>{{c.content}}</div>
        </div>

        <div style="margin-top:20px;background:#f5f7fa;padding:20px;border-radius:4px">
          <h4>发表评论</h4>
          <el-input type="textarea" v-model="newComment" placeholder="请输入你的评价..." rows="3"></el-input>
          <div style="margin-top:10px;display:flex;align-items:center;justify-content:space-between">
            <div style="display:flex;align-items:center">评分: <el-rate v-model="newRating" style="margin-left:8px"></el-rate></div>
            <el-button type="primary" @click="postComment">提交评价</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script>
import { detail, fav } from '../api';
export default {
  data(){ return { p: null, comments: [], newComment: '', newRating:5 }},
  async mounted(){
    const r = await detail(this.$route.params.id);
    if (r.data.code===0) this.p = r.data.data;
    const rc = await window.api.get('/prod/comments/' + this.$route.params.id);
    if (rc.data.code===0) this.comments = rc.data.data
  },
  methods:{
    async postComment(){
      const user = JSON.parse(localStorage.getItem('trader_user')||'null');
      if (!user) { this.$message.error('请先登录'); return;}
      const payload = { userId: user.id, prodId: this.p.id, content: this.newComment, rating: this.newRating };
      const r = await window.api.post('/prod/comment', payload);
      if (r.data.code===0){
        this.$message.success('评论发布成功');
        this.comments.push({...payload, createdAt: Date.now()});
        this.newComment='';
      }
    },
    fmt(s){ if (!s) return ''; return s.startsWith('/uploads/')? 'http://localhost:8080'+s: s },
    async favIt(){
      const r = await fav({ prodId: this.p.id });
      if (r.data.code===0) this.$message.success('收藏成功');
      else this.$message.error(r.data.msg)
    }
  }
}
</script>