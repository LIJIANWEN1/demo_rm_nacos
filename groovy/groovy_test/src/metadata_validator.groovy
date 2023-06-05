import cn.amberdata.common.util.excel.ExcelUtilEx
import cn.amberdata.common.util.httpclient.HttpClientUtil
import cn.amberdata.metadata.facade.MetadataColumnConfigFacade
import cn.amberdata.metadata.facade.TemplateFacade
import cn.amberdata.metadata.facade.dto.MetadataColumnConfigDTO
import cn.amberdata.metadata.facade.dto.PropertyDTO
import cn.amberdata.metadata.facade.dto.TemplateDTO
import com.alibaba.fastjson.JSON
import com.alipay.sofa.rpc.config.ConsumerConfig
import com.alipay.sofa.rpc.config.RegistryConfig
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import lombok.Data
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.nifi.processor.io.InputStreamCallback

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

// 内置变量
// ProcessSession 提供FlowFile获取、传输、读写等方法
// GroovyProcessSessionWrap session
// 内置日志对象
// ComponentLog log
// 内置关联关系
// Relationship REL_SUCCESS
// Relationship REL_FAILURE
//===================================================================
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-annotations', version = '2.13.4')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-core', version = '2.13.4')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-databind', version = '2.13.4')
@Grab(group = 'com.jayway.jsonpath', module = 'json-path', version = '2.4.0')
@Grab(group = 'com.google.code.gson', module = 'gson', version = '2.8.6')
@Grab(group = 'mysql', module = 'mysql-connector-java', version = '8.0.19')
@Grab(group = 'com.alipay.sofa', module = 'sofa-rpc-all', version = '5.6.7')
@Grab(group = 'org.projectlombok', module = 'lombok', version = '1.18.6')
@Grab(group = 'org.apache.commons', module = 'commons-lang3', version = '3.7')
@Grab(group = 'com.alibaba.nacos', module = 'nacos-api', version = '1.4.1')
@Grab(group = 'com.alibaba.nacos', module = 'nacos-client', version = '1.4.1')
@Grab(group = 'org.apache.commons', module = 'commons-lang3', version = '3.7')
//tLBGXXX 分类表 通过 LBH 和 tBGXXX 的 LBH 关联
//tQZGL 全宗单位  tXTDM 标准代码项
//tZDXX 表字段名  tYWXX 原文表
Map<String,String> tableCategoryMap = new HashMap<>();
//旧表明，档案路径，元数据方案id,整理方式，类型
tableCategoryMap.put("u_20211104092608","/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）@629d7d51-7bc0-4a20-8798-168b621fa72d@volume@volume@INIT_CLASS")
tableCategoryMap.put("u_20211104092839","/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）@d5172839-ae59-4a42-8521-c5873ee9ec6f@volume@record@INIT_CLASS")
tableCategoryMap.put("u_20211104094217","/RM.档案管理/档案库/YW.业务档案/B.业务档案（卷）@e11bf43c-0e00-447f-9ccf-7b6684ab3bc6@volume@volume@INIT_CLASS")
tableCategoryMap.put("u_20211104094336","/RM.档案管理/档案库/YW.业务档案/B.业务档案（卷）@775eb3c1-b7c3-4c95-9a95-1996a30b82aa@volume@record@INIT_CLASS")
tableCategoryMap.put("u_20211104094234","/RM.档案管理/档案库/NH.农户档案/B.农户档案（卷）@56595b7f-a433-451a-b121-0b151723f797@volume@volume@INIT_CLASS")
tableCategoryMap.put("u_20211104094234","/RM.档案管理/档案库/NH.农户档案/B.农户档案（卷）@56595b7f-a433-451a-b121-0b151723f797@volume@volume@INIT_CLASS")
tableCategoryMap.put("u_20211104101505","/RM.档案管理/档案库/SW.实物档案@bccbceb8-35da-4b34-b71e-87633b48b19a@record@record@INIT_CLASS")
//tableCategoryMap.put("u_20211104094234","/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）_e11bf43c-0e00-447f-9ccf-7b6684ab3bc6_volume_record")
String unitName = "石佛村"
String unitCode = "bd8fdb07-cdb9-4473-9d05-d72845e5073d";
String FLBH = "3"
DbManager dbManager = new DbManager();
Sql sqlIns = dbManager.getDBClient();
Sql mysqlIns = dbManager.getMysqlDBClient();
try {
    List<Map<String,String>> list = new LinkedList<>();
    Map<String,String> headers = new HashMap<>();
    ERMSapi ermSapi = new ERMSapi("localhost:8099","10.50.128.217:18080","10.50.128.217:18080",headers)
    OldERMSapi oldERMSapi = new OldERMSapi(sqlIns,mysqlIns);
    oldERMSapi.getDisplayNameByLBH(list,FLBH)
    for(Map<String,String> map : list) {
        String LBMC = map.get("LBMC");
        if (!LBMC.contains("实物")) {
            continue
        }
        String LBH = map.get("LBH");
        List<Map<String, String>> tbList = oldERMSapi.getTbNameAndDataCountByLBH(LBH)
        Map<String, String> ruleMap = oldERMSapi.getRules()
        for (Map<String, String> cmap : tbList) {
            Map<String, List<String>> fFieldMap = oldERMSapi.queryFieldsByTableName(cmap.get("egName"))
            List<Map<String, String>> data1 = oldERMSapi.querySqlServerData(null, fFieldMap.get("fields"), cmap.get("egName"), ruleMap)
            ermSapi.sendImportRequest(unitName, unitCode, tableCategoryMap, cmap.get("egName"), data1, oldERMSapi);
        }
    }
//            excelMap.put("案卷",oldERMSapi.convertToTwoArray(fFieldMap.get("titles"),data1));
//            excelMap.put("文件级",oldERMSapi.convertToTwoArray(cFieldMap.get("titles"),data2))
//            oldERMSapi.exportExcel(unitName+"_"+cmap.get("disPlayName"),excelMap);
} catch (Exception e) {
    print(e.printStackTrace())
}finally{
    dbManager.closeMysqlDbConnection()
    dbManager.closeSqlSeverDbConnection();
}


