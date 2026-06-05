<template>
  <div class="home">
    <!-- Banner 轮播 -->
    <div class="banner">
      <div class="banner-slide banner-1">
        <div class="banner-text">
          <h2>品质生活，从这里开始</h2>
          <p>精选好物，限时特惠</p>
        </div>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="categories-bar">
      <el-tag
        :type="!currentCategoryId ? 'primary' : 'info'"
        :effect="!currentCategoryId ? 'dark' : 'plain'"
        @click="selectCategory(null)"
        class="cat-tag"
      >
        全部
      </el-tag>
      <el-tag
        v-for="cat in categories"
        :key="cat.id"
        :type="currentCategoryId == cat.id ? 'primary' : 'info'"
        :effect="currentCategoryId == cat.id ? 'dark' : 'plain'"
        @click="selectCategory(cat.id)"
        class="cat-tag"
      >
        {{ cat.name }}
      </el-tag>
    </div>

    <!-- 排序选项 -->
    <div class="sort-bar">
      <span class="sort-label">排序：</span>
      <el-radio-group v-model="sortBy" @change="handleSortChange" size="small">
        <el-radio-button value="">默认</el-radio-button>
        <el-radio-button value="price_asc">价格 ↑</el-radio-button>
        <el-radio-button value="price_desc">价格 ↓</el-radio-button>
        <el-radio-button value="sales">销量</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商品列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    <template v-else>
      <div v-if="products.length === 0" class="empty-container">
        <el-empty description="暂无商品" />
      </div>
      <div v-else class="product-grid">
        <el-card
          v-for="product in products"
          :key="product.id"
          :body-style="{ padding: '0' }"
          shadow="hover"
          class="product-card"
          @click="goToDetail(product.id)"
        >
          <div class="product-img">
            <img :src="product.mainImage || '/placeholder.png'" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-meta">
              <span class="product-price">¥{{ product.price?.toFixed(2) }}</span>
              <span class="product-sales">已售 {{ product.sales || 0 }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </template>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts, getCategories } from '@/api'

const route = useRoute()
const router = useRouter()

const products = ref([])
const categories = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const sortBy = ref('')
const currentCategoryId = ref(null)
const keyword = ref('')

async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
    }
    if (currentCategoryId.value) params.categoryId = currentCategoryId.value
    if (keyword.value) params.keyword = keyword.value
    if (sortBy.value) params.sort = sortBy.value
    const res = await getProducts(params)
    products.value = res.data.records || res.data.list || res.data || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('Failed to fetch products:', e)
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch categories:', e)
  }
}

function selectCategory(id) {
  currentCategoryId.value = id
  page.value = 1
  router.replace({ query: { ...route.query, categoryId: id || undefined } })
  fetchProducts()
}

function handleSortChange() {
  page.value = 1
  fetchProducts()
}

function handlePageChange(p) {
  page.value = p
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function goToDetail(id) {
  router.push(`/products/${id}`)
}

function parseQuery() {
  const qCategoryId = route.query.categoryId
  const qKeyword = route.query.keyword
  currentCategoryId.value = qCategoryId ? Number(qCategoryId) : null
  keyword.value = qKeyword || ''
}

watch(() => route.query, () => {
  parseQuery()
  page.value = 1
  fetchProducts()
})

onMounted(async () => {
  await fetchCategories()
  parseQuery()
  fetchProducts()
})
</script>

<style scoped>
.home { padding-bottom: 40px; }

.banner { margin-bottom: 24px; border-radius: 8px; overflow: hidden; }
.banner-slide { height: 280px; display: flex; align-items: center; justify-content: center; }
.banner-1 { background: linear-gradient(135deg, #1890ff 0%, #096dd9 50%, #0050b3 100%); }
.banner-text { text-align: center; color: #fff; }
.banner-text h2 { font-size: 36px; margin-bottom: 12px; }
.banner-text p { font-size: 18px; opacity: 0.85; }

.categories-bar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.cat-tag { cursor: pointer; font-size: 14px; }

.sort-bar { display: flex; align-items: center; margin-bottom: 20px; }
.sort-label { font-size: 14px; color: #666; margin-right: 8px; }

.loading-container { padding: 40px; }
.empty-container { padding: 60px 0; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
@media (max-width: 992px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .product-grid { grid-template-columns: 1fr; } }

.product-card { cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; }
.product-card:hover { transform: translateY(-4px); }
.product-img { width: 100%; height: 220px; overflow: hidden; background: #f5f5f5; }
.product-img img { width: 100%; height: 100%; object-fit: cover; }
.product-info { padding: 12px 16px 16px; }
.product-name { font-size: 14px; font-weight: normal; color: #333; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-meta { display: flex; justify-content: space-between; align-items: center; }
.product-price { color: #ff4d4f; font-size: 18px; font-weight: bold; }
.product-sales { color: #999; font-size: 12px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
