package com.example.demo1_nacos.service;
import cn.amberdata.cache.CacheTemplate;
import cn.amberdata.common.contenttransfer.ContentTransfer;
import cn.amberdata.dm.common.context.session.SessionContext;
import cn.amberdata.dm.document.DocumentDO;
import cn.amberdata.dm.session.SessionUtil;
import cn.amberdata.dm.sysobject.SysObjectDO;
import cn.amberdata.dm.sysobject.mapper.SysObjectMapper;
import cn.amberdata.rm.archive.record.Record;
import cn.amberdata.rm.archive.record.RecordDO;
import cn.amberdata.rm.archive.record.RecordRepository;
import cn.amberdata.rm.archive.record.mapper.RecordMapper;
import cn.amberdata.rm.archive.volume.Volume;
import cn.amberdata.rm.archive.volume.VolumeRepository;
import cn.amberdata.rm.classification.SubCategory;
import cn.amberdata.rm.common.domain.TypeClassConstant;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumn;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumnRepository;
import cn.amberdata.rm.settings.strategy.RetentionStrategy;
import cn.amberdata.rm.settings.strategy.RetentionStrategyRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1_nacos.Demo1NacosApplication;
import com.example.demo1_nacos.pojo.YC.CertificatePO;
import com.example.demo1_nacos.pojo.YC.OrderInfo;
import com.example.demo1_nacos.pojo.YC.YCResult;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import io.minio.MinioClient;
import net.minidev.json.JSONArray;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 17:50
 */
@Service
public class UpdateArchiveServiceImpl {

    @Resource
    private SysObjectMapper sysObjectMapper;

    @Resource
    private RetentionStrategyRepository retentionStrategyRepository;

    @Resource
    private MetadataColumnRepository metadataColumnRepository;

    @Resource
    private RmOtherServiceImpl rmOtherService;

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private RecordRepository recordRepository;

    private static final Logger StartLogger = LoggerFactory.getLogger(UpdateArchiveServiceImpl.class);

    Map<String,String> codeMap = new HashMap<>();

