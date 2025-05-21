package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.entity.ReservationInfo;
import com.djc.springbootinit.service.ReservationInfoService;
import com.djc.springbootinit.mapper.ReservationInfoMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author djc
* @description 针对表【reservation_info】的数据库操作Service实现
* @createDate 2025-05-21 13:32:49
*/
@Service
public class ReservationInfoServiceImpl extends ServiceImpl<ReservationInfoMapper, ReservationInfo>
    implements ReservationInfoService{

    @Override
    public boolean doReservation(DoReservationRequest doReservationRequest) {
        ReservationInfo reservationInfo = new ReservationInfo();
        reservationInfo.setReservationId(doReservationRequest.getId());
        reservationInfo.setStudentId(doReservationRequest.getStudentId());
        return this.save(reservationInfo);
    }

    @Override
    public boolean unDoReservation(DoReservationRequest doReservationRequest) {
        QueryWrapper<ReservationInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reservationId", doReservationRequest.getId());
        queryWrapper.eq("studentId", doReservationRequest.getStudentId());
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean getIsReservation(Long reservationId) {
        QueryWrapper<ReservationInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reservationId", reservationId);
        return !this.list(queryWrapper).isEmpty();
    }

}




