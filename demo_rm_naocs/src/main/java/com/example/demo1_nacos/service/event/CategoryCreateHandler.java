package com.example.demo1_nacos.service.event;

import cn.amberdata.dm.common.context.unit.UnitContext;
import cn.amberdata.dm.common.domain.event.DomainEventSubscriber;
import cn.amberdata.dm.common.domain.event.EventHandler;
import cn.amberdata.rm.classification.*;
import cn.amberdata.rm.classification.mapper.SubCategoryMapper;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItem;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItemRepository;
import cn.amberdata.rm.settings.strategy.RetentionStrategy;
import cn.amberdata.rm.settings.strategy.RetentionStrategyDO;
import cn.amberdata.rm.settings.strategy.RetentionStrategyRepository;
import cn.amberdata.rm.settings.strategy.mapper.RetentionStrategyMapper;
import cn.amberdata.rm.settings.strategy.valueobject.DisposalType;
import cn.amberdata.rm.settings.strategy.valueobject.RetentionStrategyStyle;
import cn.amberdata.rm.unit.context.UnitFolderContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: 门类创建事件
 *
 * @author wd
 * @since 2022/3/3
 */
@EventHandler(eventClass = CategoryEvent.class)
@Component
public class CategoryCreateHandler implements DomainEventSubscriber<CategoryEvent> {

    @Resource
    private SubCategoryRepository subCategoryRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @Resource
    private SubCategoryMapper subCategoryMapper;

    @Resource
    private RetentionStrategyMapper retentionStrategyMapper;

    @Resource
    private MetadataCodeItemRepository metadataCodeItemRepository;

    @Resource
    private RetentionStrategyRepository retentionStrategyRepository;

    private static final String RETENTION_PERIOD = "保管期限";
    private static final String PERPETUAL = "永久";
    private static final String DEFAULT_CLASS_CODE = "00";
    private static final String DEFAULT_CLASS_NAME = "初始类目";
    private static final String DEFAULT_POLICY_NAME = "保留10年后移交";
    private static final String DEFAULT_DISPOSAL_DATE = "提前-1-月提醒";


    @Override
    public void handle(CategoryEvent event) {
        Category category = categoryRepository.find(event.getCategoryId());
        //判断该门类下初始类目是否已经存在，不存在新建
        SubCategoryDO subCategoryDO = subCategoryMapper.findByPath(category.getObjectPath() + "/" + DEFAULT_CLASS_CODE + "." + DEFAULT_CLASS_NAME);
        if (null == subCategoryDO) {
            //获取元数据平台保管期限为永久的代码项id
            String retentionPeriodId = "";
            List<MetadataCodeItem> itemList = metadataCodeItemRepository.getMetadataCodeItem(RETENTION_PERIOD).stream().filter(v -> StringUtils.equals(PERPETUAL, v.getName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(itemList)) {
                retentionPeriodId = itemList.get(0).getId();
            }
            //获取默认的保留处置策略id，不存在新建
            String retentionStrategyId;
            RetentionStrategyDO retentionStrategyDO = retentionStrategyMapper.findBySignAndName(event.getUnitId(), DEFAULT_POLICY_NAME);
            if (null != retentionStrategyDO) {
                retentionStrategyId = retentionStrategyDO.getId();
            } else {
                RetentionStrategy retentionStrategy = retentionStrategyRepository.newEntity(DEFAULT_POLICY_NAME, UnitContext.getObject().getObjectId().getId(),
                        RetentionStrategyStyle.CUSTOM, 10, DEFAULT_DISPOSAL_DATE,
                        DisposalType.TRANSFER, 1, event.getRetentionPolicyPath());
                //存储保留处置策略
                retentionStrategyRepository.store(retentionStrategy);
                retentionStrategyId = retentionStrategy.getObjectId().getId();
            }
            //创建初始化类目
            SubCategory subCategory = subCategoryRepository.newEntity(category.getObjectId().getId(), DEFAULT_CLASS_NAME, DEFAULT_CLASS_CODE, retentionStrategyId, retentionPeriodId, "",
                    category.getObjectId().getId(), null, true);
            subCategory.inheritParent(category.getCollectionWay().asValue(), category.getObjectId().getId());
            subCategoryRepository.store(subCategory);
        }
    }
}
