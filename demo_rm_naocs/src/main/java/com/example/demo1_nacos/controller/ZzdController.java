package com.example.demo1_nacos.controller;
import com.example.demo1_nacos.service.ArchiveServiceImpl;
import com.example.demo1_nacos.service.ZzdServiceImpl;
import com.example.demo1_nacos.util.ChangeCharset;
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

    @GetMapping("/zzd")
    public boolean zzd(@RequestParam String code, @RequestParam String admin_tenantid) {
        zzdService.orgHaveUsers(code,admin_tenantid);
        return true;
    }

}
