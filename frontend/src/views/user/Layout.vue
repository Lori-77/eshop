<template>
  <div class="user-layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">🛒 网上购物商城</router-link>
        <div class="search-bar" v-if="showSearch">
          <el-input v-model="keyword" placeholder="搜索商品..." size="large" @keyup.enter="search">
            <template #append>
              <el-button @click="search" :icon="Search">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="header-actions">
          <template v-if="auth.isLoggedIn && auth.isUser">
            <router-link to="/cart" class="cart-link">
              <el-badge :value="cart.totalCount" :hidden="!cart.totalCount">
                <el-icon :size="22"><ShoppingCart /></el-icon>
              </el-badge>
              <span>购物车</span>
            </router-link>
            <router-link to="/orders" class="header-btn">我的订单</router-link>
            <el-dropdown>
              <span class="header-btn">{{ auth.user?.nickname }}</span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/addresses')">收货地址</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else-if="auth.isLoggedIn && (auth.isMerchant || auth.isAdmin)">
            <span class="header-btn">{{ auth.user?.nickname }}</span>
            <el-button type="primary" size="small" @click="goBackend">进入后台</el-button>
            <el-button size="small" @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <router-link to="/login" class="header-btn">登录</router-link>
            <router-link to="/register">
              <el-button type="primary" size="small">注册</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </header>
    <nav class="nav" v-if="showNav">
      <div class="nav-inner">
        <div class="categories-dropdown">
          <span class="all-cats">全部商品分类</span>
        </div>
        <router-link to="/" class="nav-item">首页</router-link>
        <a class="nav-item" v-for="cat in categories" :key="cat.id"
           @click="$router.push({ path: '/', query: { categoryId: cat.id } })">{{ cat.name }}</a>
      </div>
    </nav>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>© 2026 网上购物商城 - 软件工程大作业</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { getCategories } from '@/api'
import { Search, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const keyword = ref('')
const categories = ref([])

const showSearch = computed(() => !auth.isLoggedIn || auth.isUser)
const showNav = computed(() => !auth.isLoggedIn || auth.isUser)

function search() {
  if (keyword.value.trim()) {
    router.push({ path: '/', query: { keyword: keyword.value.trim() } })
  }
}

function goBackend() {
  if (auth.isMerchant) router.push('/merchant')
  else if (auth.isAdmin) router.push('/admin')
}

function handleLogout() {
  auth.logout()
  router.push('/')
}

onMounted(async () => {
  if (auth.isLoggedIn) {
    auth.fetchUser()
    if (auth.isUser) cart.fetchCart()
  }
  try {
    const res = await getCategories()
    // Show only top-level categories
    categories.value = res.data.filter(c => c.children?.length).slice(0, 8)
  } catch {}
})
</script>

<style scoped>
.user-layout { min-height: 100vh; display: flex; flex-direction: column; }
.header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; height: 64px; gap: 24px; }
.logo { font-size: 20px; font-weight: bold; color: #1890ff; white-space: nowrap; }
.search-bar { flex: 1; max-width: 500px; }
.header-actions { display: flex; align-items: center; gap: 16px; white-space: nowrap; }
.cart-link { display: flex; align-items: center; gap: 4px; color: #333; font-size: 14px; }
.header-btn { color: #333; font-size: 14px; cursor: pointer; }
.nav { background: #1890ff; }
.nav-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; padding: 0 20px; height: 40px; }
.all-cats { color: #fff; font-size: 14px; font-weight: bold; margin-right: 24px; cursor: pointer; }
.nav-item { color: rgba(255,255,255,0.9); font-size: 14px; margin-right: 20px; cursor: pointer; }
.nav-item:hover { color: #fff; }
.main { flex: 1; max-width: 1200px; margin: 0 auto; padding: 20px; width: 100%; }
.footer { background: #333; color: #999; text-align: center; padding: 24px; margin-top: auto; }
</style>
