package com.example.yiliaoxitong.service;

import com.example.yiliaoxitong.dto.PrescriptionItemRequest;
import com.example.yiliaoxitong.dto.PrescriptionRequest;
import com.example.yiliaoxitong.dto.RegistrationRequest;
import com.example.yiliaoxitong.entity.DoctorSchedule;
import com.example.yiliaoxitong.entity.Drug;
import com.example.yiliaoxitong.entity.Prescription;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.exception.DuplicateRegistrationException;
import com.example.yiliaoxitong.exception.NoSlotAvailableException;
import com.example.yiliaoxitong.exception.StockNotEnoughException;
import com.example.yiliaoxitong.mapper.DoctorScheduleMapper;
import com.example.yiliaoxitong.mapper.DrugMapper;
import com.example.yiliaoxitong.mapper.PrescriptionItemMapper;
import com.example.yiliaoxitong.mapper.PrescriptionMapper;
import com.example.yiliaoxitong.mapper.RegistrationMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 实验三核心业务测试：挂号事务、号源扣减、重复挂号、取消释放号源、
 * 处方库存扣减与异常回滚。
 *
 * <p>本类不使用 @Transactional 回滚测试数据，因为要验证的是业务方法自身的事务提交与回滚；
 * 各用例通过不同的排班日期相互隔离。</p>
 */
@SpringBootTest
class Experiment3TxTest {

    @Autowired
    private DoctorScheduleService scheduleService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private PrescriptionService prescriptionService;
    @Autowired
    private DoctorScheduleMapper scheduleMapper;
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private DrugMapper drugMapper;
    @Autowired
    private PrescriptionMapper prescriptionMapper;
    @Autowired
    private PrescriptionItemMapper itemMapper;

    private DoctorSchedule newSchedule(String date, String slot, int total) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(1L);
        schedule.setScheduleDate(LocalDate.parse(date));
        schedule.setTimeSlot(slot);
        schedule.setTotalSlots(total);
        schedule.setRegFee(new BigDecimal("15.00"));
        schedule.setStatus(1);
        return scheduleService.create(schedule);
    }

    private RegistrationRequest request(Long patientId, Long scheduleId) {
        RegistrationRequest request = new RegistrationRequest();
        request.setPatientId(patientId);
        request.setScheduleId(scheduleId);
        return request;
    }

    @Test
    @DisplayName("挂号成功：号源已挂数量 +1，挂号单状态为待就诊")
    void registerShouldBookOneSlot() {
        DoctorSchedule schedule = newSchedule("2026-12-01", "上午", 8);
        Registration registration = registrationService.register(request(1L, schedule.getId()));

        assertNotNull(registration.getId());
        assertEquals(0, registration.getStatus());
        assertEquals(schedule.getId(), registration.getScheduleId());
        assertEquals(1, scheduleMapper.selectById(schedule.getId()).getBookedSlots());
    }

    @Test
    @DisplayName("重复挂号：同一患者同一医生同一天再次挂号被拦截，号源不再增加")
    void duplicateRegistrationShouldBeRejected() {
        DoctorSchedule schedule = newSchedule("2026-12-02", "上午", 8);
        registrationService.register(request(1L, schedule.getId()));

        assertThrows(DuplicateRegistrationException.class,
                () -> registrationService.register(request(1L, schedule.getId())));
        assertEquals(1, scheduleMapper.selectById(schedule.getId()).getBookedSlots());
    }

    @Test
    @DisplayName("号源已满：容量为 1 的排班第二个患者挂号被拒绝")
    void fullScheduleShouldRejectSecondPatient() {
        DoctorSchedule schedule = newSchedule("2026-12-03", "下午", 1);
        registrationService.register(request(1L, schedule.getId()));

        assertThrows(NoSlotAvailableException.class,
                () -> registrationService.register(request(2L, schedule.getId())));
        assertEquals(1, scheduleMapper.selectById(schedule.getId()).getBookedSlots());
    }

    @Test
    @DisplayName("取消挂号：挂号状态置为已取消，号源被释放")
    void cancelShouldReleaseSlot() {
        DoctorSchedule schedule = newSchedule("2026-12-04", "上午", 5);
        Registration registration = registrationService.register(request(1L, schedule.getId()));

        registrationService.cancel(registration.getId());

        assertEquals(2, registrationMapper.selectById(registration.getId()).getStatus());
        assertEquals(0, scheduleMapper.selectById(schedule.getId()).getBookedSlots());
    }

    @Test
    @DisplayName("开处方：库存按处方数量扣减，处方与明细写入成功")
    void prescriptionShouldDeductStock() {
        DoctorSchedule schedule = newSchedule("2026-12-05", "上午", 5);
        Registration registration = registrationService.register(request(2L, schedule.getId()));
        int stockBefore = drugMapper.selectById(1L).getStock();

        PrescriptionRequest request = new PrescriptionRequest();
        request.setRegistrationId(registration.getId());
        request.setDiagnosis("急性扁桃体炎");
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setDrugId(1L);
        item.setQuantity(3);
        item.setDosage("一次2粒，一日3次");
        request.setItems(List.of(item));

        Prescription prescription = prescriptionService.create(request);

        assertNotNull(prescription.getId());
        assertEquals(stockBefore - 3, drugMapper.selectById(1L).getStock());
        assertEquals(1, itemMapper.selectList(null).stream()
                .filter(i -> i.getPrescriptionId().equals(prescription.getId())).count());
    }

    @Test
    @DisplayName("库存不足：第二味药库存不够时整张处方回滚，第一味药的库存不被扣减")
    void stockShortageShouldRollbackWholePrescription() {
        DoctorSchedule schedule = newSchedule("2026-12-06", "上午", 5);
        Registration registration = registrationService.register(request(1L, schedule.getId()));
        int drug1Before = drugMapper.selectById(1L).getStock();
        int drug2Before = drugMapper.selectById(2L).getStock();
        long prescriptionsBefore = prescriptionMapper.selectCount(null);

        PrescriptionRequest request = new PrescriptionRequest();
        request.setRegistrationId(registration.getId());
        request.setDiagnosis("感冒伴发热");
        PrescriptionItemRequest first = new PrescriptionItemRequest();
        first.setDrugId(1L);
        first.setQuantity(2);
        PrescriptionItemRequest second = new PrescriptionItemRequest();
        second.setDrugId(2L);
        second.setQuantity(drug2Before + 10);
        request.setItems(List.of(first, second));

        assertThrows(StockNotEnoughException.class, () -> prescriptionService.create(request));

        // 整个方法回滚：库存完全不变，处方记录与明细都没有落库
        assertEquals(drug1Before, drugMapper.selectById(1L).getStock());
        assertEquals(drug2Before, drugMapper.selectById(2L).getStock());
        assertEquals(prescriptionsBefore, prescriptionMapper.selectCount(null));
        assertEquals(0, prescriptionMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getRegistrationId, registration.getId())));
    }
}
