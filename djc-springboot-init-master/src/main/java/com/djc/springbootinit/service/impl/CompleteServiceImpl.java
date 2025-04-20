package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.model.entity.Complete;
import com.djc.springbootinit.model.vo.TeacherVo;
import com.djc.springbootinit.service.CompleteService;
import com.djc.springbootinit.mapper.CompleteMapper;
import com.djc.springbootinit.service.TasService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author djc
 * @description 针对表【complete】的数据库操作Service实现
 * @createDate 2025-04-20 10:03:32
 */
@Service
public class CompleteServiceImpl extends ServiceImpl<CompleteMapper, Complete>
        implements CompleteService {
    @Resource
    private TasService tasService;

    @Override
    public void completeQuestion(Long studentId, Long questionId) {
        //获取该学生的教师id
        TeacherVo teacherByStudentId = tasService.getTeacherByStudentId(studentId);
        ThrowUtils.throwIf(teacherByStudentId == null, ErrorCode.NOT_FOUND_ERROR,"该学生没有绑定教师");
        Complete complete = new Complete();
        complete.setStudentId(studentId);
        complete.setQuestionId(questionId);
        complete.setTeacherId(teacherByStudentId.getId());
        complete.setIsDelete(0);
        this.save(complete);
    }
}




