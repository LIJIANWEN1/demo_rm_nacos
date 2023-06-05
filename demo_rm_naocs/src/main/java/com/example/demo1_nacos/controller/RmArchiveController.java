package com.example.demo1_nacos.controller;
import com.example.demo1_nacos.service.RmArchiveServiceImpl;
import com.example.demo1_nacos.vo.ImportArchivePackageVO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/17 9:20
 */
@RestController
@RequestMapping("/rm_archive")
public class RmArchiveController {

    @Resource
    private RmArchiveServiceImpl rmArchiveService;

    @GetMapping("/update_archival_id")
    public String updateArchvivalId(@RequestParam String path) throws Exception {
        rmArchiveService.updateArchivalId(path);
        return "resultStr";
    }


    @PostMapping("/import_archives")
    public String createRecord(@RequestBody ImportArchivePackageVO importArchivePackageVO) {
        if (importArchivePackageVO.getType().equals(ImportArchivePackageVO.RECORD) &&
                (importArchivePackageVO.getCollectionWay().equals(ImportArchivePackageVO.VOLUME)||
                importArchivePackageVO.getCollectionWay().equals(ImportArchivePackageVO.RECORD))){
            rmArchiveService.importRecordFromExcel(importArchivePackageVO.getMetadataSchemeId(),importArchivePackageVO.getCollectionWay(),importArchivePackageVO);
        }else {
            rmArchiveService.importVolumeFromExcel(importArchivePackageVO.getMetadataSchemeId(), importArchivePackageVO.getCollectionWay(), importArchivePackageVO);
        }
        return "666";
    }

}
