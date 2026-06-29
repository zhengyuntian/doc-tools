<template>
  <div class="mod-demo__darkdetectcrossresult">
    <div style="margin-bottom: 15px;">
      <el-button @click="goBack()">返回批次列表</el-button>
    </div>
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item label="批次ID">
        <el-input v-model="state.dataForm.batchId" placeholder="请输入批次ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="分析类型">
        <el-select v-model="state.dataForm.analysisType" placeholder="请选择" clearable>
          <el-option label="敏感词跨文件合并" value="SENSITIVE_CROSS"></el-option>
          <el-option label="格式一致性" value="FORMAT_CONSISTENCY"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="state.getDataList()">查询</el-button>
        <el-button @click="state.resetDataForm()">重置</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetectcrossresult:update')" type="success" @click="analyzeHandle()">执行关联分析</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetectcrossresult:export')" type="warning" @click="state.exportHandle()">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="结果ID" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="batchName" label="批次名称" header-align="center" align="center" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="analysisType" label="分析类型" header-align="center" align="center" width="140">
        <template v-slot="scope">
          <el-tag v-if="scope.row.analysisType === 'SENSITIVE_CROSS'" type="danger">敏感词跨文件</el-tag>
          <el-tag v-else-if="scope.row.analysisType === 'FORMAT_CONSISTENCY'" type="warning">格式一致性</el-tag>
          <span v-else>{{ scope.row.analysisType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="analysisName" label="分析名称" header-align="center" align="center" min-width="180"></el-table-column>
      <el-table-column prop="involvedFiles" label="涉及文件" header-align="center" align="center" min-width="250">
        <template v-slot="scope">
          <template v-if="scope.row.involvedFiles && scope.row.involvedFiles.length > 0">
            <el-tooltip v-if="scope.row.involvedFiles.split(',').length > 3" :content="scope.row.involvedFiles" placement="top-start">
              <span>{{ scope.row.involvedFiles.split(',').slice(0, 3).join(', ') }}...</span>
            </el-tooltip>
            <span v-else>{{ scope.row.involvedFiles }}</span>
          </template>
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column prop="isPass" label="检测结果" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag :type="scope.row.isPass === 1 ? 'success' : 'danger'">
            {{ scope.row.isPass === 1 ? '通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="severity" label="严重程度" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag v-if="scope.row.severity === 1" type="info">警告</el-tag>
          <el-tag v-else-if="scope.row.severity === 2" type="warning">一般</el-tag>
          <el-tag v-else-if="scope.row.severity === 3" type="danger">严重</el-tag>
          <span v-else>{{ scope.row.severity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="actualValue" label="实际值" header-align="center" align="center" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="remark" label="建议" header-align="center" align="center" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="createTime" label="创建时间" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('demo:darkdetectcrossresult:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs, onMounted } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/demo/darkdetectcrossresult/page",
  getDataListIsPage: true,
  exportURL: "/demo/darkdetectcrossresult/export",
  deleteURL: "/demo/darkdetectcrossresult",
  dataForm: {
    batchId: "",
    analysisType: ""
  }
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const analyzeHandle = () => {
  const batchId = (state.dataForm as any).batchId;
  if (!batchId) {
    ElMessage.warning("请先输入批次ID");
    return;
  }

  baseService.post("/demo/darkdetectcrossresult/analyze", { batchId: parseInt(batchId) })
    .then(() => {
      ElMessage.success("关联分析执行成功");
      state.getDataList();
    })
    .catch(() => {
      ElMessage.error("关联分析执行失败");
    });
};

const resetDataForm = () => {
  (state.dataForm as any).batchId = "";
  (state.dataForm as any).analysisType = "";
};

const goBack = () => {
  window.location.href = '#/demo/darkdetectbatch';
};

onMounted(() => {
  const params = new URLSearchParams(window.location.hash.substring(1));
  const batchIdParam = params.get('batchId');
  if (batchIdParam) {
    (state.dataForm as any).batchId = batchIdParam;
  }
  state.getDataList();
});
</script>
