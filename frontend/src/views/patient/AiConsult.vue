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
import { useAuthStore } from '@/stores/auth'

const currentSessionId = ref<number | null>(null)
const messages = ref<any[]>([])
const input = ref('')
const sending = ref(false)
const auth = useAuthStore()

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
    // 先在 UI 里追加“用户消息”和一个占位的“AI 消息”，提升响应感
    const now = new Date().toISOString()
    messages.value.push({ id: `u-${Date.now()}`, sender: 'USER', content: t, createdAt: now })
    const aiMsg = { id: `a-${Date.now()}`, sender: 'AI', content: '', createdAt: now }
    messages.value.push(aiMsg)

    const token = auth.tokenValue()
    // 开发环境直连后端，避免 dev 代理对 SSE 的潜在缓冲
    const base =
      import.meta.env.DEV
        ? 'http://localhost:8080/api'
        : '/api'
    const resp = await fetch(`${base}/ai/consult/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: token } : {}),
      },
      body: JSON.stringify({
        sessionId: currentSessionId.value || undefined,
        content: t,
      }),
    })

    if (!resp.ok || !resp.body) {
      throw new Error(`请求失败(${resp.status})`)
    }

    input.value = ''

    // 简易 SSE 解析（只处理 event: name + data: xxx）
    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    const handleEvent = (evt: { event?: string; data: string }) => {
      if (evt.event === 'meta') {
        try {
          const meta = JSON.parse(evt.data)
          if (meta?.sessionId) currentSessionId.value = meta.sessionId
        } catch {
          // ignore
        }
        return
      }
      if (evt.event === 'delta') {
        aiMsg.content += evt.data
        return
      }
      if (evt.event === 'error') {
        throw new Error(evt.data || 'AI stream error')
      }
      // done: ignore
    }

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // SSE 事件以空行分隔
      let idx: number
      while ((idx = buffer.indexOf('\n\n')) !== -1) {
        const raw = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)

        const lines = raw.split('\n').map((l) => l.trimEnd())
        let eventName: string | undefined
        const dataLines: string[] = []
        for (const line of lines) {
          if (line.startsWith('event:')) eventName = line.slice(6).trim()
          if (line.startsWith('data:')) dataLines.push(line.slice(5).trimStart())
        }
        const data = dataLines.join('\n')
        if (data) handleEvent({ event: eventName, data })
      }
    }

    // 流结束后，从后端拉一次完整消息，拿到真实 id/时间
    if (currentSessionId.value) {
      messages.value = await get<any[]>(`/ai/sessions/${currentSessionId.value}/messages`)
    }
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
