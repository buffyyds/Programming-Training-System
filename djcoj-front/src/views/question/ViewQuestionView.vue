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
              <pre><code>{{ answerContent }}</code></pre>
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
          <a-tab-pane key="ai" title="AI代码评分">
            <div class="tab-content">
              <div class="ai-score-content">
                <div class="ai-score-header">
                  <a-tooltip position="left">
                    <template #content>
                      <span class="score-tooltip">评分与题目无关</span>
                    </template>
                    <a-button
                      type="primary"
                      :loading="aiScoring"
                      @click="requestAIScore"
                    >
                      {{ aiScoring ? "评分中..." : "开始评分" }}
                    </a-button>
                  </a-tooltip>
                </div>
                <div v-if="aiScoreResult" class="ai-score-result">
                  <div
                    v-html="formatMarkdown(aiScoreResult)"
                    class="markdown-content"
                  ></div>
                </div>
                <div v-else class="empty-content">
                  <a-empty description="暂无评分结果" />
                </div>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form :model="form" layout="inline" style="margin-bottom: 16px">
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
              <a-option value="java">Java</a-option>
              <a-option value="cpp">C++</a-option>
              <a-option value="python">Python</a-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              :loading="submitting"
              @click="doSubmit"
              style="min-width: 120px"
            >
              {{ submitting ? "提交中..." : "提交代码" }}
            </a-button>
          </a-form-item>
        </a-form>
        <CodeEditor
          :value="form.code as string"
          :language="form.language"
          :handle-change="changeCode"
        />
        <!-- 修改判题结果展示区域，移除v-if条件 -->
        <div class="judge-result">
          <a-tabs v-model:active-key="activeJudgeTab">
            <a-tab-pane key="input" title="输入用例">
              <div class="tab-content">
                <template v-if="judgeCases.length > 0">
                  <div
                    v-for="(testCase, index) in judgeCases.slice(0, 2)"
                    :key="index"
                    class="test-case-content"
                    style="margin-bottom: 16px"
                  >
                    <div class="case-title">用例{{ index + 1 }}：</div>
                    <div class="case-content">
                      <div class="case-item">
                        <div class="case-label">输入：</div>
                        <div class="case-value">{{ testCase.input }}</div>
                      </div>
                      <div class="case-item">
                        <div class="case-label">输出：</div>
                        <div class="case-value">{{ testCase.output }}</div>
                      </div>
                    </div>
                  </div>
                </template>
                <div v-else class="empty-content">
                  <a-empty description="暂无输入用例" />
                </div>
              </div>
            </a-tab-pane>
            <a-tab-pane key="result" title="判题结果">
              <div class="tab-content">
                <div v-if="judgeResult" class="judge-result-content">
                  <div class="judge-result-item">
                    <span class="label">判题结果：</span>
                    <span
                      :class="[
                        'value',
                        getJudgeResultClass(judgeResult.message),
                      ]"
                    >
                      <span
                        :class="[
                          'status-dot',
                          getJudgeResultClass(judgeResult.message),
                        ]"
                      ></span>
                      {{ judgeResult.message }}
                    </span>
                  </div>
                  <div class="judge-result-item">
                    <span class="label">内存占用：</span>
                    <span class="value">{{ judgeResult.memory }}B</span>
                  </div>
                  <div class="judge-result-item">
                    <span class="label">消耗时间：</span>
                    <span class="value">{{ judgeResult.time }}ms</span>
                  </div>
                  <div v-if="judgeCases.length > 0">
                    <div
                      v-for="(testCase, index) in judgeCases.slice(0, 2)"
                      :key="index"
                      class="test-case-content"
                      style="margin-bottom: 16px"
                    >
                      <div class="case-title">用例{{ index + 1 }}：</div>
                      <div class="case-content">
                        <div class="case-item">
                          <div class="case-label">输入：</div>
                          <div class="case-value">{{ testCase.input }}</div>
                        </div>
                        <div class="case-item">
                          <div class="case-label">运行结果：</div>
                          <div
                            :class="[
                              'case-value',
                              judgeResult.testCaseResults?.[index] ===
                              testCase.output
                                ? 'success-result'
                                : 'error-result',
                            ]"
                          >
                            {{ judgeResult.testCaseResults?.[index] }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-content">
                  <a-empty description="暂无判题结果" />
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {
  onMounted,
  ref,
  watchEffect,
  withDefaults,
  defineProps,
  onUnmounted,
  computed,
  nextTick,
  watch,
} from "vue";
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
import { marked } from "marked";
import DOMPurify from "dompurify";

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

// 添加判例相关的状态
const judgeCases = ref<Array<{ input: string; output: string }>>([]);

// 计算要显示的判例（最多显示2个）
const displayJudgeCases = computed(() => {
  return judgeCases.value.slice(0, 2);
});

const activeJudgeTab = ref("input");

// 添加轮询超时时间常量
const POLL_TIMEOUT = 30000; // 30秒超时

// 添加一个标志位来标记是否已经获取到结果
const hasGotResult = ref(false);

// 修改加载数据的逻辑
const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id
  );
  if (res.code === 0) {
    question.value = res.data;
    // 解析判例数据
    try {
      if (question.value.judgeCase) {
        judgeCases.value = JSON.parse(question.value.judgeCase);
      }
    } catch (error) {
      console.error("解析判例数据失败:", error);
    }
  } else {
    message.error("加载失败，" + res.message);
  }
};

