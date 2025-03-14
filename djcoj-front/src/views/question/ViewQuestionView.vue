<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs
          default-active-key="question"
          v-model:activeKey="activeTab"
          @change="handleTabChange"
        >
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="comment" title="评论">
            <div class="comment-section">
              <!-- 评论输入框 -->
              <div class="comment-input">
                <a-textarea
                  v-model="commentContent"
                  :max-length="1000"
                  placeholder="写下你的评论..."
                  allow-clear
                  show-word-limit
                />
                <div class="comment-actions">
                  <a-button type="primary" @click="submitComment"
                    >发布评论</a-button
                  >
                </div>
              </div>
              <!-- 分隔线 -->
              <a-divider style="margin: 16px 0" />
              <!-- 评论列表 -->
              <div class="comment-list">
                <a-comment
                  v-for="comment in comments"
                  :key="comment.id"
                  :author="comment.userName"
                  :content="comment.content"
                  :datetime="formatTime(comment.createTime)"
                >
                  <template #avatar>
                    <a-avatar>{{ comment.userName[0] }}</a-avatar>
                  </template>
                  <template #actions>
                    <span class="action-item" @click="handleLike(comment)">
                      <icon-heart-fill
                        v-if="comment.isLiked"
                        style="color: #ff4757"
                      />
                      <icon-heart v-else />
                      {{ comment.likeCount || 0 }}
                    </span>
                    <span class="action-item" @click="toggleReply(comment)">
                      <icon-message />
                      {{ comment.showReplyInput ? "收起" : "回复" }}
                    </span>
                    <span
                      v-if="isCurrentUser(comment.userId)"
                      class="action-item"
                      @click="deleteComment(comment)"
                    >
                      <icon-delete /> 删除
                    </span>
                  </template>
                  <!-- 回复输入框 -->
                  <div v-if="comment.showReplyInput" class="reply-input">
                    <a-textarea
                      v-model="comment.replyContent"
                      :max-length="500"
                      placeholder="写下你的回复..."
                      allow-clear
                    />
                    <div class="reply-actions">
                      <a-space>
                        <a-button @click="toggleReply(comment)">取消</a-button>
                        <a-button type="primary" @click="submitReply(comment)"
                          >回复</a-button
                        >
                      </a-space>
                    </div>
                  </div>
                  <!-- 回复列表 -->
                  <template
                    v-if="comment.replies && comment.replies.length > 0"
                  >
                    <a-divider style="margin: 12px 0" />
                    <a-comment
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      :author="reply.userName"
                      :content="reply.content"
                      :datetime="formatTime(reply.createTime)"
                    >
                      <template #avatar>
                        <a-avatar>{{ reply.userName[0] }}</a-avatar>
                      </template>
                      <template #actions>
                        <span class="action-item" @click="handleLike(reply)">
                          <icon-heart-fill
                            v-if="reply.isLiked"
                            style="color: #ff4757"
                          />
                          <icon-heart v-else />
                          {{ reply.likeCount || 0 }}
                        </span>
                        <span
                          v-if="isCurrentUser(reply.userId)"
                          class="action-item"
                          @click="deleteComment(reply)"
                        >
                          <icon-delete /> 删除
                        </span>
                      </template>
                    </a-comment>
                  </template>
                </a-comment>
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane key="answer" title="答案">
            <div v-if="canViewAnswer" class="question-answer">
              <MdViewer :value="answerContent" />
            </div>
            <div v-else class="no-answer">
              <a-result
                status="warning"
                title="未答题成功"
                sub-title="无法查看答案，请继续做题。"
              >
                <template #extra>
                  <a-button type="primary" @click="activeTab = 'question'">
                    返回做题
                  </a-button>
                </template>
              </a-result>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form :model="form" layout="inline">
          <a-form-item
            field="language"
            label="编程语言"
            style="min-width: 240px"
          >
            <a-select
              v-model="form.language"
              :style="{ width: '320px' }"
              placeholder="选择编程语言"
            >
              <a-option>java</a-option>
              <a-option>cpp</a-option>
              <a-option>go</a-option>
              <a-option>html</a-option>
            </a-select>
          </a-form-item>
        </a-form>
        <CodeEditor
          :value="form.code as string"
          :language="form.language"
          :handle-change="changeCode"
        />
        <a-divider size="0" />
        <a-button type="primary" style="min-width: 200px" @click="doSubmit">
          提交代码
        </a-button>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect, withDefaults, defineProps } from "vue";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import {
  QuestionControllerService,
  QuestionSubmitAddRequest,
  QuestionVO,
} from "../../../generated";
import { useRoute } from "vue-router";
import { useStore } from "vuex";
import { Message } from "@arco-design/web-vue";
import {
  IconMessage,
  IconDelete,
  IconHeart,
  IconHeartFill,
} from "@arco-design/web-vue/es/icon";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const question = ref<QuestionVO>();
const loading = ref(false);
const activeTab = ref("question");
const canViewAnswer = ref(false);
const hasCheckedAnswer = ref(false);
const answerContent = ref("");

const route = useRoute();
const store = useStore();

const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code: "",
});

/**
 * 检查是否可以查看答案
 */
