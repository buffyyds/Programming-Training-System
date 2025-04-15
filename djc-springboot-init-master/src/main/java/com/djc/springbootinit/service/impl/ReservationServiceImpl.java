package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.dto.Reservation.ReservationEditRequest;
import com.djc.springbootinit.model.entity.RemindComplete;
import com.djc.springbootinit.model.entity.Reservation;
import com.djc.springbootinit.model.vo.ReservationVO;
import com.djc.springbootinit.model.vo.UserVO;
import com.djc.springbootinit.service.RemindCompleteService;
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

    @Resource
    private RemindCompleteService remindCompleteService;

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
        String newTime_slot = reservationEditRequest.getTime_slot();
        //查询原有预约信息
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约信息不存在");
        }
        String oldTime_slot = reservation.getTime_slot();
        reservation.setTime_slot(newTime_slot);
        //更新时间记录
        reservation.setUpdateTime(Date.from(Instant.now()));
        this.updateById(reservation);
        //如果已经有学生预约了该时间段，则需要提醒学生
        if (reservation.getStudentId() != null) {
            //查询学生信息
            Long studentId = reservation.getStudentId();
            Long teacherId = reservation.getTeacherId();
            UserVO teacherUser = UserVO.objToVo(userService.getById(teacherId));
            //创建提醒信息
            RemindComplete remindComplete = new RemindComplete();
            remindComplete.setTeacherId(teacherId);
            remindComplete.setStudentId(studentId);
            remindComplete.setIsRead(false);
            remindComplete.setContent("您预约的教师："+teacherUser.getUserName()+"的答疑时间段："+oldTime_slot +"已更新为"+newTime_slot+"，请及时查看");
            remindCompleteService.save(remindComplete);
        }
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

    @Override
    public boolean removeAndRemindStudent(long reservationId) {
        // 查询预约信息
        Reservation reservation = this.getById(reservationId);
        if (reservation != null) {
            // 删除预约信息
            this.removeById(reservationId);
            // 如果有学生预约了该时间段，则需要提醒学生
            if (reservation.getStudentId() != null) {
                // 查询学生信息
                Long studentId = reservation.getStudentId();
                Long teacherId = reservation.getTeacherId();
                UserVO teacherUser = UserVO.objToVo(userService.getById(teacherId));
                // 创建提醒信息
                RemindComplete remindComplete = new RemindComplete();
                remindComplete.setTeacherId(teacherId);
                remindComplete.setStudentId(studentId);
                remindComplete.setIsRead(false);
                remindComplete.setContent("您预约的教师："+teacherUser.getUserName()+"的答疑时间段："+reservation.getTime_slot() +"已被删除，请及时查看");
                return remindCompleteService.save(remindComplete);
            }
            return true;
        }
        return false;
    }
}




