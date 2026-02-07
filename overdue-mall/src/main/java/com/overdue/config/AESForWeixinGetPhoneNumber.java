package com.overdue.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

public class AESForWeixinGetPhoneNumber {
  // 加密方式
  private static String keyAlgorithm = "AES";
  // 避免重复new生成多个BouncyCastleProvider对象，因为GC回收不了，会造成内存溢出
  // 只在第一次调用decrypt()方法时才new 对象
  private static boolean initialized = false;
  // 用于Base64解密
  private Base64.Decoder decoder = Base64.getDecoder();

  // 待解密的数据
  private String originalContent;
  // 会话密钥sessionKey
  private String encryptKey;
  // 加密算法的初始向量
  private String iv;

  public AESForWeixinGetPhoneNumber(String originalContent, String encryptKey, String iv) {
    this.originalContent = originalContent;
    this.encryptKey = encryptKey;
    this.iv = iv;
  }

  /**
   * AES解密
   * 填充模式AES/CBC/PKCS7Padding
   * 解密模式128
   *
   * @return 解密后的信息对象
   */
  public JSONObject decrypt() {
    initialize();
    try {
      // 检查参数是否为空
      if (originalContent == null || encryptKey == null || iv == null) {
        throw new IllegalArgumentException("解密参数不能为空");
      }

      // 检查参数长度
      if (originalContent.isEmpty() || encryptKey.isEmpty() || iv.isEmpty()) {
        throw new IllegalArgumentException("解密参数不能为空字符串");
      }

      // 打印调试信息
      System.out.println("解密参数信息:");
      System.out.println("encryptedData长度: " + originalContent.length());
      System.out.println("sessionKey长度: " + encryptKey.length());
      System.out.println("iv长度: " + iv.length());

      // 验证Base64格式
      byte[] encryptedBytes;
      byte[] sessionKeyBytes;
      byte[] ivBytes;

      try {
        encryptedBytes = decoder.decode(this.originalContent);
        sessionKeyBytes = decoder.decode(this.encryptKey);
        ivBytes = decoder.decode(this.iv);

        System.out.println("Base64解码成功:");
        System.out.println("encryptedData字节长度: " + encryptedBytes.length);
        System.out.println("sessionKey字节长度: " + sessionKeyBytes.length);
        System.out.println("iv字节长度: " + ivBytes.length);

        // 检查sessionKey长度是否为16字节（AES-128）
        if (sessionKeyBytes.length != 16) {
          throw new IllegalArgumentException("sessionKey长度不正确，应为16字节，实际为: " + sessionKeyBytes.length);
        }

        // 检查iv长度是否为16字节
        if (ivBytes.length != 16) {
          throw new IllegalArgumentException("iv长度不正确，应为16字节，实际为: " + ivBytes.length);
        }

        // 检查encryptedData长度是否为16的倍数（AES块大小）
        if (encryptedBytes.length % 16 != 0) {
          System.err.println("警告: encryptedData长度不是16的倍数，可能导致解密失败");
        }

      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Base64解码失败: " + e.getMessage());
      }

      // 尝试多种解密方式
      JSONObject result = tryDecryptWithDifferentModes(encryptedBytes, sessionKeyBytes, ivBytes);
      if (result != null) {
        return result;
      }

      // 如果所有方式都失败，抛出异常
      throw new RuntimeException("所有解密方式都失败，请检查参数是否正确");

    } catch (IllegalArgumentException e) {
      System.err.println("参数错误: " + e.getMessage());
      e.printStackTrace();
      return null;
    } catch (Exception e) {
      System.err.println("解密失败: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 尝试多种解密方式
   */
  private JSONObject tryDecryptWithDifferentModes(byte[] encryptedBytes, byte[] sessionKeyBytes, byte[] ivBytes) {
    // 方式1: AES/CBC/PKCS7Padding (推荐方式)
    try {
      System.out.println("尝试方式1: AES/CBC/PKCS7Padding");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
      Key sKeySpec = new SecretKeySpec(sessionKeyBytes, keyAlgorithm);
      cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivBytes));
      byte[] data = cipher.doFinal(encryptedBytes);
      String datastr = new String(data, StandardCharsets.UTF_8);
      System.out.println("方式1解密成功");
      return JSON.parseObject(datastr);
    } catch (Exception e) {
      System.err.println("方式1失败: " + e.getMessage());
    }

    // 方式2: AES/CBC/PKCS5Padding
    try {
      System.out.println("尝试方式2: AES/CBC/PKCS5Padding");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      Key sKeySpec = new SecretKeySpec(sessionKeyBytes, keyAlgorithm);
      cipher.init(Cipher.DECRYPT_MODE, sKeySpec, new IvParameterSpec(ivBytes));
      byte[] data = cipher.doFinal(encryptedBytes);
      String datastr = new String(data, StandardCharsets.UTF_8);
      System.out.println("方式2解密成功");
      return JSON.parseObject(datastr);
    } catch (Exception e) {
      System.err.println("方式2失败: " + e.getMessage());
    }

    // 方式3: 使用原始IV参数
    try {
      System.out.println("尝试方式3: 使用原始IV参数");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
      Key sKeySpec = new SecretKeySpec(sessionKeyBytes, keyAlgorithm);
      cipher.init(Cipher.DECRYPT_MODE, sKeySpec, new IvParameterSpec(ivBytes));
      byte[] data = cipher.doFinal(encryptedBytes);
      String datastr = new String(data, StandardCharsets.UTF_8);
      System.out.println("方式3解密成功");
      return JSON.parseObject(datastr);
    } catch (Exception e) {
      System.err.println("方式3失败: " + e.getMessage());
    }

    return null;
  }

  /** BouncyCastle作为安全提供，防止我们加密解密时候因为jdk内置的不支持改模式运行报错。 **/
  private static void initialize() {
    if (initialized) {
      return;
    }
    Security.addProvider(new BouncyCastleProvider());
    initialized = true;
  }

  // 生成iv
  private static AlgorithmParameters generateIV(byte[] iv)
      throws NoSuchAlgorithmException, InvalidParameterSpecException {
    AlgorithmParameters params = AlgorithmParameters.getInstance(keyAlgorithm);
    params.init(new IvParameterSpec(iv));
    return params;
  }
}
