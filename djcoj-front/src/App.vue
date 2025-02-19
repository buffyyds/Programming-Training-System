<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<style>
#app {
}
</style>
<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout.vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import AccessEnum from "@/access/accessEnum";
import { onMounted } from "vue";

/**
 * 全局初始化函数，有全局单次调用的代码，都可以写到这里
 */
const doInit = () => {
  console.log("hello 欢迎来到我的项目");
};

onMounted(() => {
  doInit();
});

const router = useRouter();
const store = useStore();
router.beforeEach((to, from, next) => {
  //console.log(to);
  //如果没有权限，跳转到没有权限页面
  if (to.meta?.access === AccessEnum.ADMIN) {
    if (store.state.user.loginUser?.userRole !== AccessEnum.ADMIN) {
      next("/noAuth");
      return;
    }
  }
  next();
});
</script>