    public void updateArchivalId(String path,String archiveType) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_object_path", path).eq("archive_type",archiveType);
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 1000)).getRecords();
        for (SysObjectDO sysObjectDO : list) {
            String id = sysObjectDO.getId();
            Record record = recordRepository.find(id);
            String third_class_code = record.getString("third_class_code");
            Map<String, Object> map = new HashMap<>();
            map.put("retention_period", third_class_code);
            DocumentContext documentContext = updateJson(record.getJsonMetadata(), map);
            Record updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), documentContext.jsonString());
            updateRecord.update();
            updateRecord.updateJsonMetadata(documentContext.jsonString());
            fixedThreadPool.execute(() -> {
                recordRepository.store(updateRecord);
                StartLogger.error("---旧档号---" + record.getArchivalId() + "-----" + third_class_code);
            });
            StartLogger.error("------修改完成----");
        }
    }

    public void updateArchivalIdByYuLin(List<String []> ruleByYuLin){
        SessionContext.setSession(SessionUtil.getAdminSession());
        for (String [] arr : ruleByYuLin) {
         String year = arr[0];
         String firstName = arr[1];
         String secondName =arr[2];
         String thirdName =arr[3];
         Map<String,String> map= new HashMap<>();
            map.put("file_year",year);
            map.put("first_class_code",firstName);
            map.put("second_class_code",secondName);
            map.put("third_class_code",thirdName);
            QueryWrapper<RecordDO> queryWrapper = new QueryWrapper<>();
            //构建查询条件
            queryWrapper.eq("file_year",year)
                    .eq("first_class_code", firstName)
                    .eq("second_class_code",secondName)
            .eq("third_class_code",thirdName).eq("archive_type","da_record").orderByAsc("s_create_date");
            //TODO
//            List<String> ids = recordMapper.getListByWrapper(queryWrapper,new Page<RecordDO>(1, 1000)).getRecords();
            List<String> ids = null;
            System.out.println(year+"--"+firstName+"---"+secondName+"----"+thirdName+"---"+ids.size());
            int i = 1;
            for (String id : ids) {
////                SubCategory subCategory = rmOtherService.getSubCategoryByClassRule("file_year@first_class_name@retention_period","bf14013771219501056",map,"bf14013771219501056");
//                Record record = recordRepository.find(id);
////                //设置分类号
////                record.updateClassification(subCategory.getClassificationCode(), subCategory.getObjectId().getId());
//                List<MetadataColumn> recordMetadataColumnList = metadataColumnRepository.getMetadataColumnByMetadataSchemeId(record.getMetadataSchemeId());
////                //移动到档案库对应门类下
////                record.move(subCategory.getObjectPath());
////                RetentionStrategy retentionStrategy = retentionStrategyRepository.find("bf14012897260765184");
////                record.archive(null, true, "", retentionStrategy);
//                String newArchivalId = year+"."+record.getString("first_class_code")+"."+record.getString("second_class_code")+"."+retentionPeriod+
//                        "-"+String.format("%04d", i);
//                record.updateArchivalId(newArchivalId);
//                record.updateJsonForm(null, recordMetadataColumnList);
//                recordRepository.store(record);
                System.out.println("---------------"+id);
                i++;
            }

        }
    }

    @Resource
    private VolumeRepository volumeRepository;

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(500);

    public void addWSBlock(String objectType) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s_object_type",objectType).isNull("update_doc_date_status");
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 120000)).getRecords();
//        List<List<SysObjectDO>> partitionList = Lists.partition(list, 5000);
//        partitionList.forEach(o -> {
//            System.out.println(o);
//        });
        AtomicReference<Integer> a = new AtomicReference<>(list.size());
        for (SysObjectDO sysObjectDO : list) {
            Volume volume = null;
            try {
                volume = volumeRepository.find(sysObjectDO.getId(),false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(null == volume){
                continue;
            }
            Map<String,Object> map = new HashMap<>();
            map.put("载体规格","ztgg");
            String newJsonStr = updateJsonName(volume.getJsonMetadata(),map).jsonString();
            if(StringUtils.isBlank(newJsonStr)){
                continue;
            }
            Volume updateRecord = null;
            try {
                updateRecord = volumeRepository.findByVolumeIdAndUpdateJsonMetadata(volume.getObjectId().getId(), newJsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(null == updateRecord){
                continue;
            }
            System.out.println("当前档号："+updateRecord.getArchivalId());
            updateRecord.update();
            updateRecord.setString("update_doc_date_status","01");
            updateRecord.updateJsonMetadata(newJsonStr);
            Volume finalUpdateRecord = updateRecord;
            fixedThreadPool.execute(() -> {
                volumeRepository.store(finalUpdateRecord);
                a.getAndSet(a.get() - 1);
                System.out.println(a+" "+finalUpdateRecord.getArchivalId());
            });
        }
        System.out.println("------修改完成----");
    }

    public void addLyXCSJ(String objectType) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s_object_type",objectType).isNull("update_doc_date_status");
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 120000)).getRecords();
//        List<List<SysObjectDO>> partitionList = Lists.partition(list, 5000);
//        partitionList.forEach(o -> {
//            System.out.println(o);
//        });
        AtomicReference<Integer> a = new AtomicReference<>(list.size());
        for (SysObjectDO sysObjectDO : list) {
            Record record = null;
            try {
                record = recordRepository.find(sysObjectDO.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(null == record){
                System.out.println("1空数据"+record.getObjectId().getId());
                continue;
            }
String newJsonStr = "";
//            String newJsonStr = repairDocDate(record.getJsonMetadata(),record.getArchivalId());
            if(StringUtils.isBlank(newJsonStr)){
                continue;
            }
            Record updateRecord = null;
            try {
                updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), newJsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(null == updateRecord){
                System.out.println("2空数据"+record.getObjectId().getId());
                continue;
            }
            updateRecord.update();
            updateRecord.setString("update_doc_date_status","01");
            updateRecord.updateJsonMetadata(newJsonStr);
            Record finalUpdateRecord = updateRecord;
            fixedThreadPool.execute(() -> {
                recordRepository.store(finalUpdateRecord);
                a.getAndSet(a.get() - 1);
                    System.out.println(a.get());
            });
        }
        System.out.println("------修改完成----");
    }


    public void addJbXCSJ(String objectType,String path,Integer size) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s_object_type",objectType).isNull("update_doc_date_status").likeRight("s_object_path",path);
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, size)).getRecords();
        AtomicReference<Integer> a = new AtomicReference<>(list.size());
        for (SysObjectDO sysObjectDO : list) {
            Record record = null;
            try {
                record = recordRepository.find(sysObjectDO.getId());
            } catch (Exception e) {
                System.out.println("----1.根据id构造record失败---"+sysObjectDO.getId());
                e.printStackTrace();
            }
            if(null == record){
                System.out.println("1空数据"+record.getObjectId().getId());
                continue;
            }
            System.out.println("----1.根据id构造record成功---"+sysObjectDO.getId());
            String newJsonStr = repairDocDate(record.getJsonMetadata());
            if(StringUtils.isBlank(newJsonStr)){
                System.out.println("----2.根据json 生成新json失败---"+sysObjectDO.getId());
                continue;
            }
            System.out.println("----2.根据json 生成新json成功---"+sysObjectDO.getId());
            Record updateRecord = null;
            try {
                updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), newJsonStr);
            } catch (Exception e) {
                System.out.println("----3.根据json 生成新json失败---"+sysObjectDO.getId()+"---"+record.getObjectId().getId());
                e.printStackTrace();
            }
            if(null == updateRecord){
                System.out.println("2空数据"+record.getObjectId().getId());
                continue;
            }
            System.out.println("----3.根据json 生成新json成功---"+sysObjectDO.getId());
            updateRecord.update();
            updateRecord.setString("update_doc_date_status","01");
            updateRecord.updateJsonMetadata(newJsonStr);
            Record finalUpdateRecord = updateRecord;
            fixedThreadPool.execute(() -> {
                try {
                    recordRepository.store(finalUpdateRecord);
                } catch (Exception e) {
                    System.out.println("----4.档案保存失败---"+sysObjectDO.getId());
                    e.printStackTrace();
                }
                a.getAndSet(a.get() - 1);
                System.out.println(a.get());
            });
        }
        System.out.println("------修改完成----");
        System.out.println("2323232323");
    }

    public void repairXCSJFormat(String objectType) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s_object_type",objectType).like("XCSJ","T");
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 120000)).getRecords();
        AtomicReference<Integer> a = new AtomicReference<>(list.size());
        for (SysObjectDO sysObjectDO : list) {
            Record record = null;
            try {
                record = recordRepository.find(sysObjectDO.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(null == record){
                continue;
            }
            String xcsj = readJson(record.getJsonMetadata(),"XCSJ");
            xcsj = xcsj.substring(0,xcsj.indexOf("T")).replace("-","");
            Map<String, Object> map = new HashMap<>();
            map.put("XCSJ", xcsj);
            DocumentContext documentContext = updateJson(record.getJsonMetadata(), map);
            String newJsonStr = documentContext.jsonString();
            if(StringUtils.isBlank(newJsonStr)){
                continue;
            }
            Record updateRecord = null;
            try {
                updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), newJsonStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(null == updateRecord){
                continue;
            }
            updateRecord.update();
            updateRecord.updateJsonMetadata(newJsonStr);
            Record finalUpdateRecord = updateRecord;
            fixedThreadPool.execute(() -> {
                recordRepository.store(finalUpdateRecord);
                a.getAndSet(a.get() - 1);
                System.out.println(a+"------"+finalUpdateRecord.getArchivalId());
            });
        }
        System.out.println("------修改完成----");
    }


    private String repairXCSJ(String aa){
        DocumentContext parse = JsonPath.parse(aa);
        String xcsj = readJson(aa,"XCSJ");
        System.out.println(aa.substring(0,aa.indexOf("T")).replace("-",""));
        return  parse.jsonString();
    }

    private static String repairDocDate(String aa){
        String orgKewWord = "doc_date";
        String keyword = "xcsj";
        DocumentContext parse = JsonPath.parse(aa);
        //-------江北文书卷内 开始-----------
        //江北文书卷内
        Object docDate = readJson(aa,"doc_date");
        List<Map<String,String>>  read = parse.read("$.record..[?(@.name == '内容信息')].property");
        if(read.size()==0){
            System.out.println("内容信息 block---");
        }
        List<Map<String,Object>> bb = parse.read("$.record..[?(@.name == '"+keyword+"')].title");
        if(bb.size()>0){
            System.out.println("存在形成时间字段");
            parse.put("$.record..[?(@.name == '" + keyword + "')]", "content", docDate);
        }else {
            List<Map<String,Object>> list = (List<Map<String, Object>>) read.get(0);
            Map<String,Object> val = new HashMap<>();
            val.put("name","xcsj");
            val.put("title","形成时间");
            val.put("content",docDate);
            list.add(val);
            parse.set("$.record..[?(@.name == '内容信息')].property",list);
        }
        parse.delete("$.record..[?(@.name == '" + orgKewWord + "')].content");
        //-------江北文书卷内 结束-----------

        return  parse.jsonString();
    }



    private static DocumentContext addWsRecordInVolume(DocumentContext parse){
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> val = new HashMap<>();
        val.put("name","TZ");
        val.put("title","图照");
        list.add(val);
        Map<String,Object> ywxx = new HashMap<>();
        ywxx.put("name","业务信息");
        ywxx.put("property",list);
        List<Map<String,Object>> read = parse.read("$.record.block");
        read.add(ywxx);
        return  parse;
    }

    private static DocumentContext addYWRecordInVolume(DocumentContext parse){
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> val = new HashMap<>();
        val.put("name","LBH");
        val.put("title","类别号");
        list.add(val);
        Map<String,Object> ywxx = new HashMap<>();
        ywxx.put("name","业务信息");
        ywxx.put("property",list);
        List<Map<String,Object>> read = parse.read("$.record.block");
        read.add(ywxx);
        return  parse;
    }

    private static DocumentContext addSWRecord(DocumentContext parse){
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> val = new HashMap<>();
        val.put("name","swlbh");
        val.put("title","实物类别号");
        list.add(val);
        Map<String,Object> ywxx = new HashMap<>();
        ywxx.put("name","实物");
        ywxx.put("property",list);
        List<Map<String,Object>> read = parse.read("$.record.block");
        read.add(ywxx);
        return  parse;
    }


    public String repairXCSJgs(String xcsj){
        if(xcsj.contains("T")){
            xcsj = xcsj.substring(0,xcsj.indexOf("T")).replace("-","");
        }
        return xcsj;
    }

    public void updateTimeStrJb(String path) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_object_path", path).eq("archive_type","da_volume").like("start_time","T");
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 6000)).getRecords();
        AtomicReference<Integer> a = new AtomicReference<>(list.size());
        for (SysObjectDO sysObjectDO : list) {
            String id = sysObjectDO.getId();
            Volume volume = volumeRepository.find(id,false);
            String start_time;
            String end_time;
            try{
                start_time  = readJson(volume.getJsonMetadata(), "start_time");
                end_time  = readJson(volume.getJsonMetadata(), "end_time");
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(volume.getArchivalId());
                continue;
            }

            System.out.println("---旧档号---" + volume.getArchivalId() + "----" + end_time);
            Volume updateVolume = volumeRepository.findByVolumeIdAndUpdateJsonMetadata(volume.getObjectId().getId(), volume.getJsonMetadata());
            updateVolume.update();
            updateVolume.setString("end_time", end_time);
            updateVolume.setString("start_time", start_time);
            fixedThreadPool.execute(() -> {
                volumeRepository.store(updateVolume);
                a.getAndSet(a.get() - 1);
                System.out.println(a);
            });
        }
        System.out.println("------修改完成----");
