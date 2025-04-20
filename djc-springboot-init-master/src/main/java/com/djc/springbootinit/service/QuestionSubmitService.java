package com.djc.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.djc.springbootinit.model.entity.Question;
import com.djc.springbootinit.model.entity.QuestionSubmit;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.QuestionCompletionVO;
import com.djc.springbootinit.model.vo.QuestionSubmitVO;
import com.djc.springbootinit.model.vo.QuestionVO;
import com.djc.springbootinit.model.vo.StudentCompletionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author djc
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2025-02-21 11:16:10
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

    Boolean getQuestionSubmitPass(Long questionId, Long userId);

    QueryWrapper<QuestionSubmit> getQueryWrapperTeacher(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    Double getPassPercentByTeacherId(Long teacherId, Long questionId);

    int getSubmitNumByTeacherId(Long teacherId, Long questionId);

    List<StudentCompletionVO> getStudentCompletion(Long questionId, HttpServletRequest request);

    List<QuestionCompletionVO> getStudentCompletionByCurrentUser(User loginUser);

    void isThirdErrorSubmission(Question question, long userId);
}
