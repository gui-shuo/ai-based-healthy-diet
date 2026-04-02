<template>
  <div class="food-recognition-view">
    <!-- Frosted-glass header -->
    <div class="recognition-header">
      <el-button :icon="ArrowLeft" text style="margin-bottom: 16px" @click="goToHome">
        返回首页
      </el-button>
      <h2>🍎 AI食物识别</h2>
      <p>输入食物名称，AI智能识别营养成分</p>
    </div>

    <div class="recognition-body">
      <!-- Alert banner -->
      <el-alert type="info" :closable="true" show-icon style="margin-bottom: 16px">
        <template #title>
          上传食物图片即可自动识别并获取完整营养成分数据，支持各类常见食物。营养数据仅供参考。
          <router-link to="/legal/disclaimer" style="color:#409eff">详细声明</router-link>
        </template>
      </el-alert>

      <!-- 左侧：输入区域 -->
      <el-card class="input-card glass-card">
        <template #header>
          <h3>食物识别</h3>
        </template>

        <!-- 文本输入方式 -->
        <div class="input-section">
          <h4>📝 文本输入</h4>
          <div class="text-input-wrapper">
            <el-input
              v-model="foodName"
              placeholder="请输入食物名称，如：苹果、鸡胸肉、燕麦"
              size="large"
              clearable
              @keyup.enter="recognizeByName"
            >
              <template #append>
                <el-button type="primary" :loading="isRecognizing" @click="recognizeByName">
                  识别
                </el-button>
              </template>
            </el-input>
          </div>
          <div class="input-tip">💡 提示：输入常见食物名称，AI会自动识别营养成分</div>
        </div>

        <!-- 图片上传方式 -->
        <div class="upload-section">
          <h4>📷 图片识别</h4>
          <div class="upload-zone-wrapper">
            <el-upload
              ref="uploadRef"
              class="upload-demo"
              drag
              :auto-upload="false"
              :on-change="handleImageChange"
              :show-file-list="false"
              :disabled="isRecognizing"
              :limit="1"
              accept="image/jpeg,image/png,image/gif,image/webp,image/bmp"
            >
              <el-icon class="el-icon--upload">
                <upload-filled />
              </el-icon>
              <div class="el-upload__text">拖拽图片到此处或 <em>点击上传</em></div>
              <template #tip>
                <div class="el-upload__tip">支持 JPG / PNG / GIF / WebP / BMP，大小不超过 5MB（大图将自动压缩）</div>
                <div class="el-upload__tip" style="color: #e6a23c; margin-top: 4px;">💡 识别结果请以置信度为准，置信度越高结果越可靠</div>
              </template>
            </el-upload>
          </div>

          <!-- 预览图片 -->
          <div v-if="previewUrl" class="preview-section">
            <div class="preview-image-wrapper">
              <img :src="previewUrl" alt="预览" class="preview-image" />
              <el-button
                class="delete-image-btn"
                type="danger"
                :icon="Delete"
                circle
                size="small"
                title="删除图片"
                @click="clearImage"
              />
            </div>
            <el-button
              type="primary"
              :loading="isRecognizing"
              style="width: 100%; margin-top: 10px"
              @click="recognizeByImage"
            >
              开始识别
            </el-button>
          </div>
        </div>

        <!-- 快捷输入 -->
        <div class="quick-input">
          <h4>⚡ 快捷输入</h4>
          <el-space wrap>
            <el-tag
              v-for="food in quickFoods"
              :key="food"
              :style="{
                cursor: isRecognizing ? 'not-allowed' : 'pointer',
                opacity: isRecognizing ? 0.5 : 1
              }"
              effect="plain"
              @click="quickRecognize(food)"
            >
              {{ food }}
            </el-tag>
          </el-space>
        </div>
      </el-card>

      <!-- 右侧：识别结果 -->
      <el-card v-if="recognitionResult" class="result-card glass-card">
        <template #header>
          <div class="result-header">
            <h3>识别结果</h3>
            <el-tag type="success"> {{ recognitionResult.totalCount }} 项 </el-tag>
          </div>
        </template>

        <div class="result-list">
          <div
            v-for="(food, index) in recognitionResult.foods"
            :key="index"
            class="food-result-item"
          >
            <div class="food-item-gradient-header">
              <div class="food-header">
                <div class="food-name-wrap">
                  <h4>{{ food.name }}</h4>
                  <el-tag v-if="food.category" :type="food.category === '果蔬' ? 'success' : 'warning'" size="small" effect="plain">
                    {{ food.category }}
                  </el-tag>
                </div>
                <div class="confidence-badge-wrapper">
                  <el-tag :type="getConfidenceType(food.confidence)" size="small">
                    置信度: {{ (food.confidence * 100).toFixed(0) }}%
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 基础营养（每100g） -->
            <div class="nutrition-section">
              <div class="section-label">🔥 基础营养 (每100g)</div>
              <div class="nutrition-grid basic-grid">
                <div class="nutrition-item nut-calories">
                  <div class="nut-icon">🔥</div>
                  <div class="nutrition-label">热量</div>
                  <div class="nutrition-value">
                    {{ safeFixed(food.nutrition.energy) }}
                    <span class="unit">kcal</span>
                  </div>
                </div>
                <div class="nutrition-item nut-protein">
                  <div class="nut-icon">💪</div>
                  <div class="nutrition-label">蛋白质</div>
                  <div class="nutrition-value">
                    {{ safeFixed(food.nutrition.protein) }}
                    <span class="unit">g</span>
                  </div>
                </div>
                <div class="nutrition-item nut-carbs">
                  <div class="nut-icon">🌾</div>
                  <div class="nutrition-label">碳水化合物</div>
                  <div class="nutrition-value">
                    {{ safeFixed(food.nutrition.carbohydrate) }}
                    <span class="unit">g</span>
                  </div>
                </div>
                <div class="nutrition-item nut-fat">
                  <div class="nut-icon">🫒</div>
                  <div class="nutrition-label">脂肪</div>
                  <div class="nutrition-value">
                    {{ safeFixed(food.nutrition.fat) }}
                    <span class="unit">g</span>
                  </div>
                </div>
                <div class="nutrition-item nut-fiber">
                  <div class="nut-icon">🥬</div>
                  <div class="nutrition-label">膳食纤维</div>
                  <div class="nutrition-value">
                    {{ safeFixed(food.nutrition.fiber) }}
                    <span class="unit">g</span>
                  </div>
                </div>
                <div class="nutrition-item nut-cholesterol">
                  <div class="nut-icon">❤️</div>
                  <div class="nutrition-label">胆固醇</div>
                  <div class="nutrition-value">
                    {{ safeFixed(food.nutrition.cholesterol) }}
                    <span class="unit">mg</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 矿物质 -->
            <div v-if="hasMineral(food.nutrition)" class="nutrition-section">
              <div class="section-label">💎 矿物质</div>
              <div class="nutrition-grid mineral-grid">
                <div v-if="food.nutrition.calcium != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">钙</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.calcium) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.iron != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">铁</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.iron) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.zinc != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">锌</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.zinc) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.sodium != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">钠</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.sodium) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.potassium != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">钾</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.potassium) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.magnesium != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">镁</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.magnesium) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.phosphorus != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">磷</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.phosphorus) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.selenium != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">硒</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.selenium) }} <em>μg</em></span>
                </div>
                <div v-if="food.nutrition.copper != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">铜</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.copper, 2) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.manganese != null" class="nutrition-item-sm mineral-pill">
                  <span class="label-sm">锰</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.manganese, 2) }} <em>mg</em></span>
                </div>
              </div>
            </div>

            <!-- 维生素 -->
            <div v-if="hasVitamin(food.nutrition)" class="nutrition-section">
              <div class="section-label">🧪 维生素</div>
              <div class="nutrition-grid mineral-grid">
                <div v-if="food.nutrition.vitaminA != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">维生素A</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.vitaminA) }} <em>μg</em></span>
                </div>
                <div v-if="food.nutrition.vitaminC != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">维生素C</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.vitaminC) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.vitaminE != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">维生素E</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.vitaminE, 2) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.carotene != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">胡萝卜素</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.carotene) }} <em>μg</em></span>
                </div>
                <div v-if="food.nutrition.thiamine != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">硫胺素(B1)</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.thiamine, 2) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.riboflavin != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">核黄素(B2)</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.riboflavin, 2) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.niacin != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">烟酸(B3)</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.niacin) }} <em>mg</em></span>
                </div>
                <div v-if="food.nutrition.retinolEquivalent != null" class="nutrition-item-sm vitamin-pill">
                  <span class="label-sm">视黄醇当量</span>
                  <span class="value-sm">{{ safeFixed(food.nutrition.retinolEquivalent) }} <em>μg</em></span>
                </div>
              </div>
            </div>

            <div class="data-source">
              <el-icon><InfoFilled /></el-icon>
              数据来源: {{ getSourceText(food.nutrition.source) }}
            </div>
          </div>
        </div>

        <div class="result-footer">
          <div class="recognition-time">识别耗时: {{ recognitionResult.recognitionTime }}ms</div>
        </div>
      </el-card>

      <!-- 空状态提示 -->
      <el-card v-if="!recognitionResult && !isRecognizing" class="empty-card glass-card">
        <div class="empty-content">
          <div class="empty-icon-wrapper">
            <el-icon :size="80" color="#dcdfe6">
              <Picture />
            </el-icon>
          </div>
          <h3>开始识别食物</h3>
          <p>输入食物名称或上传图片，AI将为您分析营养成分</p>
        </div>
      </el-card>

      <!-- 加载状态 -->
      <el-card v-if="isRecognizing" class="loading-card glass-card">
        <div class="loading-content">
          <div class="loading-pulse">
            <el-icon class="loading-icon">
              <Loading />
            </el-icon>
          </div>
          <h3>AI正在识别食物...</h3>
          <p>正在分析食物营养成分，通常需要几秒钟，请耐心等待</p>
        </div>
      </el-card>
    </div>

    <!-- 识别历史 -->
    <el-card v-if="history.length > 0" class="history-card glass-card">
      <template #header>
        <div class="history-header">
          <h3>📜 识别历史</h3>
          <el-tag type="info"> {{ history.length }} 条记录 </el-tag>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="item in history"
          :key="item.id"
          :timestamp="formatTime(item.createdAt)"
        >
          <div class="history-item">
            <div class="history-summary">
              <span class="history-text" @click="toggleHistoryDetail(item.id)">
                {{ getHistoryText(item.recognitionResult) }}
              </span>
              <div class="history-actions">
                <el-icon class="expand-icon" @click="toggleHistoryDetail(item.id)">
                  <ArrowDown v-if="!expandedHistory[item.id]" />
                  <ArrowUp v-else />
                </el-icon>
                <el-button
                  type="danger"
                  :icon="Delete"
                  size="small"
                  circle
                  title="删除记录"
                  @click="confirmDeleteHistory(item.id)"
                />
              </div>
            </div>

            <!-- 详细信息 -->
            <el-collapse-transition>
              <div v-show="expandedHistory[item.id]" class="history-detail">
                <div class="detail-info">
                  <div class="detail-row">
                    <span class="detail-label">识别方式:</span>
                    <el-tag
                      size="small"
                      :type="item.recognitionType === 'TEXT' ? 'success' : 'primary'"
                    >
                      {{ item.recognitionType === 'TEXT' ? '📝 文本识别' : '📷 图片识别' }}
                    </el-tag>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">识别内容:</span>
                    <span>{{ item.inputText || '图片' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="detail-label">识别时间:</span>
                    <span>{{ formatFullTime(item.createdAt) }}</span>
                  </div>
                </div>

                <!-- 图片识别时显示原始图片 -->
                <div v-if="item.recognitionType === 'IMAGE' && item.imageUrl" class="history-image-section">
                  <h5>识别图片:</h5>
                  <div class="history-image-wrapper">
                    <img :src="item.imageUrl" alt="识别图片" class="history-image" />
                  </div>
                </div>

                <!-- 识别结果详情 -->
                <div v-if="item.recognitionResult" class="result-detail">
                  <h5>识别结果:</h5>
                  <div
                    v-for="(food, index) in parseRecognitionResult(item.recognitionResult).foods"
                    :key="index"
                    class="history-food-item"
                  >
                    <div class="food-name-row">
                      <strong>{{ food.name }}</strong>
                      <el-tag size="small" :type="getConfidenceType(food.confidence)">
                        {{ (food.confidence * 100).toFixed(0) }}%
                      </el-tag>
                    </div>
                    <div class="nutrition-mini-grid">
                      <span>热量: {{ safeFixed(food.nutrition?.energy) }}kcal</span>
                      <span>蛋白质: {{ safeFixed(food.nutrition?.protein) }}g</span>
                      <span>碳水: {{ safeFixed(food.nutrition?.carbohydrate) }}g</span>
                      <span>脂肪: {{ safeFixed(food.nutrition?.fat) }}g</span>
                    </div>
                    <div v-if="food.nutrition?.source" class="data-source-mini">
                      <el-icon><InfoFilled /></el-icon>
                      {{ getSourceText(food.nutrition.source) }}
                    </div>
                  </div>
                </div>
              </div>
            </el-collapse-transition>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Loading,
  InfoFilled,
  UploadFilled,
  Picture,
  Delete,
  ArrowDown,
  ArrowUp,
  ArrowLeft
} from '@element-plus/icons-vue'
import api from '@/services/api'

// ─── 常量 ────────────────────────────────────────────────────────
const ALLOWED_IMAGE_TYPES = new Set([
  'image/jpeg',
  'image/png',
  'image/gif',
  'image/webp',
  'image/bmp'
])
const MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB
const COMPRESS_THRESHOLD = 1 * 1024 * 1024 // 1MB — 超过此大小自动压缩

const router = useRouter()

// ─── 状态 ────────────────────────────────────────────────────────
const foodName = ref('')
const isRecognizing = ref(false)
const recognitionResult = ref(null)
const history = ref([])
const previewUrl = ref(null)
const selectedFile = ref(null)
const expandedHistory = reactive({})
const uploadRef = ref(null)

const quickFoods = [
  '苹果',
  '香蕉',
  '鸡胸肉',
  '鸡蛋',
  '牛奶',
  '燕麦',
  '西兰花',
  '三文鱼',
  '糙米',
  '红薯'
]

// ─── 图片处理 ────────────────────────────────────────────────────

/** canvas压缩图片 — 大图自动缩小以加速识别 */
const compressImage = (file, maxWidth = 1280, quality = 0.7) => {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      let { width, height } = img
      if (width > maxWidth) {
        height = Math.round(height * maxWidth / width)
        width = maxWidth
      }
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        blob => resolve(new File([blob], file.name, { type: 'image/jpeg' })),
        'image/jpeg',
        quality
      )
    }
    img.onerror = () => resolve(file)
    img.src = URL.createObjectURL(file)
  })
}

