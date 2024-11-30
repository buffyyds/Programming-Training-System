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
            <img class="logo" src="../assets/logo.png" />
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
          <a-menu-item v-for="item in routes" :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </a-menu>
      </div>
    </a-col>
    <a-col flex="100px">
      <!--记录用户登录信息-->
      <div>{{ store.state.user?.loginUser?.name ?? "未登录" }}</div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRoute, useRouter } from "vue-router";
import { ref } from "vue";
import { useStore } from "vuex";

const router = useRouter();

// 动态变量，用于记录当前选中的菜单项，在刷新的时候能够保持选中状态（默认主页）
const selectedKeys = ref(["/"]);
// 路由跳转后，更新选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const store = useStore();
setTimeout(() => {
  store.dispatch("user/getLoginUser", {});
}, 3000);
</script>

<style scoped>
.logo {
  height: 80px;
}
</style>
