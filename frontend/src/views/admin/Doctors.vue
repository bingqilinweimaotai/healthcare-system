<template>
  <div class="doctors">
    <el-card>
      <div class="toolbar">
        <el-select v-model="auditStatus" placeholder="审核状态" clearable style="width:140px">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
      </div>
      <el-table :data="list" stripe style="margin-top:16px">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="hospital" label="医院" show-overflow-tooltip />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="auditStatus" label="审核状态" width="110">
          <template #default="{ row }">{{ auditStatusText(row.auditStatus) }}</template>
        </el-table-column>
        <el-table-column prop="userStatus" label="账号状态" width="110">
          <template #default="{ row }">{{ userStatusText(row.userStatus) }}</template>
        </el-table-column>
        <el-table-column prop="consultCount" label="接诊数" width="90" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <template v-if="row.auditStatus === 'PENDING'">
              <el-button type="success" link @click="audit(row.id, 'APPROVED')">通过</el-button>
              <el-button type="danger" link @click="audit(row.id, 'REJECTED')">驳回</el-button>
            </template>
            <el-button
              v-if="row.userStatus === 'NORMAL'"
              type="warning"
              link
              @click="updateStatus(row.id, 'DISABLED')"
            >禁用</el-button>
            <el-button
              v-else-if="row.userStatus === 'DISABLED'"
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
      <el-dialog v-model="editVisible" title="编辑医生" width="520px">
        <el-form :model="editForm" label-width="90px">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="editForm.realName" />
          </el-form-item>
          <el-form-item label="医院">
            <el-input v-model="editForm.hospital" />
          </el-form-item>
          <el-form-item label="科室">
            <el-input v-model="editForm.department" />
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="editForm.title" />
          </el-form-item>
          <el-form-item label="执业证号">
            <el-input v-model="editForm.licenseNo" />
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

const auditStatus = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const editVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const editForm = reactive<any>({
  username: '',
  realName: '',
  hospital: '',
  department: '',
  title: '',
  licenseNo: '',
})

function auditStatusText(s: string) {
  const m: Record<string, string> = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
  return m[s] ?? s
}
function userStatusText(s: string) {
  const m: Record<string, string> = { NORMAL: '正常', DISABLED: '禁用', PENDING: '待审核' }
  return m[s] ?? s
}

async function load() {
  const params = new URLSearchParams()
  params.set('page', String(page.value - 1))
  params.set('size', String(size.value))
  if (auditStatus.value) params.set('auditStatus', auditStatus.value)
  const res = await get<{ content: any[]; totalElements: number }>(`/admin/doctors?${params}`)
  list.value = res?.content ?? []
  total.value = res?.totalElements ?? 0
}

async function audit(id: number, status: string) {
  try {
    await put(`/admin/doctors/${id}/audit?status=${status}`)
    ElMessage.success('操作成功')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

async function updateStatus(id: number, status: string) {
  try {
    await put(`/admin/doctors/${id}/status?status=${status}`)
    ElMessage.success('操作成功')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

function openEdit(row: any) {
  editingId.value = row.id
  editForm.username = row.username
  editForm.realName = row.realName
  editForm.hospital = row.hospital
  editForm.department = row.department
  editForm.title = row.title
  editForm.licenseNo = row.licenseNo
  editVisible.value = true
}

async function saveEdit() {
  if (!editingId.value) return
  saving.value = true
  try {
    await put(`/admin/doctors/${editingId.value}`, {
      realName: editForm.realName,
      hospital: editForm.hospital,
      department: editForm.department,
      title: editForm.title,
      licenseNo: editForm.licenseNo,
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
.doctors { max-width: 1100px; margin: 0 auto; }
.toolbar { display: flex; gap: 12px; align-items: center; }
</style>