/** el-upload onChange 回调：校验文件类型和大小，大图自动压缩 */
const handleImageChange = async file => {
  const raw = file.raw
  if (!raw) return

  if (!ALLOWED_IMAGE_TYPES.has(raw.type)) {
    ElMessage.error('仅支持 JPG / PNG / GIF / WebP / BMP 格式的图片')
    uploadRef.value?.clearFiles()
    return
  }
  if (raw.size > MAX_IMAGE_SIZE) {
    ElMessage.error('图片大小不能超过 5MB，请压缩后重试')
    uploadRef.value?.clearFiles()
    return
  }

  // 大图自动压缩
  let finalFile = raw
  if (raw.size > COMPRESS_THRESHOLD) {
    ElMessage.info('图片较大，正在自动压缩...')
    finalFile = await compressImage(raw)
    ElMessage.success(`已压缩: ${(raw.size / 1024).toFixed(0)}KB → ${(finalFile.size / 1024).toFixed(0)}KB`)
  }

  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  selectedFile.value = finalFile
  previewUrl.value = URL.createObjectURL(finalFile)
}

/** 清除已选图片 */
const clearImage = () => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = null
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  ElMessage.success('已清除图片')
}

// ─── 识别逻辑 ─────────────────────────────────────────────────────

/** 图片识别 */
const recognizeByImage = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  if (isRecognizing.value) return // 防止重复提交

  isRecognizing.value = true
  recognitionResult.value = null

  try {
    const formData = new FormData()
    formData.append('image', selectedFile.value)

    const response = await api.post('/food-recognition/recognize-by-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000 // 菜品+果蔬并行识别需要更多时间
    })
    const data = response.data

    if (data.code === 200) {
      recognitionResult.value = data.data
      ElMessage.success(`识别成功，共识别到 ${data.data.totalCount} 种食物`)
      loadHistory()
    } else if (data.code === 4001) {
      // 未识别到食物的业务状态
      ElMessage.warning(data.message || '未能从图片中识别到食物，请换一张更清晰的照片')
    } else if (data.code === 501) {
      ElMessage.info('图片识别功能暂未开放，建议使用文本输入或快捷输入')
    } else {
      ElMessage.error(data.message || '识别失败，请稍后重试')
    }
  } catch (error) {
    console.error('图片识别请求失败:', error)
    const msg = error.response?.data?.message || error.message || '网络错误，请检查网络后重试'
    ElMessage.error('识别失败: ' + msg)
  } finally {
    isRecognizing.value = false
  }
}

