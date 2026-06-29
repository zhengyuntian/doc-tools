<template>
  <div class="mod-demo__darkdetectresult">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetectresult:save')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:darkdetectresult:delete')" type="danger" @click="state.deleteHandle()">删除</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
              <el-table-column prop="id" label="结果ID" header-align="center" align="center"></el-table-column>
              <el-table-column prop="taskId" label="任务ID" header-align="center" align="center"></el-table-column>
              <el-table-column prop="ruleCode" label="规则编码" header-align="center" align="center"></el-table-column>
              <el-table-column prop="ruleName" label="规则名称" header-align="center" align="center"></el-table-column>
              <el-table-column prop="ruleCategory" label="规则分类：layout/font/table/sensitive" header-align="center" align="center"></el-table-column>
              <el-table-column prop="pageNo" label="违规所在页码" header-align="center" align="center"></el-table-column>
              <el-table-column prop="paragraphIndex" label="违规所在段落索引" header-align="center" align="center"></el-table-column>
              <el-table-column prop="actualValue" label="实际检测值" header-align="center" align="center"></el-table-column>
              <el-table-column prop="expectedValue" label="期望值" header-align="center" align="center"></el-table-column>
              <el-table-column prop="isPass" label="0-不通过，1-通过" header-align="center" align="center"></el-table-column>
              <el-table-column prop="severity" label="1-警告，2-一般，3-严重" header-align="center" align="center"></el-table-column>
              <el-table-column prop="remark" label="修改建议" header-align="center" align="center"></el-table-column>
              <el-table-column prop="createTime" label="创建时间" header-align="center" align="center"></el-table-column>
            <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('demo:darkdetectresult:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
          <el-button v-if="state.hasPermission('demo:darkdetectresult:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList">确定</add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./darkdetectresult-add-or-update.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/demo/darkdetectresult/page",
  getDataListIsPage: true,
  exportURL: "/demo/darkdetectresult/export",
  deleteURL: "/demo/darkdetectresult"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
