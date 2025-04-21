<template>
  <div class="question-container">
    <a-page-header
      title="学生完成情况"
      subtitle="查看和分析学生的题目完成情况"
      :show-back="false"
    >
      <!--      <template #extra>-->
      <!--        <a-button type="primary" @click="goBack">-->
      <!--          <template #icon>-->
      <!--            <icon-arrow-left />-->
      <!--          </template>-->
      <!--          返回-->
      <!--        </a-button>-->
      <!--      </template>-->
    </a-page-header>

    <a-row :gutter="16" class="statistics-cards">
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>总题目数</template>
          <div class="stat-value">{{ questionList.length }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>平均通过率</template>
          <div class="stat-value">{{ averageAcceptedRate }}%</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>总提交次数</template>
          <div class="stat-value">
            {{ totalSubmitCount }}
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>平均完成人数</template>
          <div class="stat-value">
            {{ averageCompletedStudents }}
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="table-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <icon-book class="title-icon" />
            题目列表
          </a-space>
        </div>
      </template>

      <a-table
        :data="questionList"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
        :bordered="false"
        :stripe="true"
      >
        <template #columns>
          <a-table-column title="题目名称" data-index="title" width="30%">
            <template #cell="{ record }">
              <a-link @click="goToDetail(record.id)">{{ record.title }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="标签" data-index="tags" width="20%">
            <template #cell="{ record }">
              <a-space wrap>
                <a-tag v-for="tag in record.tags" :key="tag" color="blue">
                  {{ tag }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>
          <a-table-column
            title="通过率"
            data-index="acceptedPercent"
            width="20%"
          >
            <template #cell="{ record }">
              <a-progress
                :percent="record.acceptedPercent * 100"
                :stroke-width="20"
                :stroke-color="getProgressColor(record.acceptedPercent)"
                :status="record.acceptedPercent < 0.5 ? 'danger' : undefined"
              >
                <template #text>
                  {{ (record.acceptedPercent * 100).toFixed(0) }}%
                </template>
              </a-progress>
              <div class="progress-text">
                {{
                  getCompletedStudentNum(
                    record.acceptedPercent,
                    record.studentNum
                  )
                }}/{{ record.studentNum }}
              </div>
            </template>
          </a-table-column>
          <a-table-column title="提交次数" data-index="submitNum" width="15%">
            <template #cell="{ record }">
              <a-tag color="orange">{{ record.submitNum }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" width="15%" fixed="right">
            <template #cell="{ record }">
              <a-button type="primary" @click="goToDetail(record.id)">
                <template #icon>
                  <icon-eye />
                </template>
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
import { ref, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconBook, IconArrowLeft, IconEye } from "@arco-design/web-vue/es/icon";
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

// 计算统计数据
const averageAcceptedRate = computed(() => {
  if (questionList.value.length === 0) return 0;
  const total = questionList.value.reduce(
    (sum, item) => sum + item.acceptedPercent,
    0
  );
  return ((total / questionList.value.length) * 100).toFixed(1);
});

const totalSubmitCount = computed(() => {
  return questionList.value.reduce((sum, item) => sum + item.submitNum, 0);
});

const averageCompletedStudents = computed(() => {
  if (questionList.value.length === 0) return 0;
  const total = questionList.value.reduce(
    (sum, item) =>
      sum + getCompletedStudentNum(item.acceptedPercent, item.studentNum),
    0
  );
  return Math.round(total / questionList.value.length);
});

// 获取进度条颜色
const getProgressColor = (percent: number) => {
  if (percent >= 0.8) return "#00B42A"; // 绿色
  if (percent >= 0.5) return "#FF7D00"; // 橙色
  return "#F53F3F"; // 红色
};

// 获取题目列表
const loadQuestions = async () => {
  loading.value = true;
  try {
    const res =
      await QuestionControllerService.listStudentProgressVoByPageUsingPost({
        current: pagination.value.current,
        pageSize: pagination.value.pageSize,
      });
    if (res.code === 0 && res.data) {
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
  if (!currentUser || currentUser.userRole !== ACCESS_ENUM.TEACHER) {
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

.statistics-cards {
  margin: 20px 0;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card :deep(.arco-card-header) {
  border-bottom: none;
  padding-bottom: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text-1);
  margin-top: 8px;
}

.table-card {
  margin-top: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.progress-text {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-3);
}

:deep(.arco-table) {
  border-radius: 8px;
}

:deep(.arco-table-th) {
  background-color: var(--color-fill-2);
  font-weight: 500;
}

:deep(.arco-table-tr) {
  transition: background-color 0.3s;
}

:deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-2);
}

:deep(.arco-progress-text) {
  color: var(--color-text-1);
  font-weight: 500;
}
</style>
