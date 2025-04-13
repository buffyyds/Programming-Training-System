package com.djc.springbootinit.model.dto.Reservation;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *

 */
@Data
public class DoReservationRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 学生id
     */
    private Long studentId;

    private static final long serialVersionUID = 1L;
}