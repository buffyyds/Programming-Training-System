package com.djc.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.djc.springbootinit.model.dto.RemindComplete.RemindCompleteQueryRequest;
import com.djc.springbootinit.model.entity.RemindComplete;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author djc
* @description 针对表【remind_complete】的数据库操作Service
* @createDate 2025-04-12 15:18:49
*/
public interface RemindCompleteService extends IService<RemindComplete> {

    Wrapper<RemindComplete> getQueryWrapper(RemindCompleteQueryRequest remindCompleteQueryRequest);

    Long getUnread(Long id);

    boolean markAsRead(Long id, Long userId);
}
