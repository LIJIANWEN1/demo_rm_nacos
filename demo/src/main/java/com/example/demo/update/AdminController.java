package com.example.demo.update;

import cn.amberdata.common.core.common.utils.httpclient.HttpClientUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * admin平台相关接口
 *
 * @author Jiangyang
 * @see
 * @since 2019/8/12
 */

@RestController
@RequestMapping(path = "/admin")
public class AdminController {

    @PostMapping(value = "/update_userByRole")
    public Boolean updateUserByRole(String newRoleName, String oldRoleName,String ip,@RequestBody String Cookie) {
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Cookie", Cookie);
        Map<String, String> a = new HashMap<>(1);
        //获取组织下所有的单位
        String allUnitUrl = "http://"+ip+"/adminapi/org/get_sub_org_list_by_parent_org_id?parentId=1";
        String allUnit = HttpClientUtil.doGet(allUnitUrl, a, headers);
        List<JSONObject> date = new Gson().fromJson(allUnit, new TypeToken<List<JSONObject>>() {
        }.getType());
        for (JSONObject jsonObject : date) {
            String unitId = jsonObject.getString("id");
            String unitName = jsonObject.getString("name");
            String unitCode = jsonObject.getString("code");
            System.out.println(unitId + "----" + unitName);
            //获取单位下所有的角色
            String allRoleUrl = "http://"+ip+"/adminapi/org/get_sub_org_list_by_parent_org_id?parentId=" + unitId;
            String allRole = HttpClientUtil.doGet(allRoleUrl, a, headers);
            List<JSONObject> roleData = new Gson().fromJson(allRole, new TypeToken<List<JSONObject>>() {
            }.getType());
            for (JSONObject jsonObject2 : roleData) {
                String roleId = jsonObject2.getString("id");
                String roleName = jsonObject2.getString("name");
                System.out.println(roleId + "----" + roleName);
                if (oldRoleName.equals(roleName)) {
                    //创建新角色
                    String createNewRoleUrl = "http://"+ip+"/adminapi/role/create_role";
                    String createNewRole = "{\n" +
                            "  \"name\": \"" + newRoleName + "\",\n" +
                            "  \"code\": \"" + unitCode + "_dagly" + "\",\n" +
                            "  \"remarks\": \"档案管理员角色\",\n" +
                            "  \"imagePath\": \"\",\n" +
                            "  \"parentId\": \"" + unitId + "\",\n" +
                            "  \"type\": 4\n" +
                            "}";
                    String isCreate = HttpClientUtil.doPostJson(createNewRoleUrl, createNewRole, headers);
                    System.out.println(isCreate);

                    //获取原先角色下的用户
                    String url = "http://"+ip+"/adminapi/org/get_org_members_list_by_parent_org_id?parentId=" + roleId;
                    String json = "{\n" +
                            "  \"currentPage\": \"1\",\n" +
                            "  \"pageCount\": 0,\n" +
                            "  \"pageSize\": \"200\",\n" +
                            "  \"totalCount\": 0\n" +
                            "}";
                    String queryRet = HttpClientUtil.doPostJson(url, json, headers);
                    String data1 = JSONObject.parseObject(queryRet).getString("resultSet");
                    List<JSONObject> o = new Gson().fromJson(data1, new TypeToken<List<JSONObject>>() {
                    }.getType());
                    for (JSONObject jsonObject3 : o) {
                        String userId = jsonObject3.getString("id");
                        String userName = jsonObject3.getString("name");
                        System.out.println(userId + "----" + userName);
                        //获取单位下所有的角色
                        String allRoleUrl2 = "http://"+ip+"/adminapi/org/get_sub_org_list_by_parent_org_id?parentId=" + unitId;
                        String allRole2 = HttpClientUtil.doGet(allRoleUrl2, a, headers);
                        List<JSONObject> roleData2 = new Gson().fromJson(allRole2, new TypeToken<List<JSONObject>>() {
                        }.getType());
                        for (JSONObject jsonObject4 : roleData2) {
                            String newRoleId = jsonObject4.getString("id");
                            String newRoleName2 = jsonObject4.getString("name");
                            System.out.println(newRoleId + "----" + newRoleName2);
                            if (newRoleName2.equals(newRoleName)) {
                                //将用户添加到新角色下
                                String addUserTORoleUrl = "http://"+ip+"/adminapi/org/add_users_to_org?parentId=" + newRoleId;
                                String addUserToRole = "[\n" +
                                        "  \"" + userId + "\"\n" +
                                        "]";
                                String isTrue = HttpClientUtil.doPostJson(addUserTORoleUrl, addUserToRole, headers);
                                System.out.println(isTrue);
                            }
                        }
                    }
                }

            }
        }


        //将用户从原先角色中移除
//        String removeUserFromRoleUrl = "http://192.168.10.52/adminapi/org/remove_org_users?parentId=9acf5ee3-8ae9-447e-a81e-d28ad0e7126d";
//        String isRemove = HttpClientUtil.doPostJson(removeUserFromRoleUrl, json2, headers);
//
//        System.out.println(isRemove);
        return true;
    }
}
