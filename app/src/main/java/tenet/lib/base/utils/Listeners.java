package tenet.lib.base.utils;

import java.util.ArrayList;
import java.util.List;

/** Список обработчиков некоторого события
 * @param <LISTENER> Тип обработчика
 */
public class Listeners<LISTENER>{

    protected List<LISTENER> mList = new ArrayList<>();

    public List<LISTENER> getList() {
        return mList;
    }

    public boolean isEmpty() {
        return getList().isEmpty();
    }

    public void registerListener(LISTENER listener){
        if(listener != null && mList.indexOf(listener) < 0)
            mList.add(listener);
    }

    public void unregisterListener(LISTENER listener){
        mList.remove(listener);
    }

}
