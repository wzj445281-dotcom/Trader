<template>
  <div style="padding:16px;max-width:1000px;margin:0 auto">
    <h2>平台数据统计</h2>
    <el-card>
      <div id="chart" style="width:100%;height:400px"></div>
    </el-card>
  </div>
</template>
<script>
import * as echarts from 'echarts';
export default {
  async mounted(){
    const r = await window.api.get('/prod/recommend/top');
    const data = r.data.code===0? r.data.data : [];
    const names = data.map(d => d.title);
    const vals = data.map((d,i) => d.viewCount || (i+1)*5);
    const chart = echarts.init(document.getElementById('chart'));
    chart.setOption({
      title: { text: '热门商品浏览量排行' },
      tooltip: {},
      xAxis: { type: 'category', data: names, axisLabel: { interval: 0, rotate: 30 } },
      yAxis: { type: 'value', name: '浏览次' },
      series: [{ name: '浏览量', data: vals, type: 'bar', itemStyle: { color: '#409EFF' } }]
    });
  }
}
</script>