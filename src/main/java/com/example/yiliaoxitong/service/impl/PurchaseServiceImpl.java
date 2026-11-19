package com.example.yiliaoxitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yiliaoxitong.dto.PurchaseRequest;
import com.example.yiliaoxitong.entity.Drug;
import com.example.yiliaoxitong.entity.PurchaseOrder;
import com.example.yiliaoxitong.exception.BusinessException;
import com.example.yiliaoxitong.exception.StockNotEnoughException;
import com.example.yiliaoxitong.mapper.DrugMapper;
import com.example.yiliaoxitong.mapper.PurchaseOrderMapper;
import com.example.yiliaoxitong.service.PurchaseService;
import com.example.yiliaoxitong.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 购药订单服务实现。
 *
 * <p>事务边界：purchase() 标注 @Transactional(rollbackFor = Exception.class)。
 * 下单流程为"校验药品在售 → 条件更新原子扣库存 → 写订单"，库存不足时整体回滚，
 * 不会出现"扣了库存、订单没写"或"库存扣成负数"的中间状态。</p>
 */
@Service
public class PurchaseServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseServiceImpl.class);

    /** 沙箱收银台统一支付密码，仅用于教学演示 */
    private static final String SANDBOX_PAY_PASSWORD = "123456";

    private final DrugMapper drugMapper;
    private final WalletService walletService;

    public PurchaseServiceImpl(DrugMapper drugMapper, WalletService walletService) {
        this.drugMapper = drugMapper;
        this.walletService = walletService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder purchase(Long patientId, PurchaseRequest request) {
        Drug drug = drugMapper.selectById(request.getDrugId());
        if (drug == null) {
            throw new BusinessException("药品不存在，请重新选择");
        }
        if (drug.getStatus() == null || drug.getStatus() != 1) {
            throw new BusinessException("该药品已停售，请选择其他药品");
        }
        if (drug.getStock() < request.getQuantity()) {
            throw new StockNotEnoughException(
                    "药品「" + drug.getDrugName() + "」库存不足，当前仅余 " + drug.getStock() + " " + drug.getUnit());
        }

        // 条件更新原子扣减库存：返回 0 说明并发下库存已被抢空
        int deducted = drugMapper.deductStock(drug.getId(), request.getQuantity());
        if (deducted == 0) {
            throw new StockNotEnoughException(
                    "药品「" + drug.getDrugName() + "」库存已被抢空，请稍后再试");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(nextOrderNo());
        order.setPatientId(patientId);
        order.setDrugId(drug.getId());
        order.setDrugName(drug.getDrugName());
        order.setSpec(drug.getSpec());
        order.setQuantity(request.getQuantity());
        order.setPrice(drug.getPrice());
        order.setAmount(drug.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        // 下单仅锁定库存，订单进入待支付状态，由虚拟收银台完成支付
        order.setStatus(0);
        save(order);
        log.info("患者购药成功：orderNo={}, patientId={}, drugId={}, quantity={}",
                order.getOrderNo(), patientId, drug.getId(), request.getQuantity());
        return order;
    }

    @Override
    public IPage<PurchaseOrder> page(Long patientId, long pageNum, long pageSize) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>()
                .eq(patientId != null, PurchaseOrder::getPatientId, patientId)
                .orderByDesc(PurchaseOrder::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder pay(Long patientId, Long orderId, String payPassword, String payChannel, String bankCardNo) {
        PurchaseOrder order = getById(orderId);
        if (order == null || (patientId != null && !patientId.equals(order.getPatientId()))) {
            throw new BusinessException("订单不存在，请刷新后重试");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("该订单已支付或已退款，请勿重复支付");
        }
        // 沙箱收银台：模拟支付密码校验，演示密码固定为 123456
        if (!SANDBOX_PAY_PASSWORD.equals(payPassword)) {
            log.warn("虚拟支付密码错误：orderNo={}", order.getOrderNo());
            throw new BusinessException("付款密码错误，请重新输入");
        }
        // 银行卡支付时校验卡号格式（沙箱环境仅校验位数）
        if ("BANK".equals(payChannel) && (bankCardNo == null || !bankCardNo.matches("\\d{13,19}"))) {
            throw new BusinessException("请输入正确的银行卡号（13-19 位数字）");
        }
        // 余额支付：余额不足时整体回滚（库存不会误扣）
        walletService.deduct(order.getPatientId(), order.getAmount());
        order.setStatus(1);
        updateById(order);
        log.info("虚拟支付成功：orderNo={}, amount={}, channel={}",
                order.getOrderNo(), order.getAmount(), channelName(payChannel));
        return order;
    }

    private String channelName(String channel) {
        return switch (channel == null ? "WECHAT" : channel) {
            case "ALIPAY" -> "支付宝";
            case "BANK" -> "银行卡";
            default -> "微信支付";
        };
    }

    /** 订单号：P + 当前日期 + 6 位序列，保证可读且基本唯一 */
    private String nextOrderNo() {
        String date = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = Math.abs(System.nanoTime() % 1000000L);
        return "P" + date + String.format("%06d", seq);
    }
}
