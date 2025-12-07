<template>
  <div style="padding:16px;max-width:700px;margin:0 auto">
    <el-card>
      <div slot="header">
        <strong>实时聊天</strong>
      </div>
      <div style="border:1px solid #eee;height:400px;overflow-y:auto;padding:16px;background:#f9f9f9;border-radius:4px;margin-bottom:16px">
        <div v-if="msgs.length===0" style="text-align:center;color:#ccc;margin-top:20px">暂无消息记录</div>
        <div v-for="(m,i) in msgs" :key="i" style="margin-bottom:10px;background:#fff;padding:8px 12px;border-radius:8px;box-shadow:0 1px 2px rgba(0,0,0,0.05);display:inline-block;max-width:80%">
          {{m}}
        </div>
      </div>
      <div style="display:flex;gap:10px">
        <el-input v-model="txt" @keydown.enter="send" placeholder="输入消息内容..." />
        <el-button type="primary" @click="send">发送</el-button>
      </div>
      <div style="margin-top:10px;color:#999;font-size:12px">
        提示: 这是一个简单的 WebSocket 演示。你需要知道对方的用户ID才能发送。格式 JSON: {"to":2, "message":"你好"}
      </div>
    </el-card>
  </div>
</template>
<script>
export default {
  data(){ return { msgs: [], txt: '' }},
  mounted(){
    const user = JSON.parse(localStorage.getItem('trader_user')||'{}');
    const uid = user.id || 0;
    // 自动适配端口：如果是8888访问，ws也连8080
    const host = location.hostname;
    // 后端端口固定为 8080 (Docker 映射)
    this.ws = new WebSocket((location.protocol==='https:'?'wss':'ws') + '://' + host + ':8080/ws/chat?uid='+uid);
    this.ws.onmessage = (e)=> this.msgs.push(e.data);
  },
  methods:{
    send(){ if (this.txt.trim()){ this.ws.send(this.txt); this.txt=''; } }
  },
  beforeUnmount(){ if (this.ws) this.ws.close(); }
}
</script>