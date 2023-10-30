package com.example.demo1_nacos.service;
import cn.amberdata.cache.CacheTemplate;
import cn.amberdata.common.contenttransfer.ContentTransfer;
import cn.amberdata.common.util.aes.AESUtils;
import cn.amberdata.common.util.crypto.EncoderUtils;
import cn.amberdata.common.util.excel.ExcelUtilEx;
import cn.amberdata.common.util.excel.old.ExcelUtils;
import cn.amberdata.common.util.httpclient.HttpClientUtil;
import cn.amberdata.common.util.zip.CompressUtils;
import cn.amberdata.common.util.zip.ZipFileUtil;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumn;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo1_nacos.pojo.YC.CertificatePO;
import com.example.demo1_nacos.pojo.YC.OrderInfo;
import com.example.demo1_nacos.pojo.YC.YCResult;
import com.google.gson.Gson;
import com.jayway.jsonpath.DocumentContext;
import io.minio.MinioClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 17:50
 */
@Service
public class ArchiveServiceImpl {

    @Resource
    private CacheTemplate cacheTemplate;

    private static String appKey = "2b1e37d32079c22135c2f4657eb6378f";
    private static String appSecret = "101aa9a0ce675b47c30cb19884e0959ff";

    private static String ip = "192.168.10.77";

    private static String bucketName = "datacenter";

    private static String PZmetadataSchemeId = "acefd0a8-0e1d-47c9-9e45-8b0d2ce479b1";

    private static String DJmetadataSchemeId = "845758e0-f2e2-4d0d-a4ad-8f3b01bd00f9";

    private static String submitEDASUrl = "http://192.168.10.78/dataarchivesapi/data/submit";

    private static String accountCode = "1584244570835057173";

    private static String unitName = "大同二村";

    private static String unitCode = "dtec";

    public void readExcel() throws Exception {
    }
    public static String generateMetadataPackage() throws Exception {
        String token = getToken();
        System.out.println("--------1.获取token--------"+token);
        YCResult ycResult = getCertificateList(token);
        if (null == ycResult.getData()) {
            System.out.println("-----未获取到数据-----");
            return "未获取到数据";
        }
//        System.out.println("--------2.获取有成凭证数据--------"+ycResult.getData().getList().size());
//        TemplateMetadata templateMetadata = metadataService.findTemplateByMetadataSchemeIdAndVersionNo(PZmetadataSchemeId, null);
//        String jsonMetadataTemplate = metadataService.getJsonTemplateById(templateMetadata.getId());
//        List<MetadataColumn> columns = metadataService.getMetadataColumnByMetadataSchemeId(PZmetadataSchemeId);
//        DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
//        TemplateMetadata djTemplateMetadata = metadataService.findTemplateByMetadataSchemeIdAndVersionNo(DJmetadataSchemeId, null);
//        String djJsonMetadataTemplate = metadataService.getJsonTemplateById(djTemplateMetadata.getId());
//        List<MetadataColumn> djColumns = metadataService.getMetadataColumnByMetadataSchemeId(DJmetadataSchemeId);
//        System.out.println("--------3.获取元数据模板--------");
//        DocumentContext djDocumentContext = JsonPath.parse(djJsonMetadataTemplate);
//        for (CertificatePO certificatePO : ycResult.getData().getList()) {
//            Map<String, Object> certificateMap = generateCertificateMap(certificatePO);
//            saveJsonData(documentContext, certificateMap, columns);
//            String certificateXml = metadataRecord.getMetadataXml(documentContext.jsonString());
//            File certificateFile = writXmlToFile(certificateXml);
//            submitPackage("KJPZ","KJPZ","会计凭证","exchange_area/"+certificateFile.getName(),
//                    MD5Utils.getMD5(certificateFile),certificateFile.getName());
//            System.out.println("--------4.提交凭证数据包--------");
//            for (OrderInfo orderInfo : certificatePO.getOrderInfoList()) {
//                Map<String, Object> orderMap = generateOrderMap(orderInfo);
//                saveJsonData(djDocumentContext, orderMap, djColumns);
//                String orderXml = metadataRecord.getMetadataXml(djDocumentContext.jsonString());
//                File orderFile = writXmlToFile(orderXml);
//                submitPackage("DJ","DJ","单据","exchange_area/"+orderFile.getName(),
//                        MD5Utils.getMD5(orderFile),orderFile.getName());
//                System.out.println("--------4.提交单据数据包--------");
//            }
//        }
      return "数据提交成功";
    }

