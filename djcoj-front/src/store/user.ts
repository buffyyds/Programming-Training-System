import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";

interface UserState {
  loginUser: {
    id?: number;
    userName: string;
    userRole?: string;
  } | null;
  aiMessages: {
    [key: number]: Array<{
      type: "user" | "ai";
      content: string;
    }>;
  };
}

interface RootState {
  user: UserState;
}

export default {
  namespaced: true,
  // state 存储用户状态信息
  state: {
    loginUser: null,
    aiMessages: {}, // 存储每个用户的AI对话记录
  } as UserState,
  //actions 执行异步操作，并且触发mutations更新state
  actions: {
    async getLoginUser({ commit, state }, payload) {
      const res = await UserControllerService.getLoginUserUsingGet();
      if (res.code === 0 && res.data) {
        commit("updateUser", res.data);
        // 如果用户没有对话记录，初始化一个空数组
        if (res.data.id) {
          if (!state.aiMessages[res.data.id]) {
            commit("initAiMessages", res.data.id);
            // 添加欢迎消息
            commit("addAiMessage", {
              userId: res.data.id,
              message: {
                type: "ai",
                content: "你好！我是AI助手，有什么可以帮你的吗？",
              },
            });
          }
        }
      } else {
        commit("updateUser", {
          ...state.loginUser,
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
    },
    // 添加新的AI消息
    addAiMessage({ commit, state }, { userId, message }) {
      commit("addAiMessage", { userId, message });
    },
    // 清除用户的AI对话记录
    clearAiMessages({ commit }, userId) {
      commit("clearAiMessages", userId);
    },
    // 用户退出登录
    async logout({ commit, state }) {
      try {
        await UserControllerService.userLogoutUsingPost();
        // 清空当前用户的对话记录
        if (state.loginUser?.id) {
          commit("clearAiMessages", state.loginUser.id);
        }
        // 重置用户状态
        commit("updateUser", {
          userName: "未登录",
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      } catch (error) {
        console.error("退出登录失败:", error);
      }
    },
  },
  //mutations 定义对变量更新的方法（尽量同步）
  mutations: {
    updateUser(state, user) {
      state.loginUser = user;
    },
    // 初始化用户的AI对话记录
    initAiMessages(state, userId) {
      state.aiMessages[userId] = [];
    },
    // 添加新的AI消息
    addAiMessage(state, { userId, message }) {
      if (!state.aiMessages[userId]) {
        state.aiMessages[userId] = [];
      }
      state.aiMessages[userId].push(message);
    },
    // 清除用户的AI对话记录
    clearAiMessages(state, userId) {
      state.aiMessages[userId] = [];
    },
  },
} as StoreOptions<UserState>;
