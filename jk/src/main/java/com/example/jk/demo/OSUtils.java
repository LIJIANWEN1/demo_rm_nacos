package com.example.jk.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.Cleaner;
import sun.misc.Unsafe;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@RestController
public class OSUtils {

    private static List<byte[]> b = new ArrayList<>();
//    @PreDestroy
//    public void DestoryMethod(){
//        Unsafe unsafe = UnsafeUtil.getUnsafe();
//        for (Long aLong : address) {
//            unsafe.freeMemory(aLong);
//        }
//        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
//    }

    /**
     * 功能：内存使用率
     */
    @RequestMapping("/memory")
    public String memoryUsage(@RequestParam Integer m) {
        for (int i = 0; i < m; i++) {
            byte[] c = new byte[1024 * 1024 * 500];
            b.add(c);
        }
//        Unsafe unsafe = UnsafeUtil.getUnsafe();
//        address.add(unsafe.allocateMemory(1024 * 1024 * m)) ;
        return "分配完成";
//        Map<String, Object> map = new HashMap<String, Object>();
//        InputStreamReader inputs = null;
//        BufferedReader buffer = null;
//        try {
//            inputs = new InputStreamReader(new FileInputStream("/proc/meminfo"));
//            buffer = new BufferedReader(inputs);
//            String line = "";
//            while (true) {
//                line = buffer.readLine();
//                if (line == null)
//                    break;
//                int beginIndex = 0;
//                int endIndex = line.indexOf(":");
//                if (endIndex != -1) {
//                    String key = line.substring(beginIndex, endIndex);
//                    beginIndex = endIndex + 1;
//                    endIndex = line.length();
//                    String memory = line.substring(beginIndex, endIndex);
//                    String value = memory.replace("kB", "").trim();
//                    map.put(key, value);
//                }
//            }
//
//            long memTotal = Long.parseLong(map.get("MemTotal").toString());
//            System.out.println("内存总量"+memTotal+"KB");
//            long memFree = Long.parseLong(map.get("MemFree").toString());
//            System.out.println("剩余内存"+memFree+"KB");
//            long memused = memTotal - memFree;
//            System.out.println("已用内存"+memused+"KB");
//            long buffers = Long.parseLong(map.get("Buffers").toString());
//            long cached = Long.parseLong(map.get("Cached").toString());
//
//            double usage = (double) (memused - buffers - cached) / memTotal * 100;
//            System.out.println("内存使用率"+usage+"%");
//            if(usage > 10.0 ){
////            SendMail.sendMail("xxxxx@qq.com", "服务器cpu使用率过高，请注意查看", "服务器提醒");
//            }
//            return memFree;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                buffer.close();
//                inputs.close();
//            } catch (Exception e2) {
//                e2.printStackTrace();
//            }
//        }
//        return 0;
    }

    /**
     * 功能：获取Linux系统cpu使用率
     */
    @RequestMapping("/cpu")
    public static void cpuUsage(@RequestParam int core) {
        for (int i = 0; i < core; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                    }
                }
            }).start();
        }
