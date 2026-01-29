<template>
  <div class="prescriptions">
    <el-table :data="list" stripe style="width: 100%">
      <el-table-column prop="id" label="处方ID" min-width="100" />
      <el-table-column prop="diagnosis" label="诊断" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="开具时间" min-width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { get } from '@/api/request'

const list = ref<any[]>([])
const detail = ref<any>(null)
const detailVisible = ref(false)

function formatTime(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN')
}

async function load() {
  list.value = (await get<any[]>('/doctor/prescriptions')) ?? []
}
async function showDetail(row: any) {
  detail.value = await get<any>(`/doctor/prescriptions/${row.id}`).catch(() => null)
  if (!detail.value) detail.value = row
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.prescriptions { width: 100%; }
</style>
