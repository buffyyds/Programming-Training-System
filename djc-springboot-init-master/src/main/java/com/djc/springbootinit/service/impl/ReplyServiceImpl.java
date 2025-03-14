package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.djc.springbootinit.mapper.ReplyMapper;
import com.djc.springbootinit.model.entity.Reply;
import com.djc.springbootinit.service.ReplyService;
import org.springframework.stereotype.Service;

/**
* @author djc
* @description 针对表【reply】的数据库操作Service实现
* @createDate 2025-03-14 19:23:34
*/
@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply>
    implements ReplyService {

}




