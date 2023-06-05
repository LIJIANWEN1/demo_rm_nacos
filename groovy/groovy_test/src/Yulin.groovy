// 使用@Grab注解加载依赖
@Grab(group = 'org.dom4j', module = 'dom4j', version = '2.1.3')
@Grab(group = 'com.google.guava', module = 'guava', version = '28.2-jre')
@Grab(group = 'io.minio', module = 'minio', version = '6.0.12')
@Grab(group = 'cn.hutool', module = 'hutool-all', version = '5.7.17')
@Grab(group = 'com.alibaba', module = 'fastjson', version = '1.2.78')
@Grab(group = 'jaxen', module = 'jaxen', version = '1.2.0')
@Grab(group = 'commons-io', module = 'commons-io', version = '2.10.0')
@Grab(group = 'org.apache.commons', module = 'commons-compress', version = '1.21')
@Grab(group = 'org.apache.commons', module = 'commons-lang3', version = '3.8.1')
@Grab(group = 'com.amazon.opendistroforelasticsearch.client', module = 'opendistro-sql-jdbc', version = '1.13.0.0')

import cn.hutool.core.util.ZipUtil
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.org.apache.http.HttpEntity;
import com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.org.apache.http.client.methods.CloseableHttpResponse
import com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.org.apache.http.client.methods.HttpPost
import com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.org.apache.http.entity.StringEntity
import com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.org.apache.http.impl.client.CloseableHttpClient
import com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.org.apache.http.impl.client.HttpClientBuilder
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.minio.MinioClient
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.Node
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter

import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


static Map<String, String> start(ComponentLog log, Map<String, String> obj) throws Exception {
    Map<String, String> map = new HashMap<String, String>();
    String sourceFolderPath = "";
    String localPath = "";
    FileInputStream inputStream = null;
    try {
        String objectPath = obj['data_path'];
        String dataId = obj['data_id'];
        boolean checkParameter = checkParameter(objectPath, dataId);
        if (!checkParameter) {
            map.put("response", JsonResult.failed(214, "归档提交接口，必要参数缺失", dataId));
            return map;
        }
        localPath = "/amberdata" + File.separator + new File(obj['data_path']).getName();
        String response = download(objectPath, localPath, dataId, log);
        if (StringUtils.isNotBlank(response)) {
            map.put("response", response);
            return map;
        }
        File file = new File(localPath);
        String unzipPath = file.getPath().substring(0, file.getPath().lastIndexOf(File.separator)) + File.separator;
        String charset = "GBK";
        boolean flag = unzipFile(file, unzipPath, charset);
        if (!flag) {
            charset = "UTF-8";
            FileUtils.deleteDirectory(new File(unzipPath.substring(0, unzipPath.lastIndexOf(File.separator))));
            flag = unzipFile(file, unzipPath, charset);
            if (!flag) {
                map.put("response", JsonResult.failed(202, "提交的归档数据包无法解压", dataId));
                return map;
            }
        }
        sourceFolderPath = file.getPath().substring(0, file.getPath().lastIndexOf("."));
        File xmlFile = new File(sourceFolderPath + File.separator + "元数据描述信息.xml");
        response = setXml(xmlFile, dataId, log);
        if (StringUtils.isNotBlank(response)) {
            map.put("response", response);
            return map;
        }
        ZipUtil.zip(sourceFolderPath, sourceFolderPath + ".zip", true);
        inputStream = new FileInputStream(new File(sourceFolderPath + ".zip"));
        def checkSum = Md5Utils.getFileMD5String(inputStream);
        map.put("checksum", checkSum);
        response = upload(objectPath, sourceFolderPath + ".zip", dataId, log);
        if (StringUtils.isNotBlank(response)) {
            map.put("response", response);
            return map;
        }
        return map;
    } catch (Exception e) {
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        e.printStackTrace(pw)
        log.error sw.toString()
    } finally {
        if (inputStream != null) {
            inputStream.close();
        }
        if (StringUtils.isNotBlank(localPath)) {
            String path = localPath
            File file1 = new File(path);
            if (file1.exists()) {
                file1.delete();
            }
        }
        if (StringUtils.isNotBlank(sourceFolderPath)) {
            String dirPath = sourceFolderPath
            File file2 = new File(dirPath)
            if (file2.exists()) {
                FileUtils.deleteDirectory(file2);
            }
        }
    }
}

