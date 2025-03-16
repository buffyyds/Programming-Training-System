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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public ReplyVO postObjToReplyVo(Post post) {
        if (post == null) {
            return null;
        }
        ReplyVO replyVO = new ReplyVO();
        BeanUtils.copyProperties(post, replyVO);
        User user = userService.getById(post.getUserId());
        replyVO.setUser(userService.getUserVO(user));
        return replyVO;
    }

    @Override
    public List<Post> getMyReply(Long id) {
        //通过用户id查询该用户的所有回复
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", id);
        //按创建时间排序
        queryWrapper.orderByAsc("createTime");
        //获取该用户的所有评论
        List<Post> replyList = this.list(queryWrapper);
        //获取到这些评论的id，通过id查询这些评论的回复
        List<Long> replyIds = replyList.stream().map(Post::getId).collect(Collectors.toList());
        List<Long> questionIds = replyList.stream().map(Post::getQuestionId).collect(Collectors.toList());
        //清空replyList，然后将所有回复添加到replyList中
        List<Post> replys = null;
        replyList.clear();
        for (int i = 0; i < replyIds.size(); i++) {
            replys = this.getReply(questionIds.get(i), replyIds.get(i));
            if (ObjectUtils.isNotEmpty(replys)) {
                //删掉replys中userId = id的值 删除的时候注意IndexOutOfBoundsException
                for (int j = 0; j < replys.size(); j++) {
                    if (replys.get(j).getUserId().equals(id)) {
                        replys.remove(j);
                        j--;
                    }
                }
            }
            if (ObjectUtils.isNotEmpty(replys)) {
                replyList.addAll(replys);
            }
        }
        if (ObjectUtils.isNotEmpty(replyList)) {
            return replyList;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        // 查询回复是否存在
        Post post = this.getById(id);
        if (post == null) {
            return false;
        }
        // 验证当前用户是否是原评论的作者
        Post originalPost = this.getById(post.getReplyId());
        if (originalPost == null || !originalPost.getUserId().equals(userId)) {
            return false;
        }
        // 标记为已读
        post.setIsRead(true);
        return this.updateById(post);
    }
}




