<template>
<div style="padding:16px">
  <h2>Chat</h2>
  <div v-for="m in msgs" :key="m">{{m}}</div>
  <input v-model="txt" @keydown.enter="send"/>
</div>
</template>
<script>
export default {
  data(){ return { msgs: [], txt: '' }},
  mounted(){
    this.ws = new WebSocket((location.protocol==='https:'?'wss':'ws') + '://' + location.host.replace(':5173',':8080') + '/ws/chat');
    this.ws.onmessage = (e)=> this.msgs.push(e.data);
  },
  methods:{
    send(){ if (this.txt.trim()){ this.ws.send(this.txt); this.txt=''; } }
  },
  beforeUnmount(){ if (this.ws) this.ws.close(); }
}
</script>
