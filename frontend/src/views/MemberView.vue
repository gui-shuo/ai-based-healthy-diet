<template>
  <div class="member-container">
    <div class="member-layout">
      <!-- 返回首页 -->
      <div class="back-home">
        <el-button :icon="ArrowLeft" @click="router.push('/')">返回首页</el-button>
      </div>

      <!-- 会员信息卡片（顶部全宽） -->
      <div class="member-header">
        <MemberInfoCard :member-info="memberInfo" :loading="loading" />
      </div>

      <!-- 主要内容区 -->
      <div class="member-content">
        <!-- 左侧：成长值图表 + 签到日历 -->
        <div class="left-section">
          <GrowthChart :user-id="userId" />
          <SignInCalendar @signed="fetchMemberInfo" />
        </div>

        <!-- 右侧：VIP充值 + 权益列表 -->
        <div class="right-section">
          <VipPurchasePanel @vip-activated="fetchMemberInfo" />
          <BenefitsList
            :benefits="currentBenefits"
            :level-name="memberInfo?.currentLevel?.levelName"
          />
        </div>
      </div>

      <!-- 底部：邀请好友（全宽） -->
      <div class="bottom-section">
        <InvitationPanel :member-info="memberInfo" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getMemberInfo } from '@/services/member'
import MemberInfoCard from '@/components/member/MemberInfoCard.vue'
import GrowthChart from '@/components/member/GrowthChart.vue'
import InvitationPanel from '@/components/member/InvitationPanel.vue'
import BenefitsList from '@/components/member/BenefitsList.vue'
import SignInCalendar from '@/components/member/SignInCalendar.vue'
import VipPurchasePanel from '@/components/member/VipPurchasePanel.vue'
import message from '@/utils/message'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const userId = computed(() => authStore.user?.id)

const loading = ref(false)
const memberInfo = ref(null)

const currentBenefits = computed(() => memberInfo.value?.currentLevel?.benefits || {})

const fetchMemberInfo = async () => {
  loading.value = true
  try {
    const res = await getMemberInfo()
    if (res.data.code === 200) {
      memberInfo.value = res.data.data
    } else {
      message.error(res.data.message || '获取会员信息失败')
    }
  } catch (err) {
    console.error('获取会员信息失败:', err)
    message.error('获取会员信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMemberInfo()
})

onBeforeUnmount(() => {
  // 清理可能残留的 Element Plus 弹层
  setTimeout(() => {
    document.querySelectorAll('body > .el-overlay').forEach(el => el.remove())
  }, 50)
})
</script>

<style scoped lang="scss">
.member-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.member-layout {
  max-width: 1400px;
  margin: 0 auto;
}

.back-home {
  margin-bottom: 16px;
}

.member-header {
  margin-bottom: 24px;
}

.member-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.left-section,
.right-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.bottom-section {
  width: 100%;
}
</style>
