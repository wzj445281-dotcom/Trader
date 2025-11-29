<template>
<div style="padding:16px">
  <h2>Your Cart</h2>
  <div v-for="c in cart" :key="c.id" style="margin-bottom:8px">
    <div>Prod ID: {{c.prodId}} qty: {{c.qty}}</div>
  </div>
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
