package com.djc.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.mapper.ReplyMapper;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.entity.Reply;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.PostVO;
import com.djc.springbootinit.model.vo.ReplyVO;
import com.djc.springbootinit.service.PostService;
import com.djc.springbootinit.service.ReplyService;
import com.djc.springbootinit.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
* @author djc
* @description 获取评论的回复service（回复本质上还是评论，只是换了个名字叫Reply，但实体本质上还是Post）
* @createDate 2025-03-14 19:23:34
*/
@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Post>
    implements ReplyService {

    @Resource
    private UserService userService;

    @Override
    public List<Post> getReply(Long questionId, Long id) {
        //通过问题id和评论id查询该问题的该评论下的所有回复
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.eq("replyId", id);
        //按创建时间排序
        queryWrapper.orderByAsc("createTime");
        //获取该评论的回复
        List<Post> replyList = this.list(queryWrapper);
        if (ObjectUtils.isNotEmpty(replyList)) {
            return replyList;
        }
        return Collections.emptyList();
    }


    /**
     * 对象转包装类
     *
     * @param post
     * @return
     */
    public ReplyVO objToVo(Post post) {
        if (post == null) {
            return null;
        }
        ReplyVO replyVO = new ReplyVO();
        BeanUtils.copyProperties(post, replyVO);
        User user = userService.getById(post.getUserId());
        replyVO.setUser(userService.getUserVO(user));
        return replyVO;
    }
}




