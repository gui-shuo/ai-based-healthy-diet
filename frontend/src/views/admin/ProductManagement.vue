<template>
  <div class="product-management">
    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索产品名称" clearable style="width: 220px"
            @keyup.enter="fetchList" @clear="fetchList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterCategory" placeholder="分类筛选" clearable style="width: 150px" @change="fetchList">
            <el-option v-for="(label, code) in ProductCategories" :key="code" :label="label" :value="code" />
          </el-select>
        </div>
        <el-button type="primary" @click="openForm()">
          <el-icon><Plus /></el-icon> 添加产品
        </el-button>
      </div>
    </el-card>

    <!-- 列表 -->
    <el-card class="list-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="图片" width="70" align="center">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" style="width:40px;height:40px;border-radius:6px" fit="cover">
              <template #error><div style="width:40px;height:40px;background:#f5f5f5;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:10px;color:#ccc">无图</div></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="产品名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ ProductCategories[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" width="90" show-overflow-tooltip />
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
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="ProductStatuses[row.status]?.type || 'info'" size="small">
              {{ ProductStatuses[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="70" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.isRecommended" size="small" @change="toggleRecommend(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="openForm(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => changeStatus(row.id, cmd)">
              <el-button type="success" size="small" text>状态<el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="ACTIVE">上架</el-dropdown-item>
                  <el-dropdown-item command="INACTIVE">下架</el-dropdown-item>
                  <el-dropdown-item command="SOLD_OUT">售罄</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-popconfirm title="确定删除此产品？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑产品' : '添加产品'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" style="width:100%">
                <el-option v-for="(label, code) in ProductCategories" :key="code" :label="label" :value="code" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="产品主图URL" />
        </el-form-item>
        <el-form-item label="简短描述">
          <el-input v-model="form.brief" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="售价" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple filterable allow-create style="width:100%"
            placeholder="输入后回车添加标签">
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="在售" value="ACTIVE" />
                <el-option label="下架" value="INACTIVE" />
                <el-option label="售罄" value="SOLD_OUT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推荐">
              <el-switch v-model="form.isRecommended" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { Search, Plus, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getAdminProducts, createProduct, updateProduct, deleteProduct,
  updateProductStatus, ProductCategories, ProductStatuses
} from '@/services/adminShop'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const keyword = ref('')
const filterCategory = ref('')

const formVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const editId = ref(null)

const defaultForm = () => ({
  name: '', category: 'SUPPLEMENT', brand: '', imageUrl: '',
  brief: '', description: '', originalPrice: 99, salePrice: 79,
  stock: 999, tags: [], status: 'ACTIVE', isRecommended: false, sortOrder: 0
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (filterCategory.value) params.category = filterCategory.value
    const res = await getAdminProducts(params)
    if (res.data.code === 200) {
      list.value = res.data.data.content || []
      total.value = res.data.data.totalElements || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const openForm = (row) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    Object.assign(form, {
      name: row.name, category: row.category, brand: row.brand || '',
      imageUrl: row.imageUrl || '', brief: row.brief || '',
      description: row.description || '', originalPrice: row.originalPrice,
      salePrice: row.salePrice, stock: row.stock,
      tags: row.tags || [], status: row.status,
      isRecommended: row.isRecommended || false, sortOrder: row.sortOrder || 0
    })
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, defaultForm())
  }
  formVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const res = isEdit.value
      ? await updateProduct(editId.value, form)
      : await createProduct(form)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      formVisible.value = false
      fetchList()
    } else { ElMessage.error(res.data.message) }
  } catch (e) { ElMessage.error('操作失败') }
  finally { saving.value = false }
}

const handleDelete = async (id) => {
  try {
    const res = await deleteProduct(id)
    if (res.data.code === 200) { ElMessage.success('删除成功'); fetchList() }
  } catch (e) { ElMessage.error('删除失败') }
}

const changeStatus = async (id, status) => {
  try {
    const res = await updateProductStatus(id, status)
    if (res.data.code === 200) { ElMessage.success('状态已更新'); fetchList() }
  } catch (e) { ElMessage.error('更新失败') }
}

const toggleRecommend = async (row) => {
  try {
    const res = await updateProduct(row.id, { isRecommended: row.isRecommended })
    if (res.data.code === 200) { ElMessage.success(row.isRecommended ? '已设为推荐' : '已取消推荐') }
    else { row.isRecommended = !row.isRecommended }
  } catch (e) { row.isRecommended = !row.isRecommended; ElMessage.error('操作失败') }
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.product-management {
  font-family: 'Patrick Hand', cursive, sans-serif;

  .toolbar-card {
    margin-bottom: 16px;
    border: 2px solid #2d2d2d;
    border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
    box-shadow: 3px 3px 0px 0px rgba(45,45,45,0.1);
    background: #fdfbf7;
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
    border: 2px solid #2d2d2d;
    border-radius: 15px 225px 15px 255px / 255px 15px 225px 15px;
    box-shadow: 4px 4px 0px 0px #2d2d2d;
    background: #fdfbf7;

    :deep(.el-table th.el-table__cell) {
      background: #fff9c4;
      color: #2d2d2d;
      font-family: 'Kalam', cursive;
      font-weight: 600;
      border-bottom: 2.5px solid #2d2d2d;
    }
    :deep(.el-table td.el-table__cell) {
      border-bottom: 1.5px dashed #e5e0d8;
    }
    :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
      background: rgba(253, 251, 247, 0.6);
    }
  }
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
  }
}
</style>
