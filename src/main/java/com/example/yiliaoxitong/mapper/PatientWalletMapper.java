package com.example.yiliaoxitong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yiliaoxitong.entity.PatientWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 患者钱包 Mapper。余额增减使用条件更新，防止并发下余额被扣成负数。
 */
@Mapper
public interface PatientWalletMapper extends BaseMapper<PatientWallet> {

    /** 余额充足时原子扣减，返回 0 表示余额不足 */
    @Update("UPDATE patient_wallet SET balance = balance - #{amount} "
            + "WHERE patient_id = #{patientId} AND balance >= #{amount}")
    int deduct(@Param("patientId") Long patientId, @Param("amount") BigDecimal amount);

    /** 充值：无账户则自动开户 */
    @Update("INSERT INTO patient_wallet (patient_id, balance) VALUES (#{patientId}, #{amount}) "
            + "ON DUPLICATE KEY UPDATE balance = balance + #{amount}")
    int credit(@Param("patientId") Long patientId, @Param("amount") BigDecimal amount);

    @Select("SELECT balance FROM patient_wallet WHERE patient_id = #{patientId}")
    BigDecimal balanceOf(@Param("patientId") Long patientId);
}
