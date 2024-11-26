package com.djc.springbootinit.manager;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.djc.springbootinit.client.AiAPIClient;
import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.exception.BusinessException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用于对接AI服务
 * TIME: 2024/10/30 17:01
 */
@Service
public class AiManager {
    @Resource
    private AiAPIClient aiAPIClient;

    /**
     * AI对话
     * TODO 改善返回值
     */
    public List<ChatCompletionChoice> doChat(String message) {
        List<ChatCompletionChoice> response = aiAPIClient.doChat(message);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应错误");
        }
        return response;

    }
}
