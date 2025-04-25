package com.djc.springbootinit.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveWordServiceTest {


    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Test
    void testDesensitizeSensitiveWord() {
        String post = "五星红旗，毛主席，我爱你！";
        String result = sensitiveWordService.desensitizeSensitiveWord(post);
        boolean containsSensitiveWord = sensitiveWordService.isContainsSensitiveWord(post);
        System.out.println("脱敏后的内容: " + result);
        System.out.println("是否包含敏感词: " + containsSensitiveWord);
    }
}
