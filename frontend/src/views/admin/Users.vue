<template>
  <div class="users">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名/手机号" clearable style="width:200px" @keyup.enter="load" />
        <el-select v-model="status" placeholder="状态" clearable style="width:120px">
          <el-option label="正常" value="NORMAL" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="list" stripe style="margin-top:16px">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">{{ userRoleText(row.role) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">{{ userStatusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'NORMAL'"
              type="danger"
              link
              @click="updateStatus(row.id, 'DISABLED')"
            >禁用</el-button>
            <el-button
              v-else
              type="success"
              link
              @click="updateStatus(row.id, 'NORMAL')"
            >启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px"
        @current-change="load"
        @size-change="load"
      />
      <el-dialog v-model="editVisible" title="编辑用户" width="480px">
        <el-form :model="editForm" label-width="90px">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="editForm.role" style="width: 180px">
              <el-option label="患者" value="PATIENT" />
              <el-option label="医生" value="DOCTOR" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editVisible = false">取 消</el-button>
          <el-button type="primary" :loading="saving" @click="saveEdit">保 存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { get, put } from '@/api/request'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const status = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const editVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const editForm = reactive<any>({
  username: '',
  nickname: '',
  phone: '',
  role: '',
})

function userStatusText(s: string) {
  const m: Record<string, string> = { NORMAL: '正常', DISABLED: '禁用', PENDING: '待审核' }
  return m[s] ?? s
}

function userRoleText(r: string) {
  const m: Record<string, string> = { PATIENT: '患者', DOCTOR: '医生', ADMIN: '管理员' }
  return m[r] ?? r
}

function formatTime(s: string) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN')
}

async function load() {
  const params = new URLSearchParams()
  params.set('page', String(page.value - 1))
  params.set('size', String(size.value))
  if (keyword.value) params.set('keyword', keyword.value)
  if (status.value) params.set('status', status.value)
  const res = await get<{ content: any[]; totalElements: number }>(`/admin/users?${params}`)
  list.value = res?.content ?? []
  total.value = res?.totalElements ?? 0
}

async function updateStatus(id: number, s: string) {
  try {
    await put(`/admin/users/${id}/status?status=${s}`)
    ElMessage.success('操作成功')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

function openEdit(row: any) {
  editingId.value = row.id
  editForm.username = row.username
  editForm.nickname = row.nickname
  editForm.phone = row.phone
  editForm.role = row.role
  editVisible.value = true
}

async function saveEdit() {
  if (!editingId.value) return
  saving.value = true
  try {
    await put(`/admin/users/${editingId.value}`, {
      nickname: editForm.nickname,
      phone: editForm.phone,
      role: editForm.role,
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.users { max-width: 1100px; margin: 0 auto; }
.toolbar { display: flex; gap: 12px; align-items: center; }
</style>
