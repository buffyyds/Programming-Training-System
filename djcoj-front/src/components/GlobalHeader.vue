<template>
  <a-row id="globalHeader" style="margin-bottom: 16px" align="center">
    <a-col flex="100px">
      <div>
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div>
            <img class="logo" src="../assets/logo2.png" />
          </div>
        </a-menu-item>
      </div>
    </a-col>
    <a-col flex="auto">
      <div>
        <a-menu
          mode="horizontal"
          :selected-keys="selectedKeys"
          @menu-item-click="doMenuClick"
        >
          <!--for循环遍历路由，动态的获取路由文件中的路由信息-->
          <a-menu-item v-for="item in visibleRoutes" :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </a-menu>
      </div>
    </a-col>
    <a-col flex="100px">
      <a-dropdown trigger="hover">
        <a-avatar style="cursor: pointer" :size="32">
          <template #icon>
            <user-outlined />
          </template>
          <div>{{ store.state.user?.loginUser?.userName ?? "未登录" }}</div>
        </a-avatar>
        <template #content>
          <a-doption @click="router.push('/user/profile')">
            <template #icon><user-outlined /></template>
            个人信息
          </a-doption>
          <a-doption @click="router.push('/user/messages')">
            <template #icon><message-outlined /></template>
            我的消息
          </a-doption>
          <a-doption @click="handleLogout">
            <template #icon><logout-outlined /></template>
            退出登录
          </a-doption>
        </template>
      </a-dropdown>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, watch } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import {
  UserOutlined,
  MessageOutlined,
  LogoutOutlined,
} from "@ant-design/icons-vue";
import { UserControllerService } from "../../generated";
import { Message } from "@arco-design/web-vue";

const router = useRouter();
const route = useRoute();
const store = useStore();
const loginUser = store.state.user.loginUser;

console.log("loginUser", loginUser);
// 展示在菜单的路由数组
//computed是一个计算属性，用于计算一个新的值，当依赖的值发生变化时，会重新计算
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

// 动态变量，用于记录当前选中的菜单项
const selectedKeys = ref([route.path]);

// 监听路由变化，更新选中的菜单项
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath];
  }
);

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const handleLogout = async () => {
  try {
    await UserControllerService.userLogoutUsingPost();
    Message.success("退出登录成功");
    // 清除用户状态
    store.commit("user/updateUser", null);
    await router.push("/user/login");
  } catch (error) {
    Message.error("退出登录失败");
  }
};

// setTimeout(() => {
//   store.dispatch("user/getLoginUser", {
//     userName: "djc管理员",
//     userRole: ACCESS_ENUM.ADMIN,
//   });
// }, 3000);
</script>

<style scoped>
.logo {
  height: 80px;
}
</style>
