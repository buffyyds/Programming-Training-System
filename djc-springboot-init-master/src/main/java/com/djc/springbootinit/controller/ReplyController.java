package com.djc.springbootinit.controller;


import com.djc.springbootinit.common.BaseResponse;
import com.djc.springbootinit.common.ResultUtils;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.entity.Reply;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.ReplyVO;
import com.djc.springbootinit.service.PostService;
import com.djc.springbootinit.service.ReplyService;
import com.djc.springbootinit.service.UserService;
import com.djc.springbootinit.service.impl.PostServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reply")
@Slf4j
public class ReplyController {

    @Resource
    private ReplyService replyService;

    @Resource
    private UserService userService;
    @Autowired
    private PostService postService;

    //标记消息为已读
    @PutMapping("/markAsRead/{id}")
    public BaseResponse<Boolean> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = replyService.markAsRead(id, loginUser.getId());
        return ResultUtils.success(result);
    }

    //获取我的所有回复
    @GetMapping("/getMyReply")
    public BaseResponse<List<ReplyVO>> getMyReply(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<Post> replyList = replyService.getMyReply(loginUser.getId());
        Map<Long, Boolean> longBooleanMap = postService.searchThumbPost(replyList, loginUser.getId());
        List<ReplyVO> replyVOList = replyList.stream().map(replyService::postObjToReplyVo).collect(Collectors.toList());
        replyVOList.forEach(replyVO -> {
            replyVO.setHasThumb(longBooleanMap.get(replyVO.getId()));
        });
        return ResultUtils.success(replyVOList);
    }

}
