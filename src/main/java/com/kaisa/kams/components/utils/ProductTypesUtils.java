package com.kaisa.kams.components.utils;

import org.apache.commons.lang3.StringUtils;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pengyueyang on 2017/1/17.
 */
public class ProductTypesUtils {

    public final static String FILE_PATH = "/data0/java/config/custom/productType.json";

    public static List<Map<String,String>> getProductTypeList() {
        List<Map<String, String>> productTypeList = new ArrayList<>();
        try {
            String productTypeJson = Files.read(FILE_PATH);
            if (StringUtils.isNotEmpty(productTypeJson)) {
                productTypeList = (List<Map<String, String>>) Json.fromJson(NutType.list(NutType.mapStr(String.class)), productTypeJson);
            }
        }catch (Exception e) {
            return productTypeList;
        }
       return productTypeList;
    }

    public static String getName(String code){
        List<Map<String, String>> productTypeList  = getProductTypeList();
        for(Map<String,String> map  : productTypeList){
            if(code.equals(map.get("code"))){
                return  map.get("name");
            }
        }
        return null;
    }

    public static String getCode(String name){
        List<Map<String, String>> productTypeList  = getProductTypeList();
        for(Map<String,String> map  : productTypeList){
            if(name.equals(map.get("name"))){
                return  map.get("code");
            }
        }
        return null;
    }
}
