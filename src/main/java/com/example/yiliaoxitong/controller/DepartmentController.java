package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.dto.DepartmentQuery;
import com.example.yiliaoxitong.entity.Department;
import com.example.yiliaoxitong.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 科室管理接口：分页查询、详情、新增、编辑、删除。
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public Result<IPage<Department>> page(DepartmentQuery query) {
        return Result.ok(departmentService.pageQuery(query));
    }

    @GetMapping("/{id}")
    public Result<Department> detail(@PathVariable Long id) {
        return Result.ok(departmentService.getById(id));
    }

    @PostMapping
    @RequireRole(Role.ADMIN)
    public Result<Void> create(@Valid @RequestBody Department department) {
        departmentService.create(department);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<Void> modify(@PathVariable Long id, @Valid @RequestBody Department department) {
        departmentService.modify(id, department);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole(Role.ADMIN)
    public Result<Void> remove(@PathVariable Long id) {
        departmentService.remove(id);
        return Result.ok();
    }
}