class OldERMSapi{

    Sql sqlInstance
    Sql mysqlSqlInstance

    OldERMSapi(Sql sqlInstance,Sql mysqlSqlInstance) {
        this.sqlInstance = sqlInstance;
        this.mysqlSqlInstance = mysqlSqlInstance
    }

    List<Map<String,String>> getDisplayNameByLBH(List<Map<String,String>> dataList,String FLBH){
        String sql = "select LBH,LBMC from tLBGXXX where FLBH = '"+FLBH+"'"
        List<Map<String,String>>  dblist = sqlInstance.rows(sql)
        for(Map<String,String> map : dblist){
            String cLBH = map.get("LBH")
            if("67".equals(cLBH)){
                continue
            }
            String cLBMC = map.get("LBMC")
            List<Map<String,String>>  cdblist = getDisplayNameByLBH(dataList,cLBH);
            if(cdblist.size() == 0){
                Map<String,String> cmap = new HashMap<>();
                cmap.put("LBMC",cLBMC);
                cmap.put("LBH",cLBH);
                dataList.add(cmap);
            }
        }
        return dblist
    }

    List<Map<String,String>> getTbNameAndDataCountByLBH(String lbh){
        List<Map> returnList = new ArrayList<>();
        String sql = "select BM,ZWBM,FBBM from tBGXXX where LBH = '"+lbh+"' order by CCH"
        sqlInstance.eachRow(sql) { row ->
            String tableName = "$row.ZWBM"
            String engLishTableName = "$row.BM"
            String fEngLishTableName = "$row.FBBM"
            String countSql = 'select count(*) from ' + engLishTableName;
            Integer res = sqlInstance.firstRow(countSql).values().first()
            Map<String,String> dataTableName = new HashMap<>();
            dataTableName.put("disPlayName",tableName)
            dataTableName.put("egName",engLishTableName)
            dataTableName.put("fEgName",fEngLishTableName)
            dataTableName.put("count",res)
            returnList.add(dataTableName)
        }
        return returnList
    }

