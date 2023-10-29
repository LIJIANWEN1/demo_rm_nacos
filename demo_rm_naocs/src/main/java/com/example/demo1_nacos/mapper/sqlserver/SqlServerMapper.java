package com.example.demo1_nacos.mapper.sqlserver;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.example.demo1_nacos.pojo.BaseQueryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;
import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 9:06
 */

@Mapper
public interface SqlServerMapper extends BaseMapper<BaseQueryModel> {

    Cursor<Map<String, Object>> findByWrapper(@Param("queryFields") String queryFields,
                                              @Param("table") String table,
                                              @Param("model") BaseQueryModel model);

    Long count(@Param("table")String tableName, @Param("model") BaseQueryModel model);
}
