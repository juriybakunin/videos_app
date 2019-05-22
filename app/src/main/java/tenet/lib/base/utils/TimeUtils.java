package tenet.lib.base.utils;


import android.annotation.SuppressLint;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Константы и методы для работы со временем */
public class TimeUtils {

    /** Количество секунд в часе */
    public static final int HOUR_SEC = 60*60;

    /** Количество секунд в сутках */
    public static final int DAY_SEC = 24*HOUR_SEC;

    /** Количество милисекунд в сутках */
    public static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final char NO_DELIM = (char)0;

    private static final int [] TEXT_DATE_FIELDS = {
            Calendar.YEAR,
            Calendar.MONTH,
            Calendar.DAY_OF_MONTH,
            Calendar.HOUR_OF_DAY,
            Calendar.MINUTE,
            Calendar.SECOND
    };

    public static String getDateText(Calendar calendar) {
        return getDateText(calendar,true,'-');
    }

    /** Форматирует дату в формате "год-месяц-день-час-минуты-секунды"
     * @param calendar Нужное время
     * @param printSeconds true - печатать секунды
     * @param delim разделитель полей
     * @return Возвращает отформатированную дату
     */
    public static String getDateText(Calendar calendar,boolean printSeconds,char delim) {
        StringBuilder sb = new StringBuilder();
        int lastField = printSeconds?Calendar.SECOND:Calendar.MINUTE;
        for (int field:TEXT_DATE_FIELDS){
            char d = field == lastField? NO_DELIM : delim;
            int val = calendar.get(field);
            if(field == Calendar.MONTH)
                ++val;
            addTimeInt(sb,val,d);
            if(d == NO_DELIM)
                break;
        }
        return sb.toString();
    }


    public static Calendar getCalendarFromTextDate(String textDate,Calendar reusableCalendar) {
        return getCalendarFromTextDate(textDate,"-",reusableCalendar);
    }

    /** Возвращает календарь для даты в формате "год-месяц-день-час-минуты-секунды"
     * @param textDate Дата. Компоненты времени не обязательны
     * @param delim Разделитель для функции {@link String#split(String)}
     * @param reusableCalendar Календарь, куда запишется дата. null - для нового объекта
     * @return Возвращает календарь
     */
    public static Calendar getCalendarFromTextDate(String textDate,String delim,Calendar reusableCalendar) {
        reusableCalendar = reusableCalendar == null? new GregorianCalendar() : reusableCalendar;
        String fields[] = textDate.split(delim);
        for (int i = 0; i<TEXT_DATE_FIELDS.length; i++) {
            int val = 0;
            int field = TEXT_DATE_FIELDS[i];
            if(i<fields.length) {
                String sval = fields[i];
                val = Integer.valueOf(sval);
                if(field == Calendar.MONTH)
                    --val;
            }
            reusableCalendar.set(field,val);
        }
        return reusableCalendar;
    }

    private static StringBuilder addTimeInt(StringBuilder sb, int val, char delim) {
        if(val<10)
            sb.append(0);
        sb.append(val);
        if(delim != NO_DELIM)
            sb.append(delim);
        return sb;
    }

