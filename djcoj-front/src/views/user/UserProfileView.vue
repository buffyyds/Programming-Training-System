<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <template #title>
        <div class="card-title">
          <icon-user class="title-icon" />
          个人信息
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

        <a-form-item field="userRole" label="权限">
          <div class="role-field">
            <a-tag :color="userForm.userRole === 'admin' ? 'blue' : 'green'">
              {{ userForm.userRole === "admin" ? "管理员" : "普通用户" }}
            </a-tag>
            <a-button
              type="text"
              @click="showAdminCodeInput = !showAdminCodeInput"
            >
              申请管理员权限
            </a-button>
          </div>
        </a-form-item>

        <a-form-item v-if="showAdminCodeInput" field="adminCode" label="管理码">
          <a-input-password
            v-model="adminCode"
            placeholder="请输入管理码"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" long> 保存修改</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { useStore } from "vuex";
import { IconUser } from "@arco-design/web-vue/es/icon";
import { UserControllerService } from "../../../generated";

const store = useStore();
const showChangePassword = ref(false);
const showAdminCodeInput = ref(false);
const adminCode = ref("");

const userForm = ref({
  userName: "",
  userProfile: "",
  userPhone: "",
  userRole: "",
  userPassword: "",
});

const passwordForm = ref({
  newPassword: "",
  confirmPassword: "",
});

onMounted(async () => {
  const currentUser = store.state.user.loginUser;
  console.log("获取登录用户信息", currentUser);
  if (currentUser) {
    userForm.value = {
      userName: currentUser.userName || "",
      userProfile: currentUser.userProfile || "",
      userPhone: currentUser.userPhone || "",
      userRole: currentUser.userRole || "user",
      //userPassword: currentUser.userPassword || "",
    };
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

    if (showAdminCodeInput.value) {
      // todo 这里可以有更好的逻辑，但是这个管理员申请不是很重要，所以就这样了，有时间再完善
      if (adminCode.value !== "djcyyds") {
        Message.error("管理码错误");
        return;
      } else {
        userForm.value.userRole = "admin";
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
    showAdminCodeInput.value = false;
    passwordForm.value.newPassword = "";
    passwordForm.value.confirmPassword = "";
    adminCode.value = "";
  } catch (error) {
    Message.error("更新失败");
  }
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

.role-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.change-password-btn {
  width: 120px;
}
</style>