    List<String> queryYearByTb(String tb) {
        List<String> years = new ArrayList<>();
        try{
            String sql = 'select distinct nd from ' + tb +' order by nd asc'
            sqlInstance.eachRow(sql) { row ->
                String nd = "$row.nd"
                years.add(nd)
            }
        }catch(Exception e){
            years.add("无nd字段")
        }

        return years
    }

    List querySqlServerData(String fondsId,List<String> fieldList,String tableName,Map<String,String> ruleMap) {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0;i<fieldList.size();i++){
            stringBuffer.append(tableName).append(".").append(fieldList.get(i)).append(" AS ").append(ruleMap.get(fieldList.get(i))).append(",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("select ").append(stringBuffer.toString()).append(" from ").append(tableName)
        if(!StringUtils.isBlank(fondsId)){
            sqlBuffer.append(" where qzh='").append(fondsId).append("'")
        }
        String sql = sqlBuffer.toString();
        println(sql)
        List<Map<String,String>> list = sqlInstance.rows(sql)
        println("--------" + list.size())
        return list
    }

    Map<String,List<String>> queryFieldsByTableName(String tbname) {
        Map<String,List<String>> map = new HashMap<>();
        List<String> titles = new ArrayList<>();
        List<String> fields  = new ArrayList<>();
//            String sql = "select kjZdmSx,bm from tKJSX where bm = '"+tbname+"'"
        String sql = "select ZDM,BM,ZDZWM from tzdxx where BM = '"+tbname+"'"
        sqlInstance.eachRow(sql) {  row ->
            String oldField = "$row.ZDM"
//            String displayName = "$row.ZDZWM"
//            titles.add(displayName)
            fields.add(oldField);
        }
        map.put("titles",convertHeaders(fields))
        map.put("fields",fields)
//                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return map
    }

    Map<String,String> getRules() {
        Map<String,String> ruleMap = new HashMap<>();
        mysqlSqlInstance.eachRow("select * from matching_rule where new_field is not null ") { it ->
            String newFd = "${it.new_field}"
            String oldFd = "${it.old_field}"
            ruleMap.put(oldFd, newFd)
        }
    return ruleMap;
    }

    void exportExcel(String fileName, Map<String, String[][]> data) throws Exception {
        String excelName = "C:\\Users\\AB_ZhangLei\\Desktop\\现场环境\\龙游项目\\数据迁移\\脚本\\" + fileName + ".xlsx";
        ExcelUtilEx.creatExcel(excelName, data);
        print("导出成功---" + fileName)
    }

    public static Map<String, Integer> sortMap(Map<String, Integer> map) {
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        //利用Collections的sort方法对list排序
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                //正序排列，倒序反过来
                return o1.getValue() - o2.getValue();
            }
        });
        //遍历排序好的list，一定要放进LinkedHashMap，因为只有LinkedHashMap是根据插入顺序进行存储
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String,Integer> e : entryList
        ) {
            linkedHashMap.put(e.getKey(),e.getValue());
        }
        return linkedHashMap;
    }

    List<String> convertHeaders(List<String> oldFields){
        Map<String,String> map = new HashMap<>()
        List<String> newHeader = new ArrayList<>();
        mysqlSqlInstance.eachRow("select old_field,new_description from matching_rule where new_field is not null ") { it ->
            String oldFd = "${it.old_field}"
            String des = "${it.new_description}"
            map.put(oldFd, des)
        }
        for(String s:oldFields){
            if(map.containsKey(s)){
                newHeader.add(map.get(s))
            }else{
                newHeader.add(s)
            }
        }
        return newHeader
    }

    String[][] convertToTwoArray( List<String> title,List<Map<String,String>> list){
            String[][] z = new String[list.size()][];
            for (int i = 1; i < z.length; i++) {
                Map<String, String> map1 = list.get(i);
                Set set = map1.keySet();
                z[i] = new String[map1.size()];
                Iterator it = set.iterator();
                for (int j = 0; it.hasNext(); j++) {
                    String s = (String) it.next();
                    if (map1.get(s) != null) {
                        z[i][j] = map1.get(s).toString();
                    }
                }
            }
            z[0] = title.toArray()
        return z;
        }

    }

