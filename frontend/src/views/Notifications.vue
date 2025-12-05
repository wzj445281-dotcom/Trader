<template>
  <div style="padding:16px;max-width:800px;margin:0 auto">
    <h2>消息通知</h2>
    <div v-if="notes.length===0" style="color:#999">暂无新通知</div>
    <el-card v-for="n in notes" :key="n.id" style="margin-bottom:10px">
      <div style="font-weight:bold;margin-bottom:5px">{{n.title}}</div>
      <div style="color:#666">{{n.body}}</div>
    </el-card>
  </div>
</template>
<script>
export default {
  data(){ return { notes: [] }},
  async mounted(){
    const u = JSON.parse(localStorage.getItem('trader_user')||'null');
    if (!u) return;
    const r = await window.api.get('/prod/notifications/' + u.id);
    if (r.data.code===0) this.notes = r.data.data;
  }
}
</script>