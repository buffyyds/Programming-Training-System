<template>
  <div class="comment-management">
    <a-card class="general-card" title="评论管理">
      <a-table
        :data="commentList"
        :loading="loading"
        :pagination="pagination"
        @page-change="onPageChange"
      >
        <template #columns>
          <a-table-column title="评论者" data-index="user.userName" />
          <a-table-column title="角色" data-index="user.userRole">
            <template #cell="{ record }">
              <a-tag
                :color="record.user.userRole === 'teacher' ? 'blue' : 'green'"
              >
                {{ record.user.userRole === "teacher" ? "教师" : "学生" }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="评论内容" data-index="content">
            <template #cell="{ record }">
              <div class="comment-content">{{ record.content }}</div>
            </template>
          </a-table-column>
          <a-table-column title="评论题目" data-index="questionName" />
          <a-table-column title="评论时间" data-index="createTime">
            <template #cell="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" align="center">
            <template #cell="{ record }">
              <a-button
                type="text"
                status="danger"
                @click="handleDelete(record)"
              >
                删除
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
import { PostControllerService } from "../../../generated";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

// 评论列表相关
const commentList = ref([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
};

// 加载评论列表
const loadCommentList = async () => {
  loading.value = true;
  try {
    const res =
      await PostControllerService.getHasSensitiveWordPostListUsingGet();
    if (res.code === 0) {
      commentList.value = res.data;
      pagination.value.total = res.data.length;
    }
  } catch (error) {
    Message.error("加载评论列表失败");
  } finally {
    loading.value = false;
  }
};

// 分页变化
const onPageChange = (current: number) => {
  pagination.value.current = current;
};

// 删除评论
const handleDelete = (record: any) => {
  Message.warning({
    content: "确定要删除该评论吗？",
    okText: "确定",
    cancelText: "取消",
    onOk: async () => {
      try {
        const res = await PostControllerService.deletePostUsingPost({
          id: record.id,
        });
        if (res.code === 0) {
          Message.success("删除成功");
          loadCommentList();
        }
      } catch (error) {
        Message.error("删除失败");
      }
    },
  });
};

onMounted(() => {
  loadCommentList();
});
</script>

<style scoped>
.comment-management {
  padding: 20px;
}

.general-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.comment-content {
  max-width: 300px;
  word-break: break-all;
  line-height: 1.5;
}

:deep(.arco-table-th) {
  background-color: #f7f8fa;
  font-weight: 500;
}

:deep(.arco-table-td) {
  padding: 16px;
}

:deep(.arco-btn-text) {
  padding: 4px 8px;
}

:deep(.arco-btn-text:hover) {
  background-color: rgba(var(--red-6), 0.1);
}

:deep(.arco-tag) {
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  font-size: 12px;
}
</style>
