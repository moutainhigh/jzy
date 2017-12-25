package com.kaisa.kams.components.utils;

import org.apache.commons.lang.StringUtils;

/**
 * 关键信息脱敏
 * Created by liuwen01 on 2017/6/30.
 */
public class CoverUtil {
    //姓名
    public  static String coverName(String name){
        if(StringUtils.isNotEmpty(name)){
            return name.trim().substring(0,1) + createAsterisk(name.length() - 1);
        }
        return null;
    }

    //身份证
    public  static String coverIdNumber(String idNumber){
        String result = null;
        if (StringUtils.isNotEmpty(idNumber)){
            if(idNumber.length() ==18){
                result = idNumber.replaceAll("(\\d{6})\\d{8}(\\w{4})","$1********$2");

            }else if(idNumber.length()==19){
                result = idNumber.replaceAll("(\\d{6})\\d{9}(\\w{4})","$1*********$2");
            }
            else {
                result = createAsterisk(idNumber.length()-4) + idNumber.substring(idNumber.length()-4,idNumber.length());
            }
        }

        return  result;
    }

    //手机
    public  static String coverPhone(String phone){
        if(StringUtils.isNotEmpty(phone)){
            return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
        }
        return null;
    }

    //生成很多个*号
    public static String createAsterisk(int length) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            stringBuffer.append("*");
        }
        return stringBuffer.toString();
    }


    public static void main(String args[]){
        System.out.print(coverName(" 第一次"));
        System.out.print(coverIdNumber("430321199013122712"));
        System.out.print(coverPhone("13128824821"));

//        String email = "abcdfefabc@gamil.com";
//        String regex = "(\\w{3})(\\w+)(\\w{3})";
//        String phone="13128824821";
//        String idCard="430321199013122712";
//        String a = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
//        String b = idCard.replaceAll("(\\d{6})\\d{8}(\\w{4})","$1********$2");
//        System.out.println(a+"+++++++++++"+b);
    }
}
