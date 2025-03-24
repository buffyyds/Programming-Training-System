package com.djc.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.dto.question.QuestionQueryRequest;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.djc.springbootinit.model.entity.Question;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;


/**
* @author djc
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2025-02-21 11:14:41
*/
public interface QuestionService extends IService<Question> {
    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest, User loginUser);

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    Page<QuestionVO> getStudentProgressVOPage(Page<Question> questionPage, HttpServletRequest request);

    String getQuestionAnswerById(long questionId);

    String getAIScore(QuestionSubmitAddRequest questionSubmitAddRequest);
}
