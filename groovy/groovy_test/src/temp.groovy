import cn.amberdata.common.util.excel.ExcelUtilEx
import cn.amberdata.metadata.facade.MetadataColumnConfigFacade
import cn.amberdata.metadata.facade.TemplateFacade
import cn.amberdata.metadata.facade.dto.MetadataColumnConfigDTO
import cn.amberdata.metadata.facade.dto.PropertyDTO
import cn.amberdata.metadata.facade.dto.TemplateDTO
import com.alipay.sofa.rpc.config.ConsumerConfig
import com.alipay.sofa.rpc.config.RegistryConfig
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
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
//@GrabResolver(name='aliyun', root='http://hzent.amberdata.cn:8081/repository/maven-public/')
//@GrabResolver(name='aliyun', root='https://maven.aliyun.com/repository/central')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-annotations', version = '2.13.4')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-core', version = '2.13.4')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-databind', version = '2.13.4')
@Grab(group = 'com.jayway.jsonpath', module = 'json-path', version = '2.4.0')
@Grab(group = 'com.google.code.gson', module = 'gson', version = '2.8.6')
@Grab(group = 'mysql', module = 'mysql-connector-java', version = '8.0.19')
//@Grab(group = 'com.alipay.sofa', module = 'sofa-rpc-all', version = '5.6.7')
//@Grab(group = 'org.jboss.resteasy', module = 'resteasy-client', version = '3.6.3.Final')
//@Grab(group = 'org.jboss.resteasy', module = 'resteasy-jaxrs-all', version = '3.6.3.Final')
@Grab(group = 'org.projectlombok', module = 'lombok', version = '1.18.6')
@Grab(group = 'org.apache.commons', module = 'commons-lang3', version = '3.7')
@Grab(group = 'com.alibaba.nacos', module = 'nacos-api', version = '1.4.1')
@Grab(group = 'com.alibaba.nacos', module = 'nacos-client', version = '1.4.1')
// 获取FlowFile
def flowfile = session.get()
List<Map<String, String>> objList = new ArrayList<>();
// 如果FlowFile对象为null则进行回滚
if(flowfile == null) session.rollback()
try {
    // 通过ProcessSession读取FlowFile
    session.read(flowfile, new InputStreamCallback() {
        @Override
        void process(InputStream inputStream) throws IOException {
            def reader = new BufferedReader(new InputStreamReader(inputStream))
            def lines = reader.readLines()
            def jsonStr = String.join('', lines)

            objList = new ObjectMapper().readValue(jsonStr, new TypeReference<List<Map<String, String>>>() {})
        }
    })
} catch (Exception e) {
    def sw = new StringWriter()
    def pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    log.error sw.toString()
    // 如果catch到异常，那么将FlowFile传递给REL_FAILURE关联关系
    session.transfer(flowfile, REL_FAILURE)
    return
}
// 如果处理完毕，将FlowFile传递给REL_SUCCESS关联关系
session.transfer(flowfile, REL_SUCCESS)




