package com.example.demo1_nacos.service.event;

import cn.amberdata.dm.common.domain.event.DomainEvent;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Description: 门类创建事件
 *
 * @author wd
 * @since 2022/01/21
 */
@Data
public class CategoryEvent implements DomainEvent {

    /**
     * 整个门类树id集合
     */
    List<String> singleCategoryTreeIdList;

    @Override
    public Date occurredTime() {
        return new Date();
    }
}
