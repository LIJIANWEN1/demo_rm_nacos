package com.example.demo1_nacos.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.xxpt.gateway.shared.api.request
        .OapiMoziOrganizationPageOrganizationEmployeeCodesRequest;
import com.alibaba.xxpt.gateway.shared.api.response.OapiMoziOrganizationPageOrganizationEmployeeCodesResponse;
import com.alibaba.xxpt.gateway.shared.client.http.ExecutableClient;
import com.alibaba.xxpt.gateway.shared.client.http.IntelligentPostClient;
import com.alibaba.xxpt.gateway.shared.client.http.api.OapiSpResultContent;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2023/10/29 10:28
 */
public class ZzdServiceImpl {
    private static String key = "jbqqyszdaglfwythxt-ikJhzuQFnem";
    private static String appsecret = "8YS8xy03896T8SMUrjYO6Hei5YPKZJ50vl81FJDz";
    private static String domainname ="openplatform-pro.ding.zj.gov.cn";
    private static Integer PAGE_SIZE = 20;
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

    public  boolean orgHaveUsers( String code, String admin_tenantid) {
        ExecutableClient zzdSyncConfig = executableClient();
        IntelligentPostClient intelligentPostClient = zzdSyncConfig.newIntelligentPostClient(PAGE_EMPLOYEE_CODES);
        OapiMoziOrganizationPageOrganizationEmployeeCodesRequest request = new OapiMoziOrganizationPageOrganizationEmployeeCodesRequest();

        request.setReturnTotalSize(true);
        request.setPageSize(PAGE_SIZE);
        request.setOrganizationCode(code);
        request.setPageNo(1);
        request.setTenantId(Long.valueOf(admin_tenantid));
        OapiMoziOrganizationPageOrganizationEmployeeCodesResponse apiResult = intelligentPostClient.post(request);

        if (apiResult.getSuccess()) {
            OapiSpResultContent content = apiResult.getContent();
            if (content.getSuccess()) {
                String data = content.getData();
                System.out.println(data);
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
