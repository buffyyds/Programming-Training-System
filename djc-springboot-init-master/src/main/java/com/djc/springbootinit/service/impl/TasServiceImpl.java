package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.model.entity.Tas;
import com.djc.springbootinit.model.entity.User;
import com.djc.springbootinit.model.vo.StudentsVo;
import com.djc.springbootinit.model.vo.TeacherVo;
import com.djc.springbootinit.service.TasService;
import com.djc.springbootinit.mapper.TasMapper;
import com.djc.springbootinit.service.UserService;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.stereotype.Service;
import com.djc.springbootinit.common.ErrorCode;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author djc
* @description 针对表【tas】的数据库操作Service实现
* @createDate 2025-03-24 09:15:56
*/
@Service
public class TasServiceImpl extends ServiceImpl<TasMapper, Tas>
    implements TasService{

    @Resource
    private UserService userService;

    @Override
    public List<StudentsVo> getStudentsByTeacherId(Long id) {
        //通过教师id获取旗下所有学生信息
        List<Long> studentIds = getStudentIdsByTeacherId(id);
        if (studentIds.size() > 0) {
            //获取学生信息
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("id", studentIds);
            List<User> userList = userService.list(userQueryWrapper);
            return userList.stream().map(user -> {
                StudentsVo studentsVo = new StudentsVo();
                studentsVo.setId(user.getId());
                studentsVo.setUserName(user.getUserName());
                return studentsVo;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public TeacherVo getTeacherByStudentId(Long id) {
        //通过学生id获取对应教师信息（一个学生只能有一个教师）
        QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("studentId", id);
        queryWrapper.eq("isDelete", 0);
        Tas tas = getOne(queryWrapper);
        if (tas == null) {
            return null;
        }
        Long teacherId = tas.getTeacherId();
        User user = userService.getById(teacherId);
        TeacherVo teacherVo = new TeacherVo();
        teacherVo.setId(user.getId());
        teacherVo.setUserName(user.getUserName());
        return teacherVo;
    }

    @Override
    public List<Long> getStudentIdsByTeacherId(Long id) {
        //通过教师id获取旗下所有学生信息
        QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", id);
        queryWrapper.eq("isDelete", 0);
        List<Tas> tasList = list(queryWrapper);
        //获取学生id
        return tasList.stream().map(Tas::getStudentId).collect(Collectors.toList());
    }

    /**
     * 记录教师学生关系
     * @param id
     * @return
     */
    @Override
    public boolean setTAS(Long id) {
        //设置教师学生关系 id为学生id，先查询到学生的adminCode，再通过adminCode查询到教师id
        User user = userService.getById(id);
        String adminCode = user.getAdminCode();
        ThrowUtils.throwIf(adminCode == null, ErrorCode.OPERATION_ERROR);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("adminCode", adminCode);
        userQueryWrapper.eq("userRole", "admin");
        User teacher = userService.getOne(userQueryWrapper);
        ThrowUtils.throwIf(teacher == null, ErrorCode.PARAMS_ERROR);
        Tas tas = new Tas();
        tas.setTeacherId(teacher.getId());
        tas.setStudentId(id);
        return save(tas);
    }


}




