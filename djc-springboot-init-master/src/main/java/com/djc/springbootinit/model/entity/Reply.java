package com.djc.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.djc.springbootinit.model.vo.UserVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 
 * @TableName reply
 */
@Data
public class Reply {
    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer thumbNum;

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

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 题目Id
     */
    private Long questionId;

    /**
     * 评论Id
     */
    private Long replyId;


    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}