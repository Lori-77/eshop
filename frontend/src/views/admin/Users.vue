<template>
  <div class="admin-users">
    <h2 class="page-title">用户管理</h2>

    <!-- Search & filter bar -->
    <el-card shadow="hover" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/昵称/手机号..."
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="statusFilter" placeholder="用户状态" clearable style="width: 140px" @change="handleSearch">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="已冻结" value="FROZEN" />
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
    <el-empty v-else-if="!users.length" description="暂无用户数据" />

    <!-- User table -->
    <el-card v-else shadow="hover">
      <el-table :data="users" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              :title="row.status === 'ACTIVE' ? '确定冻结该用户？' : '确定解冻该用户？'"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="toggleUserStatus(row)"
            >
              <template #reference>
                <el-button
                  text
                  :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
                >
                  {{ row.status === 'ACTIVE' ? '冻结' : '解冻' }}
                </el-button>
              </template>
            </el-popconfirm>
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
          @current-change="fetchUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers, updateUserStatus } from '@/api'
import { Search } from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref('')
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const statusFilter = ref('')

async function fetchUsers() {
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
    const res = await getUsers(params)
    const data = res.data
    users.value = data.records || data.list || data || []
    total.value = data.total || users.value.length
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取用户列表失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchUsers()
}

async function toggleUserStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'FROZEN' : 'ACTIVE'
  try {
    await updateUserStatus({ userId: row.id, status: newStatus })
    row.status = newStatus
  } catch (e) {
    console.error('Toggle status failed:', e)
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-users {
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
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
