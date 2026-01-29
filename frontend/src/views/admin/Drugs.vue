<template>
  <div class="drugs">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索药品名称" clearable style="width:200px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" @click="openEdit(null)">新增药品</el-button>
      </div>
      <el-table :data="list" stripe style="margin-top:16px; width: 100%">
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="spec" label="规格" min-width="140" />
        <el-table-column prop="unit" label="单位" min-width="80" />
        <el-table-column prop="usageInstruction" label="用法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">{{ drugStatusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="danger"
              link
              @click="updateStatus(row, 'DISABLED')"
            >禁用</el-button>
            <el-button v-else type="success" link @click="updateStatus(row, 'ACTIVE')">启用</el-button>
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
    </el-card>
    <el-dialog v-model="editVisible" :title="editing?.id ? '编辑药品' : '新增药品'" width="500" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="药品名称" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" placeholder="如 0.5g*24粒" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如 盒、瓶" />
        </el-form-item>
        <el-form-item label="用法">
          <el-input v-model="form.usageInstruction" type="textarea" :rows="2" placeholder="用法用量" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { get, post, put } from '@/api/request'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const editVisible = ref(false)
const editing = ref<any>(null)
const saving = ref(false)
const form = reactive({ name: '', spec: '', unit: '', usageInstruction: '' })

function drugStatusText(s: string) {
  const m: Record<string, string> = { ACTIVE: '启用', DISABLED: '禁用' }
  return m[s] ?? s
}

async function load() {
  const params = new URLSearchParams()
  params.set('page', String(page.value - 1))
  params.set('size', String(size.value))
  if (keyword.value) params.set('keyword', keyword.value)
  const res = await get<{ content: any[]; totalElements: number }>(`/admin/drugs?${params}`)
  list.value = res?.content ?? []
  total.value = res?.totalElements ?? 0
}

function openEdit(row: any) {
  editing.value = row
  form.name = row?.name ?? ''
  form.spec = row?.spec ?? ''
  form.unit = row?.unit ?? ''
  form.usageInstruction = row?.usageInstruction ?? ''
  editVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入药品名称')
    return
  }
  saving.value = true
  try {
    if (editing.value?.id) {
      await put(`/admin/drugs/${editing.value.id}`, form)
    } else {
      await post('/admin/drugs', form)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

async function updateStatus(row: any, status: string) {
  try {
    await put(`/admin/drugs/${row.id}`, { ...row, status })
    ElMessage.success('操作成功')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.drugs { width: 100%; }
.toolbar { display: flex; gap: 12px; align-items: center; }
</style>
