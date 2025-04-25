<template>
  <div class="reservation-container">
    <a-card class="reservation-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <icon-calendar class="title-icon" />
            答疑预约管理
          </a-space>
          <a-button
            type="primary"
            @click="goToStatistics"
            style="margin-left: auto"
          >
            <template #icon>
              <icon-bar-chart />
            </template>
            查看学生预约情况图表
          </a-button>
        </div>
      </template>

      <!-- 状态过滤 -->
      <div class="filter-section">
        <a-radio-group
          v-model="currentStatus"
          type="button"
          @change="handleStatusChange"
        >
          <a-radio value="all">全部</a-radio>
          <a-radio value="available">未预约</a-radio>
          <a-radio value="reserved">已预约</a-radio>
          <a-radio value="expired">已过期</a-radio>
        </a-radio-group>
      </div>

      <!-- 时间选择器 -->
      <div class="time-picker-section">
        <a-space>
          <a-date-picker
            v-model="selectedDate"
            :disabled-date="disabledDate"
            @change="handleDateChange"
            style="width: 200px"
          />
          <a-time-picker
            v-model="startTime"
            format="HH:mm"
            placeholder="开始时间"
            style="width: 120px"
          />
          <span class="time-separator">至</span>
          <a-time-picker
            v-model="endTime"
            format="HH:mm"
            placeholder="结束时间"
            style="width: 120px"
          />
          <a-button type="primary" @click="addTimeSlot">
            <template #icon>
              <icon-plus />
            </template>
            添加时间段
          </a-button>
        </a-space>
      </div>

      <!-- 预约列表 -->
      <a-table
        :data="filteredAndPaginatedReservations"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: filteredReservations.length,
          showTotal: true,
          showJumper: true,
          showPageSize: true,
          pageSizeOptions: [10, 20, 30, 50],
          size: 'small',
        }"
        @page-change="onPageChange"
        @page-size-change="onPageSizeChange"
        class="reservation-table"
      >
        <template #columns>
          <a-table-column title="时间段" data-index="timeSlot">
            <template #cell="{ record }">
              <div class="time-slot">
                <icon-clock-circle />
                {{ record.time_slot }}
              </div>
            </template>
          </a-table-column>
          <a-table-column title="预约状态" data-index="isReservation">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record)">
                {{ getStatusText(record) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="预约学生" data-index="studentUser">
            <template #cell="{ record }">
              <span v-if="record.studentUser">
                {{ record.studentUser.userName }}
              </span>
              <span v-else>-</span>
            </template>
          </a-table-column>
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  v-if="!isExpired(record)"
                  type="outline"
                  status="warning"
                  @click="showUpdateModal(record)"
                >
                  修改
                </a-button>
                <a-button
                  v-if="!isExpired(record)"
                  type="text"
                  status="danger"
                  @click="deleteTimeSlot(record.id)"
                >
                  删除
                </a-button>
                <a-button
                  v-if="record.isReservation"
                  type="primary"
                  @click="viewReservationDetail(record)"
                >
                  查看预约信息
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 预约详情弹窗 -->
    <a-modal
      v-model:visible="showDetailModal"
      title="预约详情"
      @cancel="handleModalClose"
      :footer="false"
    >
      <div class="detail-container" v-if="currentReservation">
        <div class="detail-item">
          <span class="label">时间段：</span>
          <span class="value">{{ currentReservation.time_slot }}</span>
        </div>
        <div class="detail-item">
          <span class="label">预约学生：</span>
          <span class="value">{{
            currentReservation.studentUser?.userName
          }}</span>
        </div>
        <div class="detail-item">
          <span class="label">学生电话：</span>
          <span class="value">{{
            currentReservation.studentUser?.userPhone || "未填写"
          }}</span>
        </div>
      </div>
    </a-modal>

    <!-- 修改时间弹窗 -->
    <a-modal
      v-model:visible="showUpdateTimeModal"
      title="修改时间段"
      @ok="updateTimeSlot"
      @cancel="handleUpdateModalClose"
      :width="600"
    >
      <div class="update-time-container">
        <div class="time-picker-title">请选择新的时间段</div>
        <div class="time-picker-section">
          <div class="date-picker-wrapper">
            <div class="picker-label">日期</div>
            <a-date-picker
              v-model="selectedDate"
              :disabled-date="disabledDate"
              @change="handleDateChange"
              style="width: 200px"
              class="custom-date-picker"
            />
          </div>
          <div class="time-picker-wrapper">
            <div class="picker-label">时间</div>
            <div class="time-range-picker">
              <a-time-picker
                v-model="startTime"
                format="HH:mm"
                placeholder="开始时间"
                style="width: 120px"
                class="custom-time-picker"
              />
              <span class="time-separator">至</span>
              <a-time-picker
                v-model="endTime"
                format="HH:mm"
                placeholder="结束时间"
                style="width: 120px"
                class="custom-time-picker"
              />
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconCalendar,
  IconPlus,
  IconClockCircle,
  IconBarChart,
} from "@arco-design/web-vue/es/icon";
import { ReservationControllerService } from "../../../generated";
import dayjs from "dayjs";

const router = useRouter();
const loading = ref(false);
const reservationList = ref<any[]>([]);
const showDetailModal = ref(false);
const currentReservation = ref<any>(null);
const currentStatus = ref("all");

// 分页相关
const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10,
});

