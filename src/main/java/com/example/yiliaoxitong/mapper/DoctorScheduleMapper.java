package com.example.yiliaoxitong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yiliaoxitong.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 排班 Mapper。号源的扣减与释放使用条件更新，把"判断"和"修改"合并成一条语句，
 * 避免并发下先查后改造成的超卖。
 */
@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    /** 号源未满时原子地加一，返回 0 表示号源已满 */
    @Update("UPDATE doctor_schedule SET booked_slots = booked_slots + 1 "
            + "WHERE id = #{id} AND booked_slots < total_slots")
    int bookSlot(@Param("id") Long id);

    /** 取消挂号时释放号源，返回 0 表示已无计数可释放 */
    @Update("UPDATE doctor_schedule SET booked_slots = booked_slots - 1 "
            + "WHERE id = #{id} AND booked_slots > 0")
    int releaseSlot(@Param("id") Long id);
}
