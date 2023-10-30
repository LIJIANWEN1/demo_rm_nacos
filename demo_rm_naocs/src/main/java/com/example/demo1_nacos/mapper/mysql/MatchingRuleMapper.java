package com.example.demo1_nacos.mapper.mysql;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.example.demo1_nacos.pojo.MatchingRule;
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
public interface MatchingRuleMapper extends BaseMapper<MatchingRule> {

    List<MatchingRule> findByWrapper(@Param(Constants.WRAPPER) Wrapper<MatchingRule> wrapper);
}
