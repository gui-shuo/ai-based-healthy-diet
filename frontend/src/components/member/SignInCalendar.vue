<template>
  <el-card class="sign-in-calendar">
    <template #header>
      <div class="card-header">
        <span class="title">每日签到</span>
        <span class="month-label">{{ currentYear }}年{{ currentMonth }}月</span>
      </div>
    </template>

    <div class="calendar-body">
      <!-- 签到统计 -->
      <div class="sign-stats">
        <div class="stat-item">
          <span class="stat-num">{{ signedDays.length }}</span>
          <span class="stat-label">本月已签</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num">{{ continuousDays }}</span>
          <span class="stat-label">连续天数</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num">{{ signedDays.length * 10 }}</span>
          <span class="stat-label">本月成长值</span>
        </div>
      </div>

      <!-- 日历格子 -->
      <div class="calendar-grid">
        <div v-for="day in weekDays" :key="day" class="week-label">{{ day }}</div>
        <div v-for="n in firstDayOffset" :key="'empty-' + n" class="day-cell empty" />
        <div
          v-for="day in daysInMonth"
          :key="day"
          class="day-cell"
          :class="{
            signed: signedDays.includes(day),
            today: day === todayDay,
            future: day > todayDay
          }"
        >
          <span class="day-num">{{ day }}</span>
          <el-icon v-if="signedDays.includes(day)" class="check-icon"><CircleCheck /></el-icon>
        </div>
      </div>

      <!-- 签到按钮 -->
      <div class="sign-action">
        <el-button
          type="warning"
          size="large"
          :loading="loading"
          :disabled="todaySigned"
          round
          class="sign-btn"
          @click="handleSignIn"
        >
          <el-icon><Calendar /></el-icon>
          {{ todaySigned ? '今日已签到 ✓' : '立即签到 +10成长值' }}
        </el-button>
      </div>

      <!-- 签到提示 -->
      <div class="sign-tip">
        <el-icon><InfoFilled /></el-icon>
        每日签到可获得 10 成长值，积累升级会员等级
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Calendar, CircleCheck, InfoFilled } from '@element-plus/icons-vue'
import { dailySignIn, getSignInCalendar } from '@/services/member'
import message from '@/utils/message'

const emit = defineEmits(['signed'])

const loading = ref(false)
const signedDays = ref([])

const now = new Date()
const currentYear = now.getFullYear()
const currentMonth = now.getMonth() + 1
const todayDay = now.getDate()

const daysInMonth = computed(() => {
  return new Date(currentYear, currentMonth, 0).getDate()
})

// 本月第一天是周几（0=周日，1=周一...）转换为以周一为第一列
const firstDayOffset = computed(() => {
  const day = new Date(currentYear, currentMonth - 1, 1).getDay()
  return day === 0 ? 6 : day - 1
})

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const todaySigned = computed(() => signedDays.value.includes(todayDay))

// 计算连续签到天数（从今天往前数）
const continuousDays = computed(() => {
  let count = 0
  for (let d = todayDay; d >= 1; d--) {
    if (signedDays.value.includes(d)) {
      count++
    } else {
      break
    }
  }
  return count
})

const fetchCalendar = async () => {
  try {
    const res = await getSignInCalendar()
    if (res.data.code === 200) {
      signedDays.value = res.data.data || []
    }
  } catch (err) {
    console.error('获取签到日历失败:', err)
  }
}

const handleSignIn = async () => {
  if (todaySigned.value) return
  loading.value = true
  try {
    const res = await dailySignIn()
    if (res.data.code === 200 && res.data.data > 0) {
      signedDays.value = [...signedDays.value, todayDay]
      message.success(res.data.message || '签到成功！获得10成长值')
      emit('signed')
    } else {
      message.info('今日已签到')
    }
  } catch (err) {
    console.error('签到失败:', err)
    message.error('签到失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCalendar()
})
</script>

<style scoped lang="scss">
.sign-in-calendar {
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

    .month-label {
      font-size: 14px;
      color: #6b7280;
    }
  }

  .calendar-body {
    .sign-stats {
      display: flex;
      align-items: center;
      justify-content: space-around;
      padding: 12px 0 20px;
      border-bottom: 1px solid #f3f4f6;
      margin-bottom: 16px;

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;

        .stat-num {
          font-size: 24px;
          font-weight: 700;
          color: #f59e0b;
        }

        .stat-label {
          font-size: 12px;
          color: #9ca3af;
        }
      }

      .stat-divider {
        width: 1px;
        height: 36px;
        background: #e5e7eb;
      }
    }

    .calendar-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 4px;
      margin-bottom: 20px;

      .week-label {
        text-align: center;
        font-size: 12px;
        color: #9ca3af;
        padding: 4px 0;
        font-weight: 500;
      }

      .day-cell {
        position: relative;
        aspect-ratio: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        font-size: 13px;
        color: #374151;
        background: #f9fafb;
        transition: all 0.2s;

        &.empty {
          background: transparent;
        }

        &.signed {
          background: linear-gradient(135deg, #f59e0b, #fbbf24);
          color: white;

          .check-icon {
            position: absolute;
            bottom: 2px;
            right: 2px;
            font-size: 10px;
            color: white;
          }
        }

        &.today:not(.signed) {
          border: 2px solid #f59e0b;
          color: #f59e0b;
          font-weight: 700;
          background: #fffbeb;
        }

        &.future {
          color: #d1d5db;
          background: #f9fafb;
        }

        .day-num {
          font-size: 13px;
          line-height: 1;
        }
      }
    }

    .sign-action {
      text-align: center;
      margin-bottom: 12px;

      .sign-btn {
        width: 80%;
        font-size: 15px;
        font-weight: 600;
      }
    }

    .sign-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      font-size: 12px;
      color: #9ca3af;
    }
  }
}
</style>
