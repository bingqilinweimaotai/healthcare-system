<template>
  <div class="register-page">
    <div class="card">
      <h1>注册</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="角色" size="large" style="width:100%">
            <el-option label="患者" value="PATIENT" />
            <el-option label="医生" value="DOCTOR" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="submit">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { post } from '@/api/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '', role: 'PATIENT' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

async function submit() {
  await formRef.value?.validate().catch(() => {})
  loading.value = true
  try {
    await post('/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
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
