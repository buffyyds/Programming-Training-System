package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.dto.Reservation.ReservationEditRequest;
import com.djc.springbootinit.model.entity.Reservation;
import com.djc.springbootinit.model.vo.ReservationVO;
import com.djc.springbootinit.model.vo.UserVO;
import com.djc.springbootinit.service.ReservationService;
import com.djc.springbootinit.mapper.ReservationMapper;
import com.djc.springbootinit.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author djc
* @description 针对表【reservation】的数据库操作Service实现
* @createDate 2025-04-13 16:29:55
*/
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation>
    implements ReservationService{

    @Resource
    private UserService userService;

    @Override
    public void addReservation(long teacherId, String time_slot) {
        //记录预约信息
        Reservation reservation = new Reservation();
        reservation.setTeacherId(teacherId);
        reservation.setTime_slot(time_slot);
        this.save(reservation);
    }

    @Override
    public void updateReservation(ReservationEditRequest reservationEditRequest) {
        Long reservationId = reservationEditRequest.getId();
        String time_slot = reservationEditRequest.getTime_slot();
        //查询原有预约信息
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约信息不存在");
        }
        reservation.setTime_slot(time_slot);
        //更新时间记录
        reservation.setUpdateTime(Date.from(Instant.now()));
        this.updateById(reservation);
    }

    @Override
    public List<Reservation> getReservationByTeacherId(Long teacherId) {
        //查询对应教师发布的预约信息，根据时间降序排列
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacherId);
        queryWrapper.orderByDesc("time_slot");
        return this.list(queryWrapper);
    }

    @Override
    public List<ReservationVO> getReservationVO(List<Reservation> reservationList) {
        //将预约信息转换为预约视图对象
        if (reservationList != null && !reservationList.isEmpty()) {
            return reservationList.stream()
                    .map(reservation -> {
                        ReservationVO reservationVO = new ReservationVO();
                        reservationVO.setId(reservation.getId());
                        reservationVO.setTeacherUser(UserVO.objToVo(userService.getById(reservation.getTeacherId())));
                        reservationVO.setStudentUser(UserVO.objToVo(reservation.getStudentId() == null? null : userService.getById(reservation.getStudentId())));
                        reservationVO.setTime_slot(reservation.getTime_slot());
                        reservationVO.setIsReservation(reservation.getStudentId() != null);
                        return reservationVO;
                    }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean doReservation(DoReservationRequest doReservationRequest) {
        Reservation reservation = this.getById(doReservationRequest.getId());
        reservation.setStudentId(doReservationRequest.getStudentId());
        return this.updateById(reservation);
    }

    @Override
    public boolean unDoReservation(DoReservationRequest doReservationRequest) {
        // 通过 UpdateWrapper 明确指定要更新的字段（即使值为 null）
        UpdateWrapper<Reservation> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq("id", doReservationRequest.getId())  // 指定更新条件
                .set("studentId", null);       // 强制设置 studentId 为 null
        return this.update(updateWrapper);
    }
}




