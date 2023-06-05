package com.example.demo1_nacos.service.event;

import cn.amberdata.dm.common.domain.event.DomainEvent;
import lombok.Data;

import java.util.Date;

/**
 * Description: 门类创建事件
 *
 * @author wd
 * @since 2022/01/21
 */
@Data
public class CategoryEvent implements DomainEvent {

    /**
     * 门类id
     */
    private String categoryId;

    /**
     * unitid
     */
    private String unitId;

    private String retentionPolicyPath;

    @Override
    public Date occurredTime() {
        return new Date();
    }
}
