package tenet.lib.base.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewUtils {

    public static void updateVisibleItems(LinearLayoutManager manager, RecyclerView.Adapter adapter){
        int start = manager.findFirstVisibleItemPosition();
        int last = manager.findLastVisibleItemPosition();
        int count = last-start+1;
        if(start < 0 || count == 0)
            return;
        adapter.notifyItemRangeChanged(start, count);
    }

    public static void updateVisibleItems(RecyclerView recyclerView){

        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if(adapter == null)
            return;
        int start = 0;
        int last = 0;
        if(lm instanceof LinearLayoutManager) {
            LinearLayoutManager manager = (LinearLayoutManager) lm;
            start = manager.findFirstVisibleItemPosition();
            last = manager.findLastVisibleItemPosition();
        }
        int count = last-start+1;
        if(start < 0 || count <= 0)
            return;
        adapter.notifyItemRangeChanged(start, count);
    }

    public static boolean removeFromParent(View v){
        if(v.getParent() instanceof ViewGroup){
            ((ViewGroup)v.getParent()).removeView(v);
            return true;
        }
        return false;
    }
}
