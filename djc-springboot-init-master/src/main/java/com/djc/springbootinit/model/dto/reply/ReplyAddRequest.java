package com.djc.springbootinit.model.dto.reply;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *

 */
@Data
public class ReplyAddRequest implements Serializable {

    /**
     *评论id
     */
    private Long postId;

    /**
     *  回复的评论Id
     */
    private Long replyId;

    /**
     * 题目id
     */
    private Long questionId;



    private static final long serialVersionUID = 1L;
}