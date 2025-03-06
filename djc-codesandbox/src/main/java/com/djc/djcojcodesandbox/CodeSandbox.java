package com.djc.djcojcodesandbox;


import com.djc.djcojcodesandbox.model.ExecuteCodeRequest;
import com.djc.djcojcodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
