<template>
  <div class="merchant-management">
    <!-- Header -->
    <div class="page-header">
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加商家
      </el-button>
      <el-input
        v-model="keyword"
        placeholder="搜索商家名称/地址"
        style="width: 260px"
        clearable
        @keyup.enter="loadMerchants"
        @clear="loadMerchants"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="总商家数" :value="stats.total || 0" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="营业中" :value="stats.active || 0">
            <template #suffix>
              <el-tag type="success" size="small">活跃</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="已停业" :value="stats.inactive || 0">
            <template #suffix>
              <el-tag type="info" size="small">停业</el-tag>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table :data="merchants" v-loading="loading" style="width: 100%" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商家信息" min-width="200">
        <template #default="{ row }">
          <div class="merchant-info-cell">
            <el-avatar v-if="row.logo" :size="40" :src="row.logo" />
            <el-avatar v-else :size="40">{{ (row.name || '').charAt(0) }}</el-avatar>
            <div>
              <div class="merchant-name">{{ row.name }}</div>
              <div class="merchant-addr">{{ row.address || '-' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ MerchantTypes[row.type] || row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="营业时间" width="140">
        <template #default="{ row }">{{ row.businessHours || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="MerchantStatuses[row.status]?.type || 'info'" size="small">
            {{ MerchantStatuses[row.status]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 'ACTIVE' ? '停业' : '营业' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <el-pagination
      v-if="total > 0"
      class="pagination"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="handlePageChange"
    />

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商家' : '添加商家'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-form-item label="商家名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商家名称" />
        </el-form-item>
        <el-form-item label="商家类型" prop="type">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option v-for="(label, key) in MerchantTypes" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="form.logo" placeholder="商家Logo图片链接" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.businessHours" placeholder="如：09:00-21:00" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商家简介" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getAdminMerchants,
  createMerchant,
  updateMerchant,
  deleteMerchant,
  updateMerchantStatus,
  getMerchantStats,
  MerchantStatuses,
  MerchantTypes
} from '@/services/adminShop'

const merchants = ref([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref({})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  type: 'BOTH',
  phone: '',
  address: '',
  logo: '',
  businessHours: '',
  description: '',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择商家类型', trigger: 'change' }]
}

function formatDate(d) {
  if (!d) return '-'
  return d.replace('T', ' ').substring(0, 19)
}

function resetForm() {
  form.name = ''; form.type = 'BOTH'; form.phone = ''
  form.address = ''; form.logo = ''; form.businessHours = ''
  form.description = ''; form.sortOrder = 0
  isEdit.value = false; editingId.value = null
}

function openDialog(row = null) {
  resetForm()
  if (row) {
    isEdit.value = true
    editingId.value = row.id
    form.name = row.name || ''
    form.type = row.type || 'BOTH'
    form.phone = row.phone || ''
    form.address = row.address || ''
    form.logo = row.logo || ''
    form.businessHours = row.businessHours || ''
    form.description = row.description || ''
    form.sortOrder = row.sortOrder || 0
  }
  dialogVisible.value = true
}

async function loadMerchants() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      keyword: keyword.value || undefined
    }
    const res = await getAdminMerchants(params)
    const data = res.data?.data || res.data
    merchants.value = data?.content || []
    total.value = data?.totalElements || 0
  } catch (e) {
    ElMessage.error('加载商家列表失败')
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await getMerchantStats()
    stats.value = res.data?.data || res.data || {}
  } catch {}
}

function handlePageChange(page) {
  currentPage.value = page
  loadMerchants()
}

async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch { return }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateMerchant(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createMerchant({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadMerchants()
    loadStats()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await updateMerchantStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success('状态已更新')
    loadStats()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商家「${row.name}」？此操作不可逆。`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMerchant(row.id)
    ElMessage.success('删除成功')
    loadMerchants()
    loadStats()
  } catch {}
}

onMounted(() => {
  loadMerchants()
  loadStats()
})
</script>

<style scoped>
.merchant-management {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.merchant-info-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.merchant-name {
  font-weight: 500;
  font-size: 14px;
}

.merchant-addr {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
