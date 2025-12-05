<template>
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
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
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
    }
  }
}
</script>