package com.kaisa.kams.components.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.codec.Base64;

/**
 * shiro进行加密解密的工具类封装
 * Created by weid on 2016/11/23.
 */
public class EndecryptUtils {


    public static final String PASSWORD="123456";

    public static final String KEY_PRE="KAMS_";


    /**
     * base64进制解密
     * @param cipherText
     * @return
     */
    public static String decrypt(String cipherText) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cipherText), "消息摘要不能为空");
        return Base64.decodeToString(cipherText);
    }

    /**
     * md5加密
     * @param password
     * @return
     */
    public static String md5Encrypt(String password) {
        if (StringUtils.isNotEmpty(password)) {
            return MD5Util.getMD5Code(KEY_PRE + password);
        }
        throw new IllegalArgumentException();
    }


}
