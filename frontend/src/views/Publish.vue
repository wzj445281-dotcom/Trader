<template>
  <div class="publish-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布商品</span>
        </div>
      </template>
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="商品图片" prop="images">
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
          <div class="tips">第一张将作为封面，最多上传 5 张。</div>
        </el-form-item>

        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="品牌型号，都是关键字" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="详细描述" prop="descr">
          <el-input
              v-model="form.descr"
              type="textarea"
              :rows="6"
              placeholder="描述一下商品的转手原因、入手渠道和新旧程度..."
          />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="价格 (¥)" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" size="large" @click="onSubmit" :loading="loading">立即发布</el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

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

const categories = ['书籍', '电子产品', '生活用品', '美妆', '服饰', '其他']

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  descr: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
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
        // 1. 上传图片
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

        // 2. 发布商品
        const r = await publish(form)
        if (r.data.code === 0) {
          ElMessage.success('发布成功！')
          router.push('/')
        } else {
          ElMessage.error(r.data.msg || '发布失败')
        }
      } catch (e) {
        console.error(e)
        ElMessage.error('发布过程中出错')
      } finally {
        loading.value = false
      }
    }
  })
}
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