static String setXml(File xmlFile, String dataId, ComponentLog log) {
    SAXReader saxReader = new SAXReader();
    //读取元数据xml
    OutputStream outputStream = null;
    Document templateDocument;
    XMLWriter writer = null;
    try {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setPadText(false);
        templateDocument = saxReader.read(xmlFile);
        outputStream = new FileOutputStream(xmlFile);
        writer = new XMLWriter(outputStream, format);
        //获取年度
        String fileYear = (String) getoneNodeValue("//block[@name='说明信息']/property[@name='file_year']", templateDocument);
        //获取一级类目代码
        String firstClass = (String) getoneNodeValue("//block[@name='资源标识']/property[@name='first_class_code']", templateDocument);
        //获取二级类目代码
        String secordClass = (String) getoneNodeValue("//block[@name='资源标识']/property[@name='second_class_code']", templateDocument);
        //获取三级类目
        String threeClass = (String) getoneNodeValue("//block[@name='资源标识']/property[@name='third_class_code']", templateDocument);
        setNodesVal("//block[@name='资源标识']/property[@name='classification']",
                firstClass + "." + secordClass,
                templateDocument);
        setNodesVal("//block[@name='说明信息']/property[@name='digitized_status']",
                "03",
                templateDocument);
        String jsonStr = getArchivalId(fileYear, firstClass,secordClass, threeClass, log);
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        JSONArray data = jsonObject.getJSONArray("data");
        String oldArchivalId = data.getString(0);
        log.error oldArchivalId
        setNodesVal("//block[@name='资源标识']/property[@name='archival_id']",
                oldArchivalId,
                templateDocument);
        //格式化
        spaceFormat(templateDocument);
        //写入
        writer.write(templateDocument);
    } catch (Exception e) {
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        e.printStackTrace(pw)
        log.error sw.toString()
        return JsonResult.failed(204, "提交的归档数据根据xml解析补充档号、分类号等内容失败", dataId)
    } finally {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    return null;
}

/**
 * 参数判空
 *
 * @param args 参数
 * @return 是否为空
 */
static boolean checkParameter(String... args) {
    // 检查必须的参数是否传输正确
    return StringUtils.isNoneBlank(args);
}

/**
 * 格式化：把</>转换为<></>
 *
 * @param src 源文件document
 */
private static void spaceFormat(Document src) {
    //获取所有节点
    List<Node> allNodes = src.selectNodes("//*");
    for (Node node : allNodes) {
        if ("".equals(node.getText())) {
            node.setText("");
        }
    }
}

private static String getArchivalId(String fileYear,String firstClass, String secordClass, String threeClass, ComponentLog log) {
    //获取客户端（也就是先获取一个浏览器）
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost httpPost = new HttpPost("https://erms.arc.smylhxpm.com:8800/ermsapi/archival/get_archival_ids");
    httpPost.setHeader("Content-Type", "application/json");
    //发送json数据的字符串数据
    JSONObject json = new JSONObject();
    json.put("categoryCode", fileYear);
    json.put("firstClassCode", firstClass);
    json.put("number", 1);
    json.put("projectCode", "");
    json.put("secondClassCode", secordClass);
    json.put("sign", "");
    json.put("thirdClassCode", threeClass);
    json.put("volumeArchivalId", "");
    HttpEntity formEntity = new StringEntity(json.toString(), "utf-8");
    httpPost.setEntity(formEntity);
    //建立一个响应端
    CloseableHttpResponse response = null;
    try {
        //客户端执行post请求
        response = httpClient.execute(httpPost);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] respBody;
        if (response.getEntity() != null) {
            try {
                response.getEntity().writeTo(bos);
            } catch (IOException var3) {
                return null;
            }
        }
        respBody = bos.toByteArray();
        try {
            return respBody.length > 0 ? new String(respBody, "UTF-8") : "";
        } catch (Exception var4) {
            throw new SecurityException(var4);
        }
    } catch (Exception e) {
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        e.printStackTrace(pw)
        log.error sw.toString()
        throw new Exception("获取生成档号接口失败")
    } finally {
        try {
            // 释放资源
            if (httpClient != null) {
                httpClient.close();
            }
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    return null;
}

private static void setNodesVal(String xPath, Object value, Document document) {
    Element element = (Element) document.selectSingleNode(xPath);
    if (element != null) {
        element.setText(null == value ? "" : value.toString());
    }
}

/**
 * 根据xPath获取节点值
 *
 * @param xPath xPath语句
 * @param document 对象
 * @return 节点值
 */
private static Object getoneNodeValue(String xPath, Document document) {
    Node node;
    Object value = null;
    node = document.selectSingleNode(xPath);
    if (node != null) {
        value = node.getText();
    }
    return value;
}

private static boolean unzipFile(File file, String unzipPath, String charset) {
    boolean flag = false;
    if (!flag) {
        try {
            ZipUtil.unzip(file, new File(unzipPath), Charset.forName(charset));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return flag;
        }
    }
    return flag;
}

class Md5Utils {

    private static char[] hexDeists = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
                                       'f'];

    /**
     * 获取md5校验码
     *
     * @param inputStream 输入流
     */
    static String getFileMD5String(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest messagedigest = MessageDigest.getInstance("MD5");
        // 每次读取1024字节
        byte[] buffer = new byte[1024];
        int readNum;
        while ((readNum = inputStream.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, readNum);
        }
        return bufferToHex(messagedigest.digest());
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, bytes.length);
    }

    private static String bufferToHex(byte[] bytes, int n) {
        StringBuffer stringBuffer = new StringBuffer(n * 2);
        for (int i = 0; i < n; i++) {
            appendHexPair(bytes[i], stringBuffer);
        }
        return stringBuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringBuffer) {
        // 取字节中高四位进行转换
        char ch0 = hexDeists[(bt & 0xf0) >> 4];
        // 取字节中低四位进行转换
        char ch1 = hexDeists[(bt & 0xf)];

        stringBuffer.append(ch0);
        stringBuffer.append(ch1);
    }
}

