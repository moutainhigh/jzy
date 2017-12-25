package com.kaisa.kams.components.utils;

import com.kaisa.kams.models.BaseModel;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlUtil {

    public static <T extends BaseModel> SqlCallback getSqlCallback(java.lang.Class<T> clazz){
       return new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<T> list  = new ArrayList<T>();
                try {
                    while (rs.next()) {
                        T obj = clazz.newInstance();
                        SqlUtil.fromResultSet(rs, obj);
                        list.add(obj);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return list;
            }
        };
    }
    public static <T extends BaseModel> void   fromResultSet(ResultSet rs , T obj )throws Exception{
        Class currentClass = obj.getClass();
        while(BaseModel.class.isAssignableFrom(currentClass)){
            matchField(rs,obj,currentClass);
            currentClass = currentClass.getSuperclass();
        }
    }
    private static <T extends BaseModel>    void matchField(ResultSet rs ,T obj,Class clazz)throws Exception{
        for(Field field : clazz.getDeclaredFields()){
            field.setAccessible(true);
            Type type = field.getGenericType();
            try {
                if(String.class==type){
                    field.set(obj,rs.getString(field.getName()));
                }else if(Integer.class==type){
                    field.set(obj,rs.getInt(field.getName()));
                }else if(BigDecimal.class==type){
                    field.set(obj,rs.getBigDecimal(field.getName()));
                }else if(Date.class==type){
                    field.set(obj,rs.getDate(field.getName()));
                }else if(field.getType().isEnum()){
                    String name = rs.getString(field.getName());
                    for (Object constant : field.getType().getEnumConstants()){
                        if (constant.toString().equalsIgnoreCase(name)) {
                            field.set(obj,constant);
                        }
                    }
                }
            }catch (SQLException sqlE){
            }
        }
    }
}
