package com.djc.springbootinit.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djc.springbootinit.annotation.AuthCheck;
import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.common.ResultUtils;
import com.djc.springbootinit.constant.UserConstant;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.QuestionSubmitVO;
import com.djc.springbootinit.model.vo.StudentsVo;
import com.djc.springbootinit.model.vo.TeacherVo;
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
@RequestMapping("/TAS")
@Slf4j
public class TASController {

    @Resource
    private UserService userService;

    @Resource
    private TasService tasService;
    /**
     * 通过教师id获取旗下所有学生信息(只有教师可以访问)
     */
    @GetMapping("getStudents")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<List<StudentsVo>> getStudents(HttpServletRequest request) {
        Long id = userService.getLoginUser(request).getId();
        List<StudentsVo> studentsByTeacherId = tasService.getStudentsByTeacherId(id);
        return ResultUtils.success(studentsByTeacherId);
    }

    /**
     * 通过学生id获取对应教师信息
     */
    @GetMapping("getTeacher")
    public BaseResponse<TeacherVo> getTeacher(HttpServletRequest request) {
        Long id = userService.getLoginUser(request).getId();
        TeacherVo teacherByStudentId = tasService.getTeacherByStudentId(id);
        return ResultUtils.success(teacherByStudentId);
    }

    /**
     * 学生获取对应教师发布的题目
     */


    /**
     * 教师获取对应学生的完成情况
     */
//    @GetMapping("getStudentFinish")
//    public BaseResponse<Page<QuestionSubmitVO>> listStudentFinishByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
//                                                                        HttpServletRequest request) {
//
//        return ResultUtils.success();
//    }

    /**
     * 教师踢出学生
     */
    @GetMapping("kickStudent")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<String> kickStudent(Long studentId, HttpServletRequest request) {
        Long teacherId = userService.getLoginUser(request).getId();
        boolean b = tasService.kickStudent(teacherId, studentId);
        return b ? ResultUtils.success("踢出学生成功") : ResultUtils.error(ErrorCode.OPERATION_ERROR,"踢出学生失败");
    }

    /**
     * 管理员获取教师列表
     */
    @GetMapping("getTeacherList")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<TeacherVo>> getTeacherList(HttpServletRequest request) {
        List<TeacherVo> teacherList = tasService.getTeacherList();
        return ResultUtils.success(teacherList);
    }

    /**
     * 管理员获取学生列表
     */
    @GetMapping("getStudentList")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<StudentsVo>> getStudentList(HttpServletRequest request) {
        List<StudentsVo> studentList = tasService.getStudentList();
        return ResultUtils.success(studentList);
    }

}
