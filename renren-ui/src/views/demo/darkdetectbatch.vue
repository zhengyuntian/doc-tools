<template>
  <div class="mod-demo__darkdetectbatch">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon blue">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.total || 0 }}</div>
            <div class="stat-label">总批次</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.processing || 0 }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon green">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.completed || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon red">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.failed || 0 }}</div>
            <div class="stat-label">部分失败</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item label="批次名称">
        <el-input v-model="state.dataForm.batchName" placeholder="请输入批次名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="state.dataForm.status" placeholder="全部" clearable>
          <el-option label="处理中" :value="0"></el-option>
          <el-option label="全部完成" :value="1"></el-option>
          <el-option label="部分失败" :value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="检测类型">
        <el-select v-model="state.dataForm.detectType" placeholder="全部" clearable>
          <el-option label="方案检测" :value="1"></el-option>
          <el-option label="敏感词检测" :value="2"></el-option>
          <el-option label="方案+敏感词检测" :value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="state.dataForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="state.getDataList()">查询</el-button>
        <el-button @click="resetForm()">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" style="margin-bottom: 15px;">
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetectbatch:save')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetectbatch:delete')" type="danger" @click="state.deleteHandle()">删除</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" @expand-change="handleExpandChange" style="width: 100%">
      <el-table-column type="expand" width="50">
        <template v-slot="scope">
          <div v-if="taskErrorMap.has(scope.row.id)" style="text-align: center; padding: 20px;">
            <el-icon :size="24" style="color: #f56c6c;"><CircleClose /></el-icon>
            <span style="margin-left: 8px; color: #f56c6c;">{{ taskErrorMap.get(scope.row.id) }}</span>
            <el-button type="text" size="small" style="margin-left: 10px;" @click="loadBatchTasks(scope.row.id)">重新加载</el-button>
          </div>
          <div v-else-if="taskDataMap.has(scope.row.id)">
            <el-table :data="taskDataMap.get(scope.row.id) || []" border style="width: 100%">
              <el-table-column prop="fileName" label="文件名" header-align="center" align="center" width="150" show-overflow-tooltip></el-table-column>
              <el-table-column prop="fileType" label="文件类型" header-align="center" align="center" width="100">
                <template v-slot="scope">
                  <el-tag v-if="scope.row.fileType === 1" type="info">DOCX</el-tag>
                  <el-tag v-else type="warning">PDF</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" header-align="center" align="center" width="120">
                <template v-slot="scope">
                  <el-tag v-if="scope.row.status === 0" type="info">排队中</el-tag>
                  <el-tag v-else-if="scope.row.status === 1" type="warning">检测中</el-tag>
                  <el-tag v-else-if="scope.row.status === 2" type="success">已完成</el-tag>
                  <el-tag v-else-if="scope.row.status === 3" type="danger">失败</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="totalRules" label="检测进度" header-align="center" align="center" width="150">
                <template v-slot="scope">
                  <div v-if="scope.row.status === 1" style="padding: 5px;">
                    <el-progress :percentage="scope.row.totalRules ? Math.round(((scope.row.passRules || 0) + (scope.row.failRules || 0)) / scope.row.totalRules * 100) : 0" :stroke-width="12"></el-progress>
                  </div>
                  <span v-else-if="scope.row.totalRules && scope.row.totalRules > 0">{{ (scope.row.passRules || 0) }}/{{ scope.row.totalRules }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="failRules" label="违规数" header-align="center" align="center" width="80">
                <template v-slot="scope">
                  <span v-if="scope.row.failRules && scope.row.failRules > 0" style="color: #f56c6c; font-weight: bold;">{{ scope.row.failRules }}</span>
                  <span v-else>0</span>
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" label="检测时间" header-align="center" align="center" width="180"></el-table-column>
            </el-table>
          </div>
          <div v-else style="text-align: center; padding: 20px;">
            <el-icon :size="24" style="color: #909399;"><Loading /></el-icon>
            <span style="margin-left: 8px; color: #909399;">加载中...</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="batchNo" label="批次编号" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column prop="batchName" label="批次名称" header-align="center" align="center" min-width="200"></el-table-column>
      <el-table-column prop="schemeName" label="检测方案" header-align="center" align="center" min-width="150"></el-table-column>
      <el-table-column label="检测类型" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-tag v-if="scope.row.schemeEnabled === 1 && scope.row.sensitiveEnabled === 0" type="info">方案检测</el-tag>
          <el-tag v-else-if="scope.row.schemeEnabled === 0 && scope.row.sensitiveEnabled === 1" type="warning">敏感词检测</el-tag>
          <el-tag v-else-if="scope.row.schemeEnabled === 1 && scope.row.sensitiveEnabled === 1" type="success">方案+敏感词</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalFiles" label="文件数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="passFiles" label="通过" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="failFiles" label="违规" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="errorFiles" label="失败" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center" width="120">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" type="info">待检测</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="warning">检测中</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">全部完成</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">部分失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creatorName" label="创建人" header-align="center" align="center" width="100"></el-table-column>
      <el-table-column prop="createTime" label="创建时间" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="350">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('demo:darkdetectbatch:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetectbatch:page')" type="primary" link @click="goToTaskList(scope.row.id)">文件任务列表</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetectbatch:page') && scope.row.isRelated === 1" type="primary" link @click="goToCrossResult(scope.row.id)">关联结果分析</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetectbatch:upload')" type="primary" link @click="uploadHandle(scope.row)">上传文件</el-button>
          <el-button 
            v-if="state.hasPermission('demo:darkdetectbatch:detect')" 
            type="success" 
            link 
            :disabled="scope.row.status === 3"
            @click="detectHandle(scope.row)">
            {{ scope.row.status === 3 ? '检测中' : (scope.row.totalFiles > 0 ? '启动检测' : '启动检测') }}
          </el-button>
          <el-button v-if="state.hasPermission('demo:darkdetectbatch:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>

    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList">确定</add-or-update>

    <upload-dialog ref="uploadDialogRef" @refreshDataList="state.getDataList"></upload-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs, onMounted, onUnmounted } from "vue";