class ERMSapi{

    String ermsIp

    String adminIp

    String localIp

    Map<String, String> headers

    ERMSapi(String localIp,String adminIp,String ermsIp,Map<String, String> headers) {
        this.localIp = localIp;
        this.adminIp = adminIp
        this.ermsIp = ermsIp
        this.headers = headers
    }

    void sendImportRequest(String unitName,String unitCode,Map<String,String> tableCategoryMap,String tableName,List<Map<String,String>> data,OldERMSapi oldERMSapi){
        if(tableCategoryMap.containsKey(tableName)){
            String val = tableCategoryMap.get(tableName)
            String parentPath = "/"+unitName+"-"+unitCode+ val.split("@")[0]
            String metadataId = val.split("@")[1]
            String collectionWay = val.split("@")[2]
            String type = val.split("@")[3]
            String classRule = val.split("@")[4]
            combinedImportArchivePackage(metadataId,collectionWay,type,unitCode,parentPath,data,classRule)
        }else{
            println("没有该表映射："+tableName)
        }
    }

    void combinedImportArchivePackage(String metadataSchemeId,String collectionWay, String type,String unitCode,String parentPath,
                                      List<Map<String,Object>> dataList,String classRule){
            String tokenUrl = "http://" + localIp + "/demoapi/rm_archive/import_archives";
            Map<String, String> map = new HashMap<>(2);
            map.put("metadataSchemeId", metadataSchemeId);
            map.put("collectionWay", collectionWay);
            map.put("type", type);
            map.put("unitCode", unitCode);
            map.put("parentPath", parentPath);
            map.put("dataList", dataList);
            map.put("classRule",classRule)
            String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map),headers);
            System.out.println(jsonStr);
    }

    void  createClass(String nd,String categoryId){
        String tokenUrl = "http://" + localIp + "/demoapi/rm_archive_other/create_class";
        Map<String, String> map = new HashMap<>(2);
        map.put("categoryId", categoryId);
        map.put("classNumber", "");
        map.put("classificationCodePrefix", null);
        map.put("dataSync", "false");
        map.put("description", "");
        map.put("name", nd);
        map.put("parentId", categoryId);
        map.put("retentionPeriodId", "14bae594-c374-4bba-8c5c-95c12aaa0124");
        map.put("retentionPolicyId", "bf14053038209662976");
        String jsonStr = HttpClientUtil.doPostJson(tokenUrl, JSON.toJSONString(map));
        System.out.println(jsonStr);
    }

    void  getClassByNameAndPath(String name,String path){
        String tokenUrl = "http://" + localIp + "/demoapi/rm_archive_other/get_subcategory_id_by_path";
        Map<String, String> map = new HashMap<>(2);
        map.put("name", name);
        map.put("path", path);
        String jsonStr = HttpClientUtil.doGet(tokenUrl, map,headers);
        System.out.println(jsonStr);
    }

    String  getIdByPath(String path){
        String tokenUrl = "http://" + localIp + "/demoapi/rm_archive_other/get_by_path";
        Map<String, String> map = new HashMap<>(2);
        map.put("path", path);
        String jsonStr = HttpClientUtil.doGet(tokenUrl, map,headers);
        return jsonStr
    }

    void getAdminUnitByCode(String code){
        String tokenUrl = "http://" + adminIp + "/adminapi/unit/get_unit_by_code";
        Map<String, String> map = new HashMap<>(2);
        map.put("code", code);
        String jsonStr = HttpClientUtil.doGet(tokenUrl, map,headers);
        System.out.println(jsonStr);
    }

