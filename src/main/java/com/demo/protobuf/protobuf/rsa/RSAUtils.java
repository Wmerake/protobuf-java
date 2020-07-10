package com.demo.protobuf.protobuf.rsa;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class RSAUtils {

    private RSAUtils() {

    }

    /**
     * 密钥长度(bit)
     */
    private static final int KEYSIZE = 1024;


    /**
     * 最大加密明文长度
     */
    private static final int MAX_ENCODE_LENGTH =  KEYSIZE/8 -11;


    /**
     * 最大解密文长度
     */
    private static final int MAX_DECODE_LENGTH = KEYSIZE/8;

    /**
     * CIPHER_ALGORITHM RSA
     */
    private static final String CIPHER_ALGORITHM_RSA = "RSA/ECB/PKCS1Padding";

    /**
     * RSA加密
     */
    public static final String KEY_ALGORITHM_RSA = "RSA";

    /**
     * RSAPublicKey
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * RSAPrivateKey
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 获取配对的公钥和私钥
     * @return keyMap
     * @throws Exception 异常
     * @author sucb
     */
    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA);
        keyPairGen.initialize(KEYSIZE);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 对公钥进行编码转换，并进行BASE64转码
     * @param keyMap 配对的公钥和私钥组成的map
     * @return 处理后的公钥
     * @throws Exception 异常
     * @author sucb
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 对私钥进行编码转换，并进行BASE64转码
     * @param keyMap 配对的公钥和私钥组成的map
     * @return 处理后的私钥
     * @throws Exception 异常
     * @author sucb
     */
    public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return encryptBASE64(key.getEncoded());
    }

    /**
     * RSA加密
     * @param publicKeyStr 公钥
     * @param plainText 需要加密的数组
     * @return 加密后数组
     * @author sucb
     */
    public static byte[] encodeRSA(String publicKeyStr, byte[] plainText) {
        try {
            int i = plainText.length/(MAX_DECODE_LENGTH-11) +1; //加密数组需要切割的段数
            byte[] resultByte = new byte[i * KEYSIZE/8];//用于接收加密后数组
            for (int j = 0; j < i; j++) {
                byte[] adata ={}; //需要加密的每段数据
                if(plainText.length <= adata.length) {//数据不需要切割的情况
                    adata = new byte[plainText.length];
                    System.arraycopy(plainText, 0, adata, 0, plainText.length);
                }else {
                    if(j<i-1) {
                        adata = new byte[MAX_ENCODE_LENGTH];
                        System.arraycopy(plainText, j * MAX_ENCODE_LENGTH, adata, 0, MAX_ENCODE_LENGTH);
                    }else{//最后一段需要加密的数组长度小于KEYSIZE/8-11
                        adata = new byte[plainText.length- j * MAX_ENCODE_LENGTH];
                        System.arraycopy(plainText, j * MAX_ENCODE_LENGTH, adata, 0, plainText.length- j * MAX_ENCODE_LENGTH);
                    }
                }
                RSAPublicKey key = restorePublicKey(publicKeyStr);
                Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_RSA);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] cipherText = cipher.doFinal(adata);
                //数据拼接
                System.arraycopy(cipherText, 0, resultByte, j * cipherText.length, cipherText.length);
            }
            return resultByte;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * RSA解密
     * @param privateKeyStr 私钥
     * @param plainText 需要解密的数组
     * @return 解密后数组
     * @author sucb
     */
    public static byte[] decodeRSA(String privateKeyStr, byte[] plainText) {
        int i = plainText.length/MAX_DECODE_LENGTH;
        byte[] decodeByte = new byte[i * plainText.length];
        try {
            byte[] cipherText = new byte[MAX_DECODE_LENGTH];//需要解密的每段数据（也就是之前每段加密后得到的数组）
            int m = 0;
            for (int j = 0; j < i; j++) {
                System.arraycopy(plainText, j * cipherText.length, cipherText, 0, cipherText.length);
                RSAPrivateKey key = restorePrivateKey(privateKeyStr);
                Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_RSA);
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] newPlainText = cipher.doFinal(cipherText);
                System.arraycopy(newPlainText, 0, decodeByte, m, newPlainText.length);
                m += newPlainText.length;
            }
            byte[] resultByte = new byte[m];
            System.arraycopy(decodeByte, 0, resultByte, 0, resultByte.length);
            return resultByte;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 还原公钥，X509EncodedKeySpec 用于构建公钥的规范
     * @param publicKeyStr 公钥
     * @return 还原后的公钥
     * @throws Exception 异常
     * @author sucb
     */
    private static RSAPublicKey restorePublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = decryptBASE64(publicKeyStr);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
        return (RSAPublicKey) factory.generatePublic(x509EncodedKeySpec);
    }

    /**
     * 还原私钥 PKCS8EncodedKeySpec
     * @param privateKeyStr 私钥
     * @return 还原后的私钥
     * @throws Exception 异常
     * @author sucb
     */
    private static RSAPrivateKey restorePrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = decryptBASE64(privateKeyStr);
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
        return (RSAPrivateKey) factory.generatePrivate(priPKCS8);
    }

    /**
     * 进行BASE64转码
     * @param key byte型密钥
     * @return 转码后的String型密钥
     * @throws Exception 异常
     * @author sucb
     */
    private static String encryptBASE64(byte[] key) throws Exception {
        return Base64.encodeBase64String(key);
    }

    /**
     * 进行BASE64解码
     * @param key String型密钥
     * @return 解码后的byte型密钥
     * @throws Exception 异常
     * @author sucb
     */
    private static byte[] decryptBASE64(String key) throws Exception {
        return Base64.decodeBase64(key);
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> keyMap = initKey();
        String publicKey = getPublicKey(keyMap);
        String privateKey = getPrivateKey(keyMap);

        String a = "aaaaaaaaaaaa";
        byte[] miByte = encodeRSA(publicKey, a.getBytes());
        //将密文转换成String,使用Base64.encodeBase64String转换；转换后密文转byte,使用Base64.decodeBase64。
        System.out.println(Arrays.equals(miByte, Base64.decodeBase64(Base64.encodeBase64String(miByte))));
        byte[] mingByte = decodeRSA(privateKey, miByte);
        String actual = new String(mingByte);
        System.out.println("actual : " + actual);
        System.out.println(a.equals(actual));

        String message = "然而，这几乎是个不可能完成的任务。当p和q是非常大的质数时，根据pq的乘积去分解因子p和q，这是数学上公认的难题。通常，p和q都会选的非常大，比如说200位。这导致n也非常大，有400位。寻找一个400位数字的质数分解并不容易，我们要做的除法运算次数大约为10 199！世界最强的超级计算机天河2号每秒浮点运算是1016级别。那么，分解出p和q，大约需要10174年。10174就是1的后面跟上174个0，时间是不是很长？";
        byte[] miByte2 = encodeRSA(publicKey, message.getBytes());
        byte[] mingByte2 = decodeRSA(privateKey, miByte2);
        String actual2 = new String(mingByte2);
        System.out.println("actual2 : " + actual2);
        System.out.println(message.equals(actual2));
    }

}