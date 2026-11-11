package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.DepartmentQuery;
import com.example.yiliaoxitong.entity.Department;
import com.example.yiliaoxitong.entity.Doctor;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.DepartmentMapper;
import com.example.yiliaoxitong.mapper.DoctorMapper;
import com.example.yiliaoxitong.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department>
        implements DepartmentService {

    private final DoctorMapper doctorMapper;

    public DepartmentServiceImpl(DoctorMapper doctorMapper) {
        this.doctorMapper = doctorMapper;
    }

    @Override
    public IPage<Department> pageQuery(DepartmentQuery query) {
        Page<Department> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Department> wrapper = Wrappers.<Department>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), Department::getDeptName, query.getName())
                .eq(query.getStatus() != null, Department::getStatus, query.getStatus())
                .orderByAsc(Department::getId);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Department department) {
        if (this.count(Wrappers.<Department>lambdaQuery().eq(Department::getDeptCode, department.getDeptCode())) > 0) {
            throw new BusinessException("科室编码已存在：" + department.getDeptCode());
        }
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        this.save(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(Long id, Department department) {
        Department exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException("科室不存在，ID=" + id);
        }
        if (this.count(Wrappers.<Department>lambdaQuery()
                .eq(Department::getDeptCode, department.getDeptCode())
                .ne(Department::getId, id)) > 0) {
            throw new BusinessException("科室编码已被其他科室占用：" + department.getDeptCode());
        }
        department.setId(id);
        this.updateById(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("科室不存在，ID=" + id);
        }
        long doctorCount = doctorMapper.selectCount(
                Wrappers.<Doctor>lambdaQuery().eq(Doctor::getDepartmentId, id));
        if (doctorCount > 0) {
            throw new BusinessException("该科室下仍有 " + doctorCount + " 名医生，不能删除");
        }
        this.removeById(id);
    }
}
