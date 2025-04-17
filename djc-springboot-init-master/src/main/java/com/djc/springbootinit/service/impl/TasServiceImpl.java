package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.exception.ThrowUtils;
import com.djc.springbootinit.model.entity.Reservation;
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
import org.springframework.transaction.annotation.Transactional;

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
                studentsVo.setCreateTime(user.getCreateTime());
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
     * @param teacherId
     * @return
     */
    @Override
    public boolean doBind(Long teacherId,User loginUser) {
        //设置教师学生关系 id为教师id
        User teacher = userService.getById(teacherId);
        String adminCode = teacher.getAdminCode();
        ThrowUtils.throwIf(adminCode == null, ErrorCode.OPERATION_ERROR);
        loginUser.setAdminCode(adminCode);
        userService.updateById(loginUser);  //用户表记录学生adminCode
        Tas tas = new Tas();
        tas.setTeacherId(teacher.getId());
        tas.setStudentId(loginUser.getId());
        return save(tas);
    }

    @Override
    public boolean unDoBind(User loginUser) {
        QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("studentId", loginUser.getId());
        queryWrapper.eq("isDelete", 0);
        Tas tas = getOne(queryWrapper);
        if (tas != null) {
            UpdateWrapper<Tas> updateWrapperTas = new UpdateWrapper<>();
            updateWrapperTas
                    .eq("id", tas.getId())  // 指定更新条件
                    .set("isDelete", 1);       // 强制设置 studentId 为 null
            update(updateWrapperTas);
            //删除用户表中的adminCode
            UpdateWrapper<User> updateWrapperUser = new UpdateWrapper<>();
            updateWrapperUser
                    .eq("id", loginUser.getId())  // 指定更新条件
                    .set("adminCode", null);       // 强制设置 studentId 为 null
            userService.update(updateWrapperUser);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean kickStudent(Long teacherId, List<Long> studentIds) {
        //教师踢出学生
        //先删除user表中的adminCode
        UpdateWrapper<User> updateWrapperUser = new UpdateWrapper<>();
        updateWrapperUser
                .in("id", studentIds)  // 指定更新条件
                .set("adminCode", null);       // 强制设置 studentId 为 null
        boolean update = userService.update(updateWrapperUser);
        if (!update) {
            return false;
        }
        QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacherId);
        queryWrapper.eq("isDelete", 0);
        queryWrapper.in("studentId", studentIds);
        return this.remove(queryWrapper);
    }

}




