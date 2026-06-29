<template>
  <div class="mod-demo__darkrulescheme">
    <div class="content-header">
      <div class="header-title">
        <span>规则方案管理</span>
      </div>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input v-model="searchForm.schemeName" placeholder="方案名称" clearable @keyup.enter="state.getDataList()" />
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
      <el-button v-if="state.hasPermission('demo:darkrulescheme:save')" type="primary" @click="addOrUpdateHandle()">
        + 新增方案
      </el-button>
      <el-button v-if="state.hasPermission('demo:darkrulescheme:delete')" type="danger" @click="state.deleteHandle()">
        删除
      </el-button>
    </div>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50" />
      <el-table-column prop="schemeName" label="方案名称" header-align="center" align="center" width="200" />
      <el-table-column prop="schemeDesc" label="方案描述" header-align="center" align="center" />
      <el-table-column prop="isDefault" label="默认" header-align="center" align="center" width="80">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="warning" size="small">默认</el-tag>
          <el-tag v-else type="info" size="small">非默认</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag :type="scope.row.enabled === 1 ? 'success' : 'danger'">{{ scope.row.enabled === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" header-align="center" align="center" width="180" />
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="250">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('demo:darkrulescheme:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">
            修改
          </el-button>
          <el-button v-if="state.hasPermission('demo:darkrulescheme:update') && scope.row.isDefault !== 1" type="primary" link @click="setDefaultHandle(scope.row.id)">
            设为默认
          </el-button>
          <el-button v-if="state.hasPermission('demo:darkrulescheme:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">
            删除
          </el-button>
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
import { ElMessage } from 'element-plus';
import AddOrUpdate from './darkrulescheme-add-or-update.vue';

const addOrUpdateRef = ref();

const searchForm = reactive({
  schemeName: '', enabled: null as number | null
});

const view = reactive({
  deleteIsBatch: true, getDataListURL: '/demo/darkrulescheme/page',
  getDataListIsPage: true, deleteURL: '/demo/darkrulescheme'
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const getParams = () => ({ ...searchForm });
const originalGetDataList = state.getDataList;
state.getDataList = () => { state.dataForm = getParams(); originalGetDataList.call(state); };

const addOrUpdateHandle = (id?: number) => { addOrUpdateRef.value.init(id); };

const setDefaultHandle = (id: number) => {
  baseService.post('/demo/darkrulescheme/' + id + '/default').then((res: any) => {
    if (res.code === 0) {
      ElMessage.success('已设为默认方案');
      state.getDataList();
    }
  });
};

const resetSearchHandle = () => {
  searchForm.schemeName = ''; searchForm.enabled = null;
  state.getDataList();
};
</script>

<style lang="less" scoped>
.mod-demo__darkrulescheme { padding: 16px; }
.content-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; border-bottom: 1px solid #e6e6e6; margin-bottom: 16px; }
.header-title { font-weight: bold; font-size: 16px; }
.search-form { display: flex; align-items: center; gap: 12px; }
.content-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
