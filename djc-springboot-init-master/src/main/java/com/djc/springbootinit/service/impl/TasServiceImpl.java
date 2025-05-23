package com.djc.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djc.springbootinit.constant.UserConstant;
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
    public boolean kickStudent(Long teacherId, Long studentId) {
        //教师踢出学生
        //先删除user表中的adminCode
        UpdateWrapper<User> updateWrapperUser = new UpdateWrapper<>();
        updateWrapperUser
                .eq("id", studentId)  // 指定更新条件
                .set("adminCode", null);       // 强制设置 adminCode 为 null
        boolean update = userService.update(updateWrapperUser);
        if (!update) {
            return false;
        }
        QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacherId);
        queryWrapper.eq("studentId", studentId);
        return this.remove(queryWrapper);
    }

    @Override
    public List<TeacherVo> getTeacherList() {
        //获取所有教师信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userRole", "teacher");
        queryWrapper.eq("isDelete", 0);
        List<User> userList = userService.list(queryWrapper);
        //封装TeacherVo
        List<TeacherVo> teacherVoList = userList.stream().map(user -> {
            TeacherVo teacherVo = new TeacherVo();
            teacherVo.setId(user.getId());
            teacherVo.setUserName(user.getUserName());
            teacherVo.setUserPhone(user.getUserPhone());
            teacherVo.setCreateTime(user.getCreateTime());
            //获取旗下学生人数
            List<Long> studentIds = getStudentIdsByTeacherId(user.getId());
            teacherVo.setStudentCount(studentIds.size());
            return teacherVo;
        }).collect(Collectors.toList());
        return teacherVoList;
    }

    @Override
    public List<StudentsVo> getStudentList() {
        //获取所有学生信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userRole", "user");
        queryWrapper.eq("isDelete", 0);
        List<User> userList = userService.list(queryWrapper);
        //封装StudentsVo
        List<StudentsVo> studentsVoList = userList.stream().map(user -> {
            StudentsVo studentsVo = new StudentsVo();
            studentsVo.setId(user.getId());
            studentsVo.setUserName(user.getUserName());
            studentsVo.setCreateTime(user.getCreateTime());
            studentsVo.setUserPhone(user.getUserPhone());
            //获取对应教师信息
            TeacherVo teacherVo = getTeacherByStudentId(user.getId());
            if (teacherVo == null) {
                studentsVo.setTeacher(null);
            } else {
                studentsVo.setTeacher(userService.getUserVO(userService.getById(teacherVo.getId())));
            }
            return studentsVo;
        }).collect(Collectors.toList());
        return studentsVoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacherStudent(User deleteUser) {
        if (deleteUser.getUserRole().equals(UserConstant.TEACHER_ROLE)){
            //删除教师和学生的关系
            QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teacherId", deleteUser.getId());
            queryWrapper.eq("isDelete", 0);
            List<Tas> list = this.list(queryWrapper);
            //获取学生ids
            List<Long> studentIds = list.stream().map(Tas::getStudentId).collect(Collectors.toList());
            //同时更新用户表中的adminCode
            UpdateWrapper<User> updateWrapperUser = new UpdateWrapper<>();
            updateWrapperUser
                    .in("id", studentIds)  // 指定更新条件
                    .set("adminCode", null);       // 强制设置 studentId 为 null
            userService.update(updateWrapperUser);
            //删除tas表中的记录
            UpdateWrapper<Tas> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("teacherId", deleteUser.getId());
            updateWrapper.in("studentId", studentIds);
            updateWrapper.set("isDelete", 1);
            return this.update(updateWrapper);
        }else {
            //学生的话则只需要删除自己的记录即可
            QueryWrapper<Tas> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("studentId", deleteUser.getId());
            //同时更新用户表中的adminCode
            UpdateWrapper<User> updateWrapperUser = new UpdateWrapper<>();
            updateWrapperUser
                    .eq("id", deleteUser.getId())  // 指定更新条件
                    .set("adminCode", null);       // 强制设置 studentId 为 null
            userService.update(updateWrapperUser);
            return this.remove(queryWrapper);
        }
    }
}




