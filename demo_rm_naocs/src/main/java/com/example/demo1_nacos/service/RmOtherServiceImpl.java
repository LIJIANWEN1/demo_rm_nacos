package com.example.demo1_nacos.service;
import cn.amberdata.afc.api.object.IAfPersistentObject;
import cn.amberdata.afc.api.object.IAfSysObject;
import cn.amberdata.afc.common.util.AfSessionUtils;
import cn.amberdata.dm.common.context.session.SessionContext;
import cn.amberdata.dm.common.context.unit.UnitContext;
import cn.amberdata.dm.common.domain.event.DomainEventPublisher;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.dm.organization.department.DepartmentCommand;
import cn.amberdata.dm.organization.department.DepartmentService;
import cn.amberdata.dm.session.SessionUtil;
import cn.amberdata.dm.sysobject.SysObjectDO;
import cn.amberdata.dm.sysobject.SysObjectRepository;
import cn.amberdata.dm.sysobject.SysObjectService;
import cn.amberdata.dm.sysobject.mapper.SysObjectMapper;
import cn.amberdata.metadata.facade.dto.MetadataSchemeInfoDTO;
import cn.amberdata.rm.archive.AbstractArchive;
import cn.amberdata.rm.archive.AbstractArchiveRepository;
import cn.amberdata.rm.archive.record.mapper.RecordMapper;
import cn.amberdata.rm.archive.volume.mapper.VolumeMapper;
import cn.amberdata.rm.classification.*;
import cn.amberdata.rm.classification.mapper.SubCategoryMapper;
import cn.amberdata.rm.common.exception.ExceptionCode;
import cn.amberdata.rm.metadata.category.MetadataCategory;
import cn.amberdata.rm.metadata.category.MetadataCategoryRepository;
import cn.amberdata.rm.metadata.info.MetadataSchemeInfo;
import cn.amberdata.rm.metadata.info.MetadataSchemeInfoRepository;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItem;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItemRepository;
import cn.amberdata.rm.settings.strategy.RetentionStrategy;
import cn.amberdata.rm.settings.strategy.RetentionStrategyDO;
import cn.amberdata.rm.settings.strategy.RetentionStrategyRepository;
import cn.amberdata.rm.settings.strategy.mapper.RetentionStrategyMapper;
import cn.amberdata.rm.settings.strategy.valueobject.DisposalType;
import cn.amberdata.rm.settings.strategy.valueobject.RetentionStrategyStyle;
import cn.amberdata.rm.unit.context.UnitFolderContext;
import com.aspose.slides.Collections.Specialized.CollectionsUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1_nacos.service.command.CategoryCreateCommand;
import com.example.demo1_nacos.service.command.SubCategoryCreateCommand;
import com.example.demo1_nacos.service.event.CategoryEvent;
import com.example.demo1_nacos.service.specification.CategoryCreateSpecification;
import com.example.demo1_nacos.service.specification.ClassNumberSpecification;
import com.example.demo1_nacos.service.specification.ClassWhetherOperatedSpecification;
import com.example.demo1_nacos.service.specification.SubCategoryCreateSpecification;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 17:50
 */
@Service
public class RmOtherServiceImpl {

    @Resource
    private FolderRepository folderRepository;

    @Resource
    private SubCategoryMapper subCategoryMapper;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private SysObjectRepository sysObjectRepository;

    @Resource
    private RetentionStrategyMapper retentionStrategyMapper;

    @Resource
    private VolumeMapper volumeMapper;

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private MetadataSchemeInfoRepository metadataSchemeInfoRepository;

    @Resource
    private MetadataCodeItemRepository metadataCodeItemRepository;

    @Resource
    private MetadataCategoryRepository metadataCategoryRepository;

    @Resource
    private SubCategoryRepository subCategoryRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private SysObjectMapper sysObjectMapper;

    @Resource
    private SysObjectService sysObjectService;

    @Resource
    private RetentionStrategyRepository retentionStrategyRepository;

