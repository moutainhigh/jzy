package com.kaisa.kams.components.security;

import org.abego.treelayout.internal.util.java.lang.string.StringUtil;
import org.apache.commons.lang.StringUtils;

/**
 * 将二进制转换成权限处理定义
 * Created by weid on 2016/11/24.
 */
public class BitPermissionResolver {

    public static String getPermission(String tag){
        StringBuffer result = new StringBuffer();
        if(StringUtils.isEmpty(tag)||tag.length()!=4){
            return null;
        }

        if(tag.substring(0,1).equals("1")){
            if (result.length()!=0){
                result.append(",");
            }
            result.append("create");
        }

        if(tag.substring(1,2).equals("1")){
            if (result.length()!=0){
                result.append(",");
            }
            result.append("delete");
        }

        if(tag.substring(2,3).equals("1")){
            if (result.length()!=0){
                result.append(",");
            }
            result.append("update");
        }

        if(tag.substring(3,4).equals("1")){
            if (result.length()!=0){
                result.append(",");
            }
            result.append("view");
        }
        return result.toString();
    }
}
