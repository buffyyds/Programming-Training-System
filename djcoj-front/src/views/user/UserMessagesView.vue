<template>
  <div class="messages-container">
    <!-- 返回按钮 -->
    <div class="header">
      <a-button class="back-button" type="text" @click="goBack">
        <template #icon>
          <icon-left />
        </template>
      </a-button>
      <h2>我的消息</h2>
    </div>

    <!-- 消息列表 -->
    <div class="message-list">
      <template v-if="loading">
        <div class="loading-container">
          <a-spin />
        </div>
      </template>
      <template v-else-if="replies.length === 0">
        <a-empty description="暂无消息" />
      </template>
      <template v-else>
        <div
          v-for="reply in replies"
          :key="reply.id"
          class="message-item"
          @click="reply.id && markAsRead(reply.id)"
        >
          <!-- 未读标记 -->
          <div v-if="!reply.isRead" class="unread-dot"></div>
          <div class="message-content">
            <div class="message-header">
              <a-space>
                <a-avatar :size="32">{{ reply.user?.userName?.[0] }}</a-avatar>
                <span class="username">{{ reply.user?.userName }}</span>
              </a-space>
              <span class="time">{{
                reply.createTime && formatTime(reply.createTime)
              }}</span>
            </div>
            <div class="message-body">回复了你的评论：{{ reply.content }}</div>
            <a-button
              type="text"
              @click.stop="goToQuestion(reply.questionId, reply.replyId)"
            >
              查看详情
            </a-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  ReplyControllerService,
  PostControllerService,
} from "../../../generated";
import { IconLeft, IconUser } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import dayjs from "dayjs";
import type { ReplyVO } from "../../../generated";

interface ReplyWithRead extends ReplyVO {
  isRead: boolean;
}

const router = useRouter();
const loading = ref(true);
const replies = ref<ReplyWithRead[]>([]);

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到问题详情页
const goToQuestion = async (questionId?: number, replyId?: number) => {
  if (questionId && replyId) {
    try {
      // 获取评论页码
      console.log("questionId", questionId);
      console.log("replyId", replyId);
      const res = await PostControllerService.getCommentPagePositionUsingGet(
        questionId,
        replyId
      );
      if (res.code === 0) {
        const page = res.data;
        router.push({
          path: `/view/question/${questionId}`,
          query: {
            tab: "comment", // 默认打开评论页签
            page: page.toString(), // 传递页码
            replyId: replyId.toString(), // 传递评论ID
          },
        });
      } else {
        Message.error("获取评论位置失败：" + res.message);
      }
    } catch (error: any) {
      Message.error("获取评论位置失败：" + error.message);
    }
  } else if (questionId) {
    router.push({
      path: `/view/question/${questionId}`,
      query: {
        tab: "comment", // 默认打开评论页签
      },
    });
  }
};

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
};

// 标记消息为已读（仅前端状态）
const markAsRead = async (id: number) => {
  try {
    // 调用后端接口标记消息为已读
    const res = await ReplyControllerService.markAsReadUsingPut(id);
    if (res.code === 0) {
      // 更新前端状态
      const reply = replies.value.find((r) => r.id === id);
      if (reply) {
        reply.isRead = true;
      }
      Message.success("已标记为已读");
    } else {
      Message.error("标记已读失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("标记已读失败：" + error.message);
  }
};

// 加载消息数据
const loadMessages = async () => {
  try {
    loading.value = true;
    const res = await ReplyControllerService.getMyReplyUsingGet();
    if (res.code === 0) {
      // 使用后端返回的已读状态,并按时间倒序排序
      replies.value = (res.data || [])
        .map((reply) => ({
          ...reply,
          isRead: reply.isRead ?? false, // 如果后端返回 null 则默认为未读
        }))
        .sort((a, b) => {
          const timeA = new Date(a.createTime || "").getTime();
          const timeB = new Date(b.createTime || "").getTime();
          return timeB - timeA; // 倒序排序,最新的在前面
        });
    } else {
      Message.error("获取消息失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("获取消息失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadMessages();
});
</script>

<style scoped>
.messages-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.back-button {
  margin-right: 16px;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.message-list {
  background: var(--color-bg-2);
  border-radius: 4px;
  padding: 16px;
}

.message-item {
  position: relative;
  padding: 16px;
  border-bottom: 1px solid var(--color-neutral-3);
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:last-child {
  border-bottom: none;
}

.message-item:hover {
  background-color: var(--color-fill-2);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.unread-dot {
  position: absolute;
  top: 16px;
  left: -4px;
  width: 8px;
  height: 8px;
  background-color: #f53f3f;
  border-radius: 50%;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  color: var(--color-text-1);
}

.time {
  color: var(--color-text-3);
  font-size: 12px;
}

.message-body {
  color: var(--color-text-1);
  margin: 12px 0;
  background-color: var(--color-fill-2);
  padding: 12px;
  border-radius: 4px;
}

.message-content {
  margin: 8px 0;
  color: #666;
}

.reply-content {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin-top: 8px;
}
</style>
