package com.djc.springbootinit.model.dto.RemindComplete;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *

 */
@Data
public class RemindCompleteAddRequest implements Serializable {
    /**
     * 教师id
     */
    private Long teacherId;
    /**
     * 学生id
     */
    private Long studentId;
    /**
     * 内容
     */
    private String content;
    /**
     * 题目id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}