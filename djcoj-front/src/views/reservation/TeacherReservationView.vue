<template>
  <div class="reservation-container">
    <a-card class="reservation-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <a-button type="text" @click="goBack">
              <template #icon>
                <icon-arrow-left />
              </template>
            </a-button>
            <icon-calendar class="title-icon" />
            答疑预约管理
          </a-space>
        </div>
      </template>

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
        :data="reservationList"
        :loading="loading"
        :pagination="false"
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
              <a-tag :color="record.isReservation ? 'green' : 'red'">
                {{ record.isReservation ? "已预约" : "未预约" }}
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
                  v-if="record.isReservation"
                  type="primary"
                  @click="viewReservationDetail(record)"
                >
                  查看预约信息
                </a-button>
                <a-button
                  v-else
                  type="text"
                  status="danger"
                  @click="deleteTimeSlot(record.id)"
                >
                  删除
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
      :footer="null"
    >
      <div class="detail-container" v-if="currentReservation">
        <div class="detail-item">
          <span class="label">时间段：</span>
          <span class="value">{{ currentReservation.timeSlot }}</span>
        </div>
        <div class="detail-item">
          <span class="label">预约学生：</span>
          <span class="value">{{
            currentReservation.studentUser?.userName
          }}</span>
        </div>
        <div class="detail-item">
          <span class="label">学生简介：</span>
          <span class="value">{{
            currentReservation.studentUser?.userProfile || "无"
          }}</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconCalendar,
  IconPlus,
  IconClockCircle,
} from "@arco-design/web-vue/es/icon";
import { ReservationControllerService } from "../../../generated";
import dayjs from "dayjs";

const router = useRouter();
const loading = ref(false);
const reservationList = ref<any[]>([]);
const showDetailModal = ref(false);
const currentReservation = ref<any>(null);

// 时间选择相关
const selectedDate = ref<Date>();
const startTime = ref<string>();
const endTime = ref<string>();

// 禁用过去的日期
const disabledDate = (current: Date) => {
  return current && current < dayjs().startOf("day").toDate();
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
    } else {
      Message.error("获取预约列表失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("获取预约列表失败：" + error.message);
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
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
  margin-bottom: 24px;
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
</style>