/** 文本识别 */
const recognizeByName = async () => {
  const name = foodName.value.trim()
  if (!name) {
    ElMessage.warning('请输入食物名称')
    return
  }
  if (isRecognizing.value) return // 防止重复提交

  isRecognizing.value = true
  recognitionResult.value = null

  try {
    const response = await api.post('/food-recognition/recognize-by-name', null, {
      params: { foodName: name },
      timeout: 30000
    })
    const data = response.data

    if (data.code === 200) {
      recognitionResult.value = data.data
      ElMessage.success('识别成功！')
      loadHistory()
    } else {
      ElMessage.error(data.message || '识别失败，请稍后重试')
    }
  } catch (error) {
    console.error('文本识别请求失败:', error)
    const msg = error.response?.data?.message || error.message || '网络错误，请检查网络后重试'
    ElMessage.error('识别失败: ' + msg)
  } finally {
    isRecognizing.value = false
  }
}

/** 快捷识别（防止正在识别时重复触发） */
const quickRecognize = food => {
  if (isRecognizing.value) return
  foodName.value = food
  recognizeByName()
}

// ─── 历史记录 ─────────────────────────────────────────────────────

const loadHistory = async () => {
  try {
    const response = await api.get('/food-recognition/history')
    const data = response.data
    if (data.code === 200) {
      history.value = data.data || []
    }
  } catch (error) {
    console.error('加载历史失败:', error)
  }
}

