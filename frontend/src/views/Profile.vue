<template>
  <div style="padding:20px;max-width:1000px;margin:0 auto">
    <el-card>
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
        <div style="display:flex;align-items:center;gap:20px">
          <el-avatar :size="80" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          <div>
            <h2 style="margin:0">{{ user.username }}</h2>
            <p style="color:#666">ID: {{ user.id }}</p>
            <p style="color:#999;font-size:12px">角色: {{ user.role }}</p>
          </div>
        </div>
        <el-button type="primary" plain @click="editProfileVisible = true">编辑个人资料</el-button>
      </div>

      <el-tabs type="card" v-model="activeTab">
        <!-- 我发布的 -->
        <el-tab-pane label="我发布的" name="published">
          <el-table :data="myProds" stripe empty-text="暂无发布">
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div style="display:flex;align-items:center">
                  <img v-if="scope.row.images" :src="fmt(scope.row.images.split(',')[0])" style="width:40px;height:40px;margin-right:10px;object-fit:cover;border-radius:4px">
                  <span>{{ scope.row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">¥{{scope.row.price}}</template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status==='AVAILABLE'?'success':'info'">{{scope.row.status}}</el-tag>
              </template>
            </el-table-column>
            <!-- 🔥 新增的操作列 -->
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" icon="Edit" @click="openProdEdit(scope.row)">编辑</el-button>
                <el-popconfirm title="确定删除该商品吗?" @confirm="deleteProd(scope.row)">
                  <template #reference>
                    <el-button size="small" type="danger" icon="Delete">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的订单 -->
        <el-tab-pane label="我的订单" name="orders">
          <el-table :data="myOrders" stripe empty-text="暂无订单">
            <el-table-column prop="id" label="订单号" width="180"/>
            <el-table-column label="角色" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.buyerId === user.id" type="warning" effect="dark">买入</el-tag>
                <el-tag v-else type="success" effect="dark">卖出</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">¥{{scope.row.totalAmount}}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope"><el-tag>{{ getStatusText(scope.row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <!-- 买家操作 -->
                <template v-if="scope.row.buyerId === user.id">
                  <el-button v-if="scope.row.status==='CREATED'" type="danger" size="small" @click="toPay(scope.row)">去支付</el-button>
                  <el-button v-if="scope.row.status==='CREATED'" type="info" size="small" @click="doAction('cancel', scope.row.id)">取消</el-button>
                  <el-button v-if="scope.row.status==='SHIPPED'" type="success" size="small" @click="doAction('complete', scope.row.id)">收货</el-button>
                </template>
                <!-- 卖家操作 -->
                <template v-else>
                  <el-button v-if="scope.row.status==='CREATED'" type="info" size="small" @click="doAction('cancel', scope.row.id)">取消</el-button>
                  <el-button v-if="scope.row.status==='PAID'" type="primary" size="small" @click="doAction('ship', scope.row.id)">发货</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的收藏 -->
        <el-tab-pane label="我的收藏" name="favs">
          <el-table :data="myFavs" stripe empty-text="暂无收藏" @row-click="goDetail" style="cursor:pointer">
            <el-table-column prop="title" label="商品名称" />
            <el-table-column prop="price" label="价格" width="100"/>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 编辑个人资料弹窗 -->
    <el-dialog v-model="editProfileVisible" title="修改个人资料" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatar" placeholder="请输入图片链接" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="editForm.password" type="password" placeholder="不修改请留空" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editProfileVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 🔥 编辑商品弹窗 -->
    <el-dialog v-model="editProdVisible" title="编辑商品" width="600px">
      <el-form :model="prodForm" label-width="100px">
        <el-form-item label="商品图片">
          <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleProdFileChange"
              :on-remove="handleProdFileRemove"
              :file-list="prodFileList"
              multiple
              :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div style="font-size:12px;color:#999">修改图片需重新上传，或者保留原样不做变动</div>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="prodForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="prodForm.descr" type="textarea" :rows="4" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="价格">
              <el-input-number v-model="prodForm.price" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存">
              <el-input-number v-model="prodForm.stock" :min="0" :step="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分类">
          <el-select v-model="prodForm.category" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editProdVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProdEdit" :loading="savingProd">保存修改</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import { uploadImg } from '../api'

export default {
  data(){
    return {
      user: {},
      myProds: [],
      myOrders: [],
      myFavs: [],
      activeTab: 'published',

      // 个人资料编辑
      editProfileVisible: false,
      editForm: { avatar: '', email: '', phone: '', password: '' },

      // 商品编辑
      editProdVisible: false,
      savingProd: false,
      prodForm: { id: null, title: '', descr: '', price: 0, stock: 1, category: '', images: '' },
      prodFileList: [], //用于回显
      newProdFiles: [], //用于新上传
      categories: ['书籍', '电子产品', '生活用品', '美妆', '服饰', '其他']
    }
  },
  async mounted(){
    const u = JSON.parse(localStorage.getItem('trader_user')||'null');
    if(!u) return this.$router.push('/login');
    this.user = u;
    this.editForm = { ...u, password: '' };

    if(this.$route.query.tab) this.activeTab = this.$route.query.tab;
    this.refresh();
  },
  methods: {
    async refresh(){
      try {
        const [pRes, oRes, fRes] = await Promise.all([
          window.api.get('/prod/my'),
          window.api.get('/order/my'),
          window.api.get('/prod/favs')
        ]);
        if(pRes.data.code===0) this.myProds = pRes.data.data;
        if(oRes.data.code===0) this.myOrders = oRes.data.data;
        if(fRes.data.code===0) this.myFavs = fRes.data.data;
      } catch(e) { console.error(e) }
    },

    // --- 个人资料相关 ---
    async saveProfile() {
      const payload = { ...this.editForm, id: this.user.id };
      if(!payload.password) delete payload.password;
      try {
        const r = await window.api.post('/user/update', payload);
        if(r.data.code===0) {
          this.$message.success('保存成功');
          this.user = r.data.data;
          localStorage.setItem('trader_user', JSON.stringify(this.user));
          this.editForm = { ...this.user, password: '' };
          this.editProfileVisible = false;
        } else {
          this.$message.error(r.data.msg);
        }
      } catch(e) { this.$message.error('保存失败'); }
    },

    // --- 商品管理相关 ---
    openProdEdit(row) {
      // 复制数据到表单
      this.prodForm = { ...row };
      this.newProdFiles = [];
      this.prodFileList = [];
      // 处理图片回显
      if(row.images) {
        row.images.split(',').forEach((url, index) => {
          if(url) this.prodFileList.push({ name: 'img'+index, url: this.fmt(url) });
        });
      }
      this.editProdVisible = true;
    },

    handleProdFileChange(file) {
      this.newProdFiles.push(file.raw);
    },

    handleProdFileRemove(file) {
      // 简单的处理：如果用户移除了图片，我们可能需要复杂的逻辑来判断是移除了旧的还是新的
      // 这里为了演示方便，如果涉及图片修改，建议重传。
      // 或者简单从 newProdFiles 移除
      const idx = this.newProdFiles.indexOf(file.raw);
      if(idx !== -1) this.newProdFiles.splice(idx, 1);

      // 如果是从 prodFileList 移除的（即旧图片），在 prodForm.images 字符串里也应该处理，
      // 但这里简化逻辑：编辑模式下，如果上传了新图，就覆盖旧图；没上传新图，就保留旧图。
    },

    async saveProdEdit() {
      this.savingProd = true;
      try {
        // 1. 如果有新图片，先上传
        if(this.newProdFiles.length > 0) {
          const uploadedUrls = [];
          for (const file of this.newProdFiles) {
            const fd = new FormData();
            fd.append('file', file);
            const res = await uploadImg(fd);
            if (res.data.code === 0) uploadedUrls.push(res.data.data);
          }
          // 覆盖旧图
          this.prodForm.images = uploadedUrls.join(',');
        }

        // 2. 提交更新
        const r = await window.api.post('/prod/update', this.prodForm);
        if(r.data.code === 0) {
          this.$message.success('商品已更新');
          this.editProdVisible = false;
          this.refresh(); // 刷新列表
        } else {
          this.$message.error(r.data.msg);
        }
      } catch(e) {
        this.$message.error('更新失败');
      } finally {
        this.savingProd = false;
      }
    },

    async deleteProd(row) {
      try {
        const r = await window.api.delete('/prod/' + row.id);
        if(r.data.code === 0) {
          this.$message.success('删除成功');
          this.refresh();
        } else {
          this.$message.error(r.data.msg);
        }
      } catch(e) {
        this.$message.error('删除失败');
      }
    },

    // --- 其他 Helper ---
    toPay(row){ this.$router.push(`/pay/${row.id}?amount=${row.totalAmount}`); },
    async doAction(action, orderId){
      const r = await window.api.post(`/order/${action}/${orderId}`);
      if(r.data.code===0) { this.$message.success('操作成功'); this.refresh(); }
      else { this.$message.error(r.data.msg); }
    },
    goDetail(row) { this.$router.push('/p/' + row.id); },
    getStatusText(s) {
      const map = {'CREATED':'待支付','PAID':'待发货','SHIPPED':'待收货','COMPLETED':'已完成','CANCELLED':'已取消'};
      return map[s] || s;
    },
    fmt(s) {
      if (!s) return ''
      return s.startsWith('/uploads/') ? 'http://localhost:8080' + s : s
    }
  }
}
</script>