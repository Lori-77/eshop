<template>
  <div class="merchant-refunds">
    <h2 class="page-title">退款处理</h2>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- Error -->
    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      show-icon
      closable
      @close="error = ''"
    />

    <!-- Empty -->
    <el-empty v-else-if="!refunds.length" description="暂无退款申请" />

    <!-- Refund table -->
    <el-card v-else shadow="hover">
      <el-table :data="refunds" stripe style="width: 100%">
        <el-table-column prop="orderId" label="订单号" width="180" />
        <el-table-column label="退款金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ (row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退款原因" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.reason || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="120">
          <template #default="{ row }">
            <span>{{ row.username || row.user?.nickname || row.user?.username || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="openHandleDialog(row, 'APPROVED')">
                同意
              </el-button>
              <el-button type="danger" size="small" @click="openHandleDialog(row, 'REJECTED')">
                拒绝
              </el-button>
            </template>
            <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已同意</el-tag>
            <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
            <span v-else class="no-action">--</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Handle refund dialog -->
    <el-dialog
      v-model="handleDialogVisible"
      :title="handleAction === 'APPROVED' ? '同意退款' : '拒绝退款'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-alert
        :type="handleAction === 'APPROVED' ? 'warning' : 'error'"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #title>
          {{ handleAction === 'APPROVED'
            ? '确认同意该退款申请？退款金额将返还给买家。'
            : '确认拒绝该退款申请？请填写拒绝理由。'
          }}
        </template>
      </el-alert>
      <el-form
        ref="handleFormRef"
        :model="handleForm"
        :rules="handleFormRules"
        label-width="80px"
      >
        <el-form-item
          label="拒绝理由"
          prop="remark"
          v-if="handleAction === 'REJECTED'"
        >
          <el-input
            v-model="handleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请填写拒绝理由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-descriptions :column="1" border size="small" v-if="handleRefundingOrder">
          <el-descriptions-item label="订单号">{{ handleRefundingOrder.orderId }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ (handleRefundingOrder.amount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="退款原因">{{ handleRefundingOrder.reason }}</el-descriptions-item>
        </el-descriptions>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button
          :type="handleAction === 'APPROVED' ? 'success' : 'danger'"
          :loading="handling"
          @click="handleRefundAction"
        >
          {{ handleAction === 'APPROVED' ? '确认同意' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchantRefunds, handleRefund } from '@/api'

const loading = ref(true)
const error = ref('')
const refunds = ref([])

// Handle dialog
const handleDialogVisible = ref(false)
const handling = ref(false)
const handleAction = ref('APPROVED')
const handleRefundingOrder = ref(null)
const handleFormRef = ref(null)
const handleForm = ref({
  remark: '',
})
const handleFormRules = {
  remark: [{ required: true, message: '请填写拒绝理由', trigger: 'blur' }],
}

const statusMap = {
  PENDING: { label: '待处理', type: 'warning' },
  APPROVED: { label: '已同意', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
}

function statusLabel(status) {
  return statusMap[status]?.label || status
}

function statusType(status) {
  return statusMap[status]?.type || ''
}

async function fetchRefunds() {
  loading.value = true
  error.value = ''
  try {
    const res = await getMerchantRefunds()
    refunds.value = res.data || []
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取退款列表失败'
  } finally {
    loading.value = false
  }
}

function openHandleDialog(row, action) {
  handleAction.value = action
  handleRefundingOrder.value = row
  handleForm.value = { remark: '' }
  handleDialogVisible.value = true
}

async function handleRefundAction() {
  if (handleAction.value === 'REJECTED') {
    const valid = await handleFormRef.value.validate().catch(() => false)
    if (!valid) return
  }

  handling.value = true
  try {
    await handleRefund({
      refundId: handleRefundingOrder.value.id,
      action: handleAction.value,
      remark: handleAction.value === 'REJECTED' ? handleForm.value.remark : undefined,
    })
    handleDialogVisible.value = false
    fetchRefunds()
  } catch (e) {
    console.error('Handle refund failed:', e)
  } finally {
    handling.value = false
  }
}

onMounted(fetchRefunds)
</script>

<style scoped>
.merchant-refunds {
  max-width: 1200px;
}
.page-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}
.loading-container {
  padding: 40px;
}
.amount {
  font-weight: 600;
  color: #ff4d4f;
}
.no-action {
  color: #ccc;
}
</style>
