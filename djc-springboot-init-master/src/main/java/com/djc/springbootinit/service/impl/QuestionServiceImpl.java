package com.djc.springbootinit.service.impl;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.constant.CommonConstant;
import com.djc.springbootinit.exception.BusinessException;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.mapper.QuestionMapper;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.djc.springbootinit.model.vo.TeacherVo;
import com.djc.springbootinit.service.QuestionSubmitService;
import com.djc.springbootinit.service.TasService;
import org.apache.commons.collections4.CollectionUtils;
import com.djc.springbootinit.model.dto.question.QuestionQueryRequest;
import com.djc.springbootinit.model.entity.*;
import com.djc.springbootinit.model.vo.QuestionVO;
import com.djc.springbootinit.model.vo.UserVO;
import com.djc.springbootinit.service.QuestionService;
import com.djc.springbootinit.service.UserService;
import com.djc.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.dashscope.app.*;

/**
* @author djc
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-02-21 11:14:41
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private TasService tasService;

    @Autowired
    @Lazy
    private QuestionSubmitService questionSubmitService;

    /**
     * 校验题目是否合法
     * @param question
     * @param add
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 获取查询包装类(用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类)
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest , User loginUser) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        if (loginUser.getUserRole().equals("admin")){
            queryWrapper.eq("userId", loginUser.getId());
        }else {
            //获取loginUser的教师/管理员id
            TeacherVo teacher = tasService.getTeacherByStudentId(loginUser.getId());
            if (teacher != null) {
                Long teacherId = teacher.getId();
                queryWrapper.eq("userId", teacherId);
            }else{
                queryWrapper.eq("userId", loginUser.getId());   //学生是没有创建题目的，所以，这里的条件只是限制查出来的数据一定是空的
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        long questionId = question.getId();
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userService.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());
        // 2. 关联查询提交信息用于判断是否完成
        Long loginUserId = userService.getLoginUser(request).getId();
        questionVOList = getQuestionCompleteByUserId(loginUserId, questionVOList);
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    @Override
    public Page<QuestionVO> getStudentProgressVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        User loginUser = userService.getLoginUser(request); //教师信息
        Long teacherId = loginUser.getId();
        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            Long questionId = questionVO.getId();
            // 获取教师旗下的学生的通过数
            Double acceptedNum = questionSubmitService.getPassPercentByTeacherId(teacherId,questionId);
            //获取提交数
            int submitNum = questionSubmitService.getSubmitNumByTeacherId(teacherId,questionId);
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userService.getUserVO(user));
            questionVO.setAcceptedPercent(acceptedNum);
            questionVO.setSubmitNum(submitNum);
            questionVO.setStudentNum(tasService.getStudentIdsByTeacherId(teacherId).size());
            return questionVO;
        }).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    @Override
    public String getQuestionAnswerById(long questionId) {
        Question question = getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return question.getAnswer();
    }

    @Override
    public String getAIScore(QuestionSubmitAddRequest questionSubmitAddRequest) {
        String code = questionSubmitAddRequest.getCode();
        String language = questionSubmitAddRequest.getLanguage();
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ApplicationParam param = ApplicationParam.builder()
                // 若没有配置环境变量，可用百炼API Key将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
                .apiKey("sk-4962bb75201c45959f53b3bda82689d7")
                .appId("2c7611926de447d28e235a86d579bce9")  // 替换为实际的应用 ID
                .prompt("请你分析以下"+language+"代码："+"\n"+code)
                .build();

        Application application = new Application();
        ApplicationResult result = null;
        try {
            result = application.call(param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result.getOutput().getText();
    }

    public List<QuestionVO> getQuestionCompleteByUserId(Long userId, List<QuestionVO> questionVOList) {
        // 1. 收集所有问题ID
        List<Long> questionIds = questionVOList.stream()
                .map(QuestionVO::getId)
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return questionVOList;
        }
        // 2. 批量查询该用户所有相关提交记录
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT questionId")  // 去重，避免重复判断
                .eq("userId", userId)
                .in("questionId", questionIds);
        // 3. 获取该用户已完成的题目ID集合
        Set<Long> completedIds = questionSubmitService.list(queryWrapper)
                .stream()
                .map(QuestionSubmit::getQuestionId)
                .collect(Collectors.toSet());
        // 4. 批量设置完成状态
        questionVOList.forEach(questionVO ->
                questionVO.setIsCompletion(completedIds.contains(questionVO.getId()))
        );
        return questionVOList;
    }

}




