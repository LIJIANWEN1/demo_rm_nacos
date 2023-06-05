package com.example.demo1_nacos.demo;
import cn.amberdata.metadata.facade.MetadataColumnConfigFacade;
import cn.amberdata.metadata.facade.TemplateFacade;
import cn.amberdata.metadata.facade.dto.MetadataColumnConfigDTO;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.rpc.api.GenericService;
import com.alipay.sofa.rpc.api.future.SofaResponseFuture;
import com.alipay.sofa.rpc.boot.container.RegistryConfigContainer;
import com.alipay.sofa.rpc.boot.runtime.binding.RpcBindingType;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.aspose.imaging.internal.ad.S;
import com.example.demo1_nacos.rpc.HelloService;
import com.example.demo1_nacos.rpc.HelloServiceImpl;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2023/2/2 17:11
 */
public class boltInvoke {

    @SofaReference(uniqueId = "metadataColumnConfigFacade", binding = @SofaReferenceBinding(bindingType = "bolt"))
    private MetadataColumnConfigFacade metadataColumnConfigFacade;

    @SofaReference(uniqueId = "templateFacade", binding = @SofaReferenceBinding(bindingType = "bolt"))
    private TemplateFacade templateFacade;

    public static void ddd(String table){
        Map<String,String> map = new HashMap<>();
        map.put("aaa","value-aaa");
        map.put("bbb","value-bbb");
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : map.keySet()) {
            stringBuffer.append(s).append(" AS ").append(table).append(".").append(map.get(s)).append(",");
        }
        stringBuffer.deleteCharAt(stringBuffer.length()-1);
        System.out.println(stringBuffer.toString());
    }

    public static void main(String[] args) {
        ddd("xxx");
//        Map<String,String> map = new HashMap<>();
//        List<Map<String,String>> list = new ArrayList<>();
//        List<Map<String,String>> newList = new ArrayList<>();
//        for (Map<String, String> dataMap : list) {
//            HashMap<String,String> newDataMap = new HashMap<>();
//            for (String s : map.keySet()) {
//                newDataMap.put(s,dataMap.get(map.get(s)));
//            }
//            newList.add(newDataMap);
//        }
////        Map<String,String> map = new HashMap<>();
//////        map.put("username","nacos");
//////        map.put("password","Dctm@1234");
//        RegistryConfig registryConfig = new RegistryConfig()
//                .setProtocol("nacos")
//                .setParameters(map)
//                .setSubscribe(true)
//                .setAddress("10.50.128.217:8848/amberdata")
//                .setRegister(true);
////
////        ServerConfig serverConfig = new ServerConfig()
////                .setProtocol("bolt")
////                .setHost("0.0.0.0")
////                .setPort(12200);
////
////        ProviderConfig<HelloService> providerConfig = new ProviderConfig<HelloService>()
////                .setInterfaceId(HelloService.class.getName())
////                .setRef(new HelloServiceImpl())
////                .setServer(serverConfig)
////                .setRegister(true)
////                .setRegistry(Lists.newArrayList(registryConfig));
////        providerConfig.export();
////
////        System.out.println("服务提供完成");
////
//        String interfaceClass = "cn.amberdata.metadata.facade.MetadataColumnConfigFacade";
//        ConsumerConfig<MetadataColumnConfigFacade> consumerConfig = new ConsumerConfig<MetadataColumnConfigFacade>()
//                .setInterfaceId(interfaceClass)
//                .setCheck(false)
//                .setUniqueId("metadataColumnConfigFacade")
//                .setProtocol("bolt")
//                .setRegistry(registryConfig);
//        consumerConfig.setRepeatedReferLimit(0);
//        consumerConfig.getRepeatedReferLimit();
////        MetadataColumnConfigFacade refer = consumerConfig.refer();
////        MetadataColumnConfigDTO ws_record = refer.getByTypeName("ws_record");
////        System.out.println(ws_record);
//////        GenericService genericService = (GenericService) consumerConfig.refer();
//////        Object genericObjectInvoke = genericService.$genericInvoke(methodName, prmname, vlue, Map.class);
//////        MetadataColumnConfigDTO platResponseBody = (MetadataColumnConfigDTO) genericObjectInvoke;
//////        System.out.println(JSONObject.toJSONString(platResponseBody));
    }

}
