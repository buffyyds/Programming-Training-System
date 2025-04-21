package com.djc.springbootinit.service;

import com.djc.springbootinit.model.entity.Wrongquestion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.vo.WrongquestionDetailVO;
import com.djc.springbootinit.model.vo.WrongquestionPerformanceVO;
import com.djc.springbootinit.model.vo.WrongquestionVO;

import java.util.List;

/**
* @author djc
* @description 针对表【wrongquestion】的数据库操作Service
* @createDate 2025-04-13 16:29:55
*/
public interface WrongquestionService extends IService<Wrongquestion> {

    boolean unDoWrongQuestion(long questionId,Long userId);

    boolean doWrongQuestion(long questionId,Long userId);

    List<Wrongquestion> getWrongQuestionListByStudent(Long userId);

    List<WrongquestionVO> getWrongQuestionListByTeacher(Long userId);

    List<WrongquestionDetailVO> getWrongQuestionDetailByTeacher(long questionId, Long userId);

    List<WrongquestionPerformanceVO> getWrongQuestionAnalysis(Long userId);
}
