package com.djc.springbootinit.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ErrorCode;
import com.djc.springbootinit.common.ResultUtils;
import com.djc.springbootinit.exception.BusinessException;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.model.dto.RemindComplete.RemindCompleteAddRequest;
import com.djc.springbootinit.model.dto.RemindComplete.RemindCompleteQueryRequest;
import com.djc.springbootinit.model.entity.RemindComplete;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.RemindCompleteVO;
import com.djc.springbootinit.service.RemindCompleteService;
import com.djc.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 提醒完成接口
 *

 */
@RestController
@RequestMapping("/remind")
@Slf4j
public class RemindController {


    @Resource
    private UserService userService;
    @Resource
    private RemindCompleteService remindCompleteService;
    /**
     * 创建
     *
     * @param remindCompleteAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addRemind(@RequestBody RemindCompleteAddRequest remindCompleteAddRequest, HttpServletRequest request) {
        if (remindCompleteAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        RemindComplete remindComplete = new RemindComplete();
        BeanUtils.copyProperties(remindCompleteAddRequest, remindComplete);
        boolean result = remindCompleteService.save(remindComplete);
        return result ? ResultUtils.success(true) : ResultUtils.error(ErrorCode.OPERATION_ERROR);
    }
    /**
     * 分页获取列表（封装类）
     *
     * @param remindCompleteQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<RemindComplete>> listRemindCompleteByPage(@RequestBody RemindCompleteQueryRequest remindCompleteQueryRequest,
                                                                   HttpServletRequest request) {
        long current = remindCompleteQueryRequest.getCurrent();
        long size = remindCompleteQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        remindCompleteQueryRequest.setStudentId(loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<RemindComplete> remindCompletePage = remindCompleteService.page(new Page<>(current, size),
                remindCompleteService.getQueryWrapper(remindCompleteQueryRequest));
        return ResultUtils.success(remindCompletePage);
    }

    /**
     * 获取是否有未读消息
     */
    @GetMapping("/get/unread")
    public BaseResponse<Long> getUnread(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(remindCompleteService.getUnread(loginUser.getId()));
    }


    //标记消息为已读
    @PutMapping("/markAsRead/{id}")
    public BaseResponse<Boolean> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = remindCompleteService.markAsRead(id, loginUser.getId());
        return ResultUtils.success(result);
    }
}
