package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.dto.ScheduleQuery;
import com.example.yiliaoxitong.entity.DoctorSchedule;

/**
 * 医生排班服务：排班维护与号源查询。
 */
public interface DoctorScheduleService {

    IPage<DoctorSchedule> page(ScheduleQuery query);

    DoctorSchedule detail(Long id);

    DoctorSchedule create(DoctorSchedule schedule);

    DoctorSchedule update(Long id, DoctorSchedule schedule);

    void delete(Long id);
}
