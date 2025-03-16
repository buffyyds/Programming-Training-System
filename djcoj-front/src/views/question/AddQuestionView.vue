<template>
  <div id="addQuestionView">
    <h2>{{ isUpdate ? "修改题目" : "创建题目" }}</h2>
    <a-form :model="form" label-align="left">
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item field="tags" label="标签">
        <div class="tag-selector">
          <div class="selected-tags">
            <a-space wrap>
              <a-tag
                v-for="tag in form.tags"
                :key="tag"
                closable
                @close="form.tags = form.tags.filter((t) => t !== tag)"
              >
                {{ tag }}
              </a-tag>
            </a-space>
          </div>
          <a-button type="outline" @click="openTagDialog"> 选择标签</a-button>
        </div>
      </a-form-item>
      <a-form-item field="content" label="题目内容">
        <MdEditor :value="form.content" :handle-change="onContentChange" />
      </a-form-item>
      <a-form-item field="answer" label="答案">
        <MdEditor :value="form.answer" :handle-change="onAnswerChange" />
      </a-form-item>
      <a-form-item label="判题配置" :content-flex="false" :merge-props="false">
        <a-space direction="vertical" style="min-width: 480px">
          <a-form-item field="judgeConfig.timeLimit" label="时间限制">
            <a-input-number
              :model-value="Number(form.judgeConfig.timeLimit)"
              @change="(value) => (form.judgeConfig.timeLimit = Number(value))"
              placeholder="请输入时间限制"
              mode="button"
              :min="0"
              size="large"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.memoryLimit" label="内存限制">
            <a-input-number
              :model-value="Number(form.judgeConfig.memoryLimit)"
              @change="
                (value) => (form.judgeConfig.memoryLimit = Number(value))
              "
              placeholder="请输入内存限制"
              mode="button"
              :min="0"
              size="large"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.stackLimit" label="堆栈限制">
            <a-input-number
              :model-value="Number(form.judgeConfig.stackLimit)"
              @change="(value) => (form.judgeConfig.stackLimit = Number(value))"
              placeholder="请输入堆栈限制"
              mode="button"
              :min="0"
              size="large"
            />
          </a-form-item>
        </a-space>
      </a-form-item>
      <a-form-item
        label="测试用例配置"
        :content-flex="false"
        :merge-props="false"
      >
        <a-form-item
          v-for="(judgeCaseItem, index) of form.judgeCase"
          :key="index"
          no-style
        >
          <a-space direction="vertical" style="min-width: 640px">
            <a-form-item
              :field="`form.judgeCase[${index}].input`"
              :label="`输入用例-${index}`"
              :key="index"
            >
              <a-textarea
                v-model="judgeCaseItem.input"
                placeholder="请输入测试输入用例"
                :auto-size="{
                  minRows: 3,
                  maxRows: 5,
                }"
                allow-clear
                style="width: 100%; border-radius: 4px; resize: vertical"
              />
            </a-form-item>
            <a-form-item
              :field="`form.judgeCase[${index}].output`"
              :label="`输出用例-${index}`"
              :key="index"
            >
              <a-textarea
                v-model="judgeCaseItem.output"
                placeholder="请输入测试输出用例"
                :auto-size="{
                  minRows: 3,
                  maxRows: 5,
                }"
                allow-clear
                style="width: 100%; border-radius: 4px; resize: vertical"
              />
            </a-form-item>
            <div class="test-case-actions">
              <a-button status="danger" @click="handleDelete(index)">
                删除
              </a-button>
            </div>
          </a-space>
        </a-form-item>
        <div style="margin-top: 32px">
          <a-button @click="handleAdd" type="outline" status="success"
            >新增测试用例
          </a-button>
        </div>
      </a-form-item>
      <div style="margin-top: 16px" />
      <a-form-item>
        <a-button type="primary" style="min-width: 200px" @click="doSubmit"
          >{{ isUpdate ? "修改" : "提交" }}
        </a-button>
      </a-form-item>
    </a-form>

    <!-- 标签选择弹窗 -->
    <a-modal
      v-model:visible="tagDialogVisible"
      title="选择标签"
      @ok="confirmTagSelection"
      @cancel="cancelTagSelection"
      :mask-closable="false"
      :width="800"
    >
      <div class="tag-categories">
        <div
          v-for="category in algorithmTags"
          :key="category.type"
          class="tag-category"
        >
          <h4>{{ category.type }}</h4>
          <div class="tag-list">
            <a-tag
              v-for="tag in category.tags"
              :key="tag"
              :class="{ 'tag-selected': selectedTags.includes(tag) }"
              @click="toggleTag(tag)"
              style="margin: 4px; cursor: pointer"
            >
              {{ tag }}
            </a-tag>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRoute, useRouter } from "vue-router";
