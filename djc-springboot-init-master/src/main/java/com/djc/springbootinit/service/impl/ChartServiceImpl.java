package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.mapper.ChartMapper;
import com.djc.springbootinit.model.entity.Chart;
import com.djc.springbootinit.service.ChartService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




