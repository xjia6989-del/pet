package com.petservices.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PetVisionRecognitionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String species;
    private String breed;
    private String color;
    private String size;
    private String ageEstimate;
    private Integer age;
    private List<String> traits;
    private String personalitySummary;
    private Double confidence;
    private String source;
    private String rawName;
}