//    getClassByCategoryId(){
//        String data = "{\"code\":\"200\",\"data\":[{\"id\":\"bf14063843609182209\",\"parentId\":\"bf14063843600793601\",\"name\":\"00.初始类目\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/00.初始类目\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"001\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":\"初始类目(00)\",\"classNumber\":\"00\"},{\"id\":\"bf14063933182738432\",\"parentId\":\"bf14063843600793601\",\"name\":\"2003\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2003\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"002\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933182738433\",\"parentId\":\"bf14063843600793601\",\"name\":\"2010\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2010\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"003\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933191127040\",\"parentId\":\"bf14063843600793601\",\"name\":\"2012\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2012\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"004\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933191127041\",\"parentId\":\"bf14063843600793601\",\"name\":\"2013\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2013\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"005\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933199515648\",\"parentId\":\"bf14063843600793601\",\"name\":\"2014\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2014\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"006\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933207904256\",\"parentId\":\"bf14063843600793601\",\"name\":\"2015\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2015\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"007\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933207904257\",\"parentId\":\"bf14063843600793601\",\"name\":\"2016\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2016\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"008\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933216292864\",\"parentId\":\"bf14063843600793601\",\"name\":\"2017\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2017\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"009\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933216292865\",\"parentId\":\"bf14063843600793601\",\"name\":\"2018\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2018\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"010\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933224681472\",\"parentId\":\"bf14063843600793601\",\"name\":\"2019\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2019\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"011\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"},{\"id\":\"bf14063933224681473\",\"parentId\":\"bf14063843600793601\",\"name\":\"2020\",\"childCount\":\"0\",\"folderPath\":\"/石佛村-bd8fdb07-cdb9-4473-9d05-d72845e5073d/RM.档案管理/档案库/WS.文书档案/B.文书档案（卷）/2020\",\"folderType\":\"da_class\",\"filePlanType\":null,\"hasChild\":false,\"childType\":null,\"sort\":null,\"codePrefix\":null,\"classificationCodePrefix\":null,\"code\":\"012\",\"archiveCount\":null,\"value\":null,\"templateType\":null,\"templateName\":null,\"otherName\":null,\"classNumber\":\"\"}],\"message\":null,\"timestamp\":1676553097250,\"uri\":\"/ermsapi/navigation/get_tree_node_list_by_parent_id\",\"requestId\":null}"
//        http://10.50.128.217:18080/ermsapi/navigation/get_tree_node_list_by_parent_id?parentId=bf14063843600793601&_=1676551500439
//    }
}



class Validator {
    static RegistryConfig registryConfig = new RegistryConfig()
            .setProtocol("nacos")
            .setParameters(new HashMap<String, String>())
            .setSubscribe(true)
            .setAddress("10.50.128.217:8848/amberdata")
            .setRegister(true);

    Sql sqlInstance;

    Sql mysqlSqlInstance;

    static MetadataColumnConfigFacade metadataColumnConfigFacade;

    static TemplateFacade templateFacade;

    List<MetadataColumn> columnList;

    String metadataSchemeId;

    Map<String, String> ruleMap = new HashMap<>()


    Validator(Sql sqlInstance,Sql mysqlSqlInstance) {
        this.sqlInstance = sqlInstance;
        this.mysqlSqlInstance = mysqlSqlInstance
    }

    Map<String,Integer> countTableByFondsId(String qzh) {
        Integer a = 0;
        Map<String,Integer> dataTableName = new HashMap<>();
            //        "select name from sysobjects where xtype ='U' and name like 'u_20%'"
            String sql = "select name from sysobjects where xtype = \'U\' and name not in ('wFlowStep','tZHZDXX','tJKRZ'" +
                    ",'tOnline','tQZGL','tSSC','tUser','sLOG','yyda20211103145323','yyda20211103145503','u_AJJML'" +
                    ",'u_JJAJ','u_JJWJ','u_JNJML','u_kuaijiAJ','u_u_KUWJ','u_xzsp')"
            sqlInstance.eachRow(sql) { row ->
                String tableName = "$row.name"
                println tableName
                String countSql = 'select count(*) from ' + tableName + ' where qzh =? ';
                try{
                    Integer res = sqlInstance.firstRow(countSql,[qzh]).values().first()
                    if(res>0){
                        dataTableName.put(tableName,res)
                    }
                }catch (Exception e) {
                    print("---------------------")
                }

            }
        print(a)
        return sortMap(dataTableName)
    }

