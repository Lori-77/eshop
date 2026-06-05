<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <div class="profile-content">
      <!-- 头像 -->
      <el-card class="avatar-card" shadow="never">
        <div class="avatar-section">
          <el-avatar :size="100" :src="avatarUrl" class="profile-avatar" />
          <el-upload
            :show-file-list="false"
            :before-upload="handleBeforeUpload"
            :http-request="handleUpload"
            accept="image/*"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>
      </el-card>

      <!-- 基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span class="section-title">基本信息</span>
        </template>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          size="large"
        >
          <el-form-item label="用户名">
            <span class="field-value">{{ authStore.user?.username || '-' }}</span>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">
              保存修改
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, updateUserProfile } from '@/api'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const formRef = ref(null)
const saving = ref(false)

const form = reactive({
  nickname: '',
  phone: ''
})

const avatarUrl = ref('')

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

async function loadProfile() {
  try {
    const res = await getUserProfile()
    const data = res.data || res
    form.nickname = data.nickname || authStore.user?.nickname || ''
    form.phone = data.phone || ''
    avatarUrl.value = data.avatar || authStore.user?.avatar || ''
  } catch (e) {
    // Use store data as fallback
    form.nickname = authStore.user?.nickname || ''
    avatarUrl.value = authStore.user?.avatar || ''
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateUserProfile({
      nickname: form.nickname,
      phone: form.phone,
      avatar: avatarUrl.value
    })
    ElMessage.success('保存成功')
    // Refresh user info
    await authStore.fetchUser()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleReset() {
  loadProfile()
}

function handleBeforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  // For demo: create object URL for the uploaded file
  // In production this would upload to a server
  try {
    const file = options.file
    const url = URL.createObjectURL(file)
    avatarUrl.value = url
    ElMessage.success('头像已更新')
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page { padding-bottom: 40px; }
.page-title { font-size: 22px; color: #333; margin: 0 0 24px; }
.profile-content { max-width: 600px; }

.avatar-card { margin-bottom: 20px; }
.avatar-section { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 20px 0; }
.profile-avatar { border: 3px solid #1890ff; }

.info-card { margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: bold; }
.field-value { color: #333; font-size: 14px; }
</style>
