package com.djc.springbootinit.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新请求
 *

 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

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
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 管理员号（用于确认学生在哪个教师/管理员下）
     */
    private String adminCode;

    private static final long serialVersionUID = 1L;
}