    Map<String,Integer> queryTableAndInsertTableFields(String tbname) {
        Integer a = 0;
        Map<String,Integer> dataTableName = new HashMap<>();
        try {
            //        "select name from sysobjects where xtype ='U' and name like 'u_20%'"
            String sql = "select ZDM,ZDZWM from tZDXX where BM = '"+tbname+"'"
            sqlInstance.eachRow(sql) { row ->
                String oldField = "$row.ZDM"
                String oldFieldName = "$row.ZDZWM"
                String oldTableName = tbname
                println(oldTableName+"----"+oldField)
                def arr = [oldField,oldFieldName,oldTableName];
                try{
                    mysqlSqlInstance.execute("INSERT INTO matching_rule(old_field,description,old_table_code) VALUES (?,?,?)",arr)
                }catch (Exception e) {
                    print(e.printStackTrace())
                }
            }
        } catch (Exception e) {
            print(e.printStackTrace())
        } finally {
            sqlInstance.close()
            mysqlSqlInstance.close()
        }
        print(a)
        return sortMap(dataTableName)
    }



    void run() {

        //获取数据模板
        TemplateDTO templateDTO = templateFacade.getTemplateByMetadataSchemeIdAndVersionNo(metadataSchemeId, null);
        //根据数据模板id获取空的json表单模板
        def jsonMetadataTemplate = templateFacade.getJsonTemplateById(templateDTO.id);
        print jsonMetadataTemplate
        DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
        for (HashMap<String, String> map1 : list) {
            saveJsonData(documentContext, map1, columnList)
        }
    }


    void initMetadataRpc() {

        ConsumerConfig<MetadataColumnConfigFacade> consumerConfig = new ConsumerConfig<MetadataColumnConfigFacade>()
                .setInterfaceId("cn.amberdata.metadata.facade.MetadataColumnConfigFacade")
                .setUniqueId("metadataColumnConfigFacade")
                .setProtocol("bolt")
                .setRegistry(registryConfig);
        consumerConfig.setRepeatedReferLimit(0);
        metadataColumnConfigFacade = consumerConfig.refer();


        ConsumerConfig<TemplateFacade> templateConsumerConfig = new ConsumerConfig<TemplateFacade>()
                .setInterfaceId("cn.amberdata.metadata.facade.TemplateFacade")
                .setUniqueId("templateFacade")
                .setProtocol("bolt")
                .setRegistry(registryConfig);
        templateConsumerConfig.setRepeatedReferLimit(0)
        templateFacade = templateConsumerConfig.refer();
    }

