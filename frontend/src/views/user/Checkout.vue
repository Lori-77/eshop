<template>
  <div class="checkout-page">
    <h2 class="page-title">确认订单</h2>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else>
      <!-- 收货地址 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">收货地址</span>
        </template>
        <div v-if="addresses.length === 0" class="no-address">
          <p>请添加收货地址</p>
          <el-button type="primary" size="small" @click="showAddressDialog = true">
            添加地址
          </el-button>
        </div>
        <div v-else class="address-list">
          <div
            v-for="addr in addresses"
            :key="addr.id"
            class="address-item"
            :class="{ active: selectedAddressId === addr.id }"
            @click="selectedAddressId = addr.id"
          >
            <div class="address-info">
              <span class="addr-name">{{ addr.receiverName }}</span>
              <span class="addr-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" size="small" type="primary" class="default-tag">默认</el-tag>
            </div>
            <div class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
          </div>
          <el-button type="primary" link size="small" @click="showAddressDialog = true" class="add-addr-btn">
            + 使用新地址
          </el-button>
        </div>
      </el-card>

      <!-- 商品信息 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">商品信息</span>
        </template>
        <el-table :data="orderItems" style="width: 100%">
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="order-product-cell">
                <el-image
                  :src="row.productImage || '/placeholder.png'"
                  fit="cover"
                  class="order-product-img"
                />
                <span>{{ row.productName || row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              ¥{{ (row.price || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center">
            <template #default="{ row }">
              x{{ row.quantity }}
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="price">¥{{ ((row.price || 0) * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 备注 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <span class="section-title">订单备注</span>
        </template>
        <el-input
          v-model="remark"
          type="textarea"
          :rows="3"
          placeholder="选填：如有特殊要求请在此备注"
          maxlength="200"
          show-word-limit
        />
      </el-card>

      <!-- 底部 -->
      <div class="checkout-footer">
        <div class="footer-sum">
          合计：<span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" :loading="submitting" @click="handleSubmitOrder">
          提交订单
        </el-button>
      </div>
    </template>

    <!-- 地址对话框 -->
    <el-dialog
      v-model="showAddressDialog"
      title="新增收货地址"
      width="520px"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" prop="province">
          <el-input v-model="addressForm.province" placeholder="如：广东省广州市天河区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress" :loading="savingAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { getAddresses, saveAddress, submitOrder } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const remark = ref('')
const orderItems = ref([])

const showAddressDialog = ref(false)
const savingAddress = ref(false)
const addressFormRef = ref(null)

const addressForm = ref({
  receiverName: '',
  phone: '',
  province: '',
  detail: '',
  isDefault: false
})

const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入所在地区', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0)
})

async function loadAddresses() {
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
    const defaultAddr = addresses.value.find(a => a.isDefault)
    if (defaultAddr) selectedAddressId.value = defaultAddr.id
    else if (addresses.value.length > 0) selectedAddressId.value = addresses.value[0].id
  } catch (e) {
    console.error('Failed to load addresses:', e)
  }
}

async function handleSaveAddress() {
  const valid = await addressFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingAddress.value = true
  try {
    const res = await saveAddress({
      receiverName: addressForm.value.receiverName,
      phone: addressForm.value.phone,
      province: addressForm.value.province,
      city: '',
      district: '',
      detail: addressForm.value.detail,
      isDefault: addressForm.value.isDefault ? 1 : 0
    })
    ElMessage.success('地址保存成功')
    showAddressDialog.value = false
    await loadAddresses()
    if (res.data?.id) selectedAddressId.value = res.data.id
    // Reset form
    addressForm.value = { receiverName: '', phone: '', province: '', detail: '', isDefault: false }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    savingAddress.value = false
  }
}

async function handleSubmitOrder() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddressId.value,
      remark: remark.value,
      items: orderItems.value.map(item => ({
        productId: item.productId || item.id,
        quantity: item.quantity,
        price: item.price
      }))
    }
    const res = await submitOrder(orderData)
    ElMessage.success('订单提交成功')
    // Clear selected items from cart
    await cartStore.fetchCart()
    const orderId = res.data?.id || res.data?.orderId
    if (orderId) {
      router.push(`/orders/${orderId}`)
    } else {
      router.push('/orders')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交订单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([
    cartStore.fetchCart(),
    loadAddresses()
  ])
  // Get selected items from cart
  const selectedItems = cartStore.items.filter(i => i.selected)
  if (selectedItems.length > 0) {
    orderItems.value = selectedItems.map(item => ({
      ...item,
      productId: item.productId || item.id,
      productName: item.productName || item.name
    }))
  }
  loading.value = false

  if (orderItems.value.length === 0) {
    ElMessage.warning('请先在购物车中选择商品')
    router.push('/cart')
  }
})
</script>

<style scoped>
.checkout-page { padding-bottom: 40px; }
.page-title { font-size: 22px; color: #333; margin: 0 0 24px; }
.loading-wrap { padding: 40px; }

.section-card { margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: bold; }

.no-address { padding: 20px; text-align: center; color: #999; }

.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-item { border: 2px solid #eee; border-radius: 6px; padding: 16px; cursor: pointer; transition: border-color 0.2s; }
.address-item:hover { border-color: #1890ff; }
.address-item.active { border-color: #1890ff; background: #e6f7ff; }
.address-info { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.addr-name { font-size: 15px; font-weight: bold; color: #333; }
.addr-phone { font-size: 14px; color: #666; }
.default-tag { flex-shrink: 0; }
.address-detail { font-size: 13px; color: #999; }
.add-addr-btn { margin-top: 8px; }

.order-product-cell { display: flex; align-items: center; gap: 12px; }
.order-product-img { width: 60px; height: 60px; border-radius: 4px; flex-shrink: 0; background: #f5f5f5; }

.price { color: #ff4d4f; font-weight: bold; font-size: 15px; }

.checkout-footer { display: flex; justify-content: flex-end; align-items: center; gap: 24px; padding: 20px 0; border-top: 1px solid #eee; background: #fff; position: sticky; bottom: 0; }
.footer-sum { font-size: 16px; color: #333; }
.total-price { color: #ff4d4f; font-size: 28px; font-weight: bold; }
</style>
