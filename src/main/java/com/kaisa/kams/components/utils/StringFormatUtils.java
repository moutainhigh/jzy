package com.kaisa.kams.components.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pengyueyang created on 2017/11/10.
 */
public class StringFormatUtils {

    public static String format(String content, Map<String, String> valueMap){
        if (StringUtils.isEmpty(content) || MapUtils.isEmpty(valueMap)) {
            return content;
        }
        Set<Map.Entry<String, String>> sets = valueMap.entrySet();
        for(Map.Entry<String, String> entry : sets) {
            String regex = "\\$\\{" + entry.getKey() + "\\}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            String value = entry.getValue();
            if (StringUtils.isEmpty(value)) {
                value = "";
            }
            content = matcher.replaceFirst(value);
        }
        return content;
    }

    public static String format(String content, String key, String value){
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return content;
        }
        String regex = "\\$\\{" + key + "\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        content = matcher.replaceFirst(value);
        return content;
    }

    public static boolean isEmpty(Object object){
        return object==null||StringUtils.isEmpty(object.toString());
    }

    public static boolean isNotEmpty(Object object){
        return !isEmpty(object);
    }
}
