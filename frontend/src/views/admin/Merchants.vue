<template>
  <div class="admin-merchants">
    <h2 class="page-title">商家管理</h2>

    <!-- Search & filter bar -->
    <el-card shadow="hover" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商家名称/用户名..."
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="statusFilter" placeholder="商家状态" clearable style="width: 140px" @change="handleSearch">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="已冻结" value="FROZEN" />
            <el-option label="待审核" value="PENDING" />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
      </div>
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
    <el-empty v-else-if="!merchants.length" description="暂无商家数据" />

    <!-- Merchant table -->
    <el-card v-else shadow="hover">
      <el-table :data="merchants" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="storeName" label="店铺名称" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="merchantStatusType(row.status)" size="small">
              {{ merchantStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'ACTIVE'">
              <el-popconfirm
                title="确定冻结该商家？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="toggleMerchantStatus(row, 'FROZEN')"
              >
                <template #reference>
                  <el-button text type="danger">冻结</el-button>
                </template>
              </el-popconfirm>
            </template>
            <template v-else-if="row.status === 'FROZEN'">
              <el-popconfirm
                title="确定解冻该商家？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="toggleMerchantStatus(row, 'ACTIVE')"
              >
                <template #reference>
                  <el-button text type="success">解冻</el-button>
                </template>
              </el-popconfirm>
            </template>
            <template v-else-if="row.status === 'PENDING'">
              <el-button text type="success" @click="toggleMerchantStatus(row, 'ACTIVE')">通过</el-button>
              <el-popconfirm
                title="确定拒绝该商家申请？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="toggleMerchantStatus(row, 'REJECTED')"
              >
                <template #reference>
                  <el-button text type="danger">拒绝</el-button>
                </template>
              </el-popconfirm>
            </template>
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
          @current-change="fetchMerchants"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMerchants, updateMerchantStatus } from '@/api'
import { Search } from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref('')
const merchants = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref('')

const merchantStatusMap = {
  ACTIVE: { label: '正常', type: 'success' },
  FROZEN: { label: '已冻结', type: 'danger' },
  PENDING: { label: '待审核', type: 'warning' },
  REJECTED: { label: '已拒绝', type: 'info' },
}

function merchantStatusLabel(status) {
  return merchantStatusMap[status]?.label || status
}

function merchantStatusType(status) {
  return merchantStatusMap[status]?.type || ''
}

async function fetchMerchants() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res = await getMerchants(params)
    const data = res.data
    merchants.value = data.records || data.list || data || []
    total.value = data.total || merchants.value.length
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取商家列表失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchMerchants()
}

async function toggleMerchantStatus(row, newStatus) {
  try {
    await updateMerchantStatus({ merchantId: row.id, status: newStatus })
    row.status = newStatus
  } catch (e) {
    console.error('Toggle merchant status failed:', e)
  }
}

onMounted(fetchMerchants)
</script>

<style scoped>
.admin-merchants {
  max-width: 1200px;
}
.page-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}
.toolbar-card {
  margin-bottom: 16px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.loading-container {
  padding: 40px;
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
