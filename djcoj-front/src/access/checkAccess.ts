//菜单组件中需要判断权限，权限拦截也需要用到权限判断，所以抽离出来成公共方法
import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 检查权限
 * @param loginUser 当前登录用户
 * @param needAccess 需要的权限
 * @param exclusive 是否需要精确匹配
 * @return boolean 有无权限
 */
const checkAccess = (
  loginUser: any,
  needAccess = ACCESS_ENUM.NOT_LOGIN,
  exclusive = false
) => {
  //获取当前登录用户具有的权限 (没有loginUser则默认为未登录)
  const loginUserAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN;
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true;
  }
  //如果需要用户登录才能访问
  if (needAccess === ACCESS_ENUM.USER) {
    //没登录，无权限
    if (loginUserAccess === ACCESS_ENUM.NOT_LOGIN) {
      return false;
    }
    //如果需要精确匹配，则必须是USER角色
    if (exclusive) {
      return loginUserAccess === ACCESS_ENUM.USER;
    }
  }
  //如果需要老师权限才能访问
  if (needAccess === ACCESS_ENUM.TEACHER) {
    //没登录或者不是老师，无权限
    if (
      loginUserAccess === ACCESS_ENUM.NOT_LOGIN ||
      loginUserAccess === ACCESS_ENUM.USER
    ) {
      return false;
    }
    //如果需要精确匹配，则必须是TEACHER角色
    if (exclusive) {
      return loginUserAccess === ACCESS_ENUM.TEACHER;
    }
  }
  //如果需要管理员权限才能访问
  if (needAccess === ACCESS_ENUM.ADMIN) {
    //没登录或者不是管理员，无权限
    if (loginUserAccess !== ACCESS_ENUM.ADMIN) {
      return false;
    }
    //如果需要精确匹配，则必须是ADMIN角色
    if (exclusive) {
      return loginUserAccess === ACCESS_ENUM.ADMIN;
    }
  }
  return true;
};

export default checkAccess;
