package com.example.demo1_nacos.service;

import cn.amberdata.common.response.exception.BusinessException;
import cn.amberdata.dm.common.domain.event.DomainEventPublisher;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.rm.classification.Category;
import cn.amberdata.rm.classification.CategoryRepository;
import cn.amberdata.rm.common.exception.ExceptionCode;
import cn.amberdata.rm.metadata.category.MetadataCategory;
import cn.amberdata.rm.metadata.category.MetadataCategoryRepository;
import cn.amberdata.rm.unit.context.UnitFolderContext;
import cn.amberdata.tdr.classification.CategoryEvent;
import cn.amberdata.tdr.classification.command.CategoryCreateCommand;
import cn.amberdata.tdr.classification.specification.CategoryCreateSpecification;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 * 类目 service
 *
 * @author lyf
 * @since 2022/1/12 14:19
 */
@Service
public class SubCategoryServiceImpl {

    @Resource
    private FolderRepository folderRepository;

    @Resource
    private CategoryRepository categoryRepository;


    @Resource
    private MetadataCategoryRepository metadataCategoryRepository;




}