const checkCanViewAnswer = async (forceCheck = false) => {
  const userId = store.state.user.loginUser?.id;

  if (hasCheckedAnswer.value && !forceCheck) {
    return;
  }

  if (!props.id || !userId) {
    canViewAnswer.value = false;
    hasCheckedAnswer.value = true;
    return;
  }

  try {
    const res = await QuestionControllerService.getQuestionSubmitPassUsingPost({
      questionId: props.id,
      userId: userId,
    });

    if (res.code === 0) {
      // 如果通过了，获取答案
      if (res.data === true) {
        const answer = await QuestionControllerService.getQuestionByIdUsingGet(
          props.id
        );
        if (answer.code === 0 && answer.data?.answer) {
          answerContent.value = answer.data.answer;
          canViewAnswer.value = true;
        } else {
          canViewAnswer.value = false;
        }
      } else {
        canViewAnswer.value = false;
      }
      hasCheckedAnswer.value = true;
    } else {
      canViewAnswer.value = false;
      hasCheckedAnswer.value = true;
    }
  } catch (error) {
    console.error("检查答案权限失败:", error);
    canViewAnswer.value = false;
    hasCheckedAnswer.value = true;
  }
};

/**
 * 切换页签
 */
const handleTabChange = async (key: string) => {
  activeTab.value = key;
  if (key === "answer") {
    await checkCanViewAnswer();
  }
};

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }

  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
    hasCheckedAnswer.value = false;
    if (activeTab.value === "answer") {
      await checkCanViewAnswer(true);
    }
  } else {
    message.error("提交失败," + res.message);
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const changeCode = (value: string) => {
  form.value.code = value;
};

// 评论相关的状态
const commentContent = ref("");
const comments = ref<any[]>([
  // 模拟数据，后续替换为接口数据
  {
    id: 1,
    userId: 1,
    userName: "用户A",
    content: "这道题解题思路很清晰",
    createTime: "2024-01-20 12:00:00",
    likeCount: 12,
    isLiked: false,
    showReplyInput: false,
    replyContent: "",
    replies: [
      {
        id: 2,
        userId: 2,
        userName: "用户B",
        content: "同意，我也是这么想的",
        createTime: "2024-01-20 12:30:00",
        likeCount: 3,
        isLiked: false,
      },
    ],
  },
]);

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow();
};

// 切换回复框显示状态
const toggleReply = (comment: any) => {
  // 先关闭其他所有评论的回复框
  comments.value.forEach((item) => {
    if (item.id !== comment.id) {
      item.showReplyInput = false;
      item.replyContent = "";
    }
  });
  // 切换当前评论的回复框状态
  comment.showReplyInput = !comment.showReplyInput;
  if (!comment.showReplyInput) {
    comment.replyContent = "";
  }
};

// 点赞/取消点赞
const handleLike = async (comment: any) => {
  try {
    // TODO: 调用后端接口进行点赞/取消点赞
    // const res = await PostControllerService.likePostUsingPost({
    //   postId: comment.id,
    //   userId: store.state.user.loginUser?.id
    // });
    // if (res.code === 0) {
    comment.isLiked = !comment.isLiked;
    comment.likeCount += comment.isLiked ? 1 : -1;
    Message.success(comment.isLiked ? "点赞成功" : "已取消点赞");
    // }
  } catch (error) {
    Message.error("操作失败，请稍后重试");
  }
};

// 检查是否是当前用户的评论
const isCurrentUser = (userId: number) => {
  return store.state.user.loginUser?.id === userId;
};

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    Message.warning("评论内容不能为空");
    return;
  }
  // TODO: 调用后端接口提交评论
  Message.success("评论成功");
  commentContent.value = "";
};

// 删除评论
const deleteComment = (comment: any) => {
  // TODO: 调用后端接口删除评论
  console.log("删除评论:", comment);
};

// 提交回复
const submitReply = async (comment: any) => {
  if (!comment.replyContent?.trim()) {
    Message.warning("回复内容不能为空");
    return;
  }
  // TODO: 调用后端接口提交回复
  Message.success("回复成功");
  comment.showReplyInput = false;
};
</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

.question-answer {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-answer :deep(.markdown-body) {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
}

.question-answer :deep(pre) {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  margin: 16px 0;
}

.question-answer :deep(code) {
  font-family: Monaco, Consolas, "Courier New", monospace;
  font-size: 14px;
}

.no-answer {
  padding: 40px 0;
  text-align: center;
}

/* 评论区样式优化 */
.comment-section {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.comment-input {
  margin-bottom: 0; /* 修改这里，让分隔线有更好的间距 */
  background-color: #f7f8fa;
  padding: 16px;
  border-radius: 8px;
}

.comment-input :deep(.arco-textarea-wrapper) {
  background-color: #fff;
}

.comment-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  margin-top: 24px;
}

.comment-list :deep(.arco-comment) {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.comment-list :deep(.arco-comment:hover) {
  background-color: #f0f1f2;
}

.comment-list :deep(.arco-comment .arco-comment) {
  background-color: #fff;
  margin-top: 16px;
  margin-bottom: 0;
  border: 1px solid #e5e6eb;
}

.comment-list :deep(.arco-comment .arco-comment:hover) {
  background-color: #f8f9fa;
}

.action-item {
  cursor: pointer;
  color: #86909c;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
}

.action-item:hover {
  color: #00aeec;
  background-color: rgba(0, 174, 236, 0.1);
}

.action-item .icon-heart-fill {
  animation: heartBeat 0.3s ease-in-out;
}

@keyframes heartBeat {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.reply-input {
  margin: 16px 0;
  padding: 16px;
  background-color: #f7f8fa;
  border-radius: 8px;
}

.reply-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

:deep(.arco-comment-title) {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

:deep(.arco-comment-datetime) {
  font-size: 12px;
  color: #86909c;
}

:deep(.arco-comment-content) {
  font-size: 14px;
  color: #4e5969;
  margin-top: 8px;
  line-height: 1.6;
}

:deep(.arco-avatar) {
  background-color: #00aeec;
  color: #fff;
}
</style>
