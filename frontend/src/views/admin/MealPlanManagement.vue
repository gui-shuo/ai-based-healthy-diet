<template>
  <div class="mealplan-management">
    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索膳食计划" clearable style="width: 220px"
            @keyup.enter="fetchList" @clear="fetchList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterPlanType" placeholder="计划类型" clearable style="width: 140px" @change="fetchList">
            <el-option v-for="(label, code) in PlanTypeMap" :key="code" :label="label" :value="code" />
          </el-select>
          <el-select v-model="filterDietGoal" placeholder="饮食目标" clearable style="width: 140px" @change="fetchList">
            <el-option v-for="(label, code) in DietGoalMap" :key="code" :label="label" :value="code" />
          </el-select>
        </div>
        <el-button type="primary" @click="openForm()">
          <el-icon><Plus /></el-icon> 新建计划
        </el-button>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="list-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="封面" width="70" align="center">
          <template #default="{ row }">
            <el-image :src="row.coverImage" style="width:40px;height:40px;border-radius:6px" fit="cover">
              <template #error>
                <div class="image-placeholder">无图</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column label="计划类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ PlanTypeMap[row.planType] || row.planType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="饮食目标" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ DietGoalMap[row.dietGoal] || row.dietGoal }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标热量" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.targetCalories ?? '-' }} kcal</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationDays" label="天数" width="70" align="center" />
        <el-table-column prop="suitableCrowd" label="适用人群" min-width="110" show-overflow-tooltip />
        <el-table-column label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="difficultyTagType(row.difficulty)">{{ DifficultyMap[row.difficulty] || row.difficulty || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.tags">{{ row.tags }}</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.ratingCount">⭐ {{ Number(row.avgRating).toFixed(1) }} ({{ row.ratingCount }})</span>
            <span v-else style="color:#94a3b8">-</span>
          </template>
        </el-table-column>
        <el-table-column label="跟随" width="70" align="center">
          <template #default="{ row }">{{ row.followCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="精选" width="70" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.isFeatured" size="small" @change="toggleFeatured(row)" />
          </template>
        </el-table-column>
        <el-table-column label="上架" width="70" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.isActive" size="small" @change="toggleActive(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="openForm(row)">编辑</el-button>
            <el-popconfirm title="确定删除此计划？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small" text>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="page" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑膳食计划' : '新建膳食计划'" width="920px" destroy-on-close top="3vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="mealplan-form">
        <!-- 基础信息 -->
        <div class="form-section-title">基础信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图URL">
              <el-input v-model="form.coverImage" placeholder="输入封面图片地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="form.planType" style="width:100%">
                <el-option v-for="(label, code) in PlanTypeMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="饮食目标" prop="dietGoal">
              <el-select v-model="form.dietGoal" style="width:100%">
                <el-option v-for="(label, code) in DietGoalMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="天数" prop="durationDays">
              <el-input-number v-model="form.durationDays" :min="1" :max="30" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="适用人群">
              <el-input v-model="form.suitableCrowd" placeholder="如：上班族、学生、老年人" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width:100%">
                <el-option v-for="(label, code) in DifficultyMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="精选">
              <el-switch v-model="form.isFeatured" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="逗号分隔，如：素食,低卡,养生" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 目标营养 -->
        <div class="form-section-title">目标营养</div>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="热量">
              <el-input-number v-model="form.targetCalories" :min="0" :precision="0" style="width:100%" />
              <span class="unit-label">kcal</span>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="蛋白质">
              <el-input-number v-model="form.targetProtein" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="脂肪">
              <el-input-number v-model="form.targetFat" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="碳水">
              <el-input-number v-model="form.targetCarbs" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 每日计划 -->
        <div class="form-section-title">
          每日安排
          <el-button type="primary" size="small" text @click="addDay">
            <el-icon><Plus /></el-icon> 添加一天
          </el-button>
        </div>
        <el-tabs v-if="form.days.length" v-model="activeDay" type="card" closable @tab-remove="removeDay">
          <el-tab-pane
            v-for="(day, dIdx) in form.days"
            :key="dIdx"
            :label="'第 ' + (dIdx + 1) + ' 天'"
            :name="String(dIdx)"
          >
            <div class="day-nutrition">
              <el-row :gutter="12">
                <el-col :span="6">
                  <el-form-item label="热量" label-width="60px">
                    <el-input-number v-model="day.totalCalories" :min="0" size="small" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="蛋白质" label-width="60px">
                    <el-input-number v-model="day.totalProtein" :min="0" :precision="1" size="small" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="脂肪" label-width="60px">
                    <el-input-number v-model="day.totalFat" :min="0" :precision="1" size="small" style="width:100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="碳水" label-width="60px">
                    <el-input-number v-model="day.totalCarbs" :min="0" :precision="1" size="small" style="width:100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <div class="items-header">
              <span class="items-title">餐食安排</span>
              <el-button type="primary" size="small" text @click="addItem(dIdx)">
                <el-icon><Plus /></el-icon> 添加餐食
              </el-button>
            </div>

            <div v-for="(item, iIdx) in day.items" :key="iIdx" class="dynamic-row">
              <el-row :gutter="8" align="middle">
                <el-col :span="4">
                  <el-select v-model="item.mealType" size="small" style="width:100%">
                    <el-option v-for="(label, code) in MealTypeMap" :key="code" :label="label" :value="code" />
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-input v-model="item.foodName" size="small" placeholder="食物名称" />
                </el-col>
                <el-col :span="3">
                  <el-input v-model="item.portion" size="small" placeholder="份量" />
                </el-col>
                <el-col :span="3">
                  <el-input-number v-model="item.calories" :min="0" size="small" style="width:100%" placeholder="热量" />
                </el-col>
                <el-col :span="2">
                  <el-input-number v-model="item.protein" :min="0" :precision="1" size="small" style="width:100%" controls-position="right" />
                </el-col>
                <el-col :span="2">
                  <el-input-number v-model="item.fat" :min="0" :precision="1" size="small" style="width:100%" controls-position="right" />
                </el-col>
                <el-col :span="2">
                  <el-input-number v-model="item.carbs" :min="0" :precision="1" size="small" style="width:100%" controls-position="right" />
                </el-col>
                <el-col :span="2">
                  <el-input-number v-model="item.recipeId" :min="0" size="small" style="width:100%" placeholder="食谱ID" controls-position="right" />
                </el-col>
                <el-col :span="2">
                  <el-button type="danger" size="small" text @click="day.items.splice(iIdx, 1)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-col>
              </el-row>
            </div>
            <div v-if="!day.items.length" class="empty-items">暂无餐食，请点击上方按钮添加</div>
          </el-tab-pane>
        </el-tabs>
        <div v-else class="empty-days">暂无每日计划，请点击上方按钮添加</div>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/services/api'

const PlanTypeMap = {
  DAILY: '每日计划',
  WEEKLY: '每周计划'
}

const DietGoalMap = {
  BALANCED: '均衡饮食',
  WEIGHT_LOSS: '减脂塑形',
  MUSCLE_GAIN: '增肌增重',
  WELLNESS: '养生调理',
  DIABETES_FRIENDLY: '糖尿病适用',
  PREGNANCY: '孕期营养',
  POSTPARTUM: '产后恢复',
  ELDERLY: '老年营养'
}

const MealTypeMap = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
  SNACK: '加餐'
}

const DifficultyMap = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

const difficultyTagType = (d) => {
  if (d === 'EASY') return 'success'
  if (d === 'HARD') return 'danger'
  return 'warning'
}

// 列表状态
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const keyword = ref('')
const filterPlanType = ref('')
const filterDietGoal = ref('')

// 表单状态
const formVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const editId = ref(null)
const activeDay = ref('0')

const createDefaultItem = () => ({
  mealType: 'BREAKFAST',
  recipeId: null,
  foodName: '',
  portion: '',
  calories: 0,
  protein: 0,
  fat: 0,
  carbs: 0
})

const createDefaultDay = () => ({
  dayNumber: 1,
  totalCalories: 0,
  totalProtein: 0,
  totalFat: 0,
  totalCarbs: 0,
  items: []
})

const defaultForm = () => ({
  title: '',
  description: '',
  coverImage: '',
  planType: 'DAILY',
  dietGoal: 'BALANCED',
  targetCalories: 2000,
  targetProtein: 60,
  targetFat: 65,
  targetCarbs: 250,
  durationDays: 1,
  suitableCrowd: '',
  tags: '',
  difficulty: 'MEDIUM',
  isFeatured: false,
  days: []
})

const form = reactive(defaultForm())

const rules = {
  title: [{ required: true, message: '请输入计划标题', trigger: 'blur' }],
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  dietGoal: [{ required: true, message: '请选择饮食目标', trigger: 'change' }],
  durationDays: [{ required: true, message: '请输入天数', trigger: 'blur' }]
}

const addDay = () => {
  const day = createDefaultDay()
  day.dayNumber = form.days.length + 1
  form.days.push(day)
  activeDay.value = String(form.days.length - 1)
}

const removeDay = (tabName) => {
  const idx = Number(tabName)
  form.days.splice(idx, 1)
  form.days.forEach((d, i) => { d.dayNumber = i + 1 })
  if (Number(activeDay.value) >= form.days.length) {
    activeDay.value = String(Math.max(0, form.days.length - 1))
  }
}

const addItem = (dayIdx) => {
  form.days[dayIdx].items.push(createDefaultItem())
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (filterPlanType.value) params.planType = filterPlanType.value
    if (filterDietGoal.value) params.dietGoal = filterDietGoal.value
    const res = await api.get('/admin/meal-plans', { params })
    if (res.data.code === 200) {
      list.value = res.data.data.content || []
      total.value = res.data.data.totalElements || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openForm = async (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    try {
      const res = await api.get(`/admin/meal-plans/${row.id}`)
      if (res.data.code === 200) {
        const data = res.data.data
        Object.assign(form, {
          title: data.title || '',
          description: data.description || '',
          coverImage: data.coverImage || '',
          planType: data.planType || 'DAILY',
          dietGoal: data.dietGoal || 'BALANCED',
          targetCalories: data.targetCalories ?? 2000,
          targetProtein: data.targetProtein ?? 60,
          targetFat: data.targetFat ?? 65,
          targetCarbs: data.targetCarbs ?? 250,
          durationDays: data.durationDays ?? 1,
          suitableCrowd: data.suitableCrowd || '',
          tags: data.tags || '',
          difficulty: data.difficulty || 'MEDIUM',
          isFeatured: data.isFeatured || false,
          days: (data.days || []).map(d => ({
            dayNumber: d.dayNumber || 1,
            totalCalories: d.totalCalories ?? 0,
            totalProtein: d.totalProtein ?? 0,
            totalFat: d.totalFat ?? 0,
            totalCarbs: d.totalCarbs ?? 0,
            items: (d.items || []).map(it => ({
              mealType: it.mealType || 'BREAKFAST',
              recipeId: it.recipeId ?? null,
              foodName: it.foodName || '',
              portion: it.portion || '',
              calories: it.calories ?? 0,
              protein: it.protein ?? 0,
              fat: it.fat ?? 0,
              carbs: it.carbs ?? 0
            }))
          }))
        })
        activeDay.value = '0'
      }
    } catch (e) {
      ElMessage.error('获取计划详情失败')
      return
    }
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, defaultForm())
    form.days = []
    activeDay.value = '0'
  }
  formVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      ...form,
      days: form.days.map((d, dIdx) => ({
        ...d,
        dayNumber: dIdx + 1,
        items: d.items.map(it => ({
          ...it,
          recipeId: it.recipeId || null
        }))
      }))
    }
    const res = isEdit.value
      ? await api.put(`/admin/meal-plans/${editId.value}`, payload)
      : await api.post('/admin/meal-plans', payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    const res = await api.delete(`/admin/meal-plans/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      fetchList()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const toggleFeatured = async (row) => {
  try {
    const res = await api.put(`/admin/meal-plans/${row.id}/toggle-featured`)
    if (res.data.code === 200) {
      ElMessage.success(row.isFeatured ? '已设为精选' : '已取消精选')
    } else {
      row.isFeatured = !row.isFeatured
    }
  } catch (e) {
    row.isFeatured = !row.isFeatured
    ElMessage.error('操作失败')
  }
}

const toggleActive = async (row) => {
  try {
    const res = await api.put(`/admin/meal-plans/${row.id}/toggle-active`)
    if (res.data.code === 200) {
      ElMessage.success(row.isActive ? '已上架' : '已下架')
    } else {
      row.isActive = !row.isActive
    }
  } catch (e) {
    row.isActive = !row.isActive
    ElMessage.error('操作失败')
  }
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.mealplan-management {
  font-family: 'Inter', sans-serif;

  .toolbar-card {
    margin-bottom: 16px;
    border: 1px solid #E2E8F0;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    background: #FFFFFF;

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .toolbar-left {
        display: flex;
        gap: 12px;
      }
    }
  }

  .list-card {
    border: 1px solid #E2E8F0;
    border-radius: 12px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
    background: #FFFFFF;

    :deep(.el-table th.el-table__cell) {
      background: #F1F5F9;
      color: #0F172A;
      font-family: 'Inter', sans-serif;
      font-weight: 600;
      border-bottom: 1px solid #E2E8F0;
    }

    :deep(.el-table td.el-table__cell) {
      border-bottom: 1px solid #E2E8F0;
    }

    :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
      background: #FAFAFA;
    }
  }

  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
  }

  .image-placeholder {
    width: 40px;
    height: 40px;
    background: #f5f5f5;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #ccc;
  }
}

.mealplan-form {
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 8px;

  .form-section-title {
    font-size: 15px;
    font-weight: 600;
    color: #0F172A;
    margin: 20px 0 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #E2E8F0;
    display: flex;
    align-items: center;
    justify-content: space-between;

    &:first-child {
      margin-top: 0;
    }
  }

  .unit-label {
    margin-left: 4px;
    font-size: 12px;
    color: #94a3b8;
  }

  .day-nutrition {
    background: #F8FAFC;
    border: 1px solid #E2E8F0;
    border-radius: 8px;
    padding: 12px 12px 0;
    margin-bottom: 12px;
  }

  .items-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .items-title {
      font-weight: 600;
      font-size: 13px;
      color: #334155;
    }
  }

  .dynamic-row {
    background: #FAFAFA;
    border: 1px solid #E2E8F0;
    border-radius: 8px;
    padding: 8px 10px;
    margin-bottom: 6px;
  }

  .empty-items,
  .empty-days {
    text-align: center;
    color: #94a3b8;
    padding: 24px 0;
    font-size: 13px;
  }
}

:deep(.el-tabs--card > .el-tabs__header .el-tabs__item.is-active) {
  color: #10B981;
  border-bottom-color: #FFFFFF;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #10B981;
  --el-button-border-color: #10B981;
  --el-button-hover-bg-color: #059669;
  --el-button-hover-border-color: #059669;
  --el-button-active-bg-color: #047857;
  --el-button-active-border-color: #047857;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #10B981;
  border-color: #10B981;
}
</style>
