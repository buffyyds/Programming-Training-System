<template>
  <div class="question-container">
    <a-card class="question-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <a-button type="text" @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
            </a-button>
            <icon-book class="title-icon" />
            学生完成情况
          </a-space>
        </div>
      </template>

      <a-table
        :data="questionList"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #columns>
          <a-table-column title="题目名称" data-index="title">
            <template #cell="{ record }">
              <a-link @click="goToDetail(record.id)">{{ record.title }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="标签" data-index="tags">
            <template #cell="{ record }">
              <a-space wrap>
                <a-tag v-for="tag in record.tags" :key="tag" color="blue">
                  {{ tag }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>
          <a-table-column title="通过率" data-index="acceptedPercent">
            <template #cell="{ record }">
              {{ (record.acceptedPercent * 100).toFixed(0) }}%（{{
                getCompletedStudentNum(
                  record.acceptedPercent,
                  record.studentNum
                )
              }}/{{ record.studentNum }}）
            </template>
          </a-table-column>
          <a-table-column title="提交次数" data-index="submitNum">
            <template #cell="{ record }">
              {{ record.submitNum }}
            </template>
          </a-table-column>
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-button type="primary" @click="goToDetail(record.id)">
                详情
              </a-button>
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
import { IconBook, IconArrowLeft } from "@arco-design/web-vue/es/icon";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { QuestionControllerService } from "../../../generated";

const router = useRouter();
const store = useStore();
const loading = ref(false);
const questionList = ref([]);
const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 获取题目列表
const loadQuestions = async () => {
  loading.value = true;
  try {
    const res =
      await QuestionControllerService.listStudentProgressVoByPageUsingPost({
        current: pagination.value.current,
        pageSize: pagination.value.pageSize,
      });
    if (res.code === 0) {
      questionList.value = res.data.records;
      pagination.value.total = res.data.total;
    }
  } catch (error) {
    console.error("获取题目列表失败:", error);
    Message.error("获取题目列表失败");
  } finally {
    loading.value = false;
  }
};

// 计算完成学生人数
const getCompletedStudentNum = (
  acceptedPercent: number,
  studentNum: number
) => {
  return Math.round(acceptedPercent * studentNum);
};

// 页面变化
const onPageChange = (current: number) => {
  pagination.value.current = current;
  loadQuestions();
};

// 跳转到详情页
const goToDetail = (id: string) => {
  router.push(`/student_progress/${id}`);
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
  await loadQuestions();
});
</script>

<style scoped>
.question-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.question-card {
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
</style>
