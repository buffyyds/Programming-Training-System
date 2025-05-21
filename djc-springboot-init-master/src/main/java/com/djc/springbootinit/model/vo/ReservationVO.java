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
public class ReservationVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 教师User
     */
    private UserVO teacherUser;

    /**
     * 预约学生User
     */
    private List<UserVO> studentUser;

    /**
     * 时间段
     */
    private String time_slot;

    /**
     * 是否已经预约
     */
    private Boolean isReservation;
}
