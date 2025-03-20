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
        <div class="avatar-wrapper">
          <a-avatar style="cursor: pointer" :size="32">
            <template #icon>
              <user-outlined />
            </template>
            <div>{{ loginUser?.userName || "未登录" }}</div>
          </a-avatar>
          <div v-if="hasUnreadMessages" class="red-dot avatar-badge"></div>
        </div>
        <template #content>
          <template v-if="loginUser?.userName">
            <a-doption @click="router.push('/user/profile')">
              <template #icon>
                <user-outlined />
              </template>
              个人信息
            </a-doption>
            <a-doption @click="router.push('/user/messages')">
              <template #icon>
                <div class="message-icon-wrapper">
                  <message-outlined />
                  <div
                    v-if="hasUnreadMessages"
                    class="red-dot message-badge"
                  ></div>
                </div>
              </template>
              我的消息
            </a-doption>
            <a-doption @click="handleLogout">
              <template #icon>
                <logout-outlined />
              </template>
              退出登录
            </a-doption>
          </template>
          <template v-else>
            <a-doption @click="router.push('/user/login')">
              <template #icon>
                <login-outlined />
              </template>
              登录
            </a-doption>
            <a-doption @click="router.push('/user/register')">
              <template #icon>
                <user-add-outlined />
              </template>
              注册
            </a-doption>
          </template>
        </template>
      </a-dropdown>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, watch, onMounted, onUnmounted } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import {
  UserOutlined,
  MessageOutlined,
  LogoutOutlined,
  LoginOutlined,
  UserAddOutlined,
} from "@ant-design/icons-vue";
import { UserControllerService, PostControllerService } from "../../generated";
import { Message } from "@arco-design/web-vue";

const router = useRouter();
const route = useRoute();
const store = useStore();
const loginUser = computed(() => store.state.user?.loginUser);

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

// 添加未读消息相关的状态
const hasUnreadMessages = ref(false);
const unreadCount = ref("0");

// 检查未读消息
const checkUnreadMessages = async () => {
  if (!loginUser.value) return;

  try {
    const res = await PostControllerService.getUnreadUsingGet();
    if (res.code === 0 && res.data) {
      const count = parseInt(res.data);
      if (!isNaN(count)) {
        unreadCount.value = res.data;
        hasUnreadMessages.value = count > 0;
      }
    }
  } catch (error) {
    console.error("获取未读消息状态失败:", error);
  }
};

// 定期检查未读消息
let checkInterval: number | null = null;

onMounted(() => {
  // 首次检查
  checkUnreadMessages();
  // 每分钟检查一次
  checkInterval = window.setInterval(checkUnreadMessages, 60000);
});

onUnmounted(() => {
  if (checkInterval) {
    clearInterval(checkInterval);
    checkInterval = null;
  }
});

// 监听用户登录状态变化
watch(
  () => loginUser.value,
  (newUser) => {
    if (newUser) {
      checkUnreadMessages();
    } else {
      hasUnreadMessages.value = false;
      unreadCount.value = "0";
    }
  }
);

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

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-badge {
  position: absolute;
  top: -2px;
  right: -2px;
}

.message-icon-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.message-badge {
  position: absolute;
  top: -2px;
  right: -2px;
}

:deep(.arco-badge-dot) {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f53f3f;
  box-shadow: 0 0 0 2px #fff;
}

:deep(.arco-badge-text) {
  background-color: #f53f3f;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 16px;
  color: #fff;
  box-shadow: 0 0 0 1px #fff;
}

:deep(.arco-dropdown-option) {
  padding: 8px 12px;
}

:deep(.arco-dropdown-option:hover) {
  background-color: #f2f3f5;
}

.red-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f53f3f;
  box-shadow: 0 0 0 2px #fff;
}
</style>
