package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.constant.CommonConstant;
import com.djc.springbootinit.mapper.PostFavourMapper;
import com.djc.springbootinit.mapper.PostMapper;
import com.djc.springbootinit.mapper.PostThumbMapper;
import com.djc.springbootinit.model.dto.post.PostEsDTO;
import com.djc.springbootinit.model.dto.post.PostQueryRequest;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.entity.PostFavour;
import com.djc.springbootinit.model.entity.PostThumb;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.PostVO;
import com.djc.springbootinit.model.vo.ReplyVO;
import com.djc.springbootinit.model.vo.UserVO;
import com.djc.springbootinit.service.PostService;
import com.djc.springbootinit.service.ReplyService;
import com.djc.springbootinit.service.UserService;
import com.djc.springbootinit.utils.SqlUtils;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

/**
 * 帖子服务实现
 *

 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private PostMapper postMapper;

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long questionId = postQueryRequest.getQuestionId();
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 获取非回复类型的评论，在下层获取VO封装类时再获取对应评论的回复评论。
        queryWrapper.eq("isReply", 0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_DESC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String content = postQueryRequest.getContent();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        User loginUser = userService.getLoginUser(request);
        if (loginUser != null) {
//            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());

            // 获取点赞
//            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
//            postThumbQueryWrapper.in("postId", postIdSet);
//            postThumbQueryWrapper.eq("userId", loginUser.getId());
//            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
//            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            postIdHasThumbMap = searchThumbPost(postList, loginUser.getId());
        }
        // 填充信息
        Map<Long, Boolean> finalPostIdHasThumbMap = postIdHasThumbMap;
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(finalPostIdHasThumbMap.getOrDefault(post.getId(), false));
            //获取回复评论列表
            List<Post> reply = replyService.getReply(post.getQuestionId(), post.getId());
            if (CollUtil.isNotEmpty(reply)) {
                //转成VO
                List<ReplyVO> replyVOList = reply.stream().map(replyService::postObjToReplyVo).collect(Collectors.toList());
                //获取登录用户的点赞评论
                Map<Long, Boolean> longBooleanMap = searchThumbPost(reply, loginUser.getId());
                replyVOList.forEach(replyVO -> {
                    replyVO.setHasThumb(longBooleanMap.get(replyVO.getId()));
                });
                postVO.setReply(replyVOList);
            }
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public List<Long> searchAllReply(long id) {
        //查询所有replyId为id的评论，返回id列表
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("replyId", id);
        List<Post> postList = baseMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(postList)) {
            return postList.stream().map(Post::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    //获取登录用户的点赞评论
    @Override
    public Map<Long, Boolean> searchThumbPost(List<Post> postList, long userId) {
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        // 获取点赞
        Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
        QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
        postThumbQueryWrapper.in(ObjectUtils.isNotEmpty(postIdSet),"postId", postIdSet);
        postThumbQueryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId", userId);
        List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
        postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
        return postIdHasThumbMap;
    }

    @Override
    public long getCommentPagePosition(long questionId, long commentId) {
        // 获取非回复类型的评论的总数
        long total = postMapper.selectCount(
            new QueryWrapper<Post>()
                .eq("questionId", questionId)
                .eq("isReply", 0)
                .eq("isDelete", 0)
        );
        
        // 获取评论在列表中的位置（从0开始）
        long position = postMapper.selectCount(
            new QueryWrapper<Post>()
                .eq("questionId", questionId)
                .eq("isDelete", 0)
                .lt("createTime", postMapper.selectById(commentId).getCreateTime())
        );
        
        // 计算页码（从1开始）
        return (position / 10) + 1;
    }

}




