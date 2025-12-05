<template>
  <div style="padding:16px;max-width:800px;margin:0 auto">
    <h2>我的购物车</h2>
    <div v-if="cart.length===0" style="text-align:center;color:#999;margin-top:40px">
      购物车空空如也，快去选购吧
    </div>
    <el-card v-for="c in cart" :key="c.id" style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between">
        <span>商品ID: <strong>{{c.prodId}}</strong></span>
        <span>数量: <el-tag>{{c.qty}}</el-tag></span>
      </div>
    </el-card>
  </div>
</template>
<script>
import { getCart } from '../api';
export default {
  data(){ return { cart: [] }},
  async mounted(){
    const user = JSON.parse(localStorage.getItem('trader_user')||'null');
    if (!user) return;
    const r = await getCart(user.id);
    if (r.data.code===0) this.cart = r.data.data;
  }
}
</script>