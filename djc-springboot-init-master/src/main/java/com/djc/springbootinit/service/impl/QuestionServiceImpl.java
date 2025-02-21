package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.mapper.QuestionMapper;
import com.djc.springbootinit.model.entity.Question;
import com.djc.springbootinit.service.QuestionService;
import org.springframework.stereotype.Service;

/**
* @author djc
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-02-21 11:14:41
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

}




