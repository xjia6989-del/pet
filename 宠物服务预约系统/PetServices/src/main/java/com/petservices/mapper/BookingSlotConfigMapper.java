package com.petservices.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservices.entity.BookingSlotConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.Date;
import java.util.List;

@Mapper
public interface BookingSlotConfigMapper extends BaseMapper<BookingSlotConfig> {

    @Update("DELETE FROM booking_slot_config WHERE serveId = #{serveId} AND DATE_FORMAT(configDate, '%Y-%m-%d') = #{day} AND timeSlot = #{timeSlot}")
    int deleteSlotConfig(@Param("serveId") Integer serveId,
                         @Param("day") String day,
                         @Param("timeSlot") String timeSlot);

    @Select("SELECT serveId, configDate, timeSlot, enabled, capacity FROM booking_slot_config WHERE serveId = #{serveId} AND DATE_FORMAT(configDate, '%Y-%m-%d') = #{day} AND timeSlot = #{timeSlot} LIMIT 1")
    BookingSlotConfig selectSlotConfig(@Param("serveId") Integer serveId,
                                       @Param("day") String day,
                                       @Param("timeSlot") String timeSlot);

    @Insert("INSERT INTO booking_slot_config (serveId, configDate, timeSlot, enabled, capacity) VALUES (#{serveId}, #{configDate}, #{timeSlot}, #{enabled}, #{capacity})")
    int insertSlotConfig(@Param("serveId") Integer serveId,
                         @Param("configDate") Date configDate,
                         @Param("timeSlot") String timeSlot,
                         @Param("enabled") Integer enabled,
                         @Param("capacity") Integer capacity);


}