const confirmDeleteHistory = id => {
  ElMessageBox.confirm('确定要删除这条识别记录吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true,
    customClass: 'custom-message-box'
  })
    .then(() => deleteHistory(id))
    .catch(() => {})
}

const deleteHistory = async id => {
  try {
    const response = await api.delete(`/food-recognition/history/${id}`)
    const data = response.data
    if (data.code === 200) {
      ElMessage.success('删除成功')
      loadHistory()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

const toggleHistoryDetail = id => {
  expandedHistory[id] = !expandedHistory[id]
}

// ─── 工具函数 ─────────────────────────────────────────────────────

const getConfidenceType = confidence => {
  if (confidence >= 0.9) return 'success'
  if (confidence >= 0.7) return 'warning'
  return 'info'
}

const getSourceText = source => {
  const map = {
    tianapi: '天聚数行API',
    database: '数据库（准确）',
    estimated: 'AI估算',
    default: '默认值',
    'baidu-calorie-only': '识别（仅卡路里）'
  }
  return map[source] || source
}

const hasMineral = n => {
  if (!n) return false
  return [n.calcium, n.iron, n.zinc, n.sodium, n.potassium, n.magnesium, n.phosphorus, n.selenium, n.copper, n.manganese]
    .some(v => v != null && v !== 0)
}

const hasVitamin = n => {
  if (!n) return false
  return [n.vitaminA, n.vitaminC, n.vitaminE, n.carotene, n.thiamine, n.riboflavin, n.niacin, n.retinolEquivalent]
    .some(v => v != null && v !== 0)
}

const safeFixed = (val, digits = 1) => {
  if (val == null || isNaN(val)) return '—'
  return Number(val).toFixed(digits)
}

const formatTime = time => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return String(time)
  }
}

const formatFullTime = time => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch {
    return String(time)
  }
}

