package com.example.demo1_nacos.controller;
import com.example.demo1_nacos.service.ArchiveServiceImpl;
import com.example.demo1_nacos.util.ChangeCharset;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/17 9:20
 */
@Controller
@RequestMapping("/archive")
@ResponseBody
public class ArchiveController {

    @Resource
    private ArchiveServiceImpl archiveServiceImpl;

//    @GetMapping("/submit")
//    public String getToken() throws Exception {
//        String resultStr = archiveServiceImpl.generateMetadataPackage();
//        return resultStr;
//    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String url = "定期（30年）!!!!!2012$$$$$2号";
        ChangeCharset changeCharset = new ChangeCharset();
        System.out.println(changeCharset.toUTF_8(url));
    }

}
