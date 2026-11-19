package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.RegistrationQuery;
import com.example.yiliaoxitong.dto.RegistrationRequest;
import com.example.yiliaoxitong.entity.Doctor;
import com.example.yiliaoxitong.entity.DoctorSchedule;
import com.example.yiliaoxitong.entity.Patient;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.exception.DuplicateRegistrationException;
import com.example.yiliaoxitong.exception.NoSlotAvailableException;
import com.example.yiliaoxitong.mapper.DoctorMapper;
import com.example.yiliaoxitong.mapper.DoctorScheduleMapper;
import com.example.yiliaoxitong.mapper.PatientMapper;
import com.example.yiliaoxitong.mapper.RegistrationMapper;
import com.example.yiliaoxitong.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 挂号服务实现。
 *
 * <p>事务边界：register() 与 cancel() 均标注 @Transactional(rollbackFor = Exception.class)。
 * 挂号流程为"校验 → 原子扣号源 → 写挂号单"，任一步失败，号源扣减与挂号单一起回滚，
 * 不会出现"号扣了、单没生成"的中间状态。</p>
 */
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration>
        implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final DoctorScheduleMapper scheduleMapper;

    public RegistrationServiceImpl(PatientMapper patientMapper, DoctorMapper doctorMapper,
                                   DoctorScheduleMapper scheduleMapper) {
        this.patientMapper = patientMapper;
        this.doctorMapper = doctorMapper;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Registration register(RegistrationRequest request) {
        // 患者登录挂号：强制使用登录态中的本人患者 ID，防止代挂他人
        var actor = com.example.yiliaoxitong.security.AuthContext.get();
        Long patientId = request.getPatientId();
        if (actor != null && "PATIENT".equals(actor.role())) {
            patientId = actor.patientId();
        }
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在，请先完成患者建档");
        }

        DoctorSchedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排班不存在，请重新选择就诊时段");
        }
        if (schedule.getStatus() == null || schedule.getStatus() != 1) {
            throw new BusinessException("该医生当前时段已停诊，请选择其他时段");
        }
        if (schedule.getBookedSlots() >= schedule.getTotalSlots()) {
            throw new NoSlotAvailableException("该医生 " + schedule.getScheduleDate() + " "
                    + schedule.getTimeSlot() + " 号源已满，请选择其他时段");
        }

        long duplicated = count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getPatientId, request.getPatientId())
                .eq(Registration::getDoctorId, schedule.getDoctorId())
                .eq(Registration::getVisitDate, schedule.getScheduleDate())
                .ne(Registration::getStatus, 2));
        if (duplicated > 0) {
            throw new DuplicateRegistrationException("该患者当天已挂过该医生的号，请勿重复挂号");
        }

        // 条件更新原子扣减号源：返回 0 说明并发下号源已被抢空
        int booked = scheduleMapper.bookSlot(schedule.getId());
        if (booked == 0) {
            throw new NoSlotAvailableException("号源已被抢完，请重新选择就诊时段");
        }

        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        Registration registration = new Registration();
        registration.setRegNo(nextRegNo(schedule));
        registration.setPatientId(patient.getId());
        registration.setDoctorId(schedule.getDoctorId());
        registration.setDepartmentId(doctor.getDepartmentId());
        registration.setScheduleId(schedule.getId());
        registration.setVisitDate(schedule.getScheduleDate());
        registration.setTimeSlot(schedule.getTimeSlot());
        registration.setRegFee(schedule.getRegFee());
        registration.setStatus(0);
        save(registration);
        log.info("挂号成功：regNo={}, patientId={}, scheduleId={}",
                registration.getRegNo(), patient.getId(), schedule.getId());
        return registration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Registration registration = detail(id);
        // 患者只能取消非当天的预约；管理员不受限
        var actor = com.example.yiliaoxitong.security.AuthContext.get();
        if (actor != null && "PATIENT".equals(actor.role())
                && java.time.LocalDate.now().equals(registration.getVisitDate())) {
            throw new BusinessException("当天就诊的号不能自行取消，请当天前来就诊");
        }
        if (registration.getStatus() == 2) {
            throw new BusinessException("该挂号已取消，无需重复操作");
        }
        if (registration.getStatus() == 1) {
            throw new BusinessException("该挂号已就诊，不能取消");
        }
        registration.setStatus(2);
        updateById(registration);
        if (registration.getScheduleId() != null) {
            scheduleMapper.releaseSlot(registration.getScheduleId());
        }
        log.info("取消挂号：regNo={}, scheduleId={}", registration.getRegNo(), registration.getScheduleId());
    }

    @Override
    public IPage<Registration> page(RegistrationQuery query) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<Registration>()
                .eq(query.getPatientId() != null, Registration::getPatientId, query.getPatientId())
                .eq(query.getDoctorId() != null, Registration::getDoctorId, query.getDoctorId())
                .eq(query.getVisitDate() != null, Registration::getVisitDate, query.getVisitDate())
                .eq(query.getStatus() != null, Registration::getStatus, query.getStatus())
                .orderByDesc(Registration::getCreateTime);
        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public Registration detail(Long id) {
        Registration registration = getById(id);
        if (registration == null) {
            throw new BusinessException("挂号记录不存在");
        }
        return registration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        Registration registration = detail(id);
        if (registration.getStatus() == 1) {
            throw new BusinessException("该挂号已完成就诊，请勿重复操作");
        }
        if (registration.getStatus() == 2) {
            throw new BusinessException("该挂号已取消，无法完成就诊");
        }
        registration.setStatus(1);
        updateById(registration);
        log.info("完成就诊：regNo={}", registration.getRegNo());
    }

    /** 挂号单号：R + 就诊日期 + 6 位序列，保证可读且基本唯一 */
    private String nextRegNo(DoctorSchedule schedule) {
        String date = schedule.getScheduleDate().toString().replace("-", "");
        long seq = Math.abs(System.nanoTime() % 1000000L);
        return "R" + date + String.format("%06d", seq);
    }
}
