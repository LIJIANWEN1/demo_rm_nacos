package com.example.demo1_nacos.service.specification;

import cn.amberdata.dm.common.domain.AbstractSpecification;
import cn.amberdata.rm.classification.SubCategory;
import cn.amberdata.rm.common.exception.ExceptionCode;
import org.apache.commons.lang.StringUtils;

/**
 * Description:
 * 类目编号保存校验
 *
 * @author wd
 * @since 2022/8/04 15:32
 */
public class ClassNumberSpecification extends AbstractSpecification<SubCategory> {

    private static final String REGEX_MATCH = "([a-z0-9A-Z]){1,10}";

    @Override
    public boolean isSatisfiedBy(SubCategory subCategory) {
        //类目编号不为空并且不匹配正则校验则抛异常
        if (StringUtils.isNotBlank(subCategory.getClassNumber()) && !subCategory.getClassNumber().matches(REGEX_MATCH)) {
//            throw new BusinessException(ExceptionCode.CLASS_NUMBER_NOT_MATCH);
        }
        return true;
    }
}
