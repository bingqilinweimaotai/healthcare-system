<template>
  <div class="profile-page">
    <el-card class="card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-form :model="profileForm" label-width="90px" v-loading="loading">
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
              :size="56"
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
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存信息</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="card">
      <template #header>
        <div class="card-header">
          <span>安全设置</span>
        </div>
      </template>
      <el-form :model="passwordForm" label-width="90px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { get, post, put } from '@/api/request'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()

const defaultAvatar = 'https://avatars.dicebear.com/api/identicon/ai-healthcare.svg'

const loading = ref(false)
const saving = ref(false)
const changingPwd = ref(false)

const profileForm = reactive<any>({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  hospital: '',
  department: '',
  title: '',
  auditStatus: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
})

async function loadProfile() {
  loading.value = true
  try {
    const data = await get<any>('/user/profile')
    profileForm.username = data.username
    profileForm.nickname = data.nickname
    profileForm.phone = data.phone
    profileForm.avatar = data.avatar
    profileForm.hospital = data.hospital
    profileForm.department = data.department
    profileForm.title = data.title
    profileForm.auditStatus = data.auditStatus
    if (data.nickname) auth.nickname = data.nickname
    if (data.avatar) auth.avatar = data.avatar
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载个人信息失败')
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await put('/user/profile', {
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      avatar: profileForm.avatar,
      hospital: auth.role === 'DOCTOR' ? profileForm.hospital : undefined,
      department: auth.role === 'DOCTOR' ? profileForm.department : undefined,
      title: auth.role === 'DOCTOR' ? profileForm.title : undefined,
    })
    auth.nickname = profileForm.nickname || auth.username
    auth.avatar = profileForm.avatar || ''
    ElMessage.success('个人信息已保存')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写原密码和新密码')
    return
  }
  changingPwd.value = true
  try {
    await put('/user/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    ElMessage.success('密码已修改')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '修改密码失败')
  } finally {
    changingPwd.value = false
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

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card {
  width: 100%;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>

