package com.djc.springbootinit.model.vo;

import com.djc.springbootinit.model.entity.RemindComplete;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子视图
 *

 */
@Data
public class RemindCompleteVO implements Serializable {

    /**
     * id
     */
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
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 包装类转对象
     *
     * @param remindCompleteVO
     * @return
     */
    public static RemindComplete voToObj(RemindCompleteVO remindCompleteVO) {
        if (remindCompleteVO == null) {
            return null;
        }
        RemindComplete remindComplete = new RemindComplete();
        BeanUtils.copyProperties(remindCompleteVO, remindComplete);
        return remindComplete;
    }

    /**
     * 对象转包装类
     *
     * @param remindComplete
     * @return
     */
    public static RemindCompleteVO objToVo(RemindComplete remindComplete) {
        if (remindComplete == null) {
            return null;
        }
        RemindCompleteVO remindCompleteVO = new RemindCompleteVO();
        BeanUtils.copyProperties(remindComplete, remindCompleteVO);
        return remindCompleteVO;
    }
}
