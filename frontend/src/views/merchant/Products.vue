<template>
  <div class="merchant-products">
    <h2 class="page-title">商品管理</h2>

    <!-- Search & actions bar -->
    <el-card shadow="hover" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称..."
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon> 新增商品
        </el-button>
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
    <el-empty v-else-if="!products.length" description="暂无商品数据" />

    <!-- Product table -->
    <el-card v-else shadow="hover">
      <el-table :data="products" stripe style="width: 100%">
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.mainImage"
              style="width: 60px; height: 60px"
              fit="cover"
              :preview-src-list="[row.mainImage]"
            >
              <template #error>
                <div class="image-placeholder">暂无图片</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span>¥{{ row.price?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'ON_SALE'"
              active-text="上架"
              inactive-text="下架"
              @change="(val) => toggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该商品吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button text type="danger">删除</el-button>
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
          @current-change="fetchProducts"
        />
      </div>
    </el-card>

    <!-- Add / Edit product dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑商品' : '新增商品'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="right"
        status-icon
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :step="1"
            style="width: 200px"
            placeholder="请输入价格"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            :step="1"
            style="width: 200px"
            placeholder="请输入库存数量"
          />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-input v-model="form.mainImage" placeholder="输入图片路径，如 /uploads/xxx.jpg" />
          <div style="font-size: 12px; color: #999; margin-top: 4px">
            把图片放到 backend/uploads/ 目录下，然后填写路径如 /uploads/图片名.jpg
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProducts, saveProduct, updateProduct, deleteProduct, updateProductStatus, getCategories } from '@/api'
import { Search, Plus } from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref('')
const products = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// Dialog
const dialogVisible = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const defaultForm = {
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  description: '',
  mainImage: '',
}

const form = ref({ ...defaultForm })

const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

async function fetchProducts() {
  loading.value = true
  error.value = ''
  try {
    const res = await getProducts({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
    })
    const data = res.data
    products.value = data.records || data.list || data || []
    total.value = data.total || products.value.length
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取商品列表失败'
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch {
    // silent
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchProducts()
}

async function toggleStatus(row, val) {
  const newStatus = val ? 'ON_SALE' : 'OFF_SHELF'
  try {
    await updateProductStatus(row.id, newStatus)
    row.status = newStatus
  } catch (e) {
    row.status = row.status
  }
}

function openAddDialog() {
  isEditing.value = false
  editingId.value = null
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEditing.value = true
  editingId.value = row.id
  form.value = {
    name: row.name,
    categoryId: row.categoryId || row.category?.id,
    price: row.price,
    stock: row.stock,
    description: row.description || '',
    mainImage: row.mainImage || '',
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      categoryId: form.value.categoryId,
      price: form.value.price,
      stock: form.value.stock,
      description: form.value.description,
      mainImage: form.value.mainImage,
    }

    if (isEditing.value) {
      payload.id = editingId.value
      await updateProduct(payload)
    } else {
      await saveProduct(payload)
    }

    dialogVisible.value = false
    fetchProducts()
  } catch (e) {
    console.error('Save failed:', e)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await deleteProduct(row.id)
    fetchProducts()
  } catch (e) {
    console.error('Delete failed:', e)
  }
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.merchant-products {
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
.image-placeholder {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 12px;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
