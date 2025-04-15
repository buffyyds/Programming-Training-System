package com.djc.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *

 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

//    /**
//     * 管理员号（用于确认学生在哪个教师/管理员下）
//     */
//    private String adminCode;

    private static final long serialVersionUID = 1L;
}