<template>
  <div class="wrong-question-list">
    <a-page-header
      title="学生错题管理"
      subtitle="查看和分析学生的错题情况"
      :show-back="false"
    >
      <template #extra>
        <a-button type="primary" @click="goToAnalysis">
          <template #icon>
            <icon-chart-line />
          </template>
          查看错题分析图表
        </a-button>
      </template>
    </a-page-header>

    <a-row :gutter="16" class="statistics-cards">
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>总错题数</template>
          <div class="stat-value">{{ wrongQuestionList.length }}</div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>标记错题学生数</template>
          <div class="stat-value">
            {{ totalWrongStudents }}
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>平均错误提交次数</template>
          <div class="stat-value">
            {{ averageWrongSubmits }}
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <template #title>错题标记率</template>
          <div class="stat-value">{{ wrongQuestionRate }}%</div>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="table-card">
      <template #title>
        <div class="card-title">
          <span>错题列表</span>
          <a-tooltip content="点击题目名称查看详情">
            <icon-question-circle />
          </a-tooltip>
        </div>
      </template>
      <a-table
        :data="wrongQuestionList"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
        :bordered="false"
        :stripe="true"
      >
        <template #columns>
          <a-table-column
            title="题目名称"
            data-index="questionName"
            width="30%"
          >
            <template #cell="{ record }">
              <a-link @click="showDetail(record)"
                >{{ record.questionName }}
              </a-link>
            </template>
          </a-table-column>
          <a-table-column
            title="标记错题学生数"
            data-index="wrongQuestionStudentCount"
            width="20%"
          >
            <template #cell="{ record }">
              <a-tag color="red">{{ record.wrongQuestionStudentCount }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column
            title="学生总数"
            data-index="studentCount"
            width="20%"
          />
          <a-table-column
            title="错误提交总数"
            data-index="totalWrongSubmitCount"
            width="20%"
          >
            <template #cell="{ record }">
              <a-tag color="orange">{{ record.totalWrongSubmitCount }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" width="10%" fixed="right">
            <template #cell="{ record }">
              <a-button type="text" @click="showDetail(record)">
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

    <!-- 详情弹窗 -->
    <a-modal
      v-model:visible="detailVisible"
      :title="currentQuestion?.questionName"
      width="800px"
      :footer="null"
    >
      <a-descriptions
        :data="[
          { label: '题目ID', value: currentQuestion?.questionId },
          {
            label: '标记错题学生数',
            value: currentQuestion?.wrongQuestionStudentCount,
          },
          { label: '学生总数', value: currentQuestion?.studentCount },
          {
            label: '错误提交总数',
            value: currentQuestion?.totalWrongSubmitCount,
          },
        ]"
        :column="2"
        bordered
      />
      <a-divider />
      <a-table
        :data="detailList"
        :loading="detailLoading"
        :pagination="{ pageSize: 5 }"
        :bordered="false"
        :stripe="true"
      >
        <template #columns>
          <a-table-column title="学生姓名" data-index="studentUser.userName" />
          <a-table-column title="手机号">
            <template #cell="{ record }">
              {{ record.studentUser.userPhone || "未填写" }}
            </template>
          </a-table-column>
          <a-table-column title="错误提交数" data-index="wrongSubmitNum">
            <template #cell="{ record }">
              <a-tag color="orange">{{ record.wrongSubmitNum }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="是否标记为错题">
            <template #cell="{ record }">
              <a-tag :color="record.isWrongQuestion ? 'red' : 'green'">
                {{ record.isWrongQuestion ? "是" : "否" }}
              </a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { WrongQuestionControllerService } from "../../../generated";
import { Message } from "@arco-design/web-vue";
import { IconQuestionCircle, IconEye } from "@arco-design/web-vue/es/icon";

const router = useRouter();
const loading = ref(false);
const detailLoading = ref(false);
const detailVisible = ref(false);
const currentQuestion = ref<any>(null);
const wrongQuestionList = ref<any[]>([]);
const detailList = ref<any[]>([]);

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 计算统计数据
const totalWrongStudents = computed(() => {
  return wrongQuestionList.value.reduce(
    (sum, item) => sum + (item.wrongQuestionStudentCount || 0),
    0
  );
});

const averageWrongSubmits = computed(() => {
  if (wrongQuestionList.value.length === 0) return 0;
  const total = wrongQuestionList.value.reduce(
    (sum, item) => sum + (item.totalWrongSubmitCount || 0),
    0
  );
  return (total / wrongQuestionList.value.length).toFixed(1);
});

const wrongQuestionRate = computed(() => {
  if (wrongQuestionList.value.length === 0) return 0;
  const totalStudents = wrongQuestionList.value.reduce(
    (sum, item) => sum + (item.studentCount || 0),
    0
  );
  if (totalStudents === 0) return 0;
  return ((totalWrongStudents.value / totalStudents) * 100).toFixed(1);
});

// 加载错题列表
const loadWrongQuestionList = async () => {
  loading.value = true;
  try {
    const res =
      await WrongQuestionControllerService.getWrongQuestionListByTeacherUsingGet();
    if (res.code === 0 && res.data) {
      wrongQuestionList.value = res.data;
      pagination.value.total = res.data.length;
    } else {
      Message.error("获取错题列表失败：" + res.message);
    }
  } catch (error) {
    Message.error("获取错题列表失败");
  } finally {
    loading.value = false;
  }
};

// 显示详情
const showDetail = async (record: any) => {
  currentQuestion.value = record;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res =
      await WrongQuestionControllerService.getWrongQuestionDetailByTeacherUsingGet(
        record.questionId
      );
    if (res.code === 0 && res.data) {
      detailList.value = res.data;
    } else {
      Message.error("获取详情失败：" + res.message);
    }
  } catch (error) {
    Message.error("获取详情失败");
  } finally {
    detailLoading.value = false;
  }
};

// 跳转到分析页面
const goToAnalysis = () => {
  router.push("/wrongQuestion/statistics");
};

// 分页变化
const onPageChange = (page: number) => {
  pagination.value.current = page;
  loadWrongQuestionList();
};

onMounted(() => {
  loadWrongQuestionList();
});
</script>

<style scoped>
.wrong-question-list {
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
  gap: 8px;
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

:deep(.arco-modal) {
  border-radius: 8px;
}

:deep(.arco-modal-header) {
  border-bottom: 1px solid var(--color-border-2);
}

:deep(.arco-descriptions) {
  margin-bottom: 16px;
}
</style>
