package com.djc.springbootinit.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.exception.BusinessException;
import com.djc.springbootinit.judge.codesandbox.CodeSandbox;
import com.djc.springbootinit.judge.codesandbox.model.ExecuteCodeRequest;
import com.djc.springbootinit.judge.codesandbox.model.ExecuteCodeResponse;
import com.djc.springbootinit.judge.codesandbox.model.JudgeInfo;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
public class RemoteCodeSandbox implements CodeSandbox {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("调用远程代码沙箱");
        String url = "http://192.168.189.149:8090/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();

        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        if(responseStr.contains("编译错误")) {
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("编译错误");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
