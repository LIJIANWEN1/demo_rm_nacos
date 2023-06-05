package com.example.demo1_nacos.vo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ImportArchivePackageVO {

    public static final String VOLUME = "volume";

    public static final String RECORD = "record";

    String parentPath;

    //整理方式，volume 卷整理、record 件整理
    String collectionWay;

    //元数据方案id
    String metadataSchemeId;

    //record 件类型，volume 卷类型
    String type;

    String unitCode;

    List<Map<String,String>> dataList;

    String classRule;

}