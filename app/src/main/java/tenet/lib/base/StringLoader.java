package tenet.lib.base;

import android.os.AsyncTask;

import tenet.lib.base.utils.NetUtils;

/** Простой асинхронный загрузчик данных в виде строки */
public class StringLoader extends AsyncTask<Void,Void,String> {
    private final OnStringDataLoaded mListener;
    private final String mUrl;

    public StringLoader(String url, OnStringDataLoaded listener){
        mListener = listener;
        mUrl = url;
    }

    /** Стартует новую загрузку строковых данных
     * @param url Ссылка для загрузки
     * @param listener Обработчик полученных данных
     * @return Возвращает созданный загрузчик
     */
    public static StringLoader start(String url, OnStringDataLoaded listener){
        StringLoader loader = new StringLoader(url,listener);
        loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return loader;
    }

    protected String loadData() throws Throwable {
        return NetUtils.loadUrl(mUrl);
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            String s = loadData();
            return s;
        }
        catch (Throwable err){
            MyLog.err(err);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String data) {
        super.onPostExecute(data);
        MyLog.log("Data loaded: "+data);
        if(mListener != null)
            mListener.onStringDataLoaded(data);
    }

    /** Обработчик загрузки строки */
    public interface OnStringDataLoaded {
        void onStringDataLoaded(String data);
    }
}
