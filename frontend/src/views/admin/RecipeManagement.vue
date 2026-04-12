<template>
  <div class="recipe-management">
    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索食谱标题" clearable style="width: 220px"
            @keyup.enter="fetchList" @clear="fetchList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterCategory" placeholder="分类筛选" clearable style="width: 130px" @change="fetchList">
            <el-option v-for="(label, code) in CategoryMap" :key="code" :label="label" :value="code" />
          </el-select>
          <el-select v-model="filterCuisine" placeholder="菜系筛选" clearable style="width: 130px" @change="fetchList">
            <el-option v-for="(label, code) in CuisineTypeMap" :key="code" :label="label" :value="code" />
          </el-select>
          <el-select v-model="filterDiet" placeholder="饮食类型" clearable style="width: 130px" @change="fetchList">
            <el-option v-for="(label, code) in DietTypeMap" :key="code" :label="label" :value="code" />
          </el-select>
        </div>
        <el-button type="primary" @click="openForm()">
          <el-icon><Plus /></el-icon> 新建食谱
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
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column label="分类" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ CategoryMap[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="菜系" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ CuisineTypeMap[row.cuisineType] || row.cuisineType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="饮食类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ DietTypeMap[row.dietType] || row.dietType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="70" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.difficulty === 'EASY' ? 'success' : row.difficulty === 'HARD' ? 'danger' : ''">
              {{ DifficultyMap[row.difficulty] || row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热量" width="80" align="center">
          <template #default="{ row }">
            <span>{{ row.caloriesPerServing ?? '-' }} kcal</span>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="80" align="center">
          <template #default="{ row }">
            <span style="color:#f59e0b;font-weight:600">{{ row.ratingAvg != null ? row.ratingAvg.toFixed(1) : '-' }}</span>
            <span style="color:#94a3b8;font-size:11px"> ({{ row.ratingCount || 0 }})</span>
          </template>
        </el-table-column>
        <el-table-column prop="favoriteCount" label="收藏数" width="70" align="center" />
        <el-table-column prop="viewCount" label="浏览数" width="70" align="center" />
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
            <el-popconfirm title="确定删除此食谱？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑食谱' : '新建食谱'" width="860px" destroy-on-close top="3vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="recipe-form">
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
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" style="width:100%">
                <el-option v-for="(label, code) in CategoryMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="菜系" prop="cuisineType">
              <el-select v-model="form.cuisineType" style="width:100%">
                <el-option v-for="(label, code) in CuisineTypeMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="饮食类型">
              <el-select v-model="form.dietType" style="width:100%">
                <el-option v-for="(label, code) in DietTypeMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width:100%">
                <el-option v-for="(label, code) in DifficultyMap" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="精选">
              <el-switch v-model="form.isFeatured" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 时间与份量 -->
        <div class="form-section-title">时间与份量</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="准备时间">
              <el-input-number v-model="form.prepTime" :min="0" style="width:100%" />
              <span class="unit-label">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="烹饪时间">
              <el-input-number v-model="form.cookingTime" :min="0" style="width:100%" />
              <span class="unit-label">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="份数">
              <el-input-number v-model="form.servings" :min="1" style="width:100%" />
              <span class="unit-label">人份</span>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 营养信息 -->
        <div class="form-section-title">营养信息（每份）</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="热量">
              <el-input-number v-model="form.caloriesPerServing" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">kcal</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="蛋白质">
              <el-input-number v-model="form.proteinPerServing" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="脂肪">
              <el-input-number v-model="form.fatPerServing" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="碳水">
              <el-input-number v-model="form.carbsPerServing" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="膳食纤维">
              <el-input-number v-model="form.fiberPerServing" :min="0" :precision="1" style="width:100%" />
              <span class="unit-label">g</span>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 食材列表 -->
        <div class="form-section-title">
          食材列表
          <el-button type="primary" size="small" text @click="addIngredient">
            <el-icon><Plus /></el-icon> 添加食材
          </el-button>
        </div>
        <div v-for="(ing, idx) in form.ingredients" :key="idx" class="dynamic-row">
          <el-row :gutter="8" align="middle">
            <el-col :span="5">
              <el-input v-model="ing.name" placeholder="名称" />
            </el-col>
            <el-col :span="4">
              <el-input v-model="ing.amount" placeholder="用量" />
            </el-col>
            <el-col :span="4">
              <el-input v-model="ing.unit" placeholder="单位" />
            </el-col>
            <el-col :span="4">
              <el-switch v-model="ing.isMain" active-text="主料" inactive-text="辅料" />
            </el-col>
            <el-col :span="4">
              <el-input-number v-model="ing.sortOrder" :min="0" size="small" style="width:100%" placeholder="排序" />
            </el-col>
            <el-col :span="3">
              <el-button type="danger" size="small" text @click="form.ingredients.splice(idx, 1)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 步骤列表 -->
        <div class="form-section-title">
          制作步骤
          <el-button type="primary" size="small" text @click="addStep">
            <el-icon><Plus /></el-icon> 添加步骤
          </el-button>
        </div>
        <div v-for="(step, idx) in form.steps" :key="idx" class="dynamic-row">
          <div class="step-header">
            <span class="step-number">步骤 {{ idx + 1 }}</span>
            <el-button type="danger" size="small" text @click="form.steps.splice(idx, 1)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-row :gutter="8">
            <el-col :span="16">
              <el-input v-model="step.description" type="textarea" :rows="2" placeholder="步骤描述" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="step.imageUrl" placeholder="步骤图片URL" style="margin-bottom:4px" />
              <el-input v-model="step.tips" placeholder="小贴士（可选）" />
            </el-col>
          </el-row>
        </div>

        <!-- 标签 -->
        <div class="form-section-title">
          标签
          <el-button type="primary" size="small" text @click="addTag">
            <el-icon><Plus /></el-icon> 添加标签
          </el-button>
        </div>
        <div class="tags-row">
          <div v-for="(tag, idx) in form.tags" :key="idx" class="tag-item">
            <el-input v-model="tag.tagName" placeholder="标签名" style="width:140px" />
            <el-button type="danger" size="small" text @click="form.tags.splice(idx, 1)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
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

const CategoryMap = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
  SNACK: '小食',
  DESSERT: '甜品',
  SOUP: '汤品'
}

const CuisineTypeMap = {
  CHINESE: '中式',
  WESTERN: '西式',
  JAPANESE: '日式',
  KOREAN: '韩式',
  SOUTHEAST_ASIAN: '东南亚',
  OTHER: '其他'
}

const DietTypeMap = {
  NORMAL: '普通',
  LOW_FAT: '低脂',
  HIGH_PROTEIN: '高蛋白',
  VEGETARIAN: '素食',
  VEGAN: '纯素',
  KETO: '生酮',
  MEDITERRANEAN: '地中海'
}

const DifficultyMap = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

// 列表状态
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const keyword = ref('')
const filterCategory = ref('')
const filterCuisine = ref('')
const filterDiet = ref('')

// 表单状态
const formVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const editId = ref(null)

const defaultForm = () => ({
  title: '',
  description: '',
  coverImage: '',
  category: 'BREAKFAST',
  cuisineType: 'CHINESE',
  dietType: 'NORMAL',
  difficulty: 'EASY',
  prepTime: 10,
  cookingTime: 20,
  servings: 2,
  caloriesPerServing: 0,
  proteinPerServing: 0,
  fatPerServing: 0,
  carbsPerServing: 0,
  fiberPerServing: 0,
  isFeatured: false,
  ingredients: [],
  steps: [],
  tags: []
})

const form = reactive(defaultForm())

const rules = {
  title: [{ required: true, message: '请输入食谱标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  cuisineType: [{ required: true, message: '请选择菜系', trigger: 'change' }]
}

const addIngredient = () => {
  form.ingredients.push({ name: '', amount: '', unit: '', isMain: true, sortOrder: form.ingredients.length })
}

const addStep = () => {
  form.steps.push({ stepNumber: form.steps.length + 1, description: '', imageUrl: '', tips: '' })
}

const addTag = () => {
  form.tags.push({ tagName: '' })
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (filterCategory.value) params.category = filterCategory.value
    if (filterCuisine.value) params.cuisineType = filterCuisine.value
    if (filterDiet.value) params.dietType = filterDiet.value
    const res = await api.get('/admin/recipes', { params })
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
      const res = await api.get(`/admin/recipes/${row.id}`)
      if (res.data.code === 200) {
        const data = res.data.data
        Object.assign(form, {
          title: data.title || '',
          description: data.description || '',
          coverImage: data.coverImage || '',
          category: data.category || 'BREAKFAST',
          cuisineType: data.cuisineType || 'CHINESE',
          dietType: data.dietType || 'NORMAL',
          difficulty: data.difficulty || 'EASY',
          prepTime: data.prepTime ?? 0,
          cookingTime: data.cookingTime ?? 0,
          servings: data.servings ?? 1,
          caloriesPerServing: data.caloriesPerServing ?? 0,
          proteinPerServing: data.proteinPerServing ?? 0,
          fatPerServing: data.fatPerServing ?? 0,
          carbsPerServing: data.carbsPerServing ?? 0,
          fiberPerServing: data.fiberPerServing ?? 0,
          isFeatured: data.isFeatured || false,
          ingredients: (data.ingredients || []).map(i => ({
            name: i.name || '',
            amount: i.amount || '',
            unit: i.unit || '',
            isMain: i.isMain ?? true,
            sortOrder: i.sortOrder ?? 0
          })),
          steps: (data.steps || []).map(s => ({
            stepNumber: s.stepNumber || 0,
            description: s.description || '',
            imageUrl: s.imageUrl || '',
            tips: s.tips || ''
          })),
          tags: (data.tags || []).map(t => ({ tagName: t.tagName || '' }))
        })
      }
    } catch (e) {
      ElMessage.error('获取食谱详情失败')
      return
    }
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, defaultForm())
    form.ingredients = []
    form.steps = []
    form.tags = []
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
      steps: form.steps.map((s, idx) => ({ ...s, stepNumber: idx + 1 }))
    }
    const res = isEdit.value
      ? await api.put(`/admin/recipes/${editId.value}`, payload)
      : await api.post('/admin/recipes', payload)
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
    const res = await api.delete(`/admin/recipes/${id}`)
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
    const res = await api.put(`/admin/recipes/${row.id}/toggle-featured`)
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
    const res = await api.put(`/admin/recipes/${row.id}/toggle-active`)
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
.recipe-management {
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

.recipe-form {
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

  .dynamic-row {
    background: #FAFAFA;
    border: 1px solid #E2E8F0;
    border-radius: 8px;
    padding: 10px 12px;
    margin-bottom: 8px;
  }

  .step-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;

    .step-number {
      font-weight: 600;
      font-size: 13px;
      color: #10B981;
    }
  }

  .tags-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .tag-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
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
