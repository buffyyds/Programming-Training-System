package com.djc.springbootinit.service;

import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.dto.Reservation.ReservationEditRequest;
import com.djc.springbootinit.model.entity.Reservation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.vo.ReservationVO;

import java.util.List;

/**
* @author djc
* @description 针对表【reservation】的数据库操作Service
* @createDate 2025-04-13 16:29:55
*/
public interface ReservationService extends IService<Reservation> {

    void addReservation(long teacherId, String time_slot);

    void updateReservation(ReservationEditRequest reservationEditRequest);

    List<Reservation> getReservationByTeacherId(Long teacherId);

    List<ReservationVO> getReservationVO(List<Reservation> reservationList);

    boolean doReservation(DoReservationRequest doReservationRequest);

    boolean unDoReservation(DoReservationRequest doReservationRequest);

    boolean removeAndRemindStudent(long reservationId);
}
