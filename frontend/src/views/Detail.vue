<template>
  <div v-if="p" class="detail-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ p.category || '商品' }}</el-breadcrumb-item>
      <el-breadcrumb-item>详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="product-main">
      <div class="gallery">
        <el-image v-if="currentImg" :src="fmt(currentImg)" fit="contain" class="main-img" :preview-src-list="imgList.map(fmt)" />
        <div class="thumb-list" v-if="imgList.length > 1">
          <div v-for="(img, idx) in imgList" :key="idx" class="thumb-item" :class="{ active: currentImg === img }" @click="currentImg = img">
            <el-image :src="fmt(img)" fit="cover" class="thumb-img" />
          </div>
        </div>
      </div>

      <div class="info-col">
        <h1 class="title">{{ p.title }}</h1>
        <div class="price-box">
          <span class="currency">¥</span>
          <span class="amount">{{ p.price }}</span>
          <div class="view-info"><el-icon><View /></el-icon> {{ p.viewCount }} 次浏览</div>
        </div>

        <div class="meta-info">
          <div class="meta-item"><span class="label">分类：</span><span>{{ p.category }}</span></div>
          <div class="meta-item">
            <span class="label">库存：</span>
            <el-tag :type="p.stock > 0 ? 'success' : 'danger'">{{ p.stock > 0 ? '有货 ('+p.stock+')' : '缺货' }}</el-tag>
          </div>
          <div class="meta-item"><span class="label">发布人：</span>用户 {{ p.userId }}</div>
          <div class="meta-item"><span class="label">发布时间：</span><span>{{ formatTime(p.createdAt) }}</span></div>
        </div>

        <div class="actions">
          <el-button type="danger" size="large" icon="ShoppingCart" @click="handleBuy" :disabled="!p.stock || p.stock <= 0">
            {{ (!p.stock || p.stock <= 0) ? '已售罄' : '加入购物车' }}
          </el-button>
          <el-button type="primary" size="large" plain icon="Star" @click="favIt">收藏</el-button>
          <el-button size="large" icon="ChatDotRound" @click="contactSeller">联系卖家</el-button>
          <!-- 🔥 新增举报按钮 -->
          <el-button size="large" type="warning" link @click="reportDialogVisible = true">举报商品</el-button>
        </div>

        <el-divider content-position="left">商品描述</el-divider>
        <div class="description">{{ p.descr }}</div>
      </div>
    </div>

    <!-- 留言区保持不变 -->
    <div class="comments-section">
      <h3>留言区</h3>
      <div class="comment-input">
        <el-input v-model="newComment" type="textarea" :rows="3" placeholder="对这件商品感兴趣？留言问问吧..." />
        <div class="comment-tools">
          <el-rate v-model="newRating" />
          <el-button type="primary" @click="postComment">发布留言</el-button>
        </div>
      </div>
      <div class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-user">用户 {{ c.userId }}</span>
            <el-rate v-model="c.rating" disabled size="small" />
            <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ c.content }}</div>
        </div>
      </div>
    </div>

    <!-- 🔥 新增举报弹窗 -->
    <el-dialog v-model="reportDialogVisible" title="举报商品" width="30%">
      <el-form>
        <el-form-item label="举报理由">
          <el-input v-model="reportReason" type="textarea" placeholder="请描述违规情况..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  <el-empty v-else description="加载中..." />
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

// 举报相关状态
const reportDialogVisible = ref(false)
const reportReason = ref('')

const imgList = computed(() => p.value?.images ? p.value.images.split(',') : [])

onMounted(async () => {
  await loadData()
  if (p.value) { try { window.api.post('/prod/view/' + p.value.id) } catch(e){} }
})

const loadData = async () => {
  try {
    const r = await detail(route.params.id)
    if (r.data.code === 0) {
      p.value = r.data.data
      if (imgList.value.length > 0) currentImg.value = imgList.value[0]
    } else { ElMessage.error('商品不存在或已下架') }
    const rc = await window.api.get('/prod/comments/' + route.params.id)
    if (rc.data.code === 0) comments.value = rc.data.data
  } catch(e) { ElMessage.error('加载失败') }
}

const fmt = (s) => s // 图片路径处理

const formatTime = (time) => {
  if (!time) return ''
  if (Array.isArray(time)) return `${time[0]}-${time[1]}-${time[2]}`
  return new Date(time).toLocaleDateString()
}

