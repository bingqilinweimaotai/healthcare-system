<template>
  <div class="sessions">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="待认领" name="waiting">
        <el-table :data="waiting" stripe>
          <el-table-column prop="id" label="会话ID" width="100" />
          <el-table-column prop="patientName" label="患者" />
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="claim(row.id)">认领</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="refresh">
          <el-button @click="loadWaiting">刷新</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的会话" name="mine">
        <el-table :data="mine" stripe>
          <el-table-column prop="id" label="会话ID" width="100" />
          <el-table-column prop="patientName" label="患者" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button type="primary" link @click="openChat(row)">聊天/开药</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="chatVisible" title="会话" width="640" :close-on-click-modal="false" destroy-on-close>
      <div v-if="currentSession" class="chat-dialog">
        <div class="chat-messages">
          <div
            v-for="m in chatMessages"
            :key="m.id"
            :class="['msg', m.senderType === 'DOCTOR' ? 'me' : m.senderType === 'SYSTEM' ? 'sys' : 'other']"
          >
            <div class="bubble">{{ m.content }}</div>
            <div class="time">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input v-model="chatInput" type="textarea" :rows="2" placeholder="回复患者..." @keydown.ctrl.enter="sendChat" />
          <div class="actions">
            <el-button type="primary" :loading="sending" @click="sendChat">发送</el-button>
            <el-button type="success" :loading="prescribing" @click="openPrescription">开处方</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
    <el-dialog v-model="rxVisible" title="开具处方" width="560" destroy-on-close>
      <el-form ref="rxFormRef" :model="rxForm" label-width="100px">
        <el-form-item label="诊断">
          <el-input v-model="rxForm.diagnosis" type="textarea" :rows="2" placeholder="诊断结论" />
        </el-form-item>
        <el-form-item label="药品">
          <div v-for="(it, idx) in rxForm.items" :key="idx" class="rx-row">
            <el-select v-model="it.drugId" placeholder="选择药品" filterable style="width:200px">
              <el-option v-for="d in drugs" :key="d.id" :label="`${d.name} ${d.spec}`" :value="d.id" />
            </el-select>
            <el-input-number v-model="it.quantity" :min="1" :max="99" style="width:100px" />
            <el-input v-model="it.dosage" placeholder="用法" style="width:120px" />
            <el-input v-model="it.frequency" placeholder="频次" style="width:100px" />
            <el-button link type="danger" @click="rxForm.items.splice(idx,1)">删</el-button>
          </div>
          <el-button type="primary" link @click="rxForm.items.push({ drugId: null, quantity: 1, dosage: '', frequency: '' })">+ 添加药品</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPrescription">提交处方</el-button>
          <el-button @click="rxVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { get, post } from '@/api/request'
import { createStompClient } from '@/api/stomp'
import { Client } from '@stomp/stompjs'
import { ElMessage } from 'element-plus'

const activeTab = ref('waiting')
const waiting = ref<any[]>([])
const mine = ref<any[]>([])
const chatVisible = ref(false)
const currentSession = ref<any>(null)
const chatMessages = ref<any[]>([])
const chatInput = ref('')
const sending = ref(false)
const rxVisible = ref(false)
const prescribing = ref(false)
const drugs = ref<any[]>([])
const rxForm = ref<{ diagnosis: string; items: { drugId: number | null; quantity: number; dosage: string; frequency: string }[] }>({
  diagnosis: '',
  items: [{ drugId: null, quantity: 1, dosage: '', frequency: '' }],
})
let stomp: Client | null = null
let sub: any = null

function formatTime(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN')
}

async function loadWaiting() {
  waiting.value = (await get<any[]>('/doctor/consult/waiting')) ?? []
}
async function loadMine() {
  mine.value = (await get<any[]>('/doctor/consult/sessions')) ?? []
}

async function claim(id: number) {
  try {
    await post(`/doctor/consult/claim/${id}`)
    ElMessage.success('认领成功')
    loadWaiting()
    loadMine()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '认领失败')
  }
}

async function loadChat(sessionId: number) {
  chatMessages.value = (await get<any[]>(`/doctor/consult/sessions/${sessionId}/messages`)) ?? []
}
function openChat(row: any) {
  currentSession.value = row
  chatVisible.value = true
  loadChat(row.id)
  sub?.unsubscribe()
  sub = null
  if (stomp?.connected) {
    sub = stomp.subscribe(`/topic/consult/${row.id}`, () => loadChat(row.id))
  }
}
async function sendChat() {
  const t = chatInput.value?.trim()
  if (!t || !currentSession.value || sending.value) return
  sending.value = true
  try {
    await post('/doctor/consult/send', { sessionId: currentSession.value.id, content: t })
    chatInput.value = ''
    loadChat(currentSession.value.id)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发送失败')
  } finally {
    sending.value = false
  }
}

function openPrescription() {
  if (!currentSession.value) return
  rxForm.value = { diagnosis: '', items: [{ drugId: null, quantity: 1, dosage: '', frequency: '' }] }
  rxVisible.value = true
}
async function submitPrescription() {
  const items = rxForm.value.items.filter((it) => it.drugId != null)
  if (!items.length) {
    ElMessage.warning('请至少添加一种药品')
    return
  }
  prescribing.value = true
  try {
    await post('/doctor/prescriptions', {
      sessionId: currentSession.value.id,
      diagnosis: rxForm.value.diagnosis,
      items: items.map((it) => ({ drugId: it.drugId, quantity: it.quantity, dosage: it.dosage, frequency: it.frequency })),
    })
    ElMessage.success('处方已开具')
    rxVisible.value = false
    loadChat(currentSession.value.id)
    loadMine()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '开具失败')
  } finally {
    prescribing.value = false
  }
}

onMounted(() => {
  loadWaiting()
  loadMine()
  get<any[]>('/doctor/drugs').then((r) => { drugs.value = r ?? [] })
  stomp = createStompClient()
  stomp.onConnect = () => {
    stomp?.subscribe('/topic/new-consult', () => loadWaiting())
  }
  stomp.activate()
})

watch(chatVisible, (v) => {
  if (!v) sub?.unsubscribe()
})
</script>

<style scoped>
.sessions { max-width: 960px; margin: 0 auto; }
.refresh { margin-top: 12px; }
.chat-dialog { min-height: 360px; }
.chat-messages {
  min-height: 280px;
  max-height: 360px;
  overflow-y: auto;
  padding: 12px;
  background: #f7fafc;
  border-radius: 8px;
  margin-bottom: 12px;
}
.msg { margin-bottom: 10px; }
.msg.me { text-align: right; }
.msg.sys { text-align: center; }
.bubble {
  display: inline-block;
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg.me .bubble { background: #4299e1; color: #fff; }
.msg.other .bubble { background: #e2e8f0; color: #1a202c; }
.msg.sys .bubble { background: #feebc8; color: #744210; }
.time { font-size: 12px; color: #a0aec0; margin-top: 4px; }
.chat-input .actions { margin-top: 8px; }
.rx-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
</style>