import AddOrUpdate from "./darkdetectbatch-add-or-update.vue";
import UploadDialog from "./darkdetectbatch-upload.vue";
import baseService from "@/service/baseService";
import { Document, Loading, CircleCheck, CircleClose } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/demo/darkdetectbatch/page",
  getDataListIsPage: true,
  deleteURL: "/demo/darkdetectbatch"
});

const state = reactive({ 
  ...useView(view), 
  ...toRefs(view),
  dataForm: {
    batchName: '',
    status: null as number | null,
    detectType: null as number | null,
    dateRange: [] as string[]
  }
});

// 覆盖 getDataList 方法，处理 dateRange 转换
state.getDataList = () => {
  state.page = 1;
  state.dataListLoading = true;
  const params: any = {
    order: state.order,
    orderField: state.orderField,
    page: state.getDataListIsPage ? state.page : null,
    limit: state.getDataListIsPage ? state.limit : null,
    batchName: state.dataForm.batchName,
    status: state.dataForm.status
  };
  // 处理日期范围
  if (state.dataForm.dateRange && state.dataForm.dateRange.length === 2) {
    params.startTime = state.dataForm.dateRange[0];
    params.endTime = state.dataForm.dateRange[1];
  }
  baseService.get(state.getDataListURL, params).then((res: any) => {
    state.dataListLoading = false;
    state.dataList = state.getDataListIsPage ? res.data.list : res.data;
    state.total = state.getDataListIsPage ? res.data.total : 0;
  }).catch(() => {
    state.dataListLoading = false;
  });
};

const statistics = reactive({
  total: 0,
  processing: 0,
  completed: 0,
  failed: 0
});

const taskDataMap = reactive(new Map<number, any[]>());
const taskErrorMap = reactive(new Map<number, string>());

const loadBatchTasks = (batchId: number) => {
  if (taskDataMap.has(batchId)) {
    return;
  }
  taskErrorMap.delete(batchId);
  baseService.get('/demo/darkdetecttask/page', { batchId, page: 1, limit: 999 }).then((res: any) => {
    taskDataMap.set(batchId, res.data.list || []);
  }).catch((err: any) => {
    taskErrorMap.set(batchId, '加载失败');
  });
};

const handleExpandChange = (row: any, expandedRows: any[]) => {
  if (row) {
    loadBatchTasks(row.id);
  }
};

const addOrUpdateRef = ref();
const uploadDialogRef = ref();

const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

const goToTaskList = (batchId: number) => {
  window.location.href = '#/demo/darkdetecttask?batchId=' + batchId;
};

const goToCrossResult = (batchId: number) => {
  window.location.href = '#/demo/darkdetectcrossresult?batchId=' + batchId;
};

const uploadHandle = (row: any) => {
  uploadDialogRef.value.init(row);
};

const detectHandle = (row: any) => {
  if (row.totalFiles === 0) {
    ElMessage.warning('请先上传文件');
    return;
  }
  
  ElMessageBox.confirm('确认启动检测？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    baseService.post('/demo/darkdetectbatch/' + row.id + '/detect').then(() => {
      ElMessage.success('检测已启动');
      state.getDataList();
    });
  }).catch(() => {});
};

const resetForm = () => {
  state.dataForm.batchName = '';
  state.dataForm.status = null;
  state.dataForm.detectType = null;
  state.dataForm.dateRange = [];
  state.getDataList();
};

const getStatistics = () => {
  baseService.get('/demo/darkdetectbatch/page?page=1&limit=9999').then((res: any) => {
    const list = res.data.list || [];
    statistics.total = list.length;
    statistics.processing = list.filter((item: any) => item.status === 0).length;
    statistics.completed = list.filter((item: any) => item.status === 1).length;
    statistics.failed = list.filter((item: any) => item.status === 2).length;
  });
};

let refreshTimer: number | null = null;

const refreshExpandedTasks = () => {
  taskDataMap.forEach((_, batchId) => {
    baseService.get('/demo/darkdetecttask/page', { batchId, page: 1, limit: 999 }).then((res: any) => {
      taskDataMap.set(batchId, res.data.list || []);
    });
  });
};

onMounted(() => {
  state.getDataList();
  getStatistics();
  
  refreshTimer = window.setInterval(() => {
    const dataList = state.dataList || [];
    const hasDetecting = dataList.some((item: any) => item.status === 3);
    if (hasDetecting) {
      state.getDataList();
      getStatistics();
      if (taskDataMap.size > 0) {
        refreshExpandedTasks();
      }
    }
  }, 5000);
});

onUnmounted(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer);
    refreshTimer = null;
  }
});
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
}

.stat-icon.blue {
  background-color: #ecf5ff;
  color: #409eff;
}

.stat-icon.orange {
  background-color: #fff7e6;
  color: #e6a23c;
}

.stat-icon.green {
  background-color: #f0f9eb;
  color: #67c23a;
}

.stat-icon.red {
  background-color: #fef0f0;
  color: #f56c6c;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}
</style>