const getHistoryText = resultJson => {
  try {
    const result = JSON.parse(resultJson)
    const names = result.foods?.map(f => f.name).join('、') || '未知食物'
    return `识别了 ${names}`
  } catch {
    return '识别记录'
  }
}

const parseRecognitionResult = resultJson => {
  try {
    return JSON.parse(resultJson)
  } catch {
    return { foods: [] }
  }
}

const goToHome = () => router.push('/')

// ─── 生命周期 ─────────────────────────────────────────────────────

onMounted(() => {
  loadHistory()
})

onBeforeUnmount(() => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = null
  }
  uploadRef.value?.clearFiles?.()
  selectedFile.value = null
  recognitionResult.value = null
  history.value = []
  foodName.value = ''
  isRecognizing.value = false
  Object.keys(expandedHistory).forEach(key => delete expandedHistory[key])
})
</script>

<style scoped lang="scss">
/* ═══════════════════════════════════════════════════════════
   Premium glassmorphism theme for AI Food Recognition
   ═══════════════════════════════════════════════════════════ */

/* ── Gradient background ──────────────────────────────────── */
.food-recognition-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px 24px 48px;
  position: relative;

  &::before {
    content: '';
    position: fixed;
    inset: 0;
    background:
      radial-gradient(ellipse at 20% 50%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
      radial-gradient(ellipse at 80% 20%, rgba(255, 119, 198, 0.15) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

/* ── Frosted-glass header ─────────────────────────────────── */
.recognition-header {
  text-align: center;
  margin-bottom: 36px;
  padding: 28px 24px 24px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.18);

  :deep(.el-button) {
    color: rgba(255, 255, 255, 0.85) !important;
    font-weight: 500;

    &:hover {
      color: #fff !important;
    }
  }

  h2 {
    font-size: 36px;
    margin: 0 0 8px;
    color: #fff;
    font-weight: 800;
    letter-spacing: 1px;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  }

  p {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.75);
    margin: 0;
    font-weight: 400;
  }
}

/* ── Glass card mixin ─────────────────────────────────────── */
.glass-card {
  background: rgba(255, 255, 255, 0.72) !important;
  backdrop-filter: blur(10px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.45) !important;
  border-radius: 16px !important;
  box-shadow:
    0 8px 32px rgba(31, 38, 135, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.06) !important;
  overflow: hidden;
  transition: box-shadow 0.3s ease, transform 0.3s ease;

  &:hover {
    box-shadow:
      0 12px 40px rgba(31, 38, 135, 0.18),
      0 4px 12px rgba(0, 0, 0, 0.08) !important;
  }
}

/* ── Alert override ───────────────────────────────────────── */
.recognition-body {
  :deep(.el-alert) {
    border-radius: 14px;
    background: rgba(255, 255, 255, 0.65);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.4);
  }
}

