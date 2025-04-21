package com.djc.springbootinit.model.vo;

import com.djc.springbootinit.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 *

 */
@Data
public class WrongquestionVO implements Serializable {
    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 题目名
     */
    private String questionName;

    /**
     * 标记错题学生人数
     */
    private Integer wrongQuestionStudentCount;

    /**
     * 学生总人数
     */
    private Integer studentCount;

    /**
     * 该题目的总错误提交数
     */
    private Integer totalWrongSubmitCount;


}
