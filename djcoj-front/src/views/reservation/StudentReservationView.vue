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

      <!-- 预约列表 -->
      <a-table
        :data="reservationList"
        :loading="loading"
        :pagination="false"
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
              <a-tag
                :color="
                  record.isReservation
                    ? record.studentUser?.id === currentUserId
                      ? 'green'
                      : 'red'
                    : 'blue'
                "
              >
                {{
                  record.isReservation
                    ? record.studentUser?.id === currentUserId
                      ? "您预约了该时间段"
                      : "已被预约"
                    : "可预约"
                }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  v-if="!record.isReservation"
                  type="primary"
                  @click="doReservation(record)"
                >
                  预约
                </a-button>
                <a-button
                  v-else-if="record.studentUser?.id === currentUserId"
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

const router = useRouter();
const store = useStore();
const loading = ref(false);
const reservationList = ref<any[]>([]);

// 获取当前用户ID
const currentUserId = computed(() => store.state.user.loginUser?.id);

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
