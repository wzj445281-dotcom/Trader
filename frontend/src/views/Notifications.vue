<template>
<div style="padding:16px">
  <h2>Notifications</h2>
  <div v-for="n in notes" :key="n.id">
    <div><strong>{{n.title}}</strong> - {{n.body}}</div>
  </div>
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
