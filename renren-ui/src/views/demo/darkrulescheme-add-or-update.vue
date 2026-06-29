<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增方案' : '修改方案'" :close-on-click-modal="false" :close-on-press-escape="false" width="800px" top="20px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="100px">
      <el-form-item label="方案名称" prop="schemeName">
        <el-input v-model="dataForm.schemeName" placeholder="请输入方案名称"></el-input>
      </el-form-item>
      <el-form-item label="方案描述" prop="schemeDesc">
        <el-input v-model="dataForm.schemeDesc" type="textarea" :rows="2" placeholder="请输入方案描述"></el-input>
      </el-form-item>
      <el-form-item label="设为默认">
        <el-switch v-model="dataForm.isDefault" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否"></el-switch>
      </el-form-item>
      <el-form-item label="启用状态">
        <el-switch v-model="dataForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用"></el-switch>
      </el-form-item>

      <div style="margin-top: 20px; padding-top: 20px; border-top: 1px dashed #e6e6e6;">
        <div style="font-weight: bold; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
          <span>选择规则并配置参数值</span>
          <el-button type="primary" link @click="loadAllRules">刷新规则列表</el-button>
        </div>

        <el-tabs v-model="activeTab" type="border-card" @tab-change="onTabChange">
          <el-tab-pane v-for="cat in categories" :key="cat.value" :label="cat.label" :name="cat.value">
            <div v-if="(rulesByCategory[cat.value] || []).length === 0" style="text-align: center; color: #909399; padding: 40px;">
              该分类暂无规则
            </div>
            <div v-else>
              <el-table :data="rulesByCategory[cat.value]" size="small" style="width: 100%;">
                <el-table-column type="selection" width="50" :selectable="(row) => row.paramType !== 'FIXED'">
                  <template #default="scope">
                    <el-checkbox :model-value="getItemEnabled(scope.row.id)" @change="() => toggleRule(scope.row)"></el-checkbox>
                  </template>
                </el-table-column>
                <el-table-column label="规则名称" width="200">
                  <template #default="scope">
                    <span>{{ scope.row.ruleName }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="参数名称" width="120">
                  <template #default="scope">
                    <span>{{ scope.row.paramName }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="参数值" width="250">
                  <template #default="scope">
                    <template v-if="scope.row.paramType === 'BOOLEAN' && getItemEnabled(scope.row.id)">
                      <el-switch :model-value="getItemValue(scope.row.id, 'BOOLEAN') === 'true'" @change="(val) => setItemValue(scope.row.id, val)" active-text="是" inactive-text="否"></el-switch>
                    </template>
                    <template v-else-if="scope.row.paramType === 'FIXED' && getItemEnabled(scope.row.id)">
                      <el-input :value="getDisplayValue(scope.row)" disabled style="width: 150px;" />
                      <span style="margin-left: 8px; color: #909399; font-size: 12px;">(固定值)</span>
                    </template>
                    <template v-else-if="scope.row.paramType === 'NUMBER' && getItemEnabled(scope.row.id)">
                      <el-input-number :model-value="getItemValue(scope.row.id, 'NUMBER')" @change="(val) => setItemValue(scope.row.id, val)" :min="0" style="width: 150px;"></el-input-number>
                      <span v-if="scope.row.paramUnit" style="margin-left: 8px; font-size: 12px;">{{ scope.row.paramUnit }}</span>
                    </template>
                    <template v-else-if="scope.row.paramType === 'FLOAT' && getItemEnabled(scope.row.id)">
                      <el-input :model-value="getItemValue(scope.row.id, 'FLOAT')" @input="(val) => setItemValue(scope.row.id, val)" type="number" style="width: 150px;"></el-input>
                      <span v-if="scope.row.paramUnit" style="margin-left: 8px; font-size: 12px;">{{ scope.row.paramUnit }}</span>
                    </template>
                    <template v-else-if="(scope.row.paramType === 'SELECT' || scope.row.paramType === 'MULTI_SELECT') && getItemEnabled(scope.row.id)">
                      <el-select :model-value="getItemValue(scope.row.id, scope.row.paramType)" @change="(val) => setItemValue(scope.row.id, val)" :multiple="scope.row.paramType === 'MULTI_SELECT'" style="width: 200px;">
                        <el-option v-for="opt in getOptions(scope.row.paramOptions)" :key="opt.value" :label="opt.label" :value="opt.value" />
                      </el-select>
                    </template>
                    <template v-else-if="getItemEnabled(scope.row.id)">
                      <el-input :model-value="getItemValue(scope.row.id)" @input="(val) => setItemValue(scope.row.id, val)" style="width: 200px;"></el-input>
                      <span v-if="scope.row.paramUnit" style="margin-left: 8px; font-size: 12px;">{{ scope.row.paramUnit }}</span>
                    </template>
                    <template v-else>
                      <span style="color: #909399; font-size: 12px;">已禁用</span>
                    </template>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref, computed } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();
const activeTab = ref('layout');
const allRules = ref<any[]>([]);

const categories = [
  { label: '版式', value: 'layout' },
  { label: '字体', value: 'font' },
  { label: '表格', value: 'table' },
  { label: '图表', value: 'chart' },
  { label: '标题', value: 'title' },
  { label: '目录', value: 'toc' },
  { label: '自检', value: 'self_check' },
  { label: '页数', value: 'page_count' },
  { label: '敏感词', value: 'sensitive' },
  { label: '交叉检测', value: 'cross' }
];

const dataForm = reactive({
  id: '', schemeName: '', schemeDesc: '', isDefault: 0, enabled: 1,
  items: [] as any[]
});

const rules = ref({
  schemeName: [{ required: true, message: '方案名称不能为空', trigger: 'blur' }]
});

// 计算属性：按分类分组的规则
const rulesByCategory = computed(() => {
  const result: Record<string, any[]> = {};
  allRules.value.forEach(r => {
    const enabledMatch = typeof r.enabled === 'number' ? r.enabled === 1 : parseInt(r.enabled) === 1;
    if (enabledMatch && r.ruleCategory) {
      if (!result[r.ruleCategory]) {
        result[r.ruleCategory] = [];
      }
      result[r.ruleCategory].push(r);
    }
  });
  return result;
});

// 获取指定分类的规则
const getRulesByCategory = (category: string) => {
  const rules = rulesByCategory.value[category] || [];
  return rules;
};

const loadAllRules = () => {
  baseService.get('/demo/darkruleconfig/page', { page: 1, limit: 999 }).then((res: any) => {
    if (res.code === 0) {
      allRules.value = res.data.list || [];
    }
  }).catch((err: any) => {
    console.error('loadAllRules error:', err);
  });
};

const getItemEnabled = (ruleConfigId: number | string) => {
  const id = Number(ruleConfigId);
  const item = dataForm.items.find(i => Number(i.ruleConfigId) === id);
  if (!item) return true;
  return item.enabled === 1;
};

const getItemValue = (ruleConfigId: number | string, paramType?: string) => {
  const id = Number(ruleConfigId);
  const item = dataForm.items.find(i => Number(i.ruleConfigId) === id);
  
  if (item && item.paramValue !== undefined && item.paramValue !== '') {
    if (paramType === 'NUMBER') {
      const val = Number(item.paramValue);
      return isNaN(val) ? null : val;
    }
    if (paramType === 'FLOAT') {
      const val = parseFloat(item.paramValue);
      return isNaN(val) ? null : val;
    }
    if (paramType === 'BOOLEAN') {
      return String(item.paramValue);
    }
    if (paramType === 'MULTI_SELECT') {
      return item.paramValue.split(',');
    }
    return item.paramValue;
  }
  
  if (paramType === 'SELECT') {
    const rule = allRules.value.find(r => Number(r.id) === id);
    if (rule && rule.paramOptions) {
      const options = getOptions(rule.paramOptions);
      if (options.length > 0) {
        return options[0].value;
      }
    }
    return null;
  }
  
  if (paramType === 'MULTI_SELECT') {
    const rule = allRules.value.find(r => Number(r.id) === id);
    if (rule && rule.paramOptions) {
      const options = getOptions(rule.paramOptions);
      return options.map(opt => opt.value);
    }
    return [];
  }
  
  const rule = allRules.value.find(r => Number(r.id) === id);
  if (rule && rule.paramValue !== undefined && rule.paramValue !== '') {
    if (paramType === 'NUMBER') {
      const val = Number(rule.paramValue);
      return isNaN(val) ? null : val;
    }
    if (paramType === 'FLOAT') {
      const val = parseFloat(rule.paramValue);
      return isNaN(val) ? null : val;
    }
    if (paramType === 'BOOLEAN') {
      return String(rule.paramValue);
    }
    return rule.paramValue;
  }
  
  return null;
};

const setItemValue = (ruleConfigId: number | string, value: any) => {
  const id = Number(ruleConfigId);
  let item = dataForm.items.find(i => Number(i.ruleConfigId) === id);
  if (!item) {
    const rule = allRules.value.find(r => Number(r.id) === id);
    item = { ruleConfigId: id, paramValue: '', enabled: 1, ruleCode: rule?.ruleCode || '', ruleName: rule?.ruleName || '' };
    dataForm.items.push(item);
  }
  if (Array.isArray(value)) {
    item.paramValue = value.join(',');
  } else {
    item.paramValue = value;
  }
};

const toggleRule = (rule: any) => {
  const id = Number(rule.id);
  const index = dataForm.items.findIndex(i => Number(i.ruleConfigId) === id);
  
  const getDefaultValue = () => {
    if (rule.paramType === 'SELECT' && rule.paramOptions) {
      const options = getOptions(rule.paramOptions);
      if (options.length > 0) {
        return options[0].value;
      }
    }
    if (rule.paramType === 'MULTI_SELECT' && rule.paramOptions) {
      const options = getOptions(rule.paramOptions);
      return options.map(opt => opt.value).join(',');
    }
    if (rule.paramValue && rule.paramValue !== '') {
      return rule.paramValue;
    }
    return '';
  };
  
  if (index >= 0) {
    const item = dataForm.items[index];
    if (item.enabled === 1) {
      item.enabled = 0;
    } else {
      item.enabled = 1;
      if (!item.paramValue) {
        item.paramValue = getDefaultValue();
      }
    }
  } else {
    dataForm.items.push({
      ruleConfigId: id,
      paramValue: getDefaultValue(),
      enabled: 1,
      ruleCode: rule.ruleCode,
      ruleName: rule.ruleName
    });
  }
};

const valueMapping: Record<string, Record<string, string>> = {
  color: {
    'black': '黑色',
    'red': '红色',
    'blue': '蓝色',
    'green': '绿色'
  },
  direction: {
    'left_to_right': '从左到右',
    'right_to_left': '从右到左',
    'portrait': '纵向',
    'landscape': '横向'
  },
  title_style: {
    'luoyang': '洛阳'
  }
};

const getDisplayValue = (rule: any) => {
  if (!rule.paramValue) return '';
  
  if (rule.paramKey?.includes('color') || rule.paramName?.includes('颜色')) {
    return valueMapping.color[rule.paramValue] || rule.paramValue;
  }
  if (rule.paramKey?.includes('direction') || rule.paramName?.includes('方向') || rule.paramName?.includes('纸张')) {
    return valueMapping.direction[rule.paramValue] || rule.paramValue;
  }
  if (rule.paramKey?.includes('style') || rule.paramName?.includes('样式')) {
    return valueMapping.title_style[rule.paramValue] || rule.paramValue;
  }
  
  return rule.paramValue;
};

const getOptions = (optionsStr: string) => {
  if (!optionsStr) return [];
  try {
    const arr = JSON.parse(optionsStr);
    return arr.map((item: string) => {
      if (item.includes(':')) {
        const [value, label] = item.split(':');
        return { value, label };
      }
      return { value: item, label: item };
    });
  } catch {
    return [];
  }
};

const onTabChange = (tabName: string) => {
  activeTab.value = tabName;
};

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";
  dataForm.schemeName = "";
  dataForm.schemeDesc = "";
  dataForm.isDefault = 0;
  dataForm.enabled = 1;
  dataForm.items = [];

  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  loadAllRules();

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: number) => {
  baseService.get("/demo/darkrulescheme/" + id).then((res: any) => {
    if (res.code === 0) {
      const data = res.data;
      dataForm.id = data.id;
      dataForm.schemeName = data.schemeName;
      dataForm.schemeDesc = data.schemeDesc;
      dataForm.isDefault = data.isDefault || 0;
      dataForm.enabled = data.enabled || 1;
      dataForm.items = data.items || [];

      const firstEnabled = data.items?.find((item: any) => parseInt(item.enabled) === 1);
      if (firstEnabled) {
        activeTab.value = firstEnabled.ruleCategory || 'layout';
      }
    }
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) return false;

    // 将所有显示为启用状态的规则都添加到items中
    allRules.value.forEach(rule => {
      const id = Number(rule.id);
      const existingItem = dataForm.items.find(i => Number(i.ruleConfigId) === id);
      if (!existingItem) {
        const getDefaultValue = () => {
          if (rule.paramType === 'SELECT' && rule.paramOptions) {
            const options = getOptions(rule.paramOptions);
            if (options.length > 0) return options[0].value;
          }
          if (rule.paramType === 'MULTI_SELECT' && rule.paramOptions) {
            const options = getOptions(rule.paramOptions);
            return options.map(opt => opt.value).join(',');
          }
          if (rule.paramValue && rule.paramValue !== '') {
            return rule.paramValue;
          }
          return '';
        };
        dataForm.items.push({
          ruleConfigId: id,
          paramValue: getDefaultValue(),
          enabled: 1,
          ruleCode: rule.ruleCode,
          ruleName: rule.ruleName,
          ruleCategory: rule.ruleCategory
        });
      }
    });

    const submitData = { ...dataForm };
    submitData.items = dataForm.items.filter((item: any) => item.enabled === 1);

    (!dataForm.id ? baseService.post : baseService.put)("/demo/darkrulescheme", submitData).then((res: any) => {
      if (res.code === 0) {
        ElMessage.success({
          message: '成功',
          duration: 500,
          onClose: () => {
            visible.value = false;
            emit("refreshDataList");
          }
        });
      } else {
        ElMessage.error(res.msg || '操作失败');
      }
    });
  });
};

defineExpose({ init });
</script>

<style scoped>
.disabled-text { color: #909399; }
</style>
