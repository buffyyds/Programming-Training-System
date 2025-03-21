package com.djc.djcojcodesandbox;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CodeSandboxFactory {
    private final Map<String, CodeSandbox> codeSandboxMap;

    public CodeSandboxFactory(
            JavaDockerCodeSandbox javaDockerCodeSandbox,
            CppDockerCodeSandbox cppDockerCodeSandbox,
            PythonDockerCodeSandbox pythonDockerCodeSandbox) {
        codeSandboxMap = new HashMap<>();
        codeSandboxMap.put("java", javaDockerCodeSandbox);
        codeSandboxMap.put("cpp", cppDockerCodeSandbox);
        codeSandboxMap.put("python", pythonDockerCodeSandbox);
    }

    public CodeSandbox getCodeSandbox(String language) {
        CodeSandbox codeSandbox = codeSandboxMap.get(language);
        if (codeSandbox == null) {
            throw new IllegalArgumentException("不支持的语言类型：" + language);
        }
        return codeSandbox;
    }
} 