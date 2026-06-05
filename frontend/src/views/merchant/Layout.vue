<template>
  <div class="merchant-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <span class="sidebar-title">商家管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/merchant">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/merchant/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/refunds">
          <el-icon><Money /></el-icon>
          <span>退款处理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- Main content area -->
    <div class="main-area">
      <!-- Top bar -->
      <header class="top-bar">
        <div class="top-bar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="'/'">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="top-bar-right">
          <span class="store-info">
            <el-icon><Shop /></el-icon>
            <span>{{ auth.user?.storeName || '我的店铺' }}</span>
          </span>
          <span class="merchant-name">{{ auth.user?.nickname || auth.user?.username }}</span>
          <el-button text type="primary" @click="goToShop">回到商城</el-button>
          <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <!-- Page content -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { DataAnalysis, Goods, Tickets, Money, Shop } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => route.path)

const breadcrumbTitle = computed(() => {
  const map = {
    '/merchant': '仪表盘',
    '/merchant/products': '商品管理',
    '/merchant/orders': '订单管理',
    '/merchant/refunds': '退款处理',
  }
  return map[route.path] || '商家管理'
})

function goToShop() {
  router.push('/')
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.merchant-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}
.sidebar {
  width: 220px;
  background: #001529;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 10;
}
.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-menu {
  border-right: none;
  flex: 1;
}
.sidebar-menu .el-menu-item {
  font-size: 14px;
}
.sidebar-menu .el-menu-item.is-active {
  background-color: #1890ff !important;
}
.main-area {
  margin-left: 220px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.top-bar {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 9;
}
.top-bar-left {
  display: flex;
  align-items: center;
}
.top-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.store-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #333;
  font-size: 14px;
}
.merchant-name {
  color: #666;
  font-size: 14px;
}
.content {
  flex: 1;
  padding: 24px;
}
</style>