/* ── Two-column grid ──────────────────────────────────────── */
.recognition-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 28px;
  max-width: 1280px;
  margin-left: auto;
  margin-right: auto;
}

/* ── Input / result / loading / history cards ─────────────── */
.input-card,
.result-card,
.loading-card,
.history-card {
  height: fit-content;
}

/* ── Section headings ─────────────────────────────────────── */
.input-section,
.upload-section,
.quick-input {
  margin-bottom: 28px;
}

.input-section h4,
.upload-section h4,
.quick-input h4 {
  font-size: 16px;
  margin: 0 0 14px;
  color: #303133;
  font-weight: 700;
}

/* ── Text input glow ──────────────────────────────────────── */
.text-input-wrapper {
  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 12px;
      transition: box-shadow 0.35s ease;
    }

    &.is-focus .el-input__wrapper,
    .el-input__wrapper:focus-within {
      box-shadow:
        0 0 0 1px var(--el-input-focus-border-color) inset,
        0 0 16px rgba(102, 126, 234, 0.25);
    }
  }

  :deep(.el-input-group__append) {
    border-radius: 0 12px 12px 0;
  }
}

.input-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

/* ── Upload zone ──────────────────────────────────────────── */
.upload-zone-wrapper {
  :deep(.el-upload-dragger) {
    border-radius: 14px;
    border: 2px dashed rgba(102, 126, 234, 0.35);
    background: rgba(102, 126, 234, 0.03);
    padding: 40px 20px;
    transition: all 0.4s ease;

    &:hover {
      border-color: transparent;
      background: linear-gradient(white, white) padding-box,
        linear-gradient(135deg, #667eea, #764ba2, #f093fb) border-box;
      border: 2px solid transparent;
      box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
    }

    .el-icon--upload {
      font-size: 52px;
      color: #764ba2;
      margin-bottom: 8px;
      transition: transform 0.3s ease;
    }

    &:hover .el-icon--upload {
      transform: translateY(-4px) scale(1.1);
    }
  }
}

.upload-demo {
  width: 100%;
}

/* ── Image preview ────────────────────────────────────────── */
.preview-section {
  margin-top: 18px;
  text-align: center;
}

.preview-image-wrapper {
  position: relative;
  display: inline-block;
  width: 100%;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 14px;
  margin-bottom: 16px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.12);
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.01);
  }
}

