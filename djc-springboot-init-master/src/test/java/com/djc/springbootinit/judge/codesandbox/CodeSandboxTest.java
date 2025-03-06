package com.djc.springbootinit.judge.codesandbox;


import com.djc.springbootinit.judge.codesandbox.CodeSandbox;
import com.djc.springbootinit.judge.codesandbox.CodeSandboxFactory;
import com.djc.springbootinit.judge.codesandbox.model.ExecuteCodeRequest;
import com.djc.springbootinit.judge.codesandbox.model.ExecuteCodeResponse;
import com.djc.springbootinit.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

@ContextConfiguration
@SpringBootTest
class CodeSandboxTest {

    @Value("${codesandbox.type:example}")  //读取配置文件中的codesandbox.type的值，如果没有则默认为example
    private String type;

    @Test
    void executeCode() {
        //通过new的方式写死了，如果后面项目要改用其他沙箱，可能会需要改很多地方，所以可以使用工厂模式
        //CodeSandbox codeSandbox = new RemoteCodeSandbox();
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main() { }";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    /**
     * 代理测试
     */
    @Test
    void executeCodeByProxy() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"结果:\" + (a + b));\n" +
                "    }\n" +
                "}";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }


//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNext()) {
//            String type = scanner.next();
//            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
//            String code = "int main() { }";
//            String language = QuestionSubmitLanguageEnum.JAVA.getValue();
//            List<String> inputList = Arrays.asList("1 2", "3 4");
//            ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
//                    .code(code)
//                    .language(language)
//                    .inputList(inputList)
//                    .build();
//            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
//        }
//    }
}