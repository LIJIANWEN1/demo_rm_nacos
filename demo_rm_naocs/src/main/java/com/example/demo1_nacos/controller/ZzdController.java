package com.example.demo1_nacos.controller;
import com.example.demo1_nacos.service.ArchiveServiceImpl;
import com.example.demo1_nacos.service.ZzdServiceImpl;
import com.example.demo1_nacos.util.ChangeCharset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/17 9:20
 */
@Controller
@RequestMapping("/zzd")
@ResponseBody
public class ZzdController {

    @Resource
    private ZzdServiceImpl zzdService;

    @GetMapping("/ss")
    @Scheduled(cron = "0 0/1 * * * ?")
    public boolean zzd() {
        System.out.println("xxxxxxxx");
        zzdService.getOrgDetail("GO_20d5215098b849f5a4c8660e5ac55a02","196729");
        return true;
    }

}