// 判断是否过期
const isExpired = (record: any) => {
  const timeSlot = dayjs(record.time_slot);
  return timeSlot.isBefore(dayjs());
};

// 获取状态颜色
const getStatusColor = (record: any) => {
  if (isExpired(record)) {
    return "gray";
  }
  return record.isReservation ? "green" : "red";
};

// 获取状态文本
const getStatusText = (record: any) => {
  if (isExpired(record)) {
    return "已过期";
  }
  return record.isReservation ? "已预约" : "未预约";
};

// 过滤后的预约列表
const filteredReservations = computed(() => {
  return reservationList.value.filter((record) => {
    if (currentStatus.value === "all") return true;
    if (currentStatus.value === "expired") return isExpired(record);
    if (currentStatus.value === "reserved")
      return record.isReservation && !isExpired(record);
    if (currentStatus.value === "available")
      return !record.isReservation && !isExpired(record);
    return true;
  });
});

// 分页后的数据
const filteredAndPaginatedReservations = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return filteredReservations.value.slice(start, end);
});

// 状态变化处理
const handleStatusChange = () => {
  pagination.value.current = 1; // 切换状态时重置到第一页
};

// 分页变化处理
const onPageChange = (current: number) => {
  pagination.value.current = current;
};

// 每页条数变化处理
const onPageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize;
  pagination.value.current = 1;
};

// 时间选择相关
const selectedDate = ref<Date>();
const startTime = ref<string>();
const endTime = ref<string>();

// 禁用过去的日期
const disabledDate = (current?: Date) => {
  if (!current) return false;
  return current < dayjs().startOf("day").toDate();
};

// 处理日期变化
const handleDateChange = () => {
  startTime.value = undefined;
  endTime.value = undefined;
};

