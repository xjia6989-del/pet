package com.petservices.controller;


import com.petservices.config.AppConsts;
import com.petservices.dto.BookingListDto;
import com.petservices.dto.PageDto;
import com.petservices.entity.Booking;
import com.petservices.entity.Pet;
import com.petservices.service.BookingService;
import com.petservices.service.IPetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ${author}
 * @since 2025-02-23
 */
@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    BookingService bookingService;
    @Autowired
    private IPetService petService;

    /**
     * 获取所有预约分页
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @GetMapping("/getBookingList/{currentPage}/{pageSize}")
    public PageDto getBookingList(@PathVariable Integer currentPage, @PathVariable Integer pageSize) {
        return bookingService.getBookingList(currentPage, pageSize);
    }

    /**
     * 修改预约信息
     *
     * @param bookingId
     * @param flag
     */
    @GetMapping("/updateBookingFlag/{bookingId}/{flag}")
    public PageDto updateBooking(@PathVariable("bookingId") Integer bookingId , @PathVariable("flag") Integer flag) {
        return bookingService.updateBooking(bookingId , flag);
    }

    /**
     * 添加预约
     *
     * @param booking
     * @return
     */
    @PostMapping("/addBooking")
    public Booking addBooking(@RequestBody Booking booking, HttpSession session) {
        try {
            Integer userId = booking.getUserId() != null ? booking.getUserId() : 1;

            Integer petId = booking.getPetId();
            if (petId == null) {
                return null;
            }
            Pet pet = petService.getById(petId);
            if (pet == null) {
                return null;
            }
            if (!pet.getUserId().equals(userId)) {
                userId = pet.getUserId();
            }
            if (booking.getServe() == null) {
                return null;
            }

            booking.setUserId(userId);
            boolean saved = bookingService.addBooking(booking);
            if (!saved) {
                return null;
            }
            return booking;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询某服务某天可预约时间段
     */
    @GetMapping("/availableSlots")
    public List<String> getAvailableSlots(@RequestParam Integer serveId,
                                          @RequestParam String date) {
        try {
            Date parsedDate = new java.text.SimpleDateFormat(AppConsts.dateFormatPattern).parse(date);
            return bookingService.getAvailableSlots(serveId, parsedDate);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 取消预约（用户）
     */
    @PostMapping("/cancel/{bookingId}")
    public Boolean cancelBooking(@PathVariable Integer bookingId,
                                 @RequestParam(value = "userId", required = false) Integer userId,
                                 HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        return bookingService.cancelBooking(bookingId, currentUserId);
    }

    /**
     * 删除预约（管理端）
     */
    @GetMapping("/deleteBooking/{bookingId}")
    public PageDto deleteBooking(@PathVariable Integer bookingId) {
        return bookingService.deleteBooking(bookingId);
    }

    /**
     * 按状态查询预约
     */
    @GetMapping("/searchBooking/{flag}")
    public List<BookingListDto> searchBooking(@PathVariable Integer flag) {
        return bookingService.searchBooking(flag);
    }

    /**
     * 查询当前用户预约
     */
    @GetMapping("/getMyBooking/{userId}")
    public List<BookingListDto> getMyBooking(@PathVariable Integer userId) {
        List<BookingListDto> list = bookingService.getMyBooking(userId);
        return list == null ? java.util.Collections.emptyList() : list;
    }

    /**
     * 提交评价
     */
    @PostMapping("/updateBookingComment")
    public List<BookingListDto> updateBookingComment(@RequestBody Booking booking) {
        return bookingService.updateBookingComment(booking);
    }

    private Integer resolveUserId(Integer userId, HttpSession session) {
        if (userId != null) {
            return userId;
        }
        Object sessionUserId = session.getAttribute("userId");
        if (sessionUserId instanceof Integer) {
            return (Integer) sessionUserId;
        }
        return null;
    }
}