//       }
//       Record updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), documentContext.jsonString());
//       //修改
//       updateRecord.update();
//       //刷新json
//       updateRecord.updateJsonMetadata(documentContext.jsonString());
    }

    public void updateTimeStrYL(String path) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_object_path", path).eq("archive_type","da_volume").like("start_time","T");
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 6000)).getRecords();
        AtomicReference<Integer> a = new AtomicReference<>(list.size());
        for (SysObjectDO sysObjectDO : list) {
            String id = sysObjectDO.getId();
            Volume volume = volumeRepository.find(id,false);
            String start_time;
            String end_time;
            try{
                start_time  = readJson(volume.getJsonMetadata(), "start_time");
                end_time  = readJson(volume.getJsonMetadata(), "end_time");
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(volume.getArchivalId());
                continue;
            }

            System.out.println("---旧档号---" + volume.getArchivalId() + "----" + end_time);
            Volume updateVolume = volumeRepository.findByVolumeIdAndUpdateJsonMetadata(volume.getObjectId().getId(), volume.getJsonMetadata());
            updateVolume.update();
            updateVolume.setString("end_time", end_time);
            updateVolume.setString("start_time", start_time);
            fixedThreadPool.execute(() -> {
                volumeRepository.store(updateVolume);
                a.getAndSet(a.get() - 1);
                System.out.println(a);
            });
        }
        System.out.println("------修改完成----");
