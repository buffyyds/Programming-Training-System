package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.dto.Reservation.ReservationEditRequest;
import com.djc.springbootinit.model.entity.RemindComplete;
import com.djc.springbootinit.model.entity.Reservation;
import com.djc.springbootinit.model.entity.ReservationInfo;
import com.djc.springbootinit.model.vo.ReservationPerformanceVO;
import com.djc.springbootinit.model.vo.ReservationVO;
import com.djc.springbootinit.model.vo.UserVO;
import com.djc.springbootinit.service.*;
import com.djc.springbootinit.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author djc
 * @description 针对表【reservation】的数据库操作Service实现
 * @createDate 2025-04-13 16:29:55
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation>
        implements ReservationService {

    @Resource
    private UserService userService;

    @Resource
    private RemindCompleteService remindCompleteService;
    @Autowired
    private TasService tasService;

    @Autowired
    private ReservationInfoService reservationInfoService;

    @Override
    public void addReservation(long teacherId, String time_slot) {
        //记录预约信息
        Reservation reservation = new Reservation();
        reservation.setTeacherId(teacherId);
        reservation.setTime_slot(time_slot);
        this.save(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        QueryWrapper<ReservationInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reservationId", reservationId);
        List<ReservationInfo> reservationInfoList = reservationInfoService.list(queryWrapper);
        if (!reservationInfoList.isEmpty()) {
            //查询学生信息
            for( ReservationInfo reservationInfo : reservationInfoList) {
                Long studentId = reservationInfo.getStudentId();
                UserVO teacherUser = UserVO.objToVo(userService.getById(reservation.getTeacherId()));
                //创建提醒信息
                RemindComplete remindComplete = new RemindComplete();
                remindComplete.setTeacherId(reservation.getTeacherId());
                remindComplete.setStudentId(studentId);
                remindComplete.setIsRead(false);
                remindComplete.setContent("您预约的教师：" + teacherUser.getUserName() + "的答疑时间段：" + oldTime_slot + "已更新为" + newTime_slot + "，请及时查看");
                remindCompleteService.save(remindComplete);
            }
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
        //查询每个预约时间段的学生预约信息
        List<ReservationVO> reservationVOS = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            //查询预约信息
            QueryWrapper<ReservationInfo> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("reservationId", reservation.getId());
            List<ReservationInfo> reservationInfoList = reservationInfoService.list(queryWrapper1);
            ReservationVO reservationVO = new ReservationVO();
            reservationVO.setId(reservation.getId());
            reservationVO.setTime_slot(reservation.getTime_slot());
            reservationVO.setTeacherUser(UserVO.objToVo(userService.getById(reservation.getTeacherId())));
            reservationVO.setStudentUser(reservationInfoList.stream().map(reservationInfo -> {
                Long studentId = reservationInfo.getStudentId();
                return UserVO.objToVo(userService.getById(studentId));
            }).collect(Collectors.toList()));
            reservationVO.setIsReservation(reservationInfoService.getIsReservation(reservation.getId()));
            reservationVOS.add(reservationVO);
        }
        return reservationVOS;
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
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAndRemindStudent(long reservationId) {
        // 查询预约信息
        Reservation reservation = this.getById(reservationId);
        if (reservation != null) {
            // 如果有学生预约了该时间段，则需要提醒学生
            QueryWrapper<ReservationInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("reservationId", reservationId);
            List<ReservationInfo> reservationInfoList = reservationInfoService.list(queryWrapper);
            if (!reservationInfoList.isEmpty()) {
                for (ReservationInfo reservationInfo : reservationInfoList){
                    //删除reservationInfo对应信息
                    reservationInfoService.remove(queryWrapper);
                    Long studentId = reservationInfo.getStudentId();
                    UserVO teacherUser = UserVO.objToVo(userService.getById(reservation.getTeacherId()));
                    //创建提醒信息
                    RemindComplete remindComplete = new RemindComplete();
                    remindComplete.setTeacherId(reservation.getTeacherId());
                    remindComplete.setStudentId(studentId);
                    remindComplete.setIsRead(false);
                    remindComplete.setContent("您预约的教师：" + teacherUser.getUserName() + "的答疑时间段：" + reservation.getTime_slot() + "已被删除，请及时查看");
                    remindCompleteService.save(remindComplete);
                }
            }
            return this.removeById(reservationId);
        }
        return false;
    }

    @Override
    public List<ReservationPerformanceVO> getAllStudentReservation(long teacherId) {
        List<Long> studentIds = tasService.getStudentIdsByTeacherId(teacherId);
        QueryWrapper<ReservationInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("studentId", studentIds);
        List<ReservationInfo> reservationInfoList = reservationInfoService.list(queryWrapper);
        if (reservationInfoList != null && !reservationInfoList.isEmpty()) {
            List<ReservationPerformanceVO> TOPTen = reservationInfoList.stream()
                .collect(Collectors.groupingBy(ReservationInfo::getStudentId))
                .entrySet().stream()
                .map(entry -> {
                    Long studentId = entry.getKey();
                    List<ReservationInfo> reservationInfos = entry.getValue();
                    List<Reservation> reservations = reservationInfos.stream()
                        .map(reservationInfo -> {
                            Long reservationId = reservationInfo.getReservationId();
                            return this.getById(reservationId);
                        })
                        .collect(Collectors.toList());
                    ReservationPerformanceVO reservationPerformanceVO = new ReservationPerformanceVO();
                    reservationPerformanceVO.setStudentUser(UserVO.objToVo(userService.getById(studentId)));
                    reservationPerformanceVO.setTotalTime(calculateTotalTime(reservations));
                    reservationPerformanceVO.setReservationCount(reservations.size());
                    return reservationPerformanceVO;
                }).collect(Collectors.toList());

            return TOPTen;
        }
        return Collections.emptyList();
    }

    /**
     * 计算预约时间段的总和
     */
    private String calculateTotalTime(List<Reservation> reservations) {
        int totalTime = 0;
        for (Reservation reservation : reservations) {
            String timeSlot = reservation.getTime_slot();
            // 时间段格式为 "2025-04-14 16:00-17:00"
            String[] timeParts = timeSlot.split(" ");
            String[] timeRange = timeParts[1].split("-");
            String startTime = timeRange[0];
            String endTime = timeRange[1];
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            // 计算时间差
            int hourDifference = endHour - startHour;
            int minuteDifference = endMinute - startMinute;
            if (minuteDifference < 0) {
                hourDifference--;
                minuteDifference += 60;
            }
            totalTime += hourDifference * 60 + minuteDifference;
        }
        // 将总时间转换为小时和分钟的格式
        int hours = totalTime / 60;
        int minutes = totalTime % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}




