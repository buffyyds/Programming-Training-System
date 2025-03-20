package com.djc.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.dto.post.PostQueryRequest;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.vo.PostVO;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务
 *

 */
public interface PostService extends IService<Post> {

//    /**
//     * 校验
//     *
//     * @param post
//     * @param add
//     */
//    void validPost(Post post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param postQueryRequest
     * @return
     */
    Page<Post> searchFromEs(PostQueryRequest postQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @param request
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);

    List<Long> searchAllReply(long id);

    //获取登录用户的点赞评论
    Map<Long, Boolean> searchThumbPost(List<Post> postList, long userId);

    /**
     * 获取评论在分页中的位置
     * @param questionId 问题ID
     * @param commentId 评论ID
     * @return 页码（从1开始）
     */
    long getCommentPagePosition(long questionId, long commentId);

    Long getUnread(Long id);
}
