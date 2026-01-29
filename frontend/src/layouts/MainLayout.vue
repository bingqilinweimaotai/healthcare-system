<template>
  <el-container class="main-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">AI 智能问诊</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1a1d29"
        text-color="#a0aec0"
        active-text-color="#4299e1"
      >
        <template v-if="auth.role === 'PATIENT'">
          <el-menu-item index="/patient/dashboard">首页</el-menu-item>
          <el-menu-item index="/patient/ai-consult">AI 问诊</el-menu-item>
          <el-menu-item index="/patient/manual-consult">人工咨询</el-menu-item>
          <el-menu-item index="/patient/history">问诊记录</el-menu-item>
        </template>
        <template v-else-if="auth.role === 'DOCTOR'">
          <el-menu-item index="/doctor/dashboard">工作台</el-menu-item>
          <el-menu-item index="/doctor/sessions">会话管理</el-menu-item>
          <el-menu-item index="/doctor/prescriptions">处方记录</el-menu-item>
        </template>
        <template v-else-if="auth.role === 'ADMIN'">
          <el-menu-item index="/admin/dashboard">数据概览</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/doctors">医生管理</el-menu-item>
          <el-menu-item index="/admin/drugs">药品管理</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ pageTitle }}</span>
        <div class="user">
          <span>{{ auth.nickname || auth.username }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { post } from '@/api/request'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const pageTitle = computed(() => {
  const m: Record<string, string> = {
    '/patient/dashboard': '患者首页',
    '/patient/ai-consult': 'AI 问诊',
    '/patient/manual-consult': '人工咨询',
    '/patient/history': '问诊记录',
    '/doctor/dashboard': '医生工作台',
    '/doctor/sessions': '会话管理',
    '/doctor/prescriptions': '处方记录',
    '/admin/dashboard': '数据概览',
    '/admin/users': '用户管理',
    '/admin/doctors': '医生管理',
    '/admin/drugs': '药品管理',
  }
  return m[route.path] ?? 'AI 智能问诊平台'
})

async function handleLogout() {
  try { await post('/auth/logout') } catch (_) {}
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.main-layout { height: 100%; }
.aside { background: #1a1d29; }
.logo { padding: 20px; color: #fff; font-weight: 700; font-size: 1.1rem; text-align: center; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  padding: 0 24px;
}
.title { font-size: 1.1rem; font-weight: 600; }
.user { display: flex; align-items: center; gap: 12px; }
.main { background: #f7fafc; padding: 24px; overflow: auto; }
</style>
