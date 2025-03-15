package com.djc.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.entity.Post;
import com.djc.springbootinit.model.vo.ReplyVO;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;


/**
* @author djc
* @description 针对表【reply】的数据库操作Service
* @createDate 2025-03-14 19:23:34
*/
public interface ReplyService extends IService<Post> {

    List<Post> getReply(Long questionId, Long id);

    ReplyVO objToVo(Post post);
}
