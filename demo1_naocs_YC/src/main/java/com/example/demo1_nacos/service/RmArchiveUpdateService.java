package com.example.demo1_nacos.service;
import cn.amberdata.cache.CacheTemplate;
import cn.amberdata.common.contenttransfer.ContentTransfer;
import cn.amberdata.common.util.httpclient.HttpClientUtil;
import cn.amberdata.common.util.md5.MD5Utils;
import cn.amberdata.common.util.zip.CompressUtils;
import cn.amberdata.metadata.domain.metadatascheme.template.record.Record;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo1_nacos.importdata.SelectUtil;
import com.example.demo1_nacos.mapper.DataMapper;
import com.example.demo1_nacos.pojo.MetadataColumn;
import com.example.demo1_nacos.pojo.TemplateMetadata;
import com.example.demo1_nacos.pojo.YC.CertificatePO;
import com.example.demo1_nacos.pojo.YC.OrderInfo;
import com.example.demo1_nacos.pojo.YC.YCResult;
import com.google.gson.Gson;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.minio.MinioClient;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 17:50
 */
@Service
public class RmArchiveUpdateService {

    @Resource
    private IMetadataService metadataService;

    @Resource
    private DataMapper dataMapper;

    @Resource
    private CacheTemplate cacheTemplate;

    private static String appKey = "2b1e37d32079c22135c2f4657eb6378f";

    private static String ip = "192.168.10.77";

    private static String bucketName = "datacenter";

    private static String appSecret = "101aa9a0ce675b47c30cb19884e0959ff";

    private static String PZmetadataSchemeId = "acefd0a8-0e1d-47c9-9e45-8b0d2ce479b1";

    private static String DJmetadataSchemeId = "845758e0-f2e2-4d0d-a4ad-8f3b01bd00f9";

    private static String submitEDASUrl = "http://192.168.10.78/dataarchivesapi/data/submit";

    private static String accountCode = "1584244570835057173";

    @Value("${amberdata.unitName}")
    private  String unitName;

    @Value("${amberdata.unitCode}")
    private  String unitCode;

    public String generateMetadataPackage() throws Exception {
        Record metadataRecord = new Record();
        String token = getToken();
        System.out.println("--------1.获取token--------"+token);
        YCResult ycResult = getCertificateList(token);
        if (null == ycResult.getData()) {
            System.out.println("-----未获取到数据-----");
            return "未获取到数据";
        }
        System.out.println("--------2.获取有成凭证数据--------"+ycResult.getData().getList().size());
        TemplateMetadata templateMetadata = metadataService.findTemplateByMetadataSchemeIdAndVersionNo(PZmetadataSchemeId, null);
        String jsonMetadataTemplate = metadataService.getJsonTemplateById(templateMetadata.getId());
        List<MetadataColumn> columns = metadataService.getMetadataColumnByMetadataSchemeId(PZmetadataSchemeId);
        DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
        TemplateMetadata djTemplateMetadata = metadataService.findTemplateByMetadataSchemeIdAndVersionNo(DJmetadataSchemeId, null);
        String djJsonMetadataTemplate = metadataService.getJsonTemplateById(djTemplateMetadata.getId());
        List<MetadataColumn> djColumns = metadataService.getMetadataColumnByMetadataSchemeId(DJmetadataSchemeId);
        System.out.println("--------3.获取元数据模板--------");
        DocumentContext djDocumentContext = JsonPath.parse(djJsonMetadataTemplate);
        for (CertificatePO certificatePO : ycResult.getData().getList()) {
            Map<String, Object> certificateMap = generateCertificateMap(certificatePO);
            saveJsonData(documentContext, certificateMap, columns);
            String certificateXml = metadataRecord.getMetadataXml(documentContext.jsonString());
            File certificateFile = writXmlToFile(certificateXml);
            submitPackage("KJPZ","KJPZ","会计凭证","exchange_area/"+certificateFile.getName(),
                    MD5Utils.getMD5(certificateFile),certificateFile.getName());
            System.out.println("--------4.提交凭证数据包--------");
            for (OrderInfo orderInfo : certificatePO.getOrderInfoList()) {
                Map<String, Object> orderMap = generateOrderMap(orderInfo);
                saveJsonData(djDocumentContext, orderMap, djColumns);
                String orderXml = metadataRecord.getMetadataXml(djDocumentContext.jsonString());
                File orderFile = writXmlToFile(orderXml);
                submitPackage("DJ","DJ","单据","exchange_area/"+orderFile.getName(),
                        MD5Utils.getMD5(orderFile),orderFile.getName());
                System.out.println("--------4.提交单据数据包--------");
            }
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("certificateId",certificatePO.getCertificateId());
            map.put("status",1);
            map.put("voucherCode","xxxx");
            list.add(map);
            callback(token,list);
        }
      return "数据提交成功";
    }

    private void submitPackage(String businessCode,String taskCode,String taskName,String packagePath,String md5Code,
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

    public void callback(String accessToken,List<Map<String,Object>> certificateIdList) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("accountCode", "1584244570835057173");
        map.put("businessType", "file");
        map.put("certificateIdList",  certificateIdList);
        String url = "https://yiqbdatatest.superboss.cc/reimburse/certificate/callback.rjson?appKey="+appKey+"&accessToken="+accessToken;
        String jsonStr = HttpClientUtil.doPostJson(url, JSON.toJSONString(map));
        System.out.println(jsonStr);
        System.out.println("--------5.回调凭证接口--------");
    }

