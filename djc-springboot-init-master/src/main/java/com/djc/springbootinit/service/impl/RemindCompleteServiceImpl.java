package com.djc.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.constant.CommonConstant;
import com.djc.springbootinit.model.dto.RemindComplete.RemindCompleteQueryRequest;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.entity.RemindComplete;
import com.djc.springbootinit.service.RemindCompleteService;
import com.djc.springbootinit.mapper.RemindCompleteMapper;
import com.djc.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author djc
* @description 针对表【remind_complete】的数据库操作Service实现
* @createDate 2025-04-12 15:18:49
*/
@Service
public class RemindCompleteServiceImpl extends ServiceImpl<RemindCompleteMapper, RemindComplete>
    implements RemindCompleteService{


    @Override
    public Wrapper<RemindComplete> getQueryWrapper(RemindCompleteQueryRequest remindCompleteQueryRequest) {
        QueryWrapper<RemindComplete> queryWrapper = new QueryWrapper<>();
        if (remindCompleteQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = remindCompleteQueryRequest.getSortField();
        String sortOrder = remindCompleteQueryRequest.getSortOrder();
        Long studentId = remindCompleteQueryRequest.getStudentId();
        queryWrapper.eq(ObjectUtils.isNotEmpty(studentId), "studentId", studentId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Long getUnread(Long id) {
        QueryWrapper<RemindComplete> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("studentId", id);
        queryWrapper.eq("isRead", 0);
        long remindCompletes = this.count(queryWrapper);
        if (remindCompletes == 0) {
            return 0L;
        }
        return remindCompletes;
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        // 查询数据是否存在
        RemindComplete remindComplete = this.getById(id);
        if (remindComplete == null) {
            return false;
        }
        // 标记为已读
        remindComplete.setIsRead(true);
        return this.updateById(remindComplete);
    }

}




