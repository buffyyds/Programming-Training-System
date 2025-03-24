<template>
  <div class="progress-container">
    <a-card class="progress-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <a-button type="text" @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
            </a-button>
            <icon-book class="title-icon" />
            {{ questionTitle }} - 学生完成情况
          </a-space>
        </div>
      </template>

      <a-table
        :data="studentList"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #columns>
          <a-table-column title="学生用户名" data-index="studentName">
            <template #cell="{ record }">
              {{ record.studentName }}
            </template>
          </a-table-column>
          <a-table-column title="完成情况" data-index="isCompletion">
            <template #cell="{ record }">
              <a-space>
                <a-tag :color="record.isCompletion ? 'green' : 'red'">
                  <template #icon>
                    <icon-check-circle-fill v-if="record.isCompletion" />
                    <icon-close-circle-fill v-else />
                  </template>
                  {{ record.isCompletion ? "已完成" : "未完成" }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconBook,
  IconArrowLeft,
  IconCheckCircleFill,
  IconCloseCircleFill,
} from "@arco-design/web-vue/es/icon";
import { useRouter, useRoute } from "vue-router";
import { useStore } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { QuestionControllerService } from "../../../generated";

const router = useRouter();
const route = useRoute();
const store = useStore();
const loading = ref(false);
const questionTitle = ref("");
const studentList = ref([]);
const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 获取学生完成情况
const loadStudentProgress = async () => {
  loading.value = true;
  try {
    const res =
      await QuestionControllerService.getStudentCompletionVoByQuestionIdUsingGet(
        route.params.id as string
      );
    if (res.code === 0) {
      studentList.value = res.data;
      pagination.value.total = res.data.length;
    }
  } catch (error) {
    console.error("获取学生完成情况失败:", error);
    Message.error("获取学生完成情况失败");
  } finally {
    loading.value = false;
  }
};

// 页面变化
const onPageChange = (current: number) => {
  pagination.value.current = current;
  loadStudentProgress();
};

// 返回上一页
const goBack = () => {
  router.back();
};

onMounted(async () => {
  // 检查用户权限
  const currentUser = store.state.user.loginUser;
  if (!currentUser || currentUser.userRole !== ACCESS_ENUM.ADMIN) {
    Message.error("无权限访问");
    router.push("/");
    return;
  }

  // 获取题目信息
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    route.params.id as string
  );
  if (res.code === 0) {
    questionTitle.value = res.data.title;
  }

  await loadStudentProgress();
});
</script>

<style scoped>
.progress-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.progress-card {
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

:deep(.arco-table-th) {
  background-color: #f5f5f5;
}

:deep(.arco-table-tr) {
  cursor: pointer;
}

:deep(.arco-table-tr:hover) {
  background-color: #f5f5f5;
}

:deep(.arco-tag) {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
