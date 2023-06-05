//import com.alibaba.excel.EasyExcel
//import com.alibaba.excel.context.AnalysisContext
//import com.alibaba.excel.event.AnalysisEventListener
//import com.csvreader.CsvWriter
//import lombok.AllArgsConstructor
//import lombok.Getter
//import lombok.NoArgsConstructor
//import lombok.Setter
//import org.apache.commons.io.FileUtils
//import org.apache.commons.lang3.StringUtils
//import org.apache.tika.detect.AutoDetectReader
//import org.apache.tika.exception.TikaException
//import org.dom4j.Document
//import org.dom4j.DocumentException
//import org.dom4j.DocumentHelper
//import org.dom4j.Element
//
//import java.nio.charset.Charset
//import java.sql.SQLException
//import java.text.SimpleDateFormat
//import java.util.regex.Matcher
//import java.util.regex.Pattern
//import java.util.regex.PatternSyntaxException
//import java.util.stream.Collectors
//
///**
// @author zhongcb
// @create 2022-02-22 4:11 下午
// */
//@Grab(group = 'org.dom4j', module = 'dom4j', version = '2.1.3')
//@Grab(group = 'com.alibaba', module = 'fastjson', version = '1.2.78')
//@Grab(group = 'commons-io', module = 'commons-io', version = '2.10.0')
//@Grab(group = 'org.apache.commons', module = 'commons-compress', version = '1.21')
//@Grab(group = 'cn.hutool', module = 'hutool-all', version = '5.7.17')
//@Grab(group = 'jaxen', module = 'jaxen', version = '1.2.0')
////@Grab(group = 'org.apache.poi', module = 'poi-ooxml', version = '4.1.2')
////@Grab(group = 'org.apache.poi', module = 'poi', version = '4.1.2')
////@Grab(group = 'org.apache.poi', module = 'poi-scratchpad', version = '4.1.2')
//@Grab(group = 'org.projectlombok', module = 'lombok', version = '1.16.10')
//@Grab(group = 'org.apache.commons', module = 'commons-lang3', version = '3.3.2')
//@Grab(group = 'org.mvel', module = 'mvel2', version = '2.2.8.Final')
//@Grab(group = 'net.sourceforge.javacsv', module = 'javacsv', version = '2.0')
//@Grab(group = 'org.apache.tika', module = 'tika-parsers', version = '1.24.1')
//@Grab(group = 'com.alibaba', module = 'easyexcel', version = '3.0.5')
//
////单位编码
//String unitCode = "testUnit";
////数据Url
//String dataUrl = "/tmp/W074.csv"
////规则Url
//String xmlUrl = "/tmp/ws·a_record(1).xml"
//String dataCode = codeString(dataUrl)
//String xmlCode = codeString(xmlUrl)
////获取数据
//List<Map> list = getDataList(dataUrl, dataCode)
//List<Property> propertyList = getXmlRule(xmlUrl, xmlCode)
//String fileName = dataUrl.substring(dataUrl.lastIndexOf(File.separator) + 1)
////导出Excel的位置
//String exportPath = "/tmp/" + fileName.substring(0, fileName.lastIndexOf(".")) + "最新整理后的Excel" + System.currentTimeMillis() + ".xls"
//Map<String, Property> rule = new HashMap<>()
//
//
//propertyList.each {
//    rule.put(it.name, it)
//}
//
////解析数据，并检测
//List<List<String>> dataList = new ArrayList<>()
//List<String> repeatBucket = new ArrayList<>()
//
//def line = 1
//list.each {
//    println("正在检测第" + line + "行的数据")
//    line++
//    List<String> exception = new ArrayList<>()
//    StringBuilder repeatError = new StringBuilder()
//    List<String> data = new ArrayList<>()
//
//    //取出档号加入集合中，校验是否存在相同的档号，如果存在则记录档号重复异常
//    String archiveId = it.get(rule.get("archival_id").thirdName)
//    if (repeatBucket.contains(archiveId)) {
//        repeatError.append(String.format("【%s】此号码已经存在。", archiveId))
//    } else {
//        repeatBucket.add(archiveId)
//    }
//
//    propertyList.each { property ->
//        String name = property.name
//        String thirdName = property.thirdName
//        String value = property.value
//        boolean nullAble = property.nullAble
//        Integer maxLength = property.maxLength
//        String type = property.type
//        String allowedValues = property.allowedValues
//        String repairMethod = property.repairMethod
//        String thirdValue = StringUtils.isBlank(thirdName) ? "" : it.get(thirdName)
//
//        //有value值的为固定字段
//        if (StringUtils.isNotBlank(value)) {
//            if (StringUtils.equals(value, "archival_id")) {
//                data.add(it.get(rule.get("archival_id").thirdName))
//            } else if (StringUtils.equals(value, "archival_id_1")) {
//                String archiveValue = (String) it.get(rule.get("archival_id").thirdName)
//                data.add(archiveValue.substring(0, archiveValue.lastIndexOf("-")))
//            } else if (StringUtils.equals(value, "unit_code")) {
//                data.add(unitCode)
//            } else if (StringUtils.equals(value, "retention_period")) {
//                String retentionPeriod = it.get(rule.get("retention_period").thirdName)
//                //保管期限有可能为空
//                if (StringUtils.isBlank(retentionPeriod)) {
//                    data.add("01")
//                } else {
//                    if (StringUtils.equals(retentionPeriod, "长期") || StringUtils.equals(retentionPeriod, "C")) {
//                        data.add("04")
//                    } else if (StringUtils.equals(retentionPeriod, "短期") || StringUtils.equals(retentionPeriod, "D")) {
//                        data.add("05")
//                    } else if (StringUtils.equals(retentionPeriod, "10年") || StringUtils.equals(retentionPeriod, "D10")) {
//                        data.add("03")
//                    } else if (StringUtils.equals(retentionPeriod, "30年") || StringUtils.equals(retentionPeriod, "D30")) {
//                        data.add("02")
//                    } else if (StringUtils.equals(retentionPeriod, "永久") || StringUtils.equals(retentionPeriod, "Y")) {
//                        data.add("01")
//                    } else {
//                        data.add("根据原有的保管期限未找到对应的编号。")
//                    }
//                }
//            } else {
//                data.add(value)
//            }
//        } else {
//            //标记是否已经修复，如果修复过至少一次，则不再检测和修复
//            boolean isRepair = false
//            String repairData = null;
//            //进行校验
//            //1。首先非空校验（符合非空才继续进行校验）
//            if (!isRepair && StringUtils.isBlank(thirdValue)) {
//                if (!nullAble) {
//                    exception.add(String.format("【%s】不能为空", name))
//                    //如果修复方案存在nullAble类型，则进行非空类型的修复
//                    if (repairMethod.contains("nullAble")) {
//                        //修复方案
//                        String result = dealNullAble(thirdValue, repairMethod, it, rule)
//                        repairData = result.split("##")[0]
//                        String errorMsg = result.split("##")[1]
//                        if (!StringUtils.equals(errorMsg, "null")) {
//                            isRepair = true
//                            //如果修复，移除最后一个错误信息，并重新添加新的错误信息
//                            exception.removeLast()
//                            exception.add(String.format(errorMsg, name))
//                        }
//                    }
//                }
//            }
//            //2。长度校验
//            if (!isRepair && StringUtils.isNotBlank(thirdValue) && thirdValue.length() > maxLength) {
//                exception.add(String.format("【%s】长度不符合规则,要求长度为%s,实际长度为%s", name, maxLength, thirdValue.length()))
//                //如果修复方案存在length类型，则进行长度类型的修复
//                if (repairMethod.contains("length")) {
//                    //修复方案
//                    String result = dealLength(thirdValue, repairMethod, it, rule)
//                    repairData = result.split("##")[0]
//                    String errorMsg = result.split("##")[1]
//                    if (!StringUtils.equals(errorMsg, "null")) {
//                        isRepair = true
//                        //如果修复，移除最后一个错误信息，并重新添加新的错误信息
//                        exception.removeLast()
//                        exception.add(String.format(errorMsg, name))
//                    }
//                }
//            }
//            //3。类型校验
//            //用来保存当前值的类型，因为值至少包含了一个String类型，所以存在所属类型为多个的可能
//            if (!isRepair && StringUtils.isNotBlank(thirdValue)) {
//                List<String> fileTypeList = new ArrayList<>();
//                //类型至少包含了String
//                fileTypeList.add("string");
//                //判断字段类型是否是int、Boolean和date类型
//                if (isInteger(thirdValue)) {
//                    fileTypeList.add("int");
//                }
//                if (isBoolean(thirdValue)) {
//                    fileTypeList.add("boolean");
//                }
//                if (isDate(thirdValue)) {
//                    fileTypeList.add("date");
//                }
//                if (isFloat(thirdValue)) {
//                    fileTypeList.add("float");
//                }
//                if (!fileTypeList.contains(type)) {
//                    String format = "【%s】的类型值为:%s,设定的类型值为%s,检测不通过."
//                    exception.add(String.format(format, name, fileTypeList.get(0), type))
//                    //如果修复方案存在type类型，则进行类型的修复
//                    if (repairMethod.contains("type")) {
//                        //修复方案
//                        String result = dealType(thirdValue, repairMethod, it, rule)
//                        repairData = result.split("##")[0]
//                        String errorMsg = result.split("##")[1]
//                        if (!StringUtils.equals(errorMsg, "null")) {
//                            isRepair = true
//                            //如果修复，移除最后一个错误信息，并重新添加新的错误信息
//                            exception.removeLast()
//                            exception.add(String.format(errorMsg, name))
//                        }
//                    }
//                }
//
//            }
//            //4。值域校验
//            if (!isRepair && StringUtils.isNotBlank(thirdValue) && StringUtils.isNotBlank(allowedValues)) {
//                List<String> allowed = new ArrayList<>()
//                String[] a = allowedValues.split(",")
//                for (int i = 0; i < a.size(); i++) {
//                    allowed.add(a[i].split("-")[1])
//                }
//                if (!allowed.contains(thirdValue)) {
//                    String format = "【%s】的值为:%s,值域为%s,不在值域范围内,检测不通过。";
//                    exception.add(String.format(format, name, thirdValue, allowed.toString()))
//                    //如果修复方案存在值域类型，则进行值域类型的修复
//                    if (repairMethod.contains("allowedValue")) {
//                        //修复方案、
//                        String result = dealAllowedValue(thirdValue, repairMethod, it, rule, allowedValues)
//                        repairData = result.split("##")[0]
//                        String errorMsg = result.split("##")[1]
//                        if (!StringUtils.equals(errorMsg, "null")) {
//                            isRepair = true
//                            //如果修复，移除最后一个错误信息，并重新添加新的错误信息
//                            exception.removeLast()
//                            exception.add(String.format(errorMsg, name))
//                        }
//                    }
//                }
//            }
//            //5。重复性校验（暂时不处理）
//
//            //将最终的值加入到整理结果列表中
//            if (isRepair) {
//                data.add(repairData)
//            } else {
//                data.add(thirdValue)
//            }
//        }
//
//    }
//    //处理完一条数据后，将检测的结果追加到数据后面
//    //处理exception格式
//    StringBuilder exceptionBuilder = new StringBuilder()
//    exceptionBuilder.append(archiveId + ":[")
//    for (int i = 0; i < exception.size(); i++) {
//        exceptionBuilder.append(exception.get(i))
//    }
//    exceptionBuilder.append("]")
//    data.add(exceptionBuilder.toString())
//    data.add(repeatError.toString())
//    dataList.add(data)
//}
//
//println("检测完成，开始导出Excel！！！！！！！！！")
////导出Excel
////String[][] dataArray = new String[dataList.size() + 2][]
//List<List<String>> dataArray = new ArrayList<>()
////第一行和第二行为字段名
//List firstNameList = new ArrayList()
//List secondNameList = new ArrayList()
//propertyList.each {
//    entry ->
//        firstNameList.add(entry.name)
//        secondNameList.add(StringUtils.isBlank(entry.thirdName) ? entry.name : entry.thirdName)
//}
//firstNameList.add("errorMessage")
//firstNameList.add("repeatError")
//secondNameList.add("errorMessage")
//secondNameList.add("repeatError")
//dataArray.add(firstNameList)
//dataArray.add(secondNameList)
////dataArray[0] = firstNameList.toArray(new String[firstNameList.size()])
////dataArray[1] = secondNameList.toArray(new String[secondNameList.size()])
//for (int i = 0; i < dataList.size(); i++) {
//    //dataArray[i + 2] = dataList.get(i).toArray(new String[dataList.get(i).size()]);
//    dataArray.add(dataList.get(i))
//}
//
//println("数据开始导出！！！！")
//EasyExcelUtil.writeExcelWithStringList(exportPath, dataArray)
//
//println("转换完成！！！！！")
//
//
//boolean isDate(String value) {
//    if (value == null) {
//        return false;
//    }
//    Date date = converterStringTime(value);
//    if (null == date) {
//        return false;
//    }
//    return true;
//}
//
//Date converterStringTime(String value) {
//    Date date = null
//    /*try {
//        SimpleDateFormat simple = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//        date = simple.parse(value)
//        return date
//    } catch (Exception e) {
//    }
//
//    try {
//        SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        date = simple.parse(value)
//        return date
//    } catch (Exception e) {
//    }
//    try {
//        SimpleDateFormat simple = new SimpleDateFormat("yyyyMMdd HH:mm:ss")
//        date = simple.parse(value)
//        return date
//    } catch (Exception e) {
//    }*/
//
//    try {
//        SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd")
//        date = simple.parse(value)
//        return date
//    } catch (Exception e) {
//    }
//    try {
//        SimpleDateFormat simple = new SimpleDateFormat("yyyy/MM/dd")
//        date = simple.parse(value);
//        return date
//    } catch (Exception e) {
//    }
//    try {
//        String DATE_REGEX = "(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))0229)";
//        Pattern compile = Pattern.compile(DATE_REGEX)
//        boolean matches = compile.matcher(value).matches()
//        return matches ? new Date() : null
//    } catch (Exception e) {
//    }
//    return null
//}
//
///**
// * 是否是布尔值
// *
// * @param value
// * @return
// */
//boolean isBoolean(String value) {
//    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
//}
//
///**
// * 判断字符串是否是整数
// */
//boolean isInteger(String value) {
//    try {
//        if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
//            return false;
//        }
//        Integer.parseInt(value);
//        return true;
//    } catch (NumberFormatException e) {
//        return false;
//    }
//}
//
//boolean isFloat(String value) {
//    try {
//        Float.valueOf(value);
//    } catch (NumberFormatException e) {
//        return false;
//    }
//    return true;
//}
//
//static boolean containsChar(String str) throws PatternSyntaxException {
//    // 清除掉所有特殊字符
//    String regEx = ".*[`~!@#\$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+.*"
//    Pattern p = Pattern.compile(regEx);
//    Matcher m = p.matcher(str);
//    return m.matches();
//}
//
//List<Map> getDataList(String dataUrl, String code) {
//    if (StringUtils.endsWithIgnoreCase(dataUrl, "csv")) {
//        return getCSVDataList(dataUrl, code)
//    } else if (StringUtils.endsWithIgnoreCase(dataUrl, "xls")) {
//        return getExcelDataList(dataUrl)
//    } else if (StringUtils.endsWithIgnoreCase(dataUrl, "xlsx")) {
//        return getExcelDataList(dataUrl)
//    }
//}
//
//List<Map> getCSVDataList(String dataUrl, String code) {
//
//    List<Map> datalist = new ArrayList<>()
//    BufferedReader bufferedReader = null
//    String sCurrentLine;
//    bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataUrl), code))
//    // 读取表格第一行作为map中的key
//    String key = bufferedReader.readLine();
//    List<String> keyList = Arrays.stream(key.split(","))
//            .collect(Collectors.toList());
//
//    String s = null;
//    int line = 1;
//    def time1 = System.currentTimeMillis()
//    while ((s = bufferedReader.readLine()) != null) {
//        def dataMap = new HashMap<>()
//        // 从第二行开始读取数据作为value
//        List<String> param = Arrays.stream(s.split(","))
//                .collect(Collectors.toList());
//        line++;
//
//        //方法三
//        (0..<keyList.size()).each {
//            def value = null;
//            try {
//                value = param.get(it)
//            } catch (Exception e) {
//                value = ""
//            }
//            dataMap.put(keyList.get(it), value)
//        }
//        datalist.add(dataMap)
//    }
//    def time2 = System.currentTimeMillis()
//    println(time2 - time1)
//
//    return datalist
//}
//
///**
// * 获取xls/xlsx格式数据
// * @param dataUrl
// * @param code
// * @return
// */
//List<Map> getExcelDataList(String dataUrl) {
//    List<Map> datalist = new ArrayList<>()
//    List<List<String>> data = EasyExcelUtil.readExcelWithStringList(new FileInputStream(dataUrl))
//    //获取第一行作为key
//    List<String> keys = data.get(0)
//
//    for (int i = 1; i < data.size(); i++) {
//        def dataMap = new HashMap<>()
//        List<String> param = data.get(i)
//        //方法三
//        (0..<keys.size()).each {
//            def value = null;
//            try {
//                value = param.get(it)
//            } catch (Exception e) {
//                value = ""
//            }
//            dataMap.put(keys.get(it), value)
//        }
//        datalist.add(dataMap)
//    }
//
//    return datalist
//}
//
//
//List<Property> getXmlRule(String xmlUrl, String code) {
//    File file = new File(xmlUrl)
//    List<Property> properties = getCheckProperties(file, code)
//    return properties
//}
///**
// * 读取元数据xml文件
// *
// * @param xmlFile
// * @return
// * @throws IOException
// * @throws org.dom4j.DocumentException
// */
//List<Property> getCheckProperties(File xmlFile, String code) throws IOException, DocumentException {
//    List<Property> properties = new ArrayList<>()
//
//    String xmlFromFile = getXmlFromFile(xmlFile, code);
//    Element rootElement = getRootElement(xmlFromFile);
//    List<Element> elements = rootElement.elements();
//    for (Element element : elements) {
//        getProperty(element, properties);
//    }
//    return properties;
//}
//
//String getXmlFromFile(File file, String code) throws IOException {
//    return FileUtils.readFileToString(file, code);
//}
//
//Element getRootElement(String xml) throws DocumentException {
//    Document document = DocumentHelper.parseText(xml);
//    return document.getRootElement();
//}
//
//void getProperty(Element element, List<Property> properties) {
//    if (element.getName() == "property") {
//        Property property = new Property(element);
//        if (StringUtils.isNotBlank(property.thirdName) || StringUtils.isNotBlank(property.value)) {
//            properties.add(property);
//        }
//    } else {
//        List<Element> elements = element.elements();
//        for (Element e : elements) {
//            getProperty(e, properties);
//        }
//    }
//}
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//class Property {
//    /**
//     * ID
//     */
//     String id;
//
//    /**
//     * 名称 如 archive_id
//     */
//     String name;
//
//    /**
//     * 字段名 如 档号
//     */
//     String title;
//
//    /**
//     * 三方名称 如 DH
//     */
//     String thirdName;
//
//    /**
//     * 三方字段名 如 档号
//     */
//     String thirdTitle;
//
//    /**
//     * 修复方法 如 dateRepair
//     */
//     String repairMethod;
//
//    /**
//     * 值
//     */
//     String value;
//    /**
//     * 类型
//     */
//     String type;
//
//    /**
//     * 最大值
//     */
//     Integer maxLength;
//    /**
//     * 是否为空
//     */
//     boolean nullAble;
//
//    /**
//     * 应用标准代码值项（WN-五年,SN-十年,SWN-十五年）
//     */
//     String allowedValues;
//
//    /**
//     * 标准代码名称
//     */
//     String allowedValuesCode;
//
//    /**
//     * 正则
//     */
//     String regex;
//
//    Property(String name, String value) {
//        this.name = name;
//        this.value = value;
//    }
//
//    Property(Element element) {
//        if (StringUtils.isNotBlank(element.attributeValue("maxLength")) && !"null".equals(element.attributeValue("maxLength"))) {
//            this.maxLength = Integer.valueOf(element.attributeValue("maxLength"));
//        }
//        this.title = element.attributeValue("title");
//        this.name = element.attributeValue("name");
//        this.thirdName = element.attributeValue("thirdName");
//        this.thirdTitle = element.attributeValue("thirdTitle");
//        this.repairMethod = element.attributeValue("repairMethod")
//        this.type = element.attributeValue("type");
//        this.nullAble = "true" == element.attributeValue("nullAble");
//        this.allowedValuesCode = element.attributeValue("allowedValuesCode");
//        this.allowedValues = element.attributeValue("allowedValues");
//        this.regex = element.attributeValue("regex");
//        this.value = element.attributeValue("value");
//
//    }
//
//    @Override
//     String toString() {
//        return "Property{" +
//                "id='" + id + '\'' +
//                ", name='" + name + '\'' +
//                ", title='" + title + '\'' +
//                ", thirdName='" + thirdName + '\'' +
//                ", thirdTitle='" + thirdTitle + '\'' +
//                ", repairMethod='" + repairMethod + '\'' +
//                ", value='" + value + '\'' +
//                ", type='" + type + '\'' +
//                ", maxLength=" + maxLength +
//                ", nullAble=" + nullAble +
//                ", allowedValues='" + allowedValues + '\'' +
//                ", allowedValuesCode='" + allowedValuesCode + '\'' +
//                ", regex='" + regex + '\'' +
//                '}';
//    }
//}
//
//class EasyExcelUtil {
//    /**
//     * StringList 解析监听器
//     */
//     static class StringExcelListener extends AnalysisEventListener<Map<Integer, String>> {
//        /**
//         * 自定义用于暂时存储data
//         * 可以通过实例获取该值
//         */
//         List<Map<Integer, String>> datas = new ArrayList<>();
//
//        /**
//         * 每解析一行都会回调invoke()方法
//         *
//         * @param object
//         * @param context
//         */
//        @Override
//        void invoke(Map<Integer, String> map, AnalysisContext context) {
//            List<String> list = new ArrayList<>(map.size());
//            map.each { list.add(it.getValue()) }
//            //数据存储到list，供批量处理，或后续自己业务逻辑处理。
//            datas.add(list);
//            //根据自己业务做处理
//        }
//
//        @Override
//        void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
//            List<String> list = new ArrayList<>(headMap.size());
//            headMap.each { list.add(it.getValue()) }
//            datas.add(list)
//        }
//
//        @Override
//        void doAfterAllAnalysed(AnalysisContext context) {
//            //解析结束销毁不用的资源
//            //注意不要调用datas.clear(),否则getDatas为null
//        }
//
//        List<List<String>> getDatas() {
//            return datas;
//        }
//
//        void setDatas(List<List<String>> datas) {
//            this.datas = datas;
//        }
//    }
//
//    /**
//     * 使用 StringList 来读取Excel
//     * @param inputStream Excel的输入流
//     * @param excelTypeEnum Excel的格式(XLS或XLSX)
//     * @return 返回 StringList 的列表
//     */
//    static List<List<String>> readExcelWithStringList(InputStream inputStream) {
//        // 这里 只要，然后读取第一个sheet 同步读取会自动finish
//        StringExcelListener stringExcelListener = new StringExcelListener()
//        EasyExcel.read(inputStream, stringExcelListener).sheet().doRead();
//        return stringExcelListener.getDatas();
//    }
//
//    /**
//     * 使用 StringList 来写入Excel
//     * @param outputStream Excel的输出流
//     * @param data 要写入的以StringList为单位的数据
//     * @param table 配置Excel的表的属性
//     * @param excelTypeEnum Excel的格式(XLS或XLSX)
//     */
//    static void writeExcelWithStringList(String path, List<List<String>> data) {
//        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
//        EasyExcel.write(path).needHead(false).sheet(path.substring(path.lastIndexOf(File.separator) + 1)).doWrite(data);
//
//    }
//
//}
//
///**
// * 获得文件编码
// * @param fileName
// * @return
// * @throws Exception
// */
// static String codeString(String fileName) throws IOException, TikaException {
//    FileInputStream fileInputStream = new FileInputStream(fileName);
//    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//    AutoDetectReader autoDetectReader = new AutoDetectReader(bufferedInputStream);
//    return autoDetectReader.getCharset().name();
//}
//
//void writeCsv(OutputStream out, String[][] header, String code)
//        throws IOException, SQLException {
//
//    Charset CHARSET = Charset.forName("GBK");
//    char DELIMITER = ',';
//    CsvWriter writer = null;
//    try {
//        writer = new CsvWriter(out, DELIMITER, CHARSET);
//        writeCsv(writer, header);
//    } finally {
//        if (writer != null)
//            writer.close();
//    }
//}
//
//void writeCsv(CsvWriter writer, String[][] header)
//        throws IOException, SQLException {
//    if (header != null) {
//        for (int i = 0; i < header.length; i++) {
//            writer.writeRecord(header[i]);
//        }
//        writer.endRecord();
//    }
//}
//
//void writeCsv(File file, String[][] header, String code)
//        throws IOException, SQLException {
//    BufferedOutputStream out = null;
//    FileOutputStream fileOutputStream = null;
//    try {
//        fileOutputStream = new FileOutputStream(file);
//        out = new BufferedOutputStream(fileOutputStream);
//        writeCsv(out, header, code);
//    } finally {
//        if (out != null) {
//            out.flush();
//            out.close();
//        }
//        if (fileOutputStream != null) {
//            fileOutputStream.close();
//        }
//    }
//}
//
//void writeCsv(String csvFilePath, String[][] header, String code) throws IOException, SQLException {
//    writeCsv(new File(csvFilePath), header, code);
//}
//
//
//String dealNullAble(String value, String repairMethod, Map it, Map<String, Property> rule) {
//    String[] split = repairMethod.split(",")
//    String repairedData = value
//    String errorMsg = null
//    for (int i = 0; i < split.size(); i++) {
//        if (split[i].contains("nullAble")) {
//            switch (split[i]) {
//            //年度为空的修复方案
//                case "nullAble-fileYear":
//                    repairedData = "2022"
//                    errorMsg = "【%s】为空问题已修复,空->2022。"
//                    break
//            //日期类型为空的修复方案，按照年度来修复
//                case "nullAble-date":
//                    //为空的话，取年度的值+0101
//                    String fileDate = it.get(rule.get("file_year").thirdName)
//                    if (StringUtils.isBlank(fileDate)) {
//                        repairedData = "2022" + "0101"
//                        errorMsg = "【%s】为空的问题已修复,空->20220101。"
//                    } else {
//                        repairedData = fileDate + "0101"
//                        errorMsg = "【%s】为空的问题已修复,空->" + fileDate + "0101。"
//                    }
//                    break
//            //保管期限值域为空的修复方案
//                case "nullAble-retention":
//                    repairedData = "Y"
//                    errorMsg = "【%s】为空的问题已修复，空->Y"
//                    break
//                default:
//                    break
//            }
//        }
//    }
//
//    return repairedData + "##" + errorMsg
//}
//
//
//static String dealLength(String value, String repairMethod, Map it, Map<String, Property> rule) {
//    String[] split = repairMethod.split(",")
//    String repairedData = value
//    String errorMsg = null
//    for (int i = 0; i < split.size(); i++) {
//        if (split[i].contains("length")) {
//            switch (split[i]) {
//                case "length-1":
//                    repairedData = "固定值1"
//                    errorMsg = "【%s】的长度问题已修复。"
//                    break
//                case "length-2":
//                    repairedData = "固定值2"
//                    errorMsg = "【%s】的长度问题已修复。"
//                    break
//                default:
//                    break
//            }
//        }
//    }
//    return repairedData + "##" + errorMsg
//}
//
//static String dealType(String value, String repairMethod, Map it, Map<String, Property> rule) {
//    String[] split = repairMethod.split(",")
//    String repairedData = value
//    String errorMsg = null
//    for (int i = 0; i < split.size(); i++) {
//        if (split[i].contains("type")) {
//            switch (split[i]) {
//            //处理时间格式的case
//                case "type-date-YYYY":
//                    if (Pattern.matches("((19|20)\\d{2})", value)) {
//                        repairedData = value + "0101"
//                        errorMsg = "【%s】的类型问题已修复," + value + "->" + value + "0101。"
//                    }
//                    break
//                case "type-date-YYYY0000":
//                    if (Pattern.matches("((19|20)\\d{2})0000", value)) {
//                        repairedData = value.substring(0, 4) + "0101"
//                        errorMsg = "【%s】的类型问题已修复," + value + "->" + value.substring(0, 4) + "0101。"
//                    }
//                    break
//                case "type-date-YYYYMM00":
//                    if (Pattern.matches("[12]\\d{3}(0[1-9]|1[0-2])00", value)) {
//                        repairedData = value.substring(0, 6) + "01"
//                        errorMsg = "【%s】的类型问题已修复," + value + "->" + value.substring(0, 6) + "01。"
//                    }
//                    break
//                case "type-date-YYYYMM":
//                    if (Pattern.matches("[12]\\d{3}(0[1-9]|1[0-2])", value)) {
//                        repairedData = value + "01"
//                        errorMsg = "【%s】的类型问题已修复," + value + "->" + value.substring(0, 6) + "01。"
//                    }
//                    break
//            /*case "type-date-replace/":
//                if (Pattern.matches("[12]\\d{3}(0[1-9]|1[0-2])00", value)) {
//                    repairedData = value.substring(0, 6) + "01"
//                    errorMsg = "【%s】的类型问题已修复," + value + "->" + value.substring(0, 6) + "01。"
//                }
//                break*/
//                case "type-boolean":
//                    if (StringUtils.equals("T", value)) {
//                        repairedData = "true"
//                        errorMsg = "【%s】的类型问题已修复,T->true。"
//                    } else if (StringUtils.equals("F", value)) {
//                        repairedData = "true"
//                        errorMsg = "【%s】的类型问题已修复,F->false。"
//                    }
//                    break
//                default:
//                    break
//            }
//        }
//    }
//    return repairedData + "##" + errorMsg
//}
//
//static String dealAllowedValue(String value, String repairMethod, Map it, Map<String, Property> rule, String allowedValues) {
//    String[] split = repairMethod.split(",")
//    String repairedData = value
//    String errorMsg = null
//    for (int i = 0; i < split.size(); i++) {
//        if (split[i].contains("allowedValue")) {
//            switch (split[i]) {
//                case "allowedValue-repair":
//                    String[] allowed = allowedValues.split(",")
//                    Map<String, String> allowedMap = new HashMap<>()
//                    for (int index = 0; index < allowed.size(); index++) {
//                        String[] val = allowed[index].split("-")
//                        allowedMap.put(val[0], val[1])
//                    }
//                    if (allowedMap.containsKey(value)) {
//                        repairedData = allowedMap.get(value)
//                        errorMsg = "【%s】的值域问题已修复," + value + "->" + repairedData + "。"
//                    }
//                    break
//                case "allowedValue-2":
//                    repairedData = "固定值2"
//                    errorMsg = "%s的值域问题已修复。"
//                    break
//                default:
//                    break
//            }
//        }
//    }
//    return repairedData + "##" + errorMsg
//}
//
//
