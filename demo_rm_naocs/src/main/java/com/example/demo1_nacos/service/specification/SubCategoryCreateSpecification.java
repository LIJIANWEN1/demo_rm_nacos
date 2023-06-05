package com.example.demo1_nacos.service.specification;

import cn.amberdata.dm.common.domain.AbstractSpecification;
import cn.amberdata.rm.archive.record.mapper.RecordMapper;
import cn.amberdata.rm.archive.valueobject.CollectionWay;
import cn.amberdata.rm.archive.volume.mapper.VolumeMapper;
import cn.amberdata.rm.classification.SubCategory;
import cn.amberdata.rm.classification.SubCategoryRepository;
import cn.amberdata.rm.common.exception.ExceptionCode;
import cn.amberdata.rm.settings.strategy.RetentionStrategy;
import cn.amberdata.rm.settings.strategy.RetentionStrategyRepository;

/**
 * Description:
 * 类目创建校验
 *
 * @author lyf
 * @since 2022/1/25 15:32
 */
public class SubCategoryCreateSpecification extends AbstractSpecification<SubCategory> {

    private SubCategoryRepository subCategoryRepository;

    private RetentionStrategyRepository retentionStrategyRepository;

    private RecordMapper recordMapper;

    private VolumeMapper volumeMapper;

    public SubCategoryCreateSpecification(SubCategoryRepository subCategoryRepository, RetentionStrategyRepository retentionStrategyRepository, RecordMapper recordMapper, VolumeMapper volumeMapper) {
        this.subCategoryRepository = subCategoryRepository;
        this.retentionStrategyRepository = retentionStrategyRepository;
        this.recordMapper = recordMapper;
        this.volumeMapper = volumeMapper;
    }

    @Override
    public boolean isSatisfiedBy(SubCategory subCategory) {
        //是否有特殊字符
        String code = subCategory.getCode();
        String dot = ".";
        if (org.apache.commons.lang3.StringUtils.contains(code, dot)) {
//            throw new BusinessException(ExceptionCode.EXIST_DOT);
        }

        if (subCategory.getCode() != null && subCategory.getParentId() != null) {
            SubCategory exit = subCategoryRepository.findSubCategoryByCodeAndParentId(subCategory.getCode(), subCategory.getParentId());
            if (exit != null) {
//                throw new BusinessException(ExceptionCode.CLASS_NUM_EXIST_ERROR);
            }
        }
        //判断父类目、门类下是否有案卷,record
        if (CollectionWay.COLLECTION_WAY_VOLUME.equals(subCategory.getCollectionWay())) {
            if (!volumeMapper.childIdBySubCategoryId(subCategory.getParentId()).isEmpty()) {
//                throw new BusinessException(ExceptionCode.VOLUME_EXIST_ON_CATEGORY_ERROR);
            }
        }
        if (CollectionWay.COLLECTION_WAY_RECORD.equals(subCategory.getCollectionWay())) {
            if (!recordMapper.childIdBySubCategoryId(subCategory.getParentId()).isEmpty()) {
//                throw new BusinessException(ExceptionCode.RECORD_EXIST_ON_CATEGORY_ERROR);
            }
        }
        //判断保留策略是否存在
        RetentionStrategy retentionStrategy = retentionStrategyRepository.find(subCategory.getRetentionPolicyId());
        if (null == retentionStrategy || null == retentionStrategy.getObjectName()) {
//            throw new BusinessException(ExceptionCode.RESOURCES_BAD_REQUEST_ERROR);
        }
        return true;
    }
}
