package com.example.demo1_nacos.mapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 9:06
 */

@Mapper
public interface DataMapper{

    List<Map<String,Object>> findByWrapper();
}
