<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <a-button type="text" @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
            </a-button>
            <icon-user class="title-icon" />
            个人信息
          </a-space>
        </div>
      </template>

      <a-form :model="userForm" @submit="handleSubmit" class="form-section">
        <a-form-item field="userName" label="用户名">
          <a-input v-model="userForm.userName" placeholder="请输入用户名" />
        </a-form-item>

        <a-form-item field="userProfile" label="个人简介">
          <a-textarea
            v-model="userForm.userProfile"
            placeholder="请输入个人简介"
            :max-length="200"
            show-word-limit
          />
        </a-form-item>

        <a-form-item field="phone" label="手机号">
          <a-input v-model="userForm.userPhone" placeholder="请输入手机号" />
        </a-form-item>

        <a-form-item field="password" label="密码">
          <a-button
            type="primary"
            @click="showChangePassword = !showChangePassword"
            class="change-password-btn"
          >
            {{ showChangePassword ? "取消修改" : "修改密码" }}
          </a-button>
        </a-form-item>

        <div v-if="showChangePassword" class="password-change-section">
          <a-form-item field="newPassword" label="新密码">
            <a-input-password
              v-model="passwordForm.newPassword"
              placeholder="请输入新密码"
              allow-clear
            />
          </a-form-item>

          <a-form-item field="confirmPassword" label="确认密码">
            <a-input-password
              v-model="passwordForm.confirmPassword"
              placeholder="请确认新密码"
              allow-clear
            />
          </a-form-item>
        </div>

        <template v-if="userForm.userRole === 'admin'">
          <a-form-item label="教师身份">
            <a-tag color="blue">已认证教师</a-tag>
          </a-form-item>
        </template>
        <template v-else>
          <template v-if="userForm.adminCode">
            <a-form-item label="绑定教师">
              <a-tag color="green">已绑定教师：{{ teacherName }}</a-tag>
            </a-form-item>
          </template>
          <template v-else>
            <a-form-item field="adminCode" label="绑定教师">
              <a-input
                v-model="userForm.adminCode"
                placeholder="请输入教师注册码"
                allow-clear
              />
            </a-form-item>
          </template>
        </template>

        <a-form-item>
          <a-button type="primary" html-type="submit" long> 保存修改</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { Message } from "@arco-design/web-vue";
import { useStore } from "vuex";
import { IconUser, IconArrowLeft } from "@arco-design/web-vue/es/icon";
import {
  UserControllerService,
  TasControllerService,
} from "../../../generated";
import { useRouter } from "vue-router";

const router = useRouter();
const store = useStore();
const showChangePassword = ref(false);
const teacherName = ref("");

const userForm = ref({
  userName: "",
  userProfile: "",
  userPhone: "",
  adminCode: "", // 存储教师注册码
  userPassword: "",
  userRole: "", // 添加用户角色字段
});

const passwordForm = ref({
  newPassword: "",
  confirmPassword: "",
});

// 计算属性：是否显示绑定教师
const showBindTeacher = computed(() => {
  return userForm.value.userRole === "user" && !userForm.value.adminCode;
});

// 获取教师信息
const getTeacherInfo = async (adminCode: string | undefined) => {
  if (!adminCode) return;
  try {
    const res = await TasControllerService.getTeacherUsingGet();
    if (res.code === 0 && res.data) {
      teacherName.value = res.data.userName;
    }
  } catch (error) {
    console.error("获取教师信息失败:", error);
  }
};

onMounted(async () => {
  const currentUser = store.state.user.loginUser;
  console.log("获取登录用户信息", currentUser);
  if (currentUser) {
    userForm.value = {
      userName: currentUser.userName || "",
      userProfile: currentUser.userProfile || "",
      userPhone: currentUser.userPhone || "",
      adminCode: currentUser.adminCode || "",
      userPassword: currentUser.userPassword || "",
      userRole: currentUser.userRole || "",
    };

    // 如果有adminCode，获取教师信息
    if (userForm.value.adminCode && userForm.value.adminCode !== "") {
      await getTeacherInfo(userForm.value.adminCode);
    }
  }
});

const handleSubmit = async () => {
  try {
    if (showChangePassword.value) {
      if (
        passwordForm.value.newPassword !== passwordForm.value.confirmPassword
      ) {
        Message.error("两次输入的密码不一致");
        return;
      }
    }

    const updateData = {
      ...userForm.value,
      userPassword: showChangePassword.value
        ? passwordForm.value.newPassword
        : undefined,
    };

    const res = await UserControllerService.updateMyUserUsingPost(updateData);
    if (res.code == 0) {
      Message.success("更新成功");
    } else {
      Message.error("更新失败" + res.message);
    }
    store.commit("user/updateUser", {
      ...store.state.user.loginUser,
      ...userForm.value,
    });

    // 重置密码相关状态
    showChangePassword.value = false;
    passwordForm.value.newPassword = "";
    passwordForm.value.confirmPassword = "";
  } catch (error) {
    Message.error("更新失败");
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.title-icon {
  margin-right: 8px;
  font-size: 20px;
}

.password-change-section {
  margin-top: 16px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.change-password-btn {
  width: 120px;
}

:deep(.arco-tag) {
  font-size: 14px;
  padding: 4px 8px;
}
</style>
