package com.example.demo1_nacos.pojo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 11:48
 */

@Data
@NoArgsConstructor
public class BaseQueryModel {

    private String qzh;

    public BaseQueryModel(Map<String, Object> map) {
            this.qzh = (String) map.get("qzh");
    }
}
