package com.djc.springbootinit.service;

import com.djc.springbootinit.model.entity.Tas;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djc.springbootinit.model.vo.StudentsVo;
import com.djc.springbootinit.model.vo.TeacherVo;

import java.util.List;

/**
* @author djc
* @description 针对表【tas】的数据库操作Service
* @createDate 2025-03-24 09:15:56
*/
public interface TasService extends IService<Tas> {

    List<StudentsVo> getStudentsByTeacherId(Long id);

    TeacherVo getTeacherByStudentId(Long id);

    List<Long> getStudentIdsByTeacherId(Long id);

    boolean setTAS(Long id);
}
