<template>
  <el-dialog v-model="visible" :title="'上传文件 - ' + batchName" :close-on-click-modal="false" :close-on-press-escape="false" width="600px">
    <div class="upload-area" :class="{ 'is-dragover': isDragover }" @dragover.prevent="isDragover = true" @dragleave.prevent="isDragover = false" @drop.prevent="handleDrop" @click="handleUploadClick">
      <input type="file" ref="fileInputRef" multiple accept=".docx,.pdf" @change="handleFileChange" style="display: none;">
      <div class="upload-icon">
        <el-icon size="48"><UploadFilled /></el-icon>
      </div>
      <div class="upload-text">点击或拖拽文件到此处上传</div>
      <div class="upload-hint">支持 .docx 和 .pdf 格式文件</div>
    </div>

    <div v-if="fileList.length > 0" class="file-list">
      <el-table :data="fileList" border size="small" style="width: 100%;">
        <el-table-column prop="name" label="文件名" header-align="center" align="center"></el-table-column>
        <el-table-column prop="size" label="大小" header-align="center" align="center" width="100">
          <template v-slot="scope">
            {{ formatSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" header-align="center" align="center" width="80"></el-table-column>
        <el-table-column label="操作" header-align="center" align="center" width="80">
          <template v-slot="scope">
            <el-button type="danger" link @click="removeFile(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from "vue";
import { UploadFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import baseService from "@/service/baseService";

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const isDragover = ref(false);
const uploading = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);
const batchId = ref(0);
const batchName = ref('');

interface FileItem {
  file: File;
  name: string;
  size: number;
  type: string;
}

const fileList = reactive<FileItem[]>([]);

const init = (row: any) => {
  visible.value = true;
  batchId.value = row.id;
  batchName.value = row.batchName;
  fileList.length = 0;
};

const handleDrop = (e: DragEvent) => {
  isDragover.value = false;
  const files = e.dataTransfer?.files;
  if (files) {
    addFiles(Array.from(files));
  }
};

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  const files = target.files;
  if (files) {
    addFiles(Array.from(files));
  }
};

const handleUploadClick = () => {
  fileInputRef.value?.click();
};

const addFiles = (files: File[]) => {
  for (const file of files) {
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    if (ext === '.docx' || ext === '.pdf') {
      fileList.push({
        file,
        name: file.name,
        size: file.size,
        type: ext === '.docx' ? 'DOCX' : 'PDF'
      });
    } else {
      ElMessage.warning(file.name + ' 格式不支持，仅支持 .docx 和 .pdf');
    }
  }
};

const removeFile = (index: number) => {
  fileList.splice(index, 1);
};

const formatSize = (size: number) => {
  if (size < 1024) {
    return size + ' B';
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(1) + ' KB';
  } else {
    return (size / (1024 * 1024)).toFixed(1) + ' MB';
  }
};

const submitUpload = () => {
  if (fileList.length === 0) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }

  uploading.value = true;
  const formData = new FormData();
  
  for (const item of fileList) {
    formData.append('files', item.file);
  }

  baseService.post('/demo/darkdetectbatch/' + batchId.value + '/upload', formData)
    .then((res: any) => {
      uploading.value = false;
      if (res.code === 0) {
        ElMessage.success(res.msg || `${fileList.length}个文件上传成功`);
        visible.value = false;
        emit('refreshDataList');
      } else {
        ElMessage.error(res.msg || '上传失败');
      }
    }).catch(() => {
      uploading.value = false;
      ElMessage.error('上传失败');
    });
};

defineExpose({
  init
});
</script>

<style scoped>
.upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-area.is-dragover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.upload-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.file-list {
  margin-top: 20px;
}
</style>
