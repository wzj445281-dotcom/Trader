<template>
  <div style="padding:16px;max-width:700px;margin:0 auto">
    <el-card>
      <div slot="header">
        <strong>实时聊天</strong>
        <span style="font-size:12px;color:#999;margin-left:10px">ID: {{ myId }}</span>
      </div>
      <div ref="msgBox" style="border:1px solid #eee;height:400px;overflow-y:auto;padding:16px;background:#f9f9f9;border-radius:4px;margin-bottom:16px">
        <div v-if="msgs.length===0" style="text-align:center;color:#ccc;margin-top:20px">暂无消息记录</div>
        <div v-for="(m,i) in msgs" :key="i" class="msg-row" :class="{ 'me': m.fromUserId === myId || m.from === myId }">
          <div class="msg-bubble">
            <div class="msg-sender">用户 {{ m.fromUserId || m.from }}</div>
            <div class="msg-text">{{ m.message }}</div>
          </div>
        </div>
      </div>
      <div style="display:flex;gap:10px;margin-bottom:10px">
        <el-input v-model="targetId" placeholder="对方ID" style="width:100px" @blur="loadHistory" />
        <el-input v-model="txt" @keydown.enter="send" placeholder="输入消息内容..." />
        <el-button type="primary" @click="send" :disabled="!connected">发送</el-button>
      </div>
      <div style="color:#F56C6C;font-size:12px" v-if="!connected">连接断开，正在重连...</div>
    </el-card>
  </div>
</template>
<script>
export default {
  data(){ return { msgs: [], txt: '', targetId: '', myId: 0, ws: null, connected: false }},
  mounted(){
    const user = JSON.parse(localStorage.getItem('trader_user')||'{}');
    if(!user.id) return this.$router.push('/login');
    this.myId = user.id;
    this.initWs();
  },
  methods:{
    initWs() {
      const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
      const host = location.host;
      const url = `${protocol}://${host}/ws/chat?uid=${this.myId}`;
      this.ws = new WebSocket(url);
      this.ws.onopen = () => { this.connected = true; };
      this.ws.onclose = () => { this.connected = false; setTimeout(this.initWs, 3000); };
      this.ws.onmessage = (e) => {
        try {
          const data = JSON.parse(e.data);
          // 收到新消息直接追加
          this.msgs.push(data);
          this.scrollToBottom();
        } catch(err){}
      };
    },
    // 🔥 新增：加载历史记录
    async loadHistory() {
      if(!this.targetId) return;
      try {
        const r = await window.api.get(`/chat/history/${this.myId}/${this.targetId}`);
        if(r.data.code===0) {
          this.msgs = r.data.data;
          this.scrollToBottom();
        }
      } catch(e){}
    },
    send(){
      if (!this.txt.trim() || !this.targetId) return this.$message.warning('请输入对方ID和内容');
      const payload = { to: parseInt(this.targetId), message: this.txt };
      this.ws.send(JSON.stringify(payload));
      this.msgs.push({ fromUserId: this.myId, message: this.txt }); // 本地回显
      this.txt='';
      this.scrollToBottom();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.msgBox;
        if(box) box.scrollTop = box.scrollHeight;
      });
    }
  },
  beforeUnmount(){ if (this.ws) this.ws.close(); }
}
</script>
<style scoped>
.msg-row { display: flex; margin-bottom: 10px; }
.msg-row.me { justify-content: flex-end; }
.msg-bubble { background: #fff; padding: 8px 12px; border-radius: 8px; box-shadow: 0 1px 2px rgba(0,0,0,0.1); max-width: 70%; }
.msg-row.me .msg-bubble { background: #d9ecff; }
.msg-sender { font-size: 12px; color: #999; margin-bottom: 4px; }
</style>