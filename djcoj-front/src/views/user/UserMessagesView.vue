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
    <a-tabs v-model:active-key="activeTab" class="message-tabs">
      <a-tab-pane key="comment" title="评论回复">
        <template #title>
          <div class="tab-title">
            评论回复
            <div v-if="hasUnreadComments" class="red-dot"></div>
          </div>
        </template>
        <template v-if="loading">
          <div class="loading-container">
            <a-spin />
          </div>
        </template>
        <template v-else-if="commentReplies.length === 0">
          <a-empty description="暂无评论回复" />
        </template>
        <template v-else>
          <div
            v-for="reply in commentReplies"
            :key="reply.id"
            class="message-item"
            @click="handleCommentClick(reply)"
          >
            <!-- 未读标记 -->
            <div v-if="!reply.isRead" class="unread-dot"></div>
            <div class="message-content">
              <div class="message-header">
                <a-space>
                  <a-avatar :size="32">{{
                    reply.user?.userName?.[0]
                  }}</a-avatar>
                  <span class="username">{{ reply.user?.userName }}</span>
                </a-space>
                <span class="time">{{
                  reply.createTime && formatTime(reply.createTime)
                }}</span>
              </div>
              <div class="message-body">
                回复了你的评论：{{ reply.content }}
              </div>
              <a-button
                type="text"
                @click.stop="goToQuestion(reply.questionId, reply.replyId)"
              >
                查看详情
              </a-button>
            </div>
          </div>
        </template>
      </a-tab-pane>

      <a-tab-pane key="system" title="系统消息">
        <template #title>
          <div class="tab-title">
            系统消息
            <div v-if="hasUnreadSystemMessages" class="red-dot"></div>
          </div>
        </template>
        <template v-if="loading">
          <div class="loading-container">
            <a-spin />
          </div>
        </template>
        <template v-else-if="systemMessages.length === 0">
          <a-empty description="暂无系统消息" />
        </template>
        <template v-else>
          <div
            v-for="message in systemMessages"
            :key="message.id"
            class="message-item"
            @click="handleSystemMessageClick(message)"
          >
            <!-- 未读标记 -->
            <div v-if="!message.isRead" class="unread-dot"></div>
            <div class="message-content">
              <div class="message-header">
                <a-space>
                  <icon-notification />
                  <span class="username">系统通知</span>
                </a-space>
                <span class="time">{{
                  message.createTime && formatTime(message.createTime)
                }}</span>
              </div>
              <div class="message-body">{{ message.content }}</div>
              <a-button
                v-if="message.questionId"
                type="text"
                @click.stop="
                  goToQuestion(message.questionId, undefined, 'question')
                "
              >
                查看题目
              </a-button>
            </div>
          </div>
        </template>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import {
  ReplyControllerService,
  PostControllerService,
  RemindControllerService,
} from "../../../generated";
import { IconLeft, IconNotification } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import dayjs from "dayjs";
import type { ReplyVO } from "../../../generated";

interface ReplyWithRead extends ReplyVO {
  isRead: boolean;
}

interface SystemMessage {
  id: number;
  content: string;
  createTime: string;
  isRead: boolean;
  questionId?: number;
}

const router = useRouter();
const store = useStore();
const loading = ref(true);
const activeTab = ref("comment");
const commentReplies = ref<ReplyWithRead[]>([]);
const systemMessages = ref<SystemMessage[]>([]);

// 计算是否有未读评论
const hasUnreadComments = computed(() => {
  return commentReplies.value.some((reply) => !reply.isRead);
});

// 计算是否有未读系统消息
const hasUnreadSystemMessages = computed(() => {
  return systemMessages.value.some((message) => !message.isRead);
});

// 计算是否有任何未读消息
const hasAnyUnread = computed(() => {
  return hasUnreadComments.value || hasUnreadSystemMessages.value;
});

// 返回上一页
const goBack = () => {
  router.back();
};

// 跳转到问题详情页
const goToQuestion = async (
  questionId?: number,
  replyId?: number,
  tab = "comment"
) => {
  if (questionId && replyId) {
    try {
      const res = await PostControllerService.getCommentPagePositionUsingGet(
        questionId,
        replyId
      );
      if (res.code === 0) {
        const page = res.data;
        router.push({
          path: `/view/question/${questionId}`,
          query: {
            tab: tab,
            page: page?.toString(),
            replyId: replyId.toString(),
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
        tab: tab,
      },
    });
  }
};

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
};

// 更新全局未读状态
const updateGlobalUnreadStatus = () => {
  store.commit("updateUnreadCount", hasAnyUnread.value);
};

// 处理评论点击
const handleCommentClick = async (reply: ReplyWithRead) => {
  if (!reply.isRead && reply.id) {
    await markAsRead(reply.id);
  }
};

// 处理系统消息点击
const handleSystemMessageClick = async (message: SystemMessage) => {
  if (!message.isRead && message.id) {
    await markSystemMessageAsRead(message.id);
  }
};

// 加载评论回复数据
const loadCommentReplies = async () => {
  try {
    loading.value = true;
    const res = await ReplyControllerService.getMyReplyUsingGet();
    if (res.code === 0) {
      commentReplies.value = (res.data || [])
        .map((reply) => ({
          ...reply,
          isRead: reply.isRead ?? false,
        }))
        .sort((a, b) => {
          const timeA = new Date(a.createTime || "").getTime();
          const timeB = new Date(b.createTime || "").getTime();
          return timeB - timeA;
        });
      updateGlobalUnreadStatus();
    } else {
      Message.error("获取评论回复失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("获取评论回复失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

// 加载系统消息数据
const loadSystemMessages = async () => {
  try {
    loading.value = true;
    const res = await RemindControllerService.listRemindCompleteByPageUsingPost(
      {
        current: 1,
        pageSize: 10,
        sortField: "createTime",
        sortOrder: "descend",
      }
    );
    if (res.code === 0) {
      systemMessages.value = (res.data.records || []).map((message: any) => ({
        ...message,
        isRead: message.isRead ?? false,
      }));
      updateGlobalUnreadStatus();
    } else {
      Message.error("获取系统消息失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("获取系统消息失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

// 标记评论消息为已读
const markAsRead = async (id: number) => {
  try {
    const reply = commentReplies.value.find((r) => r.id === id);
    if (!reply || reply.isRead) {
      return;
    }

    const res = await ReplyControllerService.markAsReadUsingPut(id);
    if (res.code === 0) {
      reply.isRead = true;
      updateGlobalUnreadStatus();
      Message.success("已标记为已读");
    } else {
      Message.error("标记已读失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("标记已读失败：" + error.message);
  }
};

// 标记系统消息为已读
const markSystemMessageAsRead = async (id: number) => {
  try {
    const message = systemMessages.value.find((m) => m.id === id);
    if (!message || message.isRead) {
      return;
    }

    const res = await RemindControllerService.markAsReadUsingPut(id);
    if (res.code === 0) {
      message.isRead = true;
      updateGlobalUnreadStatus();
      Message.success("已标记为已读");
    } else {
      Message.error("标记已读失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("标记已读失败：" + error.message);
  }
};

onMounted(async () => {
  await Promise.all([loadCommentReplies(), loadSystemMessages()]);
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

.message-tabs {
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

.tab-title {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f53f3f;
  box-shadow: 0 0 0 2px #fff;
}

:deep(.arco-badge-dot),
:deep(.arco-badge-count) {
  display: none;
}
</style>
