package com.example.demo1_nacos.util.lyt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.Security;
import org.apache.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    public static AES aes = new AES();
    private static Logger logger = Logger.getLogger(AES.class);
    private final String KEY_GENERATION_ALG = "PBKDF2WithHmacSHA1";
    private final int HASH_ITERATIONS = 10000;
    private final int KEY_LENGTH = 256;
    char[] humanPassphrase = { 'v', 't', 'i', 'o', 'n','s','f','o','t', '.','c', 'o', 'm',  'p'};
    private byte[] salt = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };

    private PBEKeySpec myKeyspec = new PBEKeySpec(humanPassphrase, salt,
            HASH_ITERATIONS, KEY_LENGTH);
    private final String CIPHERMODEPADDING = "AES/CBC/PKCS5Padding";

    private SecretKeyFactory keyfactory = null;
    private SecretKey sk = null;
    private SecretKeySpec skforAES = null;
    private byte[] iv = { 0xA, 1, 0xB, 5, 4, 0xF, 7, 9, 0x17, 3, 1, 6, 8, 0xC,
            0xD, 91 };

    private IvParameterSpec IV;

    public AES() {
        try {
            keyfactory = SecretKeyFactory.getInstance(KEY_GENERATION_ALG);
            sk = keyfactory.generateSecret(myKeyspec);

        } catch (NoSuchAlgorithmException nsae) {
            logger.debug("no key factory support for PBEWITHSHAANDTWOFISH-CBC");
        } catch (InvalidKeySpecException ikse) {
            logger.debug( "invalid key spec for PBEWITHSHAANDTWOFISH-CBC");
        }


        byte[] skAsByteArray = sk.getEncoded();

        skforAES = new SecretKeySpec(skAsByteArray, "AES");


        IV = new IvParameterSpec(iv);

    }

    public AES(String key) {
        try {
            keyfactory = SecretKeyFactory.getInstance(KEY_GENERATION_ALG);
            myKeyspec = new PBEKeySpec(key.toCharArray(), salt,
                    HASH_ITERATIONS, KEY_LENGTH);
            sk = keyfactory.generateSecret(myKeyspec);

        } catch (NoSuchAlgorithmException nsae) {
            logger.debug("no key factory support for PBEWITHSHAANDTWOFISH-CBC");
        } catch (InvalidKeySpecException ikse) {
            logger.debug( "invalid key spec for PBEWITHSHAANDTWOFISH-CBC");
        }
        byte[] skAsByteArray = sk.getEncoded();
        skforAES = new SecretKeySpec(skAsByteArray, "AES");


        IV = new IvParameterSpec(iv);

    }
    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return
     */
    public String Encrypt(String content){
        if(content==null) return content;
        String encode = null;
        try {
            encode = encrypt(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return encode;
    }
    /**
     * 解密
     *
     * @param content 需要解密的内容
     * @return
     */
    public String Decrypt(String content){
        if(content==null) return content;
        String decode = decrypt(content);
        return decode;
    }


    public String encrypt(byte[] plaintext) {

        byte[] ciphertext = encrypt(CIPHERMODEPADDING, skforAES, IV, plaintext);
        String base64_ciphertext = Base64Encoder.encode(ciphertext);
        return base64_ciphertext;
    }

    public String decrypt(String ciphertext_base64) {
        byte[] s = Base64Decoder.decodeToBytes(ciphertext_base64);
        String decrypted = null;
        try {
            decrypted = new String(decrypt(CIPHERMODEPADDING, skforAES, IV,
                    s),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return decrypted;
    }

    private byte[] addPadding(byte[] plain) {
        byte plainpad[] = null;
        int shortage = 16 - (plain.length % 16);
        if (shortage == 0)
            shortage = 16;
        plainpad = new byte[plain.length + shortage];
        for (int i = 0; i < plain.length; i++) {
            plainpad[i] = plain[i];
        }
        for (int i = plain.length; i < plain.length + shortage; i++) {
            plainpad[i] = (byte) shortage;
        }
        return plainpad;
    }
    private byte[] dropPadding(byte[] plainpad) {
        byte plain[] = null;
        int drop = plainpad[plainpad.length - 1];
        plain = new byte[plainpad.length - drop];
        for (int i = 0; i < plain.length; i++) {
            plain[i] = plainpad[i];
            plainpad[i] = 0;
        }
        return plain;
    }

    private byte[] encrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                           byte[] msg) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.ENCRYPT_MODE, sk, IV);
            return c.doFinal(msg);
        } catch (NoSuchAlgorithmException nsae) {
            logger.debug( "no cipher getinstance support for " + cmp);
        } catch (NoSuchPaddingException nspe) {
            logger.debug( "no cipher getinstance support for padding " + cmp);
        } catch (InvalidKeyException e) {
            logger.debug( "invalid key exception");
        } catch (InvalidAlgorithmParameterException e) {
            logger.debug( "invalid algorithm parameter exception");
        } catch (IllegalBlockSizeException e) {
            logger.debug( "illegal block size exception");
        } catch (BadPaddingException e) {
            logger.debug( "bad padding exception");
        }
        return null;
    }

    private byte[] decrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                           byte[] ciphertext) {
        initialize();
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.DECRYPT_MODE, sk, IV);
            return c.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException nsae) {
            logger.debug( "no cipher getinstance support for " + cmp);
        } catch (NoSuchPaddingException nspe) {
            logger.debug( "no cipher getinstance support for padding " + cmp);
        } catch (InvalidKeyException e) {
            logger.debug( "invalid key exception");
        } catch (InvalidAlgorithmParameterException e) {
            logger.debug( "invalid algorithm parameter exception");
        } catch (IllegalBlockSizeException e) {
            logger.debug( "illegal block size exception");
        } catch (BadPaddingException e) {
            logger.debug( "bad padding exception");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean initialized = false;

    /**
     * AES解密
     * @param content 密文
     * @return
     * @throws InvalidAlgorithmParameterException
     */
    public byte[] wxdecrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws InvalidAlgorithmParameterException {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");

            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));// 初始化
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void initialize(){
        if (initialized) return;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        initialized = true;
    }

    //生成iv
    private static AlgorithmParameters generateIV(byte[] iv) throws Exception{
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }

    public static void main(String[] args) {
        String a = "3hfG8x1CVHDhSD4upG2SpmJO2tvP1VSmLJfqmnL5qYZJFXTvDR7MnrXTsWiGQr7ZJNf/QKlSA9ypPUg9QyfZJl8fPgH7/eMspYVDUW/GMonkkabaovREEy7fgu14tdmvhbqrX/oZECts2E7ArIbErCVr9uSmhXcqnhyrkvJWed/GB7q1tMs2WIR1esakCI78JPasOPYxx8kYBUsfkXEJFCmRbQfX1PBNGH77s3RIQ5xhr/wYSaFYoDnHT8XqI7FOh5cYO4JwspPFzTIMvJ1aAs1NDYjrG/EYfQkuu7v7SFaOJP+NI5FGR0mPm4t+qti2pmo+cvVbqreTIaohfy0ZxrXykf24jIIEkQJToilBnh2N/egIYOeNvjaF1RpnDQdgLG8/3ECfApySeMz+XMaHTPRkHahtswgfrYW7CgrXgHxEroASwQnC+0UatrwBDCKTq0IpTaLs5KAlI/OOl7AjIv4FrNZas2t7WmJCMT7AcPVl9naBgT06Rv03Eie6YeTCIttG/V58kM4/YPORA/YOeqVQ9HVT+yZcMg0uNARIeDzUbKbmPSrXTvVP8BbVHSPW/A7hcR+ofq8k6ZkGT8gEU+1eMdcrXF7SmKaHtAg0moYPBFqAnOinhquWDifakZcWeOjQkWXRIJw9wMEmgfmZV1lDy6DyCGKv8NU/2+VKJIk=";
        String token = "Cn7oZ42SUrJFpo4QhqRenG3x2MW1IX2b";
        String id = "rENqbHa9MwjXd gdslYLxA==";
        AES aes = new AES(token+id);
        String resultString = aes.Decrypt(a);
        System.out.println(resultString);
    }
}
