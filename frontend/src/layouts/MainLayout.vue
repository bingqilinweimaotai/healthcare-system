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
          <el-menu-item index="/patient/profile">个人信息</el-menu-item>
        </template>
        <template v-else-if="auth.role === 'DOCTOR'">
          <el-menu-item index="/doctor/dashboard">工作台</el-menu-item>
          <el-menu-item index="/doctor/sessions">会话管理</el-menu-item>
          <el-menu-item index="/doctor/prescriptions">处方记录</el-menu-item>
          <el-menu-item index="/doctor/profile">个人信息</el-menu-item>
        </template>
        <template v-else-if="auth.role === 'ADMIN'">
          <el-menu-item index="/admin/dashboard">数据概览</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/doctors">医生管理</el-menu-item>
          <el-menu-item index="/admin/drugs">药品管理</el-menu-item>
          <el-menu-item index="/admin/profile">个人信息</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ pageTitle }}</span>
        <div class="user">
          <el-dropdown @command="handleUserCommand">
            <span class="user-trigger">
              <el-avatar
                :size="32"
                :src="auth.avatar || defaultAvatar"
              >
                {{ (auth.nickname || auth.username || '?').charAt(0).toUpperCase() }}
              </el-avatar>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />

        <el-dialog v-model="profileVisible" title="个人信息" width="520px" destroy-on-close>
          <el-form :model="profileForm" label-width="90px" v-loading="profileLoading">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" />
            </el-form-item>
            <el-form-item label="头像">
              <div class="avatar-row">
                <el-avatar
                  :size="48"
                  :src="profileForm.avatar || auth.avatar || defaultAvatar"
                >
                  {{ (auth.nickname || auth.username || '?').charAt(0).toUpperCase() }}
                </el-avatar>
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :http-request="handleAvatarUpload"
                  accept="image/*"
                >
                  <el-button type="primary" link>选择图片并上传</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <template v-if="auth.role === 'DOCTOR'">
              <el-form-item label="姓名">
                <el-input v-model="profileForm.realName" />
              </el-form-item>
              <el-form-item label="医院">
                <el-input v-model="profileForm.hospital" />
              </el-form-item>
              <el-form-item label="科室">
                <el-input v-model="profileForm.department" />
              </el-form-item>
              <el-form-item label="职称">
                <el-input v-model="profileForm.title" />
              </el-form-item>
              <el-form-item label="审核状态" v-if="profileForm.auditStatus">
                <el-tag size="small">{{ profileForm.auditStatus }}</el-tag>
              </el-form-item>
            </template>
            <el-divider />
            <el-form-item label="原密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="profileVisible = false">取 消</el-button>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
          </template>
        </el-dialog>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { get, post, put } from '@/api/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const defaultAvatar = 'https://avatars.dicebear.com/api/identicon/ai-healthcare.svg'

const profileVisible = ref(false)
const profileLoading = ref(false)
const savingProfile = ref(false)
const profileForm = reactive<any>({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  realName: '',
  hospital: '',
  department: '',
  title: '',
  auditStatus: '',
})
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
})

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

async function openProfile() {
  profileVisible.value = true
  profileLoading.value = true
  try {
    const data = await get<any>('/user/profile')
    profileForm.username = data.username
    profileForm.nickname = data.nickname
    profileForm.phone = data.phone
    profileForm.avatar = data.avatar
    profileForm.realName = data.realName
    profileForm.hospital = data.hospital
    profileForm.department = data.department
    profileForm.title = data.title
    profileForm.auditStatus = data.auditStatus
    // 同步 store 中昵称和头像，避免保存前头像显示为空
    if (data.nickname) auth.nickname = data.nickname
    if (data.avatar) auth.avatar = data.avatar
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载个人信息失败')
  } finally {
    profileLoading.value = false
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await put('/user/profile', {
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      avatar: profileForm.avatar,
      realName: auth.role === 'DOCTOR' ? profileForm.realName : undefined,
      hospital: auth.role === 'DOCTOR' ? profileForm.hospital : undefined,
      department: auth.role === 'DOCTOR' ? profileForm.department : undefined,
      title: auth.role === 'DOCTOR' ? profileForm.title : undefined,
    })
    if (passwordForm.oldPassword && passwordForm.newPassword) {
      await put('/user/password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
    }
    // 更新本地显示
    auth.nickname = profileForm.nickname || auth.username
    auth.avatar = profileForm.avatar || ''
    ElMessage.success('保存成功')
    profileVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    savingProfile.value = false
  }
}

async function handleAvatarUpload(option: any) {
  const file = option.file as File
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const url = await post<string>('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    profileForm.avatar = url
    auth.avatar = url
    ElMessage.success('头像上传成功')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '头像上传失败')
  } finally {
    option.onSuccess?.({}, file)
  }
}

function handleUserCommand(cmd: string) {
  if (cmd === 'logout') {
    handleLogout()
  } else if (cmd === 'profile') {
    openProfile()
  }
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
.user-trigger { cursor: pointer; display: inline-flex; align-items: center; }
.main { background: #f7fafc; padding: 24px; overflow: auto; }
.avatar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
