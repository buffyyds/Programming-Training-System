<template>
  <div class="user-management">
    <a-card class="general-card" title="用户管理">
      <a-tabs default-active-key="teacher">
        <a-tab-pane key="teacher" title="教师管理">
          <div class="search-bar">
            <a-input-search
              v-model="teacherSearchText"
              placeholder="请输入教师账号名或手机号"
              style="width: 300px"
              @input="handleTeacherSearch"
            >
              <template #prefix>
                <icon-search />
              </template>
            </a-input-search>
          </div>
          <a-table
            :data="filteredTeacherList"
            :loading="teacherLoading"
            :pagination="teacherPagination"
            @page-change="onTeacherPageChange"
            :bordered="false"
            :stripe="true"
            size="small"
          >
            <template #columns>
              <a-table-column
                title="账号名"
                data-index="userName"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-user />
                    <span>{{ record.userName }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column
                title="手机号"
                data-index="userPhone"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-phone />
                    <span>{{ record.userPhone || "未填写" }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column
                title="学生人数"
                data-index="studentCount"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-user-group />
                    <span>{{ record.studentCount }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column
                title="创建时间"
                data-index="createTime"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-clock-circle />
                    <span>{{ formatTime(record.createTime) }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column title="操作" align="center">
                <template #cell="{ record }">
                  <a-button
                    type="text"
                    status="danger"
                    @click="handleDisableTeacher(record)"
                  >
                    <template #icon>
                      <icon-delete />
                    </template>
                    删除
                  </a-button>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="student" title="学生管理">
          <div class="search-bar">
            <a-input-search
              v-model="studentSearchText"
              placeholder="请输入学生账号名或手机号"
              style="width: 300px"
              @input="handleStudentSearch"
            >
              <template #prefix>
                <icon-search />
              </template>
            </a-input-search>
          </div>
          <a-table
            :data="filteredStudentList"
            :loading="studentLoading"
            :pagination="studentPagination"
            @page-change="onStudentPageChange"
            :bordered="false"
            :stripe="true"
            size="small"
          >
            <template #columns>
              <a-table-column
                title="账号名"
                data-index="userName"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-user />
                    <span>{{ record.userName }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column
                title="所属教师"
                data-index="teacher"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-user-group />
                    <span>{{ record.teacher?.userName || "未绑定" }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column
                title="手机号"
                data-index="userPhone"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-phone />
                    <span>{{ record.userPhone || "未填写" }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column
                title="创建时间"
                data-index="createTime"
                align="center"
              >
                <template #cell="{ record }">
                  <div class="user-info">
                    <icon-clock-circle />
                    <span>{{ formatTime(record.createTime) }}</span>
                  </div>
                </template>
              </a-table-column>
              <a-table-column title="操作" align="center">
                <template #cell="{ record }">
                  <a-button
                    type="text"
                    status="danger"
                    @click="handleDisableStudent(record)"
                  >
                    <template #icon>
                      <icon-delete />
                    </template>
                    删除
                  </a-button>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { Message, Modal } from "@arco-design/web-vue";
import {
  IconSearch,
  IconUser,
  IconPhone,
  IconUserGroup,
  IconClockCircle,
  IconDelete,
} from "@arco-design/web-vue/es/icon";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";
import {
  TasControllerService,
  UserControllerService,
} from "../../../generated";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

// 教师列表相关
const teacherList = ref<any[]>([]);
const teacherLoading = ref(false);
const teacherSearchText = ref("");
const teacherPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 学生列表相关
const studentList = ref<any[]>([]);
const studentLoading = ref(false);
const studentSearchText = ref("");
const studentPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 添加计算属性用于前端搜索过滤
const filteredTeacherList = computed(() => {
  if (!teacherSearchText.value) {
    return teacherList.value;
  }
  const searchText = teacherSearchText.value.toLowerCase();
  return teacherList.value.filter(
    (teacher) =>
      teacher.userName.toLowerCase().includes(searchText) ||
      (teacher.userPhone && teacher.userPhone.includes(searchText))
  );
});

const filteredStudentList = computed(() => {
  if (!studentSearchText.value) {
    return studentList.value;
  }
  const searchText = studentSearchText.value.toLowerCase();
  return studentList.value.filter(
    (student) =>
      student.userName.toLowerCase().includes(searchText) ||
      (student.userPhone && student.userPhone.includes(searchText))
  );
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
};

// 加载教师列表
const loadTeacherList = async () => {
  teacherLoading.value = true;
  try {
    const res = await TasControllerService.getTeacherListUsingGet();
    if (res.code === 0 && res.data) {
      teacherList.value = res.data;
      teacherPagination.value.total = res.data.length;
    } else {
      Message.error("加载教师列表失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("加载教师列表失败：" + error.message);
  } finally {
    teacherLoading.value = false;
  }
};

// 加载学生列表
const loadStudentList = async () => {
  studentLoading.value = true;
  try {
    const res = await TasControllerService.getStudentListUsingGet();
    if (res.code === 0 && res.data) {
      studentList.value = res.data;
      studentPagination.value.total = res.data.length;
    } else {
      Message.error("加载学生列表失败：" + res.message);
    }
  } catch (error: any) {
    Message.error("加载学生列表失败：" + error.message);
  } finally {
    studentLoading.value = false;
  }
};

// 教师搜索
const handleTeacherSearch = () => {
  teacherPagination.value.current = 1;
};

// 学生搜索
const handleStudentSearch = () => {
  studentPagination.value.current = 1;
};

// 教师分页变化
const onTeacherPageChange = (current: number) => {
  teacherPagination.value.current = current;
  loadTeacherList();
};

// 学生分页变化
const onStudentPageChange = (current: number) => {
  studentPagination.value.current = current;
  loadStudentList();
};

// 禁用教师
const handleDisableTeacher = (record: any) => {
  Modal.warning({
    title: "确认删除",
    content: `确定要删除教师 ${record.userName} 吗？`,
    okText: "确认",
    cancelText: "取消",
    onOk: async () => {
      try {
        const res = await UserControllerService.deleteUserUsingPost({
          id: record.id,
        });
        if (res.code === 0) {
          Message.success("删除成功");
          loadTeacherList();
        } else {
          Message.error("删除失败：" + res.message);
        }
      } catch (error: any) {
        Message.error("删除失败：" + error.message);
      }
    },
  });
};

// 禁用学生
const handleDisableStudent = (record: any) => {
  Modal.warning({
    title: "确认删除",
    content: `确定要删除学生 ${record.userName} 吗？`,
    okText: "确认",
    cancelText: "取消",
    onOk: async () => {
      try {
        const res = await UserControllerService.deleteUserUsingPost({
          id: record.id,
        });
        if (res.code === 0) {
          Message.success("删除成功");
          loadStudentList();
        } else {
          Message.error("删除失败：" + res.message);
        }
      } catch (error: any) {
        Message.error("删除失败：" + error.message);
      }
    },
  });
};

onMounted(() => {
  loadTeacherList();
  loadStudentList();
});
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.general-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.search-bar {
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.user-info :deep(.arco-icon) {
  color: rgb(var(--primary-6));
  font-size: 16px;
}

:deep(.arco-table-th) {
  background-color: #f7f8fa;
  font-weight: 500;
  text-align: center;
}

:deep(.arco-table-td) {
  padding: 12px;
  text-align: center;
}

:deep(.arco-btn-text) {
  padding: 4px 8px;
}

:deep(.arco-btn-text:hover) {
  background-color: rgba(var(--red-6), 0.1);
}

:deep(.arco-table-tr) {
  height: 48px;
}

:deep(.arco-table-pagination) {
  margin-top: 16px;
}
</style>
