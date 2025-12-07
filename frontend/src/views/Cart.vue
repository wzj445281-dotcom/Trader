<template>
  <div class="cart-container">
    <h2>我的购物车</h2>
    <el-table :data="cart" style="width: 100%" v-loading="loading">
      <el-table-column label="商品信息" width="400">
        <template #default="scope">
          <div class="cart-prod-info" v-if="scope.row.prodTitle">
            <span class="prod-title">{{ scope.row.prodTitle }}</span>
          </div>
          <div v-else>加载中...</div>
        </template>
      </el-table-column>
      <el-table-column prop="qty" label="数量" width="150" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="danger" size="small" icon="Delete" @click="removeItem(scope.row.id)">删除</el-button>
          <el-button type="primary" size="small" @click="checkout(scope.row)">去结算</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCart, detail } from '../api'
import { ElMessage } from 'element-plus'

const cart = ref([])
const loading = ref(false)

const loadCart = async () => {
  const user = JSON.parse(localStorage.getItem('trader_user')||'null')
  if (!user) return

  loading.value = true
  const r = await getCart(user.id)
  if (r.data.code === 0) {
    const items = r.data.data
    // 并发获取商品详情
    for (let item of items) {
      try {
        const p = await detail(item.prodId)
        if (p.data.code === 0) {
          item.prodTitle = p.data.data.title
          item.prodPrice = p.data.data.price
        }
      } catch(e){}
    }
    cart.value = items
  }
  loading.value = false
}

const removeItem = (id) => {
  // 暂时使用前端模拟删除，如需后端配合请调用对应API
  cart.value = cart.value.filter(item => item.id !== id)
  ElMessage.success('已删除')
}

const checkout = async (item) => {
  const user = JSON.parse(localStorage.getItem('trader_user'))
  const payload = {
    buyerId: user.id,
    sellerId: 1, // 这里需要从商品详情中获取卖家ID，暂时模拟
    prodId: item.prodId,
    price: item.prodPrice || 0
  }
  const r = await window.api.post('/order/create', payload)
  if (r.data.code === 0) {
    ElMessage.success('订单创建成功！')
    removeItem(item.id)
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
.cart-prod-info {
  font-weight: bold;
}
</style>