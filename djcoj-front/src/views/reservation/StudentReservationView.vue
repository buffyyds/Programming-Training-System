<template>
  <div class="reservation-container">
    <a-card class="reservation-card">
      <template #title>
        <div class="card-title">
          <a-space>
            <icon-calendar class="title-icon" />
            答疑预约
          </a-space>
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
          <a-radio value="available">可预约</a-radio>
          <a-radio value="reserved">已预约</a-radio>
          <a-radio value="expired">已过期</a-radio>
        </a-radio-group>
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
          <a-table-column title="教师" data-index="teacherUser">
            <template #cell="{ record }">
              <div class="teacher-info">
                <a-avatar :size="32">
                  {{ record.teacherUser?.userName?.[0] }}
                </a-avatar>
                <span class="teacher-name">{{
                  record.teacherUser?.userName
                }}</span>
              </div>
            </template>
          </a-table-column>
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
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  v-if="!record.isReservation && !isExpired(record)"
                  type="primary"
                  @click="doReservation(record)"
                >
                  预约
                </a-button>
                <a-button
                  v-else-if="
                    record.studentUser?.id === currentUserId &&
                    !isExpired(record)
                  "
                  type="primary"
                  status="danger"
                  @click="cancelReservation(record)"
                >
                  取消预约
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { Message } from "@arco-design/web-vue";
import {
  IconArrowLeft,
  IconCalendar,
  IconClockCircle,
} from "@arco-design/web-vue/es/icon";
import { ReservationControllerService } from "../../../generated";
import dayjs from "dayjs";

const router = useRouter();
const store = useStore();
const loading = ref(false);
const reservationList = ref<any[]>([]);
const currentStatus = ref("all");
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 获取当前用户ID
const currentUserId = computed(() => store.state.user.loginUser?.id);

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
  if (record.isReservation) {
    return record.studentUser?.id === currentUserId.value ? "green" : "red";
  }
  return "blue";
};

// 获取状态文本
const getStatusText = (record: any) => {
  if (isExpired(record)) {
    return "已过期";
  }
  if (record.isReservation) {
    return record.studentUser?.id === currentUserId.value
      ? "您预约了该时间段"
      : "已被预约";
  }
  return "可预约";
};

// 过滤后的预约列表
const filteredReservations = computed(() => {
  return reservationList.value.filter((record) => {
    if (currentStatus.value === "all") return true;
    if (currentStatus.value === "expired") return isExpired(record);
    if (currentStatus.value === "reserved")
      return (
        record.isReservation &&
        record.studentUser?.id === currentUserId.value &&
        !isExpired(record)
      );
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

// 预约时间段
const doReservation = async (record: any) => {
  try {
    const res = await ReservationControllerService.doReservationUsingPost({
      id: record.id,
      studentId: currentUserId.value,
    });
    if (res.code === 0) {
      Message.success("预约成功");
      loadReservations();
    } else {
      Message.error("预约失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("预约失败：" + error.message);
  }
};

// 取消预约
const cancelReservation = async (record: any) => {
  try {
    const res = await ReservationControllerService.unDoReservationUsingPost({
      id: record.id,
    });
    if (res.code === 0) {
      Message.success("取消预约成功");
      loadReservations();
    } else {
      Message.error("取消预约失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("取消预约失败：" + error.message);
  }
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

.filter-section {
  margin-bottom: 16px;
}

.reservation-table {
  margin-top: 16px;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.teacher-name {
  color: var(--color-text-1);
  font-weight: 500;
}

.time-slot {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-1);
}
</style>
