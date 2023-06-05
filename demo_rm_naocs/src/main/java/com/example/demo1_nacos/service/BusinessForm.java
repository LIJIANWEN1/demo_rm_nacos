package com.example.demo1_nacos.service;

import lombok.Data;

import java.util.Map;

/**
 * @author leanderli
 * @see BusinessForm
 * @since 2020.08.13
 */
@Data
public class BusinessForm {

    /**
     * 数据表主键
     */
    private String id;
    /**
     * 该条目标识，可以为历史系统中的主键也可以为新生成的主键，在原文表中原文数据上的该标识将与此一致
     */
    private String itemFlag;
    /**
     * 表单相关属性
     */
    private String objectId, objectPath, objectType, jsonMetadata, parentPath;
    private Map<String, Object> attributes;

    public void setAttributes(Map<String, Object> attributes) {
        attributes.remove("id");
        this.attributes = attributes;
    }

}
