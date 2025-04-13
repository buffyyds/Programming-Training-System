package com.djc.springbootinit.model.dto.Reservation;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑请求
 *

 */
@Data
public class ReservationEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 时间段
     */
    private String time_slot;

    private static final long serialVersionUID = 1L;
}