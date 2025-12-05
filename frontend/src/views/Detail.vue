<template>
<<<<<<< HEAD
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
=======
  <div v-if="p" class="detail-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首頁</el-breadcrumb-item>
      <el-breadcrumb-item>{{ p.category || '商品' }}</el-breadcrumb-item>
      <el-breadcrumb-item>詳情</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="product-main">
      <!-- 左側圖片 -->
      <div class="gallery">
        <el-image
            v-if="currentImg"
            :src="fmt(currentImg)"
            fit="contain"
            class="main-img"
            :preview-src-list="imgList.map(fmt)"
        />
        <div class="thumb-list" v-if="imgList.length > 1">
          <div
              v-for="(img, idx) in imgList"
              :key="idx"
              class="thumb-item"
              :class="{ active: currentImg === img }"
              @click="currentImg = img"
          >
            <el-image :src="fmt(img)" fit="cover" class="thumb-img" />
          </div>
        </div>
      </div>

      <!-- 右側信息 -->
      <div class="info-col">
        <h1 class="title">{{ p.title }}</h1>
        <div class="price-box">
          <span class="currency">¥</span>
          <span class="amount">{{ p.price }}</span>
          <div class="view-info"><el-icon><View /></el-icon> {{ p.viewCount }} 次瀏覽</div>
        </div>

        <div class="meta-info">
          <div class="meta-item">
            <span class="label">分類：</span>
            <span>{{ p.category }}</span>
          </div>
          <div class="meta-item">
            <span class="label">狀態：</span>
            <el-tag type="success">{{ p.status }}</el-tag>
          </div>
          <div class="meta-item">
            <span class="label">發布時間：</span>
            <span>{{ formatTime(p.createdAt) }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="danger" size="large" icon="ShoppingCart" @click="handleBuy">立即購買</el-button>
          <el-button type="primary" size="large" plain icon="Star" @click="favIt">收藏</el-button>
          <el-button size="large" icon="ChatDotRound" @click="contactSeller">聯繫賣家</el-button>
        </div>

        <el-divider content-position="left">商品描述</el-divider>
        <div class="description">
          {{ p.descr }}
        </div>
      </div>
    </div>

    <!-- 評論區 -->
    <div class="comments-section">
      <h3>留言區</h3>
      <div class="comment-input">
        <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="對這件商品感興趣？留言問問吧..."
        />
        <div class="comment-tools">
          <el-rate v-model="newRating" />
          <el-button type="primary" @click="postComment">發布留言</el-button>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-user">用戶 {{ c.userId }}</span>
            <el-rate v-model="c.rating" disabled size="small" />
            <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ c.content }}</div>
        </div>
      </div>
    </div>
  </div>
  <el-empty v-else description="加載中..." />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, ShoppingCart, Star, ChatDotRound } from '@element-plus/icons-vue'
import { detail, fav } from '../api'

const route = useRoute()
const router = useRouter()
const p = ref(null)
const comments = ref([])
const newComment = ref('')
const newRating = ref(5)
const currentImg = ref('')

// 計算屬性
const imgList = computed(() => p.value?.images ? p.value.images.split(',') : [])

onMounted(async () => {
  await loadData()
  // 記錄瀏覽
  if (p.value) window.api.post('/prod/view/' + p.value.id)
})

const loadData = async () => {
  const r = await detail(route.params.id)
  if (r.data.code === 0) {
    p.value = r.data.data
    if (imgList.value.length > 0) currentImg.value = imgList.value[0]
  }
  const rc = await window.api.get('/prod/comments/' + route.params.id)
  if (rc.data.code === 0) comments.value = rc.data.data
}

const fmt = (s) => {
  if (!s) return ''
  return s.startsWith('/uploads/') ? 'http://localhost:8080' + s : s
}

const formatTime = (time) => {
  if (!time) return ''
  // 簡單處理時間格式
  if (Array.isArray(time)) return `${time[0]}-${time[1]}-${time[2]}`
  return new Date(time).toLocaleDateString()
}

const favIt = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) { ElMessage.warning('請先登入'); return router.push('/login') }
  const r = await fav({ prodId: p.value.id })
  if (r.data.code === 0) ElMessage.success('已加入收藏')
  else ElMessage.error(r.data.msg)
}

const postComment = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) { ElMessage.warning('請先登入'); return router.push('/login') }
  if (!newComment.value.trim()) return ElMessage.warning('請輸入內容')

  const payload = { userId: user.id, prodId: p.value.id, content: newComment.value, rating: newRating.value }
  const r = await window.api.post('/prod/comment', payload)
  if (r.data.code === 0) {
    ElMessage.success('留言成功')
    newComment.value = ''
    // 重新加載評論
    const rc = await window.api.get('/prod/comments/' + p.value.id)
    if (rc.data.code === 0) comments.value = rc.data.data
  }
}

const handleBuy = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) { ElMessage.warning('請先登入'); return router.push('/login') }

  // 簡單添加到購物車邏輯
  const r = await window.api.post('/prod/cart/add', { userId: user.id, prodId: p.value.id, qty: 1 })
  if (r.data.code === 0) {
    ElMessage.success('已加入購物車')
    router.push('/cart')
  } else {
    ElMessage.error(r.data.msg)
  }
}

const contactSeller = () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) return router.push('/login')
  // 跳轉到聊天頁面，並帶上對方ID（假設 p.userId 存在）
  // 這裡需要後續完善 Chat 頁面支持 query 參數
  router.push('/chat')
  ElMessage.info('聊天功能開發中，請先進入聊天室列表')
}
</script>

<style scoped>
.detail-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
.breadcrumb {
  margin-bottom: 20px;
}
.product-main {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}
.gallery {
  width: 400px;
  flex-shrink: 0;
}
.main-img {
  width: 100%;
  height: 400px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
  background: #f9f9f9;
}
.thumb-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
}
.thumb-item {
  width: 60px;
  height: 60px;
  border: 2px solid transparent;
  cursor: pointer;
  border-radius: 4px;
}
.thumb-item.active {
  border-color: #409EFF;
}
.thumb-img {
  width: 100%;
  height: 100%;
  border-radius: 2px;
}
.info-col {
  flex: 1;
}
.title {
  font-size: 24px;
  color: #303133;
  margin-top: 0;
}
.price-box {
  background: #fff5f5;
  padding: 15px;
  border-radius: 4px;
  color: #f56c6c;
  margin: 20px 0;
  display: flex;
  align-items: baseline;
  position: relative;
}
.currency { font-size: 18px; }
.amount { font-size: 32px; font-weight: bold; }
.view-info {
  position: absolute;
  right: 15px;
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}
.meta-info {
  margin-bottom: 30px;
}
.meta-item {
  margin-bottom: 12px;
  color: #606266;
}
.meta-item .label {
  color: #909399;
  width: 80px;
  display: inline-block;
}
.actions {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}
.description {
  margin-top: 20px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}
.comments-section {
  margin-top: 40px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}
.comment-input {
  margin-bottom: 30px;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}
.comment-tools {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 15px 0;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.comment-user {
  font-weight: bold;
  color: #303133;
}
.comment-time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}
.comment-content {
  color: #606266;
}
</style>
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
