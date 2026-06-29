<template>
  <div class="dark-login">
    <div class="dark-login-bg">
      <div class="dark-login-bg-shape dark-login-bg-shape-1"></div>
      <div class="dark-login-bg-shape dark-login-bg-shape-2"></div>
      <div class="dark-login-bg-shape dark-login-bg-shape-3"></div>
    </div>

    <div class="dark-login-container">
      <div class="dark-login-left">
        <div class="dark-login-brand">
          <div class="dark-login-brand-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L4 5V11.09C4 16.14 7.41 20.85 12 22C16.59 20.85 20 16.14 20 11.09V5L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="dark-login-brand-title">暗标检测系统</h1>
          <p class="dark-login-brand-desc">文档智能检测与敏感信息识别平台</p>
        </div>
        <div class="dark-login-features">
          <div class="dark-login-feature">
            <div class="dark-login-feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M18.5 2.50001C18.8978 2.10219 19.4374 1.87869 20 1.87869C20.5626 1.87869 21.1022 2.10219 21.5 2.50001C21.8978 2.89784 22.1213 3.4374 22.1213 4.00001C22.1213 4.56262 21.8978 5.10219 21.5 5.50001L12 15L8 16L9 12L18.5 2.50001Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="dark-login-feature-text">
              <h3>敏感词检测</h3>
              <p>快速识别文档中的敏感词汇和不当表述</p>
            </div>
          </div>
          <div class="dark-login-feature">
            <div class="dark-login-feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 13H16M8 17H12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="dark-login-feature-text">
              <h3>文档解析</h3>
              <p>支持PDF、Word等格式的深度内容分析</p>
            </div>
          </div>
          <div class="dark-login-feature">
            <div class="dark-login-feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 17H15M9 13H15M9 9H10M12 3L3 8V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V8L12 3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M7 17L11 17M7 13L13 13M7 9L9 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="dark-login-feature-text">
              <h3>批量处理</h3>
              <p>一键批量检测，自动生成详细报告</p>
            </div>
          </div>
        </div>
      </div>

      <div class="dark-login-right">
        <div class="dark-login-form-wrapper">
          <div class="dark-login-form-header">
            <h2>用户登录</h2>
            <p>请输入您的账号信息</p>
          </div>
          <el-form ref="formRef" :status-icon="true" :model="login" :rules="rules" @keyup.enter="onLogin" class="dark-login-form">
            <el-form-item prop="username">
              <div class="dark-login-input-wrapper">
                <el-icon class="dark-login-input-icon"><User /></el-icon>
                <el-input v-model="login.username" placeholder="请输入用户名" autocomplete="off"></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="dark-login-input-wrapper">
                <el-icon class="dark-login-input-icon"><Lock /></el-icon>
                <el-input v-model="login.password" placeholder="请输入密码" autocomplete="off" show-password></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="captcha">
              <div class="dark-login-input-wrapper dark-login-captcha-wrapper">
                <el-icon class="dark-login-input-icon"><CircleCheck /></el-icon>
                <el-input v-model="login.captcha" placeholder="请输入验证码" autocomplete="off"></el-input>
                <img class="dark-login-captcha-img" :src="state.captchaUrl" @click="onRefreshCode" alt="验证码" />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :disabled="state.loading" @click="onLogin" class="dark-login-submit-btn">
                <span v-if="!state.loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <div class="dark-login-footer">
      <p>暗标检测系统 {{ state.year }} © 版权所有</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import { CacheToken } from "@/constants/cacheKey";
import baseService from "@/service/baseService";
import { setCache } from "@/utils/cache";
import { ElMessage } from "element-plus";
import { User, Lock, CircleCheck } from "@element-plus/icons-vue";
import { getUuid } from "@/utils/utils";
import app from "@/constants/app";
import { useAppStore } from "@/store";
import { useRouter } from "vue-router";

const store = useAppStore();
const router = useRouter();

const state = reactive({
  captchaUrl: "",
  loading: false,
  year: new Date().getFullYear()
});

const login = reactive({ username: "", password: "", captcha: "", uuid: "" });

onMounted(() => {
  store.logout();
  getCaptchaUrl();
});

const formRef = ref();

const rules = ref({
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }]
});

const getCaptchaUrl = () => {
  login.uuid = getUuid();
  state.captchaUrl = `${app.api}/captcha?uuid=${login.uuid}`;
};

const onRefreshCode = () => {
  getCaptchaUrl();
};

const onLogin = () => {
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      state.loading = true;
      baseService
        .post("/login", login)
        .then((res) => {
          state.loading = false;
          if (res.code === 0) {
            setCache(CacheToken, res.data, true);
            ElMessage.success("登录成功");
            router.push("/");
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(() => {
          state.loading = false;
          onRefreshCode();
        });
    }
  });
};
</script>

