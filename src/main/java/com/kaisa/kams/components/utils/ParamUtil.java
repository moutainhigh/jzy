package com.kaisa.kams.components.utils;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.components.utils.excelUtil.ExcelAssistant;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Sql;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/4/28.
 */
public class ParamUtil {
    private  static  String  filters = ",start,draw,length,";
    public static Cnd getCndFromRequest(HttpServletRequest request){
        Cnd cnd = Cnd.where("1", "=", 1);
        Enumeration em = request.getParameterNames();
        while (em.hasMoreElements()) {
            String name = (String) em.nextElement();
            if(!filters.contains(","+name+",")&&!name.startsWith("columns[")&&!name.startsWith("search[")){
                String value = request.getParameter(name);
                if(!StringUtils.isEmpty(value))
                    cnd.and(name,"=",value);
            }
        }
        return cnd;
    }

    public static Cnd getCndFromRequest(HttpServletRequest request,Class clazz){
        try {
            Cnd cnd = Cnd.where("1", "=", 1);
            //从当前类里面获取匹配参数
            addCnd(request,clazz,cnd);
            //从父类获取匹配参数
            addCnd(request,clazz.getSuperclass(),cnd);
            return cnd;
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return Cnd.where("1", "=", 1);
    }

    private static Cnd addCnd(HttpServletRequest request,Class clazz,Cnd cnd ){
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            String fieldName = field.getName();
            String value = request.getParameter(fieldName);
            if(!StringUtils.isEmpty(value)){
                Annotation annotation = field.getAnnotation(Condition.class);
                if(annotation!=null){
                    Condition condition = (Condition)annotation;
                    String cnds = condition.condition();
                    String sql  = condition.sql();
                    cnd.and(fieldName,cnds,sql.replace("{}",value));
                }else{
                    cnd.and(fieldName,"=",value);
                }
            }
        });
        return cnd;
    }

    public static ParamData getParamFromRequest(HttpServletRequest request,Class clazz){
        ParamData paramData = new ParamData();
        if(StringUtils.isNotEmpty(request.getParameter("start")))
            paramData.setStart(Integer.parseInt(request.getParameter("start")));
        if(StringUtils.isNotEmpty(request.getParameter("length")))
            paramData.setLength(Integer.parseInt(request.getParameter("length")));
        if(StringUtils.isNotEmpty(request.getParameter("draw")))
            paramData.setDraw(Integer.parseInt(request.getParameter("draw")));
        paramData.setCnd(getCndFromRequest(request,clazz));
        return paramData;
    }

    public static Cnd getCndFromDataTableParam(DataTableParam param,Class clazz){
        Map<String,String>  map  = param.getSearchKeys();
        Cnd cnd = Cnd.where("1", "=", 1);
        Object object = null;
        try {
            object  = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.forEach((k,v)->{
            try {
                Field field = clazz.getDeclaredField(k);
                if(field!=null){
                    if(v!=null&&( !(v instanceof String) || StringUtils.isNotEmpty((String)v) ) ){
                        Annotation annotation = field.getAnnotation(Condition.class);
                        if(annotation!=null) {
                            Condition condition = (Condition) annotation;
                            String cnds = condition.condition();
                            String sql = condition.sql();
                            cnd.and(k, cnds, sql.replace("{}", v));
                        }else{
                            cnd.and(k,"=",v);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        return cnd;
    }
    public static void matchParam( Sql sql,DataTableParam param,Map<String,Object> addParam){
        if (null != param.getSearchKeys()) {
            Map<String, String> keys = param.getSearchKeys();
            keys.forEach((k,v)->{
                sql.setParam(k,v);
            });
        }
        if (null != addParam) {
            addParam.forEach((k,v)->{
                sql.setParam(k,v);
            });
        }
    }
}
