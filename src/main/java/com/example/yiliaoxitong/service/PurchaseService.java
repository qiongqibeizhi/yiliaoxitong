package com.example.yiliaoxitong.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yiliaoxitong.dto.PurchaseRequest;
import com.example.yiliaoxitong.entity.PurchaseOrder;

/**
 * 购药订单服务：患者单独购药（扣库存事务）与订单查询。
 */
public interface PurchaseService extends IService<PurchaseOrder> {

    PurchaseOrder purchase(Long patientId, PurchaseRequest request);

    IPage<PurchaseOrder> page(Long patientId, long pageNum, long pageSize);

    /** 虚拟收银台支付：校验付款密码并完成订单 */
    PurchaseOrder pay(Long patientId, Long orderId, String payPassword, String payChannel, String bankCardNo);
}
