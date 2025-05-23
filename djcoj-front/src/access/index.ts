import router from "@/router";
import store from "@/store";
import ACCESS_ENUM from "./accessEnum";
import checkAccess from "./checkAccess";

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  console.log("登陆用户信息", (store.state as any).user.loginUser);
  let loginUser = (store.state as any).user.loginUser;
  // 如果之前没登陆过，自动登录
  if (!loginUser || !loginUser.userRole) {
    // 加 await 是为了等用户登录成功之后，再执行后续的代码
    await store.dispatch("user/getLoginUser");
    loginUser = (store.state as any).user.loginUser;
  }
  // 拿到路由需要的权限，如果没有设置，默认为不需要登录
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  const exclusive = (to.meta?.exclusive as boolean) ?? false;
  const maxAccess = (to.meta?.access as boolean) ?? false;
  // 要跳转的页面必须要登录
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 如果没登录，跳转到登录页面
    if (!loginUser || !loginUser.userRole) {
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
    // 如果已经登录了，但是权限不足，那么跳转到无权限页面
    // if (!checkAccess(loginUser, needAccess)) {
    //   next("/noAuth");
    //   return;
    // }
  }
  next();
});
