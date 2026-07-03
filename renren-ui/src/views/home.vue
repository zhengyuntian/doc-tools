<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h2>欢迎使用暗标检测系统</h2>
      <p>实时监控检测任务，高效管理检测流程</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card" @click="goTo('/demo/darkdetectbatch')">
        <div class="stat-icon batch-icon">
          <el-icon><FolderOpened /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalBatch || 0 }}</div>
          <div class="stat-label">检测批次</div>
        </div>
      </div>

      <div class="stat-card" @click="goTo('/demo/darkdetecttask')">
        <div class="stat-icon task-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalTask || 0 }}</div>
          <div class="stat-label">文件任务</div>
        </div>
      </div>

      <div class="stat-card" @click="goTo('/demo/sensitive')">
        <div class="stat-icon word-icon">
          <el-icon><Search /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalWord || 0 }}</div>
          <div class="stat-label">敏感词</div>
        </div>
      </div>

      <div class="stat-card" @click="goTo('/demo/darkruleconfig')">
        <div class="stat-icon rule-icon">
          <el-icon><Setting /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalRule || 0 }}</div>
          <div class="stat-label">检测规则</div>
        </div>
      </div>

      <div class="stat-card" @click="goTo('/demo/darkrulescheme')">
        <div class="stat-icon scheme-icon">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.totalScheme || 0 }}</div>
          <div class="stat-label">规则方案</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon trend-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ batchStatus.completed || 0 }}/{{ batchStatus.processing || 0 }}/{{ batchStatus.failed || 0 }}</div>
          <div class="stat-label">完成/处理/失败</div>
        </div>
      </div>
    </div>

    <div class="content-row">
      <div class="quick-actions">
        <h3>快捷操作</h3>
        <div class="action-grid">
          <div class="action-item" @click="goTo('/demo/darkdetectbatch')">
            <el-icon class="action-icon"><Plus /></el-icon>
            <span>新建检测批次</span>
          </div>
          <div class="action-item" @click="goTo('/demo/sensitive')">
            <el-icon class="action-icon"><Plus /></el-icon>
            <span>新增敏感词</span>
          </div>
          <div class="action-item" @click="goTo('/demo/darkruleconfig')">
            <el-icon class="action-icon"><Plus /></el-icon>
            <span>新增检测规则</span>
          </div>
          <div class="action-item" @click="goTo('/demo/darkrulescheme')">
            <el-icon class="action-icon"><Plus /></el-icon>
            <span>新增规则方案</span>
          </div>
        </div>
      </div>

      <div class="status-chart">
        <h3>批次状态分布</h3>
        <div class="chart-container">
          <div class="progress-ring">
            <svg viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="40" fill="none" stroke="#f0f0f0" stroke-width="8"></circle>
              <circle cx="50" cy="50" r="40" fill="none" stroke="#67c23a" stroke-width="8" :stroke-dasharray="getCompletedDash()" stroke-dashoffset="0" transform="rotate(-90 50 50)"></circle>
              <circle cx="50" cy="50" r="40" fill="none" stroke="#409eff" stroke-width="8" :stroke-dasharray="getProcessingDash()" :stroke-dashoffset="-getCompletedOffset()" transform="rotate(-90 50 50)"></circle>
              <circle cx="50" cy="50" r="40" fill="none" stroke="#f56c6c" stroke-width="8" :stroke-dasharray="getFailedDash()" :stroke-dashoffset="-getFailedOffset()" transform="rotate(-90 50 50)"></circle>
            </svg>
            <div class="ring-center">
              <span class="ring-total">{{ totalBatches }}</span>
              <span class="ring-label">总批次</span>
            </div>
          </div>
          <div class="legend">
            <div class="legend-item">
              <span class="legend-color completed"></span>
              <span>已完成: {{ batchStatus.completed || 0 }}</span>
            </div>
            <div class="legend-item">
              <span class="legend-color processing"></span>
              <span>处理中: {{ batchStatus.processing || 0 }}</span>
            </div>
            <div class="legend-item">
              <span class="legend-color failed"></span>
              <span>失败: {{ batchStatus.failed || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="recent-section">
      <h3>最近检测批次</h3>
      <el-table :data="recentBatches" border stripe style="width: 100%">
        <el-table-column prop="batchNo" label="批次编号" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="batchName" label="批次名称" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="fileCount" label="文件数量" width="100" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === 0" type="info">处理中</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">已完成</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="danger">部分失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180"></el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template v-slot="scope">
            <el-button size="small" @click="goTo('/demo/darkdetectbatch')">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import baseService from '@/service/baseService'
import { 
  FolderOpened, Document, Search, Setting, Grid, 
  TrendCharts, Plus 
} from '@element-plus/icons-vue'

const router = useRouter()
const statistics = ref<Record<string, number>>({})
const batchStatus = ref<Record<string, number>>({})
const recentBatches = ref<any[]>([])

const totalBatches = computed(() => {
  return Number(batchStatus.value.processing || 0) + 
         Number(batchStatus.value.completed || 0) + 
         Number(batchStatus.value.failed || 0)
})

const getCompletedDash = () => {
  if (totalBatches.value === 0) return 0
  return (batchStatus.value.completed || 0) / totalBatches.value * 251.2
}

const getProcessingDash = () => {
  if (totalBatches.value === 0) return 0
  return (batchStatus.value.processing || 0) / totalBatches.value * 251.2
}

const getFailedDash = () => {
  if (totalBatches.value === 0) return 0
  return (batchStatus.value.failed || 0) / totalBatches.value * 251.2
}

const getCompletedOffset = () => {
  return getCompletedDash()
}

const getFailedOffset = () => {
  return getCompletedDash() + getProcessingDash()
}

const goTo = (path: string) => {
  router.push({ path })
}

const loadStatistics = async () => {
  try {
    const res = await baseService.get('demo/statistics/overview')
    statistics.value = res.data || {}
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

const loadBatchStatus = async () => {
  try {
    const res = await baseService.get('demo/statistics/batchStatus')
    batchStatus.value = res.data || {}
  } catch (e) {
    console.error('加载批次状态失败', e)
  }
}

const loadRecentBatches = async () => {
  try {
    const res = await baseService.get('demo/darkdetectbatch/page', {
      page: 1,
      limit: 5,
      orderField: 'create_time',
      order: 'desc'
    })
    recentBatches.value = res.data?.list || []
  } catch (e) {
    console.error('加载最近批次失败', e)
  }
}

onMounted(() => {
  loadStatistics()
  loadBatchStatus()
  loadRecentBatches()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.dashboard-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.batch-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.task-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.word-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.rule-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}

.scheme-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
}

.trend-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #303133;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.content-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.quick-actions {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.quick-actions h3,
.status-chart h3,
.recent-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: #ecf5ff;
}

.action-icon {
  font-size: 18px;
  color: #409eff;
  margin-right: 10px;
}

.action-item span {
  font-size: 14px;
  color: #303133;
}

.status-chart {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
}

.progress-ring {
  position: relative;
  width: 160px;
  height: 160px;
}

.progress-ring svg {
  width: 100%;
  height: 100%;
}

.ring-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.ring-total {
  display: block;
  font-size: 32px;
  font-weight: 600;
  color: #303133;
}

.ring-label {
  display: block;
  font-size: 12px;
  color: #909399;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-color.completed {
  background: #67c23a;
}

.legend-color.processing {
  background: #409eff;
}

.legend-color.failed {
  background: #f56c6c;
}

.recent-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .content-row {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
