<template>
  <div class="merchant-dashboard">
    <h2 class="page-title">商家首页</h2>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Error state -->
    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      show-icon
      closable
      @close="error = ''"
    />

    <!-- Empty state -->
    <el-empty v-else-if="!stats" description="暂无统计数据" />

    <!-- Dashboard content -->
    <template v-else>
      <!-- Statistics cards -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon goods-icon">
                <el-icon :size="32"><Goods /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">商品总数</div>
                <el-statistic :value="stats.totalProducts" />
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon orders-icon">
                <el-icon :size="32"><Tickets /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">订单总数</div>
                <el-statistic :value="stats.totalOrders" />
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon delivery-icon">
                <el-icon :size="32"><Van /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">待发货</div>
                <el-statistic :value="stats.pendingDelivery" />
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-inner">
              <div class="stat-icon refund-icon">
                <el-icon :size="32"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">待处理退款</div>
                <el-statistic :value="stats.pendingRefunds" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Recent orders -->
      <el-card shadow="hover" class="recent-orders-card">
        <template #header>
          <div class="card-header">
            <span>最近订单</span>
            <el-button text type="primary" @click="$router.push('/merchant/orders')">
              查看全部
            </el-button>
          </div>
        </template>
        <el-table :data="recentOrders" stripe style="width: 100%" v-if="recentOrders.length">
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <span>{{ row.items?.map(i => i.productName).join(', ') || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="金额" width="120">
            <template #default="{ row }">
              <span>¥{{ row.totalAmount?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="receiverName" label="收货人" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="下单时间" width="180" />
        </el-table>
        <el-empty v-else description="暂无订单" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMerchantStats, getMerchantOrders } from '@/api'
import { Goods, Tickets, Van, Money } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(true)
const error = ref('')
const stats = ref(null)
const recentOrders = ref([])

const statusMap = {
  PENDING: { label: '待付款', type: 'warning' },
  PAID: { label: '已付款', type: 'info' },
  SHIPPED: { label: '已发货', type: 'primary' },
  DELIVERED: { label: '已送达', type: '' },
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

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const [statsRes, ordersRes] = await Promise.all([
      getMerchantStats(),
      getMerchantOrders({ page: 1, size: 10 }),
    ])
    stats.value = statsRes.data
    recentOrders.value = ordersRes.data?.records || ordersRes.data || []
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.merchant-dashboard {
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
.stats-row {
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 4px;
}
.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.goods-icon {
  background: #e6f7ff;
  color: #1890ff;
}
.orders-icon {
  background: #f6ffed;
  color: #52c41a;
}
.delivery-icon {
  background: #fff7e6;
  color: #fa8c16;
}
.refund-icon {
  background: #fff1f0;
  color: #ff4d4f;
}
.stat-info {
  flex: 1;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}
.recent-orders-card {
  margin-top: 8px;
}
</style>
