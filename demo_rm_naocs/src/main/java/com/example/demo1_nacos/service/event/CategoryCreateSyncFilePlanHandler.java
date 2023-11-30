package com.example.demo1_nacos.service.event;

import cn.amberdata.dm.common.domain.event.DomainEventSubscriber;
import cn.amberdata.dm.common.domain.event.EventHandler;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.rm.classification.*;
import cn.amberdata.rm.classification.mapper.FilePlanMapper;
import cn.amberdata.rm.classification.valueobject.FilePlanType;
import cn.amberdata.rm.common.domain.TypeClassConstant;
import cn.amberdata.rm.unit.context.UnitFolderContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Description: 门类创建同步创建文件计划
 *
 * @author wd
 * @since 2022/3/3
 */
@EventHandler(eventClass = CategoryEvent.class)
@Component
public class CategoryCreateSyncFilePlanHandler implements DomainEventSubscriber<CategoryEvent> {

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private FolderRepository folderRepository;

    @Resource
    private FilePlanRepository filePlanRepository;

    @Resource
    private FilePlanMapper filePlanMapper;


    @Override
    public void handle(CategoryEvent event) {
        //获取整编库文件夹路径
        StringBuilder yearPlanPath = new StringBuilder(UnitFolderContext.getObject().getFileArrangePath());
        //创建门类/类目整理计划
        for (String id : event.getSingleCategoryTreeIdList()) {
            Folder commonFolder = folderRepository.find(id);
            //获取上层整理计划(第一次为年度整理计划)
            Folder upLevelFolder = folderRepository.findByPath(yearPlanPath.toString());
            //判断此整理计划下是否已经存在相同的门类/类目整理计划,不存在则创建
            FilePlanDO filePlanByPath = filePlanMapper.findFilePlanByParentPathAndName(yearPlanPath + "%", commonFolder.getObjectName());
            if (null == filePlanByPath) {
                if (StringUtils.equals(TypeClassConstant.CATEGORY_TYPE, commonFolder.getObjectType())) {
                    Category category = categoryRepository.find(id);
                    FilePlan categoryFilePlan = filePlanRepository.newEntity(upLevelFolder.getObjectId().getId(), category.getObjectName(), null, FilePlanType.TYPE_CATEGORY.asValue(), null == category.getCollectionWay() ? null : category.getCollectionWay().asValue(), category.getObjectId().getId(), null);
                    filePlanRepository.store(categoryFilePlan);
                }
            }
            yearPlanPath.append("/").append(commonFolder.getObjectName());
        }

    }
}
