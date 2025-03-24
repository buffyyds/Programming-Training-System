package com.djc.springbootinit.model.vo;


import lombok.Data;

/**
 * 学生视图
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

    private static final long serialVersionUID = 1L;

}
