package com.example.demo1_nacos.service;
import cn.amberdata.afc.api.object.IAfPersistentObject;
import cn.amberdata.afc.api.object.IAfSysObject;
import cn.amberdata.afc.api.object.MetadataDataCacheManager;
import cn.amberdata.afc.common.exception.AfErrorCodeEnum;
import cn.amberdata.afc.common.exception.AfNotSupportNodeTypeException;
import cn.amberdata.afc.common.util.DataTypeCalibration;
import cn.amberdata.afc.repository.RepositoryConfig;
import cn.amberdata.dm.common.context.session.SessionContext;
import cn.amberdata.dm.common.context.unit.UnitContext;
import cn.amberdata.dm.common.context.user.UserContext;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.dm.session.SessionUtil;
import cn.amberdata.dm.sysobject.SysObjectDO;
import cn.amberdata.dm.sysobject.SysObjectRepository;
import cn.amberdata.dm.sysobject.SysObjectService;
import cn.amberdata.dm.sysobject.mapper.SysObjectMapper;
import cn.amberdata.dm.user.User;
import cn.amberdata.dm.user.UserRepository;
import cn.amberdata.metadata.facade.dto.PropertyDTO;
import cn.amberdata.rm.archive.AbstractArchive;
import cn.amberdata.rm.archive.domain.attributemappingscheme.AttributeMappingScheme;
import cn.amberdata.rm.archive.record.Record;
import cn.amberdata.rm.archive.record.RecordRepository;
import cn.amberdata.rm.archive.record.mapper.RecordMapper;
import cn.amberdata.rm.archive.valueobject.CollectionWay;
import cn.amberdata.rm.archive.volume.Volume;
import cn.amberdata.rm.archive.volume.VolumeRepository;
import cn.amberdata.rm.archive.volume.mapper.VolumeMapper;
import cn.amberdata.rm.archive.volume.valueobject.BindingMethodStatus;
import cn.amberdata.rm.classification.*;
import cn.amberdata.rm.common.exception.ExceptionCode;
import cn.amberdata.rm.common.log.LogUtil;
import cn.amberdata.rm.common.util.importdata.SelectUtil;
import cn.amberdata.rm.metadata.MetadataService;
import cn.amberdata.rm.metadata.info.MetadataSchemeInfoRepository;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItemRepository;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumn;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumnRepository;
import cn.amberdata.rm.metadata.template.TemplateMetadata;
import cn.amberdata.rm.metadata.template.TemplateMetadataRepository;
import cn.amberdata.rm.settings.strategy.RetentionStrategyRepository;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1_nacos.vo.ImportArchivePackageVO;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import net.minidev.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public class RmArchiveServiceImpl {

    private static final String FILE_YEAR = "file_year";

    private static final String ARCHIVAL_ID = "archival_id";

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private VolumeMapper volumeMapper;

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private VolumeRepository volumeRepository;

    @Resource
    private SysObjectMapper sysObjectMapper;

    @Resource
    private MetadataColumnRepository metadataColumnRepository;

    @Resource
    private SubCategoryRepository subCategoryRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private TemplateMetadataRepository templateMetadataRepository;

    @Resource
    private MetadataCodeItemRepository metadataCodeItemRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private MetadataSchemeInfoRepository metadataSchemeInfoRepository;

    @Resource
    private MetadataService metadataService;

    @Resource
    private RetentionStrategyRepository retentionStrategyRepository;

    @Resource
    private FolderRepository folderRepository;

    @Resource
    private RmOtherServiceImpl rmOtherService;

    private static Map<String,String> retentionPeriod = new HashMap<>();
    private static Map<String,String> swType = new HashMap<>();
    private final MetadataDataCacheManager metadataDataCacheManager = MetadataDataCacheManager.Inner.get();

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);

    public List<String> importRecordFromExcel(String metadataSchemeId,String collectionWay,
                                              ImportArchivePackageVO importArchivePackageVO) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        User user = userRepository.findByUserLoginName(RepositoryConfig.adminUsername);

        //获取元数据方案绑定的TypeName，因为如果这里是导入卷内的话，那么ImportDataCommand上传参的TypeName是案卷的并不是卷内的，这里要重新查询一次
        String typeName = metadataSchemeInfoRepository.getMetadataSchemeInfoByIdOrCode(metadataSchemeId, null).getTypeName();
        //获取列配置
        List<MetadataColumn> metadataColumnList = metadataColumnRepository.getMetadataColumnByMetadataSchemeId(metadataSchemeId);
        //获取数据模板
        TemplateMetadata templateMetadata = templateMetadataRepository.findTemplateByMetadataSchemeIdAndVersionNo(metadataSchemeId, null);
        //根据数据模板id获取空的json表单模板
        String jsonMetadataTemplate = templateMetadataRepository.getJsonTemplateById(templateMetadata.getId());
        //获取需要转换的元数据列
        List<String> needCreatePhysicalArchiveIdList = new ArrayList<>();
        //定义导入完成的档案总数
        List<Map<String, String>> dataList = importArchivePackageVO.getDataList();
        for (int i = 0; i < dataList.size(); ++i) {
            Map<String, String> singleMap = dataList.get(i);
            String archivalId = singleMap.get(ARCHIVAL_ID);
//            List<String> objectIds = recordMapper.findByArchivalId(archivalId);
            List<String> objectIds = null;
            if (CollectionUtils.isNotEmpty(objectIds)) {
                objectIds.forEach(id -> recordRepository.destroy(recordRepository.find(id)));
                System.out.println("-----删除："+archivalId);
//                System.out.println("----存在此档号："+archivalId); continue;
            }
            //获取类目
            Folder folder = folderRepository.findByPath(importArchivePackageVO.getParentPath());
            //获取类目
            SubCategory subCategory = rmOtherService.getSubCategoryByClassRule(importArchivePackageVO.getClassRule(),importArchivePackageVO.getParentPath(),singleMap,folder.getObjectId().getId());
            String parentId = subCategory.getObjectId().getId();
            //获取门类
            Category category = categoryRepository.find(subCategory.getCategoryId());
            //转换标准代码项
            convertMapRetentionPeriod(singleMap);
            convertMapSWType(singleMap);
            //每次循环初始化掉数据模避免造成数据污染
            DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
            Integer volumeArchivesNum = 0;
            try {
                //如果整理方式为volume的话，代表导入的是卷内，需要根据档号值去匹配对应的案卷id
                if (CollectionWay.COLLECTION_WAY_VOLUME.asValue().equals(collectionWay)) {
                    //卷内的档号截取掉后边的流水号则是对应的案卷档号值
                    String volumeArchivalId = archivalId.substring(0, archivalId.lastIndexOf("-"));
                    //根据parentId（导入位置的id）查询档号为该值的案卷
                    List<String> volumeIdByFilter = volumeMapper.getVolumeIdByFilter(parentId, AbstractArchive.ARCHIVAL_ID, volumeArchivalId);
                    //将匹配到的唯一案卷id重新作为卷内要导入的父id
                    parentId = volumeIdByFilter.get(0);
//                    volumeArchivesNum = volumeRepository.find(parentId).getArchivesNum();
                }
                //构建record
                Record record = getRecord(parentId, typeName, category, metadataSchemeId, documentContext, singleMap, metadataColumnList, collectionWay, Long.valueOf(templateMetadata.getVersionNo()));
                //异常情况返回null，已经记录失败数据，这里跳过本次循环
                if (record == null) {
                    continue;
                }
                //获取档案排序号
                Long sortNumber = generateSortNumber(parentId, typeName);
                //导入到档案库做快速著录行为
                record.description(sortNumber, user.getObjectId().getId(), user.getUserLoginName(), volumeArchivesNum, importArchivePackageVO.getUnitCode());
                //指定分类
                record.updateClassification(subCategory.getClassificationCode(), subCategory.getObjectId().getId());
                //快速著录行为
                record.descriptionFast(retentionStrategyRepository.find(subCategory.getRetentionPolicyId()));
                //覆盖更新json表单数据
                record.updateJsonForm(null, metadataColumnList);
                //进行数据模板校验
                List<String> errorMessageList = new ArrayList<>();
                verifyMetadata(record.getJsonMetadata(), typeName, templateMetadata.getVersionNo(), errorMessageList);
                if(errorMessageList.size()>0){
                    throw new RuntimeException(StringUtils.join(errorMessageList, ","));
                }
                //持久化
                int finalI = i;
                fixedThreadPool.execute(() -> {
                    recordRepository.store(record);
                    System.out.println("-------"+(finalI +1)+"/"+dataList.size()+"   "+archivalId);
                });

            } catch (Exception e) {
                //其他一些未知异常，直接放入失败数据集合中
                System.out.println("问题档号：--------------------"+archivalId);
                LogUtil.error("importRecordFromExcel失败", e);
            }
        }
        return needCreatePhysicalArchiveIdList;
    }

    public List<String> importVolumeFromExcel(String metadataSchemeId,String collectionWay,
                                              ImportArchivePackageVO importArchivePackageVO) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        User user = userRepository.findByUserLoginName(RepositoryConfig.adminUsername);
        //获取元数据方案绑定的TypeName，因为如果这里是导入卷内的话，那么ImportDataCommand上传参的TypeName是案卷的并不是卷内的，这里要重新查询一次
        String typeName = metadataSchemeInfoRepository.getMetadataSchemeInfoByIdOrCode(metadataSchemeId, null).getTypeName();
        //获取列配置
        List<MetadataColumn> metadataColumnList = metadataColumnRepository.getMetadataColumnByMetadataSchemeId(metadataSchemeId);
        //获取数据模板
        TemplateMetadata templateMetadata = templateMetadataRepository.findTemplateByMetadataSchemeIdAndVersionNo(metadataSchemeId, null);
        //根据数据模板id获取空的json表单模板
        String jsonMetadataTemplate = templateMetadataRepository.getJsonTemplateById(templateMetadata.getId());
        //获取需要转换的元数据列
        List<String> needCreatePhysicalArchiveIdList = new ArrayList<>();
        //定义导入完成的档案总数
        List<Map<String, String>> dataList = importArchivePackageVO.getDataList();
        for (int i = 0; i < dataList.size(); ++i) {
            Map<String, String> singleMap = dataList.get(i);
            //转换标准代码项
            convertMapRetentionPeriod(singleMap);
            convertMapSWType(singleMap);
            String archivalId = singleMap.get(ARCHIVAL_ID);
//            List<String> objectIds = volumeMapper.findByArchivalId(archivalId);
            List<String> objectIds = null;
            if (CollectionUtils.isNotEmpty(objectIds)) {
//                objectIds.forEach(id -> volumeRepository.destroy(volumeRepository.find(id)));
                System.out.println("-----删除："+archivalId);
//                System.out.println("----存在此档号："+archivalId);  continue;

            }
            //获取类目
            Folder folder = folderRepository.findByPath(importArchivePackageVO.getParentPath());
            //获取类目
            SubCategory subCategory = rmOtherService.getSubCategoryByClassRule(importArchivePackageVO.getClassRule(),importArchivePackageVO.getParentPath(),singleMap,folder.getObjectId().getId());
            String parentId = subCategory.getObjectId().getId();
            //获取门类
            Category category = categoryRepository.find(subCategory.getCategoryId());
            //每次循环初始化掉数据模避免造成数据污染
            DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
            try {
                //构建volume
                Volume volume = getVolume(parentId, typeName, category, metadataSchemeId, documentContext, singleMap, metadataColumnList, collectionWay, Long.valueOf(templateMetadata.getVersionNo()));
                //异常情况返回null，已经记录失败数据，这里跳过本次循环
                if (volume == null) {
                    continue;
                }
                //进行数据模板校验
                List<String> errorMessageList = new ArrayList<>();
                verifyMetadata(volume.getJsonMetadata(), typeName, templateMetadata.getVersionNo(), errorMessageList);
                if(errorMessageList.size()>0){
                    throw new RuntimeException(StringUtils.join(errorMessageList, ","));
                }
                //获取档案排序号
                Long sortNumber = generateSortNumber(parentId, typeName);
                //导入到档案库做快速著录行为
                volume.description(sortNumber, user.getObjectId().getId(), user.getUserLoginName(),importArchivePackageVO.getUnitCode());
                //指定分类
                volume.updateClassification(subCategory.getClassificationCode(), subCategory.getObjectId().getId());
                //快速著录行为
                volume.descriptionFast(retentionStrategyRepository.find(subCategory.getRetentionPolicyId()));
                //覆盖更新json表单数据
                volume.updateJsonForm(null, metadataColumnList);
                //持久化
                volumeRepository.store(volume);
                System.out.println("-------"+(i+1)+"/"+dataList.size()+"   "+archivalId);
            } catch (Exception e) {
                //其他一些未知异常，直接放入失败数据集合中
                LogUtil.error("importVolumeFromExcel失败", e);
            }
        }
        return needCreatePhysicalArchiveIdList;
    }

    public List<String> importDocuments(String metadataSchemeId,String collectionWay,
                                              ImportArchivePackageVO importArchivePackageVO) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        User user = userRepository.findByUserLoginName(RepositoryConfig.adminUsername);
        //获取元数据方案绑定的TypeName，因为如果这里是导入卷内的话，那么ImportDataCommand上传参的TypeName是案卷的并不是卷内的，这里要重新查询一次
        String typeName = metadataSchemeInfoRepository.getMetadataSchemeInfoByIdOrCode(metadataSchemeId, null).getTypeName();
        //获取列配置
        List<MetadataColumn> metadataColumnList = metadataColumnRepository.getMetadataColumnByMetadataSchemeId(metadataSchemeId);
        //获取数据模板
        TemplateMetadata templateMetadata = templateMetadataRepository.findTemplateByMetadataSchemeIdAndVersionNo(metadataSchemeId, null);
        //根据数据模板id获取空的json表单模板
        String jsonMetadataTemplate = templateMetadataRepository.getJsonTemplateById(templateMetadata.getId());
        //获取需要转换的元数据列
        List<String> needCreatePhysicalArchiveIdList = new ArrayList<>();
        //定义导入完成的档案总数
        List<Map<String, String>> dataList = importArchivePackageVO.getDataList();
        for (int i = 0; i < dataList.size(); ++i) {
            Map<String, String> singleMap = dataList.get(i);
            //转换标准代码项
            convertMapRetentionPeriod(singleMap);
            convertMapSWType(singleMap);
            String archivalId = singleMap.get(ARCHIVAL_ID);
//            List<String> objectIds = volumeMapper.findByArchivalId(archivalId);
            List<String> objectIds =  null;
            if (CollectionUtils.isNotEmpty(objectIds)) {
//                objectIds.forEach(id -> volumeRepository.destroy(volumeRepository.find(id)));
                System.out.println("-----删除："+archivalId);
//                System.out.println("----存在此档号："+archivalId);  continue;

            }
            //获取类目
            Folder folder = folderRepository.findByPath(importArchivePackageVO.getParentPath());
            //获取类目
            SubCategory subCategory = rmOtherService.getSubCategoryByClassRule(importArchivePackageVO.getClassRule(),importArchivePackageVO.getParentPath(),singleMap,folder.getObjectId().getId());
            String parentId = subCategory.getObjectId().getId();
            //获取门类
            Category category = categoryRepository.find(subCategory.getCategoryId());
            //每次循环初始化掉数据模避免造成数据污染
            DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
            try {
                //构建volume
                Volume volume = getVolume(parentId, typeName, category, metadataSchemeId, documentContext, singleMap, metadataColumnList, collectionWay, Long.valueOf(templateMetadata.getVersionNo()));
                //异常情况返回null，已经记录失败数据，这里跳过本次循环
                if (volume == null) {
                    continue;
                }
                //进行数据模板校验
                List<String> errorMessageList = new ArrayList<>();
                verifyMetadata(volume.getJsonMetadata(), typeName, templateMetadata.getVersionNo(), errorMessageList);
                if(errorMessageList.size()>0){
                    throw new RuntimeException(StringUtils.join(errorMessageList, ","));
                }
                //获取档案排序号
                Long sortNumber = generateSortNumber(parentId, typeName);
                //导入到档案库做快速著录行为
                volume.description(sortNumber, user.getObjectId().getId(), user.getUserLoginName(),importArchivePackageVO.getUnitCode());
                //指定分类
                volume.updateClassification(subCategory.getClassificationCode(), subCategory.getObjectId().getId());
                //快速著录行为
                volume.descriptionFast(retentionStrategyRepository.find(subCategory.getRetentionPolicyId()));
                //覆盖更新json表单数据
                volume.updateJsonForm(null, metadataColumnList);
                //持久化
                volumeRepository.store(volume);
                System.out.println("-------"+(i+1)+"/"+dataList.size()+"   "+archivalId);
            } catch (Exception e) {
                //其他一些未知异常，直接放入失败数据集合中
                LogUtil.error("importVolumeFromExcel失败", e);
            }
        }
        return needCreatePhysicalArchiveIdList;
    }


    private Record getRecord(String parentId, String typeName, Category category, String metadataSchemeId,
                             DocumentContext documentContext, Map<String, String> map, List<MetadataColumn> metadataColumns, String collectionWay, Long version) {
        Record record;
        //如果代码执行到这里，代表要么是覆盖模式但没匹配到档案，要么是不覆盖导入。两种情况都直接进行新增操作
        saveJsonData(documentContext, map, metadataColumns);
        //构建档案
        record = recordRepository.newEntity(
                typeName, parentId,
                collectionWay, metadataSchemeId,
                version, category.getCode(),
                category.getObjectId().getId(), "", documentContext.jsonString());
        return record;
    }

    private Volume getVolume(String parentId, String typeName, Category category, String metadataSchemeId,
                             DocumentContext documentContext, Map<String, String> map, List<MetadataColumn> metadataColumns, String collectionWay, Long version) {
        Volume volume;
        //如果代码执行到这里，代表要么是覆盖模式但没匹配到档案，要么是不覆盖导入。两种情况都直接进行新增操作
        saveJsonData(documentContext, map, metadataColumns);
        //构建档案
        volume = volumeRepository.newEntity(
                typeName, parentId,
                collectionWay , category.getCode(), metadataSchemeId,
                category.getObjectId().getId(), "" ,
                BindingMethodStatus.BINDING_METHOD_COMPLETE.asValue(), documentContext.jsonString(),version);
        return volume;
    }


    public String saveJsonData(DocumentContext jsonPath, Map<String, String> map, List<MetadataColumn> metadataColumns) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < metadataColumns.size(); ++j) {
            MetadataColumn metadataColumn = metadataColumns.get(j);
            String allPath = metadataColumn.getJsonPath();
            //json 元数据name，元数据code不能设置为空
            if ("$.record.metadata_scheme_code".equals(allPath) || "$.record.metadata_scheme_name".equals(allPath) || "$.record.version_no".equals(allPath)) {
                continue;
            }
            Object value = map.get(metadataColumn.getAttrName());
            String columnType = metadataColumn.getType();
            String metadataColumnDisplayName = metadataColumn.getDisplayName();
            String typeFormat = metadataColumn.getTypeFormat();
            String[] format = null;
            if (StringUtils.isNotEmpty(typeFormat)) {
                format = new String[]{typeFormat};
            }
            if (StringUtils.isNotBlank(allPath)) {
                String key = allPath.substring(allPath.lastIndexOf(".") + 1);
                String path = allPath.substring(0, allPath.lastIndexOf("."));
                try {
                    if (columnType != null && !SelectUtil.TYPE_STRING.equals(columnType) && null != value && StringUtils.isNotBlank(value.toString())) {
                        value = SelectUtil.getValueFromJsonStr(value.toString(), metadataColumnDisplayName, columnType, format);
                    }
                    if (null != value && StringUtils.isNotBlank(value.toString())) {
                        jsonPath.put(path, key, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }


    public void updateArchivalId(String path) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_object_path", path).eq("archive_type","da_record");
        List<SysObjectDO> list = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 1000)).getRecords();
        for (SysObjectDO sysObjectDO : list) {
            String id = sysObjectDO.getId();
            Record record = recordRepository.find(id);
            String third_class_code = record.getString("third_class_code");
//            String file_year = readJson(record.getJsonMetadata(), "file_year");
//            String first_class_code = readJson(record.getJsonMetadata(), "first_class_code");
//            String second_class_code = readJson(record.getJsonMetadata(), "second_class_code");
//            String third_class_code = readJson(record.getJsonMetadata(), "third_class_code");
//            String oldArchivalId = record.getArchivalId();
//            String newArchivalId = file_year.concat(".").concat(first_class_code).concat(".").concat(second_class_code).
//                    concat(".").concat(third_class_code).concat(oldArchivalId.substring(oldArchivalId.indexOf("-")));
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
//       Record updateRecord = recordRepository.findByIdAndUpdateJsonMetadata(record.getObjectId().getId(), documentContext.jsonString());
//       //修改
//       updateRecord.update();
//       //刷新json
//       updateRecord.updateJsonMetadata(documentContext.jsonString());
    }



    public DocumentContext updateJson(String jsonMetadata, Map<String, Object> map) {
        DocumentContext parse = JsonPath.parse(jsonMetadata);
        for (String s : map.keySet()) {
            parse.put("$.record..[?(@.name == '" + s + "')]", "content", map.get(s));
        }
        return parse;
    }

    public void verifyMetadata(String metadataJson, String typeName, String versionNo, List<String> errorMessageList) {
        Map<String, PropertyDTO> pMap = metadataDataCacheManager.getTemplatePropertyMappingFromCache(typeName, versionNo);
        List<Object> propertyMp = JsonPath.parse(metadataJson).read("$..property");
        for (Object o : propertyMp) {
            if (o instanceof LinkedHashMap) {
                LinkedHashMap<String, String> mp = (LinkedHashMap) o;
                validateMetadataProperty(pMap, mp.get(IAfPersistentObject.METADATA_PROPER_NAME), String.valueOf(mp.get(IAfPersistentObject.METADATA_PROPER_CONTENT)), errorMessageList);
            } else if (o instanceof ArrayList) {
                ArrayList<LinkedHashMap<String, String>> lmp = (ArrayList<LinkedHashMap<String, String>>) o;
                for (LinkedHashMap<String, String> mp : lmp) {
                    validateMetadataProperty(pMap, mp.get(IAfPersistentObject.METADATA_PROPER_NAME), String.valueOf(mp.get(IAfPersistentObject.METADATA_PROPER_CONTENT)), errorMessageList);
                }
            } else {
                // 更改异常类型
                throw new AfNotSupportNodeTypeException(AfErrorCodeEnum.ERROR_METADATA_NODE_TYPE_NOT_SUPPORT);
            }
        }
        if (pMap.size() > 0) {
            errorMessageList.add(String.format("元数据json缺少属性%s", new ArrayList<>(pMap.keySet()).toString()));
        }
    }

    private void validateMetadataProperty(Map<String, PropertyDTO> pMap, String name, String value, List<String> errorMessageList) {
        // 模板集合为空的话则不进行校验
        if (pMap == null) {
            return;
        }
        if (pMap.get(name) == null) {
            return;
        }
        boolean dataValidate = true;
        PropertyDTO validateProperty = pMap.get(name);
        if (org.apache.commons.lang.StringUtils.isNotBlank(value) && !"null".equals(value)) {
            // 校验元数据信息
            switch (validateProperty.getType()) {
                case IAfPersistentObject.INT:
                    dataValidate = DataTypeCalibration.isNumber(value);
                    break;
                case IAfPersistentObject.BOOLEAN:
                    dataValidate = DataTypeCalibration.isBool(value);
                    break;
                case IAfPersistentObject.DATE:
                    //dataValidate = DataTypeCalibration.isDate(value, validateProperty.getTypeFormat());
                    break;
                case IAfPersistentObject.STRING:
                    break;
                case IAfPersistentObject.FLOAT:
                    // 浮点数即可以为整数,也可以为浮点数
                    dataValidate = DataTypeCalibration.isNumber(value) || DataTypeCalibration.isFloat(value);
                    break;
                default:
                    errorMessageList.add(String.format("不支持元数据属性[%s]类型[%s]", validateProperty.getTitle(), validateProperty.getType()));
            }
            if (!dataValidate) {
                errorMessageList.add(String.format("元数据属性[%s]类型不正确,类型应为[%s]", validateProperty.getTitle(), validateProperty.getType()));
            }
            if (!IAfPersistentObject.DATE.equals(validateProperty.getType()) && validateProperty.getMaxLength() != null) {
                if (value.length() > validateProperty.getMaxLength()) {
                    errorMessageList.add(String.format("元数据属性[%s]长度不正确,长度应小于[%s]", validateProperty.getTitle(), validateProperty.getMaxLength()));
                }
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(validateProperty.getAllowedValuesCode())) {
                if (!validateProperty.getAllowedValues().contains(value)) {
                    errorMessageList.add(String.format("元数据属性[%s]可选值不正确,允许可选值[%s]", validateProperty.getTitle(), validateProperty.getAllowedValues()));
                }
            }
        } else {
            // 不允许为空的话则抛出异常
            if (!validateProperty.getNullAble()) {
                errorMessageList.add(String.format("缺少必填字段[%s]", validateProperty.getTitle()));
            }
        }
        pMap.remove(name);
    }

    /**
     * 校验导入档案库下业务内容
     *
     * @param abstractArchive 档案
     */
    private void validateImportArchivesBusinessContent(AbstractArchive abstractArchive) {
        //快速著录档号不能为空
        if (abstractArchive.isEmptyArchivalId()) {
//            throw new BusinessException(ExceptionCode.MSG_ARCHIVAL_IS_NOT_BLANK);
        }
    }

    private Long generateSortNumber(String parentId, String typeName) {
        Long sortNumber = recordMapper.findSortNumberByParentId(parentId, typeName);
        if (null == sortNumber || 0 == sortNumber) {
            sortNumber = 1L;
        } else {
            sortNumber += 1;
        }
        return sortNumber;
    }

    private void convertMapSWType(Map<String,String> singleMap) {
        String val = singleMap.get("swlx");
        if (StringUtils.isBlank(val)) {
            return;
        }
        if (swType.containsKey(val)) {
            singleMap.put("swlx",swType.get(val));
        }
        List<Map<String, String>> maps = metadataService.getStandardCode("实物载体类型");
        for (Map map1 : maps) {
            String standCodeVal = (String) map1.get("value");
            String standCodeName = (String) map1.get("name");
            if(val.equals(standCodeName)){
                singleMap.put("swlx",standCodeVal);
                swType.put(val,standCodeVal);
                return;
            }
        }
        throw new RuntimeException();
    }

    private void convertMapRetentionPeriod(Map<String,String> singleMap) {
        String val = singleMap.get("retention_period");
        if (StringUtils.isBlank(val)) {
            return;
        }
        if (retentionPeriod.containsKey(val)) {
            singleMap.put("retention_period",retentionPeriod.get(val));
        }
        List<Map<String, String>> maps = metadataService.getStandardCode("保管期限");
        for (Map map1 : maps) {
            String standCodeVal = (String) map1.get("value");
            String standCodeName = (String) map1.get("name");
            if(val.equals(standCodeName)){
                singleMap.put("retention_period",standCodeVal);
                retentionPeriod.put(val,standCodeVal);
                return;
            }
        }
        throw new RuntimeException();
    }

    /**
     * 获取需要转换的元数据列
     *
     * @param metadataColumnList 元数据列配置
     * @return 需要转换的元数据列配置
     */
    private List<MetadataColumn> needStandardCodeItems(List<MetadataColumn> metadataColumnList) {
        if (CollectionUtils.isEmpty(metadataColumnList)) {
//            throw new BusinessException(ExceptionCode.METADATA_COLUMN_IS_NULL);
        }
        List<MetadataColumn> returnList = new ArrayList<>();
        for (MetadataColumn metadataColumn : metadataColumnList) {
            if (StringUtils.isNotBlank(metadataColumn.getAllowedValuesCode())) {
                returnList.add(metadataColumn);
            }
        }
        return returnList;
    }

}
