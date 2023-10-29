package com.example.demo1_nacos.controller;
import cn.amberdata.common.util.excel.ExcelUtil;
import cn.amberdata.common.util.excel.ExcelUtilEx;
import cn.amberdata.common.util.excel.old.ExcelUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo1_nacos.antivirus.CommandException;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/6/15 10:20
 */
@RestController
@RequestMapping("/hello")
public class hello {

//    @Value("${aaa}")
    private String  addr;

    @PostMapping("/nifi_commit")
    public String nifiCommit(@RequestBody String rb) throws CommandException, InterruptedException, IOException {
        System.out.println(rb);
//            List<String> command = new ArrayList<>();
//            command.add("/bin/bash -c export LD_LIBRARY_PATH=/amberdata/sd/new-clamav/aa/usr/lib");
//            command.add("/bin/bash -c echo $LD_LIBRARY_PATH");
//            command.add("echo 'xxxxx'");
//            String commandOutput = CommandUtility.execute(command, true);
//            System.out.println(commandOutput);
        return "Hello World!";
    }

        @GetMapping("/hello")
        @ResponseBody
        public String hello() throws CommandException, InterruptedException, IOException {
            command("export LD_LIBRARY_PATH=/amberdata/sd/new-clamav/aa/usr/lib");
            command("echo $LD_LIBRARY_PATH ");
            command("echo xxxx");
//            List<String> command = new ArrayList<>();
//            command.add("/bin/bash -c export LD_LIBRARY_PATH=/amberdata/sd/new-clamav/aa/usr/lib");
//            command.add("/bin/bash -c echo $LD_LIBRARY_PATH");
//            command.add("echo 'xxxxx'");
//            String commandOutput = CommandUtility.execute(command, true);
//            System.out.println(commandOutput);
            return "Hello World!";
        }

    public static void command(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command.split(" ")).start();
        OutputStream outputStream = process.getOutputStream();
        String str="2021/12/01";
        outputStream.write(str.getBytes());
        outputStream.flush();
        //关闭输出流
        outputStream.close();
        //指定编码格式，解决中文乱码问题
        BufferedReader results = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
        String s;
        while ((s = results.readLine()) != null) {
            System.out.println(s);
        }
        //关闭输入流
        results.close();
        //销毁进程
        process.destroy();
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>)
        {
            for (Object o : (List<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
            List<String> idsList = new ArrayList<>();
            String json = "C:\\Users\\AB_ZhangLei\\Downloads\\response.json";
            File jsonFile = new File(json);
        String outFilePath = "C:\\Users\\AB_ZhangLei\\Desktop\\fsdownload\\data2.xlsx";
            //通过上面那个方法获取json文件的内容
            String jsonData = getStr(jsonFile);
            //转json对象
        JSONObject parse = (JSONObject) JSONObject.parse(jsonData);
        String datarows = parse.getString("datarows");
        JSONArray objects = JSONArray.parseArray(datarows);
        if(objects.get(0) instanceof List<?>){
            System.out.println("-----");
        }
//        archival_id,fonds_id,item_code,security_class,open_class,title,author,retention_period,file_year,carrier_type,doc_pub_date
        String x = String.valueOf(objects.get(0));
        String[] split = x.split(",");
        String [][] array = new String[objects.size()][split.length];
        for (int i = 0; i < objects.size(); i++) {
            List<Object> list = castList(objects.get(i), Object.class);
            List<String> cs = new ArrayList<>();
            String archival_id = (String) list.get(0);
            cs.add(archival_id);
            String fonds_id = (String) list.get(1);
            cs.add(fonds_id);
            String item_code = (String) list.get(2);
            cs.add(item_code);
            String security_class = (String) list.get(3);
            cs.add(security_class);
            String open_class = (String) list.get(4);
            cs.add(open_class);
            String title = (String) list.get(5);
            cs.add(title);
            String author = (String) list.get(6);
            cs.add(author);
            String retention_period = (String) list.get(7);
            cs.add(retention_period);
            String file_year = String.valueOf(list.get(8));
            cs.add(file_year);
            String carrier_type = (String) list.get(9);
            cs.add(carrier_type);
            String doc_pub_date = (String) list.get(10);
            cs.add(doc_pub_date);
            String[] strings = cs.toArray(new String[split.length]);
            array[i] = strings;
//            for (int i1 = 0; i1 < list.size(); i1++) {
//
//
//                array[i] = dataArr;
//            }
        }
        ExcelUtilEx.creatExcel(outFilePath,array);
        System.out.println("---------------------------");
    }

    //把一个文件中的内容读取成一个String字符串
    public static String getStr(File jsonFile){
        String jsonStr = "";
        try {
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveasfilewriter(String content) {
       String aa = "C:\\Users\\AB_ZhangLei\\Desktop\\fsdownload\\aa.txt";
        FileWriter fwriter = null;
        try {
            fwriter = new FileWriter(aa);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
