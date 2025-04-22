package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.model.entity.Complete;
import com.djc.springbootinit.model.entity.QuestionSubmit;
import com.djc.springbootinit.model.entity.Wrongquestion;
import com.djc.springbootinit.model.enums.JudgeInfoMessageEnum;
import com.djc.springbootinit.model.vo.UserVO;
import com.djc.springbootinit.model.vo.WrongquestionDetailVO;
import com.djc.springbootinit.model.vo.WrongquestionPerformanceVO;
import com.djc.springbootinit.model.vo.WrongquestionVO;
import com.djc.springbootinit.service.*;
import com.djc.springbootinit.mapper.WrongquestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author djc
* @description 针对表【wrongquestion】的数据库操作Service实现
* @createDate 2025-04-13 16:29:55
*/
@Service
public class WrongquestionServiceImpl extends ServiceImpl<WrongquestionMapper, Wrongquestion>
    implements WrongquestionService{

    @Resource
    private CompleteService completeService;

    @Autowired
    private TasService tasService;

    @Resource
    private QuestionService questionService;

    @Autowired
    @Lazy
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    @Override
    public boolean unDoWrongQuestion(long questionId,Long userId) {
        //必须要当前用户通过了该题目才能取消标记，所以要先判断完成表中有没有该学生通过该题目的数据
        QueryWrapper<Complete> completeQueryWrapper = new QueryWrapper<>();
        completeQueryWrapper.eq("studentId",userId);
        completeQueryWrapper.eq("questionId",questionId);
        completeQueryWrapper.eq("isDelete",0);
        Complete complete = completeService.getOne(completeQueryWrapper);
        if (complete != null) {
            //如果完成表中有该学生通过该题目的数据，则可以取消标记
            QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
            wrongquestionQueryWrapper.eq("studentId",userId);
            wrongquestionQueryWrapper.eq("questionId",questionId);
            wrongquestionQueryWrapper.eq("is_Delete",0);
            Wrongquestion wrongquestion = this.getOne(wrongquestionQueryWrapper);
            if (wrongquestion != null) {
                //如果错题表中有该学生的错题数据，则可以取消标记
                UpdateWrapper<Wrongquestion> updateWrapper = new UpdateWrapper<>();
                updateWrapper
                        .eq("studentId",userId)
                        .eq("questionId",questionId)
                        .set("is_Delete",1);
                return this.update(updateWrapper);
            }
        }
        return false;
    }

    @Override
    public boolean doWrongQuestion(long questionId, Long userId) {
        //不需要当前用户已完成才能标记，不过需要用户提交过一次代码，才能将其标记
        QueryWrapper<QuestionSubmit> questionSubmit = new QueryWrapper<>();
        questionSubmit.eq("questionId",questionId);
        questionSubmit.eq("userId",userId);
        questionSubmit.eq("status",2);
        List<QuestionSubmit> list = questionSubmitService.list(questionSubmit);
        ThrowUtils.throwIf(list.isEmpty(), ErrorCode.OPERATION_ERROR, "请先提交代码");
        //如果提交表中有该学生的提交数据，则可以标记
        //如果错题表中原来有该学生的错题数据，但是被取消标记了即isDelete=1，则可以重新计算错题数，并将isDelete=0
        QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
        wrongquestionQueryWrapper.eq("studentId",userId);
        wrongquestionQueryWrapper.eq("questionId",questionId);
        wrongquestionQueryWrapper.eq("is_Delete",1);
        Wrongquestion wrongquestion = this.getOne(wrongquestionQueryWrapper);
        if (wrongquestion != null) {
            //如果错题表中有该学生的错题数据，则重新计算错题数，并将isDelete=0
            UpdateWrapper<Wrongquestion> updateWrapper = new UpdateWrapper<>();
            updateWrapper
                    .eq("studentId",userId)
                    .eq("questionId",questionId)
                    .set("is_Delete",0)
                    .set("wrongSubmitNum",questionSubmitService.getUserWrongSubmitNumByQuestionId(questionId, userId));
            return this.update(updateWrapper);
        }else{
            Wrongquestion wrongQuestion = new Wrongquestion();
            wrongQuestion.setStudentId(userId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setTeacherId(tasService.getTeacherByStudentId(userId).getId());
            wrongQuestion.setWrongSubmitNum(questionSubmitService.getUserWrongSubmitNumByQuestionId(questionId, userId));
            wrongQuestion.setIs_Delete(0);
            return this.save(wrongQuestion);
        }
    }

    @Override
    public List<Wrongquestion> getWrongQuestionListByStudent(Long userId) {
        //获取当前用户（学生）的错题列表
        QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
        wrongquestionQueryWrapper.eq("studentId",userId);
        wrongquestionQueryWrapper.eq("is_Delete",0);
        return this.list(wrongquestionQueryWrapper);
    }

    @Override
    public List<WrongquestionVO> getWrongQuestionListByTeacher(Long userId) {
        //获取当前教师旗下学生的错题列表
        List<Long> studentIdsByTeacherId = tasService.getStudentIdsByTeacherId(userId);
        if (studentIdsByTeacherId == null || studentIdsByTeacherId.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
        wrongquestionQueryWrapper.in("studentId", studentIdsByTeacherId);
        wrongquestionQueryWrapper.eq("is_Delete",0);
        List<Wrongquestion> list = this.list(wrongquestionQueryWrapper);
        //根据题目划分list中的数据
        return list.stream()
            .collect(Collectors.groupingBy(Wrongquestion::getQuestionId))
            .entrySet().stream().map(entry -> {
                Long questionId = entry.getKey();
                WrongquestionVO wrongquestionVO = new WrongquestionVO();
                wrongquestionVO.setQuestionId(questionId);
                wrongquestionVO.setQuestionName(questionService.getById(questionId).getTitle());
                wrongquestionVO.setStudentCount(studentIdsByTeacherId.size());
                wrongquestionVO.setTotalWrongSubmitCount(questionSubmitService.getTotalWrongSubmitNumByTeacherId(questionId,userId)); //总错误提交数
                int size = list.stream()
                        .filter(wrongquestion -> wrongquestion.getQuestionId().equals(questionId)).collect(Collectors.toList()).size();
                wrongquestionVO.setWrongQuestionStudentCount(size);  //设置该题目的错题学生数
                return wrongquestionVO;
            }).collect(Collectors.toList());
    }

    @Override
    public List<WrongquestionDetailVO> getWrongQuestionDetailByTeacher(long questionId, Long userId) {
        //获取该题目的错题表数据
        QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
        wrongquestionQueryWrapper.eq("questionId",questionId);
        wrongquestionQueryWrapper.eq("teacherId",userId);
        wrongquestionQueryWrapper.eq("is_Delete",0);
        List<Wrongquestion> wrongquestionList = this.list(wrongquestionQueryWrapper);
        if (wrongquestionList == null || wrongquestionList.isEmpty()) {
            return Collections.emptyList();
        }
        //获取教师旗下学生id
        List<Long> studentIdsByTeacherId = tasService.getStudentIdsByTeacherId(userId);
        List<WrongquestionDetailVO> wrongquestionDetailVOList = studentIdsByTeacherId.stream()
                .map(studentId -> {
                    WrongquestionDetailVO wrongquestionDetailVO = new WrongquestionDetailVO();
                    wrongquestionDetailVO.setStudentUser(UserVO.objToVo(userService.getById(studentId)));
                    wrongquestionDetailVO.setWrongSubmitNum(questionSubmitService.getUserWrongSubmitNumByQuestionId(questionId, studentId));
                    wrongquestionDetailVO.setIsWrongQuestion(wrongquestionList.stream()
                            .anyMatch(wrongquestion -> wrongquestion.getStudentId().equals(studentId)));
                    return wrongquestionDetailVO;
                }).collect(Collectors.toList());
        return wrongquestionDetailVOList;
    }

    @Override
    public List<WrongquestionPerformanceVO> getWrongQuestionAnalysis(Long userId) {
        //获取该教师下的所有错题题目
        QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
        wrongquestionQueryWrapper.eq("teacherId",userId);
        wrongquestionQueryWrapper.eq("is_Delete",0);
        List<Wrongquestion> wrongquestionList = this.list(wrongquestionQueryWrapper);
        if (wrongquestionList == null || wrongquestionList.isEmpty()) {
            return Collections.emptyList();
        }
        //获取教师旗下学生id
        List<Long> studentIdsByTeacherId = tasService.getStudentIdsByTeacherId(userId);
        //根据题目划分list中的数据
        return wrongquestionList.stream()
                .collect(Collectors.groupingBy(Wrongquestion::getQuestionId))
                .entrySet().stream().map(entry -> {
                    Long questionId = entry.getKey();
                    WrongquestionPerformanceVO wrongquestionPerformanceVO = new WrongquestionPerformanceVO();
                    wrongquestionPerformanceVO.setQuestionName(questionService.getById(questionId).getTitle());
                    wrongquestionPerformanceVO.setTotalWrongSubmitCount(questionSubmitService.getTotalWrongSubmitNumByTeacherId(questionId,userId)); //总错误提交数
                    int size = wrongquestionList.stream()
                            .filter(wrongquestion -> wrongquestion.getQuestionId().equals(questionId)).collect(Collectors.toList()).size();
                    wrongquestionPerformanceVO.setWrongQuestionStudentCount(size);  //设置该题目的错题学生数
                    wrongquestionPerformanceVO.setWrongQuestionStudentCountPercent(
                            (double) size / studentIdsByTeacherId.size() * 100 + "%"
                    ); //设置该题目的错题学生数占比
                    return wrongquestionPerformanceVO;
                }).collect(Collectors.toList());
    }
}




