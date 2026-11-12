package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.ScheduleQuery;
import com.example.yiliaoxitong.entity.DoctorSchedule;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.DoctorMapper;
import com.example.yiliaoxitong.mapper.DoctorScheduleMapper;
import com.example.yiliaoxitong.mapper.RegistrationMapper;
import com.example.yiliaoxitong.service.DoctorScheduleService;
import org.springframework.stereotype.Service;

/**
 * 排班服务实现：维护号源，禁止删除仍有挂号记录的排班。
 */
@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule>
        implements DoctorScheduleService {

    private final DoctorMapper doctorMapper;
    private final RegistrationMapper registrationMapper;

    public DoctorScheduleServiceImpl(DoctorMapper doctorMapper, RegistrationMapper registrationMapper) {
        this.doctorMapper = doctorMapper;
        this.registrationMapper = registrationMapper;
    }

    @Override
    public IPage<DoctorSchedule> page(ScheduleQuery query) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<DoctorSchedule>()
                .eq(query.getDoctorId() != null, DoctorSchedule::getDoctorId, query.getDoctorId())
                .eq(query.getDate() != null, DoctorSchedule::getScheduleDate, query.getDate())
                .eq(query.getStatus() != null, DoctorSchedule::getStatus, query.getStatus())
                .orderByAsc(DoctorSchedule::getScheduleDate)
                .orderByAsc(DoctorSchedule::getTimeSlot);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public DoctorSchedule detail(Long id) {
        DoctorSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        return schedule;
    }

    @Override
    public DoctorSchedule create(DoctorSchedule schedule) {
        if (schedule.getDoctorId() == null || doctorMapper.selectById(schedule.getDoctorId()) == null) {
            throw new BusinessException("医生不存在，无法排班");
        }
        if (schedule.getTotalSlots() == null || schedule.getTotalSlots() < 0) {
            throw new BusinessException("号源总数不能为负数");
        }
        long exists = count(new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDoctorId, schedule.getDoctorId())
                .eq(DoctorSchedule::getScheduleDate, schedule.getScheduleDate())
                .eq(DoctorSchedule::getTimeSlot, schedule.getTimeSlot()));
        if (exists > 0) {
            throw new BusinessException("该医生当天该时段已排班，请勿重复排班");
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        if (schedule.getBookedSlots() == null) {
            schedule.setBookedSlots(0);
        }
        save(schedule);
        return schedule;
    }

    @Override
    public DoctorSchedule update(Long id, DoctorSchedule schedule) {
        DoctorSchedule current = detail(id);
        if (schedule.getTotalSlots() != null && schedule.getTotalSlots() < current.getBookedSlots()) {
            throw new BusinessException("号源总数不能小于已挂号数量 " + current.getBookedSlots());
        }
        if (schedule.getDoctorId() != null) {
            current.setDoctorId(schedule.getDoctorId());
        }
        if (schedule.getScheduleDate() != null) {
            current.setScheduleDate(schedule.getScheduleDate());
        }
        if (schedule.getTimeSlot() != null) {
            current.setTimeSlot(schedule.getTimeSlot());
        }
        if (schedule.getTotalSlots() != null) {
            current.setTotalSlots(schedule.getTotalSlots());
        }
        if (schedule.getRegFee() != null) {
            current.setRegFee(schedule.getRegFee());
        }
        if (schedule.getStatus() != null) {
            current.setStatus(schedule.getStatus());
        }
        updateById(current);
        return current;
    }

    @Override
    public void delete(Long id) {
        DoctorSchedule schedule = detail(id);
        long used = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getScheduleId, schedule.getId()));
        if (used > 0) {
            throw new BusinessException("该排班已有 " + used + " 条挂号记录，不能删除");
        }
        removeById(id);
    }
}
