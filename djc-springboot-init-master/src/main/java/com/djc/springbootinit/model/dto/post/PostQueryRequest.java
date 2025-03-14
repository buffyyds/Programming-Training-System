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
     * 标题 （本项目不使用）
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表 （本项目不使用）
     */
    private List<String> tags;

    /**
     * 至少有一个标签 （本项目不使用）
     */
    private List<String> orTags;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 收藏用户 id  （本项目不使用）
     */
    private Long favourUserId;

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