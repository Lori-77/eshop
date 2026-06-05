<template>
  <div class="admin-config">
    <h2 class="page-title">系统配置</h2>

    <el-card shadow="hover">
      <el-table :data="configs" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configKey" label="配置键" width="220" />
        <el-table-column prop="configValue" label="配置值" min-width="200">
          <template #default="{ row }">
            <template v-if="row.editing">
              <el-input v-model="row.editValue" size="small" style="width: 200px" />
              <el-button type="primary" size="small" @click="saveEdit(row)" style="margin-left: 8px">保存</el-button>
              <el-button size="small" @click="cancelEdit(row)">取消</el-button>
            </template>
            <span v-else>{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="startEdit(row)" v-if="!row.editing">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getConfigs, updateConfig } from '@/api'
import { ElMessage } from 'element-plus'

const configs = ref([])
const loading = ref(false)

async function fetchConfigs() {
  loading.value = true
  try {
    const res = await getConfigs()
    configs.value = (res.data || []).map(c => ({ ...c, editing: false, editValue: c.configValue }))
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  row.editing = true
  row.editValue = row.configValue
}

function cancelEdit(row) {
  row.editing = false
  row.editValue = row.configValue
}

async function saveEdit(row) {
  try {
    await updateConfig(row.id, row.editValue)
    row.configValue = row.editValue
    row.editing = false
    ElMessage.success('更新成功')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

onMounted(fetchConfigs)
</script>

<style scoped>
.admin-config { max-width: 1000px; }
.page-title { font-size: 20px; margin-bottom: 20px; color: #333; }
</style>