const favIt = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) { ElMessage.warning('请先登录'); return router.push('/login') }
  try {
    const r = await fav({ prodId: p.value.id })
    if (r.data.code === 0) ElMessage.success('已加入收藏')
    else ElMessage.error(r.data.msg)
  } catch(e) { ElMessage.error('操作失败') }
}

const postComment = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) return router.push('/login')
  if (!newComment.value.trim()) return ElMessage.warning('请输入内容')
  try {
    const payload = { userId: user.id, prodId: p.value.id, content: newComment.value, rating: newRating.value }
    const r = await window.api.post('/prod/comment', payload)
    if (r.data.code === 0) {
      ElMessage.success('留言成功')
      newComment.value = ''
      const rc = await window.api.get('/prod/comments/' + p.value.id)
      if (rc.data.code === 0) comments.value = rc.data.data
    }
  } catch(e) { ElMessage.error('留言失败') }
}

const handleBuy = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) return router.push('/login')
  try {
    const r = await window.api.post('/prod/cart/add', { userId: user.id, prodId: p.value.id, qty: 1 })
    if (r.data.code === 0) { ElMessage.success('已加入购物车'); router.push('/cart') }
    else { ElMessage.error(r.data.msg) }
  } catch(e) {
    if(e.response && e.response.status === 401) router.push('/login')
    else ElMessage.error('服务不可用')
  }
}

const contactSeller = () => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  if (!user) return router.push('/login')
  router.push('/chat')
  // 提示用户复制 ID
  ElMessage.info(`卖家ID是 ${p.value.userId}，请在聊天室输入该ID`)
}

// 🔥 新增：提交举报逻辑
const submitReport = async () => {
  if(!reportReason.value.trim()) return ElMessage.warning('请输入理由');
  try {
    const r = await window.api.post('/prod/report', { prodId: p.value.id, reason: reportReason.value });
    if(r.data.code===0) {
      ElMessage.success('举报已提交');
      reportDialogVisible.value = false;
      reportReason.value = '';
    } else {
      ElMessage.error(r.data.msg);
    }
  } catch(e) { ElMessage.error('提交失败'); }
}
</script>

<style scoped>
/* 样式保持不变 */
.detail-container { padding: 20px; background: #fff; border-radius: 8px; }
.breadcrumb { margin-bottom: 20px; }
.product-main { display: flex; gap: 40px; margin-bottom: 40px; }
.gallery { width: 400px; flex-shrink: 0; }
.main-img { width: 100%; height: 400px; border: 1px solid #eee; border-radius: 4px; margin-bottom: 10px; background: #f9f9f9; }
.thumb-list { display: flex; gap: 10px; overflow-x: auto; }
.thumb-item { width: 60px; height: 60px; border: 2px solid transparent; cursor: pointer; border-radius: 4px; }
.thumb-item.active { border-color: #409EFF; }
.thumb-img { width: 100%; height: 100%; border-radius: 2px; }
.info-col { flex: 1; }
.title { font-size: 24px; color: #303133; margin-top: 0; }
.price-box { background: #fff5f5; padding: 15px; border-radius: 4px; color: #f56c6c; margin: 20px 0; display: flex; align-items: baseline; position: relative; }
.currency { font-size: 18px; }
.amount { font-size: 32px; font-weight: bold; }
.view-info { position: absolute; right: 15px; color: #909399; font-size: 14px; display: flex; align-items: center; gap: 5px; }
.meta-info { margin-bottom: 30px; }
.meta-item { margin-bottom: 12px; color: #606266; }
.meta-item .label { color: #909399; width: 80px; display: inline-block; }
.actions { display: flex; gap: 15px; margin-bottom: 30px; }
.description { margin-top: 20px; color: #606266; line-height: 1.6; white-space: pre-wrap; }
.comments-section { margin-top: 40px; border-top: 1px solid #ebeef5; padding-top: 20px; }
.comment-input { margin-bottom: 30px; background: #f9f9f9; padding: 20px; border-radius: 8px; }
.comment-tools { margin-top: 10px; display: flex; justify-content: space-between; align-items: center; }
.comment-item { border-bottom: 1px solid #f0f0f0; padding: 15px 0; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.comment-user { font-weight: bold; color: #303133; }
.comment-time { color: #909399; font-size: 12px; margin-left: auto; }
.comment-content { color: #606266; }
</style>