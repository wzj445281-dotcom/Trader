<template>
  <div class="cart-container">
    <h2>我的购物车</h2>
    <el-table :data="cart" style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
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
      <div>已选商品：<span style="color:#409EFF;font-weight:bold;">{{ selectedItems.length }}</span> 件</div>
      <div class="checkout-area">
        <span style="margin-right:20px;font-size: 18px;">合计：<span style="color:red;font-size:24px;font-weight:bold;">¥{{ totalPrice }}</span></span>
        <el-button type="primary" size="large" :disabled="selectedItems.length===0" @click="checkout">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCart, detail } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user' // 引入 Pinia Store

const userStore = useUserStore()
const router = useRouter()
const cart = ref([])
const loading = ref(false)
const selectedItems = ref([])

// 计算总价
const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    const price = parseFloat(item.prodPrice || 0)
    return sum + price * item.qty
  }, 0).toFixed(2)
})

const loadCart = async () => {
  if (!userStore.userInfo) return

  loading.value = true
  // 假设 API 返回的是当前用户的购物车列表
  const r = await getCart(userStore.userInfo.id)
  if (r.data.code === 0) {
    const items = r.data.data

    // 遍历获取商品详情，获取价格快照
    for (let item of items) {
      try {
        const p = await detail(item.prodId)
        if (p.data.code === 0 && p.data.data.status === 'AVAILABLE') {
          item.prodTitle = p.data.data.title
          item.prodPrice = p.data.data.price
          item.prodImage = p.data.data.images ? p.data.data.images.split(',')[0] : ''
        } else {
          // 如果商品不存在或已下架，标记，阻止结算
          item.prodTitle = '【商品已下架/不存在】'
          item.prodPrice = 0
          item.prodImage = ''
        }
      } catch(e){
        item.prodTitle = '【商品详情加载失败】'
        item.prodPrice = 0
        item.prodImage = ''
      }
    }
    cart.value = items
  }
  loading.value = false
}

// 表格选择事件
const handleSelectionChange = (val) => {
  // 仅允许选择状态可用的商品进行结算 (prodPrice > 0)
  selectedItems.value = val.filter(item => item.prodPrice > 0)
}

// 删除购物车项
const removeItem = async (id) => {
  // 假设删除接口是 DELETE /api/prod/cart/{id}
  await window.api.delete('/prod/cart/' + id);
  cart.value = cart.value.filter(item => item.id !== id)
  selectedItems.value = selectedItems.value.filter(item => item.id !== id)
  ElMessage.success('已删除')
}

// 结算逻辑 (对应报告 3.4 业务流程)
const checkout = async () => {
  if(selectedItems.value.length === 0) return ElMessage.warning('请选择要结算的商品');

  // 1. 获取收货地址
  const { value: address } = await ElMessageBox.prompt('请输入收货地址', '确认订单', {
    confirmButtonText: '提交订单',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '地址不能为空',
    inputValue: userStore.userInfo?.address || ''
  })

  if (address) {
    // 2. 准备参数：要结算的购物车项ID列表
    const cartItemIds = selectedItems.value.map(item => item.id)

    try {
      // 3. 调用后端批量下单 API
      const r = await window.api.post('/order/create', {
        address,
        cartItemIds
      })

      if (r.data.code === 0) {
        ElMessage.success('订单创建成功！请前往个人中心查看待付款订单。')
        // 4. 清除已结算项并跳转
        loadCart()
        router.push('/profile?tab=orders') // 跳转到个人中心的订单页
      } else {
        ElMessage.error(r.data.msg || '创建订单失败')
      }
    } catch(error) {
      ElMessage.error(error.response?.data?.msg || '创建订单失败，请检查网络或商品状态')
    }
  }
}

const fmt = (s) => {
  if (!s) return 'https://via.placeholder.com/50'
  // 确保图片路径正确
  return s.startsWith('/uploads/') ? 'http://localhost:8080' + s : s
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container { max-width: 1200px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.cart-prod-info { display:flex; align-items:center; font-weight: 500; font-size: 14px; color: #303133; }
.prod-title { overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.cart-footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #ebeef5; display: flex; justify-content: space-between; align-items: center; }
.checkout-area { display: flex; align-items: center; }
</style>