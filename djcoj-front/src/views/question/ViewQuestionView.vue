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
                  {{ question.judgeConfig?.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig?.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig?.stackLimit ?? 0 }}
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
                  <a-button
                    type="primary"
                    @click="submitComment"
                    :loading="commentLoading"
                  >
                    发布评论
                  </a-button>
                </div>
              </div>
              <!-- 分隔线 -->
              <a-divider style="margin: 16px 0" />
              <!-- 评论列表 -->
              <div class="comment-list">
                <a-spin :loading="commentLoading">
                  <a-comment
                    v-for="comment in comments"
                    :key="comment.id"
                    :id="'comment-' + comment.id"
                    :author="comment.user.userName"
                    :content="comment.content"
                    :datetime="formatTime(comment.createTime)"
                  >
                    <template #avatar>
                      <a-avatar>{{ comment.user.userName[0] }}</a-avatar>
                    </template>
                    <template #actions>
                      <span class="action-item" @click="handleLike(comment)">
                        <icon-heart-fill
                          v-if="comment.hasThumb"
                          style="color: #ff4757"
                        />
                        <icon-heart v-else />
                        {{ comment.thumbNum }}
                      </span>
                      <span class="action-item" @click="toggleReply(comment)">
                        <icon-message />
                        {{
                          comment.showReplyInput
                            ? "收起"
                            : comment.reply?.length
                            ? `${comment.reply.length}条回复`
                            : "回复"
                        }}
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
                          <a-button @click="toggleReply(comment)"
                            >取消
                          </a-button>
                          <a-button type="primary" @click="submitReply(comment)"
                            >回复
                          </a-button>
                        </a-space>
                      </div>
                    </div>
                    <!-- 回复列表 -->
                    <template
                      v-if="comment.showReplyInput && comment.reply?.length"
                    >
                      <a-divider style="margin: 12px 0" />
                      <a-comment
                        v-for="reply in comment.reply"
                        :key="reply.id"
                        :author="reply.user.userName"
                        :content="reply.content"
                        :datetime="formatTime(reply.createTime)"
                      >
                        <template #avatar>
                          <a-avatar>{{ reply.user.userName[0] }}</a-avatar>
                        </template>
                        <template #actions>
                          <span class="action-item" @click="handleLike(reply)">
                            <icon-heart-fill
                              v-if="reply.hasThumb"
                              style="color: #ff4757"
                            />
                            <icon-heart v-else />
                            {{ reply.thumbNum }}
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
                </a-spin>
                <!-- 分页 -->
                <div class="pagination" v-if="commentPagination.total > 0">
                  <a-pagination
                    v-model:current="commentPagination.current"
                    v-model:pageSize="commentPagination.pageSize"
                    :total="commentPagination.total"
                    show-total
                    show-jumper
                  />
                </div>
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
        <a-divider :size="0" />
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
  PostControllerService,
  PostThumbControllerService,
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
  id: string | number;
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
    props.id
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
      questionId: typeof props.id === "string" ? parseInt(props.id) : props.id,
      userId: userId,
    });

    if (res.code === 0) {
      // 如果通过了，获取答案
      if (res.data === true) {
        const answer = await QuestionControllerService.getQuestionByIdUsingGet(
          typeof props.id === "string" ? parseInt(props.id) : props.id
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
const handleTabChange = async (key: string | number) => {
  activeTab.value = key.toString();
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
  loadComments();
  // 如果有 tab 参数，切换到对应的页签
  const tab = route.query.tab;
  if (tab) {
    activeTab.value = tab as string;
  }
  // 如果有 replyId 参数，找到对应的评论并展开回复
  const replyId = route.query.replyId;
  if (replyId) {
    watchEffect(() => {
      if (comments.value.length > 0) {
        // 先找到包含这个回复的原始评论
        const parentComment = comments.value.find((comment) =>
          comment.reply?.some((reply) => reply.id === Number(replyId))
        );
        if (parentComment) {
          parentComment.showReplyInput = true;
          // 滚动到父评论
          setTimeout(() => {
            const element = document.getElementById(
              `comment-${parentComment.id}`
            );
            if (element) {
              element.scrollIntoView({ behavior: "smooth", block: "center" });
              // 添加高亮效果
              element.classList.add("comment-highlight");
              setTimeout(() => {
                element.classList.remove("comment-highlight");
              }, 3000);
            }
          }, 100);
        }
      }
    });
  }
});

const changeCode = (value: string) => {
  form.value.code = value;
};

// 评论相关的类型定义
interface UserVO {
  id: number;
  userName: string;
  userAvatar: string;
  userProfile: string;
  userRole: string;
  createTime: string;
}

interface PostVO {
  id: number;
  content: string;
  thumbNum: number;
  userId: number;
  createTime: string;
  updateTime: string;
  user: UserVO;
  questionId: number;
  hasThumb: boolean;
  isReply: boolean;
  replyId?: number;
  reply?: PostVO[];
  showReplyInput?: boolean;
  replyContent?: string;
}

// 评论相关的状态
const commentContent = ref("");
const comments = ref<PostVO[]>([]);
const commentLoading = ref(false);
const commentPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 加载评论列表
const loadComments = async () => {
  if (!props.id) return;

  commentLoading.value = true;
  try {
    const res = await PostControllerService.listPostVoByPageUsingPost({
      current: commentPagination.value.current,
      pageSize: commentPagination.value.pageSize,
      questionId: props.id,
      sortField: "createTime",
      sortOrder: "desc",
    });

    if (res.code === 0 && res.data) {
      comments.value = res.data.records.map((comment: any) => ({
        ...comment,
        showReplyInput: false,
        replyContent: "",
      }));
      commentPagination.value.total = Number(res.data.total);
    }
  } catch (error) {
    Message.error("加载评论失败，请稍后重试");
  } finally {
    commentLoading.value = false;
  }
};

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    Message.warning("评论内容不能为空");
    return;
  }

  try {
    const res = await PostControllerService.addPostUsingPost({
      content: commentContent.value.trim(),
      isReply: false,
      questionId: props.id,
      replyId: 0,
    });

    if (res.code === 0) {
      Message.success("评论成功");
      commentContent.value = "";
      // 重新加载评论列表
      await loadComments();
    } else {
      Message.error("评论失败：" + res.message);
    }
  } catch (error) {
    Message.error("评论失败，请稍后重试");
  }
};

