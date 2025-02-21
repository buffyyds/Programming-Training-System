package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.mapper.QuestionSubmitMapper;
import com.djc.springbootinit.model.entity.QuestionSubmit;
import com.djc.springbootinit.service.QuestionSubmitService;
import org.springframework.stereotype.Service;

/**
* @author djc
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2025-02-21 11:16:10
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService {

}




