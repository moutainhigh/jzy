package com.kaisa.kams.components.utils.excelUtil;

import com.kaisa.kams.models.BaseModel;
import org.apache.poi.ss.formula.functions.T;
import org.nutz.mvc.annotation.Param;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/4/27.
 */
public class ObjectUtil {
    public static String getStringFromObject(String fieldName,Object object)throws Exception{
        Object obj = getValueFromObject(fieldName,object);
        return obj==null?"":obj.toString();
    }

    public static Object hasTitle(String title ,Class clazz){
        for(Field field : clazz.getDeclaredFields()){
            ExcelAssistant excelAssistant = (ExcelAssistant)field.getAnnotation(ExcelAssistant.class);
            if(excelAssistant.titleName().equals(title)){
                return true;
            }
        }
        return false;
    }
    public static Object getValueFromObject(String fieldName,Object object)throws Exception{
        if(fieldName.matches("[^\\(\\)]+\\(\\)")){//无参方法
            String functionName = fieldName.substring(0,fieldName.indexOf("("));
            Method function  = object.getClass().getDeclaredMethod(functionName);
            return function.invoke(object);
        }else if(fieldName.matches("[^\\(\\)]+\\([^\\(\\)]+\\)")){//有参方法
            String functionName = fieldName.substring(0,fieldName.indexOf("("));
            String funFieldName = fieldName.substring(fieldName.indexOf("(")+1,fieldName.indexOf(")"));
            Field field  = object.getClass().getDeclaredField(funFieldName);
            Object val = getValueFromObject(funFieldName,object);
            Method function  = object.getClass().getDeclaredMethod(functionName,field.getType());
            return function.invoke(object,val);
        }else if(fieldName.matches("[a-zA-Z0-9]+\\.[a-zA-Z0-9]+")){//common.name
           /* String secend = fieldName.split(".")[1];
            if(object.getClass().getDeclaredField(fieldName.split(".")[0]).getType() == java.util.List.class){//是list类型
                Type genericType = object.getClass().getDeclaredField(fieldName.split(".")[0]).getGenericType();
                if(genericType != null) {
                    // 如果是泛型参数的类型
                    if(genericType instanceof ParameterizedType){
                        ParameterizedType pt = (ParameterizedType) genericType;
                        //得到泛型里的class类型对象
                        Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
                        if(genericClazz==HashMap.class){//是hashMap

                        }else{
                            return getValueFromObject();
                        }
                    }
                }

            }else{

            }*/
        }else{//属性
            if(object instanceof Map){ //如果是Map
                return ((Map) object).get(fieldName);
            }else{
                try {
                    Field field = object.getClass().getDeclaredField(fieldName);
                    if(field!=null){
                        field.setAccessible(true);
                        return field.get(object);
                    }
                }catch (NoSuchFieldException e){
                    Field field = object.getClass().getSuperclass().getDeclaredField(fieldName);
                    if(field!=null){
                        field.setAccessible(true);
                        return field.get(object);
                    }
                }

            }
        }
        return null;
    }



    public static Object fromMap(HashMap<String,Object> map , Class clazz) throws Exception{

        Object object = null;
        try {
            object    = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            field.setAccessible(true);
            ExcelAssistant excelAssistant = (ExcelAssistant)field.getAnnotation(ExcelAssistant.class);
            if(excelAssistant==null)continue;
            Object value = map.get(excelAssistant.titleName());
            try {
                if(value!=null){
                    try{
                        clazz.getDeclaredMethod(field.getName(),Object.class).invoke(object,value);
                    }catch(NoSuchMethodException e){
                        field.set(object,value);
                    }catch(InvocationTargetException e){
                        e.printStackTrace();
                    }
                }else{
                    if(field.getType()== BigDecimal.class){
                        field.set(object,new BigDecimal("0.0"));
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new Exception("反射出现错误，错误原因为："+value +"  字段名为："+field.getName());
            }
        };
        return object;
    }
}
