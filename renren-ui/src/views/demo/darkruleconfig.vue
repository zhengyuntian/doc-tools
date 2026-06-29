<template>
  <div class="mod-demo__darkruleconfig">
    <div class="content-header">
      <div class="header-title">
        <span>检测规则配置</span>
      </div>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input v-model="searchForm.ruleName" placeholder="规则名称" clearable @keyup.enter="state.getDataList()" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.ruleCategory" placeholder="分类" clearable>
            <el-option label="版式" value="layout" />
            <el-option label="字体" value="font" />
            <el-option label="表格" value="table" />
            <el-option label="图表" value="chart" />
            <el-option label="标题" value="title" />
            <el-option label="目录" value="toc" />
            <el-option label="自检" value="self_check" />
            <el-option label="页数" value="page_count" />
            <el-option label="敏感词" value="sensitive" />
            <el-option label="交叉检测" value="cross" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.enabled" placeholder="状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="state.getDataList()">查询</el-button>
          <el-button @click="resetSearchHandle()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="content-toolbar">
      <el-button v-if="state.hasPermission('demo:darkruleconfig:save')" type="primary" @click="addOrUpdateHandle()">
        + 新增规则
      </el-button>
      <el-button v-if="state.hasPermission('demo:darkruleconfig:delete')" type="danger" @click="state.deleteHandle()">
        删除
      </el-button>
    </div>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50" :selectable="(row: any) => row.paramType !== 'FIXED'" />
      <el-table-column prop="ruleCode" label="规则编码" header-align="center" align="center" width="120" />
      <el-table-column prop="ruleName" label="规则名称" header-align="center" align="center" width="150" />
      <el-table-column prop="ruleGroup" label="分组" header-align="center" align="center" width="100" />
      <el-table-column prop="ruleCategory" label="分类" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag size="small">{{ categoryMap[scope.row.ruleCategory] || scope.row.ruleCategory }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paramKey" label="参数键" header-align="center" align="center" width="120" />
      <el-table-column prop="paramName" label="参数名称" header-align="center" align="center" width="120" />
      <el-table-column prop="paramType" label="参数类型" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag size="small" :type="getParamTypeTagType(scope.row.paramType)">{{ paramTypeMap[scope.row.paramType] || scope.row.paramType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paramValue" label="默认值" header-align="center" align="center" width="100" />
      <el-table-column prop="paramUnit" label="单位" header-align="center" align="center" width="80" />
      <el-table-column prop="enabled" label="状态" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag :type="scope.row.enabled === 1 ? 'success' : 'danger'">{{ scope.row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="200">
        <template v-slot="scope">
          <template v-if="scope.row.paramType !== 'FIXED'">
            <el-button v-if="state.hasPermission('demo:darkruleconfig:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">
              修改
            </el-button>
            <el-button v-if="state.hasPermission('demo:darkruleconfig:update')" type="primary" link @click="toggleEnabledHandle(scope.row.id)">
              {{ scope.row.enabled === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="state.hasPermission('demo:darkruleconfig:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">
              删除
            </el-button>
          </template>
          <template v-else>
            <span style="color: #909399; font-size: 12px;">不可修改</span>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle" />

    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList" />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, toRefs } from 'vue';
import useView from '@/hooks/useView';
import baseService from '@/service/baseService';
import AddOrUpdate from './darkruleconfig-add-or-update.vue';

const addOrUpdateRef = ref();

const categoryMap: Record<string, string> = {
  layout: '版式', font: '字体', table: '表格', chart: '图表',
  title: '标题', toc: '目录', self_check: '自检',
  page_count: '页数', sensitive: '敏感词', cross: '交叉检测'
};

const paramTypeMap: Record<string, string> = {
  TEXT: '文本', NUMBER: '整数', FLOAT: '浮点',
  SELECT: '下拉', MULTI_SELECT: '多选', BOOLEAN: '开关', FIXED: '固定值'
};

const searchForm = reactive({
  ruleName: '', ruleCategory: '', enabled: null as number | null
});

const view = reactive({
  deleteIsBatch: true, getDataListURL: '/demo/darkruleconfig/page',
  getDataListIsPage: true, exportURL: '/demo/darkruleconfig/export', deleteURL: '/demo/darkruleconfig'
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const getParams = () => ({ ...searchForm });
const originalGetDataList = state.getDataList;
state.getDataList = () => { state.dataForm = getParams(); originalGetDataList.call(state); };

const addOrUpdateHandle = (id?: number) => { addOrUpdateRef.value.init(id); };

const toggleEnabledHandle = (id: number) => {
  baseService.post('/demo/darkruleconfig/' + id + '/toggle').then((res: any) => {
    if (res.code === 0) state.getDataList();
  });
};

const resetSearchHandle = () => {
  searchForm.ruleName = ''; searchForm.ruleCategory = ''; searchForm.enabled = null;
  state.getDataList();
};

const getParamTypeTagType = (type: string) => {
  const map: Record<string, string> = { FIXED: 'warning', SELECT: 'info', MULTI_SELECT: 'success', BOOLEAN: 'primary' };
  return map[type] || '';
};
</script>

<style lang="less" scoped>
.mod-demo__darkruleconfig { padding: 16px; }
.content-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; border-bottom: 1px solid #e6e6e6; margin-bottom: 16px; }
.header-title { font-weight: bold; font-size: 16px; }
.search-form { display: flex; align-items: center; gap: 12px; }
.content-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
