package com.djc.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.entity.PostThumb;
import com.djc.springbootinit.model.entity.User;

/**
 * 帖子点赞服务
 *

 */
public interface SensitiveWordService{

    /**
     * 检查是否包含敏感词
     *
     * @param post 内容
     * @return 是否包含敏感词
     */
    boolean isContainsSensitiveWord(String post);

    /**
     * 敏感词脱敏
     */
    String desensitizeSensitiveWord(String post);
}