// 添加时间段
const addTimeSlot = async () => {
  if (!selectedDate.value || !startTime.value || !endTime.value) {
    Message.warning("请选择完整的时间段");
    return;
  }

  // 时间校验
  const now = dayjs();
  const selectedDateTime = dayjs(selectedDate.value);
  const startDateTime = dayjs(
    `${selectedDateTime.format("YYYY-MM-DD")} ${startTime.value}`
  );
  const endDateTime = dayjs(
    `${selectedDateTime.format("YYYY-MM-DD")} ${endTime.value}`
  );

  // 校验规则：
  // 1. 预约日期不能小于今天
  // 2. 如果选择今天，开始时间不能小于当前时间
  // 3. 结束时间必须大于开始时间
  if (selectedDateTime.isBefore(now, "day")) {
    Message.warning("预约日期不能小于今天");
    return;
  }

  if (selectedDateTime.isSame(now, "day") && startDateTime.isBefore(now)) {
    Message.warning("开始时间不能小于当前时间");
    return;
  }

  if (endDateTime.isBefore(startDateTime)) {
    Message.warning("结束时间必须大于开始时间");
    return;
  }

  const date = dayjs(selectedDate.value).format("YYYY-MM-DD");
  const timeSlot = `${date} ${startTime.value}-${endTime.value}`;

  try {
    const res = await ReservationControllerService.addReservationUsingGet(
      timeSlot
    );
    if (res.code === 0) {
      Message.success("添加成功");
      loadReservations();
    } else {
      Message.error("添加失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("添加失败：" + error.message);
  }
};

// 删除时间段
const deleteTimeSlot = async (id: number) => {
  try {
    const res = await ReservationControllerService.deleteReservationUsingGet(
      id
    );
    if (res.code === 0) {
      Message.success("删除成功");
      loadReservations();
    } else {
      Message.error("删除失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("删除失败：" + error.message);
  }
};

// 查看预约详情
const viewReservationDetail = (record: any) => {
  currentReservation.value = record;
  showDetailModal.value = true;
};

// 关闭弹窗
const handleModalClose = () => {
  currentReservation.value = null;
  showDetailModal.value = false;
};

// 加载预约列表
const loadReservations = async () => {
  loading.value = true;
  try {
    const res = await ReservationControllerService.getReservationUsingGet();
    if (res.code === 0) {
      reservationList.value = res.data || [];
      pagination.value.total = res.data?.length || 0;
    } else {
      Message.error("获取预约列表失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("获取预约列表失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

// 跳转到统计页面
const goToStatistics = () => {
  router.push("/reservation/statistics");
};

// 修改时间相关
const showUpdateModal = (record: any) => {
  currentReservation.value = record;
  // 解析当前时间段
  const [date, timeRange] = record.time_slot.split(" ");
  const [start, end] = timeRange.split("-");
  selectedDate.value = new Date(date);
  startTime.value = start;
  endTime.value = end;
  showUpdateTimeModal.value = true;
};

const showUpdateTimeModal = ref(false);

// 关闭修改时间弹窗
const handleUpdateModalClose = () => {
  showUpdateTimeModal.value = false;
  // 清空时间选择
  selectedDate.value = undefined;
  startTime.value = undefined;
  endTime.value = undefined;
};

const updateTimeSlot = async () => {
  if (!selectedDate.value || !startTime.value || !endTime.value) {
    Message.warning("请选择完整的时间段");
    return;
  }

  // 时间校验
  const now = dayjs();
  const selectedDateTime = dayjs(selectedDate.value);
  const startDateTime = dayjs(
    `${selectedDateTime.format("YYYY-MM-DD")} ${startTime.value}`
  );
  const endDateTime = dayjs(
    `${selectedDateTime.format("YYYY-MM-DD")} ${endTime.value}`
  );

  // 校验规则：
  // 1. 预约日期不能小于今天
  // 2. 如果选择今天，开始时间不能小于当前时间
  // 3. 结束时间必须大于开始时间
  if (selectedDateTime.isBefore(now, "day")) {
    Message.warning("预约日期不能小于今天");
    return;
  }

  if (selectedDateTime.isSame(now, "day") && startDateTime.isBefore(now)) {
    Message.warning("开始时间不能小于当前时间");
    return;
  }

  if (endDateTime.isBefore(startDateTime)) {
    Message.warning("结束时间必须大于开始时间");
    return;
  }

  const date = dayjs(selectedDate.value).format("YYYY-MM-DD");
  const timeSlot = `${date} ${startTime.value}-${endTime.value}`;

  try {
    const res = await ReservationControllerService.updateReservationUsingPost({
      id: currentReservation.value.id,
      time_slot: timeSlot,
    });
    if (res.code === 0) {
      Message.success("修改成功");
      loadReservations();
      showUpdateTimeModal.value = false;
      // 清空时间选择
      selectedDate.value = undefined;
      startTime.value = undefined;
      endTime.value = undefined;
    } else {
      Message.error("修改失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("修改失败：" + error.message);
  }
};

onMounted(() => {
  loadReservations();
});
</script>

<style scoped>
.reservation-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.reservation-card {
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

.time-picker-section {
  margin-bottom: 0;
  padding: 16px;
  background-color: var(--color-fill-2);
  border-radius: 4px;
}

.time-separator {
  color: var(--color-text-3);
  margin: 0 8px;
}

.reservation-table {
  margin-top: 16px;
}

.time-slot {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-1);
}

.detail-container {
  padding: 16px;
}

.detail-item {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
}

.detail-item .label {
  width: 80px;
  color: var(--color-text-3);
}

.detail-item .value {
  flex: 1;
  color: var(--color-text-1);
}

.update-time-container {
  padding: 20px;
}

.time-picker-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-1);
  margin-bottom: 20px;
}

.time-picker-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 0;
  padding: 20px;
  background-color: var(--color-fill-2);
  border-radius: 8px;
}

.date-picker-wrapper,
.time-picker-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.picker-label {
  font-size: 14px;
  color: var(--color-text-2);
}

.time-range-picker {
  display: flex;
  align-items: center;
  gap: 12px;
}

.custom-date-picker,
.custom-time-picker {
  border-radius: 4px;
  transition: all 0.2s;
}

.custom-date-picker:hover,
.custom-time-picker:hover {
  border-color: var(--color-primary-light-4);
}

.time-separator {
  color: var(--color-text-3);
  font-size: 14px;
}

.filter-section {
  margin-bottom: 16px;
}
</style>
