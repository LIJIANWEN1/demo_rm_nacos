package com.example.demo1_nacos.api;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xiao
 * @date: 2021/5/21 10:24
 */
public interface Collecter {

    /**
     * 获取总数量
     *
     * @param tableName 表名
     * @param filter    查询条件
     * @return 数量
     */
    Long count(String tableName, Map<String, Object> filter);

    Long countPersistence(String tableName);

    void pageQuery(int pageSize,String queryFields, String tableName, Map<String, Object> filter, DemoFunction function);

    List<Map> findMany(String tableName, Map<String, Object> filter);

}
