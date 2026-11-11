package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.DoctorQuery;
import com.example.yiliaoxitong.entity.Doctor;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.DepartmentMapper;
import com.example.yiliaoxitong.mapper.DoctorMapper;
import com.example.yiliaoxitong.mapper.RegistrationMapper;
import com.example.yiliaoxitong.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    private final DepartmentMapper departmentMapper;

    private final RegistrationMapper registrationMapper;

    public DoctorServiceImpl(DepartmentMapper departmentMapper, RegistrationMapper registrationMapper) {
        this.departmentMapper = departmentMapper;
        this.registrationMapper = registrationMapper;
    }

    @Override
    public IPage<Doctor> pageQuery(DoctorQuery query) {
        Page<Doctor> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Doctor> wrapper = Wrappers.<Doctor>lambdaQuery()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Doctor::getName, query.getKeyword())
                        .or()
                        .like(Doctor::getDoctorNo, query.getKeyword()))
                .eq(query.getDepartmentId() != null, Doctor::getDepartmentId, query.getDepartmentId())
                .eq(StringUtils.hasText(query.getTitle()), Doctor::getTitle, query.getTitle())
                .eq(query.getStatus() != null, Doctor::getStatus, query.getStatus())
                .orderByAsc(Doctor::getId);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Doctor doctor) {
        if (departmentMapper.selectById(doctor.getDepartmentId()) == null) {
            throw new BusinessException("所属科室不存在，无法新增医生");
        }
        if (doctor.getStatus() == null) {
            doctor.setStatus(1);
        }
        this.save(doctor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(Long id, Doctor doctor) {
        if (this.getById(id) == null) {
            throw new BusinessException("医生不存在，ID=" + id);
        }
        if (departmentMapper.selectById(doctor.getDepartmentId()) == null) {
            throw new BusinessException("所属科室不存在，无法修改医生信息");
        }
        doctor.setId(id);
        this.updateById(doctor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("医生不存在，ID=" + id);
        }
        long regCount = registrationMapper.selectCount(
                Wrappers.<Registration>lambdaQuery().eq(Registration::getDoctorId, id));
        if (regCount > 0) {
            throw new BusinessException("该医生存在 " + regCount + " 条挂号记录，不能删除");
        }
        this.removeById(id);
    }
}
