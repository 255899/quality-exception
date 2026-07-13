package com.gzklc.quality.dto;

import com.gzklc.quality.entity.ExceptionReport;
import com.gzklc.quality.entity.ProcessFlow;
import com.gzklc.quality.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class ExceptionDetailDTO {
    private ExceptionReport report;
    private List<ProcessFlow> flows;
    private User applicant;
}