    String saveJsonData(DocumentContext jsonPath, Map<String, Object> map, List<MetadataColumn> metadataColumns) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < metadataColumns.size(); ++j) {
            MetadataColumn metadataColumn = metadataColumns.get(j);
            String allPath = metadataColumn.getJsonPath();
            //json 元数据name，元数据code不能设置为空
            if ('$.record.metadata_scheme_code'.equals(allPath) || '$.record.metadata_scheme_name'.equals(allPath) || '$.record.version_no'.equals(allPath)) {
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




    void verifyMetadata(String metadataJson, String typeName, String versionNo, List<String> errorMessageList) {
        Map<String, PropertyDTO> pMap = templateFacade.getTemplatePropertyMapping(typeName, versionNo);
        List<Object> propertyMp = JsonPath.parse(metadataJson).read('$..property');
        for (Object o : propertyMp) {
            if (o instanceof LinkedHashMap) {
                LinkedHashMap<String, String> mp = (LinkedHashMap) o;
                validateMetadataProperty(pMap, mp.get("name"), mp.get("content"), errorMessageList);
            } else if (o instanceof ArrayList) {
                ArrayList<LinkedHashMap<String, String>> lmp = (ArrayList<LinkedHashMap<String, String>>) o;
                for (LinkedHashMap<String, String> mp : lmp) {
                    validateMetadataProperty(pMap, mp.get("name"), mp.get("content"), errorMessageList);
                }
            } else {
                // 更改异常类型
//                throw new AfNotSupportNodeTypeException(AfErrorCodeEnum.ERROR_METADATA_NODE_TYPE_NOT_SUPPORT);
            }
        }
        if (pMap.size() > 0) {
            errorMessageList.add(String.format("元数据json缺少属性%s", new ArrayList<>(pMap.keySet()).toString()));
        }
    }

    private void validateMetadataProperty(Map<String, PropertyDTO> pMap, String name, String value, List<String> errorMessageList) {
        // 模板集合为空的话则不进行校验
        if (pMap == null) {
            return;
        }
        if (pMap.get(name) == null) {
            return;
        }

        boolean dataValidate = true;
        PropertyDTO validateProperty = pMap.get(name);

        if (StringUtils.isNotBlank(value)) {
            // 校验元数据信息
            switch (validateProperty.getType()) {
                case "int":
                    dataValidate = DataTypeCalibration.isNumber(value);
                    break;
                case "boolean":
                    dataValidate = DataTypeCalibration.isBool(value);
                    break;
                case "date":
                    //dataValidate = DataTypeCalibration.isDate(value, validateProperty.getTypeFormat());
                    break;
                case "string":
                    break;
                case "float":
                    // 浮点数即可以为整数,也可以为浮点数
                    dataValidate = DataTypeCalibration.isNumber(value) || DataTypeCalibration.isFloat(value);
                    break;
                default:
                    errorMessageList.add(String.format("不支持元数据属性[%s]类型[%s]", validateProperty.getTitle(), validateProperty.getType()));
            }

            if (!dataValidate) {
                errorMessageList.add(String.format("元数据属性[%s]类型不正确,类型应为[%s]", validateProperty.getTitle(), validateProperty.getType()));
            }

            if (!"date".equals(validateProperty.getType()) && validateProperty.getMaxLength() != null) {
                if (value.length() > validateProperty.getMaxLength()) {
                    errorMessageList.add(String.format("元数据属性[%s]长度不正确,长度应小于[%s]", validateProperty.getTitle(), validateProperty.getMaxLength()));
                }
            }

            if (StringUtils.isNotBlank(validateProperty.getAllowedValuesCode())) {
                if (!validateProperty.getAllowedValues().contains(value)) {
                    errorMessageList.add(String.format("元数据属性[%s]可选值不正确,允许可选值[%s]", validateProperty.getTitle(), validateProperty.getAllowedValues()));
                }
            }
        } else {
            // 不允许为空的话则抛出异常
            if (!validateProperty.getNullAble()) {
                errorMessageList.add(String.format("缺少必填字段[%s]", validateProperty.getTitle()));
            }
        }
        pMap.remove(name);
    }


    public static Map<String, Integer> sortMap(Map<String, Integer> map) {
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        //利用Collections的sort方法对list排序
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                //正序排列，倒序反过来
                return o1.getValue() - o2.getValue();
            }
        });
        //遍历排序好的list，一定要放进LinkedHashMap，因为只有LinkedHashMap是根据插入顺序进行存储
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String,Integer> e : entryList
        ) {
            linkedHashMap.put(e.getKey(),e.getValue());
        }
        return linkedHashMap;
    }

}

class DbManager{
    Sql sqlServer;
    Sql mysql;

    Sql getDBClient() {
        def url = 'jdbc:sqlserver://10.50.128.216:1433;Databasename=lyzx'
        def driver = 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
        def username = 'sa'
        def passwd = 'Dctm@1234'
        sqlServer = Sql.newInstance(url, username, passwd, driver)
        return sqlServer
    }

    Sql getMysqlDBClient() {
        def url = 'jdbc:mysql://10.50.128.216:3306/migrate?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai'
        def driver = 'com.mysql.cj.jdbc.Driver'
        def username = 'root'
        def passwd = 'Dctm@1234'
        mysql = Sql.newInstance(url, username, passwd, driver)
       return  mysql
    }

    void closeSqlSeverDbConnection() {
        sqlServer.close()
    }

