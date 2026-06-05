<template>
  <div class="admin-categories">
    <h2 class="page-title">分类管理</h2>

    <!-- Toolbar -->
    <el-card shadow="hover" class="toolbar-card">
      <div class="toolbar">
        <span class="toolbar-hint">分类层级：一级分类 > 二级分类</span>
        <el-button type="primary" @click="openAddDialog(null)">
          <el-icon><Plus /></el-icon> 新增一级分类
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
    <el-empty v-else-if="!categories.length" description="暂无分类数据" />

    <!-- Category tree table -->
    <el-card v-else shadow="hover">
      <el-table :data="flattenedCategories" row-key="id" stripe style="width: 100%" default-expand-all>
        <el-table-column label="分类名称" min-width="250">
          <template #default="{ row }">
            <span :style="{ marginLeft: row.parentId ? '24px' : '0', fontWeight: row.parentId ? 'normal' : 'bold' }">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.parentId ? 'info' : 'primary'" size="small">
              {{ row.parentId ? '二级' : '一级' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="!row.parentId"
              text
              type="primary"
              @click="openAddDialog(row)"
            >
              新增子分类
            </el-button>
            <el-popconfirm
              title="确定删除该分类？子分类也将被删除。"
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
    </el-card>

    <!-- Add / Edit category dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑分类' : (parentForSub ? '新增子分类' : '新增一级分类')"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="上级分类" v-if="parentForSub">
          <el-input :value="parentForSub.name" disabled />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" :step="1" style="width: 150px" />
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
import { ref, computed, onMounted } from 'vue'
import { getCategories, saveCategory, updateCategory, deleteCategory } from '@/api'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(true)
const error = ref('')
const categories = ref([])

// Dialog
const dialogVisible = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const editingId = ref(null)
const parentForSub = ref(null)
const formRef = ref(null)

const defaultForm = {
  name: '',
  parentId: null,
  sortOrder: 0,
}
const form = ref({ ...defaultForm })

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

// Flatten the tree structure for table display
const flattenedCategories = computed(() => {
  const result = []
  function walk(items) {
    for (const item of items) {
      result.push(item)
      if (item.children && item.children.length) {
        walk(item.children)
      }
    }
  }
  walk(categories.value)
  return result
})

async function fetchCategories() {
  loading.value = true
  error.value = ''
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '获取分类列表失败'
  } finally {
    loading.value = false
  }
}

function openAddDialog(parent) {
  isEditing.value = false
  editingId.value = null
  parentForSub.value = parent
  form.value = {
    name: '',
    parentId: parent?.id || null,
    sortOrder: 0,
  }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEditing.value = true
  editingId.value = row.id
  parentForSub.value = row.parentId ? { id: row.parentId, name: row.parentName || '' } : null
  form.value = {
    name: row.name,
    parentId: row.parentId || null,
    sortOrder: row.sortOrder ?? 0,
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
      parentId: form.value.parentId || null,
      sortOrder: form.value.sortOrder,
    }

    if (isEditing.value) {
      payload.id = editingId.value
      await updateCategory(payload)
    } else {
      await saveCategory(payload)
    }

    dialogVisible.value = false
    fetchCategories()
  } catch (e) {
    console.error('Save category failed:', e)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await deleteCategory(row.id)
    fetchCategories()
  } catch (e) {
    console.error('Delete category failed:', e)
  }
}

onMounted(fetchCategories)
</script>

<style scoped>
.admin-categories {
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
.toolbar-hint {
  color: #999;
  font-size: 14px;
}
.loading-container {
  padding: 40px;
}
</style>
