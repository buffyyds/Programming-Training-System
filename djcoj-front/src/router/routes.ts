import { RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import AdminView from "../views/AdminView.vue";
import NoAuthView from "@/views/NoAuthView.vue";
import ACCESS_ENUM from "@/access/accessEnum";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthView,
  },
  //全局页面组件中绑定一个全局路由监听，每次访问页面时，都会触发这个监听，然后根据用户的权限和路由的meta信息，判断用户是否有权限访问这个页面
  {
    path: "/admin",
    name: "管理员可见",
    component: AdminView,
    meta: {
      //管理员权限
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/about",
    name: "about",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
];
