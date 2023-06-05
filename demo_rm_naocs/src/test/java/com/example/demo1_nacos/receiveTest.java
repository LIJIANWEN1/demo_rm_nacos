package com.example.demo1_nacos;


import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import java.io.File;

/**
 * 读取文件创建时间和最后修改时间
 */
public class receiveTest {

//        private static Logger logger = Logger.getLogger(receiveTest.class);

        public static void main(String[] args) throws Exception {

        DataHandler handler = sendServerFile();

        String pc_no = "0056-2021-00001";
        String FONDS_CODE = "0056";
        String file_size = "97280";
        String file_time = "2023-04-23 11:16:12";
        String md5 = "50b2ec81cf7b7e59a067daa1f9a77f67";

        Object[] params = new Object[6];
        params[0] = handler;
        params[1] = pc_no;
        params[2] = FONDS_CODE;
        params[3] = file_size;
        params[4] = file_time;
        params[5] = md5;

        String url7 = "http://10.27.165.104:1880/dataarchivesapi/services/receiveData?wsdl";
        //（IP地址、端口及虚拟子路径以实际部署为准）
        String method7="receiveData";
        //根据接口发布方式不同，选择调用方式,目前2种方法调用
        Object value7 = invokeByCxf("http://webservice.service.dataarchives.amberdata.cn",url7, method7, params);//axis调用方式
        System.out.println("接口返回值如下：\n" + value7);

    }

        public static DataHandler sendServerFile() {
                String aa = "https://www.baidu.com/s?ie=UTF-8&wd= dingtalk://dingtalkclient/page/link?url=http%3a%2f%2f58.34.242.178%3a12719%2fmobile_Approval%2f%23%2fdetail%3fid%3d%26title%3d223【请假流程】流程审批单&pc_slide=true";
                File file = new File("C:\\Users\\AB_ZhangLei\\Desktop\\0056-2021-00001.zip");
                DataSource dataSource = new FileDataSource(file);
                DataHandler dataHandler = new DataHandler(dataSource);
                return dataHandler;
        }

        /**
         * cxf
         * @param nameSpace
         * @param url
         * @param method
         * @param params
         * @return
         */
        public static Object invokeByCxf(String nameSpace, String url, String method, Object[] params) {
                Object value = null;
                try {
                        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
                        org.apache.cxf.endpoint.Client client = dcf.createClient(url);
                        HTTPConduit http = (HTTPConduit) client.getConduit();
                        HTTPClientPolicy httpClientPolicy =  new  HTTPClientPolicy();
                        httpClientPolicy.setConnectionTimeout(100000);
                        httpClientPolicy.setAllowChunking(false);
                        httpClientPolicy.setReceiveTimeout(100000);
                        http.setClient(httpClientPolicy);
                        QName name = new QName(nameSpace,method);
                        Object[] result = client.invoke(name, params);
                        value = null != result && result.length > 0 ? result[0] : null;
                } catch (Exception e) {
                        e.printStackTrace();
//                        logger.error("调用WebService出错了！", e);
                }
                return value;
        }

}