    void closeMysqlDbConnection() {
        mysql.close()
    }

}

class DataTypeCalibration {
    private static final Pattern NUMBER_PATTERN = Pattern.compile('^-?\\d+$');
    private static final Pattern FLOAT_PATTERN = Pattern.compile('(^[1-9]\\d*\\.\\d+$|^0\\.\\d+$|^[1-9]\\d*$|^0$)');

    DataTypeCalibration() {
    }

    static boolean isNumber(String data) {
        return NUMBER_PATTERN.matcher(data).matches();
    }

    static boolean isBool(String data) {
        return "true".equals(data) || "false".equals(data);
    }

    static boolean isDate(String data, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);

        try {
            LocalDate.parse(data, dtf);
            return true;
        } catch (Exception var4) {
            return false;
        }
    }

    static boolean isFloat(String data) {
        return FLOAT_PATTERN.matcher(data).matches();
    }
}


class SelectUtil {

    static final String TYPE_STRING = "string";

    static final String TYPE_DATE = "date";

    static final List<String> DATE_TYPE_LIST = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "EEE MMM dd HH:mm:ss zzz yyyy", "yyyy-MM-dd", "yyyyMMdd", "yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd 'T' HH:mm:ss");

    static Object getValueFromJsonStr(String value, String column, String type, String... formats) {
        try {
            if (TYPE_STRING.equalsIgnoreCase(type)) {
                return value;
            } else if (TYPE_DATE.equalsIgnoreCase(type)) {
                List<String> dateTypes = new ArrayList<>(DATE_TYPE_LIST.size());
                if (formats != null) {
                    dateTypes.addAll(Arrays.asList(formats));
                }

                dateTypes.addAll(DATE_TYPE_LIST);
                if (StringUtils.isBlank(value)) {
                    return "";
                } else {
                    Date date = null;

                    try {
                        date = DateUtils.parseDate(value, Locale.CHINA, dateTypes.toArray(new String[0]));
                    } catch (Exception var7) {
                        var7.printStackTrace();
                    }

                    if (date == null) {
                        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                        date = format.parse(value);
                    }

                    String strDate = "";
                    if (date != null) {
                        if (formats != null) {
                            SimpleDateFormat format = new SimpleDateFormat(formats[0], Locale.US);
                            strDate = format.format(date);
                        } else {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                            strDate = format.format(date);
                        }
                    }
                    return strDate;
                }
            } else {
                return StringUtils.isBlank(value) ? "" : value + "";
            }
        } catch (Exception var8) {
            throw new RuntimeException(var8);
        }
    }
}


/**
 * @author wd* @since 2022/2/11
 */
@Data
class MetadataColumn {

    /**
     * 属性名
     */
    String attrName;

    /**
     * 显示名
     */
    String displayName;

    /**
     * 英文名
     */
    String titleEn;

    /**
     * 是否被统计
     */
    Boolean sum;

    /**
     * 列宽
     */
    Integer columnWidth;

    /**
     * 是否显示
     */
    Boolean show;

    /**
     * 是否排序
     */
    Boolean sort;

    /**
     * 排序方式(正序：positiveSequence，倒序：negativeSequence )
     */
    String sortWay;

    /**
     * 记录属性的路径
     */
    String jsonPath;

    /**
     * 标记属性类型
     */
    String type;

    /**
     * 代码项标识
     */
    String allowedValuesCode;

    /**
     * 是否写入到数据库
     */
    Boolean required;

    /**
     * 字段格式
     */
    String typeFormat;

}

/**
 * @author wd* @since 2022/1/26
 */
@Data
class TemplateMetadata {

    /**
     * id
     */
    String id;

    /**
     * 元数据方案编号
     */
    String metadataSchemeId;

    /**
     * 版本号
     */
    String versionNo;

    /**
     * 创建时间
     */
    Date createDate;

    /**
     * 修改时间
     */
    Date modifyDate;

    /**
     * 创建人
     */
    String creator;

    /**
     * 修改人
     */
    String modifier;

    /**
     * 模板xml字符串
     */
    String templateXml;

}

