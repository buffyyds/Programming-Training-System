package com.djc.springbootinit.judge;

import com.djc.springbootinit.judge.codesandbox.model.JudgeInfo;
import com.djc.springbootinit.judge.strategy.*;
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
        switch (language){
            case "java":
                judgeStrategy = new JavaLanguageJudgeStrategy();
                break;
            case "cpp":
                judgeStrategy = new CppLanguageJudgeStrategy();
                break;
            case "python":
                judgeStrategy = new PythonLanguageJudgeStrategy();
                break;
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