<style lang="less" scoped>
.dark-login {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  &-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
    pointer-events: none;

    &-shape {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      opacity: 0.3;

      &-1 {
        width: 600px;
        height: 600px;
        background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
        top: -200px;
        left: -100px;
        animation: float 8s ease-in-out infinite;
      }

      &-2 {
        width: 500px;
        height: 500px;
        background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
        bottom: -150px;
        right: -100px;
        animation: float 10s ease-in-out infinite reverse;
      }

      &-3 {
        width: 300px;
        height: 300px;
        background: linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%);
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        animation: pulse 6s ease-in-out infinite;
      }
    }
  }

  &-container {
    position: relative;
    z-index: 1;
    display: flex;
    width: 1000px;
    max-width: 96%;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 24px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    overflow: hidden;
    animation: slideUp 0.8s ease-out;
  }

  &-left {
    flex: 1;
    background: linear-gradient(135deg, #1e3a5f 0%, #0f2744 100%);
    padding: 60px 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;

    @media only screen and (max-width: 768px) {
      display: none;
    }
  }

  &-brand {
    text-align: center;
    margin-bottom: 50px;

    &-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 24px;
      background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 10px 30px rgba(59, 130, 246, 0.4);

      svg {
        width: 48px;
        height: 48px;
        color: #fff;
      }
    }

    &-title {
      font-size: 32px;
      font-weight: 600;
      color: #fff;
      margin: 0 0 12px;
      letter-spacing: 2px;
    }

    &-desc {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.7);
      margin: 0;
    }
  }

  &-features {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  &-feature {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      transform: translateX(8px);
    }

    &-icon {
      flex-shrink: 0;
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;

      svg {
        width: 20px;
        height: 20px;
        color: #fff;
      }
    }

    &-text {
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #fff;
        margin: 0 0 4px;
      }

      p {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.6);
        margin: 0;
      }
    }
  }

  &-right {
    flex: 1;
    padding: 60px 50px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &-form-wrapper {
    width: 100%;
    max-width: 320px;
  }

  &-form-header {
    margin-bottom: 40px;
    text-align: center;

    h2 {
      font-size: 28px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 8px;
    }

    p {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }
  }

  &-form {
    .el-form-item {
      margin-bottom: 24px;
    }
  }

  &-input-wrapper {
    position: relative;
    width: 100%;

    .dark-login-input-icon {
      position: absolute;
      left: 14px;
      top: 50%;
      transform: translateY(-50%);
      color: #94a3b8;
      z-index: 2;
      font-size: 16px;
      pointer-events: none;
    }

    .el-input {
      --el-input-bg-color: #f8fafc;
      --el-input-border-color: #e2e8f0;
      --el-input-hover-border-color: #3b82f6;
      --el-input-focus-border-color: #3b82f6;

      // 深度选择器，确保隐藏Element Plus内置图标
      :deep(.el-input__wrapper) {
        padding: 4px 14px 4px 42px;
        border-radius: 10px;
        box-shadow: none !important;
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        transition: all 0.3s ease;

        &:hover {
          border-color: #cbd5e1;
        }

        &:focus-within {
          border-color: #3b82f6;
          background: #fff;
        }
      }

      :deep(.el-input__inner) {
        height: 42px;
        font-size: 15px;
        color: #1e293b;

        &::placeholder {
          color: #94a3b8;
        }
      }

      // 隐藏Element Plus自带的prefix图标区域
      :deep(.el-input__prefix) {
        display: none !important;
      }

      // 确保suffix图标（如show-password的眼睛图标）正常显示
      :deep(.el-input__suffix) {
        margin-right: 0;
      }
    }
  }

  &-captcha-wrapper {
    .el-input {
      :deep(.el-input__wrapper) {
        padding-right: 120px;
      }
    }
  }

  &-captcha-img {
    position: absolute;
    right: 4px;
    top: 50%;
    transform: translateY(-50%);
    height: 34px;
    border-radius: 6px;
    cursor: pointer;
    transition: opacity 0.3s ease;

    &:hover {
      opacity: 0.8;
    }
  }

  &-submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 4px;
    border-radius: 10px;
    border: none;
    background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
    transition: all 0.3s ease;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(59, 130, 246, 0.5);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
  }

  &-footer {
    position: absolute;
    bottom: 24px;
    text-align: center;
    color: rgba(255, 255, 255, 0.5);
    font-size: 13px;
    z-index: 1;

    p {
      margin: 0;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-30px);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.3;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media only screen and (max-width: 768px) {
  .dark-login {
    &-container {
      flex-direction: column;
    }

    &-right {
      padding: 40px 30px;
    }

    &-form-header {
      margin-bottom: 30px;

      h2 {
        font-size: 24px;
      }
    }
  }
}
</style>