import { Modal } from "@arco-design/web-vue";

interface JudgeConfig {
  memoryLimit: number;
  stackLimit: number;
  timeLimit: number;
}

interface JudgeCase {
  input: string;
  output: string;
}

interface QuestionForm {
  id?: number;
  title: string;
  tags: string[];
  answer: string;
  content: string;
  judgeConfig: JudgeConfig;
  judgeCase: JudgeCase[];
}

// 预定义的算法题目标签库
const algorithmTags = [
  // 数据结构
  {
    type: "数据结构",
    tags: [
      "数组",
      "链表",
      "栈",
      "队列",
      "哈希表",
      "树",
      "二叉树",
      "二叉搜索树",
      "堆",
      "优先队列",
      "图",
      "字符串",
      "前缀树",
      "并查集",
    ],
  },
  // 算法思想
  {
    type: "算法思想",
    tags: [
      "动态规划",
      "贪心",
      "分治",
      "回溯",
      "递归",
      "深度优先搜索",
      "广度优先搜索",
      "双指针",
      "滑动窗口",
      "二分查找",
      "排序",
    ],
  },
  // 算法技巧
  {
    type: "算法技巧",
    tags: [
      "位运算",
      "数学",
      "模拟",
      "前缀和",
      "差分",
      "状态压缩",
      "记忆化搜索",
      "剪枝",
      "拓扑排序",
    ],
  },
  // 难度分类
  { type: "难度", tags: ["简单", "中等", "困难"] },
  // 高级算法
  {
    type: "高级算法",
    tags: [
      "线段树",
      "树状数组",
      "最短路",
      "最小生成树",
      "网络流",
      "KMP",
      "马拉车算法",
      "AC自动机",
    ],
  },
  // 实际应用
  {
    type: "实际应用",
    tags: [
      "设计",
      "数据库",
      "操作系统",
      "计算机网络",
      "系统设计",
      "多线程",
      "分布式",
    ],
  },
];

// 控制标签选择弹窗的显示
const tagDialogVisible = ref(false);

// 临时存储选中的标签
const selectedTags = ref<string[]>([]);

const router = useRouter();
const route = useRoute();

// 判断是否是修改模式
const isUpdate = computed(() => route.query.isUpdate === "true");

// 初始表单数据
const initFormData: QuestionForm = {
  title: "",
  tags: [],
  answer: "",
  content: "",
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
};

const form = ref<QuestionForm>({ ...initFormData });

// 重置表单数据
const resetForm = () => {
  form.value = { ...initFormData };
  // 重置编辑器内容
  onContentChange("");
  onAnswerChange("");
  // 重置标签选择
  selectedTags.value = [];
  // 重置弹窗状态
  tagDialogVisible.value = false;
  // 滚动到页面顶部
  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });
  // 显示提示
  message.success("已重置表单，可以开始新建题目");
};

// 返回主页
const goHome = () => {
  router.push("/");
};

// 打开标签选择弹窗
const openTagDialog = () => {
  selectedTags.value = [...form.value.tags];
  tagDialogVisible.value = true;
};

