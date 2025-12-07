<template>
  <div style="padding:20px;max-width:1000px;margin:0 auto">
    <el-card>
      <div style="display:flex;align-items:center;gap:20px;margin-bottom:20px">
        <el-avatar :size="80" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <div>
          <h2 style="margin:0">{{ user.username }}</h2>
          <p style="color:#666">ID: {{ user.id }}</p>
        </div>
      </div>

      <el-tabs type="card">
        <!-- 我发布的 -->
        <el-tab-pane label="我发布的">
          <el-table :data="myProds" stripe>
            <el-table-column label="商品" width="200" prop="title"/>
            <el-table-column prop="price" label="价格" width="100"/>
            <el-table-column prop="status" label="状态">
              <template #default="scope"><el-tag>{{scope.row.status}}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的订单 (核心修改) -->
        <el-tab-pane label="我的订单">
          <el-table :data="myOrders" stripe>
            <el-table-column prop="id" label="订单号" width="80"/>
            <el-table-column label="角色" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.buyerId === user.id" type="success">买入</el-tag>
                <el-tag v-else type="warning">卖出</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="金额" width="100"/>
            <el-table-column prop="status" label="状态" width="100"/>

            <el-table-column label="操作">
              <template #default="scope">
                <!-- 买家操作 -->
                <template v-if="scope.row.buyerId === user.id">
                  <el-button v-if="scope.row.status==='CREATED'" type="danger" size="small" @click="toPay(scope.row)">去支付</el-button>
                  <el-button v-if="scope.row.status==='SHIPPED'" type="success" size="small" @click="doAction('receive', scope.row.id)">确认收货</el-button>
                </template>
                <!-- 卖家操作 -->
                <template v-else>
                  <el-button v-if="scope.row.status==='PAID'" type="primary" size="small" @click="doAction('ship', scope.row.id)">发货</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的收藏">
          <div v-for="p in myFavs" :key="p.id">{{p.title}}</div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  data(){
    return { user: {}, myProds: [], myOrders: [], myFavs: [] }
  },
  async mounted(){
    const u = JSON.parse(localStorage.getItem('trader_user')||'null');
    if(!u) return this.$router.push('/login');
    this.user = u;
    this.refresh();
  },
  methods: {
    async refresh(){
      const [pRes, oRes, fRes] = await Promise.all([
        window.api.get('/prod/my/products/'+this.user.id),
        window.api.get('/prod/my/orders/'+this.user.id),
        window.api.get('/prod/my/favorites/'+this.user.id)
      ]);
      if(pRes.data.code===0) this.myProds = pRes.data.data;
      if(oRes.data.code===0) this.myOrders = oRes.data.data;
      if(fRes.data.code===0) this.myFavs = fRes.data.data;
    },
    toPay(row){
      this.$router.push(`/pay/${row.id}?amount=${row.price}`);
    },
    async doAction(action, orderId){
      const r = await window.api.post(`/order/${action}`, { orderId });
      if(r.data.code===0) {
        this.$message.success('操作成功');
        this.refresh(); // 刷新列表状态
      } else {
        this.$message.error(r.data.msg);
      }
    }
  }
}
</script>