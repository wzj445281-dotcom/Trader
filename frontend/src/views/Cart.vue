<template>
  <div class="cart-container">
    <h2>我的购物车</h2>
    <el-table :data="cartStore.cartList" style="width: 100%" v-loading="cartStore.loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="商品信息" min-width="400">
        <template #default="scope">
          <div class="cart-prod-info" v-if="scope.row.prodTitle">
            <el-image :src="fmt(scope.row.prodImage)" fit="cover" style="width:50px;height:50px;margin-right:10px;vertical-align:middle;border-radius:4px"/>
            <span class="prod-title">{{ scope.row.prodTitle }}</span>
          </div>
          <div v-else>加载中...</div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="150">
        <template #default="scope">¥{{ scope.row.prodPrice?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="qty" label="数量" width="150" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="danger" link size="small" icon="Delete" @click="removeItem(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="cart-footer">
      <div>已选商品：<span style="color:#409EFF;font-weight:bold;">{{ selectedCount }}</span> 件</div>
      <div class="checkout-area">
        <span style="margin-right:20px;font-size: 18px;">合计：<span style="color:red;font-size:24px;font-weight:bold;">¥{{ cartStore.totalPrice }}</span></span>
        <el-button type="primary" size="large" :disabled="selectedCount===0" @click="checkout">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import api from '../api' // 🔥 引入 api

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()
const selectedItems = ref([])

const selectedCount = computed(() => cartStore.cartList.filter(i => i.selected).length)

onMounted(() => {
  cartStore.fetchCart()
})

const handleSelectionChange = (val) => {
  selectedItems.value = val
  // 同步状态到 Store
  cartStore.cartList.forEach(item => item.selected = false)
  val.forEach(v => {
    const item = cartStore.cartList.find(i => i.id === v.id)
    if(item) item.selected = true
  })
}

const removeItem = async (id) => {
  try {
    await api.delete('/prod/cart/' + id);
    ElMessage.success('已删除')
    cartStore.fetchCart()
  } catch(e) { ElMessage.error('删除失败') }
}

const checkout = async () => {
  if(selectedCount.value === 0) return ElMessage.warning('请选择商品');

  const { value: address } = await ElMessageBox.prompt('请输入收货地址', '确认订单', {
    confirmButtonText: '提交订单',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '地址不能为空',
    inputValue: userStore.userInfo?.address || ''
  })

  if (address) {
    const cartItemIds = selectedItems.value.map(item => item.id)
    try {
      const r = await api.post('/order/create', { address, cartItemIds })
      if (r.data.code === 0) {
        ElMessage.success('下单成功')
        cartStore.fetchCart()
        router.push('/profile?tab=orders')
      } else {
        ElMessage.error(r.data.msg)
      }
    } catch(e) { ElMessage.error('下单失败: ' + (e.response?.data?.msg || e.message)) }
  }
}

const fmt = (s) => s && s.startsWith('/uploads/') ? 'http://localhost:8080' + s : s
</script>

<style scoped>
.cart-container { max-width: 1200px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.cart-prod-info { display:flex; align-items:center; font-weight: 500; font-size: 14px; color: #303133; }
.prod-title { overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.cart-footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ebeef5; display: flex; justify-content: space-between; align-items: center; }
.checkout-area { display: flex; align-items: center; }
</style>