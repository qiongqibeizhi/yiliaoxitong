package com.example.yiliaoxitong.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yiliaoxitong.common.Result;
import com.example.yiliaoxitong.dto.PayRequest;
import com.example.yiliaoxitong.dto.PurchaseRequest;
import com.example.yiliaoxitong.entity.PurchaseOrder;
import com.example.yiliaoxitong.security.AuthContext;
import com.example.yiliaoxitong.security.RequireRole;
import com.example.yiliaoxitong.security.Role;
import com.example.yiliaoxitong.security.TokenPayload;
import com.example.yiliaoxitong.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者单独购药接口：不挂号也可直接购买在售药品，下单即扣库存（事务防超卖）。
 *
 * <p>数据权限：患者只能查看本人订单，管理员可见全部订单。</p>
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /** 患者下单购药：患者身份取自登录令牌，防止代他人下单 */
    @PostMapping
    @RequireRole({Role.PATIENT, Role.ADMIN})
    public Result<PurchaseOrder> purchase(@Valid @RequestBody PurchaseRequest request) {
        TokenPayload user = AuthContext.get();
        Long patientId = user.patientId();
        if (patientId == null) {
            // 管理员代客下单时由请求参数指定患者编号
            patientId = request.getPatientId();
        }
        return Result.ok(purchaseService.purchase(patientId, request));
    }

    /** 虚拟收银台支付：输入付款密码完成待支付订单 */
    @PutMapping("/{id}/pay")
    @RequireRole({Role.PATIENT, Role.ADMIN})
    public Result<PurchaseOrder> pay(@PathVariable Long id,
                                     @Valid @RequestBody PayRequest request) {
        TokenPayload user = AuthContext.get();
        Long patientId = Role.PATIENT.name().equals(user.role()) ? user.patientId() : null;
        return Result.ok(purchaseService.pay(patientId, id, request.getPayPassword(), request.getPayChannel(), request.getBankCardNo()));
    }

    @GetMapping
    @RequireRole({Role.PATIENT, Role.ADMIN})
    public Result<IPage<PurchaseOrder>> page(@RequestParam(required = false) Long patientIdParam,
                                             @RequestParam(defaultValue = "1") long pageNum,
                                             @RequestParam(defaultValue = "10") long pageSize) {
        TokenPayload user = AuthContext.get();
        Long patientId;
        if (Role.PATIENT.name().equals(user.role()) && user.patientId() != null) {
            // 患者强制只看本人订单
            patientId = user.patientId();
        } else {
            // 管理员可查全部订单，亦可按患者编号筛选
            patientId = patientIdParam;
        }
        return Result.ok(purchaseService.page(patientId, pageNum, pageSize));
    }
}
