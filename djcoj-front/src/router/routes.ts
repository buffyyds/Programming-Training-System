import { RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import AdminView from "../views/AdminView.vue";
import NoAuthView from "../views/NoAuthView.vue";
import ACCESS_ENUM from "@/access/accessEnum";
import UserLayout from "../layouts/UserLayout.vue";
import UserLoginView from "../views/user/UserLoginView.vue";
import UserRegisterView from "../views/user/UserRegisterView.vue";
import AddQuestionView from "@/views/question/AddQuestionView.vue";
import ManageQuestionView from "@/views/question/ManageQuestionView.vue";
import QuestionsView from "@/views/question/QuestionsView.vue";
import QuestionSubmitView from "@/views/question/QuestionSubmitView.vue";
import ViewQuestionView from "@/views/question/ViewQuestionView.vue";
import StudentProgressView from "@/views/question/StudentProgressView.vue";
import StudentProgressDetailView from "@/views/question/StudentProgressDetailView.vue";
import TeacherReservationView from "@/views/reservation/TeacherReservationView.vue";
import StudentReservationView from "@/views/reservation/StudentReservationView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      {
        path: "/user/login",
        name: "用户登录",
        component: UserLoginView,
      },
      {
        path: "/user/register",
        name: "用户注册",
        component: UserRegisterView,
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/",
    name: "主页",
    component: QuestionsView,
  },
  // {
  //   path: "/questions",
  //   name: "浏览题目",
  //   component: QuestionsView,
  // },
  {
    path: "/question_submit",
    name: "浏览题目提交",
    component: QuestionSubmitView,
  },
  {
    path: "/view/question/:id",
    name: "在线做题",
    component: ViewQuestionView,
    props: true,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/add/question",
    name: "创建题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.TEACHER,
    },
  },
  {
    path: "/manage/question/",
    name: "管理题目",
    component: ManageQuestionView,
    meta: {
      access: ACCESS_ENUM.TEACHER,
    },
  },
  {
    path: "/update/question",
    name: "修改题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.TEACHER,
      hideInMenu: true,
    },
  },
  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/user/profile",
    name: "个人信息",
    component: () => import("../views/user/UserProfileView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/user/messages",
    name: "我的消息",
    component: () => import("../views/user/UserMessagesView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/user/bind-teacher",
    name: "绑定教师",
    component: () => import("../views/user/BindTeacherView.vue"),
    meta: {
      access: ACCESS_ENUM.USER,
      exclusive: true,
    },
  },
  {
    path: "/student_progress",
    name: "学生完成情况",
    component: StudentProgressView,
    meta: {
      access: ACCESS_ENUM.TEACHER,
    },
  },
  {
    path: "/student_progress/:id",
    name: "学生完成详情",
    component: StudentProgressDetailView,
    props: true,
    meta: {
      access: ACCESS_ENUM.TEACHER,
      hideInMenu: true,
    },
  },
  {
    path: "/reservation/teacher",
    name: "答疑预约管理",
    component: TeacherReservationView,
    meta: {
      access: ACCESS_ENUM.TEACHER,
      exclusive: true,
    },
  },
  {
    path: "/reservation/student",
    name: "学生答疑预约",
    component: StudentReservationView,
    meta: {
      access: ACCESS_ENUM.USER,
      exclusive: true,
    },
  },
  {
    path: "/teacher/students",
    name: "学生列表",
    component: () => import("../views/teacher/StudentListView.vue"),
    meta: {
      access: ACCESS_ENUM.TEACHER,
      exclusive: true,
    },
  },
];
