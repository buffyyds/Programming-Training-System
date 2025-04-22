<template>
  <div id="userLoginView">
    <h2 style="margin-bottom: 16px">用户注册</h2>
    <a-form
      style="max-width: 480px; margin: 0 auto"
      label-align="left"
      auto-label-width
      :model="registerForm"
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="registerForm.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="registerForm.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="registerForm.checkPassword"
          placeholder="请再次输入密码"
        />
      </a-form-item>
      <a-form-item field="userRole" label="用户角色">
        <a-radio-group v-model="registerForm.userRole">
          <a-radio value="user">学生</a-radio>
          <a-radio value="teacher">教师</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="registerForm.userRole === 'teacher'"
        field="adminCode"
        label="教师码"
      >
        <a-input v-model="registerForm.adminCode" readonly />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" style="width: 120px">
            注册
          </a-button>
          <a-button
            type="outline"
            @click="router.push('/user/login')"
            style="width: 120px"
          >
            登录
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from "vue";
import {
  UserControllerService,
  UserRegisterRequest,
  UserLoginRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

/**
 * 生成随机教师码
 */
const generateAdminCode = () => {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let code = "";
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
};

/**
 * 表单信息
 */
const registerForm = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  userRole: "user", // 默认为学生
  adminCode: "", // 教师码
} as UserRegisterRequest);

const loginForm = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);

const router = useRouter();
const store = useStore();

// 监听用户角色变化
watch(
  () => registerForm.userRole,
  (newRole) => {
    if (newRole === "teacher") {
      registerForm.adminCode = generateAdminCode();
    } else {
      registerForm.adminCode = "";
    }
  }
);

/**
 * 提交表单
 * @param data
 */
const handleSubmit = async () => {
  const registRes = await UserControllerService.userRegisterUsingPost(
    registerForm
  );
  loginForm.userAccount = registerForm.userAccount;
  loginForm.userPassword = registerForm.userPassword;
  const loginRes = await UserControllerService.userLoginUsingPost(loginForm);
  // 注册成功，自动登录刚刚注册成功的用户，并跳转到主页
  if (registRes.code === 0) {
    message.success("注册成功！");
    if (loginRes.code === 0) {
      await store.dispatch("user/getLoginUser");
    } else {
      message.error("登录失败，" + loginRes.message);
    }
    router.push({
      path: "/",
      replace: true,
    });
  } else {
    message.error("注册失败，" + registRes.message);
  }
};
</script>

<style scoped>
:deep(.arco-radio-group) {
  display: flex;
  gap: 24px;
}

:deep(.arco-radio) {
  margin-right: 0;
}
</style>
