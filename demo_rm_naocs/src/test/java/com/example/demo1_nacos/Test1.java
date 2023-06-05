package com.example.demo1_nacos;

import cn.amberdata.admin.authing.sdk.common.util.AesUtil;
import cn.amberdata.admin.authing.sdk.jwt.JwtUtil;

import cn.amberdata.common.util.aes.AESUtils;

import javax.crypto.NoSuchPaddingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author wyk
 * @date 2023-06-01
 */
public class Test1 {

    public static void testSsov1(String ssov1, String tk){
        ssov1 = URLDecoder.decode(ssov1);
        ssov1 = URLDecoder.decode(ssov1);

        String decrypt = AESUtils.decrypt(ssov1);
        System.out.println(decrypt);
    }

    public static void testTk(String v1, String tk){
        tk = URLDecoder.decode(tk);
        tk = URLDecoder.decode(tk);
//        String decrypt = AESUtils.decrypt(tk);
//        System.out.println(decrypt);
        tk=AESUtils.decrypt(tk);
        Map<String, String> decode = JwtUtil.decode(tk);
        System.out.println(decode);
        String nonce = decode.get("nonce");
        String decrypt = null;


        v1 = URLDecoder.decode(v1);
        v1 = URLDecoder.decode(v1);

        v1 = AESUtils.decrypt(v1);
        try {
            decrypt = AesUtil.decrypt(nonce, v1);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        System.out.println(decrypt);
        long l = Long.parseLong(decrypt, 16);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(l)));
    }
    public static void ssoc1(){
        String s ="n4z7TKIfKaVrvlyHg7XN6w%253D%253D";
        s=URLDecoder.decode(s);
        s=URLDecoder.decode(s);
        String decrypt = AESUtils.decrypt(s);
        System.out.println(decrypt);
    }

    public static void main(String[] args) {
//        ssoc1();
//        testSsov1("n4z7TKIfKaVrvlyHg7XN6w%3D%3D",null);
//        testTk("6pMi3OfMKQSqIEbxtBGMiA==","eyJraWQiOiJWZWZDd2xZWXVHM3hrYmxxa2NPVElDSXFuaUVNK0EwaG9WUnhNZG84bzFwSGV4bVMvTHNrVWFOQlhZRzRBME1qIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJ7XCJ1c19zb3VyY2VcIjpcImx5dFwiLFwicl90aW1lXCI6MTY4NTU4NjUyNDUwNyxcImxnX25hbWVcIjpcIjQzMzAxN1wiLFwibm9uY2VcIjpcIjlmY2NlNDM3NjY5MWYxMWE5OTc2NjIwYWQ4YjUxYjRlXCJ9In0.ZfP1uaENTJErLoIRZVqSW0Iij0cDoRdrJ0EZLTUwLfs");
        String s ="eyJraWQiOiJpb1dMNXV4TCtmM1F4WWNQZkJUcWZlc0hCcndTMTlvdmRPTDFQdTRWR0htTlhYOGFXbDV6SWgxdHhueWZ6cGV1IiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJ7XCJ1c19zb3VyY2VcIjpcInp6ZF9mcmVlXCIsXCJyX3RpbWVcIjoxNjg1NjA4NzU2MzYwLFwibGdfbmFtZVwiOlwiNzkzMzU4NTZcIixcIm5vbmNlXCI6XCJ4ZjlXTXQ5OUZFankzNFJMOThcIn0ifQ.rZBHcaZy6U6i2RVGBCEaeUutH0PdApki-L0xq0S-YVg";

        Map<String, String> decode = JwtUtil.decode(s);
        System.out.println(decode);

//        testSsov1("9IBqdehvAEQnHB1wiJtFVXr3U%252FevxrglLfjM%252B3zwwuA%253D",null);
//        testTk("9IBqdehvAEQnHB1wiJtFVXr3U%252FevxrglLfjM%252B3zwwuA%253D","qHF0WWCr0THBLUrE8LdY7dI6BzL3jrqQFE3Vziac0xnijE1JzGufXkohAKMskNpCHRKm%252B86Yq1iNJUpUyu4TYwz80uOei6fa6RA5AuQdrqUf8SAjD6fNrh37%252B45pFM78vcyYitdYxiDPmca9MzmMP5r%252B1VQK5tziEv%252FIILPZHhAZ6S0uH4MLW5qt4y1e1OlmoZHBdP5qDfDT%252BF8WZiYa3snX%252BKRDrh1vCJyFS%252BCvMelSniivSsJ7zuOejc7LJtXLMx317V1VeuOQT2yRsBgMc%252FDxFyNCvywUp5HzzQefIgwJxQK0UHeTOynH4aUL%252B6gMFmCg9Ab7yQae5Eyst%252Bm87XOvSQUESXsA8xS2lawwAK4Tm1KRSRApaz%252BmAgUEBfBsc5ubA8Qj6oFA6gG6oSNX30HVkQl%252Ff41R7V9LfwqwRYTK3YfwhG3jKy58N3AMvImu");
    }

}
