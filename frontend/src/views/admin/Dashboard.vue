<template>
  <div class="admin-dashboard">
    <h2 class="page-title">数据统计</h2>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
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
    <el-empty v-else-if="!stats" description="暂无统计数据" />

    <template v-else>
      <!-- Summary cards -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="4" v-for="card in statCards" :key="card.key">
          <el-card shadow="hover" class="stat-card" :body-style="{ padding: '16px' }">
            <div class="stat-inner">
              <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
                <el-icon :size="24"><component :is="card.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ card.label }}</div>
                <div class="stat-value">{{ stats[card.key] ?? 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Today's orders card -->
      <el-card shadow="hover" class="chart-section-card">
        <template #header>
          <div class="card-header">
            <span>今日订单</span>
            <span class="today-orders">{{ stats.todayOrders ?? 0 }} 单</span>
          </div>
        </template>

        <!-- Chart bars: last 7 days orders -->
        <div class="chart-container">
          <div class="chart-title">最近7天订单趋势</div>
          <div class="bar-chart">
            <div
              v-for="(item, index) in chartData"
              :key="index"
              class="bar-item"
            >
              <div class="bar-value">{{ item.count }}</div>
              <div
                class="bar"
                :style="{ height: barHeight(item.count) + 'px', background: barColor(index) }"
              />
              <div class="bar-label">{{ item.date?.slice(5) || item.date }}</div>
            </div>
          </div>
          <div v-if="!chartData.length" class="chart-empty">暂无图表数据</div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAdminStats, getChartData } from '@/api'
import {
  User, Shop, Goods, Tickets, Van, Money,
} from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref('')
const stats = ref(null)
const chartData = ref([])

const statCards = computed(() => [
  { key: 'totalUsers', label: '用户总数', icon: User, bg: '#e6f7ff', color: '#1890ff' },
  { key: 'totalMerchants', label: '商家总数', icon: Shop, bg: '#f6ffed', color: '#52c41a' },
  { key: 'totalProducts', label: '商品总数', icon: Goods, bg: '#fff7e6', color: '#fa8c16' },
  { key: 'totalOrders', label: '订单总数', icon: Tickets, bg: '#f0f5ff', color: '#2f54eb' },
  { key: 'pendingDelivery', label: '待发货', icon: Van, bg: '#fffbe6', color: '#fadb14' },
  { key: 'pendingRefunds', label: '待处理退款', icon: Money, bg: '#fff1f0', color: '#ff4d4f' },
])

const barColors = ['#1890ff', '#52c41a', '#fa8c16', '#ff4d4f', '#722ed1', '#13c2c2', '#eb2f96']

function barColor(index) {
  return barColors[index % barColors.length]
}

function barHeight(count) {
  const max = Math.max(...chartData.value.map(d => d.count), 1)
  return Math.max(4, (count / max) * 200)
}

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const [statsRes, chartRes] = await Promise.all([
      getAdminStats(),
      getChartData('orders_7days'),
    ])
    stats.value = statsRes.data
    chartData.value = chartRes.data || []
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取统计数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.admin-dashboard {
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
  gap: 12px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  white-space: nowrap;
}
.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}
.today-orders {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}
.chart-section-card {
  margin-top: 8px;
}
.chart-container {
  text-align: center;
}
.chart-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
}
.bar-chart {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 240px;
  padding: 0 16px;
}
.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.bar-value {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}
.bar {
  width: 36px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  min-height: 4px;
}
.bar-label {
  font-size: 12px;
  color: #999;
}
.chart-empty {
  padding: 40px;
  color: #999;
}
</style>
