<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
          <el-form-item label="任务ID" prop="taskId">
        <el-input v-model="dataForm.taskId" placeholder="任务ID"></el-input>
      </el-form-item>
          <el-form-item label="规则编码" prop="ruleCode">
        <el-input v-model="dataForm.ruleCode" placeholder="规则编码"></el-input>
      </el-form-item>
          <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="dataForm.ruleName" placeholder="规则名称"></el-input>
      </el-form-item>
          <el-form-item label="规则分类：layout/font/table/sensitive" prop="ruleCategory">
        <el-input v-model="dataForm.ruleCategory" placeholder="规则分类：layout/font/table/sensitive"></el-input>
      </el-form-item>
          <el-form-item label="违规所在页码" prop="pageNo">
        <el-input v-model="dataForm.pageNo" placeholder="违规所在页码"></el-input>
      </el-form-item>
          <el-form-item label="违规所在段落索引" prop="paragraphIndex">
        <el-input v-model="dataForm.paragraphIndex" placeholder="违规所在段落索引"></el-input>
      </el-form-item>
          <el-form-item label="实际检测值" prop="actualValue">
        <el-input v-model="dataForm.actualValue" placeholder="实际检测值"></el-input>
      </el-form-item>
          <el-form-item label="期望值" prop="expectedValue">
        <el-input v-model="dataForm.expectedValue" placeholder="期望值"></el-input>
      </el-form-item>
          <el-form-item label="0-不通过，1-通过" prop="isPass">
        <el-input v-model="dataForm.isPass" placeholder="0-不通过，1-通过"></el-input>
      </el-form-item>
          <el-form-item label="1-警告，2-一般，3-严重" prop="severity">
        <el-input v-model="dataForm.severity" placeholder="1-警告，2-一般，3-严重"></el-input>
      </el-form-item>
          <el-form-item label="修改建议" prop="remark">
        <el-input v-model="dataForm.remark" placeholder="修改建议"></el-input>
      </el-form-item>
          <el-form-item label="创建时间" prop="createTime">
        <el-input v-model="dataForm.createTime" placeholder="创建时间"></el-input>
      </el-form-item>
      </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: '',  taskId: '',  ruleCode: '',  ruleName: '',  ruleCategory: '',  pageNo: '',  paragraphIndex: '',  actualValue: '',  expectedValue: '',  isPass: '',  severity: '',  remark: '',  createTime: ''});

const rules = ref({
          taskId: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          ruleCode: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          ruleName: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          ruleCategory: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          pageNo: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          paragraphIndex: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          actualValue: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          expectedValue: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          isPass: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          severity: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          remark: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          createTime: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ]
  });

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/demo/darkdetectresult/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/demo/darkdetectresult", dataForm).then((res) => {
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
