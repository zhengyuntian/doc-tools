<template>
  <el-dialog
    v-model="visible"
    :title="!dataForm.id ? '新增敏感词' : '修改敏感词'"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    width="500px"
  >
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="所属分类" prop="categoryId">
        <el-select v-model="dataForm.categoryId" placeholder="请选择分类">
          <el-option
            v-for="category in categoryList"
            :key="category.id"
            :label="category.categoryName"
            :value="category.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="敏感词" prop="word">
        <el-input v-model="dataForm.word" placeholder="请输入敏感词" />
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-select v-model="dataForm.enabled" placeholder="请选择状态">
          <el-option :label="`启用`" :value="1" />
          <el-option :label="`禁用`" :value="0" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref, watch } from 'vue';
import baseService from '@/service/baseService';
import { ElMessage } from 'element-plus';

const props = defineProps({
  categoryId: {
    type: Number,
    default: null
  }
});

const emit = defineEmits(['refreshDataList']);

const visible = ref(false);
const dataFormRef = ref();
const categoryList = ref<any[]>([]);

const dataForm = reactive({
  id: '',
  categoryId: null as number | null,
  word: '',
  enabled: 1
});

const rules = ref({
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' }
  ],
  enabled: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
});

watch(() => props.categoryId, (newVal) => {
  if (newVal !== null && !dataForm.id) {
    dataForm.categoryId = newVal;
  }
});

const init = (id?: number, defaultCategoryId?: number) => {
  visible.value = true;
  dataForm.id = '';
  dataForm.categoryId = defaultCategoryId || null;
  dataForm.word = '';
  dataForm.enabled = 1;

  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  loadCategoryList().then(() => {
    if (id) {
      getInfo(id);
    }
  });
};

const getInfo = (id: number) => {
  baseService.get('/demo/darksensitiveword/' + id).then((res: any) => {
    Object.assign(dataForm, res.data);
  });
};

const loadCategoryList = () => {
  return baseService.get('/demo/darksensitivecategory/list').then((res: any) => {
    categoryList.value = res.data || [];
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)('/demo/darksensitiveword', dataForm).then((res: any) => {
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
