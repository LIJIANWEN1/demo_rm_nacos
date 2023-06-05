package com.example.demo1_nacos.service;
import cn.amberdata.cache.CacheTemplate;
import cn.amberdata.common.contenttransfer.ContentTransfer;
import cn.amberdata.common.util.excel.old.ExcelUtils;
import cn.amberdata.common.util.httpclient.HttpClientUtil;
import cn.amberdata.common.util.zip.CompressUtils;
import cn.amberdata.common.util.zip.ZipFileUtil;
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
import com.example.demo1_nacos.pojo.YC.CertificatePO;
import com.example.demo1_nacos.pojo.YC.OrderInfo;
import com.example.demo1_nacos.pojo.YC.YCResult;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import io.minio.MinioClient;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
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
                System.out.println("---旧档号---" + record.getArchivalId() + "-----" + third_class_code);
            });
            System.out.println("------修改完成----");
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

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);

    public void addWSBlock(String objectType) {
        List<MetadataColumn> columns = metadataColumnRepository.getMetadataColumnByMetadataSchemeId("d5172839-ae59-4a42-8521-c5873ee9ec6f");
        System.out.println(columns);
//        SessionContext.setSession(SessionUtil.getAdminSession());

//        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("s_object_type",objectType).isNull("update_doc_date_status");
//        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 120000)).getRecords();
////        List<List<SysObjectDO>> partitionList = Lists.partition(list, 5000);
////        partitionList.forEach(o -> {
////            System.out.println(o);
////        });
//        AtomicReference<Integer> a = new AtomicReference<>(list.size());
//        for (SysObjectDO sysObjectDO : list) {
//            Volume volume = null;
//            try {
//                volume = volumeRepository.find(sysObjectDO.getId(),false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if(null == volume){
//                continue;
//            }
//            Map<String,Object> map = new HashMap<>();
//            map.put("载体规格","ztgg");
//            String newJsonStr = updateJsonName(volume.getJsonMetadata(),map).jsonString();
//            if(StringUtils.isBlank(newJsonStr)){
//                continue;
//            }
//            Volume updateRecord = null;
//            try {
//                updateRecord = volumeRepository.findByVolumeIdAndUpdateJsonMetadata(volume.getObjectId().getId(), newJsonStr);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            if(null == updateRecord){
//                continue;
//            }
//            updateRecord.update();
//            updateRecord.setString("update_doc_date_status","01");
//            updateRecord.updateJsonMetadata(newJsonStr);
//            Volume finalUpdateRecord = updateRecord;
//            fixedThreadPool.execute(() -> {
//                volumeRepository.store(finalUpdateRecord);
//                a.getAndSet(a.get() - 1);
//                System.out.println(a);
//            });
//        }
//        System.out.println("------修改完成----");
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
                continue;
            }
            String newJsonStr = repairDocDate(record.getJsonMetadata());
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
            updateRecord.setString("update_doc_date_status","01");
            updateRecord.updateJsonMetadata(newJsonStr);
            Record finalUpdateRecord = updateRecord;
            fixedThreadPool.execute(() -> {
                recordRepository.store(finalUpdateRecord);
                a.getAndSet(a.get() - 1);
                    System.out.println(a);
            });
        }
        System.out.println("------修改完成----");
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

    private String repairDocDate(String aa){
        DocumentContext parse = JsonPath.parse(aa);
        String doc_date = readJson(aa,"doc_date");
        String xcsj = readJson(aa,"XCSJ");
        if (!StringUtils.isBlank(xcsj)) {
            return "";
        }
        System.out.println(xcsj);
        List<Map<String,String>> a= parse.read("$.record..[?(@.name == '业务信息')].property");
        if(a.size()==0){
            return "";
        }
        List<Map<String,String>> list = (List<Map<String, String>>) a.get(0);
        Map<String,String> val = new HashMap<>();
        val.put("name","XCSJ");
        val.put("title","文件形成时间");
        val.put("content",doc_date);
        list.add(val);
        parse.set("$.record..[?(@.name == '业务信息')].property",list);
        return  parse.jsonString();
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
        String aa = "{\"record\":{\"version_no\":\"2\",\"metadata_scheme_name\":\"文书档案（卷）\",\"block\":[{\"name\":\"归档信息\",\"block\":[{\"name\":\"资源标识\",\"property\":[{\"name\":\"archive_1st_category\",\"title\":\"一级门类编码\",\"content\":\"WS\"},{\"name\":\"fonds_id\",\"title\":\"全宗号\",\"content\":\"0014\"},{\"name\":\"classification\",\"title\":\"分类号\",\"content\":\"014\"},{\"name\":\"volume_id\",\"title\":\"案卷号\",\"content\":\"002\"},{\"name\":\"archival_id\",\"title\":\"档号\",\"content\":\"0014-2005-002\"},{\"name\":\"catalog_code\",\"title\":\"目录号\"},{\"name\":\"category_code\",\"title\":\"门类编码\",\"content\":\"WS·B\"},{\"name\":\"GDM\",\"title\":\"档案馆代码\"}]},{\"name\":\"访问控制信息\",\"property\":[{\"name\":\"security_class\",\"title\":\"密级\"},{\"name\":\"open_class\",\"title\":\"开放状态\"},{\"name\":\"secrecy_period\",\"title\":\"保密期限\"},{\"name\":\"released_network\",\"title\":\"发布网段\"},{\"name\":\"decryption_status\",\"title\":\"解密标识\"}]},{\"name\":\"说明信息\",\"property\":[{\"name\":\"subject\",\"title\":\"主题词\"},{\"name\":\"retention_period\",\"title\":\"保管期限\",\"content\":\"Y\"},{\"name\":\"fonds_name\",\"title\":\"全宗名称\"},{\"name\":\"GJC\",\"title\":\"关键词\"},{\"name\":\"remark\",\"title\":\"备注\"},{\"name\":\"job_no\",\"title\":\"工号\"},{\"name\":\"file_year\",\"title\":\"年度\",\"content\":\"2005\"},{\"name\":\"filed_by\",\"title\":\"归档人\"},{\"name\":\"archives_num\",\"title\":\"归档份数\",\"content\":\"1\"},{\"name\":\"filed_date\",\"title\":\"归档日期\"},{\"name\":\"file_department\",\"title\":\"归档部门\"},{\"name\":\"title\",\"title\":\"题名\",\"content\":\"县人大常委会、办公室关于2005年度审议议题的决定、意见、通知\"},{\"name\":\"checked_by\",\"title\":\"检查人\"},{\"name\":\"checked_date\",\"title\":\"检查日期\"},{\"name\":\"file_end_date\",\"title\":\"终止日期\",\"content\":\"2005-12-01T00:00\"},{\"name\":\"binding_method\",\"title\":\"装订方式\"},{\"name\":\"author\",\"title\":\"责任者\",\"content\":\"龙游县人民代表大会常务委员会\"},{\"name\":\"file_start_date\",\"title\":\"起始日期\",\"content\":\"2005-01-01T00:00\"},{\"name\":\"material_num\",\"title\":\"载体数量\"},{\"name\":\"carrier_type\",\"title\":\"载体类型\",\"content\":\"02\"},{\"name\":\"annotation\",\"title\":\"附注\"},{\"name\":\"doc_pages\",\"title\":\"页数\",\"content\":\"87\"},{\"name\":\"documents_in_volume\",\"title\":\"卷内文件份数\",\"content\":\"16\"},{\"name\":\"ZTGG\",\"title\":\"载体规格\"},{\"name\":\"MLJBF\",\"title\":\"目录级别符\"}]},{\"name\":\"管理信息\"}]},{\"name\":\"业务内容\",\"block\":{\"name\":\"过程信息\"}},{\"name\":\"电子文件\"}],\"metadata_scheme_code\":\"WS·B-VOLUME\"}}";
        Map<String,Object> map = new HashMap<>();
        map.put("载体规格","ztgg");
//        DocumentContext parse = updateJsonName(aa,map);
//        System.out.println(parse.jsonString());
    }
}
