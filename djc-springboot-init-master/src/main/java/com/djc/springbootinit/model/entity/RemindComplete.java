package com.djc.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName remind_complete
 */
@TableName(value ="remind_complete")
@Data
public class RemindComplete {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教师id
     */
    private Long teacherId;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 是否已读
     */
    private Boolean isRead;
    /**
     * 内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}