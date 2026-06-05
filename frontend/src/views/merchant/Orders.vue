<template>
  <div class="merchant-orders">
    <h2 class="page-title">订单管理</h2>

    <!-- Status filter tabs -->
    <el-card shadow="hover" class="filter-card">
      <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待付款" name="PENDING_PAYMENT" />
        <el-tab-pane label="待发货" name="PENDING_DELIVERY" />
        <el-tab-pane label="已发货" name="DELIVERED" />
        <el-tab-pane label="已完成" name="COMPLETED" />
        <el-tab-pane label="退款中" name="REFUNDING" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>
    </el-card>

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
    <el-empty v-else-if="!orders.length" description="暂无订单数据" />

    <!-- Order table -->
    <el-card v-else shadow="hover">
      <el-table :data="orders" stripe style="width: 100%">
        <el-table-column prop="id" label="订单号" width="180" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div v-if="row.items && row.items.length">
              <div v-for="item in row.items" :key="item.id" class="order-item-summary">
                <span class="item-name">{{ item.productName }}</span>
                <span class="item-qty">x{{ item.quantity }}</span>
              </div>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ (row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收货人" width="120">
          <template #default="{ row }">
            <div>{{ row.receiverName }}</div>
            <div>{{ row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING_DELIVERY'"
              text
              type="primary"
              @click="openShipDialog(row)"
            >
              发货
            </el-button>
            <el-tag v-else-if="row.status === 'DELIVERED'" type="info" size="small">已发货</el-tag>
            <span v-else class="no-action">--</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>

    <!-- Ship dialog -->
    <el-dialog
      v-model="shipDialogVisible"
      title="发货"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-alert
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #title>
          订单号：{{ shippingOrder?.id }}
        </template>
      </el-alert>
      <el-form
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipFormRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="物流公司" prop="company">
          <el-select v-model="shipForm.company" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰" value="顺丰" />
            <el-option label="中通" value="中通" />
            <el-option label="圆通" value="圆通" />
            <el-option label="韵达" value="韵达" />
            <el-option label="EMS" value="EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input
            v-model="shipForm.trackingNo"
            placeholder="请输入物流单号"
            maxlength="50"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="handleShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchantOrders, shipOrder } from '@/api'

const loading = ref(true)
const error = ref('')
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeStatus = ref('')

// Ship dialog
const shipDialogVisible = ref(false)
const shipping = ref(false)
const shippingOrder = ref(null)
const shipFormRef = ref(null)
const shipForm = ref({
  company: '',
  trackingNo: '',
})
const shipFormRules = {
  company: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
}

const statusMap = {
  PENDING_PAYMENT: { label: '待付款', type: 'warning' },
  PENDING_DELIVERY: { label: '待发货', type: 'info' },
  DELIVERED: { label: '已发货', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' },
  REFUNDING: { label: '退款中', type: 'warning' },
  REFUNDED: { label: '已退款', type: 'info' },
}

function statusLabel(status) {
  return statusMap[status]?.label || status
}

function statusType(status) {
  return statusMap[status]?.type || ''
}

async function fetchOrders() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (activeStatus.value) {
      params.status = activeStatus.value
    }
    const res = await getMerchantOrders(params)
    const data = res.data
    orders.value = data.records || data.list || data || []
    total.value = data.total || orders.value.length
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取订单列表失败'
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  fetchOrders()
}

function openShipDialog(order) {
  shippingOrder.value = order
  shipForm.value = { company: '', trackingNumber: '' }
  shipDialogVisible.value = true
}

async function handleShip() {
  const valid = await shipFormRef.value.validate().catch(() => false)
  if (!valid) return

  shipping.value = true
  try {
    await shipOrder({
      orderId: shippingOrder.value.id,
      company: shipForm.value.company,
      trackingNo: shipForm.value.trackingNo,
    })
    shipDialogVisible.value = false
    fetchOrders()
  } catch (e) {
    console.error('Ship failed:', e)
  } finally {
    shipping.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped>
.merchant-orders {
  max-width: 1200px;
}
.page-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}
.filter-card {
  margin-bottom: 16px;
}
.loading-container {
  padding: 40px;
}
.order-item-summary {
  display: flex;
  gap: 8px;
  line-height: 1.8;
}
.item-name {
  color: #333;
}
.item-qty {
  color: #999;
}
.amount {
  font-weight: 600;
  color: #ff4d4f;
}
.receiver-phone {
  font-size: 12px;
  color: #999;
}
.no-action {
  color: #ccc;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
