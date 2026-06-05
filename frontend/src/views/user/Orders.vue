<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <!-- 状态标签 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="order-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待付款" name="pending_payment" />
      <el-tab-pane label="待发货" name="pending_delivery" />
      <el-tab-pane label="待收货" name="delivered" />
      <el-tab-pane label="已完成" name="completed" />
    </el-tabs>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="orders.length === 0" description="暂无订单" />

    <!-- 订单列表 -->
    <div v-else class="order-list">
      <el-card
        v-for="order in orders"
        :key="order.id"
        shadow="hover"
        class="order-card"
      >
        <div class="order-header">
          <div class="order-no">
            <span class="label">订单号：</span>
            <span>{{ order.orderNo || order.id }}</span>
          </div>
          <el-tag
            :type="statusTagType(order.status)"
            effect="dark"
            size="small"
          >
            {{ statusLabel(order.status) }}
          </el-tag>
        </div>

        <div class="order-items">
          <div
            class="order-item"
            v-for="item in (order.items || order.orderItems || [])"
            :key="item.id"
          >
            <el-image
              :src="item.productImage || '/placeholder.png'"
              fit="cover"
              class="order-item-img"
            />
            <div class="order-item-info">
              <p class="order-item-name">{{ item.productName || item.name }}</p>
              <p class="order-item-spec">¥{{ (item.price || 0).toFixed(2) }} x {{ item.quantity }}</p>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-total">
            共 {{ order.totalQuantity || (order.items || order.orderItems || []).reduce((s, i) => s + i.quantity, 0) }} 件商品
            合计：<span class="total-price">¥{{ (order.totalAmount || order.amount || 0).toFixed(2) }}</span>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 'PENDING_PAYMENT'"
              type="primary"
              size="small"
              @click="handlePay(order)"
            >
              立即付款
            </el-button>
            <el-button
              v-if="order.status === 'PENDING_PAYMENT'"
              size="small"
              @click="handleCancel(order)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 'DELIVERED' || order.status === 'SHIPPED'"
              type="success"
              size="small"
              @click="handleConfirm(order)"
            >
              确认收货
            </el-button>
            <el-button
              size="small"
              @click="viewDetail(order)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button type="primary" link :loading="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserOrders, cancelOrder, confirmReceive, payOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

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

async function fetchOrders(reset = false) {
  if (reset) {
    page.value = 1
    hasMore.value = true
  }
  loading.value = reset
  loadingMore.value = !reset

  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value
    }
    const res = await getUserOrders(params)
    const list = res.data.records || res.data.list || res.data || []
    if (reset) {
      orders.value = list
    } else {
      orders.value = [...orders.value, ...list]
    }
    hasMore.value = list.length >= pageSize.value
  } catch (e) {
    console.error('Failed to fetch orders:', e)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function handleTabChange() {
  fetchOrders(true)
}

function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  page.value++
  fetchOrders(false)
}

async function handlePay(order) {
  try {
    await payOrder({ orderId: order.id, amount: order.totalAmount || order.amount })
    ElMessage.success('支付成功')
    await fetchOrders(true)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '支付失败')
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    await fetchOrders(true)
  } catch {
    // cancelled
  }
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm('确认收到商品了吗？', '提示', {
      type: 'info',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await confirmReceive(order.id)
    ElMessage.success('已确认收货')
    await fetchOrders(true)
  } catch {
    // cancelled
  }
}

function viewDetail(order) {
  router.push(`/orders/${order.id}`)
}

onMounted(() => {
  fetchOrders(true)
})
</script>

<style scoped>
.orders-page { padding-bottom: 40px; }
.page-title { font-size: 22px; color: #333; margin: 0 0 16px; }
.order-tabs { margin-bottom: 20px; }
.loading-wrap { padding: 40px; }

.order-list { display: flex; flex-direction: column; gap: 16px; }

.order-card { border-radius: 8px; }
.order-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; margin-bottom: 12px; }
.order-no { font-size: 13px; color: #666; }
.order-no .label { color: #999; }

.order-items { display: flex; flex-direction: column; gap: 12px; margin-bottom: 12px; }
.order-item { display: flex; gap: 12px; }
.order-item-img { width: 64px; height: 64px; border-radius: 4px; flex-shrink: 0; background: #f5f5f5; }
.order-item-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.order-item-name { font-size: 14px; color: #333; margin: 0 0 4px; }
.order-item-spec { font-size: 12px; color: #999; margin: 0; }

.order-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.order-total { font-size: 13px; color: #666; }
.total-price { color: #ff4d4f; font-size: 18px; font-weight: bold; }
.order-actions { display: flex; gap: 8px; }

.load-more { text-align: center; padding: 20px; }

@media (max-width: 768px) {
  .order-footer { flex-direction: column; gap: 12px; }
}
</style>
