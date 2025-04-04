package com.djc.djcojcodesandbox;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class JavaDockerCodeSandbox extends DockerCodeSandboxTemplate {

    @Override
    protected String getImage() {
        return "openjdk:8-alpine";
    }

    @Override
    protected String getFileExtension() {
        return "java";
    }

    @Override
    protected String getCompileCommand(File userCodeFile) {
        return String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
    }

    @Override
    protected String getRunCommand(String userCodeParentPath, String inputArgs) {
        return String.format("java -cp /app Main %s", inputArgs);
    }
}