    private void submitPackage(String businessCode, String taskCode, String taskName, String packagePath, String md5Code,
                               String fileName) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("system_code", "ANYC");
        map.put("business_code", businessCode);
        map.put("dept_code", unitCode);
        map.put("dept_name", unitName);
        map.put("task_code", taskCode);
        map.put("task_name", taskName);
        map.put("data_id", fileName);
        map.put("data_desc", "有成数据包");
        map.put("data_path", packagePath);
        map.put("checksum_type", "md5");
        map.put("checksum", md5Code);
        String jsonStr = HttpClientUtil.doPostJson(submitEDASUrl, JSON.toJSONString(map));
        System.out.println(jsonStr);
    }

    public static YCResult getCertificateList(String token) {
        String url = "https://yiqbdatatest.superboss.cc/reimburse/certificate/queryListPage.rjson?appKey=" + appKey + "&accessToken=" + token;
        Map<String, Object> map = new HashMap<>(2);
        map.put("accountCode", "1584244570835057173");
        map.put("status",1);
        map.put("businessTyep","file");
//        map.put("beginTime", new SimpleDateFormat("yyyy-MM-DD").format(new Date()) + " 00:00:00");
//        map.put("endTime", new SimpleDateFormat("yyyy-MM-DD").format(new Date()) + " 24:00:00");
        map.put("pageSize", 10);
        String jsonStr = HttpClientUtil.doPostJson(url, JSON.toJSONString(map));
//        String jsonStr = "{\n" +
//                "    \"  apiName\": \"nguyet.goodwin\",\n" +
//                "    \"result\": 100,\n" +
//                "    \"message\": \"success\",\n" +
//                "    \"data\": {\n" +
//                "        \"success\": true,\n" +
//                "        \"list\": [\n" +
//                "            {\n" +
//                "                \"certificateId\": 851,\n" +
//                "                \"certificateWord\": \"c73cfj\",\n" +
//                "                \"certificateDate\": \"2022-01-19\",\n" +
//                "                \"certificateCode\": 374,\n" +
//                "                \"documentMaker\": \"mvzsk5\",\n" +
//                "                \"entryList\": [\n" +
//                "                    {\n" +
//                "                        \"journalizing\": 107,\n" +
//                "                        \"abstractName\": \"nguyet.goodwin\",\n" +
//                "                        \"subjectCode\": \"23790\",\n" +
//                "                        \"subjectCodeExt\": \"motrgo\",\n" +
//                "                        \"subjectName\": \"nguyet.goodwin\",\n" +
//                "                        \"subjectNameExt\": \"aepxbv\",\n" +
//                "                        \"direction\": 564,\n" +
//                "                        \"borrowMoney\": 460,\n" +
//                "                        \"loanMoney\": 574,\n" +
//                "                        \"amount\": 31.53,\n" +
//                "                        \"unit\": \"rw04mr\",\n" +
//                "                        \"unitPrice\": 25.8,\n" +
//                "                        \"auxiliaryList\": [\n" +
//                "                            {\n" +
//                "                                \"categoryCode\": \"23790\",\n" +
//                "                                \"categoryName\": \"nguyet.goodwin\",\n" +
//                "                                \"itemCode\": \"23790\",\n" +
//                "                                \"itemName\": \"nguyet.goodwin\"\n" +
//                "                            }\n" +
//                "                        ]\n" +
//                "                    }\n" +
//                "                ],\n" +
//                "                \"orderInfoList\": [\n" +
//                "                    {\n" +
//                "                        \"orderCode\": \"23790\",\n" +
//                "                        \"orderName\": \"nguyet.goodwin\",\n" +
//                "                        \"orderType\": \"hx8m6u\",\n" +
//                "                        \"templateName\": \"nguyet.goodwin\",\n" +
//                "                        \"submitAmount\": 87.97,\n" +
//                "                        \"department\": \"h6y8kk\",\n" +
//                "                        \"submitorName\": \"nguyet.goodwin\",\n" +
//                "                        \"submitorJobNumber\": \"vxt2cs\",\n" +
//                "                        \"componentAuxiliaryList\": [\n" +
//                "                            {\n" +
//                "                                \"code\": \"23790\",\n" +
//                "                                \"name\": \"nguyet.goodwin\",\n" +
//                "                                \"categoryCode\": \"23790\",\n" +
//                "                                \"categoryName\": \"nguyet.goodwin\"\n" +
//                "                            }\n" +
//                "                        ],\n" +
//                "                        \"payAccountInfo\": {\n" +
//                "                            \"holdName\": \"nguyet.goodwin\",\n" +
//                "                            \"bankName\": \"nguyet.goodwin\",\n" +
//                "                            \"bankCardNum\": \"kj2rk1\"\n" +
//                "                        }\n" +
//                "                    }\n" +
//                "                ]\n" +
//                "            }\n" +
//                "        ],\n" +
//                "        \"count\": 325,\n" +
//                "        \"errorMsg\": \"ilo76r\",\n" +
//                "        \"extendMap\": {\n" +
//                "            \"any object\": {}\n" +
//                "        },\n" +
//                "        \"nodeResponses\": [\n" +
//                "            {\n" +
//                "                \"nodeTemplateCode\": \"23790\",\n" +
//                "                \"nodeTemplateName\": \"nguyet.goodwin\"\n" +
//                "            }\n" +
//                "        ],\n" +
//                "        \"totalAmount\": 647,\n" +
//                "        \"totalArriveAmount\": 408,\n" +
//                "        \"totalNoArriveAmount\": 407\n" +
//                "    }\n" +
//                "}";
        YCResult ycResult = new Gson().fromJson(jsonStr, YCResult.class);
        System.out.println(ycResult);
        return ycResult;
    }

    private Map<String, Object> generateCertificateMap(CertificatePO certificatePO) {
        Map<String, Object> map = new HashMap<>();
        //资源标识
        map.put("archive_1st_category", "CW");
        //访问控制信息
        map.put("content_type", "02");
        //说明信息
        map.put("retention_period", "D30");
        map.put("doc_date", new SimpleDateFormat("yyyy-MM-DD").format(certificatePO.getCertificateDate()));
        map.put("carrier_type", "02");
        //内容描述信息
        map.put("je_submit_date", new SimpleDateFormat("yyyy-MM-DD").format(new Date()));
        map.put("approve_date", new SimpleDateFormat("yyyy-MM-DD").format(new Date()));
        map.put("ledger_name", "金蝶开发者测试账套");
        map.put("certificateId", certificatePO.getCertificateId());
        map.put("certificateWord", certificatePO.getCertificateWord());
        map.put("certificateDate", certificatePO.getCertificateDate());
        map.put("certificateCode", certificatePO.getCertificateCode());
        map.put("documentMaker", certificatePO.getDocumentMaker());
        map.put("file_source_system", "有成财务系统");
        return map;
    }

    private Map<String, Object> generateOrderMap(OrderInfo orderInfo) {
        Map<String, Object> map = new HashMap<>();
        //资源标识
        map.put("archive_1st_category", "CW");
        //访问控制信息
        map.put("content_type", "02");
        //说明信息
        map.put("retention_period", "D30");
        map.put("doc_date", new SimpleDateFormat("yyyy-MM-DD").format(new Date()));
        map.put("carrier_type", "02");
        //内容描述信息
        map.put("orderCode", orderInfo.getOrderCode());
        map.put("orderName", orderInfo.getOrderName());
        map.put("department", orderInfo.getDepartment());
        map.put("submitAmount", orderInfo.getSubmitAmount());
        map.put("submitorName", orderInfo.getSubmitorName());
        map.put("file_source_system", "有成财务系统");
        return map;
    }

    public static void addRole(String ip) throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put("Cookie","JSESSIONID=41E08178536F127A632C65B4476E8A18; LoginUnitId=8d8a4d59-0642-46cd-a6cc-8fde2001d9e5; IAM_TOKEN=eyJraWQiOiJsdUlVMzN3UVlPRWluVXQrUUhXQ01JcmZjNzEvT0lTMVhhSGFyNk9xN2lMWjcrZW9hSGZvM2h2L0dORkd5TFEyZTA3Yk00b1pkVnlNXG5QbUZKMHpHMENBPT0iLCJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJsZ19uYW1lXCI6XCJhZG1pbkBhbWJlcmRhdGEuY25cIixcIm5vbmNlXCI6XCIxOTkzZmMzY2UxZjJhZGExZjg3Mzg3MjFlYmI0MjExYlwiLFwidXNfc291cmNlXCI6XCJpbmxpbmUgcGFzc3dvcmRcIn0ifQ.yP1eimun0JAbE7wTyZz_qDcBOWh6flw8htNvsmnzWcw; CURR_UNIT_INFO=7E+MSdWEHrpYSpgxhWtyY+v5cKZNfMy/uYwMORjRZQsaiJZXz30Cn4tX/5oX97iTnAXrmwWyjTcm3UNZyWotKXAllhG85FNHT1kicrKISri6iL+ZzUVX4O4XR1uk+N3x; RANDOM_STAMP=7a302304-f4ff-4ce3-93ca-6c950cb8327b; AMBER_SSO_V1=sLKRvKVc1fSrrcBlBFonqA==");
        String excelPath = "C:\\Users\\AB_ZhangLei\\Desktop\\龙游项目\\全宗单位\\20221028档案分管领导、档案员(1).xlsx";
        File excelFile = new File(excelPath);
        String[][] excelArr = ExcelUtils.readExcel(excelFile);
        for (int i = 0; i < excelArr.length; i++) {
            if (0==i) {
                continue;
            }
            String [] array = excelArr[i];
            String fondsId = array[0];
            String id = array[1];
            String fenGuanName = array[3];
            String daglyName = array[5];
            if (StringUtils.isBlank(fondsId) || StringUtils.isBlank(fenGuanName) || StringUtils.isBlank(daglyName)) {
                System.out.println("存在空数据");
                continue;
            }
            //部门 2,角色 4
            //创建部门
            createDept(ip, "档案室", fondsId + "_das", id,headers);
            //创建全宗号_用户
            String dasDeptId = getSubOrgListByParentOrgId(ip, id, "2",fondsId + "_das",headers);
            createUser(ip, "档案管理员", fondsId + "_admin", fondsId + "_das_0001", dasDeptId,headers);
            //创建角色
            createRole(ip, "档案管理员", fondsId + "_dagly", id,headers);
            //添加用户
            String daglyRoleId = getSubOrgListByParentOrgId(ip, id, "4",fondsId + "_dagly",headers);
            String userId = getUserByLoginName(ip,fondsId + "_admin",headers);
            addUserToRole(ip,userId,daglyRoleId,headers);
            //添加浙政钉用户
            String zzdUserId = getUserByName(ip,daglyName,headers,id);
            addUserToRole(ip,zzdUserId,daglyRoleId,headers);
            //创建审核员角色
            createRole(ip, "一级审批员", fondsId + "_yjspy", id,headers);
            createRole(ip, "二级审批员", fondsId + "_rjspy", id,headers);
            //添加一级审核员
            String yjiRoleId = getSubOrgListByParentOrgId(ip, id, "4",fondsId+"_yjspy",headers);
            addUserToRole(ip,zzdUserId,yjiRoleId,headers);
            //添加二级审核员
            String fenguanUserId = getUserByName(ip,fenGuanName,headers,id);
            String rjiRoleId = getSubOrgListByParentOrgId(ip, id, "4",fondsId+"_rjspy",headers);
            addUserToRole(ip,fenguanUserId,rjiRoleId,headers);
        }
    }

    public static String getSubOrgListByParentOrgId(String ip, String orgId, String type,String orgCode,Map<String,String> headers) {
        String tokenUrl = "http://" + ip + "/adminapi/org/get_sub_org_list_by_parent_org_id?parentId=" + orgId + "&type=" + type;
        String jsonStr = HttpClientUtil.doGet(tokenUrl,null,headers);
        List<Map<String, String>> resultList = new Gson().fromJson(jsonStr, List.class);
        for (Map<String, String> map : resultList) {
            String code = map.get("code");
            if (code.equals(orgCode)) {
                return map.get("id");
            }
        }
        return "";
    }

    public static String getUserByName(String ip, String name,Map<String,String> headers,String unitId) {
        String tokenUrl = "http://localhost:8081/demo/match_rule/test?name="+name+"&unitId="+unitId;
        String jsonStr = HttpClientUtil.doGet(tokenUrl,null,headers);
        Map<String, String> userMap = new Gson().fromJson(jsonStr, Map.class);
        return userMap.get("id");
    }

    public static String getUserByLoginName(String ip, String loginName,Map<String,String> headers) {
        String tokenUrl = "http://" + ip + "/adminapi/user/get_user_by_login_name?loginName=" + loginName;
        String jsonStr = HttpClientUtil.doGet(tokenUrl,null,headers);
        Map<String, String> userMap = new Gson().fromJson(jsonStr, Map.class);
        return userMap.get("id");
    }

    public static void createDept(String ip, String name, String code, String orgId,Map<String,String> headers) {
        String tokenUrl = "http://" + ip + "/adminapi/department/create_department";
        Map<String, String> map = new HashMap<>(2);
        map.put("name", name);
        map.put("code", code);
        map.put("parentId", orgId);
        map.put("type", "2");
        String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map),headers);
        System.out.println(jsonStr);
    }

    public static void createUser(String ip, String name, String loginName, String code, String parentId,Map<String,String> headers) {
        String tokenUrl = "http://" + ip + "/adminapi/user/create_user";
        Map<String, String> map = new HashMap<>(2);
        map.put("name", name);
        map.put("code", code);
        map.put("loginName", loginName);
        map.put("parentId", parentId);
        map.put("password", "Y+zilsR88g9SZI8WOeHJlA==");
        String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map),headers);
        System.out.println(jsonStr);
    }

    public static void createRole(String ip, String name, String code, String parentId,Map<String,String> headers) {
        String tokenUrl = "http://" + ip + "/adminapi/role/create_role";
        Map<String, String> map = new HashMap<>(2);
        map.put("name", name);
        map.put("code", code);
        map.put("parentId", parentId);
        map.put("type", "4");
        String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map),headers);
        System.out.println(jsonStr);
    }

    public static void addUserToRole(String ip,String userId,String roleId,Map<String,String> headers) {
        String tokenUrl = "http://"+ip+"/adminapi/org/add_users_to_org?parentId="+roleId;
        List<String> ids = new ArrayList<>();
        ids.add(userId);
        String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(ids),headers);
        System.out.println(jsonStr);
    }



    public static String getToken() {
        String yc_token = "";
//        String yc_token = cacheTemplate.get("YC_token");
        if (StringUtils.isBlank(yc_token)) {
            String tokenUrl = "https://yiqbdatatest.superboss.cc/auth/getAccessToken.rjson";
            Map<String, String> map = new HashMap<>(2);
            map.put("appKey", appKey);
            map.put("appSecret", appSecret);
            String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map));
            JSONObject json = JSON.parseObject(jsonStr);
            JSONObject data = (JSONObject) json.get("data");
            yc_token = data.get("accessToken").toString();
        }
        System.out.println(yc_token);
        return yc_token;
    }


    private File writXmlToFile(String xml) {
        String uuId = UUID.randomUUID().toString();
        String rootPath = ContentTransfer.mkTransferTempDir() + File.separator;
        String exportTempDirPath = rootPath + uuId + File.separator;
        File exportTempFolder = new File(exportTempDirPath);
        if (!exportTempFolder.exists()) {
            exportTempFolder.mkdir();
        }
        try {
            Document doc = DocumentHelper.parseText(xml);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            String exportFilePath = exportTempDirPath + "元数据描述信息.xml";
            File file = new File(exportFilePath);
            XMLWriter writer = null;
            writer = new XMLWriter(new FileOutputStream(file), format);
            writer.write(doc);
            String documentZipFilePath = ContentTransfer.mkTransferTempDir() + File.separator + uuId + ".zip";
            CompressUtils.compress(documentZipFilePath, rootPath);
            File zipFile = new File(documentZipFilePath);
            uploadTest(zipFile);
            return zipFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {

        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
//        generateMetadataPackage();
//        String  password = AESUtils.decrypt("HK1pIFZ5ALiqE499zVFyOA==");
//
//        File file = new File("C:\\Users\\AB_ZhangLei\\Desktop\\da_user.xls");
//        File outfile = new File("C:\\Users\\AB_ZhangLei\\Desktop\\da_user_out.xls");
//        String[][] strings = ExcelUtils.readExcel(file);
//        for (String[] string : strings) {
//            string[2] = AESUtils.decrypt(string[2]);
//        }
//        ExcelUtils.writeExcel(strings,strings[0],new FileOutputStream(outfile));
//        String aa = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record name=\"折旧单据\"><property type=\"string\" name=\"metadata_scheme_code\" nullAble=\"false\" sys=\"true\" maxLength=\"255\" title=\"元数据方案编码\"/><property type=\"string\" name=\"metadata_scheme_name\" nullAble=\"false\" sys=\"true\" maxLength=\"255\" title=\"元数据方案名称\"/><property type=\"string\" name=\"version_no\" nullAble=\"false\" sys=\"true\" maxLength=\"10\" title=\"版本号\"/><block name=\"归档信息\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"><block name=\"资源标识\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"><property type=\"string\" name=\"archival_id\" nullAble=\"true\" sys=\"false\" maxLength=\"64\" title=\"档号\" scene=\"erms_kszl\"/><property type=\"string\" name=\"doc_number\" nullAble=\"true\" sys=\"false\" maxLength=\"64\" title=\"文件编号\"/><property type=\"string\" name=\"fonds_id\" nullAble=\"true\" sys=\"false\" maxLength=\"12\" title=\"全宗号\"/><property type=\"string\" name=\"item_code\" nullAble=\"true\" sys=\"false\" maxLength=\"12\" title=\"件号\"/><property type=\"string\" name=\"archive_1st_category\" nullAble=\"true\" sys=\"true\" maxLength=\"12\" title=\"一级门类编码\"/><property type=\"string\" name=\"category_code\" nullAble=\"true\" sys=\"true\" maxLength=\"12\" title=\"门类编码\"/><property type=\"string\" name=\"classification\" nullAble=\"true\" sys=\"true\" maxLength=\"12\" title=\"分类号\"/></block><block name=\"访问控制信息\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"><property type=\"string\" name=\"security_class\" nullAble=\"true\" sys=\"false\" maxLength=\"8\" title=\"信息安全等级\" allowedValuesCode=\"密级\" allowedValues=\"公开-L1,内部-L2,保密-L3,机密-L4\"/><property type=\"string\" name=\"content_type\" nullAble=\"true\" sys=\"false\" maxLength=\"8\" title=\"数据类型\" allowedValuesCode=\"数据类型\" allowedValues=\"客户-01,业务-02,公司-03\"/></block><block name=\"说明信息\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"><property type=\"string\" name=\"title\" nullAble=\"false\" sys=\"false\" maxLength=\"2048\" title=\"题名\"/><property type=\"string\" name=\"fonds_name\" nullAble=\"true\" sys=\"false\" maxLength=\"100\" title=\"全宗名称\"/><property type=\"string\" name=\"doc_attachments\" nullAble=\"true\" sys=\"false\" maxLength=\"200\" title=\"附件名称\"/><property type=\"string\" name=\"author\" nullAble=\"true\" sys=\"false\" maxLength=\"256\" title=\"责任者\"/><property type=\"string\" name=\"owner\" nullAble=\"true\" sys=\"false\" maxLength=\"256\" title=\"文件Owner\"/><property type=\"string\" name=\"job_no\" nullAble=\"true\" sys=\"false\" maxLength=\"256\" title=\"工号\"/><property type=\"string\" name=\"retention_period\" nullAble=\"false\" sys=\"false\" maxLength=\"8\" title=\"保管期限\" allowedValuesCode=\"保管期限\" allowedValues=\"长期-C,短期-D,10年-D10,100年-D100,15年-D15,30年-D30,45年-D45,5年-D5,70年-D70,永久-Y,待定-待定\"/><property type=\"string\" name=\"filed_by\" nullAble=\"true\" sys=\"false\" maxLength=\"64\" title=\"归档人\"/><property type=\"string\" name=\"file_department\" nullAble=\"true\" sys=\"false\" maxLength=\"256\" title=\"归档部门\"/><property type=\"date\" name=\"filed_date\" nullAble=\"true\" sys=\"false\" maxLength=\"32\" title=\"归档时间\" typeFormat=\"yyyyMMdd\"/><property type=\"date\" name=\"doc_date\" nullAble=\"true\" sys=\"false\" maxLength=\"32\" title=\"形成日期\" typeFormat=\"yyyyMMdd\"/><property type=\"string\" name=\"file_year\" nullAble=\"false\" sys=\"false\" maxLength=\"4\" title=\"年度\"/><property type=\"string\" name=\"carrier_type\" nullAble=\"false\" sys=\"false\" maxLength=\"12\" title=\"载体类型\" allowedValuesCode=\"载体类型\" allowedValues=\"实体-01,电子-02\"/><property type=\"int\" name=\"material_num\" nullAble=\"true\" sys=\"false\" maxLength=\"32\" title=\"载体数量\"/><property type=\"int\" name=\"doc_pages\" nullAble=\"true\" sys=\"false\" maxLength=\"32\" title=\"页数\"/><property type=\"int\" name=\"archives_num\" nullAble=\"true\" sys=\"false\" maxLength=\"32\" title=\"归档份数\"/><property type=\"string\" name=\"original_status\" nullAble=\"true\" sys=\"false\" maxLength=\"4\" title=\"原件状态\" allowedValuesCode=\"原件状态\" allowedValues=\"原件-1,扫描件-2\"/><property type=\"string\" name=\"digitized_status\" nullAble=\"true\" sys=\"false\" maxLength=\"4\" title=\"数字化状态\" allowedValuesCode=\"数字化状态\" allowedValues=\"未数字化-01,数字化副本-02,纯电子-03\"/><property type=\"boolean\" name=\"exist_document\" nullAble=\"true\" sys=\"false\" maxLength=\"32\" title=\"是否有原文\" allowedValuesCode=\"是否有原文\" allowedValues=\"无-false,有-true\"/><property type=\"string\" name=\"remark\" nullAble=\"true\" sys=\"false\" maxLength=\"1024\" title=\"备注\"/></block><block name=\"管理信息\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"></block></block><block name=\"业务内容\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"><block name=\"过程信息\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"></block></block><block name=\"电子文件\" can_repeat=\"false\" required=\"false\" source_type=\"da_record\" source_type_version=\"1\"></block><block name=\"业务信息\" can_repeat=\"false\" required=\"false\"><property type=\"string\" name=\"lbbm\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"类别编码\"/><property type=\"string\" name=\"zcbm\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"资产编码\"/><property type=\"string\" name=\"zcmc\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"资产名称\"/><property type=\"string\" name=\"ggxh\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"规格型号\"/><property type=\"string\" name=\"bqstzj\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"本期实提折旧（账面）\"/><property type=\"string\" name=\"bqytzj\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"本期应提折旧（账面）\"/><property type=\"string\" name=\"wtzj\" nullAble=\"true\" sys=\"false\" maxLength=\"255\" title=\"未提折旧（账面）\"/><property type=\"string\" name=\"bookkeeping_voucher_no\" nullAble=\"true\" sys=\"false\" maxLength=\"100\" title=\"记账凭证编号\"/><property type=\"string\" name=\"file_source_system\" nullAble=\"true\" sys=\"false\" maxLength=\"100\" title=\"归档来源系统\"/></block></record>";
//        String folderPath = "C:\\Users\\AB_ZhangLei\\Desktop\\条目\\文书档案(卷内)条目\\";
//        String outFilePath = folderPath+"excel";
//        File outFolder = new File(outFilePath);
//        if (!outFolder.exists()) {
//            outFolder.mkdir();
//        }
//        File folder = new File(folderPath);
//        for (File file : folder.listFiles()) {
//            if(file.isDirectory()){
//                continue;
//            }
//            String orgName = file.getName();
//            String newName = orgName.substring(0,orgName.lastIndexOf("."))+".xlsx";
//            String excelOutFilePath = outFilePath+File.separator+newName;
//            //通过上面那个方法获取json文件的内容
//            exxx(file,excelOutFilePath);
//        }
    }

    private static  String volumeStr = "档号，全宗号，目录号，案卷号，密级，发布网段，开放状态，提名，责任者，保管期限，年度，卷内文件份数，卷内页数，数字化状态，载体类型";
            private static  String itemStr = "档号，全宗号，目录号，案卷号，件号，密级，发布网段，开放状态，题名，责任者，保管期限，年度，载体类型，载体数量，\n" +
                    "数字化状态，是否有原文，终止页号，男方姓名，男方生日，男方国籍，男方常驻户口所在地，男方民族，男方职业，女方姓名\n" +
                    "女方生日，女方国籍，女方常驻户口所在地，女方民族，女方职业，女方证件号，男方证件号，申请日期，批准日期，字号，\n" +
                    "承办机关单位，页数，提供证明材料，审批意见";
    private static String[] volumeHeader = new String[]{"档案馆名称","档案馆代码","档号","全宗号","目录号",
            "案卷号","分类号","题名","主题词","附注","责任者","立档单位","保管期限","年度","卷内页数","卷内文件份数","起始日期",
            "终止日期","原文标识"};
    private static String[] itemHeader = new String[]{"档号","件号","案卷号","题名","责任者","立档单位","保管期限","年度","载体类型","数字化状态","张页号","页数",
            "形成时间","文件编号"};


    public static void exxx(File jsonFile, String outFilePath) throws Exception {
        String jsonData = getStr(jsonFile);
        //转json对象
        JSONObject parse = (JSONObject) JSONObject.parse(jsonData);
        String datarows = parse.getString("datarows");
        JSONArray objects = JSONArray.parseArray(datarows);
        if(objects.get(0) instanceof List<?>){
            System.out.println("-----");
        }

        String x = String.valueOf(objects.get(0));
        String[] split = x.split(",");
        String [][] array = new String[objects.size()+1][split.length+1];
//        if(jsonFile.getName().contains("案卷")){
//            array[0] = volumeStr.split("，");
//        }else{
//            array[0] = itemStr.split("，");
//        }


        for (int i = 0; i < objects.size(); i++) {
            List<Object> list = castList(objects.get(i), Object.class);
            for (int i1 = 0; i1 < list.size(); i1++) {
                String[] strings = array[i+1];
                String s = String.valueOf(list.get(i1));
                strings[i1] = StringUtils.isBlank(s)||"null".equals(s)?"":s;
            }
        }
        ExcelUtilEx.creatExcel(outFilePath,array);
    }


    //把一个文件中的内容读取成一个String字符串
    public static String getStr(File jsonFile){
        String jsonStr = "";
        try {
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>)
        {
            for (Object o : (List<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }


    public static void uploadTest(File file) throws Exception {
        try {
            //初始化
            MinioClient minioClient = new MinioClient("http://" + ip + ":9000", "admin", "Dctm@1234");
            //判断桶是否存在
            Boolean found = minioClient.bucketExists("yctest");
            if (found) {
                //上传文件
                minioClient.putObject(bucketName, "exchange_area/" + file.getName(), file.getPath(), null);
                System.out.println("上传成功");
            } else {
                System.out.println("桶不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateJnArchivalId() throws Exception {
        Map<String, String> archivalMap = new HashMap<>();
        String excelPath = "C:\\Users\\AB_ZhangLei\\Desktop\\宁波项目\\数据\\sysdba_AJMT - 文书.xls";
        File excelFile = new File(excelPath);
        String[][] excelArr = ExcelUtils.readExcel(excelFile);
        for (String[] ml : excelArr) {
            String gbqx = ml[1];
            String ajh = ml[3];
            String year = ml[4];
            String key = gbqx+"_"+ajh;
            archivalMap.put(key, year);
        }

        String filePath = "C:\\Users\\AB_ZhangLei\\Desktop\\宁波项目\\数据\\sysdba_JNMT - 文书.xls";
        File file = new File(filePath);
        String[][] strings = ExcelUtils.readExcel(file);
        for (String[] string : strings) {
            String gbqx = string[1];
            String ajh = string[3];
            String key = gbqx+"_"+ajh;
            String s = archivalMap.get(key);
            string[4] = s;
            System.out.println(s);
        }

        String filePath2 = "C:\\Users\\AB_ZhangLei\\Desktop\\宁波项目\\数据\\sysdba_JNMT_aaaa.xls";
        File file2 = new File(filePath2);
        file2.createNewFile();
        ExcelUtils.writeExcel(strings,new String[]{},new FileOutputStream(file2));
        System.out.println();
    }


    public static void ningBoGenerateYW() throws Exception {
        String[] nameNum = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        Map<String, String> archivalMap = new HashMap<>();
        String excelPath = "C:\\Users\\AB_ZhangLei\\Desktop\\宁波项目\\数据\\sysdba_GDMT - 副本.xls";
        File excelFile = new File(excelPath);
        String[][] excelArr = ExcelUtils.readExcel(excelFile);
        for (String[] ml : excelArr) {
            String archivalId = ml[22];
            String ywState = ml[5];
            if (ywState.equals("WG06") || StringUtils.isBlank(ywState)) {
                continue;
            }
            Integer integer = Integer.valueOf(ywState);
            if (integer == 0) {
                continue;
            }
            archivalMap.put(ywState, archivalId);
        }
        Map<String, String> fileNameMap = new HashMap<>();
        String filePath = "C:\\Users\\AB_ZhangLei\\Desktop\\宁波项目\\数据\\sysdba_MTFW.xls";
        String zipPackagePathPart = "D:\\ningbo\\media";
        String unzipPath = "D:\\ningbo\\new";
        File file = new File(filePath);
        String[][] strings = ExcelUtils.readExcel(file);
        for (String[] string : strings) {
            String id = string[0];
            String zipName = string[3];
            fileNameMap.put(id, zipName);
        }
        for (String id : archivalMap.keySet()) {
            if (fileNameMap.containsKey(id)) {
                String zipPackagePath = "";
                String unZipPackagePath = "";
                for (String s : nameNum) {
                    String testZipPackagePath = zipPackagePathPart + File.separator + s + File.separator + fileNameMap.get(id);
                    File testFile = new File(testZipPackagePath);
                    if (testFile.exists()) {
                        zipPackagePath = testZipPackagePath;
                        System.out.println(zipPackagePath);
                        unZipPackagePath = unzipPath + File.separator + archivalMap.get(id);
                        System.out.println(unZipPackagePath);
                        System.out.println("---------------------------------------------");
                        File newFolder = new File(unZipPackagePath);
                        if (!newFolder.exists()) {
                            newFolder.mkdir();
                        }
                        ZipFileUtil.unZip(zipPackagePath, unZipPackagePath);
                        break;
                    }
                }
            }

        }
    }

    public static void jieYa(String year) throws IOException {
        File folder = new File("D:\\ningbo\\new");
        File zipDir = new File("D:\\ningbo\\" + year);
        File zip = new File("D:\\ningbo\\" + year + ".zip");
        if (!zipDir.exists()) {
            zipDir.mkdir();
        }
        for (File file : folder.listFiles()) {
            if (file.isDirectory() && file.getName().contains(year)) {
                FileUtils.copyDirectoryToDirectory(file, zipDir);
            }
        }
        ZipFileUtil.zip(zipDir.getPath(), zip.getPath());
        System.out.println("----------------------------------------------------------");
    }

}
