<template>
  <div class="bind-teacher-container">
    <a-card class="bind-teacher-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <a-button type="text" @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
            </a-button>
            <icon-user class="title-icon" />
            绑定教师
          </a-space>
        </div>
      </template>

      <!-- 已绑定教师信息 -->
      <div v-if="isBound && boundTeacher" class="bound-teacher-info">
        <a-alert type="success" show-icon>
          <template #icon>
            <icon-check-circle />
          </template>
          <template #title>已绑定教师</template>
          <div class="teacher-info">
            <!--            <a-avatar :size="32">-->
            <!--              <template #icon>-->
            <!--                <icon-user />-->
            <!--              </template>-->
            <!--            </a-avatar>-->
            <div class="teacher-detail">
              <div class="teacher-name">{{ boundTeacher?.userName }}</div>
              <div class="teacher-id">
                教师电话: {{ boundTeacher?.userPhone }}
              </div>
            </div>
            <div class="action-buttons">
              <a-button type="primary" status="danger" @click="handleUnbind">
                解绑
              </a-button>
            </div>
          </div>
        </a-alert>
      </div>

      <!-- 未绑定提示 -->
      <div v-else class="unbound-tip">
        <a-alert type="warning" show-icon>
          <template #icon>
            <icon-exclamation-circle />
          </template>
          <template #title>请绑定教师</template>
          <div>绑定后即可查看题目列表</div>
        </a-alert>
      </div>

      <!-- 调试信息 -->
      <div style="display: none">
        isBound: {{ isBound }}<br />
        boundTeacher: {{ boundTeacher }}
      </div>

      <!-- 搜索框 -->
      <div class="search-box">
        <a-input-search
          v-model="searchKeyword"
          placeholder="搜索教师"
          allow-clear
          @search="handleSearch"
        />
      </div>

      <!-- 教师列表 -->
      <a-table
        :data="filteredTeacherList"
        :loading="loading"
        :pagination="false"
        :bordered="false"
      >
        <template #columns>
          <a-table-column title="教师账号" data-index="userName" />
          <a-table-column title="注册时间" data-index="createTime">
            <template #cell="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" v-if="!isBound">
            <template #cell="{ record }">
              <a-button type="primary" @click="handleBind(record)">
                绑定
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { Message } from "@arco-design/web-vue";
import { useStore } from "vuex";
import {
  IconUser,
  IconArrowLeft,
  IconCheckCircle,
  IconExclamationCircle,
} from "@arco-design/web-vue/es/icon";
import { useRouter } from "vue-router";
import { UserControllerService } from "../../../generated";
import dayjs from "dayjs";

const router = useRouter();
const store = useStore();
const loading = ref(false);
const searchKeyword = ref("");
const teacherList = ref<any[]>([]);
const isBound = ref(false);
const boundTeacher = ref<{
  id: string;
  userName: string;
  userPhone: string;
} | null>(null);

// 过滤后的教师列表
const filteredTeacherList = computed(() => {
  if (!searchKeyword.value) return teacherList.value;
  return teacherList.value.filter((teacher) =>
    teacher.userName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  );
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm");
};

// 获取绑定状态
const getBindStatus = async () => {
  try {
    const res = await UserControllerService.isBindTeacherUsingGet();
    console.log("获取绑定状态返回数据:", res);
    if (res.code === 0 && res.data) {
      const data = res.data;
      console.log("data内容:", data);
      if (data.true) {
        isBound.value = true;
        // 确保赋值时包含所有必要的属性
        if (data.true.id && data.true.userName) {
          boundTeacher.value = {
            id: String(data.true.id),
            userName: data.true.userName,
            userPhone: data.true.userPhone,
          };
        }
        console.log("已绑定教师信息:", boundTeacher.value);
      } else {
        isBound.value = false;
        boundTeacher.value = null;
      }
    }
  } catch (error) {
    console.error("获取绑定状态失败:", error);
    Message.error("获取绑定状态失败");
  }
};

// 获取教师列表
const getTeacherList = async () => {
  loading.value = true;
  try {
    const res = await UserControllerService.getAllTeacherListUsingGet();
    if (res.code === 0 && res.data) {
      teacherList.value = res.data;
    }
  } catch (error) {
    console.error("获取教师列表失败:", error);
    Message.error("获取教师列表失败");
  } finally {
    loading.value = false;
  }
};

// 绑定教师
const handleBind = async (teacher: any) => {
  try {
    const res = await UserControllerService.bindTeacherUsingPost({
      id: teacher.id,
      userName: teacher.userName,
    });
    if (res.code === 0) {
      Message.success("绑定成功");
      // 更新绑定状态
      await getBindStatus();
      // 更新用户信息
      await store.dispatch("user/getLoginUser");
    } else {
      Message.error("绑定失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("绑定失败：" + error.message);
  }
};

// 解绑教师
const handleUnbind = async () => {
  try {
    const res = await UserControllerService.unBindTeacherUsingGet();
    if (res.code === 0) {
      Message.success("解绑成功");
      // 更新绑定状态
      isBound.value = false;
      boundTeacher.value = null;
      // 更新用户信息
      await store.dispatch("user/getLoginUser");
      // 重新获取教师列表
      await getTeacherList();
    } else {
      Message.error("解绑失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("解绑失败：" + error.message);
  }
};

// 搜索处理
const handleSearch = (value: string) => {
  searchKeyword.value = value;
};

// 返回上一页
const goBack = () => {
  router.back();
};

onMounted(async () => {
  await getBindStatus();
  await getTeacherList();
});
</script>

<style scoped>
.bind-teacher-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.bind-teacher-card {
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

.bound-teacher-info,
.unbound-tip {
  margin-bottom: 20px;
}

.teacher-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.teacher-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.teacher-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-1);
}

.teacher-id {
  font-size: 14px;
  color: var(--color-text-3);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.search-box {
  margin-bottom: 20px;
}

:deep(.arco-table) {
  background-color: transparent;
}

:deep(.arco-table-th) {
  background-color: transparent;
}

:deep(.arco-table-tr) {
  background-color: transparent;
}

:deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-2);
}
</style>
