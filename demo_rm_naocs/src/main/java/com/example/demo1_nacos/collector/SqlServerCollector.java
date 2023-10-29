//package com.example.demo1_nacos.collector;
//import com.example.demo1_nacos.api.Collecter;
//import com.example.demo1_nacos.api.DemoFunction;
//import com.example.demo1_nacos.mapper.sqlserver.SqlServerMapper;
//import com.example.demo1_nacos.pojo.BaseQueryModel;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.ibatis.cursor.Cursor;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.CollectionUtils;
//
//import javax.annotation.Resource;
//import java.util.*;
//
///**
// * @author zhangLei
// * @version 1.0
// * @date 2022/9/21 16:28
// */
//@Slf4j
//@Component("sqlServerCollecter")
//public class SqlServerCollector implements Collecter {
//
//    @Resource
//    private SqlServerMapper sqlServerMapper;
//
//    @Resource(name = "otherSqlSessionFactory")
//    private SqlSessionFactory factory;
//
//    @Value("${cn.amberdata.pagesize}")
//    private Integer pageSize;
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public Long count(String tableName, Map<String, Object> filter) {
//        BaseQueryModel model;
//        if (CollectionUtils.isEmpty(filter)) {
//            model = new BaseQueryModel();
//        } else {
//            model = new BaseQueryModel(filter);
//        }
//        return sqlServerMapper.count(tableName, model);
//    }
//
//    @Override
//    public Long countPersistence(String tableName) {
//        return null;
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public void pageQuery(int pageSize,String queryFields, String tableName, Map<String, Object> filter, DemoFunction function) {
//        factory.getConfiguration().setDefaultFetchSize(pageSize);
//        BaseQueryModel model;
//        if (CollectionUtils.isEmpty(filter)) {
//            model = new BaseQueryModel();
//        } else {
//            model = new BaseQueryModel(filter);
//        }
//        try (Cursor<Map<String, Object>> allRecord = sqlServerMapper.findByWrapper(queryFields, tableName, model)) {
//            Iterator<Map<String, Object>> iterator = allRecord.iterator();
//            List<Map<String, Object>> list = new ArrayList<>();
//            Map<String, Object> map;
//            while (iterator.hasNext()) {
//                map = iterator.next();
//                list.add(map);
//                if (pageSize == list.size()) {
//                    log.info("读数据：{}， 条数： {}", tableName, pageSize);
//                    function.apply(list);
//                    list = new ArrayList<>();
//                }
//            }
//            function.apply(list);
//        } catch (Exception e) {
//            log.error("读取MySQL失败", e);
//        }
//    }
//
//    private List<Map<String, Object>> parseData(List<Map<String, Object>> list) {
//        List<Map<String, Object>> dataList = new ArrayList<>();
//        for (Map<String, Object> map : list) {
//            String sData = (String) map.get("s_data");
//            if (StringUtils.isBlank(sData)) {
//                dataList.add(map);
//                continue;
//            }
//            try {
//                dataList.add(objectMapper.readValue(sData, Map.class));
//            } catch (Exception e) {
//                log.error("解析s_data失败", e);
//            }
//        }
//        return dataList;
//    }
//
//    private long offset(long page, int pageSize) {
//        return 0 >= page ? 1 : (page - 1) * pageSize;
//    }
//
//    @Override
//    public List<Map> findMany(String tableName, Map<String, Object> filter) {
//        return new ArrayList<>();
//    }
//
//}