    public String create(SubCategoryCreateCommand command) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        //TODO
//        SubCategoryDO subCategoryDO = subCategoryMapper.findByNameAndParentId(command.getName(), command.getParentId());
//        if(null != subCategoryDO){
//            return "";
//        }
        SubCategory subCategory = subCategoryRepository.newEntity(command.getParentId(), command.getName(), command.getClassNumber(), command.getRetentionPolicyId(), command.getRetentionPeriodId(), command.getDescription(),
                command.getCategoryId(), command.getClassificationCodePrefix(), false);
        new ClassNumberSpecification().isSatisfiedBy(subCategory);
        //子类目继承父类目的整理方式和门类id或继承父门类的id
        Class<? extends IAfPersistentObject> parentType = SessionContext.getSession().getAfSession().getObjectType(command.getParentId());
        if (parentType == SubCategory.class) {
            SubCategory parentSubCategory = subCategoryRepository.find(command.getParentId());
            ClassWhetherOperatedSpecification specification = new ClassWhetherOperatedSpecification();
            specification.isSatisfiedBy(parentSubCategory);
            subCategory.inheritParent(parentSubCategory.getCollectionWay().asValue(), parentSubCategory.getCategoryId());
        } else if (parentType == Category.class) {
            Category parentCategory = categoryRepository.find(command.getParentId());
            subCategory.inheritParent(parentCategory.getCollectionWay().asValue(), parentCategory.getObjectId().getId());
        }
        SubCategoryCreateSpecification specification = new SubCategoryCreateSpecification(subCategoryRepository, retentionStrategyRepository, recordMapper, volumeMapper);
        specification.isSatisfiedBy(subCategory);
        subCategoryRepository.store(subCategory);
        return subCategory.getObjectId().getId();
    }

    public String createRetentionStrategy(String unitId,String policyPath){
        String retentionStrategyId = "";
        SessionContext.setSession(SessionUtil.getAdminSession());
        RetentionStrategyDO retentionStrategyDO = retentionStrategyMapper.findBySignAndName(unitId, "保留30年后移交");
        if (null != retentionStrategyDO) {
            retentionStrategyId = retentionStrategyDO.getId();
        }else {
            RetentionStrategy retentionStrategy = retentionStrategyRepository.newEntity("保留30年后移交", unitId,
                    RetentionStrategyStyle.CUSTOM, 30, "提前-1-月提醒",
                    DisposalType.TRANSFER, 1, policyPath);
            //存储保留处置策略
            retentionStrategyRepository.store(retentionStrategy);
            retentionStrategyId = retentionStrategy.getObjectId().getId();
        }
        return retentionStrategyId;
    }
    @Resource
    private AbstractArchiveRepository abstractArchiveRepository;

    public void syncPlatformData(String parentId,String synParentId){
        SessionContext.setSession(SessionUtil.getAdminSession());
        IAfSysObject afSysObject = sysObjectRepository.find(synParentId);
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_parent_id", parentId).notLike("s_link_ids","%"+synParentId+"%").eq("archive_type","da_record");
        List<SysObjectDO> records = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 50000)).getRecords();
        for( SysObjectDO sysObjectDO:records) {
            AbstractArchive abstractArchive = abstractArchiveRepository.find(sysObjectDO.getId());
                abstractArchive.sensitiveValidatePass("xxxx");
                //检测通过，状态改为待同步
                abstractArchive.syncAwait();
            if (abstractArchive.getLinkParentPaths().contains(afSysObject.getObjectPath())) {
                //如果已经link了同步库，跳过
                continue;
            }
                abstractArchive.link(afSysObject.getObjectPath());
            fixedThreadPool.execute(() -> {
                try {
                    abstractArchiveRepository.store(abstractArchive);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("--------");
            });

        }
    }

    public void create(CategoryCreateCommand command) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        //获取本单位档案库文件夹路径
        String archiveFilePath = command.getArchivePath();
        Folder archiveFolder = folderRepository.findByPath(archiveFilePath);
        //获取元数据门类
        String[] idFroms = command.getIdFromMetadata().split(",");
        //根据元数据平台的门类，保存相应的结构，用list集合把数据放进来
        StringBuilder categoryName = new StringBuilder();
        for (int j = 0; j < idFroms.length; j++) {
            List<MetadataCategory> metadataCategoryList = metadataCategoryRepository.getIterationMetadataCategoryListById(idFroms[j]);
            //拼接父门类编码
            StringBuilder code = new StringBuilder();
            for (int i = metadataCategoryList.size() - 1; i >= 0; i--) {
                if (i > 0) {
                    code.append(metadataCategoryList.get(i).getCode()).append("·");
                } else {
                    code.append(metadataCategoryList.get(i).getCode());
                }
            }
            String codePrefix = "";
            if (code.toString().contains("·")) {
                //父门类编码
                codePrefix = code.substring(0, code.lastIndexOf("·"));
            }
            //循环里会赋值当前门类的父id,第一次为档案库的id
            String parentId = archiveFolder.getObjectId().getId();
            for (int i = metadataCategoryList.size() - 1; i >= 0; i--) {
                MetadataCategory metadataCategory = metadataCategoryList.get(i);
                //如果当前路径下已经存在相同门类，则无需创建，父id赋值为已存在门类id并跳过本次循环
                Category codeExistCategory = categoryRepository.findCategoryByCodeAndParentId(metadataCategory.getCode(), parentId);
                if (null != codeExistCategory && i != 0) {
                    parentId = codeExistCategory.getObjectId().getId();
                    continue;
                } else if (null != codeExistCategory) {
                    parentId = codeExistCategory.getObjectId().getId();
                    //拼接所有已存在的门类名称用于给出提示
                    categoryName.append(codeExistCategory.getName()).append(",");
                    //循环门类id数组最后一个值时给出提示
                    if (idFroms.length - 1 == j) {
                        String categoryNames = categoryName.substring(0, categoryName.lastIndexOf(","));
//                        throw new BusinessException(ExceptionCode.CATEGORY_ALREADY_EXIST.getCode(), String.format("[%s]已存在，不可重复添加", categoryNames));
                    }
                    continue;
                }
                Category category;
                //如果该门类为顶级节点 那么前缀设置为null
                if (metadataCategory.getTop()) {
                    category = categoryRepository.newEntity(metadataCategory.getCode(), metadataCategory.getName(),
                            metadataCategory.getCode() + "." + metadataCategory.getName(), command.getIdFromMetadata(),
                            command.getSort(), command.getWarehouseNo(), command.getCollectionWay(), null);
                } else {
                    category = categoryRepository.newEntity(metadataCategory.getCode(), metadataCategory.getName(),
                            metadataCategory.getCode() + "." + metadataCategory.getName(), command.getIdFromMetadata(),
                            command.getSort(), command.getWarehouseNo(), command.getCollectionWay(), codePrefix);
                }
                //元数据门类赋值并设置门类父路径
                category.assignValueByMetadataCategory(metadataCategory, parentId, null);
                CategoryCreateSpecification specification = new CategoryCreateSpecification(metadataCategoryRepository);
                specification.isSatisfiedBy(category);
                //最底层门类绑定库房编号
                category.bindWarehouse(command.getWarehouseNo());
                //设置单位code
                category.setUnitCode(command.getCode());
                categoryRepository.store(category);
                parentId = category.getObjectId().getId();
            }
            //标记引用元数据门类
            metadataCategoryRepository.referenceCategoryByIds(Collections.singletonList(idFroms[j]));
            //监听门类创建，叶子节点下创建初始类目
            CategoryEvent categoryEvent = new CategoryEvent();
            categoryEvent.setUnitId(command.getUnitId());
            categoryEvent.setRetentionPolicyPath(command.getRetentionPolicyPath());
            categoryEvent.setCategoryId(parentId);
            DomainEventPublisher.publish(categoryEvent);
        }
    }

    public String getFolderByPath(String path){
        SessionContext.setSession(SessionUtil.getAdminSession());
        Folder folder = folderRepository.findByPath(path);
        if(null == folder){
            return null;
        }
        return folder.getObjectId().getId();
    }

    public String getMetadataInfoById(String metadataSchemeId){
       MetadataSchemeInfo metadataSchemeInfo = metadataSchemeInfoRepository.getMetadataSchemeInfoByIdOrCode(metadataSchemeId, null);
       return metadataSchemeInfo.getNewVersionNo();
    }

    public List<MetadataCodeItem> getMetadataCodeItem(String metadataCodeName){
        List<MetadataCodeItem> metadataCodeItems = metadataCodeItemRepository.getMetadataCodeItem(metadataCodeName);
        return metadataCodeItems;
    }

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
    public void deleteById(List<String> parentIds,String archiveType) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        if(CollectionUtils.isNotEmpty(parentIds)) {
            for (String id : parentIds) {
                IAfSysObject afSysObject = sysObjectRepository.find(id);
                QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
                queryWrapper.likeRight("s_object_path", afSysObject.getObjectPath()).eq("archive_type", archiveType);
                List<SysObjectDO> records = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 50000)).getRecords();
                for (SysObjectDO sysObjectDO : records) {
                    IAfSysObject sysObject;
                    try {
                        sysObject = sysObjectRepository.find(sysObjectDO.getId());
                    } catch (Exception e) {
                        continue;
                    }
                    fixedThreadPool.execute(() -> {
                        sysObject.destroy();
                        System.out.println("删除：" + sysObjectDO.getPath());
                    });

                }
                System.out.println("-------------------------------------------" + id);
            }
        }else {
            QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("archive_type", archiveType);
            List<SysObjectDO> records = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 50000)).getRecords();
            for (SysObjectDO sysObjectDO : records) {
                IAfSysObject sysObject;
                try {
                    sysObject = sysObjectRepository.find(sysObjectDO.getId());
                } catch (Exception e) {
                    continue;
                }
                fixedThreadPool.execute(() -> {
                    sysObject.destroy();
                    System.out.println("删除：" + sysObjectDO.getPath());
                });

            }
            System.out.println("-------------------------------------------");
        }


    }


    public SubCategory getSubCategoryByClassRule(String rule, String parentId, Map<String, String> singleMap,String categoryId){
        Map<String,String> year = new HashMap<>();
        year.put("2023","2023年度");
        Map<String,String> cls = new HashMap<>();
        cls.put("党群管理","党群工作类");
        cls.put("行政管理","行政管理类");
        cls.put("经营管理","经营管理类");
        cls.put("生产技术","生产技术管理类");
        Map<String,String> rp = new HashMap<>();
        rp.put("Y","永久");

        SubCategoryDO subCategoryDO = null;
        if(rule.contains("@")){
            String[] ruleArr = rule.split("@");
            for (int i = 0; i < ruleArr.length; i++) {
                String name = singleMap.get(ruleArr[i]);
                if("file_year".equals(ruleArr[i])){
                    name = year.get(name);
                }
                if("first_class_name".equals(ruleArr[i])){
                    name = cls.get(name);
                }
                if("retention_period".equals(ruleArr[i])){
                    name = rp.get(name);
                }
                //TODO
//                subCategoryDO = subCategoryMapper.findByNameAndParentId(name,parentId);
//                if(null == subCategoryDO){
//                    createClass(name,parentId);
//                    subCategoryDO = subCategoryMapper.findByNameAndParentId(name,parentId);
//                }
                parentId = subCategoryDO.getId();
            }
        }else{
            //TODO
//            if(rule.equals("INIT_CLASS")){
//                subCategoryDO = subCategoryMapper.findByNameAndParentId("初始类目",categoryId);
//            }else{
//                String name = singleMap.get(rule);
//                subCategoryDO = subCategoryMapper.findByNameAndParentId(name,categoryId);
//                if(null == subCategoryDO){
//                    createClass(name,categoryId);
//                    subCategoryDO = subCategoryMapper.findByNameAndParentId(name,categoryId);
//                }
//            }
        }
        if(null == subCategoryDO){
            throw new RuntimeException("类目未找到");
        }
        return getSubCategoryByNameAndParentPath(subCategoryDO.getId());
    }

    public SubCategory getSubCategoryByNameAndParentPath(String id){
        SubCategory subCategory = subCategoryRepository.find(id);
        return subCategory;
    }
    public String getSubCategoryIdByNameAndParentId(String name,String parentId){
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_parent_id", parentId).eq("da_name", name);
        List<SysObjectDO> records = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 1)).getRecords();
        if(records.size()>0){
            return records.get(0).getId();
        }
        return null;
    }
    public String getSubCategoryByNameAndPath(String name,String path){
        SessionContext.setSession(SessionUtil.getAdminSession());
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("s_object_path", path).eq("da_name", name);
        return sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 1)).getRecords().get(0).getPath();
    }
    public void createClass(String name,String categoryId){
        SubCategoryCreateCommand command = new SubCategoryCreateCommand();
        command.setCategoryId(categoryId);
        command.setClassNumber("");
        command.setClassificationCodePrefix(null);
        command.setDataSync(false);
        command.setDescription("");
        command.setName(name);
        command.setParentId(categoryId);
        command.setRetentionPeriodId("2b6e6103-4e78-4c5f-ac68-f870649abefe");
        command.setRetentionPolicyId("bf14053038209662976");
        create(command);
    }



}
