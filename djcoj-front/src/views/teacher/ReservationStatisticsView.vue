<template>
  <div class="statistics-container">
    <a-card class="statistics-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <a-button type="text" @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
            </a-button>
            <icon-bar-chart class="title-icon" />
            预约情况统计图表
          </a-space>
        </div>
      </template>

      <!-- 图表类型选择 -->
      <div class="chart-type-selector">
        <a-radio-group v-model="chartType" type="button">
          <a-radio value="bar">柱状图</a-radio>
          <a-radio value="pie">饼图</a-radio>
        </a-radio-group>
      </div>

      <!-- 图表展示区域 -->
      <div class="charts-container">
        <div class="charts-row">
          <!-- 预约次数统计 -->
          <div class="chart-wrapper">
            <div class="chart-title">预约次数排名（前10名）</div>
            <div class="chart-content">
              <v-chart
                v-if="chartType === 'bar'"
                :option="countBarOption"
                autoresize
              />
              <v-chart v-else :option="countPieOption" autoresize />
            </div>
          </div>

          <a-divider direction="vertical" />

          <!-- 预约时长统计 -->
          <div class="chart-wrapper">
            <div class="chart-title">预约时长排名（前10名）</div>
            <div class="chart-content">
              <v-chart
                v-if="chartType === 'bar'"
                :option="timeBarOption"
                autoresize
              />
              <v-chart v-else :option="timePieOption" autoresize />
            </div>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useStore } from "vuex";
import { Message } from "@arco-design/web-vue";
import { IconArrowLeft, IconBarChart } from "@arco-design/web-vue/es/icon";
import { ReservationControllerService } from "../../../generated";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart, PieChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import router from "@/router";

// 注册必要的组件
use([
  CanvasRenderer,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
]);

const store = useStore();
const loading = ref(false);
const chartType = ref("bar");
const countData = ref<any[]>([]);
const timeData = ref<any[]>([]);

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true;
  try {
    // 获取预约次数排名
    const countRes =
      await ReservationControllerService.getTopTenStudentReservationByCountUsingGet();
    if (countRes.code === 0 && countRes.data) {
      countData.value = countRes.data;
    }

    // 获取预约时长排名
    const timeRes =
      await ReservationControllerService.getTopTenStudentReservationByTotleTimeUsingGet();
    if (timeRes.code === 0 && timeRes.data) {
      timeData.value = timeRes.data;
    }
  } catch (error) {
    console.error("获取统计数据失败:", error);
    Message.error("获取统计数据失败");
  } finally {
    loading.value = false;
  }
};

// 预约次数柱状图配置
const countBarOption = computed(() => {
  const names = countData.value.map((item) => item.studentUser.userName);
  const values = countData.value.map((item) => item.reservationCount);

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
      data: names,
      axisLabel: {
        interval: 0,
        rotate: 30,
      },
    },
    yAxis: {
      type: "value",
      name: "预约次数",
    },
    series: [
      {
        name: "预约次数",
        type: "bar",
        data: values,
        itemStyle: {
          color: "#165DFF",
        },
      },
    ],
  };
});

// 预约次数饼图配置
const countPieOption = computed(() => {
  const data = countData.value.map((item) => ({
    name: item.studentUser.userName,
    value: item.reservationCount,
  }));

  return {
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} ({d}%)",
    },
    legend: {
      orient: "vertical",
      right: 10,
      top: "center",
    },
    series: [
      {
        name: "预约次数",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: "18",
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: data,
      },
    ],
  };
});

// 预约时长柱状图配置
const timeBarOption = computed(() => {
  const names = timeData.value.map((item) => item.studentUser.userName);
  const values = timeData.value.map((item) => {
    const [hours, minutes] = item.totalTime.split(":");
    return parseInt(hours) + parseInt(minutes) / 60;
  });

  return {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
      formatter: "{b}: {c} 小时",
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: names,
      axisLabel: {
        interval: 0,
        rotate: 30,
      },
    },
    yAxis: {
      type: "value",
      name: "预约时长（小时）",
    },
    series: [
      {
        name: "预约时长",
        type: "bar",
        data: values,
        itemStyle: {
          color: "#14C9C9",
        },
      },
    ],
  };
});

// 返回上一页
const goBack = () => {
  router.back();
};

// 预约时长饼图配置
const timePieOption = computed(() => {
  const data = timeData.value.map((item) => {
    const [hours, minutes] = item.totalTime.split(":");
    return {
      name: item.studentUser.userName,
      value: parseInt(hours) + parseInt(minutes) / 60,
    };
  });

  return {
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} 小时 ({d}%)",
    },
    legend: {
      orient: "vertical",
      right: 10,
      top: "center",
    },
    series: [
      {
        name: "预约时长",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: "18",
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: data,
      },
    ],
  };
});

onMounted(() => {
  fetchStatistics();
});
</script>

<style scoped>
.statistics-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
}

.statistics-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.title-icon {
  margin-right: 8px;
  font-size: 20px;
}

.chart-type-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.charts-row {
  display: flex;
  gap: 20px;
  align-items: stretch;
}

.chart-wrapper {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
}

.chart-content {
  flex: 1;
  height: 500px;
}

:deep(.arco-divider) {
  margin: 0;
  height: 100%;
}

:deep(.arco-divider-vertical) {
  margin: 0 20px;
}
</style>
