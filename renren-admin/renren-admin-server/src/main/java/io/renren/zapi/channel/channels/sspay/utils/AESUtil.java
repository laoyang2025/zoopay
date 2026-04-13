package io.renren.zapi.channel.channels.sspay.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
//import javax.xml.bind.DatatypeConverter;

//import org.apache.tomcat.util.codec.binary.Base64;


/**
 * @author Justin Yi
 * @version 1.0
 * @classname AESUtil
 * @description TODO
 * @date 2022/3/8 14:07
 **/
public class AESUtil {


    private static final String CIPHERMODE = "AES/ECB/NoPadding";


    /**
     * 加密
     * @param sSrc 加密参数
     * @param sKey aes-key
     * @return
     * @throws Exception
     */
    public static String encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        //算法/模式/补码方式"
        Cipher cipher = Cipher.getInstance("AES/ECB/pkcs5padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        ////使用BASE64做转码功能，同时能起到2次加密的作用。
//        return new Base64().encodeToString(encrypted);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     * @param sSrc 解密参数
     * @param sKey aes-key
     * @return
     * @throws Exception
     */
    public static String decrypt(String sSrc, String sKey) throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                System.out.print("Key为空null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                System.out.print("Key长度不是16位");
                return null;
            }
            byte[] raw = sKey.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            //先用base64解密
            // byte[] encrypted1 = new Base64().decode(sSrc);
            byte[] encrypted1 = Base64.getDecoder().decode(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original,"utf-8");
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }


}
