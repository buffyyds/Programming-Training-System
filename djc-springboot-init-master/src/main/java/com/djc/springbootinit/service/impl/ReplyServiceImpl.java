package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.mapper.ReplyMapper;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.entity.Reply;
import com.djc.springbootinit.service.PostService;
import com.djc.springbootinit.service.ReplyService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author djc
* @description 针对表【reply】的数据库操作Service实现
* @createDate 2025-03-14 19:23:34
*/
@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply>
    implements ReplyService {
//    @Resource
//    private PostService postService;
//
//    @Override
//    public void addReply(long newPostId, Long replyId, Long questionId) {
//        Reply reply = new Reply();
//        reply.setPostId(newPostId);
//        reply.setReplyId(replyId);
//        reply.setQuestionId(questionId);
//        save(reply);
//    }
//
//    @Override
//    public void deleteReply(long id) {
//        //先获取post表中的对应id数据
//        Post post = postService.getById(id);
//        //删除reply表中的对应数据
//        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq(ObjectUtils.isNotEmpty(), "id", id);
//        remove()
//    }
}




