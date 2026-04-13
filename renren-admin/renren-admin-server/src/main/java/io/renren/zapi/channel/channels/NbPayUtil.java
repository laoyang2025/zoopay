package io.renren.zapi.channel.channels;

import io.renren.commons.tools.exception.RenException;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class NbPayUtil {
    /**
     * 签名算法：SHA256WithRSA
     */
    private static final String SIGN_ALGORITHM = "SHA256WithRSA";

    /**
     * 私钥签名
     * @param content 待签名的原始字符串
     * @param privateKeyBase64 Base64编码的PKCS8格式私钥字符串
     * @return Base64编码的签名结果
     * @throws Exception 签名异常
     */
    public static String sign(String content, String privateKeyBase64){
        // 1. Base64解码私钥
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

            // 2. 执行签名
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();

            // 3. 返回Base64编码的签名
            return Base64.getEncoder().encodeToString(signBytes);
        } catch (Exception e) {
            throw new RenException("签名失败");
        }
    }

    /**
     * 公钥验签
     * @param content 原始待验签字符串（和签名时的content完全一致）
     * @param signBase64 Base64编码的签名结果
     * @param publicKeyBase64 Base64编码的X509格式公钥字符串
     * @return 验签结果：true-验签通过，false-验签失败
     * @throws Exception 验签异常
     */
    public static boolean verify(String content, String signBase64, String publicKeyBase64) {

        try {
            // 1. Base64解码公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);

            // 2. 执行验签
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = Base64.getDecoder().decode(signBase64);

            return signature.verify(signBytes);
        } catch (Exception e) {
            throw new RenException("验证签名失败");
        }
    }
}
