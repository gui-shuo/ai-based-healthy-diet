<template>
  <el-card class="invitation-panel">
    <template #header>
      <div class="card-header">
        <span class="title">邀请好友</span>
        <el-tag type="success">每人+50成长值</el-tag>
      </div>
    </template>

    <div class="invitation-content">
      <!-- 邀请码展示 -->
      <div class="invitation-code-section">
        <div class="code-label">我的邀请码</div>
        <div class="code-display">
          <span class="code">{{ invitationCode }}</span>
          <el-button type="primary" size="small" @click="copyInvitationCode">
            <el-icon><DocumentCopy /></el-icon>
            复制
          </el-button>
        </div>
      </div>

      <!-- 邀请链接 -->
      <div class="invitation-link-section">
        <div class="link-label">邀请链接</div>
        <el-input
          v-model="invitationLink"
          readonly
          size="large"
        >
          <template #append>
            <el-button @click="copyInvitationLink">
              <el-icon><Link /></el-icon>
              复制链接
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 邀请统计 -->
      <div class="invitation-stats">
        <div class="stat-box">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ memberInfo?.invitationCount || 0 }}</span>
            <span class="stat-label">已邀请</span>
          </div>
        </div>
        <div class="stat-box">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <el-icon><Present /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ totalReward }}</span>
            <span class="stat-label">奖励成长值</span>
          </div>
        </div>
      </div>

      <!-- 邀请记录 -->
      <div class="invitation-records">
        <div class="records-header">
          <span>最近邀请</span>
          <el-button text size="small" @click="viewAllRecords">查看全部</el-button>
        </div>
        <el-skeleton :loading="loading" animated :rows="3">
          <div v-if="records.length > 0" class="records-list">
            <div v-for="record in records.slice(0, 3)" :key="record.id" class="record-item">
              <div class="record-avatar">
                <el-avatar :size="40">{{ record.inviteeName?.charAt(0) || '?' }}</el-avatar>
              </div>
              <div class="record-info">
                <span class="record-name">{{ record.inviteeName || '新用户' }}</span>
                <span class="record-time">{{ formatTime(record.acceptedAt) }}</span>
              </div>
              <div class="record-reward">
                <el-tag v-if="record.isRewarded" type="success" size="small">+{{ record.rewardGrowth }}</el-tag>
                <el-tag v-else type="info" size="small">待接受</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无邀请记录" :image-size="80" />
        </el-skeleton>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { DocumentCopy, Link, User, Present } from '@element-plus/icons-vue'
import { useClipboard } from '@vueuse/core'
import { generateInvitationLink, getInvitationRecords } from '@/services/member'
import message from '@/utils/message'

const props = defineProps({
  memberInfo: {
    type: Object,
    default: null
  }
})

const { copy } = useClipboard()
const loading = ref(false)
const invitationCode = computed(() => props.memberInfo?.invitationCode || '')
const invitationLink = computed(() => props.memberInfo?.invitationLink || '')
const records = ref([])

// 总奖励成长值
const totalReward = computed(() => {
  return (props.memberInfo?.invitationCount || 0) * 50
})

// 复制邀请码
const copyInvitationCode = async () => {
  try {
    await copy(invitationCode.value)
    message.success('邀请码已复制到剪贴板')
  } catch (error) {
    message.error('复制失败')
  }
}

// 复制邀请链接
const copyInvitationLink = async () => {
  try {
    await copy(invitationLink.value)
    message.success('邀请链接已复制到剪贴板')
  } catch (error) {
    message.error('复制失败')
  }
}

// 获取邀请记录
const fetchInvitationRecords = async () => {
  loading.value = true
  try {
    const response = await getInvitationRecords(0, 5)
    if (response.data.code === 200) {
      records.value = response.data.data.content || []
    }
  } catch (error) {
    console.error('获取邀请记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看全部记录
const viewAllRecords = () => {
  // TODO: 打开邀请记录对话框或跳转到详情页
  message.info('查看全部记录功能开发中')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchInvitationRecords()
})
</script>

<style scoped lang="scss">
.invitation-panel {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
    }
  }

  .invitation-content {
    .invitation-code-section {
      margin-bottom: 24px;

      .code-label {
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 12px;
      }

      .code-display {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
        border-radius: 8px;
        border: 2px dashed #667eea;

        .code {
          flex: 1;
          font-size: 20px;
          font-weight: 600;
          color: #667eea;
          letter-spacing: 2px;
          font-family: 'Courier New', monospace;
        }
      }
    }

    .invitation-link-section {
      margin-bottom: 24px;

      .link-label {
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 12px;
      }
    }

    .invitation-stats {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-bottom: 24px;

      .stat-box {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        background: #f9fafb;
        border-radius: 8px;

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 24px;
        }

        .stat-info {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #1f2937;
          }

          .stat-label {
            font-size: 13px;
            color: #6b7280;
          }
        }
      }
    }

    .invitation-records {
      .records-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
      }

      .records-list {
        .record-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          border-radius: 8px;
          transition: all 0.3s;

          &:hover {
            background: #f9fafb;
          }

          &:not(:last-child) {
            border-bottom: 1px solid #f3f4f6;
          }

          .record-avatar {
            flex-shrink: 0;
          }

          .record-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 4px;

            .record-name {
              font-size: 14px;
              font-weight: 500;
              color: #1f2937;
            }

            .record-time {
              font-size: 12px;
              color: #9ca3af;
            }
          }

          .record-reward {
            flex-shrink: 0;
          }
        }
      }
    }
  }
}
</style>
