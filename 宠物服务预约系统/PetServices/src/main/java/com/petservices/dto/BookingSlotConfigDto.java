package com.petservices.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BookingSlotConfigDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer serveId;
    private String date;
    private String timeSlot;
    private Integer enabled;
    private Integer capacity;
    private Integer bookedCount;
    private Integer remainingCount;
}