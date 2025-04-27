package com.djc.springbootinit.model.vo;


import lombok.Data;

import java.util.Date;

/**
 * 教师视图
 */
@Data
public class TeacherVo {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 旗下学生人数
     */
    private Integer studentCount;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

}
