<template>
  <div style="padding:16px">
    <h2>Analytics</h2>
    <div id="chart" style="width:100%;height:400px"></div>
  </div>
</template>
<script>
import * as echarts from 'echarts';
export default {
  async mounted(){
    // sample data from /prod/recommend/top used as placeholder
    const r = await window.api.get('/prod/recommend/top');
    const data = r.data.code===0? r.data.data : [];
    const names = data.map(d => d.title);
    const vals = data.map((d,i) => (i+1)*10);
    const chart = echarts.init(document.getElementById('chart'));
    chart.setOption({
      xAxis: { type: 'category', data: names },
      yAxis: { type: 'value' },
      series: [{ data: vals, type: 'bar' }]
    });
  }
}
</script>
