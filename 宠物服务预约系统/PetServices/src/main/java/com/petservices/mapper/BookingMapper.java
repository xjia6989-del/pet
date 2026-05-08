package com.petservices.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservices.dto.BookingListDto;
import com.petservices.entity.Booking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2025-02-23
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    @Select("" +
            "SELECT\n" +
            "\tb.*,\n" +
            "\tu.NAME,\n" +
            "\tu.phone,\n" +
            "\ts.serveName,\n" +
            "\tp.petName \n" +
            "FROM\n" +
            "\t`booking` b\n" +
            "\tLEFT JOIN USER u ON b.userId = u.userId\n" +
            "\tLEFT JOIN pet p ON b.petId = p.petId\n" +
            "\tLEFT JOIN serve s ON b.serve = s.serveId \n" +
            "ORDER BY b.bookingDate DESC, b.bookingId DESC limit #{currentPage},#{pageSize}" +
            "")
    List<BookingListDto> getAllBooking(@Param("currentPage") Integer currentPage, @Param("pageSize")Integer pageSize);

    @Update("UPDATE `booking` SET vetId = #{vetId} WHERE bookingId = #{bookingId}")
    int assignVet(@Param("bookingId") Integer bookingId, @Param("vetId") Integer vetId);

    @Select("" +
            "SELECT\n" +
            "\tb.*,\n" +
            "\tu.NAME,\n" +
            "\tu.phone,\n" +
            "\ts.serveName,\n" +
            "\tp.petName \n" +
            "FROM\n" +
            "\t`booking` b\n" +
            "\tLEFT JOIN USER u ON b.userId = u.userId\n" +
            "\tLEFT JOIN pet p ON b.petId = p.petId\n" +
            "\tLEFT JOIN serve s ON b.serve = s.serveId where b.flag = #{flag}" +
            "")
    List<BookingListDto> getAllBookingByFlag(@Param("flag") Integer flag);

    @Select("" +
            "SELECT\n" +
            "\tb.*,\n" +
            "\tu.NAME,\n" +
            "\tu.phone,\n" +
            "\ts.serveName,\n" +
            "\tp.petName \n" +
            "FROM\n" +
            "\t`booking` b\n" +
            "\tLEFT JOIN USER u ON b.userId = u.userId\n" +
            "\tLEFT JOIN pet p ON b.petId = p.petId\n" +
            "\tLEFT JOIN serve s ON b.serve = s.serveId \n" +
            "WHERE b.vetId = #{vetId}\n" +
            "ORDER BY b.bookingDate DESC, b.bookingId DESC")
    List<BookingListDto> getVetBooking(@Param("vetId") Integer vetId);

    @Select("" +
            "SELECT\n" +
            "\tb.*,\n" +
            "\tu.NAME,\n" +
            "\tu.phone,\n" +
            "\ts.serveName,\n" +
            "\tp.petName \n" +
            "FROM\n" +
            "\t`booking` b\n" +
            "\tLEFT JOIN USER u ON b.userId = u.userId\n" +
            "\tLEFT JOIN pet p ON b.petId = p.petId\n" +
            "\tLEFT JOIN serve s ON b.serve = s.serveId where b.userId = #{userId}" +
            "")
    List<BookingListDto> getMyBooking(@Param("userId")Integer userId);

    @Select("" +
            "SELECT\n" +
            "\tb.*,\n" +
            "\tu.NAME,\n" +
            "\tu.phone,\n" +
            "\ts.serveName,\n" +
            "\tp.petName \n" +
            "FROM\n" +
            "\t`booking` b\n" +
            "\tLEFT JOIN USER u ON b.userId = u.userId\n" +
            "\tLEFT JOIN pet p ON b.petId = p.petId\n" +
            "\tLEFT JOIN serve s ON b.serve = s.serveId \n" +
            "WHERE p.petName LIKE CONCAT('%', #{petName}, '%')\n" +
            "ORDER BY b.bookingDate DESC")
    List<BookingListDto> searchBookingByPetName(@Param("petName") String petName);
}
