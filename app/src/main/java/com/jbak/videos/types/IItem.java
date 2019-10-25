package com.jbak.videos.types;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.jbak.videos.VideoUrlInterceptor;
import org.jetbrains.annotations.NotNull;
import tenet.lib.base.Interfaces;
import tenet.lib.base.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public interface IItem extends Interfaces.IdNamed {
    int CONTINUE = 1;
    int INTERCEPTED = 2;
    int BLOCK_ONCE = 3;

    int LOAD_EVENT_START = 1;
    int LOAD_EVENT_PAGE_STARTED = 2;
    int LOAD_EVENT_PAGE_FINISHED = 3;


    String ID = "[ID]";
    String URL = "[URL]";
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
        public ItemList() {}
        public <T extends IItem> ItemList(T[] items){
            Collections.addAll(this, items);
        }
        @Override
        public int getCount() {
            return size();
        }

        @Override
        public IItem getItem(int pos) {
            return get(pos);
        }
        public int indexById(String id) {
            return Utils.indexById(id, this);
        }
        public int deleteById(String id) {
            int i = indexById(id);
            if(i < 0)
                return -1;
            remove(i);
            return i;
        }

    }


/** Элемент, который содержит несколько источников контента */
    interface INextMedia {
        @Nullable Media getNextMedia(Media media);
    }

/** Элемент, умеющий доставать ссылку из вебстраницы, которая загружается по адресу getStartUrl()*/
    interface IResourceIntercept  {

        void onWebViewEvent(int event, @Nullable String url, @NonNull VideoUrlInterceptor interceptor);
        int interceptResource(@NonNull Uri resUri, @NonNull WebResourceRequest request, @NonNull VideoUrlInterceptor interceptor);
    }

/** Элемент, умеющий загружать ссылку на контент */
    interface IVideoUrlLoader {
        @Nullable String loadVideoUrlSync() throws Throwable;
    }



}
