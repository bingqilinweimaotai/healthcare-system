<template>
  <div class="dashboard">
    <h2>数据概览</h2>
    <el-row :gutter="24" class="stats">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="value">{{ overview.userCount ?? '-' }}</div>
          <div class="label">用户总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="value">{{ overview.doctorCount ?? '-' }}</div>
          <div class="label">医生总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="value">{{ overview.consultCount ?? '-' }}</div>
          <div class="label">问诊总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="value">{{ overview.todayConsultCount ?? '-' }}</div>
          <div class="label">今日问诊</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="24" class="charts">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>近 {{ days }} 日接诊趋势</template>
          <div ref="trendRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>医生接诊排名</template>
          <div ref="doctorRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { get } from '@/api/request'

const overview = ref<Record<string, number>>({})
const doctorStats = ref<Record<string, any>>({})
const days = ref(7)
const trendRef = ref<HTMLElement>()
const doctorRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let doctorChart: echarts.ECharts | null = null

async function loadOverview() {
  overview.value = (await get<Record<string, number>>('/admin/stats/overview')) ?? {}
}
async function loadDoctorStats() {
  doctorStats.value = (await get<Record<string, any>>(`/admin/stats/doctor-consult?days=${days.value}`)) ?? {}
}

function renderTrend() {
  if (!trendRef.value || !doctorStats.value.dates) return
  trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: (doctorStats.value.dates ?? []).map((d: string) => d.slice(5)) },
    yAxis: { type: 'value', name: '接诊量' },
    series: [{ name: '接诊量', type: 'line', data: doctorStats.value.counts ?? [], smooth: true, areaStyle: {} }],
  })
}
function renderDoctor() {
  if (!doctorRef.value || !doctorStats.value.byDoctor) return
  doctorChart = echarts.init(doctorRef.value)
  const byDoctor = doctorStats.value.byDoctor ?? []
  const pieData = byDoctor.map((d: any) => ({
    name: d.doctorName || `医生${d.doctorId}`,
    value: d.count,
  }))
  doctorChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { type: 'scroll', bottom: 0, left: 'center' },
    series: [{ name: '接诊数', type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: true, itemStyle: { borderRadius: 6 }, label: { show: true, formatter: '{b}: {c}' }, data: pieData }],
  })
}

onMounted(async () => {
  await loadOverview()
  await loadDoctorStats()
  setTimeout(() => {
    renderTrend()
    renderDoctor()
  }, 100)
})

watch(
  () => [days.value],
  async () => {
    await loadDoctorStats()
    trendChart?.setOption({
      xAxis: { data: (doctorStats.value.dates ?? []).map((d: string) => d.slice(5)) },
      series: [{ data: doctorStats.value.counts ?? [] }],
    })
    const byDoctor = doctorStats.value.byDoctor ?? []
    const pieData = byDoctor.map((d: any) => ({ name: d.doctorName || `医生${d.doctorId}`, value: d.count }))
    doctorChart?.setOption({ series: [{ data: pieData }] })
  }
)
</script>

<style scoped>
.dashboard h2 { margin: 0 0 24px; color: #1a202c; }
.stats { margin-bottom: 24px; }
.stat-card { text-align: center; padding: 20px; }
.stat-card .value { font-size: 1.8rem; font-weight: 700; color: #4299e1; }
.stat-card .label { color: #718096; font-size: 0.9rem; margin-top: 8px; }
.charts { margin-top: 24px; }
.chart { height: 320px; }
</style>