    public static StringBuilder getHourMinuteText(long millis, StringBuilder stringBuilder) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        return getHourMinuteText(cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),stringBuilder);
    }

    public static StringBuilder getHourMinuteSecondText(long millis, StringBuilder stringBuilder) {
        Calendar cal = calendar(millis);
        stringBuilder = stringBuilder == null? new StringBuilder():stringBuilder;
        addTimeInt(stringBuilder,cal.get(Calendar.HOUR_OF_DAY),':');
        addTimeInt(stringBuilder,cal.get(Calendar.MINUTE),':');
        return addTimeInt(stringBuilder,cal.get(Calendar.SECOND),NO_DELIM);
    }

    public static StringBuilder getHourMinuteText(int hour, int minutes, StringBuilder stringBuilder) {
        stringBuilder = stringBuilder == null? new StringBuilder():stringBuilder;
        addTimeInt(stringBuilder,hour,':');
        return addTimeInt(stringBuilder,minutes,NO_DELIM);
    }

    /** Возвращает дату в формате год-месяц-день */
    public static StringBuilder getDayText(long time,StringBuilder stringBuilder) {
        return getDayText(calendar(time),stringBuilder);
    }

    public static long getNextMinuteStart(long time){
        Calendar cal = TimeUtils.calendar(time);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal.getTimeInMillis() + TimeUnit.MINUTES.toMillis(1);
    }

    public static GregorianCalendar calendar(long millis) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        return cal;
    }

    public static boolean isSameDay(Calendar cal1,Calendar cal2) {
        for (int field:TEXT_DATE_FIELDS) {
            if(field >= Calendar.HOUR)
                break;
            if(cal1.get(field) != cal2.get(field))
                return false;
        }
        return true;
    }


    /** Возвращает значение "год-месяц-день" для заданной даты */
    public static StringBuilder getDayText(Calendar cal,StringBuilder stringBuilder) {
        stringBuilder = stringBuilder == null? new StringBuilder():stringBuilder;
        addTimeInt(stringBuilder,cal.get(Calendar.YEAR),'-');
        addTimeInt(stringBuilder,cal.get(Calendar.MONTH) + 1,'-');
        addTimeInt(stringBuilder, cal.get(Calendar.DAY_OF_MONTH),NO_DELIM);
        return stringBuilder;
    }

    public static void setLocale(Locale locale){
        USER_FORMAT = new SimpleDateFormat(USER_FORMAT_PATTERN,locale);
    }
    private static final String USER_FORMAT_PATTERN = "EE d MMMM";
    private static SimpleDateFormat USER_FORMAT = new SimpleDateFormat(USER_FORMAT_PATTERN);

    /** Возвращает значение вроде Пт 16 Ноября"" для заданной даты */
    public static String getDayTextUser(Calendar cal) {
        return getDayTextUser(cal.getTimeInMillis());
    }

    /** Возвращает значение вроде Пт 16 Ноября"" для заданной даты */
    @SuppressLint("SimpleDateFormat")
    public static String getDayTextUser(long millis) {
        return USER_FORMAT.format(new Date(millis));
    }

    public static StringBuilder getTimeText(long time,boolean seconds,StringBuilder stringBuilder) {
        return getTimeText(calendar(time),seconds,stringBuilder);
    }

    public static StringBuilder getTimeText(Calendar cal,boolean seconds,StringBuilder stringBuilder) {
        int range = cal.get(Calendar.HOUR_OF_DAY)*HOUR_SEC
                +cal.get(Calendar.MINUTE)*60
                +cal.get(Calendar.SECOND);
        return getTimeRangeText(range,seconds,stringBuilder);
    }


    public static StringBuilder getTimeRangeText(int delta, StringBuilder stringBuilder) {
        return getTimeRangeText(delta,true,stringBuilder);
    }

    public static String getHourMinute(int sec){
        int h = sec / HOUR_SEC;
        int min = (sec % HOUR_SEC)/60;
        String ret = ""+h+":";
        if(min<10)
            ret+='0';
        ret+=min;
        return ret;
    }

    public static int parsePtDuration(String ptDuration){
        String result = ptDuration.replace("PT","").replace("H",":").replace("M",":").replace("S","");
        String items[]=result.split(":");
        int dur = 0;
        if(items.length >0)
            dur += Integer.decode(items[items.length-1]);
        if(items.length >1)
            dur += Integer.decode(items[items.length-2])*60;
        if(items.length >2)
            dur += Integer.decode(items[items.length-3])*3600;
        return dur;
    }

    public static StringBuilder getTimeRangeText(int delta, boolean seconds, StringBuilder stringBuilder) {
        return getTimeRangeText(delta,seconds,true,stringBuilder);
    }

    /** Возвращает текст времени в виде "часы:минуты:секунды"
         * @param delta Время в секундах
         * @param stringBuilder Билдер, в который помещает значение.
         * @return Возвращает созданный или переданный билдер
         */
    public static StringBuilder getTimeRangeText(int delta, boolean seconds, boolean zeroes, StringBuilder stringBuilder) {
        stringBuilder = stringBuilder == null? new StringBuilder():stringBuilder;
        int hours = delta/HOUR_SEC;
        int minutes = delta%HOUR_SEC;
        int sec = minutes%60;
        minutes = minutes/60;
        if(zeroes && hours > 0)
            addTimeInt(stringBuilder,hours,':');
        addTimeInt(stringBuilder,minutes,seconds?':':NO_DELIM);
        if(seconds)
            addTimeInt(stringBuilder,sec,NO_DELIM);
        return stringBuilder;
    }

    /** Возвращает календарь, выставленный на старт следующего дня */
    public static Calendar getNextDayStart(long time){
        long d = time + TimeUnit.DAYS.toMillis(1);
        Calendar c = TimeUtils.calendar(d);
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c;
    }


}
