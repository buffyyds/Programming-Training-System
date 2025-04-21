package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.constant.CommonConstant;
import com.djc.springbootinit.constant.UserConstant;
import com.djc.springbootinit.exception.BusinessException;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.judge.JudgeService;
import com.djc.springbootinit.judge.JudgeServiceImpl;
import com.djc.springbootinit.mapper.QuestionSubmitMapper;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.djc.springbootinit.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.djc.springbootinit.model.entity.Question;
import com.djc.springbootinit.model.entity.QuestionSubmit;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.entity.Wrongquestion;
import com.djc.springbootinit.model.enums.JudgeInfoMessageEnum;
import com.djc.springbootinit.model.enums.QuestionSubmitStatusEnum;
import com.djc.springbootinit.model.vo.*;
import com.djc.springbootinit.rabbitmq.MyMessageProducer;
import com.djc.springbootinit.service.*;
import com.djc.springbootinit.utils.SqlUtils;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.djc.springbootinit.model.enums.QuestionSubmitLanguageEnum;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
* @author djc
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2025-02-21 11:16:10
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Resource
    private TasService tasService;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    private WrongquestionService wrongquestionService;

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 是否已题目提交
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        questionSubmit.setExecuteResult("{}");
        //save方法是继承自ServiceImpl的方法,用于判断是否插入成功
        boolean save = this.save(questionSubmit);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
