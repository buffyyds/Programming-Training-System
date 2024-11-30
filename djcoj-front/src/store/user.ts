import { StoreOptions } from "vuex";

export default {
  namespaced: true,
  // state 存储用户状态信息
  state: {
    loginUser: {
      //id?
      name: "未登录",
    },
  },
  //actions 执行异步操作，并且触发mutations更新state
  actions: {
    getLoginUser({ commit, state }, user) {
      //user需要在登录的时候从后端获取
      commit("updateUser", { name: "djc" });
    },
  },
  //mutations 定义对变量更新的方法（尽量同步）
  mutations: {
    updateUser(state, user) {
      state.loginUser = user;
    },
  },
} as StoreOptions<any>;
