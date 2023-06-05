package com.example.demo1_nacos.service;
import cn.amberdata.common.util.excel.old.ExcelUtils;
import cn.amberdata.common.util.httpclient.HttpClientUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2023/2/16 17:15
 */
public class OrganizationServiceImpl {

    public static void main(String[] args) throws Exception {
        Map<String,String> headers = new HashMap<>();
        headers.put("Cookie","JSESSIONID=D3DF903A36C7FE2CBD44934B138F0284; hs_swap_NG_TRANSLATE_LANG_KEY=cn; LoginUnitId=f3d9d657-1224-4757-bf8f-a8b218499c83; IAM_TOKEN=eyJraWQiOiJsdUlVMzN3UVlPRWluVXQrUUhXQ01JcmZjNzEvT0lTMVhhSGFyNk9xN2lMWjcrZW9hSGZvM2h2L0dORkd5TFEyZTA3Yk00b1pkVnlNXG5QbUZKMHpHMENBPT0iLCJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJsZ19uYW1lXCI6XCJhZG1pbkBhbWJlcmRhdGEuY25cIixcIm5vbmNlXCI6XCIxOTkzZmMzY2UxZjJhZGExZjg3Mzg3MjFlYmI0MjExYlwiLFwidXNfc291cmNlXCI6XCJpbmxpbmUgcGFzc3dvcmRcIn0ifQ.yP1eimun0JAbE7wTyZz_qDcBOWh6flw8htNvsmnzWcw; CURR_UNIT_INFO=7E+MSdWEHrpYSpgxhWtyY+v5cKZNfMy/uYwMORjRZQsaiJZXz30Cn4tX/5oX97iTnAXrmwWyjTcm3UNZyWotKXAllhG85FNHT1kicrKISri6iL+ZzUVX4O4XR1uk+N3x; RANDOM_STAMP=0369b398-682b-4f20-a11e-1bec812335a4; AMBER_SSO_V1=tPwKr6Ufw6OWFuOws161Ag==");
        addArchiveManager("10.50.128.217:18080","d460331b-99fb-4d79-8ec3-9c7cc6af4acd","sf02",headers);
    }
    public static void addRole(String ip) throws Exception {
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
//            //添加浙政钉用户
//            String zzdUserId = getUserByName(ip,daglyName,headers,id);
//            addUserToRole(ip,zzdUserId,daglyRoleId,headers);
//            //创建审核员角色
//            createRole(ip, "一级审批员", fondsId + "_yjspy", id,headers);
//            createRole(ip, "二级审批员", fondsId + "_rjspy", id,headers);
//            //添加一级审核员
//            String yjiRoleId = getSubOrgListByParentOrgId(ip, id, "4",fondsId+"_yjspy",headers);
//            addUserToRole(ip,zzdUserId,yjiRoleId,headers);
//            //添加二级审核员
//            String fenguanUserId = getUserByName(ip,fenGuanName,headers,id);
//            String rjiRoleId = getSubOrgListByParentOrgId(ip, id, "4",fondsId+"_rjspy",headers);
//            addUserToRole(ip,fenguanUserId,rjiRoleId,headers);
        }
    }

    public static void addArchiveManager(String ip, String id, String fondsId, Map<String, String> headers){
        //机构类型（1：公司或单位；2：部门；3：用户组；4：角色）
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


}
