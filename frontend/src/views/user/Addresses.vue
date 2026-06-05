<template>
  <div class="addresses-page">
    <div class="page-header">
      <h2 class="page-title">收货地址</h2>
      <el-button type="primary" @click="openAddDialog">添加新地址</el-button>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>

    <el-empty v-else-if="addresses.length === 0" description="暂无收货地址" />

    <div v-else class="address-list">
      <el-card
        v-for="addr in addresses"
        :key="addr.id"
        shadow="hover"
        class="address-card"
      >
        <div class="address-card-body">
          <div class="address-info">
            <div class="address-top">
              <span class="addr-name">{{ addr.name || addr.receiver }}</span>
              <span class="addr-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" size="small" type="primary" class="default-tag">默认</el-tag>
            </div>
            <p class="addr-detail">
              {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail || addr.address }}
            </p>
          </div>
          <div class="address-actions">
            <el-button
              v-if="!addr.isDefault"
              type="primary"
              link
              size="small"
              @click="handleSetDefault(addr)"
            >
              设为默认
            </el-button>
            <el-button type="primary" link size="small" @click="openEditDialog(addr)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(addr)">
              删除
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑地址对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑地址' : '添加地址'"
      width="520px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-input v-model="form.region" placeholder="如：广东省广州市天河区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAddresses, saveAddress, updateAddress, deleteAddress } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const addresses = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  phone: '',
  region: '',
  detail: '',
  isDefault: false
})

const rules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  region: [{ required: true, message: '请输入所在地区', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function loadAddresses() {
  loading.value = true
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
  } catch (e) {
    console.error('Failed to load addresses:', e)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.name = ''
  form.phone = ''
  form.region = ''
  form.detail = ''
  form.isDefault = false
  editingId.value = null
  isEditing.value = false
}

function openAddDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(addr) {
  isEditing.value = true
  editingId.value = addr.id
  form.name = addr.name || addr.receiver || ''
  form.phone = addr.phone || ''
  form.region = [addr.province, addr.city, addr.district].filter(Boolean).join(' ')
  form.detail = addr.detail || addr.address || ''
  form.isDefault = addr.isDefault || false
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const regions = form.region.split(' ')
    const addressData = {
      name: form.name,
      phone: form.phone,
      province: regions[0] || form.region,
      city: regions[1] || '',
      district: regions[2] || '',
      detail: form.detail,
      isDefault: form.isDefault
    }

    if (isEditing.value) {
      await updateAddress({ ...addressData, id: editingId.value })
      ElMessage.success('地址更新成功')
    } else {
      await saveAddress(addressData)
      ElMessage.success('地址添加成功')
    }

    dialogVisible.value = false
    resetForm()
    await loadAddresses()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(addr) {
  try {
    await ElMessageBox.confirm(`确定要删除 ${addr.name || addr.receiver} 的地址吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteAddress(addr.id)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch {
    // cancelled
  }
}

async function handleSetDefault(addr) {
  try {
    await updateAddress({ ...addr, isDefault: true })
    ElMessage.success('默认地址已更新')
    await loadAddresses()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.addresses-page { padding-bottom: 40px; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 22px; color: #333; margin: 0; }

.loading-wrap { padding: 40px; }

.address-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
@media (max-width: 768px) { .address-list { grid-template-columns: 1fr; } }

.address-card { border-radius: 8px; }
.address-card-body { display: flex; flex-direction: column; gap: 12px; }

.address-top { display: flex; align-items: center; gap: 12px; margin-bottom: 4px; }
.addr-name { font-size: 15px; font-weight: bold; color: #333; }
.addr-phone { font-size: 14px; color: #666; }
.default-tag { flex-shrink: 0; }

.addr-detail { font-size: 13px; color: #999; margin: 0; line-height: 1.5; }

.address-actions { display: flex; gap: 8px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
</style>
