import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.nifi.processor.io.InputStreamCallback
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
// 获取FlowFile
def flowfile = session.get()

// 如果FlowFile对象为null则进行回滚
if(flowfile == null) session.rollback()

try {
    // 通过ProcessSession读取FlowFile
    session.read(flowfile, new InputStreamCallback() {
        @Override
        void process(InputStream inputStream) throws IOException {
            def reader = new BufferedReader(new InputStreamReader(inputStream))
            print reader.readLines()[0]
            def lines = reader.readLines()
            def jsonStr = String.join('', lines)
            log.info jsonStr
            Map<String, String> obj = new HashMap<String, String>()
            obj = new ObjectMapper().readValue(jsonStr, new TypeReference<Map<String, String>>() {})
            log.info obj
        }
    })
} catch (Exception e) {
    def sw = new StringWriter()
    def pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    // 如果catch到异常，那么将FlowFile传递给REL_FAILURE关联关系
    session.transfer(flowfile, REL_FAILURE)
}
// 如果处理完毕，将FlowFile传递给REL_SUCCESS关联关系
session.transfer(flowfile, REL_SUCCESS)