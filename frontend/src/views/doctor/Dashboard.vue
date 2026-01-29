<template>
  <div class="dashboard">
    <h2>医生工作台</h2>
    <el-row :gutter="24" class="cards">
      <el-col :span="8">
        <el-card shadow="hover" class="card" @click="$router.push('/doctor/sessions')">
          <div class="icon">📋</div>
          <div class="label">会话管理</div>
          <div class="desc">待认领 / 进行中会话，实时沟通、开处方</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="card" @click="$router.push('/doctor/prescriptions')">
          <div class="icon">📄</div>
          <div class="label">处方记录</div>
          <div class="desc">查看已开具处方</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.dashboard h2 { margin: 0 0 24px; color: #1a202c; }
.cards { margin-top: 24px; }
.card { cursor: pointer; text-align: center; padding: 24px; }
.card .icon { font-size: 2.5rem; margin-bottom: 12px; }
.card .label { font-weight: 600; font-size: 1.1rem; margin-bottom: 8px; }
.card .desc { color: #718096; font-size: 0.9rem; }
</style>
