package com.example.demo1_nacos.controller;
import com.example.demo1_nacos.service.MatchingRuleServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 9:00
 */

@RestController
@RequestMapping("/match_rule")
public class MatchingRuleController {

    @Resource
    private MatchingRuleServiceImpl matchingRuleServiceImpl;

    @GetMapping("/convert")
    public void String(String oldCode) {
        matchingRuleServiceImpl.convertDate(oldCode);
    }

    public static void main(String[] args) {
        Map<String,String> hashMap = new LinkedHashMap<>();
        hashMap.put("/aaa/bbb/**","xxxxx");
        String s = hashMap.get("/aaa/bbb/ccc");
        System.out.println(s);

    }
}
