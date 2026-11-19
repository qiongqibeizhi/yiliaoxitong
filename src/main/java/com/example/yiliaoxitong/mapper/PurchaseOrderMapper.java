package com.example.yiliaoxitong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yiliaoxitong.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

/** 购药订单 Mapper。库存扣减复用 DrugMapper.deductStock 的条件更新。 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}
