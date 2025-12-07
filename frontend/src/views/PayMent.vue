<template>
  <div style="padding:20px;display:flex;justify-content:center">
    <el-card style="width:400px;text-align:center">
      <div slot="header"><strong>收银台</strong></div>
      <div style="margin-bottom:20px">
        <p>订单号: {{id}}</p>
        <h1 style="color:#F56C6C">¥{{amount}}</h1>
      </div>
      <div style="background:#f0f0f0;width:200px;height:200px;margin:0 auto;display:flex;align-items:center;justify-content:center;color:#999">
        <!-- 实际这里应该生成二维码 -->
        [ 模拟二维码 ]
      </div>
      <p style="color:#666;font-size:12px">请使用支付宝/微信扫码</p>
      <el-button type="success" style="width:100%;margin-top:20px" :loading="paying" @click="confirmPay">我已支付</el-button>
    </el-card>
  </div>
</template>
<script>
export default {
  data(){ return { id: this.$route.params.id, amount: this.$route.query.amount, paying:false } },
  methods:{
    async confirmPay(){
      this.paying = true;
      try {
        const r = await window.api.post('/order/pay', { orderId: this.id });
        if(r.data.code===0){
          this.$message.success('支付成功！');
          this.$router.push('/profile'); // 跳回个人中心
        } else {
          this.$message.error(r.data.msg);
        }
      } catch(e){
        this.$message.error('支付失败');
      } finally {
        this.paying = false;
      }
    }
  }
}
</script>