package tenet.lib.base;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;

/** Базовое приложение Tenet, предоставляющее набор полезных функций */
public abstract class TenetApp extends Application {
    protected static TenetApp INST;
    private String mToken;
    @Override
    public void onCreate() {
        INST = this;
        super.onCreate();
        MyLog.set(getClass().getSimpleName(),isDebug());
    }


/** Возвращает true, если приложение дебажное */
    public static boolean isDebug() {
        return (INST.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) >0;
    }

/** Возвращает строку id из ресурсов */
    public static String str(int id) {
        return INST.getString(id);
    }

/** Возвращает ресурсы приложения */
    public static Resources res() {
        return INST.getResources();
    }

    public void setToken(String token){
        mToken = token;
    }

    /** Возвращает экземпляр приложения */
    public static TenetApp getApp() {
        return INST;
    }

    public String getToken() {
        return mToken;
    }
}
