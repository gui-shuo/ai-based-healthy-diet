<template>
  <div class="community-mgmt">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <el-card v-for="s in statItems" :key="s.label" class="stat-card" shadow="hover">
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </el-card>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-select v-model="filters.status" placeholder="帖子状态" clearable style="width:120px" @change="loadPosts(true)">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="已隐藏" value="HIDDEN" />
          <el-option label="已删除" value="DELETED" />
        </el-select>
        <el-select v-model="filters.category" placeholder="分类" clearable style="width:120px" @change="loadPosts(true)">
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>
        <el-button @click="loadPosts(true)">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </el-card>

    <!-- 帖子列表 -->
    <el-card shadow="never">
      <el-table :data="posts" v-loading="loading" stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="用户" width="120">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px">
              <el-avatar :size="24" :src="row.avatarUrl">{{ (row.username || '').charAt(0) }}</el-avatar>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" prop="category" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="200">
          <template #default="{ row }">
            <div class="content-cell">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="媒体" width="80" align="center">
          <template #default="{ row }">
            <span v-if="getImageCount(row.images)">🖼️{{ getImageCount(row.images) }}</span>
            <span v-if="row.videoUrl"> 🎬</span>
            <span v-if="!getImageCount(row.images) && !row.videoUrl">-</span>
          </template>
        </el-table-column>
        <el-table-column label="赞/评" width="80" align="center">
          <template #default="{ row }">
            {{ row.likesCount || 0 }}/{{ row.commentsCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="70" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.pinned" @change="handleTogglePin(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ACTIVE'" type="warning" link size="small" @click="handleHide(row)">隐藏</el-button>
            <el-button v-if="row.status === 'HIDDEN'" type="success" link size="small" @click="handleRestore(row)">恢复</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  adminGetPosts, adminTogglePin, adminUpdateStatus, adminDeletePost, adminGetStats
} from '@/services/community'

const categories = ['饮食打卡', '食谱分享', '健身日记', '营养知识', '减脂心得', '问答交流']

const loading = ref(false)
const posts = ref([])
const page = ref(1)
const total = ref(0)
const filters = reactive({ status: '', category: '' })
const stats = ref({ total: 0, active: 0, deleted: 0 })

const statItems = computed(() => [
  { label: '总帖子数', value: stats.value.total || 0 },
  { label: '正常帖子', value: stats.value.active || 0 },
  { label: '已删除', value: stats.value.deleted || 0 }
])

function getImageCount(images) {
  if (!images) return 0
  try { return (typeof images === 'string' ? JSON.parse(images) : images).length } catch { return 0 }
}

function statusType(s) {
  return { ACTIVE: 'success', HIDDEN: 'warning', DELETED: 'danger' }[s] || 'info'
}
function statusLabel(s) {
  return { ACTIVE: '正常', HIDDEN: '已隐藏', DELETED: '已删除' }[s] || s
}
function formatDate(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

async function loadStats() {
  try {
    const res = await adminGetStats()
    stats.value = res.data?.data || res.data || {}
  } catch { /* ignore */ }
}

async function loadPosts(reset = false) {
  if (reset) page.value = 1
  loading.value = true
  try {
    const params = { page: page.value - 1, size: 20 }
    if (filters.status) params.status = filters.status
    if (filters.category) params.category = filters.category
    const res = await adminGetPosts(params)
    const data = res.data?.data || res.data
    posts.value = data.content || []
    total.value = data.totalElements || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(p) {
  page.value = p
  loadPosts()
}

async function handleTogglePin(row) {
  try {
    await adminTogglePin(row.id)
    row.pinned = !row.pinned
    ElMessage.success(row.pinned ? '已置顶' : '已取消置顶')
  } catch { ElMessage.error('操作失败') }
}

async function handleHide(row) {
  try {
    await adminUpdateStatus(row.id, 'HIDDEN')
    row.status = 'HIDDEN'
    ElMessage.success('已隐藏')
  } catch { ElMessage.error('操作失败') }
}

async function handleRestore(row) {
  try {
    await adminUpdateStatus(row.id, 'ACTIVE')
    row.status = 'ACTIVE'
    ElMessage.success('已恢复')
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该帖子？此操作不可恢复', '删除确认', { type: 'warning' })
    await adminDeletePost(row.id)
    ElMessage.success('已删除')
    loadPosts()
    loadStats()
  } catch { /* cancel */ }
}

onMounted(() => {
  loadPosts()
  loadStats()
})
</script>

<style scoped>
.community-mgmt { display: flex; flex-direction: column; gap: 16px; font-family: 'Inter', sans-serif; }
.stat-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-cards :deep(.el-card) {
  text-align: center;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  background: #FFFFFF;
}
.stat-value { font-size: 28px; font-weight: 700; color: #0F172A; font-family: 'Inter', sans-serif; }
.stat-label { font-size: 13px; color: #0F172A; opacity: 0.55; margin-top: 4px; }
.filter-card :deep(.el-card__body) { background: #FFFFFF; }
.filter-card { border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05); }
.filter-card .filter-row { display: flex; gap: 12px; align-items: center; }

.community-mgmt > :deep(.el-card:last-of-type) {
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
  background: #FFFFFF;
}

.community-mgmt :deep(.el-table th.el-table__cell) {
  background: #F1F5F9;
  color: #0F172A;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  border-bottom: 1px solid #E2E8F0;
}
.community-mgmt :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #E2E8F0;
}
.community-mgmt :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #FAFAFA;
}

.content-cell {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 13px;
}
.pagination-row { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
