package com.djc.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子视图
 *

 */
@Data
public class WrongquestionDetailVO implements Serializable {

    /**
     * 学生User
     */
    private UserVO studentUser;

    /**
     * 错题提交次数
     */
    private Integer wrongSubmitNum;

    /**
     * 是否标记该题为错题
     */
    private Boolean isWrongQuestion;

}
