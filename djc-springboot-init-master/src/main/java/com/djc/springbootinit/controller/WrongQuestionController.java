package com.djc.springbootinit.controller;



import com.djc.springbootinit.annotation.AuthCheck;
import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.common.ResultUtils;
import com.djc.springbootinit.constant.UserConstant;
import com.djc.springbootinit.model.entity.Wrongquestion;
import com.djc.springbootinit.model.enums.UserRoleEnum;
import com.djc.springbootinit.model.vo.WrongquestionDetailVO;
import com.djc.springbootinit.model.vo.WrongquestionPerformanceVO;
import com.djc.springbootinit.model.vo.WrongquestionVO;
import com.djc.springbootinit.service.UserService;
import com.djc.springbootinit.service.WrongquestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 教师学生
 */
@RestController
@RequestMapping("/wrongQuestion")
@Slf4j
public class WrongQuestionController {

    @Resource
    private UserService userService;

    @Resource
    private WrongquestionService wrongquestionService;

    /**
     * 取消标记错题
     */
    @GetMapping("/unDoWrongQuestion")
    public BaseResponse<Boolean> unDoWrongQuestion(long questionId,HttpServletRequest request) {
        //获取当前登录用户
        Long userId = userService.getLoginUser(request).getId();
        //取消标记错题
        boolean result = wrongquestionService.unDoWrongQuestion(questionId,userId);
        return result ? ResultUtils.success(result) : ResultUtils.error(ErrorCode.PARAMS_ERROR,"取消标记错题失败");
    }

    /**
     * 标记错题
     */
    @GetMapping("/doWrongQuestion")
    public BaseResponse<Boolean> doWrongQuestion(long questionId,HttpServletRequest request) {
        //获取当前登录用户
        Long userId = userService.getLoginUser(request).getId();
        //标记错题
        boolean result = wrongquestionService.doWrongQuestion(questionId,userId);
        return ResultUtils.success(result);
    }

    /**
     * 学生获取错题列表
     */
    @GetMapping("/getWrongQuestionListByStudent")
    public BaseResponse<List<Wrongquestion>> getWrongQuestionListByStudent(HttpServletRequest request) {
        //获取当前登录用户
        Long userId = userService.getLoginUser(request).getId();
        //获取错题列表
        List<Wrongquestion> wrongQuestionList = wrongquestionService.getWrongQuestionListByStudent(userId);
        return ResultUtils.success(wrongQuestionList);
    }

    /**
     * 教师获取错题列表
     */
    @GetMapping("/getWrongQuestionListByTeacher")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<List<WrongquestionVO>> getWrongQuestionListByTeacher(HttpServletRequest request) {
        //获取当前登录用户
        Long userId = userService.getLoginUser(request).getId();
        //获取错题列表
        List<WrongquestionVO> wrongQuestionList = wrongquestionService.getWrongQuestionListByTeacher(userId);
        return ResultUtils.success(wrongQuestionList);
    }

    /**
     * 教师获取错题详情
     */
    @GetMapping("/getWrongQuestionDetailByTeacher")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<List<WrongquestionDetailVO>> getWrongQuestionDetailByTeacher(long questionId ,HttpServletRequest request) {
        //获取当前登录用户
        Long userId = userService.getLoginUser(request).getId();
        //获取错题详情
        List<WrongquestionDetailVO> wrongQuestionDetailList = wrongquestionService.getWrongQuestionDetailByTeacher(questionId,userId);
        return ResultUtils.success(wrongQuestionDetailList);
    }

    /**
     * 教师获取错题分析图表数据
     */
    @GetMapping("/getWrongQuestionAnalysis")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<List<WrongquestionPerformanceVO>> getWrongQuestionAnalysis(HttpServletRequest request) {
        //获取当前登录用户
        Long userId = userService.getLoginUser(request).getId();
        //获取错题分析图表数据
        List<WrongquestionPerformanceVO> wrongQuestionAnalysisList = wrongquestionService.getWrongQuestionAnalysis(userId);
        return ResultUtils.success(wrongQuestionAnalysisList);
    }

}
