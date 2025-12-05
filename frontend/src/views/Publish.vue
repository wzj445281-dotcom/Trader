<template>
<<<<<<< HEAD
  <div style="padding:16px;max-width:600px;margin:20px auto">
    <el-card>
      <div slot="header">
        <span style="font-weight:bold;font-size:18px">发布闲置商品</span>
      </div>
      <el-form :model="f" label-width="80px">
        <el-form-item label="商品标题"><el-input v-model="f.title" placeholder="品牌型号、新旧程度..."/></el-form-item>
        <el-form-item label="详细描述"><el-input type="textarea" v-model="f.descr" placeholder="描述商品的转手原因、入手渠道和使用感受" rows="5"/></el-form-item>
        <el-form-item label="出售价格">
          <el-input-number v-model="f.price" :min="0" :precision="2"/> <span style="margin-left:5px">元</span>
        </el-form-item>
        <el-form-item label="商品分类"><el-input v-model="f.category" placeholder="例如：教材、电子数码、生活用品"/></el-form-item>
        <el-form-item label="上传图片">
          <input type="file" @change="up" multiple accept="image/*" style="display:block;margin-bottom:10px"/>
          <div style="display:flex;gap:5px;flex-wrap:wrap">
            <div v-for="u in ups" :key="u" style="border:1px solid #eee;padding:2px">
              <img :src="'http://localhost:8080'+u" style="width:60px;height:60px;object-fit:cover"/>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%" @click="pub">立即发布</el-button>
=======
  <div class="publish-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>發布商品</span>
        </div>
      </template>
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="商品圖片" prop="images">
          <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleFileChange"
              :on-remove="handleRemove"
              multiple
              :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="tips">第一張將作為封面，最多上傳 5 張。</div>
        </el-form-item>

        <el-form-item label="商品標題" prop="title">
          <el-input v-model="form.title" placeholder="品牌型號，都是關鍵字" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="詳細描述" prop="descr">
          <el-input
              v-model="form.descr"
              type="textarea"
              :rows="6"
              placeholder="描述一下商品的轉手原因、入手渠道和新舊程度..."
          />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="價格 (¥)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分類" prop="category">
              <el-select v-model="form.category" placeholder="選擇分類" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" size="large" @click="onSubmit" :loading="loading">立即發布</el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<<<<<<< HEAD
<script>
import { uploadImg, publish } from '../api';
export default {
  data(){ return { f:{ title:'', descr:'', price:0, category:'', images:'' }, ups: [] }},
  methods:{
    async up(e){
      const loading = this.$loading({ text: '图片上传中...' });
      try {
        for (const file of e.target.files){
          const fd = new FormData(); fd.append('file', file);
          const r = await uploadImg(fd);
          if (r.data.code===0) this.ups.push(r.data.data);
        }
        this.f.images = this.ups.join(',');
      } finally {
        loading.close();
      }
    },
    async pub(){
      if(!this.f.title || !this.f.price) return this.$message.warning('请补全标题和价格');
      const r = await publish(this.f);
      if (r.data.code===0) { this.$message.success('发布成功'); this.$router.push('/') }
      else this.$message.error(r.data.msg);
=======

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadImg, publish } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])

const form = reactive({
  title: '',
  descr: '',
  price: 0,
  category: '',
  images: ''
})

const categories = ['書籍', '電子產品', '生活用品', '美妝', '服飾', '其他']

const rules = {
  title: [{ required: true, message: '請輸入標題', trigger: 'blur' }],
  descr: [{ required: true, message: '請輸入描述', trigger: 'blur' }],
  price: [{ required: true, message: '請輸入價格', trigger: 'blur' }],
  category: [{ required: true, message: '請選擇分類', trigger: 'change' }]
}

const handleFileChange = (file) => {
  fileList.value.push(file.raw)
}

const handleRemove = (file) => {
  const index = fileList.value.indexOf(file.raw)
  if (index !== -1) fileList.value.splice(index, 1)
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 1. 上傳圖片
        const uploadedUrls = []
        for (const file of fileList.value) {
          const fd = new FormData()
          fd.append('file', file)
          const res = await uploadImg(fd)
          if (res.data.code === 0) {
            uploadedUrls.push(res.data.data)
          }
        }
        form.images = uploadedUrls.join(',')

        // 2. 發布商品
        const r = await publish(form)
        if (r.data.code === 0) {
          ElMessage.success('發布成功！')
          router.push('/')
        } else {
          ElMessage.error(r.data.msg || '發布失敗')
        }
      } catch (e) {
        console.error(e)
        ElMessage.error('發布過程中出錯')
      } finally {
        loading.value = false
      }
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
    }
  })
}
<<<<<<< HEAD
</script>
=======
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
}
.tips {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
>>>>>>> 98ed80e20ee63afeaa8c46ff01e529e91f6f6983
