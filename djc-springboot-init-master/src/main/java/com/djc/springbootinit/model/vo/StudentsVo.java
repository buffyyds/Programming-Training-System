package com.djc.springbootinit.model.vo;


import com.djc.springbootinit.model.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 学生视图
 */
@Data
public class StudentsVo {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 对应教师User
     */
    private UserVO teacher;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

}
