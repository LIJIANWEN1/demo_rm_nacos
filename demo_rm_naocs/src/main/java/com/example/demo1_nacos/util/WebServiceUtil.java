//package com.example.demo1_nacos.util;
//
//import org.apache.axis.client.Call;
//import org.apache.axis.client.Service;
//import org.apache.axis.encoding.XMLType;
//import org.apache.log4j.Logger;
//
//import javax.xml.namespace.QName;
//import javax.xml.rpc.ParameterMode;
//import java.net.MalformedURLException;
//import java.net.URL;
//
//
//public class WebServiceUtil {
//
//	private static Logger logger = Logger.getLogger(WebServiceUtil.class);
//
//	/**
//	 * 调用WebService接口（详细调用示例见如下main方法）
//	 *
//	 * @param url		接口地址
//	 * @param method	接口方法
//	 * @param params	接口参数
//	 * @return
//	 */
//	public static Object invoke(String url, String method, Object[] params){
//		Object value = null;
//		try {
//			Client client = new Client(new URL(url));
//			client.setProperty(CommonsHttpMessageSender.DISABLE_KEEP_ALIVE, "true");
//			Object[] result = client.invoke(method,params);
//			value = null != result && result.length > 0 ? result[0] : null;
//			client.close();
//		} catch (MalformedURLException e) {
//			logger.error("远程服务出错或无远程服务！", e);
//		} catch (Exception e) {
//			logger.error("调用WebService出错了！", e);
//		}
//		return value;
//	}
//
//
//
//    public static Object invokeByXfire(String url, String method, Object[] params){
//		Object value = null;
//		try {
//			Client client = new Client(new URL(url));
//			Object[] result = client.invoke(method,params);
//			value = null != result && result.length > 0 ? result[0] : null;
//		} catch (MalformedURLException e) {
//			logger.error("远程服务出错或无远程服务！", e);
//		} catch (Exception e) {
//			logger.error("调用WebService出错了！", e);
//		}
//		return value;
//	}
//
//	/**
//	 * 通过axis方式调用WebService接口
//	 *
//	 * @param url		接口地址
//	 * @param method	接口方法
//	 * @param params	接口参数
//	 * @return
//	 */
//	public static Object invokeByAxis(String url, String method, String[] params,String[] paramNames,String nameSpace){
//		Object value = null;
//		try{
//			Service service = new Service();
//			Call call = (Call) service.createCall();
//			//call.getSOAPActionURI(http://rb-ht.com/方法名);
//			call.setTimeout(100000);
//			call.setTargetEndpointAddress(url);
//			call.setUseSOAPAction(true);
//
//			QName operationName = new QName(nameSpace, method);
//
//			int length =params.length;
//			Object[] obj=new Object[length];
//			//Object paramName=new Object[length];
//			for(int i=0;i<length;i++){
//				//paramName=temp[i];
//				//call.addParameter(new QName(nameSpace, paramNames[i]), XMLType.XSD_STRING, ParameterMode.IN);
//				call.addParameter(paramNames[i], XMLType.XSD_STRING, ParameterMode.IN);
//			}
//
//
//			//QName operationName = new QName("http://ws.axis2/", method);
//			call.setOperationName(operationName);
//			call.setReturnType(XMLType.XSD_STRING);
//			value = call.invoke(params);
//		}catch(Exception e){
//			logger.error("调用WebService出错了！", e);
//		}
//		return value;
//	}
//
//
//
//
//
//
//
//}