class JsonResult {

    static String failed(Integer status, String message, String dataId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", status);
            jsonObject.put("message", message);
            jsonObject.put("data_id", dataId)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toJSONString();
    }
}

static String upload(String path, String localPath, String dataId, ComponentLog log) throws Exception {
    try {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        //初始化
        MinioClient minioClient = new MinioClient("http://10.89.198.162:9000", "admin", "Dctm@1234");
        //上传文件
        minioClient.putObject("datacenter", path, localPath);
    } catch (Exception e) {
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        e.printStackTrace(pw)
        log.error sw.toString()
        return JsonResult.failed(205, "提交的归档数据包重新上传minio/oss失败", dataId)
    }
    return null;
}

static String download(String path, String localPath, String dataId, ComponentLog log) throws Exception {
    try {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        //初始化
        MinioClient minioClient = new MinioClient("http://10.89.198.162:9000", "admin", "Dctm@1234");
        //下载文件
        minioClient.getObject("datacenter", path, localPath);
    } catch (Exception e) {
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        e.printStackTrace(pw)
        log.error sw.toString()
        return JsonResult.failed(201, "提交的归档数据包获取不到", dataId)
    }
    return null;
}

def flowFile = session.get()
def newFlowFile = flowFile
Map<String, String> obj = new HashMap<String, String>()
try {
    // 从FlowFile获取属性
    InputStream read = session.read(flowFile);
    if (read.available() >= 200) {
        read.close();
        // 从FlowFile获取属性
        session.write(flowFile, { inputStream, outputStream ->
            def reader = new BufferedReader(new InputStreamReader(inputStream))
            def lines = reader.readLines()
            def jsonStr = String.join('', lines)
            obj = new ObjectMapper().readValue(jsonStr, new TypeReference<Map<String, String>>() {})
        } as StreamCallback)
        // 调用移植的Java代码
        Map<String, String> map = start(log, obj)
        //如果map中不包含response错误响应信息
        if (StringUtils.isBlank(map.get("response"))) {
            obj['checksum'] = map.get("checksum");
            //重新生成输出flowFile
            newFlowFile = session.create(flowFile);
            flowFile.remove()
            session.write(newFlowFile, { inputStream, outputStream ->
                outputStream.write(new ObjectMapper().writeValueAsString(obj).getBytes('utf-8'))
            } as StreamCallback)
            session.transfer(newFlowFile, REL_SUCCESS)
        } else {
            //重新生成输出flowFile
            newFlowFile = session.create(flowFile);
            flowFile.remove();
            String response = map.get("response");
            session.write(newFlowFile, { inputStream, outputStream ->
                outputStream.write(response.getBytes('utf-8'))
            } as StreamCallback)
            session.transfer(newFlowFile, REL_FAILURE);
        }
    } else {
        read.close();
        //重新生成输出flowFile
        newFlowFile = session.create(flowFile);
        flowFile.remove();
        String response = JsonResult.failed(400, "请求体参数内容有误, 请求失败", "null");
        session.write(newFlowFile, { inputStream, outputStream ->
            outputStream.write(response.getBytes('utf-8'))
        } as StreamCallback)
        session.transfer(newFlowFile, REL_FAILURE);
    }
} catch (Exception e) {
    def sw = new StringWriter();
    def pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    log.error sw.toString();
    //重新生成输出flowFile
    newFlowFile = session.create(flowFile);
    flowFile.remove();
    String response = JsonResult.failed(999, "服务器内部未知异常，请联系管理员", "null");
    session.write(newFlowFile, { inputStream, outputStream ->
        outputStream.write(response.getBytes('utf-8'))
    } as StreamCallback)
    session.transfer(newFlowFile, REL_FAILURE);
}
