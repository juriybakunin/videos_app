package com.jbak.videos.types;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.jbak.videos.VideoUrlInterceptor;
import org.jetbrains.annotations.NotNull;
import tenet.lib.base.Interfaces;

import java.util.ArrayList;
import java.util.Iterator;

public interface IItem extends Interfaces.IdNamed {
    int CONTINUE = 1;
    int STOP_LOAD = 2;
    int BLOCK_ONCE = 3;

    int LOAD_EVENT_START = 1;
    int LOAD_EVENT_PAGE_STARTED = 2;
    int LOAD_EVENT_PAGE_FINISHED = 3;


    String ID = "[ID]";
    IItemList EMPTY_ITEM_LIST = new IItemList() {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public IItem getItem(int pos) {
            return null;
        }
    };

    @Nullable String getImageUrl();
    @Nullable String getShortDescText();

/** Список элементов */
    interface IItemList {
        int getCount();
        IItem getItem(int pos);
    }

    class ItemList extends ArrayList<IItem> implements IItemList{

        @Override
        public int getCount() {
            return size();
        }

        @Override
        public IItem getItem(int pos) {
            return get(pos);
        }
    }

/** Элемент, содержащий контент в виде ссылки */
    interface IUrlItem extends IItem{
    }



/** Элемент, который содержит несколько источников контента */
    interface INextUrl {
        @Nullable String getNextUrl(String url);
    }

/** Элемент, умеющий доставать ссылку из вебстраницы, которая загружается по адресу getStartUrl()*/
    interface IResourceIntercept extends IUrlItem {

        void onWebViewEvent(int event, String url, VideoUrlInterceptor interceptor);
        int interceptResource(@NonNull Uri resUri, WebResourceRequest request, VideoUrlInterceptor interceptor);
    }

/** Элемент, умеющий загружать ссылку на контент */
    interface IVideoUrlLoader {
        @Nullable String loadVideoUrlSync() throws Throwable;
    }


    class ItemIterator implements Iterator<IItem>, Iterable<IItem>{
        public ItemIterator(@NotNull IItemList list){
            this.items = list;
        }
        @NonNull IItemList items;
        int pos = -1;
        @Override
        public boolean hasNext() {
            return pos +1 < items.getCount();
        }

        public void start(){
            pos = -1;
        }

        @Override
        public IItem next() {
            ++ pos;
            return items.getItem(pos);
        }

        @NonNull
        @Override
        public Iterator<IItem> iterator() {
            return this;
        }
    }
}
