package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.PrescriptionItemRequest;
import com.example.yiliaoxitong.dto.PrescriptionRequest;
import com.example.yiliaoxitong.entity.Drug;
import com.example.yiliaoxitong.entity.Doctor;
import com.example.yiliaoxitong.entity.Prescription;
import com.example.yiliaoxitong.entity.PrescriptionItem;
import com.example.yiliaoxitong.entity.Registration;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.exception.StockNotEnoughException;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.mapper.DrugMapper;
import com.example.yiliaoxitong.mapper.DoctorMapper;
import com.example.yiliaoxitong.mapper.PrescriptionItemMapper;
import com.example.yiliaoxitong.mapper.PrescriptionMapper;
import com.example.yiliaoxitong.mapper.RegistrationMapper;
import com.example.yiliaoxitong.service.PrescriptionService;
import com.example.yiliaoxitong.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方服务实现。
 *
 * <p>事务边界：create() 标注 @Transactional(rollbackFor = Exception.class)。
 * 一次开方包含"写处方主表 → 逐条扣库存 → 写处方明细"，任一药品库存不足即抛异常，
 * 已扣的库存与已写入的处方、明细全部回滚，保证三者一致。</p>
 */
@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    private final RegistrationMapper registrationMapper;
    private final PrescriptionItemMapper itemMapper;
    private final DrugMapper drugMapper;
    private final DoctorMapper doctorMapper;
    private final WalletService walletService;

    public PrescriptionServiceImpl(RegistrationMapper registrationMapper,
                                   PrescriptionItemMapper itemMapper, DrugMapper drugMapper,
                                   DoctorMapper doctorMapper, WalletService walletService) {
        this.registrationMapper = registrationMapper;
        this.itemMapper = itemMapper;
        this.drugMapper = drugMapper;
        this.doctorMapper = doctorMapper;
        this.walletService = walletService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prescription create(PrescriptionRequest request) {
        Registration registration = registrationMapper.selectById(request.getRegistrationId());
        if (registration == null) {
            throw new BusinessException("挂号记录不存在，无法开具处方");
        }
        if (registration.getStatus() == 2) {
            throw new BusinessException("该挂号已取消，不能开具处方");
        }
        // 医生只能给自己接诊的挂号开方；管理员不受限
        var actor = AuthContext.get();
        if (actor != null && "DOCTOR".equals(actor.role()) && actor.doctorId() != null
                && !actor.doctorId().equals(registration.getDoctorId())) {
            throw new BusinessException("只能为本人接诊的挂号开具处方");
        }
        Prescription existing = getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getRegistrationId, registration.getId())
                .last("LIMIT 1"));
        if (existing != null) {
            // 复诊修改：更新诊断与药品明细，回退原药品库存后按新明细扣减
            return updatePrescription(existing, request);
        }

        Prescription prescription = new Prescription();
        prescription.setPrescNo(nextPrescNo());
        prescription.setRegistrationId(registration.getId());
        prescription.setPatientId(registration.getPatientId());
        prescription.setDoctorId(registration.getDoctorId());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setPrescTime(LocalDateTime.now());
        // 未开药的纯诊断记录无需支付；开单后进入待支付，由患者凭余额支付
        prescription.setStatus(request.getItems() == null || request.getItems().isEmpty() ? 1 : 0);
        save(prescription);

        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            Drug drug = drugMapper.selectById(itemRequest.getDrugId());
            if (drug == null) {
                throw new BusinessException("药品不存在，无法开具");
            }
            if (drug.getStatus() == null || drug.getStatus() != 1) {
                throw new BusinessException("药品 " + drug.getDrugName() + " 已停用");
            }
            int deducted = drugMapper.deductStock(drug.getId(), itemRequest.getQuantity());
            if (deducted == 0) {
                throw new StockNotEnoughException("药品 " + drug.getDrugName() + " 库存不足，当前仅剩 "
                        + drug.getStock() + " " + drug.getUnit() + "，请调整数量或更换药品");
            }

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription.getId());
            item.setDrugId(drug.getId());
            item.setDrugName(drug.getDrugName());
            item.setSpec(drug.getSpec());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnit(drug.getUnit());
            item.setDosage(itemRequest.getDosage());
            item.setPrice(drug.getPrice());
            itemMapper.insert(item);
        }
        log.info("开具处方成功：prescNo={}, registrationId={}, items={}",
                prescription.getPrescNo(), registration.getId(), request.getItems().size());
        return prescription;
    }

    /** 复诊：更新已有诊断记录。已支付单不允许修改药品，仅允许修改诊断内容 */
    private Prescription updatePrescription(Prescription existing, PrescriptionRequest request) {
        if (existing.getStatus() != null && existing.getStatus() == 0) {
            // 待支付：整体替换明细，回退原库存
            for (PrescriptionItem item : itemMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>()
                    .eq(PrescriptionItem::getPrescriptionId, existing.getId()))) {
                drugMapper.restoreStock(item.getDrugId(), item.getQuantity());
                itemMapper.deleteById(item.getId());
            }
            for (PrescriptionItemRequest itemRequest : request.getItems()) {
                Drug drug = drugMapper.selectById(itemRequest.getDrugId());
                if (drug == null) {
                    throw new BusinessException("药品不存在，无法开具");
                }
                if (drug.getStatus() == null || drug.getStatus() != 1) {
                    throw new BusinessException("药品 " + drug.getDrugName() + " 已停用");
                }
                int deducted = drugMapper.deductStock(drug.getId(), itemRequest.getQuantity());
                if (deducted == 0) {
                    throw new StockNotEnoughException("药品 " + drug.getDrugName() + " 库存不足，当前仅剩 "
                            + drug.getStock() + " " + drug.getUnit() + "，请调整数量或更换药品");
                }
                PrescriptionItem item = new PrescriptionItem();
                item.setPrescriptionId(existing.getId());
                item.setDrugId(drug.getId());
                item.setDrugName(drug.getDrugName());
                item.setSpec(drug.getSpec());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnit(drug.getUnit());
                item.setDosage(itemRequest.getDosage());
                item.setPrice(drug.getPrice());
                itemMapper.insert(item);
            }
            existing.setStatus(request.getItems() == null || request.getItems().isEmpty() ? 1 : 0);
        }
        existing.setDiagnosis(request.getDiagnosis());
        existing.setPrescTime(LocalDateTime.now());
        updateById(existing);
        log.info("复诊更新诊断记录：prescNo={}", existing.getPrescNo());
        return existing;
    }

    @Override
    public IPage<Prescription> page(Long registrationId, Long patientId, Long doctorId, long pageNum, long pageSize) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<Prescription>()
                .eq(registrationId != null, Prescription::getRegistrationId, registrationId)
                .eq(patientId != null, Prescription::getPatientId, patientId)
                .eq(doctorId != null, Prescription::getDoctorId, doctorId)
                .orderByDesc(Prescription::getPrescTime);
        IPage<Prescription> result = page(new Page<>(pageNum, pageSize), wrapper);
        fillDoctorNames(result.getRecords());
        return result;
    }

    /** 批量回填医生姓名，避免前端仅展示数字 ID */
    private void fillDoctorNames(List<Prescription> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (Prescription p : records) {
            if (p.getDoctorId() != null) {
                ids.add(p.getDoctorId());
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        java.util.Map<Long, String> nameMap = doctorMapper.selectBatchIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Doctor::getId, Doctor::getName));
        // 批量标记是否含药品明细：纯诊断记录无需支付
        java.util.Set<Long> withItems = itemMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>()
                        .in(PrescriptionItem::getPrescriptionId, ids))
                .stream().map(PrescriptionItem::getPrescriptionId)
                .collect(java.util.stream.Collectors.toSet());
        for (Prescription p : records) {
            p.setDoctorName(nameMap.get(p.getDoctorId()));
            p.setNeedPay(withItems.contains(p.getId()));
        }
    }

    @Override
    public java.math.BigDecimal totalAmount(Long prescriptionId) {
        return itemMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId))
                .stream()
                .map(item -> item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Prescription pay(Long patientId, Long prescriptionId) {
        Prescription prescription = detail(prescriptionId);
        if (!patientId.equals(prescription.getPatientId())) {
            throw new BusinessException("只能支付本人的处方");
        }
        if (prescription.getStatus() != null && prescription.getStatus() != 0) {
            throw new BusinessException("该处方已支付，请勿重复支付");
        }
        // 余额扣款：不足时整体回滚
        walletService.deduct(patientId, totalAmount(prescriptionId));
        prescription.setStatus(1);
        updateById(prescription);
        log.info("处方支付成功：prescNo={}, patientId={}", prescription.getPrescNo(), patientId);
        return prescription;
    }

    @Override
    public Prescription detail(Long id) {
        Prescription prescription = getById(id);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        return prescription;
    }

    @Override
    public List<PrescriptionItem> items(Long prescriptionId) {
        detail(prescriptionId);
        return itemMapper.selectList(new LambdaQueryWrapper<PrescriptionItem>()
                .eq(PrescriptionItem::getPrescriptionId, prescriptionId)
                .orderByAsc(PrescriptionItem::getId));
    }

    private String nextPrescNo() {
        return "PR" + LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 14)
                + String.format("%03d", Math.abs(System.nanoTime() % 1000));
    }
}