// 添加默认代码模板
const getDefaultCode = (language: string): string => {
  const templates: Record<string, string> = {
    java: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 在这里编写你的代码
        
        scanner.close();
    }
}`,
    cpp: `#include <iostream>
using namespace std;

int main() {
    // 在这里编写你的代码
    
    return 0;
}`,
    python: `# 在这里编写你的代码

if __name__ == "__main__":
    # 读取输入
    n = int(input())
    # 处理逻辑
    
    # 输出结果
    
`,
  };
  return templates[language] || "";
};

// 修改表单的初始值
const form = ref<QuestionSubmitAddRequest>({
  questionId: props.id,
  language: "java",
  code: getDefaultCode("java"),
});

// 监听语言变化，更新默认代码
watch(
  () => form.value.language,
  (newLang) => {
    // 直接更新为新语言的默认代码模板
    form.value.code = getDefaultCode(newLang);
  }
);

// 修改代码编辑器的变更处理函数
const changeCode = (value: string) => {
  form.value.code = value;
};

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
        const answer = await QuestionControllerService.getAnswerByIdUsingGet(
          props.id
        );
        if (answer.code === 0 && answer.data) {
          answerContent.value = answer.data;
          canViewAnswer.value = true;
        } else {
          canViewAnswer.value = false;
          console.error("获取答案失败:", answer.message);
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

// 提交状态
const submitting = ref(false);
const judgeResult = ref<any>(null);
const submitId = ref<number | null>(null);
const pollInterval = ref<number | null>(null);

// 修改轮询获取判题结果的逻辑
const pollJudgeResult = async () => {
  if (!submitId.value || hasGotResult.value) return;

  try {
    const res =
      await QuestionControllerService.getQuestionSubmitJudgeInfoByIdUsingGet(
        submitId.value
      );
    if (res.code === 0 && res.data) {
      try {
        // 解析返回的字符串数据
        const dataStr = res.data;

        // 如果数据为空或只包含空白字符，说明判题还未完成
        if (!dataStr || dataStr.trim() === "") {
          return;
        }

        // 找到第一个数组的开始位置
        const arrayStartIndex = dataStr.indexOf("[");
        if (arrayStartIndex === -1) {
          // 如果找不到数组，说明数据格式不完整，继续等待
          return;
        }

        // 分离判题信息和测试用例结果
        const judgeInfoStr = dataStr.substring(0, arrayStartIndex);
        const testCaseResultsStr = dataStr.substring(arrayStartIndex);

        // 解析判题信息
        const judgeInfo = JSON.parse(judgeInfoStr);
        // 解析测试用例结果
        const testCaseResults = JSON.parse(testCaseResultsStr);

        // 如果判题信息为空，继续等待
        if (!judgeInfo || !judgeInfo.message) {
          return;
        }

        judgeResult.value = {
          ...judgeInfo,
          testCaseResults: testCaseResults,
        };

        // 如果判题完成，停止轮询并跳转到判题结果页签
        if (judgeInfo.message !== "判题中") {
          if (pollInterval.value) {
            clearInterval(pollInterval.value);
            pollInterval.value = null;
          }
          submitting.value = false;
          hasGotResult.value = true; // 标记已获取到结果
          // 自动跳转到判题结果页签
          activeJudgeTab.value = "result";
        }
      } catch (error) {
        // 解析失败时不立即停止轮询，继续等待
        console.log("等待判题结果...");
        return;
      }
    }
  } catch (error) {
    console.error("获取判题结果失败:", error);
    // 获取失败时不立即停止轮询，继续等待
    return;
  }
};

// 修改开始轮询的逻辑
const startPolling = (id: number) => {
  submitId.value = id;
  judgeResult.value = null; // 清空之前的判题结果
  hasGotResult.value = false; // 重置结果标志位

  // 先立即获取一次
  pollJudgeResult();

  // 每2秒轮询一次
  pollInterval.value = window.setInterval(() => {
    pollJudgeResult();
  }, 2000);

  // 设置超时处理
  setTimeout(() => {
    if (!hasGotResult.value) {
      // 只有在未获取到结果时才执行超时处理
      if (pollInterval.value) {
        clearInterval(pollInterval.value);
        pollInterval.value = null;
      }
      submitting.value = false;
      // 设置超时结果
      judgeResult.value = {
        message: "Time Limit Exceeded",
        time: POLL_TIMEOUT,
        memory: 0,
        testCaseResults: [],
      };
      // 自动跳转到判题结果页签
      activeJudgeTab.value = "result";
      Message.error("判题超时，请稍后重试");
    }
  }, POLL_TIMEOUT);
};

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!form.value.code) {
    message.error("请输入代码");
    return;
  }
  submitting.value = true;
  judgeResult.value = null;
  try {
    const res = await QuestionControllerService.doQuestionSubmitUsingPost(
      form.value
    );
    if (res.code === 0 && res.data) {
      message.success("提交成功");
      // 开始轮询判题结果
      startPolling(res.data);
    } else {
      submitting.value = false;
      message.error("提交失败，" + res.message);
    }
  } catch (error) {
    submitting.value = false;
    message.error("提交失败");
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(async () => {
  await loadData();
  // 如果有 tab 参数，切换到对应的页签
  const tab = route.query.tab;
  if (tab) {
    activeTab.value = tab as string;
  }

  // 如果有 page 参数，设置当前页码
  const page = route.query.page;
  if (page) {
    commentPagination.value.current = Number(page);
  }

  // 加载评论
  await loadComments();

  // 从本地存储中获取评分结果
  const currentUser = store.state.user.loginUser;
  const savedScore = localStorage.getItem(`aiScoreResult_${props.id}`);
  if (savedScore && currentUser) {
    try {
      const scoreData = JSON.parse(savedScore);
      // 检查是否是当前题目的评分结果，以及是否是当前用户的评分
      if (
        scoreData.questionId === props.id &&
        scoreData.userId === currentUser.id
      ) {
        aiScoreResult.value = scoreData.result;
      } else {
        // 如果不是当前用户的评分，清除缓存
        localStorage.removeItem(`aiScoreResult_${props.id}`);
      }
    } catch (error) {
      console.error("解析保存的评分结果失败:", error);
      localStorage.removeItem(`aiScoreResult_${props.id}`);
    }
  }
});

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

      // 检查是否有需要展开的评论
      const replyId = route.query.replyId;
      if (replyId) {
        // 直接找到对应的评论
        const targetComment = comments.value.find(
          (comment) => comment.id == replyId
        );
        if (targetComment) {
          targetComment.showReplyInput = true;
          // 滚动到目标评论
          setTimeout(() => {
            const element = document.getElementById(
              `comment-${targetComment.id}`
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

// 获取判题结果样式
const getJudgeResultClass = (message: string) => {
  if (message === "Accepted" || message === "成功") return "success";
  if (message === "Wrong Answer" || message === "答案错误") return "error";
  if (message === "Compile Error" || message === "编译错误") return "error";
  if (message === "Memory Limit Exceeded" || message === "内存溢出")
    return "error";
  if (message === "Time Limit Exceeded" || message === "超时") return "error";
  if (message === "Presentation Error" || message === "展示错误")
    return "error";
  if (message === "Output Limit Exceeded" || message === "输出溢出")
    return "error";
  if (message === "Dangerous Operation" || message === "危险操作")
    return "error";
  if (message === "Runtime Error" || message === "运行错误") return "error";
  if (message === "System Error" || message === "系统错误") return "error";
  return "error";
};

// 组件卸载时清理轮询
onUnmounted(() => {
  if (pollInterval.value) {
    clearInterval(pollInterval.value);
    pollInterval.value = null;
  }
});

// 添加AI评分相关的功能
const aiScoring = ref(false);
const aiScoreResult = ref<string>("");

// 格式化Markdown内容
const formatMarkdown = (markdown: string) => {
  return DOMPurify.sanitize(marked(markdown));
};

// 请求AI评分
const requestAIScore = async () => {
  if (!form.value.code) {
    message.error("请先编写代码");
    return;
  }

  const currentUser = store.state.user.loginUser;
  if (!currentUser) {
    message.error("请先登录");
    return;
  }

  aiScoring.value = true;
  try {
    const res = await QuestionControllerService.getAiScoreUsingPost({
      code: form.value.code,
      language: form.value.language,
      questionId: props.id,
    });

    if (res.code === 0 && res.data) {
      // 将评分结果保存到本地存储
      const scoreData = {
        result: res.data,
        timestamp: new Date().getTime(),
        questionId: props.id,
        userId: currentUser.id, // 添加用户ID
      };
      localStorage.setItem(
        `aiScoreResult_${props.id}`,
        JSON.stringify(scoreData)
      );
      aiScoreResult.value = res.data;
    } else {
      message.error("评分失败：" + res.message);
    }
  } catch (error) {
    message.error("评分失败，请稍后重试");
  } finally {
    aiScoring.value = false;
  }
};

// 监听用户登录状态变化
watch(
  () => store.state.user.loginUser,
  (newUser) => {
    if (!newUser) {
      // 用户退出登录时，清除所有AI评分缓存
      const keys = Object.keys(localStorage);
      keys.forEach((key) => {
        if (key.startsWith("aiScoreResult_")) {
          localStorage.removeItem(key);
        }
      });
      // 清空当前显示的评分结果
      aiScoreResult.value = "";
    }
  }
);

// 监听路由变化
watch(
  () => route.path,
  () => {
    // 当路由变化时，清空评分结果
    aiScoreResult.value = "";
  }
);
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
  padding: 16px;
  background-color: var(--color-fill-2);
  border-radius: 4px;
}

.question-answer pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
  color: var(--color-text-1);
}

.question-answer code {
  display: block;
  padding: 16px;
  background-color: var(--color-bg-2);
  border-radius: 4px;
  border: 1px solid var(--color-border);
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

.empty-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  background-color: #2a2d35;
  border-radius: 4px;
}

.judge-result {
  margin-top: 16px;
  background-color: #1d2129;
  border-radius: 4px;
  padding: 16px;
  color: #fff;
  width: 100%;
  box-sizing: border-box;
  min-height: 300px;
}

.tab-content {
  padding: 16px 0;
}

.test-case-content {
  padding: 16px;
  background-color: #2a2d35;
  border-radius: 4px;
}

.case-title {
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 12px;
}

.case-content {
  padding-left: 16px;
}

.case-item {
  display: flex;
  margin-bottom: 16px;
}

.case-item:last-child {
  margin-bottom: 0;
}

.case-label {
  color: #86909c;
  min-width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  padding-top: 4px;
}

.case-value {
  flex: 1;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  color: #fff;
  font-size: 14px;
  line-height: 1.6;
  background-color: #1d2129;
  padding: 12px;
  border-radius: 4px;
}

.case-value.success-result {
  color: #52c41a !important;
}

.case-value.error-result {
  color: #f53f3f !important;
}

.judge-result-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.judge-result-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.judge-result-item .label {
  color: #86909c;
  min-width: 80px;
  flex-shrink: 0;
}

.judge-result-item .value {
  font-weight: 500;
  flex: 1;
  word-break: break-all;
}

.judge-result-item .value.success {
  color: #52c41a !important;
  font-size: 16px;
  font-weight: 600;
}

.judge-result-item .value.error {
  color: #f53f3f !important;
  font-size: 16px;
  font-weight: 600;
}

.judge-result-item .value.warning {
  color: #faad14 !important;
  font-size: 16px;
  font-weight: 600;
}

.ai-score-content {
  position: relative;
  min-height: 200px;
}

.ai-score-header {
  position: absolute;
  top: 0;
  right: 0;
  padding: 16px;
  z-index: 1;
}

.ai-score-result {
  padding: 24px;
  background-color: #1d2129;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  border: 1px solid #2b2f3a;
}

.markdown-content {
  color: #e5e7eb;
  line-height: 1.8;
  font-size: 15px;
}

.markdown-content h1 {
  color: #7ee787;
  font-size: 28px;
  margin: 32px 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #30363d;
}

.markdown-content h2 {
  color: #79c0ff;
  font-size: 24px;
  margin: 28px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #30363d;
}

.markdown-content h3 {
  color: #d2a8ff;
  font-size: 20px;
  margin: 24px 0 16px;
}

.markdown-content p {
  margin: 16px 0;
  line-height: 1.8;
}

.markdown-content ul,
.markdown-content ol {
  padding-left: 24px;
  margin: 16px 0;
}

.markdown-content li {
  margin: 8px 0;
  line-height: 1.6;
  position: relative;
}

.markdown-content li::before {
  content: "•";
  color: #58a6ff;
  position: absolute;
  left: -18px;
  font-weight: bold;
}

.markdown-content code {
  background-color: #2d333b;
  padding: 3px 6px;
  border-radius: 4px;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 14px;
  color: #ff7b72;
  border: 1px solid #444c56;
}

.markdown-content pre {
  background-color: #2d333b;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
  overflow-x: auto;
  border: 1px solid #444c56;
}

.markdown-content pre code {
  background-color: transparent;
  padding: 0;
  border: none;
  color: #e5e7eb;
  font-size: 14px;
  line-height: 1.6;
}

.markdown-content strong {
  color: #ff7b72;
  font-weight: 600;
}

.markdown-content em {
  color: #d2a8ff;
  font-style: italic;
}

.markdown-content blockquote {
  border-left: 4px solid #58a6ff;
  padding: 12px 20px;
  margin: 20px 0;
  background-color: #2d333b;
  border-radius: 0 8px 8px 0;
  color: #8b949e;
}

.markdown-content hr {
  border: none;
  height: 2px;
  background: linear-gradient(to right, #30363d, #58a6ff, #30363d);
  margin: 32px 0;
}

.markdown-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background-color: #2d333b;
  border-radius: 8px;
  overflow: hidden;
}

.markdown-content th {
  background-color: #444c56;
  color: #e5e7eb;
  font-weight: 600;
  padding: 12px 16px;
  text-align: left;
}

.markdown-content td {
  padding: 12px 16px;
  border-top: 1px solid #444c56;
  color: #e5e7eb;
}

.markdown-content tr:hover {
  background-color: #2b2f3a;
}

/* 添加评分结果的特殊样式 */
.markdown-content .score-section {
  background-color: #2d333b;
  padding: 16px 20px;
  border-radius: 8px;
  margin: 20px 0;
  border: 1px solid #444c56;
}

.markdown-content .score-value {
  font-size: 24px;
  font-weight: bold;
  color: #7ee787;
  margin: 8px 0;
}

.markdown-content .improvement {
  color: #ff7b72;
  font-weight: 500;
}

.markdown-content .success {
  color: #7ee787;
  font-weight: 500;
}

.markdown-content .warning {
  color: #e3b341;
  font-weight: 500;
}

/* 添加动画效果 */
.ai-score-result {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 代码块行号样式 */
.markdown-content pre {
  counter-reset: line;
}

.markdown-content pre code {
  display: block;
  position: relative;
  padding-left: 40px;
}

.markdown-content pre code::before {
  counter-increment: line;
  content: counter(line);
  position: absolute;
  left: -40px;
  width: 30px;
  text-align: right;
  color: #484f58;
  padding-right: 10px;
  border-right: 1px solid #444c56;
  user-select: none;
}

.score-tooltip {
  color: #f53f3f;
  font-size: 14px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: 0.5px;
}

:deep(.arco-tooltip-content) {
  background-color: #2a2d35;
  border: 1px solid #424242;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>
