<template>
  <div class="login-page">
    <div class="card">
      <h1>AI 智能问诊平台</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="submit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <router-link to="/register">注册账号</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { post } from '@/api/request'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value?.validate().catch(() => {})
  loading.value = true
  try {
    const data = await post<{ token: string; userId: number; username: string; role: string; nickname?: string }>('/auth/login', form)
    auth.setLogin({
      token: data.token,
      userId: data.userId,
      username: data.username,
      role: data.role as any,
      nickname: data.nickname,
    })
    ElMessage.success('登录成功')
    router.replace('/')
  } catch (e: any) {
    // request.ts 中 reject 的可能是字符串，也可能是 Error
    ElMessage.error(typeof e === 'string' ? e : (e?.message ?? '登录失败'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1d29 0%, #2d3748 100%);
}
.card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
}
h1 { text-align: center; margin: 0 0 32px; font-size: 1.5rem; color: #1a202c; }
.form { margin-bottom: 16px; }
.footer { text-align: center; }
.footer a { color: #4299e1; text-decoration: none; }
</style>
