package com.example.demo1_nacos.controller;


import cn.amberdata.dm.common.utils.ListResult;
import cn.amberdata.rm.admin.eventlog.EventLogConstants;
import cn.amberdata.rm.admin.eventlog.annotation.EventLogAnnotation;
import com.example.demo1_nacos.service.SubCategoryServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * 类目操作控制器类
 *
 * @author lyf
 * @since 2022/2/15 15:15
 */
@Api(tags = "类目模块接口")
@RestController
@RequestMapping("/class")
public class ClassController {

    @Resource
    private SubCategoryServiceImpl subCategoryService;

//    /**
//     * 创建类目
//     *
//     * @param subCategoryCreateCommand 类目参数对象
//     */
//    @ApiOperation(value = "创建类目")
//    @PostMapping("/create_class")
//    public void create(@Validated @RequestBody SubCategoryCreateCommand subCategoryCreateCommand) {
//        subCategoryService.create(subCategoryCreateCommand);
//
//    }

}
