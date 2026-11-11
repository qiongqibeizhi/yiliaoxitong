package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yiliaoxitong.dto.DoctorQuery;
import com.example.yiliaoxitong.entity.Doctor;

public interface DoctorService extends IService<Doctor> {

    IPage<Doctor> pageQuery(DoctorQuery query);

    void create(Doctor doctor);

    void modify(Long id, Doctor doctor);

    void remove(Long id);
}