//       }
//       Record updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), documentContext.jsonString());
//       //修改
//       updateRecord.update();
//       //刷新json
//       updateRecord.updateJsonMetadata(documentContext.jsonString());
    }

    public static String readJson(String json, String key) {
        JSONArray val = JsonPath.read(json, "$.record..[?(@.name == '" + key + "')].content", new Predicate[0]);
        if(val.size() == 0){
            return "";
        }
        String returnStr = val.get(0).toString();
        return returnStr;
    }

    public  DocumentContext updateJsonName(String jsonMetadata, Map<String, Object> map) {
        DocumentContext parse = JsonPath.parse(jsonMetadata);
        for (String s : map.keySet()) {
            parse.set("$.record..[?(@.title == '" + s + "')].name", map.get(s));
        }
        return parse;
    }

    public DocumentContext updateJson(String jsonMetadata, Map<String, Object> map) {
        DocumentContext parse = JsonPath.parse(jsonMetadata);
        for (String s : map.keySet()) {
            parse.put("$.record..[?(@.name == '" + s + "')]", "content", map.get(s));
        }
        return parse;
    }


    public static void main(String[] args) throws DocumentException {
        String sdsds = "{\"record\":{\"version_no\":\"1\",\"metadata_scheme_name\":\"文书档案（卷内）\",\"block\":[{\"name\":\"归档信息\",\"block\":[{\"name\":\"资源标识\",\"property\":[{\"name\":\"archival_id\",\"title\":\"档号\",\"content\":\"038-002-022-060\"},{\"name\":\"doc_number\",\"title\":\"文件编号\"},{\"name\":\"fonds_id\",\"title\":\"全宗号\",\"content\":\"J038\"},{\"name\":\"catalog_code\",\"title\":\"目录号\",\"content\":\"002\"},{\"name\":\"volume_id\",\"title\":\"案卷号\",\"content\":\"022\"},{\"name\":\"item_code\",\"title\":\"卷内顺序号\",\"content\":\"27\"},{\"name\":\"archive_1st_category\",\"title\":\"一级门类编码\",\"content\":\"WS\"},{\"name\":\"category_code\",\"title\":\"门类编码\",\"content\":\"WS·B\"},{\"name\":\"classification_number\",\"title\":\"分类号\"},{\"name\":\"DAGDM\",\"title\":\"档案馆代码\"}]},{\"name\":\"访问控制信息\",\"property\":[{\"name\":\"security_class\",\"title\":\"密级\",\"content\":\"L4\"},{\"name\":\"open_class\",\"title\":\"开放状态\"}]},{\"name\":\"说明信息\",\"property\":[{\"name\":\"retention_period\",\"title\":\"保管期限\",\"content\":\"C\"},{\"name\":\"fonds_name\",\"title\":\"全宗名称\"},{\"name\":\"original_status\",\"title\":\"原件状态\"},{\"name\":\"remark\",\"title\":\"备注\",\"content\":\"敏感信息已转移\"},{\"name\":\"job_no\",\"title\":\"工号\"},{\"name\":\"file_year\",\"title\":\"年度\",\"content\":\"0\"},{\"name\":\"filed_by\",\"title\":\"归档人\"},{\"name\":\"archives_num\",\"title\":\"归档份数\",\"content\":\"1\"},{\"name\":\"filed_date\",\"title\":\"归档日期\"},{\"name\":\"file_department\",\"title\":\"归档部门\"},{\"name\":\"doc_date\",\"title\":\"形成日期\",\"content\":\"0000-00-00\"},{\"name\":\"description\",\"title\":\"描述\"},{\"name\":\"digitized_status\",\"title\":\"数字化状态\"},{\"name\":\"owner\",\"title\":\"文件所有者\"},{\"name\":\"exist_document\",\"title\":\"是否有原文\",\"content\":\"false\"},{\"name\":\"author\",\"title\":\"责任者\"},{\"name\":\"material_num\",\"title\":\"载体数量\"},{\"name\":\"carrier_type\",\"title\":\"载体类型\",\"content\":\"02\"},{\"name\":\"doc_attachments\",\"title\":\"附件名称\"},{\"name\":\"page_num\",\"title\":\"页号\"},{\"name\":\"doc_pages\",\"title\":\"页数\"},{\"name\":\"title\",\"title\":\"题名\",\"content\":\"038-002-022-060\"}]},{\"name\":\"管理信息\"}]},{\"name\":\"业务内容\",\"block\":[{\"name\":\"过程信息\"},{\"name\":\"内容信息\",\"property\":[{\"name\":\"BSMTX\",\"title\":\"不扫描图像\"},{\"name\":\"ZTC\",\"title\":\"主题词\"},{\"name\":\"QWBS\",\"title\":\"全文标识\"},{\"name\":\"GJC\",\"title\":\"关键词\"},{\"name\":\"KZF\",\"title\":\"控制符\"},{\"name\":\"WJSSRM\",\"title\":\"文件所涉人名\"},{\"name\":\"HH\",\"title\":\"盒号\"},{\"name\":\"GB\",\"title\":\"稿本\"},{\"name\":\"ZTGG\",\"title\":\"载体规格\"},{\"name\":\"SXH\",\"title\":\"顺序号\"},{\"name\":\"APPRAISAL_STATUS_XH\",\"title\":\"销毁鉴定状态\"},{\"name\":\"SHELVES_STATUS\",\"title\":\"上架状态\"},{\"name\":\"ITEM_COUNT\",\"title\":\"电子全文数\"},{\"name\":\"PDF_PAGES\",\"title\":\"PDF页数\"},{\"name\":\"STARAGE_PLACE\",\"title\":\"档案存址\"},{\"name\":\"ENTRY_STATUS\",\"title\":\"进馆状态\"},{\"name\":\"ZZYM\",\"title\":\"终止页码\"},{\"name\":\"DATA_SOURCE\",\"title\":\"数据来源\",\"content\":\"2\"},{\"name\":\"physical_archive_location\",\"title\":\"档案存址\",\"content\":\"001-001-006B-004-001\"},{\"name\":\"ZTLX\",\"title\":\"专题类型\"},{\"name\":\"FZ\",\"title\":\"附注\"}]}]},{\"name\":\"电子文件\"}],\"metadata_scheme_code\":\"WS·B-ITEM\"}}";
        System.out.println(repairDocDate(sdsds));
    }
}
