package tenet.lib.base.utils;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.jbak.videos.types.IItem;
import tenet.lib.base.Interfaces;
import tenet.lib.base.MyLog;
import tenet.lib.base.TenetApp;

public class Utils {


    public static boolean isUIThread(){
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /** Преобразует значение в dp в пиксели */
    public static int dpToPx(int dp) {
        float density = TenetApp.res().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /** Возвращает первое вложенное View заданного класса в иерархии view
     * @param view Родительское view
     * @param clazz Класс, по которому выполняется поиск (без instance of)
     * @return Возвращает найденное view или null
     */
    public static <T extends View>  T findViewByClass(View view, Class<T> clazz){
        return findViewByClass(view,clazz,false);
    }

    /** Возвращает первое вложенное View заданного класса в иерархии view
     * @param view Родительское view
     * @param clazz Класс, по которому выполняется поиск
     * @param checkInstanceOf false - ищем строго заданный класс, true - также по instance of
     * @return Возвращает найденное view или null
     */
    @SuppressWarnings("unchecked")
    public static <T extends View>  T findViewByClass(View view, Class<T> clazz, boolean checkInstanceOf){
        if(view == null)
            return null;
        if(view.getClass() == clazz || checkInstanceOf && clazz.isInstance(view))
            return (T) view;
        if(!(view instanceof ViewGroup))
            return null;
        ViewGroup vg = (ViewGroup) view;
        for (int i=0;i<vg.getChildCount();i++){
            View v = vg.getChildAt(i);
            T ret = findViewByClass(v,clazz,checkInstanceOf);
            if(ret != null)
                return ret;
        }
        return null;
    }

    /** Возвращает индекс следующего/предыдущего элемента по кругу.
     *  Если текущий элемент 0 - вернет индекс последнего элемента и наоборот
     * @param next true - индекс след элемента
     * @param index текущий индекс
     * @param len Общее количество элементов
     * @return Индекс или -1, если элементов < 2
     */
    public static int getNextPreviousIndex(boolean next, int index, int len) {
        return getNextPreviousIndex(next,index,len,true);
    }

    public static <T extends Interfaces.IdNamed> T getNextPreviousItem(boolean next, String id, List<T> list) {
        return getNextPreviousItem(next,id,list,true);
    }
    public static <T extends Interfaces.IdNamed> T getNextPreviousItem(boolean next, String id, List<T> list, boolean circleMove) {
        if(list == null)
            return null;
        int curIndex = indexById(id,list);
        if(curIndex < 0)
            return null;
        int index = getNextPreviousIndex(next, curIndex, list.size(),circleMove);
        return index < 0? null : list.get(index);
    }

    public static int strToInt(String s, int defVal){
        if(TextUtils.isEmpty(s))
            return defVal;
        s = s.trim();
        while (s.length() > 0 && s.charAt(0) == '0')
            s = s.substring(1);
        try {
            return Integer.decode(s);
        } catch (Throwable e){
            MyLog.log("Bad duration:"+s);
            MyLog.err(e);
        }
        return defVal;
    }

    /** Возвращает индекс следующего/предыдущего элемента.
     *  Если текущий элемент 0 - вернет индекс последнего элемента и наоборот
     * @param next true - индекс след элемента
     * @param index текущий индекс
     * @param len Общее количество элементов
     * @param circleMove true - используется перемещение по кругу (с последнего на первый элемент)
     * @return Индекс или -1, если элементов < 2
     */
    public static int getNextPreviousIndex(boolean next, int index, int len, boolean circleMove) {
        if(index <0 || len<2)
            return -1;
        if(next){
            index++;
            if(index >= len) {
                if(circleMove)
                    index = 0;
                else
                    return -1;
            }
        } else {
            index--;
            if(index<0) {
                if(circleMove)
                    index = len - 1;
                else
                    return -1;
            }
        }
        return index;
    }


    public static boolean intHas(int val, int flag){
        return (val & flag) > 0;
    }

    public static int intSet(int value, int flag,boolean set) {
        if(set && (value & flag) == 0) {
            value = value | flag;
        }
        if(!set && (value & flag) > 0) {
            value = value ^ flag;
        }
        return value;
    }

    public static boolean isDpadEvent(int keycode){
        return isKeyEnter(keycode)
                ||keycode == KeyEvent.KEYCODE_DPAD_LEFT
                ||keycode == KeyEvent.KEYCODE_DPAD_RIGHT
                ||keycode == KeyEvent.KEYCODE_DPAD_UP
                ||keycode == KeyEvent.KEYCODE_DPAD_DOWN;

    }

    public static boolean isDpadEvent(KeyEvent keyEvent){
        return isKeyEnter(keyEvent)
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN;

    }

    public static boolean isKeyEnter(KeyEvent keyEvent){
        return isKeyEnter(keyEvent.getKeyCode());
    }

    public static boolean isKeyEnter(int keycode){
        return keycode == KeyEvent.KEYCODE_DPAD_CENTER||
                keycode == KeyEvent.KEYCODE_ENTER;
    }

    public static boolean isKeyUp(KeyEvent event){
        return  event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP;
    }

    public static boolean isKeyDown(KeyEvent event){
        return  event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN;
    }

    public static boolean isKeyRight(KeyEvent event){
        return  event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT;
    }

    public static boolean isKeyLeft(KeyEvent event){
        return  event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT;
    }



    public static boolean isKeyNext(KeyEvent keyEvent){
        return keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_TAB
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_EQUALS
                ||keyEvent.getScanCode() == 208 && keyEvent.getKeyCode() == KeyEvent.KEYCODE_UNKNOWN;
    }

    public static boolean isKeyPrevious(KeyEvent keyEvent){
        return keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_REWIND
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_MINUS
                ||keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    }

    public static Spannable smallTextAtStart(String start, String other,float szStart) {
        Spannable span = new SpannableString(start+other);
        span.setSpan(new RelativeSizeSpan(szStart),0,start.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static SpannableStringBuilder addBoldValue(SpannableStringBuilder sb, String val) {
        int start = sb.length();
        sb.append(val);
        sb.setSpan(new StyleSpan(Typeface.BOLD),start,sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }


    public static CharSequence decodeHtml(String in) {
        if(Build.VERSION.SDK_INT >= 24)
            return Html.fromHtml(in,Html.FROM_HTML_MODE_COMPACT);
        else
            return Html.fromHtml(in);
    }

    public static String stringWithChars(char ch, int len){
        char [] ar = new char[len];
        Arrays.fill(ar,ch);
        return String.valueOf(ar);
    }

    public static final String getStackString(Throwable e)
    {
        if(e==null)
            e = new Exception();
        StringBuilder msg = new StringBuilder();
        msg.append(e.toString());
        StackTraceElement st[] = e.getStackTrace();
        for(StackTraceElement s:st) {
            msg.append('\n').append(s.toString());
        }
        Throwable cause = e.getCause();
        if(cause!=null&&msg.length()<16000)
            msg.append('\n').append("CAUSED BY").append('\n').append(getStackString(cause));
        return msg.toString();
    }

    public static int arrayIndex(Object array, Object element){
        int len = Array.getLength(array);
        for(int i=0; i < len; i++){
            Object item = Array.get(array,i);
            if(element == null){
                if(item == null)
                    return i;
            } else if (element.equals(item)){
                return i;
            }
        }
        return -1;
    }

    public static <T extends Interfaces.IdNamed> T itemById(String id,Iterable<T> items){
        for (T item:items){
            if(id.equals(item.getId()))
                return item;
        }
        return null;
    }

    public static <T extends Interfaces.IdNamed> T itemById(String id,T[] items){
        for (T item:items){
            if(id.equals(item.getId()))
                return item;
        }
        return null;
    }

    public static boolean isId(IItem item, String id){
        if(item == null || id == null)
            return false;
        return item.getId().equals(id);
    }

    public static int indexById(String id, Iterable<? extends Interfaces.IdNamed> items){
        int pos = 0;
        for (Interfaces.IdNamed item:items){
            if(id.equals(item.getId()))
                return pos;
            ++pos;
        }
        return -1;
    }

    public static boolean startsWithNoCase(String str, String prefix){
        if(TextUtils.isEmpty(str) || str.length() < prefix.length())
            return false;
        return str.substring(0,prefix.length()).equalsIgnoreCase(prefix);
    }
}
