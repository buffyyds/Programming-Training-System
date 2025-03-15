package com.djc.springbootinit.model.dto.post;

import com.djc.springbootinit.common.PageRequest;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

    /**
     * 搜索词
     */
    private String searchText;


    /**
     * 内容
     */
    private String content;


    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 是否是回复类型的评论 0-否 1-是
     */
    private Integer isReply;

    /**
     * 回复的评论Id
     */
    private Long replyId;


    private static final long serialVersionUID = 1L;
}