.delete-image-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.15);
  }
}

/* ── Quick foods pills ────────────────────────────────────── */
.quick-input {
  :deep(.el-tag) {
    cursor: pointer;
    border-radius: 20px;
    padding: 6px 16px;
    font-weight: 500;
    font-size: 13px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
    border-color: rgba(102, 126, 234, 0.2);
    color: #5b4b8a;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      transform: translateY(-3px) scale(1.05);
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: #fff;
      border-color: transparent;
    }
  }
}

/* ── Result card ──────────────────────────────────────────── */
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-weight: 700;
  }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.food-result-item {
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
  }
}

.food-item-gradient-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06), rgba(118, 75, 162, 0.06));
  padding: 16px 18px 12px;
}

.food-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h4 {
    margin: 0;
    font-size: 19px;
    color: #303133;
    font-weight: 700;
  }
}

.food-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.confidence-badge-wrapper {
  :deep(.el-tag) {
    font-size: 13px;
    font-weight: 600;
    padding: 4px 14px;
    border-radius: 20px;
  }
}

/* ── Nutrition sections ───────────────────────────────────── */
.nutrition-section {
  margin: 0 18px 16px;
}

.section-label {
  font-size: 13px;
  font-weight: 700;
  color: #606266;
  margin-bottom: 10px;
  padding-top: 4px;
}

.nutrition-grid {
  display: grid;
  gap: 10px;
}

.basic-grid {
  grid-template-columns: repeat(3, 1fr);
}

.mineral-grid {
  grid-template-columns: repeat(5, 1fr);
}

/* ── Basic nutrition colorful cards ───────────────────────── */
.nutrition-item {
  text-align: center;
  padding: 14px 8px 12px;
  border-radius: 12px;
  transition: transform 0.2s ease;
  position: relative;

  &:hover {
    transform: translateY(-2px);
  }

  .nut-icon {
    font-size: 20px;
    margin-bottom: 4px;
  }
}

.nut-calories {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border: 1px solid rgba(251, 146, 60, 0.15);
}

.nut-protein {
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid rgba(59, 130, 246, 0.15);
}

.nut-carbs {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid rgba(34, 197, 94, 0.15);
}

