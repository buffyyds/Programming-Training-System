package com.djc.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子视图
 *

 */
@Data
public class WrongquestionPerformanceVO implements Serializable {
    /**
     * 题目名称
     */
    private String questionName;

    /**
     * 该题目被标记错题的学生人数
     */
    private Integer wrongQuestionStudentCount;

    /**
     * 该题目被标记错题的学生人数占比
     */
    private String wrongQuestionStudentCountPercent;

    /**
     * 该题目的总错误提交数
     */
    private Integer totalWrongSubmitCount;

}
