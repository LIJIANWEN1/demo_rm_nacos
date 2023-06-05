package com.example.demo1_nacos.util;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *公钥:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSV4nwJO5/UsP8pzFcFDoZ3jW1/whOlnua4sm3TUGCy4yfZsW2QYNns6NGcw6z00veZKtCFLwaLTqFdFN39oCC0lZKugwL+FpEI0mYKNvYGGQarwbWMafPgCIzJEfvdXMAOW1gxE+fhkJc8hEPP9dT8w+8YxgYfSrUlBAIRCxJwwIDAQAB
*/
public class RsaUtils {
    private static final String ALGO = "RSA";
    private static final String CHARSET = "UTF-8";
    private static Map<String, String> KEY_CACHE = new HashMap<>();


    /**
     * RSA公钥解密
     *
     * @param data        加密字符串
     * @param publicKey 公钥
     * @return 铭文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String data, String publicKey) throws Exception {
        byte[] inputByte = Base64.getDecoder().decode(data.getBytes(CHARSET));
        // base64 编码的公钥
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey priKey = (RSAPublicKey) KeyFactory.getInstance(ALGO).generatePublic(new X509EncodedKeySpec(decoded));
        // RSA 解密
        Cipher cipher = Cipher.getInstance(ALGO);
        // 公钥解密
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return new String(cipher.doFinal(inputByte));
    }

}
