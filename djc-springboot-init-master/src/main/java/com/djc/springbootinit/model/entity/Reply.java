package com.djc.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName reply
 */
@TableName(value ="reply")
@Data
public class Reply {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 回复的评论Id
     */
    private Long replyId;

    /**
     * 评论Id
     */
    private Long postId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 回复时间
     */
    private Date replyTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}