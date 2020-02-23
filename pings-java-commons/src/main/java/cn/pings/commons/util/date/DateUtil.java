package cn.pings.commons.util.date;


import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *********************************************************
 ** @desc  ： 日期工具类
 ** @author  Pings
 ** @date    2020/2/17
 ** @version v1.0
 * *******************************************************
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMAT= DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_FORMAT_NO_SEPARATOR = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DATE_TIME_FORMAT_NO_SEPARATOR = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     *********************************************************
     ** @desc ：把{@link Date}转换为{@link LocalDateTime}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要转换的日期
     ** @return LocalDateTime 转换后的日期
     * *******************************************************
     */
    public static LocalDateTime toLocalDateTime(@NonNull Date date){
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     *********************************************************
     ** @desc ：把{@link Date}转换为{@link LocalDate}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要转换的日期
     ** @return LocalDate     转换后的日期
     * *******************************************************
     */
    public static LocalDate toLocalDate(@NonNull Date date){
        return toLocalDateTime(date).toLocalDate();
    }

    /**
     *********************************************************
     ** @desc ：把{@link LocalDateTime}转换为{@link Date}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要转换的日期
     ** @return Date          转换后的日期
     * *******************************************************
     */
    public static Date toDate(@NonNull LocalDateTime date){
        Instant instant =  date.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     *********************************************************
     ** @desc ：把{@link LocalDate}转换为{@link Date}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要转换的日期
     ** @return Date          转换后的日期
     * *******************************************************
     */
    public static Date toDate(@NonNull LocalDate date){
        Instant instant =  date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     *********************************************************
     ** @desc ：把{@link LocalDate}格式化为为{@link String}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要格式化的日期
     ** @return String        格式化后的字符串
     * *******************************************************
     */
    public static String format(@NonNull LocalDate date){
        return date.format(DATE_FORMAT);
    }

    /**
     *********************************************************
     ** @desc ：把{@link LocalDateTime}格式化为为{@link String}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要格式化的日期
     ** @return String        格式化后的字符串
     * *******************************************************
     */
    public static String format(@NonNull LocalDateTime date){
        return date.format(DATE_TIME_FORMAT);
    }

    /**
     *********************************************************
     ** @desc ：把{@link LocalDate}格式化为为{@link String}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要格式化的日期
     ** @return String        格式化后的字符串
     * *******************************************************
     */
    public static String formatNoSeparator(@NonNull LocalDate date){
        return date.format(DATE_FORMAT_NO_SEPARATOR);
    }

    /**
     *********************************************************
     ** @desc ：把{@link LocalDateTime}格式化为为{@link String}
     ** @author Pings
     ** @date   2020/2/17
     ** @param  date          需要格式化的日期
     ** @return String        格式化后的字符串
     * *******************************************************
     */
    public static String formatNoSeparator(@NonNull LocalDateTime date){
        return date.format(DATE_TIME_FORMAT_NO_SEPARATOR);
    }

    public static void main(String[] args) {
        System.out.println(formatNoSeparator(LocalDate.now()));
        System.out.println(formatNoSeparator(LocalDateTime.now()));
    }
}
