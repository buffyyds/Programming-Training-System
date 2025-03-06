package com.djc.springbootinit.judge;

import cn.hutool.json.JSONUtil;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.exception.BusinessException;
import com.djc.springbootinit.judge.codesandbox.CodeSandbox;
import com.djc.springbootinit.judge.codesandbox.CodeSandboxFactory;
import com.djc.springbootinit.judge.codesandbox.CodeSandboxProxy;
import com.djc.springbootinit.judge.codesandbox.model.ExecuteCodeRequest;
import com.djc.springbootinit.judge.codesandbox.model.ExecuteCodeResponse;
import com.djc.springbootinit.judge.codesandbox.model.JudgeInfo;
import com.djc.springbootinit.judge.strategy.JudgeContext;
import com.djc.springbootinit.model.dto.question.JudgeCase;
import com.djc.springbootinit.model.entity.Question;
import com.djc.springbootinit.model.entity.QuestionSubmit;
import com.djc.springbootinit.model.enums.QuestionSubmitLanguageEnum;
import com.djc.springbootinit.model.enums.QuestionSubmitStatusEnum;
import com.djc.springbootinit.service.QuestionService;
import com.djc.springbootinit.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题服务实现类
 */
@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private QuestionService questionService;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")  //读取配置文件中的codesandbox.type的值，如果没有则默认为example
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 传入提交信息id，，获取对应题目，提交信息（代码，编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在！");
        }
        Long questionId = questionSubmit.getQuestionId();
        // 获取题目信息
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在！");
        }
        // 2. 判断题目是否正在判题中
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中！");
        }
        // 更新提交信息状态为判题中
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新信息状态失败！");
        }
        // 3. 调用代码沙箱，执行代码
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        // 4. 根据结果判断是否通过
        List<String> outputList = executeCodeResponse.getOutputList();
        /**
         * 可能根据不同的语言，需要对输出进行处理，会导致写很多的if-else，难以维护
         * 所以这里用策略模式进行优化
         */
        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}
