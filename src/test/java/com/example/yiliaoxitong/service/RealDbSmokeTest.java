package com.example.yiliaoxitong.service;

import com.example.yiliaoxitong.dto.PrescriptionItemRequest;
import com.example.yiliaoxitong.dto.PrescriptionRequest;
import com.example.yiliaoxitong.dto.RegistrationRequest;
import com.example.yiliaoxitong.entity.DoctorSchedule;
import com.example.yiliaoxitong.entity.Prescription;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.mapper.DoctorScheduleMapper;
import com.example.yiliaoxitong.mapper.DrugMapper;
import com.example.yiliaoxitong.mapper.PrescriptionItemMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 真实 MySQL 冒烟测试（默认不执行，需要手动开启）：
 * <pre>
 * ./mvnw -o test -Dtest=RealDbSmokeTest -DrealDb=true \
 *   -Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
 *   -Dspring.datasource.url="jdbc:mysql://localhost:3306/yiliaoxitong?..." \
 *   -Dspring.datasource.username=root -Dspring.datasource.password=*** \
 *   -Dspring.sql.init.mode=never
 * </pre>
 * 测试方法带 @Transactional，结束后整体回滚，不会污染真实库数据。
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "realDb", matches = "true")
class RealDbSmokeTest {

    @Autowired
    private DoctorScheduleService scheduleService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private PrescriptionService prescriptionService;
    @Autowired
    private DoctorScheduleMapper scheduleMapper;
    @Autowired
    private DrugMapper drugMapper;
    @Autowired
    private PrescriptionItemMapper itemMapper;

    @Test
    @DisplayName("真实库：排班与药品表可读，挂号扣号源、开方扣库存均生效（测试后回滚）")
    @Transactional
    void realDatabaseShouldSupportRegisterAndPrescribe() {
        assertTrue(scheduleMapper.selectCount(null) > 0, "真实库应存在排班数据");
        assertTrue(drugMapper.selectCount(null) > 0, "真实库应存在药品数据");

        DoctorSchedule schedule = scheduleMapper.selectById(1L);
        int bookedBefore = schedule.getBookedSlots();

        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setPatientId(4L);
        registrationRequest.setScheduleId(1L);
        Registration registration = registrationService.register(registrationRequest);
        assertNotNull(registration.getId());
        assertEquals(bookedBefore + 1, scheduleMapper.selectById(1L).getBookedSlots());

        int stockBefore = drugMapper.selectById(1L).getStock();
        PrescriptionRequest prescriptionRequest = new PrescriptionRequest();
        prescriptionRequest.setRegistrationId(registration.getId());
        prescriptionRequest.setDiagnosis("冒烟测试诊断");
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setDrugId(1L);
        item.setQuantity(1);
        prescriptionRequest.setItems(List.of(item));

        Prescription prescription = prescriptionService.create(prescriptionRequest);
        assertNotNull(prescription.getId());
        assertEquals(stockBefore - 1, drugMapper.selectById(1L).getStock());
        assertEquals(1, itemMapper.selectList(null).stream()
                .filter(i -> i.getPrescriptionId().equals(prescription.getId())).count());
    }
}
