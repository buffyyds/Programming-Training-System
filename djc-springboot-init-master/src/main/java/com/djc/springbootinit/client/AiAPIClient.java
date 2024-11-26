package com.djc.springbootinit.client;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.djc.springbootinit.common.BaseResponse;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;

import java.util.ArrayList;
import java.util.List;


public class AiAPIClient {
    private static final String baseUrl = "https://ark.cn-beijing.volces.com/api/v3/";
    private final String apiKey;
    private final String modelId;
    //预设prompt
    final String prompt = "你是一个专业的数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容,必须严格按照我给出的格式输出：\n" +
        "分析需求：\n" +
        "{数据分析的需求或者目标}\n" +
        "原始数据：\n" +
        "{csv格式的原始数据，用,作为分隔符}\n" +
        "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
        "【【【\n" +
        "{前端 Echarts V5 的 option 配置对象json代码(必须严格按照标准的json格式输出，不然会导致渲染异常)，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
        "【【【\n" +
        "{明确的数据分析结论、越详细越好，不要生成多余的注释，结论至少生成500字，这至少500字要求不要多余的内容，必须是实实在在的结论内容,可以结合市场环境、行业发展、数据分析结果等方面进行分析}";

    public AiAPIClient(String apiKey, String modelId) {
        this.apiKey = apiKey;
        this.modelId = modelId;
    }

    public List<ChatCompletionChoice> doChat(String message) {
        ArkService service = ArkService.builder().apiKey(apiKey).baseUrl(baseUrl).build();

        System.out.println("\n----- standard request -----");
        final List<ChatMessage> messages = new ArrayList<>();
        //角色设置
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(prompt).build();
        //数据分析
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(modelId)
                .messages(messages)
                .build();
        // TODO 将输出转换后返回
        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> System.out.println(choice.getMessage().getContent()));
        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
        // shutdown service
        service.shutdownExecutor();
        //TODO return response
        return choices;
    }
}
