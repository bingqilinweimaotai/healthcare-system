<template>
  <div class="ai-consult">
    <el-card>
      <div class="chat">
        <div v-if="!currentSessionId" class="empty">发起新会话：在下方输入症状或问题，发送给 AI 获取建议。</div>
        <div v-else class="messages">
          <div
            v-for="m in messages"
            :key="m.id"
            :class="['msg', m.sender === 'USER' ? 'user' : 'ai']"
          >
            <div class="bubble">{{ m.content }}</div>
            <div class="time">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
      </div>
      <div class="input-row">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          placeholder="描述症状或问题..."
          @keydown.ctrl.enter="send"
        />
        <el-button type="primary" :loading="sending" @click="send">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '@/api/request'

const currentSessionId = ref<number | null>(null)
const messages = ref<any[]>([])
const input = ref('')
const sending = ref(false)

watch(currentSessionId, async (id) => {
  if (!id) { messages.value = []; return }
  const list = await get<any[]>(`/ai/sessions/${id}/messages`)
  messages.value = list ?? []
})

function formatTime(s: string) {
  if (!s) return ''
  const d = new Date(s)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function send() {
  const t = input.value?.trim()
  if (!t || sending.value) return
  sending.value = true
  try {
    const res = await post<{ sessionId: number }>('/ai/consult', {
      sessionId: currentSessionId.value || undefined,
      content: t,
    })
    input.value = ''
    currentSessionId.value = res.sessionId
    messages.value = await get<any[]>(`/ai/sessions/${res.sessionId}/messages`)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发送失败')
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.ai-consult { max-width: 800px; margin: 0 auto; }
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
.msg.ai { align-self: flex-start; }
.bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg.user .bubble { background: #4299e1; color: #fff; }
.msg.ai .bubble { background: #e2e8f0; color: #1a202c; }
.time { font-size: 12px; color: #a0aec0; margin-top: 4px; }
.input-row { display: flex; gap: 12px; align-items: flex-end; }
.input-row .el-input { flex: 1; }
</style>
