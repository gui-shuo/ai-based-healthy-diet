<template>
  <el-card class="level-comparison-table">
    <template #header>
      <div class="card-header">
        <span class="title">等级对比</span>
        <el-tag>5个等级</el-tag>
      </div>
    </template>

    <div class="table-wrapper">
      <el-table :data="levelData" style="width: 100%" stripe>
        <el-table-column label="等级" width="140">
          <template #default="{ row }">
            <div class="level-cell">
              <div class="level-badge" :style="{ background: row.color }">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="level-info">
                <span class="level-name">{{ row.levelName }}</span>
                <el-tag v-if="isCurrentLevel(row)" type="success" size="small">当前</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所需成长值" width="120" align="center">
          <template #default="{ row }">
            <span class="growth-value">{{ row.growthRequired }}</span>
          </template>
        </el-table-column>

        <el-table-column label="AI咨询" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.maxAiQueries)" size="small">
              {{ formatLimit(row.maxAiQueries) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="核心权益" min-width="200">
          <template #default="{ row }">
            <div class="benefits-preview">
              <el-tag
                v-for="(benefit, index) in row.keyBenefits.slice(0, 2)"
                :key="index"
                size="small"
                style="margin-right: 4px; margin-bottom: 4px"
              >
                {{ benefit }}
              </el-tag>
              <span v-if="row.keyBenefits.length > 2" class="more-benefits">
                +{{ row.keyBenefits.length - 2 }}项
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 升级提示 -->
    <div v-if="currentLevel" class="upgrade-hint">
      <el-alert
        v-if="nextLevel"
        type="info"
        :closable="false"
      >
        <template #title>
          <div class="alert-content">
            <span>再获得 <strong>{{ growthToNextLevel }}</strong> 成长值即可升级到{{ nextLevel.levelName }}</span>
            <el-button type="primary" size="small" text>查看攻略</el-button>
          </div>
        </template>
      </el-alert>
      <el-alert
        v-else
        type="success"
        :closable="false"
        title="恭喜！您已达到最高等级"
      />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { Trophy } from '@element-plus/icons-vue'

const props = defineProps({
  currentLevel: {
    type: Object,
    default: null
  }
})

// 等级数据
const levelData = [
  {
    levelCode: 'ROOKIE',
    levelName: '新手会员',
    color: '#95a5a6',
    growthRequired: 0,
    maxAiQueries: 3,
    keyBenefits: ['基础营养记录', 'AI咨询(3次/天)', '基础数据分析']
  },
  {
    levelCode: 'BRONZE',
    levelName: '青铜会员',
    color: '#cd7f32',
    growthRequired: 100,
    maxAiQueries: 10,
    keyBenefits: ['营养记录无限', 'AI咨询(10次/天)', '高级数据分析', '自定义目标']
  },
  {
    levelCode: 'SILVER',
    levelName: '白银会员',
    color: '#c0c0c0',
    growthRequired: 500,
    maxAiQueries: 30,
    keyBenefits: ['所有青铜权益', 'AI咨询(30次/天)', '专属营养报告', '优先客服']
  },
  {
    levelCode: 'GOLD',
    levelName: '黄金会员',
    color: '#ffd700',
    growthRequired: 2000,
    maxAiQueries: -1,
    keyBenefits: ['所有白银权益', 'AI咨询无限', '个性化食谱', '健康顾问', '数据导出']
  },
  {
    levelCode: 'PLATINUM',
    levelName: '铂金会员',
    color: '#e5e4e2',
    growthRequired: 5000,
    maxAiQueries: -1,
    keyBenefits: ['所有黄金权益', '专属营养师', '线下活动', '合作商家折扣', '成长值加成']
  }
]

// 判断是否当前等级
const isCurrentLevel = (row) => {
  return row.levelCode === props.currentLevel?.levelCode
}

// 下一等级
const nextLevel = computed(() => {
  if (!props.currentLevel) return null
  const currentIndex = levelData.findIndex(l => l.levelCode === props.currentLevel.levelCode)
  return currentIndex < levelData.length - 1 ? levelData[currentIndex + 1] : null
})

// 距离下一等级所需成长值
const growthToNextLevel = computed(() => {
  if (!props.currentLevel || !nextLevel.value) return 0
  return nextLevel.value.growthRequired - props.currentLevel.growthRequired
})

// 格式化限制
const formatLimit = (limit) => {
  return limit === -1 ? '无限' : `${limit}次/天`
}

// 获取Tag类型
const getTagType = (limit) => {
  if (limit === -1) return 'success'
  if (limit >= 30) return 'warning'
  if (limit >= 10) return 'info'
  return 'default'
}
</script>

<style scoped lang="scss">
.level-comparison-table {
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

  .table-wrapper {
    margin-bottom: 16px;

    .level-cell {
      display: flex;
      align-items: center;
      gap: 12px;

      .level-badge {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        flex-shrink: 0;
      }

      .level-info {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .level-name {
          font-size: 14px;
          font-weight: 600;
          color: #1f2937;
        }
      }
    }

    .growth-value {
      font-size: 16px;
      font-weight: 600;
      color: #667eea;
    }

    .benefits-preview {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 4px;

      .more-benefits {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }

  .upgrade-hint {
    .alert-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      strong {
        color: #667eea;
        font-size: 18px;
        margin: 0 4px;
      }
    }
  }
}
</style>