// 提交回复
const submitReply = async (comment: PostVO) => {
  if (!comment.replyContent?.trim()) {
    Message.warning("回复内容不能为空");
    return;
  }

  try {
    const res = await PostControllerService.addPostUsingPost({
      content: comment.replyContent.trim(),
      isReply: true,
      questionId: props.id,
      replyId: comment.id,
    });

    if (res.code === 0) {
      Message.success("回复成功");
      comment.showReplyInput = false;
      // 重新加载评论列表
      await loadComments();
    } else {
      Message.error("回复失败：" + res.message);
    }
  } catch (error) {
    Message.error("回复失败，请稍后重试");
  }
};

// 删除评论
const deleteComment = async (comment: PostVO) => {
  try {
    const res = await PostControllerService.deletePostUsingPost({
      id: comment.id,
    });
    if (res.code === 0) {
      Message.success("删除成功");
      // 重新加载评论列表
      await loadComments();
    } else {
      Message.error("删除失败：" + res.message);
    }
  } catch (error) {
    Message.error("删除失败，请稍后重试");
  }
};

// 点赞/取消点赞
const handleLike = async (comment: PostVO) => {
  try {
    const res = await PostThumbControllerService.doThumbUsingPost({
      postId: comment.id,
    });

    if (res.code === 0) {
      // 更新评论的点赞状态和数量
      comment.hasThumb = !comment.hasThumb;
      comment.thumbNum += comment.hasThumb ? 1 : -1;
      Message.success(comment.hasThumb ? "点赞成功" : "已取消点赞");
    } else {
      Message.error("操作失败：" + res.message);
    }
  } catch (error) {
    Message.error("操作失败，请稍后重试");
  }
};

// 检查是否是当前用户的评论
const isCurrentUser = (userId: number) => {
  return store.state.user.loginUser?.id === userId;
};

// 切换回复框显示状态
const toggleReply = (comment: PostVO) => {
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

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow();
};

// 监听分页变化
watchEffect(() => {
  if (activeTab.value === "comment") {
    loadComments();
  }
});
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

.pagination {
  margin-top: 24px;
  text-align: center;
}

.comment-highlight {
  animation: highlight 3s ease-out;
}

@keyframes highlight {
  0% {
    background-color: rgba(var(--primary-6), 0.2);
  }
  100% {
    background-color: transparent;
  }
}
</style>
