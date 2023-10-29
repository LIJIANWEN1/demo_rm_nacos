package com.example.demo1_nacos.service.specification;

import cn.amberdata.dm.common.domain.AbstractSpecification;
import cn.amberdata.rm.classification.Category;
import cn.amberdata.rm.common.exception.ExceptionCode;
import cn.amberdata.rm.metadata.category.MetadataCategory;
import cn.amberdata.rm.metadata.category.MetadataCategoryRepository;
import org.apache.commons.lang.StringUtils;

/**
 * Description:
 * 门类创建校验
 *
 * @author lyf
 * @since 2022/1/20 14:19
 */
public class CategoryCreateSpecification extends AbstractSpecification<Category> {


    private MetadataCategoryRepository metadataCategoryRepository;

    public CategoryCreateSpecification(MetadataCategoryRepository metadataCategoryRepository) {
        this.metadataCategoryRepository = metadataCategoryRepository;
    }

    @Override
    public boolean isSatisfiedBy(Category category) {
        if (category.getMetadataSchemeId() != null) {
            MetadataCategory metadataCategory = metadataCategoryRepository.getCategoryById(category.getMetadataSchemeId());
            if (StringUtils.isBlank(metadataCategory.getCode())) {
                System.out.println(ExceptionCode.CATEGORY_IN_METADATA_NOT_EXIST);
            }
        }
        return true;
    }
}
