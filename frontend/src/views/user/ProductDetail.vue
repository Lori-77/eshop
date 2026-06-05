<template>
  <div class="product-detail">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item v-if="product.categoryName">{{ product.categoryName }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-if="product && product.id">
      <div class="detail-main">
        <!-- 商品图片 -->
        <div class="detail-image">
          <el-image
            :src="product.mainImage || '/placeholder.png'"
            :alt="product.name"
            fit="cover"
            class="main-img"
          />
        </div>

        <!-- 商品信息 -->
        <div class="detail-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-price-bar">
            <span class="price-label">价格：</span>
            <span class="price-value">¥{{ product.price?.toFixed(2) }}</span>
          </div>
          <div class="product-stats">
            <div class="stat-item">
              <span class="stat-label">销量</span>
              <span class="stat-value">{{ product.sales || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">库存</span>
              <span class="stat-value">{{ product.stock || 0 }}</span>
            </div>
          </div>

          <!-- 数量选择 -->
          <div class="quantity-section">
            <span class="quantity-label">数量：</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock || 99"
              size="large"
            />
            <span class="stock-text">件 (库存 {{ product.stock || 0 }} 件)</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :disabled="!canBuy"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
            <el-button
              type="danger"
              size="large"
              :disabled="!canBuy"
              @click="handleBuyNow"
            >
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <el-card class="desc-section" shadow="never">
        <template #header>
          <span class="desc-title">商品详情</span>
        </template>
        <div class="desc-content" v-html="product.description || '暂无详细描述'"></div>
      </el-card>
    </template>

    <el-result v-else-if="!loading" icon="error" title="商品不存在" sub-title="请返回首页浏览其他商品">
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail } from '@/api'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const product = ref({})
const loading = ref(true)
const quantity = ref(1)

const canBuy = computed(() => {
  return product.value.stock && product.value.stock > 0
})

async function fetchProduct() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getProductDetail(id)
    product.value = res.data || res
  } catch (e) {
    console.error('Failed to fetch product detail:', e)
    product.value = {}
  } finally {
    loading.value = false
  }
}

async function handleAddToCart() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.add(product.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

async function handleBuyNow() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.add(product.value.id, quantity.value)
    ElMessage.success('已加入购物车，即将跳转结算')
    router.push('/checkout')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
.product-detail { padding-bottom: 40px; }
.breadcrumb { margin-bottom: 20px; }

.loading-wrap { padding: 40px; }

.detail-main { display: flex; gap: 40px; margin-bottom: 40px; }
@media (max-width: 768px) { .detail-main { flex-direction: column; } }

.detail-image { flex-shrink: 0; width: 480px; height: 480px; border-radius: 8px; overflow: hidden; background: #f5f5f5; }
@media (max-width: 768px) { .detail-image { width: 100%; height: auto; } }
.main-img { width: 100%; height: 100%; object-fit: cover; }

.detail-info { flex: 1; }
.product-name { font-size: 24px; color: #333; margin: 0 0 20px; }

.product-price-bar { background: #fff7e6; padding: 16px 20px; border-radius: 4px; margin-bottom: 20px; }
.price-label { font-size: 14px; color: #666; }
.price-value { font-size: 28px; color: #ff4d4f; font-weight: bold; }

.product-stats { display: flex; gap: 40px; margin-bottom: 24px; }
.stat-item { display: flex; flex-direction: column; }
.stat-label { font-size: 13px; color: #999; }
.stat-value { font-size: 18px; color: #333; margin-top: 4px; }

.quantity-section { display: flex; align-items: center; gap: 12px; margin-bottom: 32px; }
.quantity-label { font-size: 14px; color: #666; }
.stock-text { font-size: 12px; color: #999; }

.action-buttons { display: flex; gap: 16px; }

.desc-section { margin-top: 20px; }
.desc-title { font-size: 16px; font-weight: bold; }
.desc-content { padding: 16px 0; line-height: 1.8; color: #555; }
</style>
