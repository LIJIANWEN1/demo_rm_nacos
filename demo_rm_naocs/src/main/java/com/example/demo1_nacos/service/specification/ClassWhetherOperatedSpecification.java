package com.example.demo1_nacos.service.specification;

import cn.amberdata.dm.common.domain.AbstractSpecification;
import cn.amberdata.rm.classification.SubCategory;
import cn.amberdata.rm.common.exception.ExceptionCode;

/**
 * Description:
 * 初始化类目校验
 *
 * @author wd
 * @since 2022/7/13
 */
public class ClassWhetherOperatedSpecification extends AbstractSpecification<SubCategory> {

    @Override
    public boolean isSatisfiedBy(SubCategory subCategory) {
        if (subCategory.getIsInitClass()) {
//            throw new BusinessException(ExceptionCode.INIT_CLASS_NON_OPERATIONAL);
        }
        return true;
    }
}
