package com.djc.springbootinit.controller;


import com.djc.springbootinit.annotation.AuthCheck;
import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.common.ResultUtils;
import com.djc.springbootinit.constant.UserConstant;
import com.djc.springbootinit.model.dto.Reservation.DoReservationRequest;
import com.djc.springbootinit.model.dto.Reservation.ReservationEditRequest;
import com.djc.springbootinit.model.entity.Reservation;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.ReservationVO;
import com.djc.springbootinit.model.vo.StudentsVo;
import com.djc.springbootinit.model.vo.TeacherVo;
import com.djc.springbootinit.service.ReservationService;
import com.djc.springbootinit.service.TasService;
import com.djc.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 教师学生
 */
@RestController
@RequestMapping("/reservation")
@Slf4j
public class ReservationController {

    @Resource
    private UserService userService;

    @Resource
    private ReservationService reservationService;

    @Resource
    private TasService tasService;

    /**
     * 教师添加预约时间段
     */
    @GetMapping("/add/teacher")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> addReservation(HttpServletRequest request, String time_slot) {
        long teacherId = userService.getLoginUser(request).getId();
        // 添加预约时间段 time_slot格式为"2025-04-13 16:00-17:00"
        reservationService.addReservation(teacherId, time_slot);
        return ResultUtils.success("添加预约时间段成功");
    }

    /**
     * 教师更新预约时间段
     */
    @PostMapping("/update/teacher")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> updateReservation(@RequestBody ReservationEditRequest reservationEditRequest, HttpServletRequest request) {
        // 更新预约时间段 time_slot格式为"2025-04-13 16:00-17:00"
        reservationService.updateReservation(reservationEditRequest);
        return ResultUtils.success("更新预约时间段成功");
    }

    /**
     * 教师删除预约时间段
     */
    @GetMapping("/delete/teacher")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> deleteReservation(long reservationId, HttpServletRequest request) {
        // 删除预约时间段
        boolean b = reservationService.removeById(reservationId);
        return b ? ResultUtils.success("删除预约时间段成功") : ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除预约时间段失败");
    }


    /**
     * 学生获取预约时间段
     */
    @GetMapping("/get")
    public BaseResponse<List<ReservationVO>> getReservation(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long id = loginUser.getId();
        //如果登录用户是学生，则获取其对应的教师id来查询预约信息
        if (loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            id = tasService.getTeacherByStudentId(id).getId();
        }
        // 获取预约时间段
        List<Reservation> reservationList = reservationService.getReservationByTeacherId(id);
        List<ReservationVO> reservationVOList = reservationService.getReservationVO(reservationList);
        return ResultUtils.success(reservationVOList);
    }

    /**
     * 学生预约时间段
     */
    @PostMapping("/doReservation")
    public BaseResponse<String> doReservation(@RequestBody DoReservationRequest doReservationRequest, HttpServletRequest request) {
        // 学生预约时间段
        boolean b = reservationService.doReservation(doReservationRequest);
        return b ? ResultUtils.success("预约成功") : ResultUtils.error(ErrorCode.SYSTEM_ERROR, "预约失败");
    }

    /**
     * 学生取消预约时间段
     */
    @PostMapping("/unDoReservation")
    public BaseResponse<String> unDoReservation(@RequestBody DoReservationRequest doReservationRequest, HttpServletRequest request) {
        // 学生预约时间段
        boolean b = reservationService.unDoReservation(doReservationRequest);
        return b ? ResultUtils.success("取消预约成功") : ResultUtils.error(ErrorCode.SYSTEM_ERROR, "取消预约失败");
    }
}
