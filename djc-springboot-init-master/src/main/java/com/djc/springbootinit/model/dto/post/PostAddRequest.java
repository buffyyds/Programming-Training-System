package com.djc.springbootinit.model.dto.post;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 创建请求
 *

 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * 标题（本项目不使用）
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（本项目不使用）
     */
    private List<String> tags;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 是否是回复类型的评论 0-否 1-是
     */
    private Integer isReply;

    private static final long serialVersionUID = 1L;
}