//        float cpusage = 0;
//        while (cpusage > 10.0) {
//            try {
//                Map<?, ?> map1 = OSUtils.cpuinfo();
//                Thread.sleep(5 * 1000);
//                Map<?, ?> map2 = OSUtils.cpuinfo();
//                long user1 = Long.parseLong(map1.get("user").toString());
//                long nice1 = Long.parseLong(map1.get("nice").toString());
//                long system1 = Long.parseLong(map1.get("system").toString());
//                long idle1 = Long.parseLong(map1.get("idle").toString());
//                long user2 = Long.parseLong(map2.get("user").toString());
//                long nice2 = Long.parseLong(map2.get("nice").toString());
//                long system2 = Long.parseLong(map2.get("system").toString());
//                long idle2 = Long.parseLong(map2.get("idle").toString());
//                long total1 = user1 + system1 + nice1;
//                long total2 = user2 + system2 + nice2;
//                float total = total2 - total1;
//                long totalIdle1 = user1 + nice1 + system1 + idle1;
//                long totalIdle2 = user2 + nice2 + system2 + idle2;
//                float totalidle = totalIdle2 - totalIdle1;
//                cpusage = (total / totalidle) * 100;
//                System.out.println("cpu使用率:" + cpusage + "%");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 功能：CPU使用信息
     */
    public static Map<?, ?> cpuinfo() {
        InputStreamReader inputs = null;
        BufferedReader buffer = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
            buffer = new BufferedReader(inputs);
            String line = "";
            while (true) {
                line = buffer.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("cpu")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    List<String> temp = new ArrayList<String>();
                    while (tokenizer.hasMoreElements()) {
                        String value = tokenizer.nextToken();
                        temp.add(value);
                    }
                    map.put("user", temp.get(1));
                    map.put("nice", temp.get(2));
                    map.put("system", temp.get(3));
                    map.put("idle", temp.get(4));
                    map.put("iowait", temp.get(5));
                    map.put("irq", temp.get(6));
                    map.put("softirq", temp.get(7));
                    map.put("stealstolen", temp.get(8));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.close();
                inputs.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 主入口
     *
     * @param args
     */
    public static void main(String[] args) {
        String aa = "\"\n" +
                "\n" +
                "\n" +
                "\n" +
                "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" ?>\\n\n" +
                "\n" +
                "\n" +
                "\n" +
                "<record name=\\\"文书档案（案卷）2\\\">\\n\n" +
                "    \n" +
                "    \n" +
                "    \n" +
                "    <property type=\\\"string\\\" name=\\\"metadata_scheme_code\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"元数据方案编码\\\" uid=\\\"f1cc1bcf99bd47799edca9241fa6fc2c\\\" />\\n\n" +
                "    <property type=\\\"string\\\" name=\\\"metadata_scheme_name\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"元数据方案名称\\\" uid=\\\"1fa02b798c4148798309e0f5a1223624\\\" />\\n\n" +
                "    <property type=\\\"string\\\" name=\\\"version_no\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"1\\\" title=\\\"版本号\\\" uid=\\\"76f5e4f4a2df4e19aac4e669441a0bbb\\\" />\\n\n" +
                "    <block name=\\\"归档信息\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"00bb0e4ed7e94378ae2ab1680d74c6da\\\">\\n\n" +
                "        \n" +
                "        \n" +
                "        \n" +
                "        <block name=\\\"资源标识\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"b13a9342e9a544c3bf865fe2e0c80dfc\\\">\\n\n" +
                "            \n" +
                "            \n" +
                "            \n" +
                "            <property type=\\\"string\\\" name=\\\"archival_id\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"64\\\" title=\\\"档号\\\" uid=\\\"899ca924aaa1454298f45d94b5c233a0\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"fonds_id\\\" nullAble=\\\"false\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"12\\\" title=\\\"全宗号\\\" uid=\\\"907c07010f1344b9b6da0c178ede5a93\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"catalog_code\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"目录号\\\" uid=\\\"6333573c12064e2f9fbb052c92d2c54a\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"volume_id\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"案卷号\\\" uid=\\\"a7532e707cae487fb1f598e3b859457a\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"classification_number\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"分类号\\\" uid=\\\"f679881fce2f40ceaeb8ad2dfb8a7855\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"archive_1st_category\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"一级门类编码\\\" uid=\\\"0b263a049e95412a8f24ed6cfb0893fc\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"category_code\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"门类编码\\\" uid=\\\"2aa8c56670334004a4419ce948289830\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"DAGDM\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"64\\\" title=\\\"档案馆代码\\\" uid=\\\"25b521d6f9e24e01bf8004f4a16d7b50\\\" />\\n\n" +
                "        </block>\\n\n" +
                "        <block name=\\\"访问控制信息\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"73a8a778846f43169dc757e00c77fd8a\\\">\\n\n" +
                "            \n" +
                "            \n" +
                "            \n" +
                "            <property type=\\\"string\\\" name=\\\"security_class\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"8\\\" title=\\\"密级\\\" allowedValuesCode=\\\"密级\\\" allowedValues=\\\"公开-L1,国内-L2,内部-L3,秘密-L4,机密-L5,绝密-L6\\\" uid=\\\"078604bf8d0f468693466da9ee9698a4\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"open_class\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"开放状态\\\" allowedValuesCode=\\\"开放状态\\\" allowedValues=\\\"开放-A,限制-C\\\" uid=\\\"6deb0ca6d6e5488c8f26e700c6d2fb23\\\" />\\n\n" +
                "        </block>\\n\n" +
                "        <block name=\\\"说明信息\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"1668cd47798a49d399d744f2b84b803d\\\">\\n\n" +
                "            \n" +
                "            \n" +
                "            \n" +
                "            <property type=\\\"string\\\" name=\\\"cross_reference_id\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"100\\\" title=\\\"互见号\\\" uid=\\\"6f84561f59e049d7ae49a2561b312be6\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"retention_period\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"8\\\" title=\\\"保管期限\\\" allowedValuesCode=\\\"保管期限\\\" allowedValues=\\\"长期-C,短期-D,10年-D10,30年-D30,永久-Y,待定-待定\\\" uid=\\\"3e65ab84c3e34110ae6ee73ac89e1ca4\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"fonds_name\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"256\\\" title=\\\"全宗名称\\\" uid=\\\"ba96eed2f7cf443ca9e226df1f488961\\\" />\\n\n" +
                "            <property type=\\\"int\\\" name=\\\"documents_in_volume\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"卷内文件份数\\\" uid=\\\"d4155a1544c4444696f5af29580d9350\\\" />\\n\n" +
                "            <property type=\\\"int\\\" name=\\\"pages\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"卷内页数\\\" uid=\\\"a5d82dfee8e84264a556ee934cd64607\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"remark\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"256\\\" title=\\\"备注\\\" uid=\\\"fbc38c42b1ac474dbd60e14e15f2ae85\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"physical_archive_location\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"实体存址号\\\" uid=\\\"efcd9eb4320948f7974d4f3963586011\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"job_no\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"64\\\" title=\\\"工号\\\" uid=\\\"5f93807571fc4ac3b10847ea45c8d7df\\\" />\\n\n" +
                "            <property type=\\\"int\\\" name=\\\"file_year\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"4\\\" title=\\\"年度\\\" uid=\\\"840c3922da9c47488e397769f78458f4\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"filed_by\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"64\\\" title=\\\"归档人\\\" uid=\\\"ebfa98b3414b48a9bab4e47468a408df\\\" />\\n\n" +
                "            <property type=\\\"int\\\" name=\\\"archives_num\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"归档份数\\\" uid=\\\"4f35d49d1acd43c0a51193c933f59a2d\\\" />\\n\n" +
                "            <property type=\\\"date\\\" name=\\\"filed_date\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"归档日期\\\" typeFormat=\\\"yyyyMMdd\\\" uid=\\\"047c8d3b8d8c42d38ee1686659e4e413\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"file_department\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"256\\\" title=\\\"归档部门\\\" uid=\\\"6b51df4977924941a20a64ab0620c2ad\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"owner\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"256\\\" title=\\\"文件所有者\\\" uid=\\\"f4db7a55c91540b4b210c206b04ca3dd\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"title\\\" nullAble=\\\"false\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"2048\\\" title=\\\"案卷题名\\\" uid=\\\"64fa69fd11f34a5f8c81f89effa2df88\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"checked_by\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"检查人\\\" uid=\\\"c2af6d518c9a425a995efe5147d6c821\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"checked_date\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"检查日期\\\" uid=\\\"7f7ed5a723914cb18c225d3699468eee\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"fonds_unit\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"256\\\" title=\\\"立档单位\\\" uid=\\\"b5607c04198a4e53b4df642de55a9f03\\\" />\\n\n" +
                "            <property type=\\\"date\\\" name=\\\"file_end_date\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"终止日期\\\" typeFormat=\\\"yyyyMMdd\\\" uid=\\\"d23aaa68acfd45379c7756a2f109940b\\\" />\\n\n" +
                "            <property type=\\\"int\\\" name=\\\"binding_method\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"装订方式\\\" allowedValuesCode=\\\"装订方式\\\" allowedValues=\\\"整卷-1,散卷-2\\\" uid=\\\"15198cac14784125a3804ee0a6edea7f\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"author\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"1000\\\" title=\\\"责任者\\\" uid=\\\"27c4bfa7459e4d9393a250d4bf9e18fc\\\" />\\n\n" +
                "            <property type=\\\"date\\\" name=\\\"file_start_date\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"起始日期\\\" typeFormat=\\\"yyyyMMdd\\\" uid=\\\"98a4e52b517c44799617bf806ff2f689\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"carrier_type\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"载体类型\\\" allowedValuesCode=\\\"载体类型\\\" allowedValues=\\\"实体-01,电子-02\\\" uid=\\\"b3a3b3d277494bfa99ddd1b648d3cc4e\\\" />\\n\n" +
                "        </block>\\n\n" +
                "        <block name=\\\"管理信息\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"dcf6f276192c4514b22ef09a6ece6861\\\"></block>\\n\n" +
                "    </block>\\n\n" +
                "    <block name=\\\"业务内容\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"c0532a2946904e6abb4efdd2c951d9f1\\\">\\n\n" +
                "        \n" +
                "        \n" +
                "        \n" +
                "        <block name=\\\"过程信息\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"837e69a18a17424e80f5ffbe576b6651\\\"></block>\\n\n" +
                "        <block name=\\\"内容信息\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"9d26461015ad41c3bfa07b85dd893edc\\\">\\n\n" +
                "            \n" +
                "            \n" +
                "            \n" +
                "            <property type=\\\"string\\\" name=\\\"GJC\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"关键词\\\" uid=\\\"067f08eeced6444a9f55479d4271a0e8\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"ZTC\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"主题词\\\" uid=\\\"bc64ab253f3c43d3bf0be2387e2d75af\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"QWBS\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"全文标识\\\" uid=\\\"1afaf413c3524a69836dd9a7dc8abb7a\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"ZTGG\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"载体规格\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1abd\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"ITEM_COUNT\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"电子全文数\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1ab3\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"DATA_SOURCE\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"数据来源\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1ab6\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"BOX_NO\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"盒号\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1ab6\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"MEDIUM_TYPE\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"移交方式\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1ab7\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"CUSTODY_STATE\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"保管状况\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1a77\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"PACKAGE_NAME\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"包名\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1a67\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"SWLX\\\" nullAble=\\\"true\\\" sys=\\\"false\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"实物类型\\\" uid=\\\"8685bcfc32fc452586bd50b0735f1a17\\\" />\\n\n" +
                "        </block>\\n\n" +
                "    </block>\\n\n" +
                "    <block name=\\\"电子文件\\\" can_repeat=\\\"false\\\" required=\\\"false\\\" source_type=\\\"da_volume\\\" uid=\\\"5f4553d31947442db0a107ba168f2207\\\">\\n\n" +
                "        \n" +
                "        \n" +
                "        \n" +
                "        <file required=\\\"false\\\" uid=\\\"a414ef9037054898860c58b83b6612cf\\\">\\n\n" +
                "            \n" +
                "            \n" +
                "            \n" +
                "            <property type=\\\"string\\\" name=\\\"name\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"名称\\\" uid=\\\"bdf944f0bbcf4804818c9c04de2882f0\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"url\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"1000\\\" title=\\\"文件路径\\\" uid=\\\"f54fee1776514d31b8d5ce53d5ff36c5\\\" />\\n\n" +
                "            <property type=\\\"long\\\" name=\\\"size\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"11\\\" title=\\\"文件大小\\\" uid=\\\"428e664a9b944133966ba22c4bef40d2\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"seq\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"序号\\\" uid=\\\"222cf98a69b043bdb3ce405a9fd23265\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"checksum_type\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"校验算法\\\" uid=\\\"81a19e9a5538463f9e131339c237b453\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"checksum\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"255\\\" title=\\\"校验值\\\" uid=\\\"1f4485c08e31419f91345f51209a9b94\\\" />\\n\n" +
                "            <property type=\\\"date\\\" name=\\\"creation_date\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"null\\\" title=\\\"创建时间\\\" uid=\\\"1dd9c17477064af1845a73aced131c59\\\" />\\n\n" +
                "            <property type=\\\"date\\\" name=\\\"modify_date\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"null\\\" title=\\\"最后修改时间\\\" uid=\\\"dc13c7a90b6b4b2eb46c067e1caba4b5\\\" />\\n\n" +
                "            <property type=\\\"string\\\" name=\\\"format\\\" nullAble=\\\"false\\\" sys=\\\"true\\\" fullTextSearch=\\\"false\\\" maxLength=\\\"32\\\" title=\\\"格式后缀\\\" uid=\\\"220d0b79df5a427f851961d4da649292\\\" />\\n\n" +
                "        </file>\\n\n" +
                "    </block>\\n\n" +
                "</record>\\n\"";
        String output = aa.replace("\n", "").replace("\r", "");
        System.out.println(output);

    }
}