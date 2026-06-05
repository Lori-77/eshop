<template>
  <div class="cart-page">
    <h2 class="page-title">购物车</h2>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="4" animated />
    </div>

    <template v-else-if="cartStore.items.length === 0">
      <el-empty description="购物车是空的" class="empty-cart">
        <template #extra>
          <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
        </template>
      </el-empty>
    </template>

    <template v-else>
      <!-- 购物车列表 -->
      <el-table
        :data="cartStore.items"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        ref="tableRef"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                :src="row.productImage || '/placeholder.png'"
                fit="cover"
                class="cart-product-img"
              />
              <div class="cart-product-info">
                <span class="cart-product-name">{{ row.productName || row.name }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ (row.price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="数量" width="200" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock || 99"
              size="small"
              @change="(val) => handleQuantityChange(row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="130" align="center">
          <template #default="{ row }">
            <span class="price subtotal">¥{{ ((row.price || 0) * row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部操作栏 -->
      <div class="cart-footer">
        <div class="cart-footer-left">
          <el-checkbox v-model="selectAll" :indeterminate="indeterminate" @change="handleSelectAll">
            全选
          </el-checkbox>
          <el-button type="danger" link @click="handleClear">清空购物车</el-button>
        </div>
        <div class="cart-footer-right">
          <div class="cart-total">
            已选 <span class="total-count">{{ selectedCount }}</span> 件商品
          </div>
          <div class="cart-sum">
            合计：<span class="total-price">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            :disabled="selectedCount === 0"
            @click="handleCheckout"
          >
            结算
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const tableRef = ref(null)
const selectAll = ref(false)
const indeterminate = ref(false)

const selectedCount = computed(() => cartStore.totalCount)
const selectedItems = computed(() => cartStore.items.filter(i => i.selected))

function handleSelectionChange(selection) {
  const len = selection.length
  const total = cartStore.items.length
  selectAll.value = len === total
  indeterminate.value = len > 0 && len < total
  // Update selected state in store
  selection.forEach(item => {
    if (!item.selected) {
      cartStore.toggleSelected(item.cartItemId || item.id, true)
    }
  })
  cartStore.items.forEach(item => {
    if (!selection.find(s => (s.cartItemId || s.id) === (item.cartItemId || item.id))) {
      if (item.selected) {
        cartStore.toggleSelected(item.cartItemId || item.id, false)
      }
    }
  })
}

async function handleSelectAll(val) {
  cartStore.items.forEach(item => {
    cartStore.toggleSelected(item.cartItemId || item.id, val)
  })
}

async function handleQuantityChange(row, val) {
  try {
    await cartStore.updateQty(row.cartItemId || row.id, val)
  } catch (e) {
    ElMessage.error('修改数量失败')
  }
}

async function handleRemove(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await cartStore.remove(row.cartItemId || row.id)
    ElMessage.success('已删除')
  } catch {
    // cancelled
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await cartStore.clear()
    ElMessage.success('购物车已清空')
  } catch {
    // cancelled
  }
}

function handleCheckout() {
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  router.push('/checkout')
}

onMounted(async () => {
  loading.value = true
  await cartStore.fetchCart()
  loading.value = false
})
</script>

<style scoped>
.cart-page { padding-bottom: 40px; }
.page-title { font-size: 22px; color: #333; margin: 0 0 24px; }
.loading-wrap { padding: 40px; }
.empty-cart { padding: 80px 0; }

.product-cell { display: flex; align-items: center; gap: 12px; }
.cart-product-img { width: 80px; height: 80px; border-radius: 4px; flex-shrink: 0; background: #f5f5f5; }
.cart-product-info { flex: 1; }
.cart-product-name { font-size: 14px; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.price { color: #ff4d4f; font-weight: bold; font-size: 14px; }
.subtotal { font-size: 16px; }

.cart-footer { display: flex; justify-content: space-between; align-items: center; padding: 20px 0; margin-top: 20px; border-top: 1px solid #eee; background: #fff; }
.cart-footer-left { display: flex; align-items: center; gap: 16px; }
.cart-footer-right { display: flex; align-items: center; gap: 20px; }
.cart-total { font-size: 14px; color: #666; }
.total-count { color: #ff4d4f; font-weight: bold; font-size: 18px; }
.cart-sum { font-size: 14px; color: #333; }
.total-price { color: #ff4d4f; font-size: 24px; font-weight: bold; }

@media (max-width: 768px) {
  .cart-footer { flex-direction: column; gap: 16px; }
  .cart-footer-right { flex-wrap: wrap; justify-content: center; }
}
</style>