.nut-fat {
  background: linear-gradient(135deg, #fefce8, #fef9c3);
  border: 1px solid rgba(234, 179, 8, 0.15);
}

.nut-fiber {
  background: linear-gradient(135deg, #faf5ff, #f3e8ff);
  border: 1px solid rgba(168, 85, 247, 0.15);
}

.nut-cholesterol {
  background: linear-gradient(135deg, #fef2f2, #fecaca);
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.nutrition-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.nutrition-value {
  font-size: 22px;
  font-weight: 800;
  color: #303133;
  line-height: 1.2;

  .unit {
    font-size: 12px;
    font-weight: normal;
    color: #909399;
    margin-left: 2px;
  }
}

/* ── Mineral & vitamin pills ──────────────────────────────── */
.nutrition-item-sm {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 6px;
  border-radius: 10px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.06);
  }
}

.mineral-pill {
  background: linear-gradient(135deg, #f0fdfa, #ccfbf1);
  border: 1px solid rgba(20, 184, 166, 0.12);
}

.vitamin-pill {
  background: linear-gradient(135deg, #fff7ed, #fed7aa);
  border: 1px solid rgba(249, 115, 22, 0.12);
}

.label-sm {
  font-size: 11px;
  color: #909399;
  margin-bottom: 3px;
  font-weight: 500;
}

.value-sm {
  font-size: 13px;
  font-weight: 700;
  color: #303133;

  em {
    font-style: normal;
    font-size: 10px;
    font-weight: normal;
    color: #909399;
  }
}

/* ── Data source footer badge ─────────────────────────────── */
.data-source {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  margin: 4px 18px 16px;
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 20px;
}

/* ── Result footer ────────────────────────────────────────── */
.result-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.recognition-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

/* ── Loading state with pulse ─────────────────────────────── */
.loading-card {
  grid-column: 1 / -1;
}

.loading-content {
  text-align: center;
  padding: 56px 24px;

  h3 {
    margin: 20px 0 8px;
    color: #303133;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: #909399;
  }
}

.loading-pulse {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  animation: pulse-ring 2s ease-in-out infinite;
}

.loading-icon {
  font-size: 44px;
  color: #667eea;
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

@keyframes pulse-ring {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.25);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(102, 126, 234, 0);
  }
}

/* ── Empty state ──────────────────────────────────────────── */
.empty-card {
  grid-column: 2;
}

.empty-content {
  text-align: center;
  padding: 80px 24px;
}

.empty-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06), rgba(118, 75, 162, 0.06));
  margin-bottom: 8px;
}

.empty-content h3 {
  margin: 20px 0 12px;
  color: #606266;
  font-size: 22px;
  font-weight: 700;
}

.empty-content p {
  margin: 0;
  color: #a8abb2;
  font-size: 15px;
}

/* ── History section ──────────────────────────────────────── */
.history-card {
  max-width: 1280px;
  margin-left: auto;
  margin-right: auto;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-weight: 700;
  }
}

.history-item {
  width: 100%;
}

.history-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-radius: 10px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.04);

  &:hover {
    background: rgba(102, 126, 234, 0.05);
    border-color: rgba(102, 126, 234, 0.12);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.08);
  }
}

.history-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;

  &:hover {
    color: #667eea;
  }
}

.history-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.expand-icon {
  cursor: pointer;
  transition: all 0.3s ease;
  color: #909399;
  font-size: 16px;

  &:hover {
    color: #667eea;
    transform: scale(1.2);
  }
}

.history-detail {
  margin-top: 14px;
  padding: 18px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03), rgba(118, 75, 162, 0.03));
  border-radius: 12px;
  border-left: 3px solid #667eea;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-info {
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  margin-right: 12px;
  min-width: 80px;
}

.result-detail h5 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #303133;
  font-weight: 700;
}

.history-food-item {
  background: rgba(255, 255, 255, 0.8);
  padding: 14px;
  border-radius: 10px;
  margin-bottom: 10px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-1px);
  }
}

.food-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  strong {
    font-size: 15px;
    color: #303133;
  }
}

.nutrition-mini-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  font-size: 13px;
  color: #606266;

  span {
    padding: 6px 10px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
    border-radius: 8px;
    font-weight: 500;
  }
}

.data-source-mini {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #909399;
  margin-top: 8px;
  padding: 2px 10px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 12px;
}

.history-image-section {
  margin-bottom: 14px;

  h5 {
    margin: 0 0 10px;
    font-size: 14px;
    color: #303133;
    font-weight: 700;
  }
}

.history-image-wrapper {
  text-align: center;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 10px;
}

.history-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 10px;
  object-fit: contain;
}

/* ── Responsive ───────────────────────────────────────────── */
@media (max-width: 768px) {
  .food-recognition-view {
    padding: 16px 12px 32px;
  }

  .recognition-header {
    padding: 20px 16px;
    border-radius: 14px;
    margin-bottom: 24px;

    h2 {
      font-size: 26px;
    }
  }

  .recognition-body {
    grid-template-columns: 1fr;
  }

  .empty-card {
    grid-column: 1;
  }

  .basic-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .mineral-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .nutrition-mini-grid {
    grid-template-columns: 1fr;
  }
}
</style>
