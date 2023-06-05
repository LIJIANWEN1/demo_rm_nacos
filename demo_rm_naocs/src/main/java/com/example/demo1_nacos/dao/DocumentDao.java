package com.example.demo1_nacos.dao;

import com.example.demo1_nacos.pojo.Document;
import com.example.demo1_nacos.service.BusinessForm;

import java.util.List;

/**
 * @Author: zhaohuaxia
 * @DateTime: 2021/10/16 11:08
 * @Description:
 */
public interface DocumentDao {

    String MODE_NORMAL = "normal";
    String MODE_REIMPORT_SKIP = "reimportWithSkip";
    String MODE_REIMPORT_WITHOUT_SKIP = "reimportWithoutSkip";

    /**
     * 实例化电子文件
     *
     * @param documents
     * @param saveMode
     * @return 匹配saveMode后的电子文件列表
     */
    void save(List<Document> documents, String saveMode);

    /**
     * 获取业务表单
     *
     * @param itemFlag
     * @param unitCode
     * @return
     */
    BusinessForm getBusinessForms(String itemFlag, String unitCode);
}
