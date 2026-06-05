<template>
  <div class="order-detail">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
      <el-breadcrumb-item>订单详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="order.id">
      <!-- 订单状态 -->
      <el-card class="status-card" shadow="never">
        <div class="status-header">
          <el-tag :type="statusTagType(order.status)" effect="dark" size="large">
            {{ statusLabel(order.status) }}
          </el-tag>
          <span class="order-no-text">订单号：{{ order.orderNo || order.id }}</span>
          <span class="order-time" v-if="order.createdAt">下单时间：{{ order.createdAt }}</span>
        </div>
        <div class="status-actions" v-if="order.status === 'PENDING_PAYMENT'">
          <p class="status-hint">请尽快完成支付，逾期订单将自动取消</p>
          <div class="action-btns">
            <el-button type="primary" :loading="paying" @click="handlePayOrder">立即付款</el-button>
            <el-button :loading="cancelling" @click="handleCancelOrder">取消订单</el-button>
          </div>
        </div>
        <div class="status-actions" v-else-if="order.status === 'DELIVERED' || order.status === 'SHIPPED'">
          <el-button type="success" :loading="confirming" @click="handleConfirmOrder">确认收货</el-button>
        </div>
        <div class="status-actions" v-else-if="order.status === 'COMPLETED'">
          <el-button type="danger" plain :loading="refunding" @click="handleApplyRefund">申请退款</el-button>
        </div>
      </el-card>

      <!-- 物流信息 -->
      <el-card v-if="order.logistics" class="section-card" shadow="never">
        <template #header>
          <span class="section-title">物流信息</span>
        </template>
        <div class="logistics-info">
          <p>物流公司：{{ order.logistics.company || '-' }}</p>
          <p>物流单号：{{ order.logistics.trackingNo || '-' }}</p>
        </div>
      </el-card>

      <!-- 收货地址 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">收货地址</span>
        </template>
        <div class="address-block" v-if="order.address">
          <p><strong>{{ order.address.name || order.address.receiver }}</strong> {{ order.address.phone }}</p>
          <p>{{ order.address.province }}{{ order.address.city }}{{ order.address.district }}{{ order.address.detail || order.address.address }}</p>
        </div>
      </el-card>

      <!-- 商品列表 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">商品信息</span>
        </template>
        <el-table :data="orderItems" style="width: 100%">
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="item-cell">
                <el-image :src="row.productImage || '/placeholder.png'" fit="cover" class="item-img" />
                <span>{{ row.productName || row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center">
            <template #default="{ row }">x{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="price">¥{{ ((row.price || 0) * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 订单金额 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">订单金额</span>
        </template>
        <div class="amount-list">
          <div class="amount-row">
            <span>商品金额</span>
            <span>¥{{ (order.productAmount || order.amount || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-row">
            <span>运费</span>
            <span>¥{{ (order.shippingFee || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-row total">
            <span>实付金额</span>
            <span class="total-price">¥{{ (order.totalAmount || order.amount || 0).toFixed(2) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 订单时间线 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">订单日志</span>
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in orderLogs"
            :key="index"
            :timestamp="log.time"
            :type="log.type || 'primary'"
          >
            {{ log.content }}
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </template>

    <el-result v-else icon="error" title="订单不存在">
      <template #extra>
        <el-button type="primary" @click="$router.push('/orders')">返回订单列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder, confirmReceive, payOrder, applyRefund } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const order = ref({})
const loading = ref(true)
const paying = ref(false)
const cancelling = ref(false)
const confirming = ref(false)
const refunding = ref(false)

const orderItems = computed(() => {
  return order.value.items || order.value.orderItems || []
})

const orderLogs = computed(() => {
  return order.value.logs || order.value.timeline || []
})

function statusLabel(status) {
  const map = {
    'PENDING_PAYMENT': '待付款',
    'PENDING_DELIVERY': '待发货',
    'SHIPPED': '待收货',
    'DELIVERED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDING': '退款中',
    'REFUNDED': '已退款'
  }
  return map[status] || status
}

function statusTagType(status) {
  const map = {
    'PENDING_PAYMENT': 'warning',
    'PENDING_DELIVERY': 'primary',
    'SHIPPED': 'info',
    'DELIVERED': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    'REFUNDING': 'danger',
    'REFUNDED': 'danger'
  }
  return map[status] || 'info'
}

async function fetchOrder() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getOrderDetail(id)
    order.value = res.data || res
  } catch (e) {
    console.error('Failed to fetch order:', e)
    order.value = {}
  } finally {
    loading.value = false
  }
}

async function handlePayOrder() {
  paying.value = true
  try {
    await payOrder({ orderId: order.value.id, amount: order.value.totalAmount || order.value.amount })
    ElMessage.success('支付成功')
    await fetchOrder()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '支付失败')
  } finally {
    paying.value = false
  }
}

async function handleCancelOrder() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    cancelling.value = true
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    await fetchOrder()
  } catch {
    // cancelled
  } finally {
    cancelling.value = false
  }
}

async function handleConfirmOrder() {
  try {
    await ElMessageBox.confirm('确认收到商品了吗？', '提示', {
      type: 'info',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    confirming.value = true
    await confirmReceive(order.value.id)
    ElMessage.success('已确认收货')
    await fetchOrder()
  } catch {
    // cancelled
  } finally {
    confirming.value = false
  }
}

async function handleApplyRefund() {
  try {
    await ElMessageBox.prompt('请输入退款原因', '申请退款', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPlaceholder: '请描述退款原因'
    }).then(async ({ value }) => {
      refunding.value = true
      await applyRefund({ orderId: order.value.id, reason: value })
      ElMessage.success('退款申请已提交')
      await fetchOrder()
    })
  } catch {
    // cancelled
  } finally {
    refunding.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped>
.order-detail { padding-bottom: 40px; }
.breadcrumb { margin-bottom: 20px; }
.loading-wrap { padding: 40px; }

.status-card { margin-bottom: 20px; }
.status-header { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.order-no-text { font-size: 14px; color: #666; }
.order-time { font-size: 13px; color: #999; }

.status-actions { margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0; }
.status-hint { font-size: 13px; color: #999; margin: 0 0 12px; }
.action-btns { display: flex; gap: 12px; }

.section-card { margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: bold; }

.logistics-info p { margin: 4px 0; font-size: 14px; color: #555; }

.address-block p { margin: 4px 0; font-size: 14px; color: #555; }

.item-cell { display: flex; align-items: center; gap: 12px; }
.item-img { width: 60px; height: 60px; border-radius: 4px; flex-shrink: 0; background: #f5f5f5; }

.price { color: #ff4d4f; font-weight: bold; }

.amount-list { max-width: 400px; }
.amount-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; color: #666; }
.amount-row.total { border-top: 1px solid #eee; padding-top: 12px; margin-top: 8px; font-weight: bold; color: #333; }
.total-price { color: #ff4d4f; font-size: 20px; font-weight: bold; }
</style>
