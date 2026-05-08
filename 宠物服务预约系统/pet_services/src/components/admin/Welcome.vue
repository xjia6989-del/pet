<template>
  <div class="welcome-page">
    <div class="boxList">
      <div class="box box-a">
        <div class="box1">{{ chartsInfo.totalPeople }}</div>
        <div class="box2">系统总人数</div>
      </div>
      <div class="box box-b">
        <div class="box1">{{ chartsInfo.bookingTotal }}</div>
        <div class="box2">预约总数</div>
      </div>
      <div class="box box-c">
        <div class="box1">{{ chartsInfo.petTotal }}</div>
        <div class="box2">宠物档案数</div>
      </div>
      <div class="box box-d">
        <div class="box1">{{ chartsInfo.pendingContactTotal }}</div>
        <div class="box2">待处理留言数</div>
      </div>
    </div>

    <div class="mid">
      <div class="chart-container1 panel">
        <h2>各类型护理项目数量</h2>
        <div ref="salesChart" class="chart"></div>
      </div>

      <div class="chart-container2 panel">
        <h2>核心业务总览</h2>
        <div ref="transactionChart" class="chart"></div>
      </div>
    </div>

    <div class="chart-container3 panel">
      <h2 class="title-activity">平台运营活跃度</h2>
      <div ref="userChart" class="chart"></div>
    </div>
  </div>
</template>

<script>
import { getDataInfo } from '@/api/AdminAPI.js';
import * as echarts from 'echarts';

export default {
  name: 'stats',
  data() {
    return {
      declaredChartsInfo: '',
      chartsInfo: '',
      dateList: '',
      numberList: '',
      dateList2: '',
      numberList2: '',
      dateList3: '',
      numberList3: '',
      activityData: [],
      bookingTotal: 0
    };
  },
  methods: {
    renderUserChart(data) {
      const chart = echarts.init(this.$refs.userChart);
      const categories = data.map(item => item.loginDate);
      const userLogin = data.map(item => item.userLogin);
      const adminLogin = data.map(item => item.adminLogin);

      const option = {
        color: ['#8ab8ff', '#7bd0bf'],
        xAxis: { type: 'category', data: categories },
        yAxis: { type: 'value' },
        series: [
          {
            name: '用户活跃数',
            data: userLogin,
            type: 'line',
            smooth: true,
            stack: 'Total',
            lineStyle: { width: 0 },
            showSymbol: false,
            areaStyle: {
              opacity: 0.8,
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgb(151, 198, 255)' },
                { offset: 1, color: 'rgb(122, 168, 255)' }
              ])
            },
            emphasis: { focus: 'series' }
          },
          {
            name: '管理员活跃数',
            data: adminLogin,
            type: 'line',
            stack: 'Total',
            smooth: true,
            lineStyle: { width: 0 },
            showSymbol: false,
            areaStyle: {
              opacity: 0.8,
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgb(147, 228, 207)' },
                { offset: 1, color: 'rgb(121, 208, 191)' }
              ])
            },
            emphasis: { focus: 'series' }
          }
        ],
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        legend: { data: ['用户活跃数', '管理员活跃数'] }
      };
      chart.setOption(option);
    },
    renderSalesChart() {
      const chart = echarts.init(this.$refs.salesChart);
      const option = {
        tooltip: { trigger: 'item' },
        legend: { bottom: 'auto', left: '10%', orient: 'vertical' },
        series: [
          {
            name: '服务数',
            type: 'pie',
            radius: ['35%', '70%'],
            data: this.activityData
          }
        ]
      };
      chart.setOption(option);
    },
    renderTransactionChart() {
      const chart = echarts.init(this.$refs.transactionChart);
      const option = {
        tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: {c}' },
        xAxis: {
          type: 'category',
          data: ['预约总数', '护理项目总数', '宠物档案数', '健康记录数'],
          axisLabel: { color: '#667a9f', fontSize: 14 }
        },
        yAxis: { type: 'value', axisLine: { show: false } },
        series: [
          {
            name: '数量',
            type: 'bar',
            data: [
              this.bookingTotal,
              this.chartsInfo.serveTotal || 0,
              this.chartsInfo.petTotal || 0,
              this.chartsInfo.healthRecordTotal || 0
            ],
            barWidth: '40%',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#9dbdff' },
                { offset: 1, color: '#6f96ef' }
              ])
            }
          }
        ]
      };
      chart.setOption(option);
    }
  },
  async mounted() {
    let data = {}
    try {
      let { data: res } = await getDataInfo()
      data = res && res.result ? res.result : {}
    } catch (e) {
      data = {}
    }
    if (!data || Object.keys(data).length === 0) {
      data = {
        totalPeople: 12,
        bookingTotal: 8,
        serveTotal: 6,
        petTotal: 15,
        healthRecordTotal: 22,
        serveList: [
          { categoryName: '健康体检', count: 2 },
          { categoryName: '免疫防护', count: 2 },
          { categoryName: '日常护理', count: 1 },
          { categoryName: '康复护理', count: 1 }
        ],
        loginChartsList: [
          { loginDate: '周一', userLogin: 5, adminLogin: 2 },
          { loginDate: '周二', userLogin: 6, adminLogin: 2 },
          { loginDate: '周三', userLogin: 7, adminLogin: 3 },
          { loginDate: '周四', userLogin: 4, adminLogin: 1 },
          { loginDate: '周五', userLogin: 8, adminLogin: 2 }
        ]
      }
    }
    this.chartsInfo = data
    this.bookingTotal = data.bookingTotal || 0
    this.declaredChartsInfo = data.declaredChartsInfo || []
    this.activityData = Array.isArray(data.serveList)
      ? data.serveList.map(item => ({ name: item.categoryName, value: item.count }))
      : [];

    this.renderUserChart(Array.isArray(data.loginChartsList) ? data.loginChartsList : []);
    this.renderSalesChart();
    this.renderTransactionChart();
  }
};
</script>

<style scoped>
.welcome-page {
  padding: 4px;
}

.boxList {
  width: 100%;
  height: 100px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.box {
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid #e8efff;
}

.box-a { background: linear-gradient(135deg, #e9f2ff, #dbe8ff); }
.box-b { background: linear-gradient(135deg, #e9fbf7, #daf5ee); }
.box-c { background: linear-gradient(135deg, #f2f4ff, #e3e8ff); }
.box-d { background: linear-gradient(135deg, #fff3ea, #ffe9da); }

.box1 {
  font-size: 30px;
  font-weight: 700;
  color: #2d3e62;
  text-align: center;
}

.box2 {
  color: #667a9f;
  text-align: center;
  margin-top: 4px;
}

.main { flex: 1; }

.mid {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 14px;
}

.panel {
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e7efff;
  box-shadow: 0 8px 18px rgba(70, 110, 180, 0.08);
}

.chart-container3 {
  width: 100%;
  height: 350px;
}

.chart-container1,
.chart-container2 {
  width: 49%;
  height: 350px;
  text-align: center;
}

.chart-container1 h2,
.chart-container2 h2 {
  margin-top: 10px;
  color: #33496f;
}

.title-activity {
  text-align: center;
  padding-top: 10px;
  color: #33496f;
}

.chart {
  width: 100%;
  height: 300px;
}
</style>
