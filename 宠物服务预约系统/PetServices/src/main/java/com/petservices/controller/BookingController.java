package com.petservices.controller;


import com.petservices.config.AppConsts;
import com.petservices.dto.BookingListDto;
import com.petservices.dto.BookingSlotConfigDto;
import com.petservices.dto.PageDto;
import com.petservices.entity.Booking;
import com.petservices.entity.Pet;
import com.petservices.service.BookingService;
import com.petservices.service.IPetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

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
     * 分配兽医
     */
    @GetMapping("/assignVet/{bookingId}/{vetId}")
    public Boolean assignVet(@PathVariable Integer bookingId, @PathVariable Integer vetId) {
        try {
            return bookingService.assignVet(bookingId, vetId);
        } catch (Exception e) {
            log.error("assignVet exception, bookingId={}, vetId={}", bookingId, vetId, e);
            return false;
        }
    }

    /**
     * 查询分配给指定兽医的预约
     */
    @GetMapping("/getVetBooking/{vetId}")
    public List<BookingListDto> getVetBooking(@PathVariable Integer vetId) {
        return bookingService.getVetBooking(vetId);
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
            log.info("addBooking request => userId={}, petId={}, serve={}, bookingDate={}",
                    booking == null ? null : booking.getUserId(),
                    booking == null ? null : booking.getPetId(),
                    booking == null ? null : booking.getServe(),
                    booking == null ? null : booking.getBookingDate());

            Integer userId = booking.getUserId() != null ? booking.getUserId() : 1;

            Integer petId = booking.getPetId();
            if (petId == null) {
                log.warn("addBooking failed: petId is null");
                return null;
            }
            Pet pet = petService.getById(petId);
            if (pet == null) {
                log.warn("addBooking failed: pet not found, petId={}", petId);
                return null;
            }
            if (!pet.getUserId().equals(userId)) {
                userId = pet.getUserId();
            }
            if (booking.getServe() == null) {
                log.warn("addBooking failed: serve is null, petId={}, userId={}", petId, userId);
                return null;
            }

            booking.setUserId(userId);
            boolean saved = bookingService.addBooking(booking);
            if (!saved) {
                log.warn("addBooking failed in service layer: petId={}, serve={}, bookingDate={}",
                        petId, booking.getServe(), booking.getBookingDate());
                return null;
            }
            log.info("addBooking success: bookingId={}, petId={}, serve={}", booking.getBookingId(), petId, booking.getServe());
            return booking;
        } catch (Exception e) {
            log.error("addBooking exception", e);
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
            log.error("getAvailableSlots failed, serveId={}, date={}", serveId, date, e);
            return bookingService.getAllDefaultSlots();
        }
    }

    /**
     * 预约时段配置（管理员）
     */
    @GetMapping("/slotConfig")
    public List<BookingSlotConfigDto> getSlotConfig(@RequestParam Integer serveId,
                                                    @RequestParam String date) {
        return bookingService.getSlotConfig(serveId, date);
    }

    /**
     * 保存单个时段配置（管理员）
     */
    @PostMapping("/slotConfig")
    public Boolean saveSlotConfig(@RequestBody BookingSlotConfigDto dto) {
        try {
            log.info("saveSlotConfig request => serveId={}, date={}, timeSlot={}, enabled={}, capacity={}",
                    dto == null ? null : dto.getServeId(),
                    dto == null ? null : dto.getDate(),
                    dto == null ? null : dto.getTimeSlot(),
                    dto == null ? null : dto.getEnabled(),
                    dto == null ? null : dto.getCapacity());
            Boolean ok = bookingService.saveSlotConfig(dto);
            log.info("saveSlotConfig result => {}", ok);
            return ok;
        } catch (Exception e) {
            log.error("saveSlotConfig exception", e);
            return false;
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
     * 按宠物名搜索预约
     */
    @GetMapping("/searchBookingByPet")
    public List<BookingListDto> searchBookingByPet(@RequestParam String petName) {
        return bookingService.searchBookingByPetName(petName);
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
