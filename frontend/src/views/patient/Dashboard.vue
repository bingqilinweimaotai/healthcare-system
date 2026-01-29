<template>
  <div class="dashboard">
    <h2>欢迎，{{ auth.nickname || auth.username }}</h2>
    <el-row :gutter="24" class="cards">
      <el-col :span="8">
        <el-card shadow="hover" class="card" @click="$router.push('/patient/ai-consult')">
          <div class="icon">🤖</div>
          <div class="label">AI 问诊</div>
          <div class="desc">向 AI 描述症状，获取建议</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="card" @click="$router.push('/patient/manual-consult')">
          <div class="icon">👨‍⚕️</div>
          <div class="label">人工咨询</div>
          <div class="desc">与医生实时沟通，可开处方</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="card" @click="$router.push('/patient/history')">
          <div class="icon">📋</div>
          <div class="label">问诊记录</div>
          <div class="desc">查看历史问诊与处方</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.dashboard h2 { margin: 0 0 24px; color: #1a202c; }
.cards { margin-top: 24px; }
.card { cursor: pointer; text-align: center; padding: 24px; }
.card .icon { font-size: 2.5rem; margin-bottom: 12px; }
.card .label { font-weight: 600; font-size: 1.1rem; margin-bottom: 8px; }
.card .desc { color: #718096; font-size: 0.9rem; }
</style>
