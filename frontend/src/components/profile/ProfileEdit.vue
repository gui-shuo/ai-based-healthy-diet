<template>
  <div class="profile-edit">
    <div class="edit-header">
      <h2 class="title">编辑资料</h2>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="edit-form"
    >
      <!-- 头像上传 -->
      <el-form-item label="头像">
        <AvatarUpload v-model="formData.avatar" @uploaded="handleAvatarUploaded" />
      </el-form-item>

      <!-- 昵称 -->
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="formData.nickname"
          placeholder="请输入昵称"
          maxlength="20"
          show-word-limit
          clearable
        />
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="formData.email"
          placeholder="请输入邮箱"
          clearable
        >
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 手机号 -->
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="formData.phone"
          placeholder="请输入手机号"
          maxlength="11"
          clearable
        >
          <template #prefix>
            <el-icon><Phone /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSave">
          <el-icon v-if="!loading"><Select /></el-icon>
          保存修改
        </el-button>
        <el-button @click="handleCancel">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message, Phone, Select, Close } from '@element-plus/icons-vue'
import AvatarUpload from './AvatarUpload.vue'
import api from '@/services/api'
import message from '@/utils/message'
import { useAuthStore } from '@/stores/auth'

const emit = defineEmits(['saved', 'cancel'])

const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const formData = reactive({
  avatar: '',
  nickname: '',
  email: '',
  phone: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取用户资料
const fetchProfile = async () => {
  try {
    const response = await api.get('/user/profile')
    if (response.data.code === 200) {
      const data = response.data.data
      formData.avatar = data.avatar || ''
      formData.nickname = data.nickname || ''
      formData.email = data.email || ''
      formData.phone = data.phone || ''
    }
  } catch (error) {
    console.error('获取用户资料失败:', error)
    message.error('获取用户资料失败')
  }
}

// 保存修改
const handleSave = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    const response = await api.put('/user/profile', {
      avatar: formData.avatar,
      nickname: formData.nickname,
      email: formData.email,
      phone: formData.phone || undefined
    })

    if (response.data.code === 200) {
      message.success('资料修改成功')
      
      // 更新store中的用户信息
      authStore.setUser({
        ...authStore.user,
        ...response.data.data
      })
      
      emit('saved')
    } else {
      message.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    if (error.response?.data) {
      message.error(error.response.data.message || '保存失败')
    } else {
      message.error('网络错误，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  emit('cancel')
}

// 头像上传成功后立即更新store
const handleAvatarUploaded = (avatarUrl) => {
  // 立即更新store中的用户信息
  authStore.setUser({
    ...authStore.user,
    avatar: avatarUrl
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
.profile-edit {
  .edit-header {
    margin-bottom: 32px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f0f0f0;

    .title {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }
  }

  .edit-form {
    max-width: 600px;

    :deep(.el-form-item) {
      margin-bottom: 24px;
    }

    :deep(.el-input) {
      .el-input__inner {
        height: 44px;
      }
    }

    :deep(.el-button) {
      height: 44px;
      padding: 0 32px;
      font-size: 15px;

      .el-icon {
        margin-right: 6px;
      }
    }
  }
}
</style>
