package com.djc.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子视图
 *

 */
@Data
public class ReservationPerformanceVO implements Serializable {
    /**
     * 学生User
     */
    private UserVO studentUser;

    /**
     * 时间段
     */
    private String totalTime;

    /**
     * 预约次数
     */
    private int reservationCount;
}
