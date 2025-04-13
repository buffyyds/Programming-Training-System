package com.djc.springbootinit.controller;


import com.djc.springbootinit.annotation.AuthCheck;
import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ResultUtils;
import com.djc.springbootinit.constant.UserConstant;
import com.djc.springbootinit.model.vo.StudentsVo;
import com.djc.springbootinit.model.vo.TeacherVo;
import com.djc.springbootinit.service.TasService;
import com.djc.springbootinit.service.UserService;
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

   

}
