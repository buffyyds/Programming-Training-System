//package com.djc.springbootinit.controller;
//
//import com.djc.springbootinit.common.BaseResponse;
//import com.djc.springbootinit.common.DeleteRequest;
//import com.djc.springbootinit.common.ResultUtils;
//import com.djc.springbootinit.exception.BusinessException;
//import com.djc.springbootinit.model.dto.reply.ReplyAddRequest;
//import com.djc.springbootinit.model.entity.Post;
//import com.djc.springbootinit.model.entity.Reply;
//import com.djc.springbootinit.model.entity.User;
//import com.djc.springbootinit.service.ReplyService;
//import com.djc.springbootinit.service.UserService;
//import lombok.extern.slf4j.Slf4j;
//import com.djc.springbootinit.common.ErrorCode;
//import org.springframework.beans.BeanUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//
///**
// * 回复接口
// */
//@RestController
//@RequestMapping("/reply")
//@Slf4j
//public class ReplyController {
//
//    @Resource
//    private UserService userService;
//
//    @Resource
//    private ReplyService replyService;
//    // region 增删
//
//    /**
//     * 创建回复
//     *
//     * @param replyAddRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/add")
//    public BaseResponse<Long> addReply(@RequestBody ReplyAddRequest replyAddRequest, HttpServletRequest request) {
//        if (replyAddRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User user = userService.getLoginUser(request);
//        Reply reply = new Reply();
//        BeanUtils.copyProperties(replyAddRequest, reply);
//        post.setUserId(user.getId());
//        return ResultUtils.success(post.getId());
//    }
//
//    /**
//     * 删除回复
//     *
//     * @param deleteRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/delete")
//    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
//        if (deleteRequest == null || deleteRequest.getId() == null) {
//            throw new BusinessException(ErrorCode.PARAM_ERROR);
//        }
//        User user = userService.getUserByToken(request);
//        Post post = postService.getPostById(deleteRequest.getId());
//        if (post == null) {
//            throw new BusinessException(ErrorCode.POST_NOT_EXIST);
//        }
//        if (!post.getUserId().equals(user.getId())) {
//            throw new BusinessException(ErrorCode.NO_PERMISSION);
//        }
//        postService.deletePost(deleteRequest.getId());
//        return ResultUtils.success(true);
//    }
//
//
//    /**
//     * 查询
//     *
//     * @param postQueryRequest
//     * @return
//
//}
