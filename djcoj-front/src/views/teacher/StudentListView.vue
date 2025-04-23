<template>
  <div class="student-list-container">
    <a-card class="student-list-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <icon-user-group class="title-icon" />
            学生列表
          </a-space>
        </div>
      </template>

      <!-- 搜索框 -->
      <div class="search-box">
        <a-input-search
          v-model="searchKeyword"
          placeholder="搜索学生姓名"
          allow-clear
          @search="handleSearch"
        />
      </div>

      <!-- 学生列表 -->
      <a-table
        :data="currentPageData"
        :loading="loading"
        :pagination="pagination"
        class="student-table"
        @page-change="onPageChange"
      >
        <template #columns>
          <a-table-column title="学生账号" data-index="userName" />
          <a-table-column title="注册时间" data-index="createTime">
            <template #cell="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-button
                type="primary"
                status="danger"
                @click="handleKick(record.id)"
              >
                踢出
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import { useRouter } from "vue-router";
import { IconArrowLeft, IconUserGroup } from "@arco-design/web-vue/es/icon";
import { TasControllerService } from "../../../generated";
import dayjs from "dayjs";

const router = useRouter();
const loading = ref(false);
const searchKeyword = ref("");
const studentList = ref<any[]>([]);

const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm");
};

// 页面变化处理
const onPageChange = (current: number) => {
  pagination.value.current = current;
};

// 获取学生列表
const getStudentList = async () => {
  loading.value = true;
  try {
    const res = await TasControllerService.getStudentsUsingGet();
    if (res.code === 0 && res.data) {
      studentList.value = res.data;
      pagination.value.total = res.data.length;
    }
  } catch (error) {
    console.error("获取学生列表失败:", error);
    Message.error("获取学生列表失败");
  } finally {
    loading.value = false;
  }
};

// 踢出学生
const handleKick = async (studentId: string) => {
  try {
    // 显示确认对话框
    const confirmed = await new Promise<boolean>((resolve) => {
      Modal.confirm({
        title: "确认踢出",
        content: "确定要踢出该学生吗？",
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      });
    });

    if (!confirmed) return;
    const res = await TasControllerService.kickStudentUsingGet(studentId);
    if (res.code === 0) {
      Message.success("踢出成功");
      // 重新获取学生列表
      await getStudentList();
    } else {
      Message.error("操作失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("操作失败：" + error.message);
  }
};

// 搜索处理
const handleSearch = (value: string) => {
  searchKeyword.value = value;
  // 重置到第一页
  pagination.value.current = 1;
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 过滤后的学生列表
const filteredStudentList = computed(() => {
  if (!searchKeyword.value) return studentList.value;
  return studentList.value.filter((student) =>
    student.userName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  );
});

// 获取当前页的数据
const currentPageData = computed(() => {
  const filtered = filteredStudentList.value;
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return filtered.slice(start, end);
});

// 监听过滤后的列表变化，更新分页总数
watch(
  filteredStudentList,
  (newList) => {
    pagination.value.total = newList.length;
  },
  { immediate: true }
);

onMounted(() => {
  getStudentList();
});
</script>

<style scoped>
.student-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.student-list-card {
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

.search-box {
  margin-bottom: 20px;
}

.student-table {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background-color: var(--color-fill-2);
  border-radius: 4px;
}

.selected-count {
  color: var(--color-text-3);
  font-size: 14px;
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
