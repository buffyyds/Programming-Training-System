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
              @search="handleTeacherSearch"
            />
          </div>
          <a-table
            :data="teacherList"
            :loading="teacherLoading"
            :pagination="teacherPagination"
            @page-change="onTeacherPageChange"
          >
            <template #columns>
              <a-table-column title="账号名" data-index="userName" />
              <a-table-column title="手机号" data-index="userPhone" />
              <a-table-column title="创建时间" data-index="createTime">
                <template #cell="{ record }">
                  {{ formatTime(record.createTime) }}
                </template>
              </a-table-column>
              <a-table-column title="操作" align="center">
                <template #cell="{ record }">
                  <a-button
                    type="text"
                    status="danger"
                    @click="handleDeleteTeacher(record)"
                  >
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
              @search="handleStudentSearch"
            />
          </div>
          <a-table
            :data="studentList"
            :loading="studentLoading"
            :pagination="studentPagination"
            @page-change="onStudentPageChange"
          >
            <template #columns>
              <a-table-column title="账号名" data-index="userName" />
              <a-table-column title="手机号" data-index="userPhone" />
              <a-table-column title="所属教师" data-index="teacherName" />
              <a-table-column title="创建时间" data-index="createTime">
                <template #cell="{ record }">
                  {{ formatTime(record.createTime) }}
                </template>
              </a-table-column>
              <a-table-column title="操作" align="center">
                <template #cell="{ record }">
                  <a-button
                    type="text"
                    status="danger"
                    @click="handleDeleteStudent(record)"
                  >
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
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

// 教师列表相关
const teacherList = ref([]);
const teacherLoading = ref(false);
const teacherSearchText = ref("");
const teacherPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 学生列表相关
const studentList = ref([]);
const studentLoading = ref(false);
const studentSearchText = ref("");
const studentPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
};

// 加载教师列表
const loadTeacherList = async () => {
  teacherLoading.value = true;
  try {
    // TODO: 调用后端接口获取教师列表
    // const res = await UserControllerService.listTeacherByPage({
    //   current: teacherPagination.value.current,
    //   pageSize: teacherPagination.value.pageSize,
    //   searchText: teacherSearchText.value,
    // });
    // if (res.code === 0) {
    //   teacherList.value = res.data.records;
    //   teacherPagination.value.total = res.data.total;
    // }
  } catch (error) {
    Message.error("加载教师列表失败");
  } finally {
    teacherLoading.value = false;
  }
};

// 加载学生列表
const loadStudentList = async () => {
  studentLoading.value = true;
  try {
    // TODO: 调用后端接口获取学生列表
    // const res = await UserControllerService.listStudentByPage({
    //   current: studentPagination.value.current,
    //   pageSize: studentPagination.value.pageSize,
    //   searchText: studentSearchText.value,
    // });
    // if (res.code === 0) {
    //   studentList.value = res.data.records;
    //   studentPagination.value.total = res.data.total;
    // }
  } catch (error) {
    Message.error("加载学生列表失败");
  } finally {
    studentLoading.value = false;
  }
};

// 教师搜索
const handleTeacherSearch = () => {
  teacherPagination.value.current = 1;
  loadTeacherList();
};

// 学生搜索
const handleStudentSearch = () => {
  studentPagination.value.current = 1;
  loadStudentList();
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

// 删除教师
const handleDeleteTeacher = (record: any) => {
  Message.warning({
    content: "确定要删除该教师吗？",
    okText: "确定",
    cancelText: "取消",
    onOk: async () => {
      try {
        // TODO: 调用后端接口删除教师
        // const res = await UserControllerService.deleteUser(record.id);
        // if (res.code === 0) {
        //   Message.success("删除成功");
        //   loadTeacherList();
        // }
      } catch (error) {
        Message.error("删除失败");
      }
    },
  });
};

// 删除学生
const handleDeleteStudent = (record: any) => {
  Message.warning({
    content: "确定要删除该学生吗？",
    okText: "确定",
    cancelText: "取消",
    onOk: async () => {
      try {
        // TODO: 调用后端接口删除学生
        // const res = await UserControllerService.deleteUser(record.id);
        // if (res.code === 0) {
        //   Message.success("删除成功");
        //   loadStudentList();
        // }
      } catch (error) {
        Message.error("删除失败");
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

:deep(.arco-table-th) {
  background-color: #f7f8fa;
  font-weight: 500;
}

:deep(.arco-table-td) {
  padding: 16px;
}

:deep(.arco-btn-text) {
  padding: 4px 8px;
}

:deep(.arco-btn-text:hover) {
  background-color: rgba(var(--red-6), 0.1);
}
</style>
