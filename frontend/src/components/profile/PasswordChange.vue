<template>
  <div class="password-change">
    <div class="change-header">
      <h2 class="title">修改密码</h2>
    </div>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      class="change-form"
    >
      <!-- 原密码 -->
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="formData.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
          clearable
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="formData.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
          clearable
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
        <div class="password-hint">
          密码需包含大小写字母、数字，长度8-20位
        </div>
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="formData.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          clearable
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          <el-icon v-if="!loading"><Select /></el-icon>
          确认修改
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Lock, Select, RefreshLeft } from '@element-plus/icons-vue'
import api from '@/services/api'
import message from '@/utils/message'

const formRef = ref()
const loading = ref(false)

const formData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证新密码
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value === formData.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,20}$/.test(value)) {
    callback(new Error('密码需包含大小写字母、数字，长度8-20位'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== formData.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 提交修改
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    const response = await api.put('/user/password', {
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword,
      confirmPassword: formData.confirmPassword
    })

    if (response.data.code === 200) {
      message.success('密码修改成功，请重新登录')
      handleReset()
      
      // 3秒后跳转到登录页
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    } else {
      message.error(response.data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
    if (error.response?.data) {
      const errorData = error.response.data
      if (errorData.code === 40010) {
        message.error('原密码错误')
      } else {
        message.error(errorData.message || '修改失败')
      }
    } else {
      message.error('网络错误，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.password-change {
  .change-header {
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

  .change-form {
    max-width: 600px;

    :deep(.el-form-item) {
      margin-bottom: 24px;
    }

    :deep(.el-input) {
      .el-input__inner {
        height: 44px;
      }
    }

    .password-hint {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
      line-height: 1.5;
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
