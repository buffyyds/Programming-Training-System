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
          <a-tab-pane key="comment" title="评论"> 评论区</a-tab-pane>
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
</style>
