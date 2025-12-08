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
        <!-- 🔥 新增编辑按钮 -->
        <el-button type="primary" plain @click="editDialogVisible = true">编辑资料</el-button>
      </div>

      <el-tabs type="card" v-model="activeTab">
        <!-- 我发布的 -->
        <el-tab-pane label="我发布的" name="published">
          <el-table :data="myProds" stripe empty-text="暂无发布">
            <el-table-column label="商品" min-width="200" prop="title"/>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">¥{{scope.row.price}}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status==='AVAILABLE'?'success':'info'">{{scope.row.status}}</el-tag>
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

        <el-tab-pane label="我的收藏" name="favs">
          <el-table :data="myFavs" stripe empty-text="暂无收藏" @row-click="goDetail" style="cursor:pointer">
            <el-table-column prop="title" label="商品名称" />
            <el-table-column prop="price" label="价格" width="100"/>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 🔥 新增编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="修改资料" width="400px">
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
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data(){
    return {
      user: {}, myProds: [], myOrders: [], myFavs: [], activeTab: 'published',
      editDialogVisible: false,
      editForm: { avatar: '', email: '', phone: '', password: '' }
    }
  },
  async mounted(){
    const u = JSON.parse(localStorage.getItem('trader_user')||'null');
    if(!u) return this.$router.push('/login');
    this.user = u;
    // 初始化编辑表单
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
    async saveProfile() {
      const payload = { ...this.editForm, id: this.user.id };
      if(!payload.password) delete payload.password; // 不修改密码则不传
      try {
        const r = await window.api.post('/user/update', payload);
        if(r.data.code===0) {
          this.$message.success('保存成功');
          this.user = r.data.data;
          // 更新本地存储和表单
          localStorage.setItem('trader_user', JSON.stringify(this.user));
          this.editForm = { ...this.user, password: '' };
          this.editDialogVisible = false;
        } else {
          this.$message.error(r.data.msg);
        }
      } catch(e) { this.$message.error('保存失败'); }
    },
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
    }
  }
}
</script>