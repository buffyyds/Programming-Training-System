<template>
  <div class="wrong-question-analysis">
    <a-card title="错题标记情况分析">
      <a-row :gutter="20">
        <a-col :span="12">
          <a-card title="错题标记情况">
            <template #extra>
              <a-radio-group v-model="leftChartType" type="button">
                <a-radio value="bar">柱状图</a-radio>
                <a-radio value="pie">饼图</a-radio>
              </a-radio-group>
            </template>
            <div class="chart-container">
              <v-chart :option="leftChartOption" autoresize />
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="错误提交情况">
            <template #extra>
              <a-radio-group v-model="rightChartType" type="button">
                <a-radio value="bar">柱状图</a-radio>
                <a-radio value="line">折线图</a-radio>
              </a-radio-group>
            </template>
            <div class="chart-container">
              <v-chart :option="rightChartOption" autoresize />
            </div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart, PieChart, LineChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { WrongQuestionControllerService } from "../../../generated";
import { Message } from "@arco-design/web-vue";

interface WrongQuestionAnalysis {
  questionName: string;
  wrongQuestionStudentCount: number;
  wrongQuestionStudentCountPercent: string;
  totalWrongSubmitCount: number;
}

use([
  CanvasRenderer,
  BarChart,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
]);

const loading = ref(false);
const analysisData = ref<WrongQuestionAnalysis[]>([]);
const leftChartType = ref("bar");
const rightChartType = ref("bar");

const loadAnalysisData = async () => {
  try {
    loading.value = true;
    const res =
      await WrongQuestionControllerService.getWrongQuestionAnalysisUsingGet();
    if (res.code === 0 && res.data) {
      analysisData.value = res.data.map((item) => ({
        questionName: item.questionName || "",
        wrongQuestionStudentCount: item.wrongQuestionStudentCount || 0,
        wrongQuestionStudentCountPercent:
          item.wrongQuestionStudentCountPercent || "0%",
        totalWrongSubmitCount: item.totalWrongSubmitCount || 0,
      }));
    } else {
      Message.error(res.message || "获取数据失败");
    }
  } catch (error) {
    Message.error("获取数据失败");
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 左侧图表配置
const leftChartOption = computed(() => {
  const questionNames = analysisData.value.map((item) => item.questionName);
  const wrongCounts = analysisData.value.map(
    (item) => item.wrongQuestionStudentCount
  );
  const percents = analysisData.value.map(
    (item) => item.wrongQuestionStudentCountPercent
  );

  if (leftChartType.value === "bar") {
    return {
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "shadow",
        },
      },
      legend: {
        data: ["标记错题学生数", "标记错题比例"],
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "3%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        data: questionNames,
        axisLabel: {
          interval: 0,
          rotate: 30,
        },
      },
      yAxis: [
        {
          type: "value",
          name: "学生数",
          axisLabel: {
            formatter: "{value}",
          },
        },
        {
          type: "value",
          name: "比例",
          axisLabel: {
            formatter: "{value}%",
          },
        },
      ],
      series: [
        {
          name: "标记错题学生数",
          type: "bar",
          data: wrongCounts,
        },
        {
          name: "标记错题比例",
          type: "line",
          yAxisIndex: 1,
          data: percents.map((p) => parseFloat(p)),
        },
      ],
    };
  } else {
    return {
      tooltip: {
        trigger: "item",
        formatter: "{b}: {c} ({d}%)",
      },
      legend: {
        orient: "vertical",
        left: "left",
      },
      series: [
        {
          name: "错题标记情况",
          type: "pie",
          radius: "50%",
          data: questionNames.map((name, index) => ({
            name,
            value: wrongCounts[index],
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        },
      ],
    };
  }
});

// 右侧图表配置
const rightChartOption = computed(() => {
  const questionNames = analysisData.value.map((item) => item.questionName);
  const wrongSubmitCounts = analysisData.value.map(
    (item) => item.totalWrongSubmitCount
  );

  if (rightChartType.value === "bar") {
    return {
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "shadow",
        },
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "3%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        data: questionNames,
        axisLabel: {
          interval: 0,
          rotate: 30,
        },
      },
      yAxis: {
        type: "value",
        name: "错误提交数",
      },
      series: [
        {
          name: "错误提交数",
          type: "bar",
          data: wrongSubmitCounts,
        },
      ],
    };
  } else {
    return {
      tooltip: {
        trigger: "axis",
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "3%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        data: questionNames,
        axisLabel: {
          interval: 0,
          rotate: 30,
        },
      },
      yAxis: {
        type: "value",
        name: "错误提交数",
      },
      series: [
        {
          name: "错误提交数",
          type: "line",
          data: wrongSubmitCounts,
          smooth: true,
          symbol: "circle",
          symbolSize: 8,
          lineStyle: {
            width: 3,
          },
        },
      ],
    };
  }
});

onMounted(() => {
  loadAnalysisData();
});
</script>

<style scoped>
.wrong-question-analysis {
  padding: 20px;
}

.chart-container {
  height: 400px;
  width: 100%;
}
</style>
