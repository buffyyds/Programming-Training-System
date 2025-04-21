package com.djc.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName wrongquestion
 */
@TableName(value ="wrongquestion")
@Data
public class Wrongquestion {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 教师id
     */
    private Long teacherId;

    /**
     * 提交错误次数（总提交错误此超过3次才会记录到此表）
     */
    private Integer wrongSubmitNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    private Integer is_Delete;
}