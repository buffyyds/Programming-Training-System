import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";

export default {
  namespaced: true,
  // state 存储用户状态信息
  state: {
    loginUser: {
      //id?
      userName: "未登录",
      userRole: ACCESS_ENUM.NOT_LOGIN,
    },
  },
  //actions 执行异步操作，并且触发mutations更新state
  actions: {
    getLoginUser({ commit, state }, user) {
      //user需要在登录的时候从后端获取
      commit("updateUser", user);
    },
  },
  //mutations 定义对变量更新的方法（尽量同步）
  mutations: {
    updateUser(state, user) {
      state.loginUser = user;
    },
  },
} as StoreOptions<any>;
