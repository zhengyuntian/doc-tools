<template>
  <div class="mod-demo__sensitive">
    <div class="content-header">
      <div class="header-title">
        <span>敏感词管理</span>
      </div>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input
            v-model="searchForm.word"
            placeholder="搜索敏感词"
            clearable
            @keyup.enter="state.getDataList()"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.categoryId"
            placeholder="所属分类"
            clearable
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchForm.enabled"
            placeholder="状态"
            clearable
          >
            <el-option :label="`启用`" :value="1" />
            <el-option :label="`禁用`" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="state.getDataList()">查询</el-button>
          <el-button @click="resetSearchHandle()">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="content-toolbar">
      <el-button
        v-if="state.hasPermission('demo:darksensitivecategory:save')"
        type="success"
        @click="addCategoryHandle()"
      >
        + 新增分类
      </el-button>
      <el-button
        v-if="state.hasPermission('demo:darksensitiveword:save')"
        type="primary"
        @click="addWordHandle()"
      >
        + 新增敏感词
      </el-button>
      <el-button
        v-if="state.hasPermission('demo:darksensitiveword:delete')"
        type="danger"
        @click="state.deleteHandle()"
      >
        删除
      </el-button>
    </div>

    <el-table
      v-loading="state.dataListLoading"
      :data="state.dataList"
      border
      @selection-change="state.dataListSelectionChangeHandle"
      style="width: 100%"
    >
      <el-table-column type="selection" header-align="center" align="center" width="50" />
      <el-table-column prop="word" label="敏感词" header-align="center" align="center" />
      <el-table-column prop="categoryName" label="所属分类" header-align="center" align="center" />
      <el-table-column prop="enabled" label="状态" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag :type="scope.row.enabled === 1 ? 'success' : 'danger'">
            {{ scope.row.enabled === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" header-align="center" align="center" width="180" />
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button
            v-if="state.hasPermission('demo:darksensitiveword:update')"
            type="primary"
            link
            @click="editWordHandle(scope.row.id)"
          >
            修改
          </el-button>
          <el-button
            v-if="state.hasPermission('demo:darksensitiveword:delete')"
            type="primary"
            link
            @click="state.deleteHandle(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="state.page"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="state.limit"
      :total="state.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="state.pageSizeChangeHandle"
      @current-change="state.pageCurrentChangeHandle"
    />

    <category-add-or-update
      ref="categoryAddOrUpdateRef"
      @refreshDataList="state.getDataList"
    />
    <word-add-or-update
      ref="wordAddOrUpdateRef"
      @refreshDataList="state.getDataList"
    />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, toRefs, onMounted } from 'vue';
import useView from '@/hooks/useView';
import baseService from '@/service/baseService';
import CategoryAddOrUpdate from './sensitive-category-add-or-update.vue';
import WordAddOrUpdate from './sensitive-word-add-or-update.vue';

const categoryAddOrUpdateRef = ref();
const wordAddOrUpdateRef = ref();

const categoryList = ref<any[]>([]);

const searchForm = reactive({
  word: '',
  categoryId: null as number | null,
  enabled: null as number | null
});

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: '/demo/darksensitiveword/page',
  getDataListIsPage: true,
  exportURL: '/demo/darksensitiveword/export',
  deleteURL: '/demo/darksensitiveword'
});

const state = reactive({ ...useView(view), ...toRefs(view) });

// 获取分类列表
const getCategoryList = () => {
  baseService.get('/demo/darksensitivecategory/page', { page: 1, limit: 1000 }).then((res: any) => {
    if (res.code === 0) {
      categoryList.value = res.data.list || [];
    }
  });
};

const getParams = () => {
  const params: any = {
    ...searchForm
  };
  return params;
};

const originalGetDataList = state.getDataList;
state.getDataList = () => {
  state.dataForm = getParams();
  originalGetDataList.call(state);
};

const addCategoryHandle = () => {
  categoryAddOrUpdateRef.value.init();
};

const addWordHandle = () => {
  wordAddOrUpdateRef.value.init();
};

const editWordHandle = (id: number) => {
  wordAddOrUpdateRef.value.init(id);
};

const resetSearchHandle = () => {
  searchForm.word = '';
  searchForm.categoryId = null;
  searchForm.enabled = null;
  state.getDataList();
};

onMounted(() => {
  getCategoryList();
});
</script>

<style lang="less" scoped>
.mod-demo__sensitive {
  padding: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
  margin-bottom: 16px;
}

.header-title {
  font-weight: bold;
  font-size: 16px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
