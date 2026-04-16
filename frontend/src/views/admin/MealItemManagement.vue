<template>
  <div class="meal-item-management">
    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索营养餐名称" clearable style="width: 220px"
            @keyup.enter="fetchList" @clear="fetchList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterCategory" placeholder="餐品分类" clearable style="width: 150px" @change="fetchList">
            <el-option v-for="(label, code) in MealCategories" :key="code" :label="label" :value="code" />
          </el-select>
          <el-select v-model="filterMealType" placeholder="餐类" clearable style="width: 120px" @change="fetchList">
            <el-option v-for="(label, code) in MealTypes" :key="code" :label="label" :value="code" />
          </el-select>
        </div>
        <el-button type="primary" @click="openForm()">
          <el-icon><Plus /></el-icon> 添加营养餐
        </el-button>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="list-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="图片" width="70" align="center">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" style="width:40px;height:40px;border-radius:6px" fit="cover">
              <template #error><div class="img-placeholder">无图</div></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="餐品名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ MealCategories[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="餐类" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ MealTypes[row.mealType] || row.mealType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="90" align="center">
          <template #default="{ row }">
            <span style="color:#f56c6c;font-weight:600">¥{{ row.salePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价" width="80" align="center">
          <template #default="{ row }">
            <span style="text-decoration:line-through;color:#999">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" align="center" />
        <el-table-column prop="salesCount" label="销量" width="70" align="center" />
        <el-table-column label="上架" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isAvailable"
              size="small"
              @change="(val) => toggleAvailable(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="70" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isRecommended"
              size="small"
              @change="(val) => toggleRecommended(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="openForm(row)">编辑</el-button>
            <el-popconfirm title="确定删除此营养餐？" @confirm="handleDelete(row.id)">
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

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑营养餐' : '添加营养餐'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="餐品名称" prop="name">
              <el-input v-model="form.name" placeholder="营养餐名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐品分类" prop="category">
              <el-select v-model="form.category" style="width:100%">
                <el-option v-for="(label, code) in MealCategories" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="餐类" prop="mealType">
              <el-select v-model="form.mealType" style="width:100%">
                <el-option v-for="(label, code) in MealTypes" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片上传">
          <el-upload
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            accept="image/*"
          >
            <div class="upload-area">
              <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:80px;height:80px;border-radius:6px" fit="cover" />
              <div v-else class="upload-placeholder">
                <el-icon style="font-size:24px;color:#ccc"><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </div>
          </el-upload>
          <el-input v-model="form.imageUrl" placeholder="或直接输入图片URL" style="margin-top:8px" />
        </el-form-item>
        <el-form-item label="简短描述">
          <el-input v-model="form.brief" placeholder="一句话描述营养餐" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详细介绍、功效说明" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="原价(元)" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价(元)" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每日限购">
              <el-input-number v-model="form.dailyLimit" :min="0" style="width:100%" placeholder="0=不限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应时间">
              <el-time-picker
                v-model="supplyTimeRange"
                is-range
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                format="HH:mm"
                value-format="HH:mm"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="营养信息">
          <el-row :gutter="8" style="width:100%">
            <el-col :span="6">
              <el-input-number v-model="form.nutritionInfo.calories" :min="0" placeholder="热量(kcal)" style="width:100%" controls-position="right" />
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="form.nutritionInfo.protein" :min="0" :precision="1" placeholder="蛋白质(g)" style="width:100%" controls-position="right" />
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="form.nutritionInfo.fat" :min="0" :precision="1" placeholder="脂肪(g)" style="width:100%" controls-position="right" />
            </el-col>
            <el-col :span="6">
              <el-input-number v-model="form.nutritionInfo.carbs" :min="0" :precision="1" placeholder="碳水(g)" style="width:100%" controls-position="right" />
            </el-col>
          </el-row>
          <div style="color:#999;font-size:12px;margin-top:4px">热量 / 蛋白质 / 脂肪 / 碳水</div>
        </el-form-item>
        <el-form-item label="食材">
          <el-input v-model="form.ingredients" type="textarea" :rows="2" placeholder="食材列表，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="tagsInput" placeholder="多个标签用逗号分隔，如：低脂,高蛋白,抗炎" />
        </el-form-item>
        <el-form-item label="过敏原">
          <el-input v-model="form.allergenInfo" placeholder="如：含坚果、含麸质" />
        </el-form-item>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="是否上架">
              <el-switch v-model="form.isAvailable" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否推荐">
              <el-switch v-model="form.isRecommended" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">{{ isEdit ? '保存修改' : '添加营养餐' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import api from '@/services/api'

const MealCategories = {
  ANTI_INFLAMMATORY: '抗炎营养餐',
  LOW_FAT: '低脂轻食',
  HIGH_PROTEIN: '高蛋白增肌',
  VEGETARIAN: '素食健康',
  BALANCED: '均衡营养',
  SLIMMING: '减脂瘦身'
}

const MealTypes = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
  SNACK: '加餐'
}

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const filterCategory = ref('')
const filterMealType = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const tagsInput = ref('')
const supplyTimeRange = ref(null)

const defaultForm = () => ({
  name: '',
  imageUrl: '',
  category: 'ANTI_INFLAMMATORY',
  mealType: 'LUNCH',
  brief: '',
  description: '',
  ingredients: '',
  originalPrice: 0,
  salePrice: 0,
  stock: 999,
  dailyLimit: 0,
  nutritionInfo: { calories: 0, protein: 0, fat: 0, carbs: 0 },
  tags: [],
  allergenInfo: '',
  isAvailable: true,
  isRecommended: false,
  availableStartTime: '07:00',
  availableEndTime: '20:00',
  sortOrder: 0
})

const form = reactive(defaultForm())
let editingId = null

const rules = {
  name: [{ required: true, message: '请输入餐品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (filterCategory.value) params.category = filterCategory.value
    if (filterMealType.value) params.mealType = filterMealType.value
    const res = await api.get('/admin/meals', { params })
    const data = res.data?.data || res.data
    if (data?.content) {
      list.value = data.content
      total.value = data.totalElements || 0
    } else if (Array.isArray(data)) {
      list.value = data
      total.value = data.length
    }
  } catch (e) {
    ElMessage.error('获取营养餐列表失败')
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  isEdit.value = !!row
  editingId = row?.id || null
  Object.assign(form, defaultForm())
  tagsInput.value = ''
  supplyTimeRange.value = null

  if (row) {
    Object.assign(form, {
      name: row.name || '',
      imageUrl: row.imageUrl || '',
      category: row.category || 'ANTI_INFLAMMATORY',
      mealType: row.mealType || 'LUNCH',
      brief: row.brief || '',
      description: row.description || '',
      ingredients: row.ingredients || '',
      originalPrice: Number(row.originalPrice) || 0,
      salePrice: Number(row.salePrice) || 0,
      stock: row.stock ?? 999,
      dailyLimit: row.dailyLimit ?? 0,
      nutritionInfo: row.nutritionInfo
        ? { ...{ calories: 0, protein: 0, fat: 0, carbs: 0 }, ...row.nutritionInfo }
        : { calories: 0, protein: 0, fat: 0, carbs: 0 },
      allergenInfo: row.allergenInfo || '',
      isAvailable: row.isAvailable !== false,
      isRecommended: !!row.isRecommended,
      availableStartTime: row.availableStartTime || '07:00',
      availableEndTime: row.availableEndTime || '20:00',
      sortOrder: row.sortOrder || 0
    })
    tagsInput.value = Array.isArray(row.tags) ? row.tags.join(',') : (row.tags || '')
    if (row.availableStartTime && row.availableEndTime) {
      supplyTimeRange.value = [row.availableStartTime, row.availableEndTime]
    }
  }
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      ...form,
      tags: tagsInput.value ? tagsInput.value.split(',').map(t => t.trim()).filter(Boolean) : [],
      availableStartTime: supplyTimeRange.value?.[0] || form.availableStartTime,
      availableEndTime: supplyTimeRange.value?.[1] || form.availableEndTime,
      originalPrice: Number(form.originalPrice),
      salePrice: Number(form.salePrice)
    }
    if (isEdit.value) {
      await api.put(`/admin/meals/${editingId}`, payload)
      ElMessage.success('修改成功')
    } else {
      await api.post('/admin/meals', payload)
      ElMessage.success('添加成功')
    }
    formVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleAvailable(row, val) {
  try {
    await api.put(`/admin/meals/${row.id}/status`, { isAvailable: val })
    row.isAvailable = val
    ElMessage.success(val ? '已上架' : '已下架')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function toggleRecommended(row, val) {
  try {
    await api.put(`/admin/meals/${row.id}`, { isRecommended: val })
    row.isRecommended = val
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id) {
  try {
    await api.delete(`/admin/meals/${id}`)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt5M) ElMessage.error('图片大小不能超过 5MB')
  return isImage && isLt5M
}

async function handleUpload({ file }) {
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await api.post('/upload/image', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.imageUrl = res.data?.data?.url || res.data?.url || ''
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败，请手动输入图片URL')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.meal-item-management { padding: 0; }

.toolbar-card { margin-bottom: 16px; }
.toolbar { display: flex; align-items: center; justify-content: space-between; }
.toolbar-left { display: flex; gap: 12px; align-items: center; }

.list-card {}
.pagination { margin-top: 16px; display: flex; justify-content: center; }

.img-placeholder {
  width: 40px; height: 40px;
  background: #f5f5f5; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: #ccc;
}

.upload-area {
  width: 80px; height: 80px;
  border: 1px dashed #d9d9d9; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; overflow: hidden;
}
.upload-placeholder {
  display: flex; flex-direction: column; align-items: center;
  gap: 4px; color: #ccc; font-size: 12px;
}
</style>
