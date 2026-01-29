<template>
  <div class="history">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="人工咨询记录" name="consult">
        <el-table :data="sessions" stripe style="width: 100%">
          <el-table-column prop="id" label="会话ID" min-width="100" />
          <el-table-column prop="status" label="状态" min-width="120">
            <template #default="{ row }">{{ consultStatusText(row.status) }}</template>
          </el-table-column>
          <el-table-column prop="doctorName" label="接诊医生" min-width="150" />
          <el-table-column prop="createdAt" label="创建时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="处方记录" name="prescription">
        <el-table :data="prescriptions" stripe style="width: 100%">
          <el-table-column prop="id" label="处方ID" min-width="100" />
          <el-table-column prop="diagnosis" label="诊断" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="开具时间" min-width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="showPrescription(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-dialog v-model="detailVisible" title="处方详情" width="560">
          <div v-if="detail">
            <p><strong>诊断：</strong>{{ detail.diagnosis || '-' }}</p>
            <el-table :data="detail.items" size="small">
              <el-table-column prop="drugName" label="药品" />
              <el-table-column prop="spec" label="规格" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column prop="dosage" label="用法" />
              <el-table-column prop="frequency" label="频次" />
            </el-table>
          </div>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get } from '@/api/request'

const activeTab = ref('consult')
const sessions = ref<any[]>([])
const prescriptions = ref<any[]>([])
const detail = ref<any>(null)
const detailVisible = ref(false)

// 会话状态中文文案（枚举：WAITING_CLAIM / IN_PROGRESS / FINISHED / CLOSED）
const statusMap: Record<string, string> = {
  WAITING_CLAIM: '待认领',
  IN_PROGRESS: '进行中',
  FINISHED: '已完成',
  CLOSED: '已关闭',
}
function consultStatusText(s: string) {
  return statusMap[s] || s || '-'
}

function formatTime(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN')
}

async function load() {
  sessions.value = (await get<any[]>('/patient/consult/sessions')) ?? []
  prescriptions.value = (await get<any[]>('/patient/consult/prescriptions')) ?? []
}

async function showPrescription(row: any) {
  detail.value = await get<any>(`/patient/consult/prescriptions/${row.id}`)
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.history { width: 100%; }
</style>
