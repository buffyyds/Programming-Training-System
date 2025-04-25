package com.djc.springbootinit.service.impl;

import com.djc.springbootinit.service.SensitiveWordService;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {
    @Override
    public String desensitizeSensitiveWord(String post) {
        //默认使用*脱敏
        return SensitiveWordHelper.replace(post);
    }

    @Override
    public boolean isContainsSensitiveWord(String post) {
        return SensitiveWordHelper.contains(post);
    }

}
