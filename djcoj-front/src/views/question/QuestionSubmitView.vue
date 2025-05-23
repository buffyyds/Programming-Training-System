<template>
  <div id="questionSubmitView">
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="title" label="题目" style="min-width: 240px">
        <a-input
          v-model="searchParams.title"
          placeholder="请输入题目"
          allow-clear
          @press-enter="doSubmit"
        />
      </a-form-item>
      <a-form-item field="language" label="编程语言" style="min-width: 240px">
        <a-select
          v-model="searchParams.language"
          :style="{ width: '320px' }"
          placeholder="选择编程语言"
          allow-clear
        >
          <a-option>java</a-option>
          <a-option>cpp</a-option>
          <a-option>python</a-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider :size="0" />
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
      <template #judgeResult="{ record }">
        <a-space>
          <icon-check-circle-fill
            v-if="
              record.judgeInfo.message === 'Accepted' ||
              record.judgeInfo.message === '成功'
            "
            style="color: #52c41a"
          />
          <icon-close-circle-fill v-else style="color: #f5222d" />
          {{ getJudgeResultText(record.judgeInfo.message) }}
        </a-space>
      </template>
      <template #memory="{ record }">
        {{ record.judgeInfo.memory + " KB" }}
      </template>
      <template #time="{ record }">
        {{ record.judgeInfo.time + " ms" }}
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import {
  Question,
  QuestionControllerService,
  QuestionSubmitQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";
import { useStore } from "vuex";
import {
  IconCheckCircleFill,
  IconCloseCircleFill,
} from "@arco-design/web-vue/es/icon";

const store = useStore();
const tableRef = ref();

const dataList = ref([]);
const total = ref(0);

// 扩展 QuestionSubmitQueryRequest 类型以包含 title
interface ExtendedQuestionSubmitQueryRequest
  extends QuestionSubmitQueryRequest {
  title?: string;
}

const searchParams = ref<ExtendedQuestionSubmitQueryRequest>({
  title: undefined,
  language: undefined,
  pageSize: 10,
  current: 1,
  userId: undefined, // 添加userId字段
});

// 判题结果映射类型
type JudgeResult =
  | "Accepted"
  | "Wrong Answer"
  | "Compile Error"
  | "Memory Limit Exceeded"
  | "Time Limit Exceeded"
  | "Presentation Error"
  | "Waiting"
  | "Output Limit Exceeded"
  | "Dangerous Operation"
  | "Runtime Error"
  | "System Error";

// 判题结果映射
const judgeResultMap: Record<JudgeResult, string> = {
  Accepted: "成功",
  "Wrong Answer": "答案错误",
  "Compile Error": "编译错误",
  "Memory Limit Exceeded": "内存溢出",
  "Time Limit Exceeded": "超时",
  "Presentation Error": "展示错误",
  Waiting: "等待中",
  "Output Limit Exceeded": "输出溢出",
  "Dangerous Operation": "危险操作",
  "Runtime Error": "运行错误",
  "System Error": "系统错误",
};

// 获取判题结果的中文描述
const getJudgeResultText = (message: string) => {
  return judgeResultMap[message as JudgeResult] || message;
};

const loadData = async () => {
  try {
    // 获取当前登录用户信息
    const currentUser = store.state.user.loginUser;
    if (!currentUser) {
      message.error("请先登录");
      return;
    }

    // 设置当前用户ID
    searchParams.value.userId = currentUser.id;

    let res;
    // 根据用户角色调用不同的接口
    if (currentUser.userRole === "teacher") {
      // 教师调用教师接口
      res =
        await QuestionControllerService.listQuestionSubmitByPageTeacherUsingPost(
          {
            ...searchParams.value,
            sortField: "createTime",
            sortOrder: "descend",
          }
        );
    } else {
      // 学生调用普通接口
      res = await QuestionControllerService.listQuestionSubmitByPageUsingPost({
        ...searchParams.value,
        sortField: "createTime",
        sortOrder: "descend",
      });
    }

    if (res.code === 0) {
      dataList.value = res.data.records;
      total.value = res.data.total;
    } else {
      message.error("加载失败，" + res.message);
    }
  } catch (error) {
    message.error("加载失败，请稍后重试");
    console.error("加载数据失败:", error);
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const columns = [
  {
    title: "题目",
    dataIndex: "questionVO.title",
  },
  {
    title: "编程语言",
    dataIndex: "language",
  },
  {
    title: "判题结果",
    slotName: "judgeResult",
  },
  {
    title: "消耗内存",
    slotName: "memory",
  },
  {
    title: "花费时间",
    slotName: "time",
  },
  {
    title: "提交者",
    dataIndex: "userVO.userName",
  },
  {
    title: "提交时间",
    slotName: "createTime",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
  loadData();
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
  loadData();
};
</script>

<style scoped>
#questionSubmitView {
  max-width: 1280px;
  margin: 0 auto;
}

:deep(.arco-table-th) {
  background-color: var(--color-fill-2) !important;
}

:deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-2);
}

:deep(.arco-input-wrapper) {
  background-color: var(--color-bg-2);
  border: 1px solid var(--color-border);
  transition: all 0.2s;
}

:deep(.arco-input-wrapper:hover) {
  border-color: rgb(var(--primary-6));
}

:deep(.arco-select-view) {
  background-color: var(--color-bg-2);
  border: 1px solid var(--color-border);
  transition: all 0.2s;
}

:deep(.arco-select-view:hover) {
  border-color: rgb(var(--primary-6));
}
</style>
