package com.djc.djcojcodesandbox;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CppDockerCodeSandbox extends DockerCodeSandboxTemplate {

    @Override
    protected String getImage() {
        return "gcc:latest";
    }

    @Override
    protected String getFileExtension() {
        return "cpp";
    }

    @Override
    protected String getCompileCommand(File userCodeFile) {
        return String.format("g++ -o %s %s", 
            userCodeFile.getAbsolutePath().replace(".cpp", ""),
            userCodeFile.getAbsolutePath());
    }

    @Override
    protected String getRunCommand(String userCodeParentPath, String inputArgs) {
        return String.format("/app/Main %s", inputArgs);
    }
} 