package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.PatientQuery;
import com.example.yiliaoxitong.entity.Patient;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.mapper.PatientMapper;
import com.example.yiliaoxitong.mapper.RegistrationMapper;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.service.PatientService;
import com.example.yiliaoxitong.util.IdCardUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final RegistrationMapper registrationMapper;

    public PatientServiceImpl(RegistrationMapper registrationMapper) {
        this.registrationMapper = registrationMapper;
    }

    @Override
    public IPage<Patient> pageQuery(PatientQuery query) {
        Page<Patient> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Patient> wrapper = Wrappers.<Patient>lambdaQuery()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Patient::getName, query.getKeyword())
                        .or()
                        .like(Patient::getPatientNo, query.getKeyword())
                        .or()
                        .like(Patient::getIdCard, query.getKeyword()))
                .eq(StringUtils.hasText(query.getPhone()), Patient::getPhone, query.getPhone())
                .like(StringUtils.hasText(query.getIdCard()), Patient::getIdCard, query.getIdCard())
                .eq(StringUtils.hasText(query.getGender()), Patient::getGender, query.getGender())
                .orderByAsc(Patient::getId);
        // 医生只能看到挂过自己号的患者；管理员可见全部患者
        List<Long> ownPatientIds = ownPatientIdsOfCurrentDoctor();
        if (ownPatientIds != null) {
            if (ownPatientIds.isEmpty()) {
                return page;
            }
            wrapper.in(Patient::getId, ownPatientIds);
        }
        return this.page(page, wrapper);
    }

    /**
     * 当前登录医生接诊过的患者 ID 列表；非医生角色返回 null（不做过滤）。
     */
    private List<Long> ownPatientIdsOfCurrentDoctor() {
        var user = AuthContext.get();
        if (user == null || !"DOCTOR".equals(user.role()) || user.doctorId() == null) {
            return null;
        }
        return registrationMapper.selectList(Wrappers.<Registration>lambdaQuery()
                        .eq(Registration::getDoctorId, user.doctorId())
                        .select(Registration::getPatientId))
                .stream().map(Registration::getPatientId).distinct().toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Patient patient) {
        validateAndFill(patient);
        this.save(patient);
        log.info("新增患者成功，患者编号={}，身份证={}",
                patient.getPatientNo(), IdCardUtils.mask(patient.getIdCard()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modify(Long id, Patient patient) {
        if (this.getById(id) == null) {
            throw new BusinessException("患者不存在，ID=" + id);
        }
        validateAndFill(patient);
        patient.setId(id);
        this.updateById(patient);
        log.info("修改患者成功，患者编号={}，身份证={}",
                patient.getPatientNo(), IdCardUtils.mask(patient.getIdCard()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException("患者不存在，ID=" + id);
        }
        long regCount = registrationMapper.selectCount(
                Wrappers.<Registration>lambdaQuery().eq(Registration::getPatientId, id));
        if (regCount > 0) {
            throw new BusinessException("该患者存在 " + regCount + " 条挂号记录，不能删除");
        }
        this.removeById(id);
    }

    /**
     * 校验身份证校验位与出生日期，并在未填写出生日期时从身份证号解析补齐。
     */
    private void validateAndFill(Patient patient) {
        if (!IdCardUtils.isValid(patient.getIdCard())) {
            throw new BusinessException("身份证号校验失败，请检查号码是否正确");
        }
        if (patient.getBirthDate() == null) {
            String birthday = patient.getIdCard().substring(6, 14);
            patient.setBirthDate(LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyyMMdd")));
        }
    }
}
