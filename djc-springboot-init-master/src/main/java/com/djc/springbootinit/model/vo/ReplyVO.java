package com.djc.springbootinit.model.vo;


import cn.hutool.json.JSONUtil;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.service.UserService;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Data
public class ReplyVO {

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
     * 是否已点赞
     */
    private Boolean hasThumb;

    /**
     * 是否是回复
     */
    private Boolean isReply;

    /**
     * 回复的评论Id
     */
    private Long replyId;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 是否包含敏感词
     */
    private Boolean isContainsSensitiveWord;

    private static final long serialVersionUID = 1L;


}
