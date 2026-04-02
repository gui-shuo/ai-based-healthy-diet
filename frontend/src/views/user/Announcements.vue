<template>
  <div class="announcements-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="router.push('/')">返回首页</el-button>
      <h1>系统公告</h1>
      <p class="subtitle">查看最新的系统通知和公告信息</p>
    </div>

    <!-- 公告列表 -->
    <div v-loading="loading" class="announcements-list">
      <el-empty v-if="announcements.length === 0" description="暂无公告" />

      <div
        v-for="announcement in announcements"
        :key="announcement.id"
        class="announcement-card"
        :class="`type-${announcement.type}`"
      >
        <div class="card-header">
          <div class="header-left">
            <el-tag :type="getTypeTag(announcement.type)" size="large" effect="dark">
              {{ getTypeLabel(announcement.type) }}
            </el-tag>
            <h3 class="title">
              {{ announcement.title }}
            </h3>
          </div>
          <div class="header-right">
            <span class="date">{{ formatDate(announcement.createdAt) }}</span>
          </div>
        </div>

        <div class="card-content">
          <p class="content">
            {{ announcement.content }}
          </p>
        </div>

        <div v-if="announcement.startTime || announcement.endTime" class="card-footer">
          <el-icon><Clock /></el-icon>
          <span class="validity">
            有效期：{{ formatDate(announcement.startTime) }} 至
            {{ formatDate(announcement.endTime) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 30, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pagination"
      @size-change="loadAnnouncements"
      @current-change="loadAnnouncements"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, ArrowLeft } from '@element-plus/icons-vue'
import api from '@/services/api'

const router = useRouter()

const loading = ref(false)
const announcements = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const { data } = await api.get('/announcements', {
      params: { page: currentPage.value, size: pageSize.value }
    })

    if (data.code === 200) {
      announcements.value = data.data.content || []
      total.value = data.data.totalElements || 0
    } else {
      ElMessage.error('加载公告失败')
    }
  } catch (error) {
    console.error('加载公告失败:', error)
    ElMessage.error('加载公告失败')
  } finally {
    loading.value = false
  }
}

// 获取类型标签
const getTypeTag = type => {
  const typeMap = {
    NOTICE: 'info',
    WARNING: 'warning',
    ERROR: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取类型标签文本
const getTypeLabel = type => {
  const labelMap = {
    NOTICE: '通知',
    WARNING: '警告',
    ERROR: '紧急'
  }
  return labelMap[type] || '通知'
}

// 格式化日期
const formatDate = dateString => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Patrick Hand', cursive;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #2d2d2d;
  margin-bottom: 8px;
  font-family: 'Kalam', cursive;
}

.subtitle {
  font-size: 16px;
  color: #2d2d2d;
  opacity: 0.6;
  font-family: 'Patrick Hand', cursive;
}

.announcements-list {
  min-height: 400px;
}

.announcement-card {
  background: #fdfbf7;
  border-radius: 15px 225px 15px 255px / 255px 15px 225px 15px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 4px 4px 0px 0px #2d2d2d;
  border: 2px solid #2d2d2d;
  transition: all 0.15s ease;
  position: relative;
}

/* tack decoration */
.announcement-card::before {
  content: '📌';
  position: absolute;
  top: -10px;
  right: 24px;
  font-size: 22px;
  transform: rotate(15deg);
  filter: drop-shadow(1px 1px 0px rgba(45,45,45,0.3));
}

.announcement-card:hover {
  box-shadow: 2px 2px 0px 0px #2d2d2d;
  transform: translate(2px, 2px);
}

.announcement-card.type-WARNING {
  background: #fff9c4;
}

.announcement-card.type-ERROR {
  background: #fdfbf7;
  border-color: #ff4d4d;
  box-shadow: 4px 4px 0px 0px #ff4d4d;
}

.announcement-card.type-ERROR:hover {
  box-shadow: 2px 2px 0px 0px #ff4d4d;
  transform: translate(2px, 2px);
}

.announcement-card.type-ERROR::before {
  content: '🚨';
}

.announcement-card.type-WARNING::before {
  content: '⚠️';
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #2d2d2d;
  margin: 0;
  font-family: 'Kalam', cursive;
}

.header-right {
  display: flex;
  align-items: center;
}

.date {
  font-size: 14px;
  color: #2d2d2d;
  opacity: 0.5;
  font-family: 'Patrick Hand', cursive;
}

.card-content {
  margin-bottom: 16px;
}

.content {
  font-size: 15px;
  line-height: 1.8;
  color: #2d2d2d;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Patrick Hand', cursive;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  border-top: 2px dashed #e5e0d8;
  color: #2d2d2d;
  font-size: 14px;
  opacity: 0.6;
  font-family: 'Patrick Hand', cursive;
}

.validity {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcements-page {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .announcement-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
  }

  .title {
    font-size: 18px;
  }
}
</style>
