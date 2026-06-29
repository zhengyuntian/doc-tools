<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增规则' : '修改规则'" :close-on-click-modal="false" :close-on-press-escape="false" width="700px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="140px">
      <el-form-item label="规则编码" prop="ruleCode">
        <el-input v-model="dataForm.ruleCode" placeholder="规则编码（固定值）"></el-input>
      </el-form-item>
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="dataForm.ruleName" placeholder="规则名称"></el-input>
      </el-form-item>
      <el-form-item label="规则分组" prop="ruleGroup">
        <el-input v-model="dataForm.ruleGroup" placeholder="规则分组标识（同一规则下的多个参数共享）"></el-input>
      </el-form-item>
      <el-form-item label="分类" prop="ruleCategory">
        <el-select v-model="dataForm.ruleCategory" placeholder="请选择分类" style="width: 100%">
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
      <el-form-item label="参数键" prop="paramKey">
        <el-input v-model="dataForm.paramKey" placeholder="参数键"></el-input>
      </el-form-item>
      <el-form-item label="参数显示名称" prop="paramName">
        <el-input v-model="dataForm.paramName" placeholder="参数显示名称"></el-input>
      </el-form-item>
      <el-form-item label="参数类型" prop="paramType">
        <el-select v-model="dataForm.paramType" placeholder="请选择参数类型" style="width: 100%" @change="onParamTypeChange">
          <el-option label="固定值（不可修改）" value="FIXED" />
          <el-option label="文本输入" value="TEXT" />
          <el-option label="整数输入" value="NUMBER" />
          <el-option label="浮点输入" value="FLOAT" />
          <el-option label="下拉选择" value="SELECT" />
          <el-option label="多选" value="MULTI_SELECT" />
          <el-option label="布尔开关" value="BOOLEAN" />
        </el-select>
      </el-form-item>
      <el-form-item label="可选选项" v-if="showOptionsInput" prop="paramOptions">
        <el-textarea v-model="dataForm.paramOptions" placeholder='格式：["值1:显示名1","值2:显示名2"]，如：["宋体","仿宋","黑体"]' :rows="4"></el-textarea>
        <div style="color: #909399; font-size: 12px; margin-top: 4px;">
          纯值模式：["选项1","选项2"] 或 键值模式：["value:显示名"]
        </div>
      </el-form-item>
      <el-form-item label="参数值" prop="paramValue">
        <template v-if="dataForm.paramType === 'BOOLEAN'">
          <el-switch v-model="dataForm.paramValue" active-value="true" inactive-value="false" active-text="启用" inactive-text="禁用"></el-switch>
        </template>
        <template v-else-if="dataForm.paramType === 'FIXED'">
          <el-input v-model="dataForm.paramValue" placeholder="固定值（用户不可修改）"></el-input>
        </template>
        <template v-else-if="dataForm.paramType === 'NUMBER'">
          <el-input-number v-model="dataForm.paramValue" :min="0" style="width: 100%" placeholder="请输入整数"></el-input-number>
        </template>
        <template v-else-if="dataForm.paramType === 'FLOAT'">
          <el-input v-model="dataForm.paramValue" type="number" placeholder="请输入浮点数"></el-input>
        </template>
        <template v-else-if="dataForm.paramType === 'SELECT' || dataForm.paramType === 'MULTI_SELECT'">
          <el-select v-model="dataForm.paramValue" :multiple="dataForm.paramType === 'MULTI_SELECT'" placeholder="请选择" style="width: 100%">
            <el-option v-for="opt in parsedOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </template>
        <template v-else>
          <el-input v-model="dataForm.paramValue" placeholder="参数值"></el-input>
        </template>
      </el-form-item>
      <el-form-item label="单位" prop="paramUnit">
        <el-input v-model="dataForm.paramUnit" placeholder="单位（如：cm、pt、字符、页）"></el-input>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="dataForm.sortOrder" :min="0" :max="999" placeholder="排序"></el-input-number>
      </el-form-item>
      <el-form-item label="启用状态" prop="enabled">
        <el-switch v-model="dataForm.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用"></el-switch>
      </el-form-item>
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

const dataForm = reactive({
  id: '',
  ruleCode: '',
  ruleName: '',
  ruleGroup: '',
  ruleCategory: '',
  paramKey: '',
  paramName: '',
  paramValue: '',
  paramUnit: '',
  paramType: 'TEXT',
  paramOptions: '',
  sortOrder: 0,
  enabled: 1
});

const rules = ref({
  ruleCode: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
  ruleName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
  ruleCategory: [{ required: true, message: '必填项不能为空', trigger: 'change' }],
  paramKey: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
  paramValue: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
  paramType: [{ required: true, message: '必填项不能为空', trigger: 'change' }]
});

const showOptionsInput = computed(() => {
  return ['SELECT', 'MULTI_SELECT', 'FIXED'].includes(dataForm.paramType);
});

const parsedOptions = computed(() => {
  if (!dataForm.paramOptions) return [];
  try {
    const arr = JSON.parse(dataForm.paramOptions);
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
});

const onParamTypeChange = () => {
  if (!showOptionsInput.value) {
    dataForm.paramOptions = '';
  }
};

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  dataForm.sortOrder = 0;
  dataForm.enabled = 1;
  dataForm.paramType = 'TEXT';
  dataForm.paramOptions = '';

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: number) => {
  baseService.get("/demo/darkruleconfig/" + id).then((res: any) => {
    Object.assign(dataForm, res.data);
    if (dataForm.paramValue === null) {
      dataForm.paramValue = '';
    }
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) return false;

    (!dataForm.id ? baseService.post : baseService.put)("/demo/darkruleconfig", dataForm).then((res: any) => {
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
