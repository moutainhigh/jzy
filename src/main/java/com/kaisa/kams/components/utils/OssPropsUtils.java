package com.kaisa.kams.components.utils;

import org.apache.commons.lang3.StringUtils;
import org.nutz.ioc.impl.PropertiesProxy;

/**
 * Created by pengyueyang on 2017/1/17.
 */
public class OssPropsUtils {

    public final static PropertiesProxy property = new PropertiesProxy("oss.properties");

    public static String getValueByKey(String key) {
        if (StringUtils.isNotEmpty(key) && property.keys().contains(key)) {
            return property.get(key);
        }
        return null;
    }
}
