package com.djc.springbootinit.model.vo;


import com.djc.springbootinit.model.dto.question.JudgeConfig;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class QuestionCompletionVO {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签列表
     */
    private List<String> tags;


    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private Boolean isCompletion;

    private static final long serialVersionUID = 1L;
}
