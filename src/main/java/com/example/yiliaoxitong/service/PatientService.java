package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yiliaoxitong.dto.PatientQuery;
import com.example.yiliaoxitong.entity.Patient;

public interface PatientService extends IService<Patient> {

    IPage<Patient> pageQuery(PatientQuery query);

    void create(Patient patient);

    void modify(Long id, Patient patient);

    void remove(Long id);
}
