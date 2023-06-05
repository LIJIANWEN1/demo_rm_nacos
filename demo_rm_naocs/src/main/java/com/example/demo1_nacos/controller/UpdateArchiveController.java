package com.example.demo1_nacos.controller;
import com.example.demo1_nacos.service.ArchiveServiceImpl;
import com.example.demo1_nacos.service.UpdateArchiveServiceImpl;
import com.example.demo1_nacos.util.ChangeCharset;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/17 9:20
 */
@Controller
@RequestMapping("/update_archive")
@ResponseBody
public class UpdateArchiveController {

    @Resource
    private UpdateArchiveServiceImpl updateArchivalIdByYuLin;

    @GetMapping("/add_ly_xcsj")
    public void addLyXCSJ(@RequestParam String objectType){
        updateArchivalIdByYuLin.addWSBlock(objectType);
    }

    @GetMapping("/update_ly_XCSJ_str")
    public void repairXCSJFormat(@RequestParam String objectType){
        updateArchivalIdByYuLin.repairXCSJFormat(objectType);
    }


    @GetMapping("/update_ly_ws_field")
    public void updateLyWsField(@RequestParam String objectType){
        updateArchivalIdByYuLin.repairXCSJFormat(objectType);
    }


    @GetMapping("/update_archival_id")
    public void updateArchivalIdByYuLin(@RequestBody List<String []> ruleByYuLin){
        updateArchivalIdByYuLin.updateArchivalIdByYuLin(ruleByYuLin);
    }


    @GetMapping("/update_time_yl")
    public void updateTimeStrYL(@RequestParam String path){
        updateArchivalIdByYuLin.updateTimeStrYL(path);
    }
}
