package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yiliaoxitong.dto.DepartmentQuery;
import com.example.yiliaoxitong.entity.Department;

public interface DepartmentService extends IService<Department> {

    IPage<Department> pageQuery(DepartmentQuery query);

    void create(Department department);

    void modify(Long id, Department department);

    void remove(Long id);
}