    public void data(){
        List<Map<String, Object>> byWrapper = dataMapper.findByWrapper();
        for (Map<String, Object> map : byWrapper) {
            String archivalId = (String) map.get("archival_id");
            if (StringUtils.isBlank(archivalId)){
                System.out.println("----------档号为空："+archivalId);
            }
            String filePath = "D:\\ningbo\\备份\\1\\归档文件管理media\\GDMT\\new\\";
            File folder = new File(filePath);
            if (folder.exists()) {
                folder.mkdir();
                System.out.println("------创建目录："+archivalId);
            }
            System.out.println("-----");
        }
    }

    public YCResult getCertificateList(String token) {
        String url = "https://yiqbdatatest.superboss.cc/reimburse/certificate/queryListPage.rjson?appKey=" + appKey + "&accessToken=" + token;
        Map<String, Object> map = new HashMap<>(2);
        map.put("accountCode", "1584244570835057173");
        map.put("businessType","file");
        map.put("status",1);
//        map.put("beginTime", new SimpleDateFormat("yyyy-MM-dd").format(new Date())+" 00:00:00");
//        map.put("endTime",  new SimpleDateFormat("yyyy-MM-dd").format(new Date())+" 24:00:00");
        map.put("pageSize", 1);
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
        map.put("content_type","02");
        //说明信息
        map.put("retention_period", "D30");
        map.put("doc_date",certificatePO.getCertificateDate());
        map.put("carrier_type","02");
        //内容描述信息
        map.put("je_submit_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        map.put("approve_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        map.put("ledger_name", "金蝶开发者测试账套");
        map.put("certificateId", certificatePO.getCertificateId());
        map.put("certificateWord", certificatePO.getCertificateWord());
        map.put("certificateDate", certificatePO.getCertificateDate());
        map.put("certificateCode", certificatePO.getCertificateCode());
        map.put("documentMaker", certificatePO.getDocumentMaker());
        map.put("file_source_system","有成财务系统");
        return map;
    }

    private Map<String, Object> generateOrderMap(OrderInfo orderInfo) {
        Map<String, Object> map = new HashMap<>();
        //资源标识
        map.put("archive_1st_category", "CW");
        //访问控制信息
        map.put("content_type","02");
        //说明信息
        map.put("retention_period", "D30");
        map.put("doc_date",new SimpleDateFormat("yyyy-MM-DD").format(new Date()));
        map.put("carrier_type","02");
        //内容描述信息
        map.put("orderCode", orderInfo.getOrderCode());
        map.put("orderName", orderInfo.getOrderName());
        map.put("department", orderInfo.getDepartment());
        map.put("submitAmount", orderInfo.getSubmitAmount());
        map.put("submitorName",orderInfo.getSubmitorName());
        map.put("file_source_system","有成财务系统");
        return map;
    }

    public String getToken() {
        String yc_token = cacheTemplate.get("YC_token");
        if (StringUtils.isBlank(yc_token)) {
            String tokenUrl = "https://yiqbdatatest.superboss.cc/auth/getAccessToken.rjson";
            Map<String, String> map = new HashMap<>(2);
            map.put("appKey", appKey);
            map.put("appSecret", appSecret);
            String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map));
            JSONObject json = JSON.parseObject(jsonStr);
            JSONObject data = (JSONObject) json.get("data");
            yc_token = data.get("accessToken").toString();
            cacheTemplate.set("YC_token", yc_token, 3600);
        }
        System.out.println(yc_token);
        return yc_token;
    }

    private static String saveJsonData(DocumentContext jsonPath, Map<String, Object> map, List<MetadataColumn> metadataColumns) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < metadataColumns.size(); ++j) {
            MetadataColumn metadataColumn = metadataColumns.get(j);
            String allPath = metadataColumn.getJsonPath();
            //json 元数据name，元数据code不能设置为空
            if ("$.record.metadata_scheme_code".equals(allPath) || "$.record.metadata_scheme_name".equals(allPath) || "$.record.version_no".equals(allPath)) {
                continue;
            }
            Object value = map.get(metadataColumn.getAttrName());
            if (StringUtils.isNotBlank(allPath)) {
                String[] format = null;
                String key = allPath.substring(allPath.lastIndexOf(".") + 1);
                String path = allPath.substring(0, allPath.lastIndexOf("."));
                try {
                    if (metadataColumn.getType() != null && !SelectUtil.TYPE_STRING.equals(metadataColumn.getType()) && null != value && StringUtils.isNotBlank(value.toString())) {
                        value = SelectUtil.getValueFromJsonStr(value.toString(), metadataColumn.getDisplayName(), metadataColumn.getType(), format);
                    }
                    if (null != value) {
                        jsonPath.put(path, key, value);
                    } else {
                        jsonPath.put(path, key, "");
                    }
                } catch (Exception e) {
                    return stringBuilder.toString();
                }
            }
        }
        return stringBuilder.toString();
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
        File file = new File("C:\\Users\\AB_ZhangLei\\Downloads\\355a532f-bd2d-4fdd-84f3-504bc5267042.zip");
        uploadTest(file);
    }

    public static void uploadTest(File file) throws Exception {
        try {
            //初始化
            MinioClient minioClient = new MinioClient("http://"+ip+":9000", "admin", "Dctm@1234");
            //判断桶是否存在
            Boolean found = minioClient.bucketExists("yctest");
            if (found) {
                //上传文件
                minioClient.putObject(bucketName, "exchange_area/"+file.getName(), file.getPath(), null);
                System.out.println("上传成功");
            } else {
                System.out.println("桶不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
