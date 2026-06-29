<template>
  <div class="mod-demo__darkdetecttask">
    <div style="margin-bottom: 15px;">
      <el-button @click="goBack()">返回批次列表</el-button>
    </div>
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item label="批次名称">
        <el-input v-model="state.dataForm.batchName" placeholder="请输入批次名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="文件名">
        <el-input v-model="state.dataForm.fileName" placeholder="请输入文件名" clearable></el-input>
      </el-form-item>
      <el-form-item label="文件类型">
        <el-select v-model="state.dataForm.fileType" placeholder="全部" clearable>
          <el-option label="DOCX" :value="1"></el-option>
          <el-option label="PDF" :value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="state.dataForm.status" placeholder="全部" clearable>
          <el-option label="排队中" :value="0"></el-option>
          <el-option label="检测中" :value="1"></el-option>
          <el-option label="已完成" :value="2"></el-option>
          <el-option label="失败" :value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="检测类型">
        <el-select v-model="state.dataForm.detectType" placeholder="全部" clearable>
          <el-option label="方案检测" :value="1"></el-option>
          <el-option label="敏感词检测" :value="2"></el-option>
          <el-option label="方案+敏感词检测" :value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="state.getDataList()">查询</el-button>
        <el-button @click="resetForm()">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" style="margin-bottom: 15px;">
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetecttask:delete')" type="danger" @click="state.deleteHandle()">删除</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="batchName" label="批次名称" header-align="center" align="center" min-width="150"></el-table-column>
      <el-table-column label="检测类型" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-tag v-if="scope.row.schemeEnabled === 1 && scope.row.sensitiveEnabled === 0" type="info">方案检测</el-tag>
          <el-tag v-else-if="scope.row.schemeEnabled === 0 && scope.row.sensitiveEnabled === 1" type="warning">敏感词检测</el-tag>
          <el-tag v-else-if="scope.row.schemeEnabled === 1 && scope.row.sensitiveEnabled === 1" type="success">方案+敏感词</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileName" label="文件名" header-align="center" align="center" min-width="250">
        <template v-slot="scope">
          <span class="file-name">
            <el-icon v-if="scope.row.fileType === 1" class="file-icon docx"><Document /></el-icon>
            <el-icon v-else-if="scope.row.fileType === 2" class="file-icon pdf"><Document /></el-icon>
            {{ scope.row.fileName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="类型" header-align="center" align="center" width="80">
        <template v-slot="scope">
          <el-tag size="small" type="info">{{ scope.row.fileType === 1 ? 'DOCX' : 'PDF' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="大小" header-align="center" align="center" width="100">
        <template v-slot="scope">
          {{ formatSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center" width="180">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" type="info">排队中</el-tag>
          <div v-else-if="scope.row.status === 1" class="progress-cell">
            <el-tag type="warning">检测中</el-tag>
            <div class="progress-wrapper" v-if="scope.row.currentRuleIndex && scope.row.totalRules">
              <div class="progress-bar">
                <div class="progress-fill" :style="{width: (scope.row.currentRuleIndex / scope.row.totalRules * 100) + '%'}"></div>
              </div>
              <span class="progress-text">{{ scope.row.currentRuleIndex }}/{{ scope.row.totalRules }}</span>
            </div>
          </div>
          <el-tag v-else-if="scope.row.status === 2" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalRules" label="检测通过比例" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <span v-if="scope.row.totalRules && scope.row.totalRules > 0" class="pass-rate">
            {{ (scope.row.passRules || 0) }}/{{ scope.row.totalRules }}
          </span>
          <span v-else class="no-progress">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="failRules" label="违规数" header-align="center" align="center" width="80">
        <template v-slot="scope">
          <span v-if="scope.row.failRules && scope.row.failRules > 0" class="fail-count">{{ scope.row.failRules }}</span>
          <span v-else>0</span>
        </template>
      </el-table-column>
      <el-table-column prop="creatorName" label="操作人" header-align="center" align="center" width="100"></el-table-column>
      <el-table-column prop="updateTime" label="检测时间" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="280">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('demo:darkdetecttask:info')" type="primary" link @click="downloadHandle(scope.row)">下载</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetecttask:info')" type="primary" link @click="detailHandle(scope.row)">详情</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetecttask:update') && scope.row.status === 0" type="success" link @click="startDetectHandle(scope.row)">启动检测</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetecttask:update') && scope.row.status === 1" type="warning" link disabled>检测中</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetecttask:update') && scope.row.status === 3" type="success" link @click="retryHandle(scope.row)">重试</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetecttask:update') && scope.row.status === 2" type="warning" link @click="retryHandle(scope.row)">再次检测</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetecttask:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>

    <el-dialog v-model="detailVisible" title="任务详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号">{{ detailData.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ detailData.fileName }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">{{ detailData.fileType === 1 ? 'DOCX' : 'PDF' }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatSize(detailData.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="检测类型">
          <el-tag v-if="detailData.schemeEnabled === 1 && detailData.sensitiveEnabled === 0" type="info">方案检测</el-tag>
          <el-tag v-else-if="detailData.schemeEnabled === 0 && detailData.sensitiveEnabled === 1" type="warning">敏感词检测</el-tag>
          <el-tag v-else-if="detailData.schemeEnabled === 1 && detailData.sensitiveEnabled === 1" type="success">方案+敏感词</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 0" type="info">排队中</el-tag>
          <el-tag v-else-if="detailData.status === 1" type="warning">检测中</el-tag>
          <el-tag v-else-if="detailData.status === 2" type="success">已完成</el-tag>
          <el-tag v-else-if="detailData.status === 3" type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="检测通过比例">{{ detailData.totalRules && detailData.totalRules > 0 ? (detailData.passRules || 0) + '/' + detailData.totalRules : '-' }}</el-descriptions-item>
        <el-descriptions-item label="违规数">{{ detailData.failRules || 0 }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2">{{ detailData.errorMsg || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ detailData.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="detailData.resultSummary" style="margin-top: 20px;">
        <el-collapse>
          <el-collapse-item title="检测结果摘要">
            <pre style="white-space: pre-wrap; word-break: break-all;">{{ formatResultSummary(detailData.resultSummary) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs, onMounted, onUnmounted } from "vue";
import { Document } from "@element-plus/icons-vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/demo/darkdetecttask/page",
  getDataListIsPage: true,
  exportURL: "/demo/darkdetecttask/export",
  deleteURL: "/demo/darkdetecttask"
});

const state = reactive({ 
  ...useView(view), 
  ...toRefs(view),
  dataForm: {
    batchName: '',
    fileName: '',
    fileType: null as number | null,
    status: null as number | null,
    detectType: null as number | null,
    batchId: null as number | null
  }
});

const detailVisible = ref(false);
const detailData = reactive<any>({});

const downloadHandle = (row: any) => {
  baseService.download('/demo/darkdetecttask/' + row.id + '/download', row.fileName);
};

const detailHandle = async (row: any) => {
  try {
    const res = await baseService.get('/demo/darkdetecttask/' + row.id + '/detail');
    Object.keys(detailData).forEach(key => delete (detailData as any)[key]);
    Object.assign(detailData, res.data);
    detailVisible.value = true;
  } catch (e) {
    ElMessage.error('获取详情失败');
  }
};

const retryHandle = (row: any) => {
  baseService.post('/demo/darkdetecttask/' + row.id + '/retry').then(() => {
    ElMessage.success('重试成功');
    state.getDataList();
  });
};

const startDetectHandle = (row: any) => {
  baseService.post('/demo/darkdetecttask/' + row.id + '/start').then(() => {
    ElMessage.success('启动检测成功');
    state.getDataList();
  });
};

const formatSize = (size: number) => {
  if (!size) return '-';
  if (size < 1024) {
    return size + ' B';
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(1) + ' KB';
  } else {
    return (size / (1024 * 1024)).toFixed(1) + ' MB';
  }
};

const formatResultSummary = (summary: string) => {
  try {
    const json = JSON.parse(summary);
    if (json.violations && Array.isArray(json.violations)) {
      return json.violations.map((v: string, i: number) => `${i + 1}. ${v}`).join('\n');
    }
    return JSON.stringify(json, null, 2);
  } catch {
    return summary;
  }
};

const resetForm = () => {
  state.dataForm.batchName = '';
  state.dataForm.fileName = '';
  state.dataForm.fileType = null;
  state.dataForm.status = null;
  state.dataForm.detectType = null;
  state.getDataList();
};

const goBack = () => {
  window.location.href = '#/demo/darkdetectbatch';
};

onMounted(() => {
  const params = new URLSearchParams(window.location.hash.substring(1));
  const batchIdParam = params.get('batchId');
  if (batchIdParam) {
    state.dataForm.batchId = parseInt(batchIdParam);
  }
  state.getDataList();
  
  // 启动自动刷新（当有检测中的任务时每5秒刷新）
  startAutoRefresh();
});

// 自动刷新定时器
let refreshTimer: number | null = null;

const startAutoRefresh = () => {
  refreshTimer = window.setInterval(() => {
    // 检查是否有检测中的任务
    const dataList = state.dataList || [];
    const hasDetecting = dataList.some((item: any) => item.status === 1);
    if (hasDetecting) {
      state.getDataList();
    }
  }, 5000);
};

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
    refreshTimer = null;
  }
});
</script>

<style scoped>
.file-name {
  display: flex;
  align-items: center;
}

.file-icon {
  margin-right: 8px;
}

.file-icon.docx {
  color: #2b579a;
}

.file-icon.pdf {
  color: #d32f2f;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  margin-top: 4px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background-color: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 8px;
}

.progress-fill {
  height: 100%;
  background-color: #409eff;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 11px;
  color: #606266;
  min-width: 50px;
  text-align: right;
}

.no-progress {
  color: #909399;
}

.fail-count {
  color: #f56c6c;
  font-weight: bold;
}
</style>
