package com.example.demo1_nacos.util;

import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 周旭
 * @see
 * @since 2020-03-11
 */
public class Md5Utils {

    private static char[] hexDeists = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f'};

    /**
     * 获取md5校验码
     *
     * @param inputStream 输入流
     */
    public static String getFileMD5String(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest messagedigest = MessageDigest.getInstance("MD5");
        // 每次读取1024字节
        byte[] buffer = new byte[1024];
        int readNum;
        while ((readNum = inputStream.read(buffer)) > 0) {
            messagedigest.update(buffer, 0, readNum);
        }
        return bufferToHex(messagedigest.digest());
    }

    /**
     * 判断字符串的md5校验码是否与一个已知的md5码相匹配
     *
     * @param inputStream 要校验的流
     * @param md5PwdStr   已知的md5校验码
     */
    public static boolean checkMd5(InputStream inputStream, String md5PwdStr) throws IOException, NoSuchAlgorithmException {
        String s = getFileMD5String(inputStream);
        IOUtils.closeQuietly(inputStream);
        return s.equals(md5PwdStr);
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

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
      String url = "C:\\Users\\AB_ZhangLei\\Desktop\\0106-202301-0001.zip";
     InputStream inputStream = new FileInputStream(new File(url));
        System.out.println(getFileMD5String(inputStream));
    }

}