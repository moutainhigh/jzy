package com.kaisa.kams.components.utils.excelUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhouchuang on 2017/4/28.
 */
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target({ElementType.FIELD})//属性范围
public  @interface Condition {
    String condition() default "=";
    String sql() default "{}";
}
