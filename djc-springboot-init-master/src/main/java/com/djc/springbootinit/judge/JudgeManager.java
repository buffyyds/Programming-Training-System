package com.djc.springbootinit.judge;

import com.djc.springbootinit.judge.codesandbox.model.JudgeInfo;
import com.djc.springbootinit.judge.strategy.DefaultJudgeStrategy;
import com.djc.springbootinit.judge.strategy.JavaLanguageJudgeStrategy;
import com.djc.springbootinit.judge.strategy.JudgeContext;
import com.djc.springbootinit.judge.strategy.JudgeStrategy;
import com.djc.springbootinit.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
