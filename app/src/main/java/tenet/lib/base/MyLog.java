package tenet.lib.base;

import android.util.Log;
/** Обертка над андроид логированием.
 * Включаем лог в debug-приложении и отключаем в релизном
 */
public class MyLog {
    private static String DEF_TAG = "MyTenet";
    private static boolean WRITE_LOG = true;

/** Включает / отключает записи в logcat */
    public static void setWriteLog(boolean write){
        WRITE_LOG = write;
    }

    public static void set(String defaultTag,boolean write){
        setWriteLog(write);
        DEF_TAG = defaultTag;
    }

    public static void setDefTag(String defaultTag){
        DEF_TAG = defaultTag;
    }

/** Логирует message с тегом по умолчанию */
    public static void log(CharSequence message){
        d(DEF_TAG,message);
    }


/** Логирует message с тегом tag через Log.d */
    public static void d(String tag, CharSequence message){
        if(WRITE_LOG && message != null)
            Log.d(tag,message.toString());
    }

/** Логирует exception с тегом по умолчанию */
    public static void err(Throwable exception){
        if(WRITE_LOG)
            Log.d(DEF_TAG,"",exception);
    }

    public static void e(CharSequence message) {
        if(WRITE_LOG && message != null)
            Log.e(DEF_TAG,message.toString());
    }
}
