<template>
  <div class="manual-consult">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="startNew">新咨询</el-button>
        <el-select
          v-model="currentSessionId"
          placeholder="选择会话"
          clearable
          filterable
          style="width:240px;margin-left:12px"
          @change="loadSession"
        >
          <el-option
            v-for="s in sessions"
            :key="s.id"
            :label="`#${s.id} ${s.status} ${s.patientName || ''}`"
            :value="s.id"
          />
        </el-select>
      </div>
      <div class="chat">
        <div v-if="!currentSessionId" class="empty">点击「新咨询」发起咨询，或选择已有会话。医生认领后可回复并开药。</div>
        <div v-else class="messages">
          <div
            v-for="m in messages"
            :key="m.id"
            :class="['msg', m.senderType === 'PATIENT' ? 'user' : m.senderType === 'SYSTEM' ? 'sys' : 'doctor']"
          >
            <div class="bubble">{{ m.content }}</div>
            <div class="time">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
      </div>
      <div v-if="currentSessionId" class="input-row">
        <el-input v-model="input" type="textarea" :rows="2" placeholder="输入消息..." @keydown.ctrl.enter="send" />
        <el-button type="primary" :loading="sending" @click="send">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '@/api/request'
import { createStompClient } from '@/api/stomp'
import { Client } from '@stomp/stompjs'

const sessions = ref<any[]>([])
const currentSessionId = ref<number | null>(null)
const messages = ref<any[]>([])
const input = ref('')
const sending = ref(false)
let stomp: Client | null = null
let sub: any = null

async function loadSessions() {
  const list = await get<any[]>('/patient/consult/sessions')
  sessions.value = list ?? []
}

async function loadSession() {
  const id = currentSessionId.value
  if (!id) { messages.value = []; return }
  const list = await get<any[]>(`/patient/consult/sessions/${id}/messages`)
  messages.value = list ?? []
  sub?.unsubscribe()
  sub = null
  if (stomp?.connected) {
    sub = stomp.subscribe(`/topic/consult/${id}`, () => {
      loadSession()
      loadSessions()
    })
  }
}

function formatTime(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function startNew() {
  currentSessionId.value = null
  messages.value = []
  input.value = ''
}

async function send() {
  const t = input.value?.trim()
  if (!t || sending.value) return
  sending.value = true
  try {
    const res = await post<{ sessionId: number }>('/patient/consult/send', {
      sessionId: currentSessionId.value || undefined,
      content: t,
    })
    input.value = ''
    currentSessionId.value = res.sessionId
    await loadSessions()
    await loadSession()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发送失败')
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  loadSessions()
  stomp = createStompClient()
  stomp.onConnect = () => {
    if (currentSessionId.value) loadSession()
  }
  stomp.activate()
})

watch(currentSessionId, (id) => {
  if (id) loadSession()
})

onUnmounted(() => {
  sub?.unsubscribe()
  stomp?.deactivate()
})
</script>

<style scoped>
.manual-consult { max-width: 800px; margin: 0 auto; }
.toolbar { margin-bottom: 16px; }
.chat {
  min-height: 360px;
  max-height: 480px;
  overflow-y: auto;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
  margin-bottom: 16px;
}
.empty { color: #718096; text-align: center; padding: 48px 16px; }
.messages { display: flex; flex-direction: column; gap: 12px; }
.msg.user { align-self: flex-end; }
.msg.doctor { align-self: flex-start; }
.msg.sys { align-self: center; }
.bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg.user .bubble { background: #4299e1; color: #fff; }
.msg.doctor .bubble { background: #e2e8f0; color: #1a202c; }
.msg.sys .bubble { background: #feebc8; color: #744210; }
.time { font-size: 12px; color: #a0aec0; margin-top: 4px; }
.input-row { display: flex; gap: 12px; align-items: flex-end; }
.input-row .el-input { flex: 1; }
</style>
