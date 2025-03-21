package com.djc.djcojcodesandbox;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PythonDockerCodeSandbox extends DockerCodeSandboxTemplate {

    @Override
    protected String getImage() {
        return "python:latest";
    }

    @Override
    protected String getFileExtension() {
        return "py";
    }

    @Override
    protected String getCompileCommand(File userCodeFile) {
        // Python不需要编译
        return "echo 'Python不需要编译'";
    }

    @Override
    protected String getRunCommand(String userCodeParentPath, String inputArgs) {
        return String.format("python /app/Main.py %s", inputArgs);
    }
} 