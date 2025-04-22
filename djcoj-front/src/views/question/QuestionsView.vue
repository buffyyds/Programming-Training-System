<template>
  <div id="questionsView">
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="title" label="名称" style="min-width: 240px">
        <a-input
          v-model="searchParams.title"
          placeholder="请输入名称"
          allow-clear
          @press-enter="doSubmit"
        />
      </a-form-item>
      <a-form-item field="tags" label="标签" style="min-width: 240px">
        <a-input-tag
          v-model="searchParams.tags"
          placeholder="请输入标签"
          allow-clear
          :options="allTags"
          @press-enter="doSubmit"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">提交</a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-table
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #status="{ record }">
        <a-tooltip
          :content="record.isWrongQuestion ? '已标记为错题' : '未标记为错题'"
          position="right"
        >
          <div
            class="wrong-question-indicator"
            :class="{ 'is-wrong': record.isWrongQuestion }"
          >
            <icon-exclamation-circle-fill v-if="record.isWrongQuestion" />
            <icon-check-circle-fill v-else />
          </div>
        </a-tooltip>
      </template>
      <template #tags="{ record }">
        <a-space wrap>
          <a-tag v-for="(tag, index) of record.tags" :key="index" color="green"
            >{{ tag }}
          </a-tag>
        </a-space>
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD") }}
      </template>
      <template #optional="{ record }">
        <a-space>
          <a-tag
            :color="record.isCompletion ? 'green' : 'red'"
            class="completion-tag"
          >
            <template #icon>
              <icon-check-circle-fill v-if="record.isCompletion" />
              <icon-close-circle-fill v-else />
            </template>
            {{ record.isCompletion ? "已完成" : "未完成" }}
          </a-tag>
          <a-button type="primary" @click="toQuestionPage(record)">
            做题
          </a-button>
          <template v-if="isStudent">
            <a-button
              v-if="record.isWrongQuestion && record.isCompletion"
              type="outline"
              status="danger"
              @click="unDoWrongQuestion(record.id)"
            >
              <template #icon>
                <icon-close />
              </template>
              取消错题标记
            </a-button>
            <a-button
              v-else-if="!record.isWrongQuestion"
              type="outline"
              status="warning"
              @click="doWrongQuestion(record.id)"
            >
              <template #icon>
                <icon-exclamation />
              </template>
              标记错题
            </a-button>
          </template>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect, computed } from "vue";
import {
  Page_Question_,
  Question,
  QuestionControllerService,
  QuestionQueryRequest,
  WrongQuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import * as querystring from "querystring";
import { useRouter } from "vue-router";
import moment from "moment";
import {
  IconCheckCircleFill,
  IconCloseCircleFill,
  IconExclamationCircleFill,
  IconExclamation,
  IconClose,
} from "@arco-design/web-vue/es/icon";
import { useStore } from "vuex";

const store = useStore();
const isStudent = computed(
  () => store.state.user.loginUser?.userRole === "user"
);

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<QuestionQueryRequest>({
  title: "",
  tags: [],
  pageSize: 8,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

// {id: "1", title: "A+ D", content: "新的题目内容", tags: "["二叉树"]", answer: "新的答案", submitNum: 0,…}

const columns = [
  ...(isStudent.value
    ? [
        {
          title: "状态",
          slotName: "status",
          width: 60,
        },
      ]
    : []),
  {
    title: "题号",
    dataIndex: "id",
  },
  {
    title: "题目名称",
    dataIndex: "title",
  },
  {
    title: "标签",
    slotName: "tags",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const router = useRouter();

/**
 * 跳转到做题页面
 * @param question
 */
const toQuestionPage = (question: Question) => {
  router.push({
    path: `/view/question/${question.id}`,
  });
};

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};

// 预定义的所有可用标签
const allTags = [
  // 数据结构
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
  // 算法思想
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
  // 算法技巧
  "位运算",
  "数学",
  "模拟",
  "前缀和",
  "差分",
  "状态压缩",
  "记忆化搜索",
  "剪枝",
  "拓扑排序",
  // 难度分类
  "简单",
  "中等",
  "困难",
  // 高级算法
  "线段树",
  "树状数组",
  "最短路",
  "最小生成树",
  "网络流",
  "KMP",
  "马拉车算法",
  "AC自动机",
  // 实际应用
  "设计",
  "数据库",
  "操作系统",
  "计算机网络",
  "系统设计",
  "多线程",
  "分布式",
];

// 标记错题
const doWrongQuestion = async (questionId: string) => {
  try {
    const res = await WrongQuestionControllerService.doWrongQuestionUsingGet(
      questionId
    );
    if (res.code === 0) {
      message.success("标记成功");
      loadData(); // 重新加载数据
    } else {
      message.error("标记失败：" + res.message);
    }
  } catch (error) {
    message.error("标记失败");
  }
};

// 取消错题标记
const unDoWrongQuestion = async (questionId: string) => {
  try {
    const res = await WrongQuestionControllerService.unDoWrongQuestionUsingGet(
      questionId
    );
    if (res.code === 0) {
      message.success("取消标记成功");
      loadData(); // 重新加载数据
    } else {
      message.error("取消标记失败：" + res.message);
    }
  } catch (error) {
    message.error("取消标记失败");
  }
};
</script>

<style scoped>
#questionsView {
  max-width: 1280px;
  margin: 0 auto;
}

:deep(.arco-input-tag) {
  background-color: var(--color-bg-2);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  transition: all 0.2s;
}

:deep(.arco-input-tag:hover) {
  border-color: rgb(var(--primary-6));
}

:deep(.arco-input-tag-inner) {
  padding: 4px 8px;
}

:deep(.arco-tag) {
  margin: 2px;
}

.completion-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.wrong-question-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transition: all 0.3s;
}

.wrong-question-indicator.is-wrong {
  color: rgb(var(--red-6));
  background-color: rgba(var(--red-1), 0.1);
}

.wrong-question-indicator:not(.is-wrong) {
  color: rgb(var(--green-6));
  background-color: rgba(var(--green-1), 0.1);
}

:deep(.arco-tag-icon) {
  margin-right: 4px;
}

:deep(.arco-btn) {
  margin-left: 8px;
}
</style>
