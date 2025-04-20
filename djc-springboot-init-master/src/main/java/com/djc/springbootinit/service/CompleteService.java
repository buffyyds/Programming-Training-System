package com.djc.springbootinit.service;

import com.djc.springbootinit.model.entity.Complete;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author djc
* @description 针对表【complete】的数据库操作Service
* @createDate 2025-04-20 10:03:32
*/
public interface CompleteService extends IService<Complete> {

    void completeQuestion(Long studentId, Long questionId);
}
