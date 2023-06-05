package com.example.demo1_nacos.demo;


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
public class  receiveTest{

//        private static Logger logger = Logger.getLogger(receiveTest.class);

        public static void main(String[] args) throws Exception {

        DataHandler handler = sendServerFile();

        String pc_no = "J103-2022-00002";
        String FONDS_CODE = "J1999";
        String file_size = "97280";
        String file_time = "2022-08-2 11:16:12";
        String md5 = "0ac70eaa4c1f4c0eea61c9e2cf7e26e2";

        Object[] params = new Object[6];
        params[0] = handler;
        params[1] = pc_no;
        params[2] = FONDS_CODE;
        params[3] = file_size;
        params[4] = file_time;
        params[5] = md5;

        String url7 = "http://localhost:8013/dataarchivesapi/services/receiveData?wsdl";
        //（IP地址、端口及虚拟子路径以实际部署为准）
        String method7="receiveData";
        //根据接口发布方式不同，选择调用方式,目前2种方法调用
        Object value7 = invokeByCxf("http://webservice.service.dataarchives.amberdata.cn",url7, method7, params);//axis调用方式
        System.out.println("接口返回值如下：\n" + value7);

    }

        public static DataHandler sendServerFile() {
                File file = new File("C:\\Users\\AB_ZhangLei\\Desktop\\现场环境\\龙游项目\\电子归档\\电子公文-发文归档样例.zip");
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
