<template>
<<<<<<< HEAD
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
=======
  <div>
    <!-- 頂部搜索與篩選區 -->
    <div class="filter-section">
      <div class="search-bar">
        <el-input
            v-model="q"
            placeholder="搜索商品..."
            size="large"
            clearable
            @clear="load"
            @keyup.enter="load"
        >
          <template #append>
            <el-button @click="load"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>
      <div class="categories">
        <span class="label">分類：</span>
        <el-check-tag
            v-for="cat in categories"
            :key="cat"
            :checked="selectedCategory === cat"
            @change="toggleCategory(cat)"
            class="cat-tag"
        >
          {{ cat }}
        </el-check-tag>
      </div>
    </div>

    <!-- 推薦商品區 (僅當無搜索時顯示) -->
    <div v-if="!q && !selectedCategory && recs.length > 0" class="section">
      <h3 class="section-title"><el-icon><Star /></el-icon> 熱門推薦</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="r in recs" :key="r.id">
          <div class="prod-card mini" @click="go(r.id)">
            <el-image :src="fmt(r.images ? r.images.split(',')[0] : '')" fit="cover" class="cover">
              <template #error><div class="image-slot"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div class="info">
              <div class="title">{{ r.title }}</div>
              <div class="price">¥{{ r.price }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 商品列表區 -->
    <div class="section">
      <h3 class="section-title">
        {{ selectedCategory ? selectedCategory : '全部商品' }}
      </h3>
      <el-empty v-if="prods.length === 0" description="暫無商品" />
      <el-row :gutter="20" v-else>
        <el-col :xs="12" :sm="8" :md="6" v-for="p in prods" :key="p.id">
          <el-card class="prod-card" shadow="hover" :body-style="{ padding: '0px' }" @click="go(p.id)">
            <div class="img-wrapper">
              <el-image
                  :src="fmt(p.images ? p.images.split(',')[0] : '')"
                  fit="cover"
                  class="cover"
                  loading="lazy"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="view-count"><el-icon><View /></el-icon> {{ p.viewCount || 0 }}</div>
            </div>
            <div class="card-body">
              <h3 class="title" :title="p.title">{{ p.title }}</h3>
              <div class="meta">
                <span class="price">¥{{ p.price }}</span>
                <span class="time">{{ formatTime(p.createdAt) }}</span>
              </div>
              <div class="tags" v-if="p.category">
                <el-tag size="small" type="info" effect="plain">{{ p.category }}</el-tag>
              </div>
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
<<<<<<< HEAD

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
=======
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Star, Picture, View } from '@element-plus/icons-vue'
import { list } from '../api'

const router = useRouter()
const prods = ref([])
const recs = ref([])
const q = ref('')
const selectedCategory = ref('')
const categories = ['書籍', '電子產品', '生活用品', '美妝', '服飾', '其他']

const load = async () => {
  const params = { q: q.value }
  if (selectedCategory.value) params.category = selectedCategory.value
  const r = await list(params)
  if (r.data.code === 0) prods.value = r.data.data
}

const toggleCategory = (cat) => {
  selectedCategory.value = selectedCategory.value === cat ? '' : cat
  load()
}

onMounted(async () => {
  load()
  const rr = await window.api.get('/prod/recommend/top')
  if (rr.data.code === 0) recs.value = rr.data.data
})

const go = (id) => router.push('/p/' + id)

const fmt = (s) => {
  if (!s) return ''
  return s.startsWith('/uploads/') ? 'http://localhost:8080' + s : s
}

const formatTime = (timeArr) => {
  if (!timeArr) return ''
  // 處理後端 LocalDateTime 數組 [2023, 11, 29, 10, 30]
  if (Array.isArray(timeArr)) {
    return `${timeArr[1]}/${timeArr[2]}`
  }
  return new Date(timeArr).toLocaleDateString()
}
<<<<<<< HEAD
</script>
=======
</script>

<style scoped>
.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.search-bar {
  max-width: 600px;
  margin: 0 auto 20px;
}
.categories {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}
.label {
  font-weight: bold;
  color: #606266;
}
.cat-tag {
  cursor: pointer;
}
.section {
  margin-top: 30px;
}
.section-title {
  font-size: 20px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}
.prod-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}
.prod-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}
.img-wrapper {
  position: relative;
  height: 200px;
  background: #f5f7fa;
  overflow: hidden;
}
.cover {
  width: 100%;
  height: 100%;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 30px;
  color: #dcdfe6;
}
.view-count {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.card-body {
  padding: 12px;
}
.title {
  font-size: 16px;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #303133;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
}
.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}
.time {
  color: #909399;
  font-size: 12px;
}
/* Mini card for recommendations */
.prod-card.mini .img-wrapper {
  height: 120px;
}
.prod-card.mini {
  display: flex;
  flex-direction: column;
  background: #fff;
  padding: 10px;
  border: 1px solid #ebeef5;
}
.prod-card.mini .info {
  margin-top: 5px;
}
</style>
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
