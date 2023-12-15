package com.example.demo1_nacos.controller;
import cn.amberdata.rm.classification.mapper.SubCategoryMapper;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItem;
import com.example.demo1_nacos.service.RmArchiveServiceImpl;
import com.example.demo1_nacos.service.RmOtherServiceImpl;
import com.example.demo1_nacos.service.command.CategoryCreateCommand;
import com.example.demo1_nacos.service.command.SubCategoryCreateCommand;
import com.example.demo1_nacos.vo.ImportArchivePackageVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/17 9:20
 */
@RestController
@RequestMapping("/rm_archive_other")
public class RmOtherController {

    @Resource
    private RmOtherServiceImpl rmOtherService;



    @GetMapping("/get_by_path")
    public String getFolderByPath(@RequestParam String path){
        return rmOtherService.getFolderByPath(path);
    }

    @GetMapping("/get_metadata_version_by_id")
    public String getMetadataInfoById(@RequestParam String id){
        return rmOtherService.getMetadataInfoById(id);
    }


    @GetMapping("/get_subcategory_id_by_path")
    public void getSubCategoryId(@RequestParam String name,@RequestParam String path){
//         rmOtherService.getSubCategoryByNameAndParentPath(name,path);
    }

    @GetMapping("/get_metadata_code_item")
    List<MetadataCodeItem> getMetadataCodeItem(@RequestParam String metadataCodeName){
        return rmOtherService.getMetadataCodeItem(metadataCodeName);
    }

    @GetMapping("/get_subcategory_by_name_and_path")
    public String getSubCategoryByNameAndPath(@RequestParam String name,@RequestParam String path)  {
        return rmOtherService.getSubCategoryByNameAndPath(name,path);
    }

    @GetMapping("/get_subcategory_by_name_and_parent_id")
    public String getSubCategoryByNameAndParentId(@RequestParam String name,@RequestParam String parentId)  {
        return rmOtherService.getSubCategoryIdByNameAndParentId(name,parentId);
    }

    /**
     * 创建门类
     *
     */
    @ApiOperation(value = "创建门类")
    @PostMapping("/create_category")
    public void createCategory(@Validated @RequestBody CategoryCreateCommand categoryCreateCommand) {
        rmOtherService.create(categoryCreateCommand);
    }

    /**
     * 创建类目
     *
     * @param subCategoryCreateCommand 类目参数对象
     */
    @ApiOperation(value = "创建类目")
    @PostMapping("/create_class")
    public String create(@Validated @RequestBody SubCategoryCreateCommand subCategoryCreateCommand) {
        return rmOtherService.create(subCategoryCreateCommand);
    }

    @ApiOperation(value = "创建保留处置策略")
    @GetMapping("/create_retention_policy")
    String createRetentionStrategy(@RequestParam String unitId,@RequestParam String policyPath){
        return rmOtherService.createRetentionStrategy(unitId,policyPath);
    }

    @ApiOperation(value = "删除文件夹")
    @PostMapping("/delete_by_id")
    void deleteRetentionStrategy(@RequestBody List<String> parentIds,@RequestParam String archiveType){
         rmOtherService.deleteById(parentIds,archiveType);
    }

    @ApiOperation(value = "同步platform")
    @GetMapping("/syn_platform_data")
    void syncPlatformData(String parentId,String synParentId){
        rmOtherService.syncPlatformData(parentId,synParentId);
    }

    @ApiOperation(value = "创建单位文件柜")
    @GetMapping("/init_unit_foler")
    public void initSyncLibFolder(Boolean flag,String unitId) {
        rmOtherService.initSyncLibFolder(flag,unitId);

    }





}