//        myMessageProducer.sendMessage("code_exchange", "my_routingKey", String.valueOf(questionSubmitId));
        CompletableFuture.runAsync(() -> {
            // 异步判题
             judgeService.doJudge(questionSubmitId);
        });
        return questionSubmitId;
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        //Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String title = questionSubmitQueryRequest.getTitle();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        //处理题目，将通过title查询到的questionId放入questionIds
        if (StringUtils.isNotBlank(title)) {
            QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
            questionQueryWrapper.like("title", title);
            List<Question> questionList = questionService.list(questionQueryWrapper);
            if (CollectionUtils.isNotEmpty(questionList)) {
                List<Long> questionIds = questionList.stream().map(Question::getId).collect(Collectors.toList());
                queryWrapper.in("questionId", questionIds);
            }
        }

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        //queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isTeacher(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        //通过questionSubmitVOList里的userId获取对应UserVo questionId获取对应QuestionVo
        questionSubmitVOList.forEach(questionSubmitVO -> {
            UserVO userVO = UserVO.objToVo(userService.getById(questionSubmitVO.getUserId()));
            QuestionVO questionVo = QuestionVO.objToVo(questionService.getById(questionSubmitVO.getQuestionId()));
            questionSubmitVO.setUserVO(userVO);
            questionSubmitVO.setQuestionVO(questionVo);
        });
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public Boolean getQuestionSubmitPass(Long questionId, Long userId) {
        // 根据questionId和userId查询题目提交记录
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.eq("userId", userId);
        queryWrapper.like("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
        List<QuestionSubmit> questionSubmitList = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(questionSubmitList)) {
            return true;
        }
        return false;
    }

    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapperTeacher(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long userId = questionSubmitQueryRequest.getUserId();
        String title = questionSubmitQueryRequest.getTitle();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        //通过userId（即teacherid）获取旗下所有学生的id
        List<Long> studentIds = tasService.getStudentIdsByTeacherId(userId);
        studentIds.add(userId); //将老师自己也加入
        //处理题目，将通过title查询到的questionId放入questionIds
        if (StringUtils.isNotBlank(title)) {
            QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
            questionQueryWrapper.like("title", title);
            List<Question> questionList = questionService.list(questionQueryWrapper);
            if (CollectionUtils.isNotEmpty(questionList)) {
                List<Long> questionIds = questionList.stream().map(Question::getId).collect(Collectors.toList());
                queryWrapper.in("questionId", questionIds);
            }
        }

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.in(ObjectUtils.isNotEmpty(studentIds), "userId", studentIds);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Double getPassPercentByTeacherId(Long teacherId, Long questionId) {
        //通过teacherId获取旗下所有学生的id
        List<Long> studentIds = tasService.getStudentIdsByTeacherId(teacherId);
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.in("userId", studentIds);
        queryWrapper.like("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
        List<QuestionSubmit> list = list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return 0.0;
        }
        long count = list.stream().map(QuestionSubmit::getUserId).distinct().count();
        Double passPercent = (double) count/studentIds.size();
        return passPercent;
    }

    @Override
    public int getSubmitNumByTeacherId(Long teacherId, Long questionId) {
        //通过teacherId获取旗下所有学生的id
        List<Long> studentIds = tasService.getStudentIdsByTeacherId(teacherId);
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.in("userId", studentIds);
        long count = this.count(queryWrapper);
        if (count == 0) {
            return 0;
        }
        return (int)count;
    }

    @Override
    public List<StudentCompletionVO> getStudentCompletion(Long questionId, HttpServletRequest request) {
        List<StudentCompletionVO> studentCompletionVOList = new ArrayList<>();
        //获取当前教师旗下的学生ids
        User loginUser = userService.getLoginUser(request);
        List<Long> studentIds = tasService.getStudentIdsByTeacherId(loginUser.getId());
        if (studentIds.isEmpty()) {
            return studentCompletionVOList;
        }
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT userId");
        queryWrapper.eq("questionId", questionId);
        queryWrapper.in("userId", studentIds);
        queryWrapper.like("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
        // 执行查询并提取已完成的用户ID
        Set<Long> completedStudentIds = this.listObjs(queryWrapper)
                .stream()
                .map(id -> (Long) id)
                .collect(Collectors.toSet());
        // 查询学生名字
        List<User> studentList = userService.listByIds(studentIds);
        // 构建学生id->名字Map
        Map<Long, String> studentIdNameMap = studentList.stream().collect(Collectors.toMap(User::getId, User::getUserName));
        // 构建结果列表
        studentIds.forEach(studentId -> {
            StudentCompletionVO vo = new StudentCompletionVO();
            vo.setIsCompletion(completedStudentIds.contains(studentId));
            vo.setStudentName(studentIdNameMap.get(studentId));
            vo.setStudentId(studentId);
            studentCompletionVOList.add(vo);
        });
        // 旧代码(尽量避免循环查询数据库，会导致性能问题)
//        studentIds.forEach(studentId -> {
//            QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("questionId", questionId);
//            queryWrapper.eq("userId", studentId);
//            queryWrapper.like("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
//            //只要查询到一条记录，即表示该学生已完成
//            long count = this.count(queryWrapper);
//            StudentCompletionVO studentCompletionVO = new StudentCompletionVO();
//            if (count > 0) {
//                studentCompletionVO.setIsCompletion(true);
//            }
//            studentCompletionVOList.add(studentCompletionVO);
//        });

        return studentCompletionVOList;
    }

    @Override
    public List<QuestionCompletionVO> getStudentCompletionByCurrentUser(User loginUser) {
//        //获取当前用户的id
//        User user = userService.getById(loginUser.getId());
//        //获取当前用户的角色
//        String role = user.getUserRole();
//        if (!role.equals(UserConstant.DEFAULT_ROLE)){
//            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "当前用户不是学生");
//        }
//        //查询学生对应的教师id
//        Long teacherId = tasService.getTeacherByStudentId(user.getId()).getId();
//        //获取该教师发布的所有题目
//        List<Question> questionList = questionService.list(new QueryWrapper<Question>().eq("userId", teacherId));

        return Collections.emptyList();
    }

    @Override
    public void isThirdErrorSubmission(Question question, long userId) {
        //先判断当前用户是否是学生，如果不是的话则之间返回
        User user = userService.getById(userId);
        if (!user.getUserRole().equals(UserConstant.STUDENT_ROLE)){
            return;
        }
        //获取当前用户的提交记录
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", question.getId());
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("status", QuestionSubmitStatusEnum.SUCCEED.getValue());
        queryWrapper.notLike("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
        queryWrapper.orderByDesc("createTime");
        List<QuestionSubmit> list = this.list(queryWrapper);
        if (list.size() > 2){
            //表明此次不是第三次错误提交，但是该题目是错题，错题数+1
            QueryWrapper<Wrongquestion> wrongquestionQueryWrapper = new QueryWrapper<>();
            wrongquestionQueryWrapper.eq("questionId", question.getId());
            wrongquestionQueryWrapper.eq("studentId", userId);
            wrongquestionQueryWrapper.eq("is_Delete", 0);
            Wrongquestion wrongquestion = wrongquestionService.getOne(wrongquestionQueryWrapper);
            //如果没有错题记录，则表明该用户已经将错题标记取消
            if (wrongquestion == null){
                QueryWrapper<Wrongquestion> wrongquestionQueryWrapper2 = new QueryWrapper<>();
                wrongquestionQueryWrapper2.eq("questionId", question.getId());
                wrongquestionQueryWrapper2.eq("studentId", userId);
                wrongquestionQueryWrapper2.eq("is_Delete", 1);
                wrongquestion = wrongquestionService.getOne(wrongquestionQueryWrapper2);
                ThrowUtils.throwIf(wrongquestion == null, ErrorCode.NOT_FOUND_ERROR, "数据查询错误");
                wrongquestion.setIs_Delete(0);
            }
            //考虑多线程问题，这里由于前端设置了提交代码在获取到结果前是置灰的，所以后端就先不处理了
            wrongquestion.setWrongSubmitNum(wrongquestion.getWrongSubmitNum() + 1);
            boolean update = wrongquestionService.updateById(wrongquestion);
            if (!update){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新错题失败");
            }
        }else if (list.size() == 2){
            //表明此次提交是第三次错误提交，直接标记为错题
            Wrongquestion wrongquestion = new Wrongquestion();
            wrongquestion.setQuestionId(question.getId());
            wrongquestion.setStudentId(userId);
            wrongquestion.setWrongSubmitNum(3);
            boolean save = wrongquestionService.save(wrongquestion);
            if (!save){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标记错题失败");
            }
        }else {
            //表明此次提交不是第三次错误提交，不需要任何操作
        }
    }

    @Override
    public Integer getUserWrongSubmitNumByQuestionId(long questionId, long userId) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.eq("userId", userId);
        queryWrapper.notLike("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public Integer getTotalWrongSubmitNumByTeacherId(long questionId, long teacherId) {
        //通过teacherId获取旗下所有学生的id
        List<Long> studentIds = tasService.getStudentIdsByTeacherId(teacherId);
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.in("userId", studentIds);
        queryWrapper.notLike("judgeInfo", JudgeInfoMessageEnum.ACCEPTED.getValue());
        return Math.toIntExact(this.count(queryWrapper));
    }

}




