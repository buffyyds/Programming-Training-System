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
public class SensitiveWordPostVO implements Serializable {

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 题目名称
     */
    private String questionName;

}
