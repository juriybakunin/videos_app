package tenet.lib.base;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class ConnManager extends BroadcastReceiver{
    private static ConnManager INST;
    private ConnectivityManager mManager;
    List<OnConnectionChanged> mCallbacks = new ArrayList<>();
    NetworkInfo mInfo;
    public static void init(Context context){
        INST = new ConnManager();
        INST.start(context);
    }
    private void start(Context context) {
        mManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        setNetworkInfo();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this,filter);
    }
    private void setNetworkInfo(){
        mInfo = mManager.getActiveNetworkInfo();
    }
    public static ConnManager get() {
        return INST;
    }
    public void addCallback(OnConnectionChanged callback){
        mCallbacks.add(callback);
    }
    public void removeCallback(OnConnectionChanged callback) {
        mCallbacks.remove(callback);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        setNetworkInfo();
        for (OnConnectionChanged cb:mCallbacks)
            cb.onConnectionChanged(isConnected(),mInfo);
    }

    public boolean isConnected() {
        NetworkInfo ni = mManager.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
    public interface OnConnectionChanged {
        void onConnectionChanged(boolean connected,NetworkInfo networkInfo);
    }
}
