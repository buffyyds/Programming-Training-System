package com.djc.springbootinit.service;

import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.entity.ReservationInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author djc
* @description 针对表【reservation_info】的数据库操作Service
* @createDate 2025-05-21 13:32:49
*/
public interface ReservationInfoService extends IService<ReservationInfo> {


    boolean doReservation(DoReservationRequest doReservationRequest);

    boolean unDoReservation(DoReservationRequest doReservationRequest);

    Boolean getIsReservation(Long reservationId);
}
