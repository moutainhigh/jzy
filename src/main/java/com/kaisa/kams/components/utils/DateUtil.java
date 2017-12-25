package com.kaisa.kams.components.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dominic on 2016/11/8.
 */
public class DateUtil {

    public static String getTimeStamp(){
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(currentTime);
    }

    /**
     * 获取几天前的时间
     * @param d
     * @param day
     * @return
     */
    public static Date getDateBefore(Date d, int day){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    /**
     * 获取几天后的时间
     * @param d
     * @param day
     * @return
     */
    public static Date getDateAfter(Date d, int day){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

    /**
     * 日期差
     * @param beginDateStr
     * @param endDateStr
     * @return
     */
    public static long getDaySub(String beginDateStr,String endDateStr)
    {
        long day=0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate;
        Date endDate;
        try
        {
            beginDate = format.parse(beginDateStr.toString());
            endDate= format.parse(endDateStr.toString());
            day = (endDate.getTime()-beginDate.getTime())/(24*60*60*1000);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        return day;
    }

    /**
     * 格式化时间
     * @param date
     * @return
     */
    public static  String formatDateTimeToString(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
    public static  String formatDateToString(Date date){
        if (null!=date) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.format(date);
        }
        return "";
    }


    public static Date getTimeToDate(long time){
        Date date=null;
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String d = formatter.format(time);
           date=formatter.parse(d);
        }catch (Exception e){

        }
        return date;
    }



    public static Date getStringToTime(String timeStr){
        if(timeStr.contains("/")){
            timeStr = timeStr.replaceAll("/","-");
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public static Date getStringToDate(String dateStr){
        if(dateStr.contains("/")){
            dateStr = dateStr.replaceAll("/","-");
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }



    //5-31 加一个月应该是6-30。  1-30加一个月应该是2-28
    public static Date addMonth(Date date,int addMonthNum){
        String month30th = ",4,6,9,11,";

        int month = date.getMonth()+1+addMonthNum;
        int year = date.getYear()+1900+(month>12?1:0);
        int day = date.getDate();
        month = month%12==0?12:month%12;

        //如果日期大于28，则为29号，需要考虑是不是二月的情况
        if(day>28){
            //如果是二月，考虑是不是闰年的情况
            if(month%12==2){
                //如果是闰年，可以是29天，如果不是闰年，则只能是28天
                if(year%4==0&&year%100!=0){
                    if(day>29){
                        day  = 29;
                    }
                }else{
                    day = 28;
                }
            }else if(month30th.contains(","+month+",")){ //如果是30天
                if(day>30){
                    day = 30;
                }
            }
        }
        return getStringToDate(year+"-"+month+"-"+day);
    }


    /**
     * 根据传入参数添加天数
     * @param date
     * @param days
     * @param months
     * @param years
     * @return
     */
    public static Date addDays(Date date, int days, int months , int years) {
        try {
            if(null != date){
                Calendar cd = Calendar.getInstance();
                cd.setTime(date);
                if(0 != days){
                    cd.add(Calendar.DATE, days);//增加days天
                }
                if(0 != months){
                    cd.add(Calendar.MONTH,months);//日期加months个月
                }
                if(0 != years){
                    cd.add(Calendar.YEAR,years);//日期加years年
                }
                return cd.getTime();
            }
            return null;
        } catch (Exception e) {
            return null;
        }

    }

    public static int dayDiffToday(Date date) {
        if (null != date) {
            LocalDate today = LocalDate.now();
            return (int)(dateToLocalDate(date).toEpochDay() - today.toEpochDay()) ;
        }
        return 0;
    }

    public static int diffTwoDate(Date endDate,Date startDate) {
        if (null != endDate && null != startDate) {
            LocalDate endLocalDate = dateToLocalDate(endDate);
            LocalDate startLocalDate = dateToLocalDate(startDate);
            return (int)(endLocalDate.toEpochDay() - startLocalDate.toEpochDay()) ;
        }
        return 0;
    }

    public static long daysBetweenTowDate(Date startDate ,Date endDate){
        if (null == startDate || null == endDate) {
            return 0l;
        }
        LocalDate s = DateUtil.UDateToLocalDate(startDate);
        LocalDate e = DateUtil.UDateToLocalDate(endDate);
        long daysDiff = ChronoUnit.DAYS.between(s, e);
        return daysDiff;
    }

    public static long minutesBetweenTowDate(Date startDate ,Date endDate){
        return (endDate.getTime()- startDate.getTime())/60000+1;
    }


    private static LocalDate UDateToLocalDate(Date date ) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        LocalDate localDate = localDateTime.toLocalDate();
        return localDate;
    }

    public static LocalDate dateToLocalDate(Date date) {
        if (null != date) {
            Instant instant = date.toInstant();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return localDateTime.toLocalDate();
        }
        return null;
    }


    public static Date getPreDaysDate(int days) {
        LocalDate today = LocalDate.now();
        LocalDate preDay = today.minus(days, ChronoUnit.DAYS);
        return localDateToDate(preDay);
    }

    public static Date localDateToDate(LocalDate localDate) {
        if (null != localDate) {
            ZoneId zone = ZoneId.systemDefault();
            Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
            return Date.from(instant);
        }
        return null;
    }

    public static Date parseStringToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getPreDaysDate(1);
    }

}
