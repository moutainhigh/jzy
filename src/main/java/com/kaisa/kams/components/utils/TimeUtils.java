package com.kaisa.kams.components.utils;

import com.kaisa.kams.components.view.loan.Duration;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.apache.commons.lang3.time.DateUtils.addYears;

/**
 * Created by sunwanchao on 2016/12/12.
 */
public class TimeUtils {
    private static final GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();

    public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static Date offset(final Date asOfDate, final Duration duration) {
        Date result = addYears(asOfDate, duration.getYears());
        result = addMonths(result, duration.getMonths());
        result = addDays(result, duration.getDays());
        return result;
    }

    public static LocalDate offset(LocalDate asOfDate, Duration duration) {
        return asOfDate.plusDays(duration.getDays()).plusMonths(duration.getMonths()).plusYears(duration.getYears());
    }

    public static LocalDate offset(LocalDate asOfDate, int days) {
        return asOfDate.plusDays(days);
    }


    public static Date get0OClock(Date date) {
        if (date == null) {
            return null;
        }
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }


    /**
     * 时间格式化成字符串
     */
    public static String formatDate(String pattern, Date date) {
        if (date == null || StringUtils.isEmpty(pattern)) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 当前时间的年月日
     */
    public static Date format(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String dateNowStr = sdf.format(date);
        try {
            date = sdf.parse(dateNowStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 时间格式化成字符串
     */
    public static Date formatDate(String pattern, String dateStr) {
        if (StringUtils.isEmpty(pattern) || StringUtils.isEmpty(dateStr)) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(dateStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static final int daysBetween(Date early, Date late) {
        LocalDate ld1 = toLocalDate(early);
        LocalDate ld2 = toLocalDate(late);
        return (int) daysBetween(ld1, ld2);
    }

    /**
     * 获取的一个月的最大的天数
     */
    public static final int getDayOfMonth() {
        Calendar aCalendar = Calendar.getInstance(Locale.CHINA);
        int day = aCalendar.getActualMaximum(Calendar.DATE);
        return day;
    }


    public static Date getQueryStartDateTime(String submitTime) {
        try {
            if (StringUtils.isNotEmpty(submitTime)) {
                String[] arr = submitTime.split("~");
                if (null != arr && arr.length == 2) {
                    return df.parse(arr[0] + " 00:00:00");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date getQueryEndDateTime(String submitTime) {
        try {
            if (StringUtils.isNotEmpty(submitTime)) {
                String[] arr = submitTime.split("~");
                if (null != arr && arr.length == 2) {
                    return df.parse(arr[1] + " 23:59:59");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LocalDate formatDateToLocalDate(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    public static long yearsBetween(LocalDate startDate, LocalDate endDate) {
        if (null == startDate || null == endDate) {
            return 0l;
        }
        return ChronoUnit.YEARS.between(startDate, endDate);
    }
}
