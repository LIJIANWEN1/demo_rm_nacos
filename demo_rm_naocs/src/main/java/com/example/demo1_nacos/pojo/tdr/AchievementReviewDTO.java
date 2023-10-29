package com.example.demo1_nacos.pojo.tdr;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Bianlj
 * @date 2023/6/13
 */
@Data
public class AchievementReviewDTO {

    private String id;

    private String archivalId;

    private String title;

    private String fileYear;

    private String author;

    private String archiveType;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date docDate;

    private String openClass;

    private String keywordApproveStatus;

    private String securityClass;

    private String fondsId;

    private String docNumber;

    private String retentionPeriod;

    private String objectPath;

    private String summary;
}
