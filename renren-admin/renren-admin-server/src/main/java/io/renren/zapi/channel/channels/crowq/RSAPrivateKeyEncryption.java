package io.renren.zapi.channel.channels.crowq;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAPrivateKeyEncryption {

    public static void main(String[] args) throws Exception {
        // 替换为你的私钥字符串
        String privateKeyString = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMfUj8xUh0g+0A5Z9TExrdQYYRWX5pETWV67A4C7+flAsSsGz9unQ1lkLeAOWALZmtZpzqF1vJrKpS3/c2kqX+xvnS4H8fL/a5aS37LnoYW2UjNB1Fjvf/Cm2e5mFmc1TcA0dYDQHkgDzZVTOCGGsyFpjUuIsVzgjnu0MQ0j53P/AgMBAAECgYEAizFyUl5E8PHrbAdxAKM/WLb2A/f+M9wruaDZONCTm3IRs1ZvQ49i/JWMg23t8c7gIuBDMFtQ2kTkIhA4V4h0B2FBE0rNkXJ/4Zpj2XHji6Jx8XjNH4ufVPbf3rVRYwyyIYZ7u9VjSR9X3cdHCC/iSjw1M9OZL1BUbWbsPqgPcCQQDpZrGXyc1tYrPspSErOLj+wH8Yx3doHJwSo43GIDg8Aa8FfdjU9JXqkI9apVHzRQht17ULJ7AmZb3Js5VpTTl/AkEA5Rne8jrS7yPGZfOWGXZmp/xxl8DmcfI6q2pNl8/HJ21tgvQbg5iJ9x5gvcYyOuHNB8hWBJqZMTNVvFItUgNlqIQJAM9o6zIYNZpPZqQrhjqRt9GtXeVi/9Ez/8hZxVMHd/V+9kg7rdGxI/vE9NVIF+Oa9TtS8yWD07vDJ2KNSr7Fp8QJBAJwrTwS0P89wq4z8+Rxy/R1ldzvQ0FpSgzXRCfUZq2jR7iNNGsVkIlg6cLhM/r99wHzBj/VTtM63qT/lIsR8pvECQQDOPcnTW10FR97es6EwH7zSCMdbf97cvjWXZtzW4dFJ8oZWTUNzRFJL4zALGOuey0cFETDMefZTX2XduPISY4FW";

        // 替换为你需要加密的明文
        String plainText = "Hello, RSA Private Key Encryption!";

        // 使用私钥加密
        String encryptedText = encrypt(plainText, privateKeyString);

        // 打印加密结果
        System.out.println("Encrypted Text: " + encryptedText);
    }

    // 使用私钥字符串加密，并返回Base64编码的字符串
    public static String encrypt(String plainText, String privateKeyString) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, String publicKeyString) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}
