package com.example.demo1_nacos.pojo.tdr;

import lombok.Data;

/**
 * @description:
 * @author: gexc
 * @create: 2023-07-07
 **/

@Data
public class HoldingStatisticsDTO {

    /**
     * 馆藏接收年度
     */
    private Integer year;

    /**
     * 总件数
     */
    private Long recordSum;

    /**
     * 总卷数
     */
    private Long volumeSum;

    /**
     * 当年新增件数
     */
    private Long currentYearRecordSum;

    /**
     * 当年新增卷数
     */
    private Long currentYearVolumeSum;
}