// 确认标签选择
const confirmTagSelection = () => {
  form.value.tags = [...selectedTags.value];
  tagDialogVisible.value = false;
};

// 取消标签选择
const cancelTagSelection = () => {
  tagDialogVisible.value = false;
  selectedTags.value = [...form.value.tags];
};

// 切换标签选择状态
const toggleTag = (tag: string) => {
  const index = selectedTags.value.indexOf(tag);
  if (index === -1) {
    selectedTags.value.push(tag);
  } else {
    selectedTags.value.splice(index, 1);
  }
};

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    // 当进入创建题目页面时，重置表单
    if (newPath === "/add/question") {
      resetForm();
    }
  }
);

/**
 * 根据题目 id 获取老的数据
 */
const loadData = async () => {
  const id = route.query.id;
  if (!id) {
    return;
  }
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    id as string
  );
  if (res.code === 0 && res.data) {
    const judgeConfig = res.data.judgeConfig
      ? (() => {
          const config = JSON.parse(res.data.judgeConfig);
          return {
            memoryLimit: Number(config.memoryLimit),
            stackLimit: Number(config.stackLimit),
            timeLimit: Number(config.timeLimit),
          };
        })()
      : initFormData.judgeConfig;
    const judgeCase = res.data.judgeCase
      ? JSON.parse(res.data.judgeCase)
      : initFormData.judgeCase;
    const tags = res.data.tags ? JSON.parse(res.data.tags) : initFormData.tags;

    form.value = {
      id: res.data.id,
      title: res.data.title || "",
      content: res.data.content || "",
      answer: res.data.answer || "",
      judgeConfig,
      judgeCase,
      tags,
    };
  } else {
    message.error("加载失败，" + res.message);
  }
};

onMounted(() => {
  loadData();
});

const doSubmit = async () => {
  console.log(form.value);
  // 区分更新还是创建
  if (isUpdate.value) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("更新成功");
    } else {
      message.error("更新失败，" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      Modal.success({
        title: "创建成功",
        content: "题目已成功创建，您可以选择返回主页或继续创建新题目。",
        okText: "返回主页",
        cancelText: "继续创建",
        hideCancel: false,
        closable: false,
        maskClosable: false,
        onOk: () => {
          goHome();
        },
        onCancel: () => {
          resetForm();
        },
      });
    } else {
      message.error("创建失败，" + res.message);
    }
  }
};

/**
 * 新增判题用例
 */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};

/**
 * 删除判题用例
 */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};

const onContentChange = (value: string) => {
  form.value.content = value;
};

const onAnswerChange = (value: string) => {
  form.value.answer = value;
};
</script>

<style scoped>
#addQuestionView {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.tag-selector {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.selected-tags {
  flex: 1;
  min-height: 32px;
  padding: 4px 0;
}

.tag-categories {
  max-height: 60vh;
  overflow-y: auto;
}

.tag-category {
  margin-bottom: 20px;
}

.tag-category h4 {
  margin: 0 0 8px 0;
  color: #1d2129;
  font-weight: 500;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-selected {
  background-color: #165dff !important;
  color: white !important;
  border-color: #165dff !important;
}

:deep(.arco-tag) {
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.arco-tag:hover) {
  opacity: 0.8;
}

.test-case-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

:deep(.arco-textarea-wrapper) {
  background-color: var(--color-fill-2);
  transition: all 0.2s;
}

:deep(.arco-textarea-wrapper:hover) {
  background-color: var(--color-fill-3);
}

:deep(.arco-textarea) {
  font-family: Monaco, Consolas, "Courier New", monospace;
}

/* 添加弹窗样式 */
:deep(.arco-modal-footer) {
  display: flex;
  justify-content: center;
  gap: 16px;
}

:deep(.arco-modal-footer .arco-btn) {
  min-width: 120px;
}
</style>
