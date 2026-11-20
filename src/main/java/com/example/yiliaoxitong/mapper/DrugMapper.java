package com.example.yiliaoxitong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yiliaoxitong.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 药品 Mapper。库存扣减使用条件更新，防止库存被扣成负数。
 */
@Mapper
public interface DrugMapper extends BaseMapper<Drug> {

    /** 库存充足时原子扣减，返回 0 表示库存不足 */
    @Update("UPDATE drug SET stock = stock - #{quantity} "
            + "WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /** 归还库存（修改诊断记录时回退原药品占用） */
    @Update("UPDATE drug SET stock = stock + #{quantity} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
