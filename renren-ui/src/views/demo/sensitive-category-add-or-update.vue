<template>
  <el-dialog
    v-model="visible"
    :title="!dataForm.id ? '新增分类' : '修改分类'"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    width="500px"
  >
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="dataForm.categoryName" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="dataForm.sortOrder" :min="0" :max="999" placeholder="排序序号" />
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-select v-model="dataForm.enabled" placeholder="请选择状态">
          <el-option :label="`启用`" :value="1" />
          <el-option :label="`禁用`" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="dataForm.remark" placeholder="请输入备注" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import baseService from '@/service/baseService';
import { ElMessage } from 'element-plus';

const emit = defineEmits(['refreshDataList']);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: '',
  categoryName: '',
  sortOrder: 0,
  enabled: 1,
  remark: ''
});

const rules = ref({
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ],
  enabled: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = '';
  dataForm.categoryName = '';
  dataForm.sortOrder = 0;
  dataForm.enabled = 1;
  dataForm.remark = '';

  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: number) => {
  baseService.get('/demo/darksensitivecategory/' + id).then((res: any) => {
    Object.assign(dataForm, res.data);
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)('/demo/darksensitivecategory', dataForm).then((res: any) => {
      ElMessage.success({
        message: '成功',
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit('refreshDataList');
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>
