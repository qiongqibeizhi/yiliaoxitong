package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.yiliaoxitong.dto.AdminAccountRequest;
import com.example.yiliaoxitong.dto.DoctorAccountRequest;
import com.example.yiliaoxitong.entity.Doctor;
import com.example.yiliaoxitong.entity.SysUser;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.DoctorMapper;
import com.example.yiliaoxitong.mapper.SysUserMapper;
import com.example.yiliaoxitong.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 账号管理实现。医院内部账号由管理员统一开设，不开放自助注册；
 * 医生开户同时创建医生档案，账号与档案一一绑定。
 */
@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final SysUserMapper userMapper;
    private final DoctorMapper doctorMapper;

    public AccountServiceImpl(SysUserMapper userMapper, DoctorMapper doctorMapper) {
        this.userMapper = userMapper;
        this.doctorMapper = doctorMapper;
    }

    @Override
    public List<SysUser> listAccounts(String role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByAsc(SysUser::getId);
        if (role != null && !role.isBlank()) {
            wrapper.eq(SysUser::getRole, role);
        }
        List<SysUser> users = userMapper.selectList(wrapper);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser createDoctorAccount(DoctorAccountRequest request) {
        checkUsername(request.getUsername());
        // 建医生档案
        Doctor doctor = new Doctor();
        doctor.setDoctorNo(nextDoctorNo());
        doctor.setName(request.getRealName());
        doctor.setGender(request.getGender());
        doctor.setDepartmentId(request.getDepartmentId());
        doctor.setTitle(request.getTitle());
        doctor.setPhone(request.getPhone());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setStatus(1);
        if (doctorMapper.insert(doctor) != 1) {
            throw new BusinessException("医生建档失败，请稍后重试");
        }
        SysUser user = newUser(request.getUsername(), request.getPassword(),
                "DOCTOR", request.getRealName());
        user.setDoctorId(doctor.getId());
        userMapper.insert(user);
        user.setPassword(null);
        log.info("管理员开设医生账号：username={}, doctorId={}", request.getUsername(), doctor.getId());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser createAdminAccount(AdminAccountRequest request) {
        checkUsername(request.getUsername());
        SysUser user = newUser(request.getUsername(), request.getPassword(),
                "ADMIN", request.getRealName());
        userMapper.insert(user);
        user.setPassword(null);
        log.info("管理员开设管理员账号：username={}", request.getUsername());
        return user;
    }

    private void checkUsername(String username) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (exists > 0) {
            throw new BusinessException("用户名已存在，请更换");
        }
    }

    private SysUser newUser(String username, String password, String role, String realName) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(AuthServiceImpl.sha256(password));
        user.setRole(role);
        user.setRealName(realName);
        user.setStatus(1);
        return user;
    }

    private String nextDoctorNo() {
        return "D" + String.format("%06d", doctorMapper.selectCount(null) + 1);
    }
}
