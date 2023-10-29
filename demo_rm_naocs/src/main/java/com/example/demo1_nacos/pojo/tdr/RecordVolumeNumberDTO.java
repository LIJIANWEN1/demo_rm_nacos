package com.example.demo1_nacos.pojo.tdr;

import lombok.Data;

/**
 * @description: 周期内起止卷件数
 * @author: gexc
 * @create: 2023-07-31
 **/

@Data
public class RecordVolumeNumberDTO {

    /**
     * 档案年度
     */
    private Integer year;

    /**
     * 最小年度
     */
    private Integer minYear;

    /**
     * 总件数
     */
    private Long recordSum;

    /**
     * 总卷数
     */
    private Long volumeSum;


}
