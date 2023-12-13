package com.example.demo1_nacos.service;

import cn.amberdata.admin.domain.Organization;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.xxpt.gateway.shared.api.request.OapiMoziOrganizationGetOrganizationByCodeRequest;
import com.alibaba.xxpt.gateway.shared.api.request
        .OapiMoziOrganizationPageOrganizationEmployeeCodesRequest;
import com.alibaba.xxpt.gateway.shared.api.response.OapiMoziOrganizationGetOrganizationByCodeResponse;
import com.alibaba.xxpt.gateway.shared.api.response.OapiMoziOrganizationPageOrganizationEmployeeCodesResponse;
import com.alibaba.xxpt.gateway.shared.client.http.ExecutableClient;
import com.alibaba.xxpt.gateway.shared.client.http.IntelligentPostClient;
import com.alibaba.xxpt.gateway.shared.client.http.api.OapiSpResultContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2023/10/29 10:28
 */
@Slf4j
@Service
public class ZzdServiceImpl {
    private static String key = "jbqqyszdaglfwythxt-ikJhzuQFnem";
    private static String appsecret = "8YS8xy03896T8SMUrjYO6Hei5YPKZJ50vl81FJDz";
    private static String domainname ="openplatform-pro.ding.zj.gov.cn";
    private static Integer PAGE_SIZE = 20;
    public static final String OK_STATUS = "A";
    private static final String ORG_DETAIL_BY_CODE = "/mozi/organization/getOrganizationByCode";
    private static final String PAGE_EMPLOYEE_CODES = "/mozi/organization/pageOrganizationEmployeeCodes";

    public static ExecutableClient executableClient() {
        ExecutableClient executableClient = ExecutableClient.getInstance();
        executableClient.setAccessKey(key);
        executableClient.setSecretKey(appsecret);
        executableClient.setDomainName(domainname);
        executableClient.setProtocal("https");
        executableClient.init();
        return executableClient;
    }

    public Organization getOrgDetail(String code, String admin_tenantid) {
        ExecutableClient zzdSyncConfig = executableClient();
        IntelligentPostClient intelligentPostClient = zzdSyncConfig.newIntelligentPostClient(ORG_DETAIL_BY_CODE);
        OapiMoziOrganizationGetOrganizationByCodeRequest codeRequest = new OapiMoziOrganizationGetOrganizationByCodeRequest();
        codeRequest.setTenantId(Long.valueOf(admin_tenantid));
        codeRequest.setOrganizationCode(code);
        OapiMoziOrganizationGetOrganizationByCodeResponse codeResponse = intelligentPostClient.post(codeRequest);
        if (codeResponse.getSuccess()) {
            OapiSpResultContent content = codeResponse.getContent();
            if (content.getSuccess()) {
                String resData = content.getData();
                log.info("浙政钉根据组织code获取详情：" + resData);
            } else {
                log.info("获取组织详情失败", content.getResponseMessage());
            }
        }
        log.info("浙政钉根据组织code获取详情失败  err_msg：" + codeResponse.getMessage());
        log.info("浙政钉根据组织code获取详情失败  err_code：" + codeResponse.getBizErrorCode());
        return null;
    }

    public  boolean orgHaveUsers( String code, String admin_tenantid) {
        log.info("进来了1");
        ExecutableClient zzdSyncConfig = executableClient();
        IntelligentPostClient intelligentPostClient = zzdSyncConfig.newIntelligentPostClient(PAGE_EMPLOYEE_CODES);
        OapiMoziOrganizationPageOrganizationEmployeeCodesRequest request = new OapiMoziOrganizationPageOrganizationEmployeeCodesRequest();

        request.setReturnTotalSize(true);
        request.setPageSize(PAGE_SIZE);
        request.setOrganizationCode(code);
        request.setPageNo(1);
        request.setTenantId(Long.valueOf(admin_tenantid));
        OapiMoziOrganizationPageOrganizationEmployeeCodesResponse apiResult = intelligentPostClient.post(request);

        log.info("进来了2");
        System.out.println("-"+apiResult.getSuccess());
        if (apiResult.getSuccess()) {
            System.out.println("--------获取成功----");
            OapiSpResultContent content = apiResult.getContent();
            if (content.getSuccess()) {
                String data = content.getData();
                System.out.println("------"+data);
                if (null != data && JSON.parseArray(data).size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }


//    public static void main(String[] args) {
//        ExecutableClient ab = executableClient();
//        orgHaveUsers(ab,"GO_603c931ab33d43a0b9b8a21878ab9651","196729");
//    }


}
