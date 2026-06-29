<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增批次' : '修改批次'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item v-if="dataForm.id" label="批次编号" prop="batchNo">
        <el-input v-model="dataForm.batchNo" placeholder="批次编号" disabled></el-input>
      </el-form-item>
      <el-form-item label="批次名称" prop="batchName">
        <el-input v-model="dataForm.batchName" placeholder="请输入批次名称"></el-input>
      </el-form-item>
      <el-form-item label="检测类型" prop="detectType">
        <div class="form-content">
          <el-checkbox-group v-model="detectTypeOptions">
            <el-checkbox label="scheme" value="scheme">方案检测</el-checkbox>
            <el-checkbox label="sensitive" value="sensitive">敏感词检测</el-checkbox>
          </el-checkbox-group>
          <div class="help-text">可多选，至少选择一项检测类型</div>
        </div>
      </el-form-item>
      <el-form-item label="检测方案" prop="schemeId" v-if="dataForm.schemeEnabled === 1">
        <el-select v-model="dataForm.schemeId" placeholder="请选择检测方案">
          <el-option v-for="scheme in schemeList" :key="scheme.id" :label="scheme.schemeName" :value="scheme.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="关联分析" prop="isRelated">
        <div class="form-content">
          <el-switch v-model="dataForm.isRelated" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否"></el-switch>
          <div class="help-text">开启后将对批次内所有文件进行跨文件关联分析，生成综合分析报告</div>
        </div>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入备注" :rows="3"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-content {
  display: flex;
  flex-direction: column;
}

.help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}
</style>

<script lang="ts" setup>
import { reactive, ref, onMounted, watch, computed } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();
const schemeList = ref<any[]>([]);

const dataForm = reactive({
  id: '',
  batchNo: '',
  batchName: '',
  schemeId: '',
  schemeEnabled: 1,
  sensitiveEnabled: 0,
  isRelated: 0,
  remark: ''
});

const detectTypeOptions = computed({
  get: () => {
    const options: string[] = [];
    if (dataForm.schemeEnabled === 1) options.push('scheme');
    if (dataForm.sensitiveEnabled === 1) options.push('sensitive');
    return options;
  },
  set: (val: string[]) => {
    dataForm.schemeEnabled = val.includes('scheme') ? 1 : 0;
    dataForm.sensitiveEnabled = val.includes('sensitive') ? 1 : 0;
  }
});

const validateDetectType = (rule: any, value: any, callback: any) => {
  if (dataForm.schemeEnabled === 0 && dataForm.sensitiveEnabled === 0) {
    callback(new Error('至少需要选择一种检测类型'));
  } else {
    callback();
  }
};

const rules = ref({
  batchName: [
    { required: true, message: '批次名称不能为空', trigger: 'blur' }
  ],
  schemeId: [
    { required: true, message: '请选择检测方案', trigger: 'blur' }
  ],
  detectType: [
    { validator: validateDetectType, trigger: 'change' }
  ]
});

onMounted(() => {
  loadSchemeList();
});

const loadSchemeList = () => {
  baseService.get("/demo/darkrulescheme/list").then((res) => {
    schemeList.value = res.data || [];
  });
};

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";
  dataForm.batchNo = "";
  dataForm.batchName = "";
  dataForm.schemeId = "";
  dataForm.schemeEnabled = 1;
  dataForm.sensitiveEnabled = 0;
  dataForm.isRelated = 0;
  dataForm.remark = "";

  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  loadSchemeList();

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: number) => {
  baseService.get("/demo/darkdetectbatch/" + id).then((res) => {
    Object.assign(dataForm, res.data);
    if (dataForm.schemeEnabled === undefined) dataForm.schemeEnabled = 1;
    if (dataForm.sensitiveEnabled === undefined) dataForm.sensitiveEnabled = 0;
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/demo/darkdetectbatch", dataForm).then((res) => {
      ElMessage.success({
        message: '成功',
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList");
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>