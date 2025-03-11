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
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px">
          注册
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import {
  UserControllerService,
  UserRegisterRequest,
  UserLoginRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

/**
 * 表单信息
 */
const registerForm = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
} as UserRegisterRequest);

const loginForm = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);

const router = useRouter();
const store = useStore();

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
