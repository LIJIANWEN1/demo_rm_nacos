package com.example.demo1_nacos.api;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xiao
 * @date: 2021/5/31 16:40
 */
@FunctionalInterface
public interface DemoFunction {
    void apply(List<Map<String, Object>> list);
}
